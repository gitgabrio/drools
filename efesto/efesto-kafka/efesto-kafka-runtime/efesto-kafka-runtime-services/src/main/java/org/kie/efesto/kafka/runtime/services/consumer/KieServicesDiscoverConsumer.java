/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.efesto.kafka.runtime.services.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.kie.efesto.kafka.api.serialization.EfestoLongDeserializer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceDiscoverMessage;
import org.kie.efesto.kafka.runtime.services.producer.KieServiceNotificationProducer;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeLocalServiceProvider;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_DISCOVER_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndProduceThread;

public class KieServicesDiscoverConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KieServicesDiscoverConsumer.class);

    private static final KafkaRuntimeLocalServiceProvider localServiceProvider = new KafkaRuntimeLocalServiceProvider();

    private static final List<EfestoKafkaRuntimeServiceDiscoverMessage> receivedMessages = new ArrayList<>();

    private KieServicesDiscoverConsumer() {
    }

    public static void startEvaluateConsumer() {
        logger.info("starting consumer....");
        Consumer<Long, JsonNode> consumer = createConsumer();
        startEvaluateConsumer(consumer, KieServicesDiscoverConsumer::notifyServices);
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer, final java.util.function.Supplier kieServiceNotificationSupplier) {
        logger.info("starting consumer.... {}", consumer);
        final int giveUp = 100;
        try {
            Thread thread = getConsumeAndProduceThread(consumer, kieServiceNotificationSupplier, giveUp, KieServicesDiscoverConsumer.class.getSimpleName(),
                    KieServicesDiscoverConsumer::consumeModelAndProduceRecord);
            thread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeServiceDiscoverMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static Object consumeModelAndProduceRecord(ConsumerRecord<Long, JsonNode> toConsume, final java.util.function.Supplier kieServiceNotificationProducer) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeServiceDiscoverMessage notificationMessage = getMessage(jsonNode);
            logger.info("notificationMessage: ({})\n", notificationMessage);
            receivedMessages.add(notificationMessage);
            return kieServiceNotificationProducer.get();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    static int notifyServices() {
        logger.info("notifyServices");
        List<KieRuntimeService> kieRuntimeServices = localServiceProvider.getKieRuntimeServices();
        kieRuntimeServices.forEach(KieServicesDiscoverConsumer::notifyService);
        return kieRuntimeServices.size();
    }

    static void notifyService(KieRuntimeService toPublish) {
        logger.info("notifyServices {}", toPublish);
        KieServiceNotificationProducer.runProducer(toPublish);
    }

    private static EfestoKafkaRuntimeServiceDiscoverMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeServiceDiscoverMessage.class);
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                KieServicesDiscoverConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                EfestoLongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(RUNTIMESERVICE_DISCOVER_TOPIC));
        return consumer;
    }
}
