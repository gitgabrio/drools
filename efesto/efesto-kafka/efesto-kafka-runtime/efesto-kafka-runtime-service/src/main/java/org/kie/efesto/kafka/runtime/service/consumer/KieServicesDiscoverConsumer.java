/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.runtime.service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeServiceDiscoverMessage;
import org.kie.efesto.kafka.runtime.service.producer.KieServiceNotificationProducer;
import org.kie.efesto.kafka.runtime.service.service.KafkaRuntimeServiceLocalProvider;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_DISCOVER_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndProduceThread;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createConsumer;

public class KieServicesDiscoverConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KieServicesDiscoverConsumer.class);

    private static final KafkaRuntimeServiceLocalProvider localServiceProvider = new KafkaRuntimeServiceLocalProvider();

    private static final List<EfestoKafkaRuntimeServiceDiscoverMessage> receivedMessages = new ArrayList<>();

    private KieServicesDiscoverConsumer() {
    }

    public static void startEvaluateConsumer() {
        logger.info("starting consumer....");
        Consumer<Long, JsonNode> consumer = createConsumer(KieServicesDiscoverConsumer.class.getSimpleName(), RUNTIMESERVICE_DISCOVER_TOPIC);
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
        List<KafkaKieRuntimeService> kieRuntimeServices = localServiceProvider.getKieRuntimeServices();
        kieRuntimeServices.forEach(KieServicesDiscoverConsumer::notifyService);
        return kieRuntimeServices.size();
    }

    static void notifyService(KafkaKieRuntimeService toPublish) {
        logger.info("notifyServices {}", toPublish);
        KieServiceNotificationProducer.runProducer(toPublish);
    }

    private static EfestoKafkaRuntimeServiceDiscoverMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeServiceDiscoverMessage.class);
    }

}
