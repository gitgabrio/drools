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
package org.kie.efesto.kafka.runtime.provider.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.serialization.EfestoLongDeserializer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;

public class KieServiceNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KieServiceNotificationConsumer.class);

    private static final List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages = new ArrayList<>();

    private static Thread consumerThread;

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private KieServiceNotificationConsumer() {
    }

    public static void startEvaluateConsumer(Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("startEvaluateConsumer");
        if (consumerThread != null) {
            logger.info("KieServiceNotificationConsumer already started");
            registeredListeners.addAll(listeners);
        } else {
            logger.info("Starting KieServiceNotificationConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer();
            startEvaluateConsumer(consumer, listeners);
        }
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer,
                                             Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("starting consumer.... {}", consumer);
        final int giveUp = 100;
        receivedMessages.clear();
        try {
            registeredListeners = new HashSet<>();
            registeredListeners.addAll(listeners);
            consumerThread = getConsumeAndListenThread(consumer, giveUp, KieServiceNotificationConsumer.class.getSimpleName(),
                    KieServiceNotificationConsumer::consumeModel,
                    registeredListeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }


    static EfestoKafkaRuntimeServiceNotificationMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeServiceNotificationMessage notificationMessage = getMessage(jsonNode);
            logger.info("notificationMessage: ({})\n", notificationMessage);
            receivedMessages.add(notificationMessage);
            return notificationMessage;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    private static EfestoKafkaRuntimeServiceNotificationMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        logger.info("getMessage: ({})\n", jsonNode);
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeServiceNotificationMessage.class);
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                KieServiceNotificationConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                EfestoLongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(RUNTIMESERVICE_NOTIFICATION_TOPIC));
        return consumer;
    }
}
