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
package org.kie.efesto.kafka.runtime.gateway.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createConsumer;

public class KieServiceNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KieServiceNotificationConsumer.class);

    private static final List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages = new ArrayList<>();

    private static Thread consumerThread;

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private KieServiceNotificationConsumer() {
    }

    public static void removeListener(EfestoKafkaMessageListener toRemove) {
        logger.info("removeListener {}", toRemove);
        if (registeredListeners != null) {
            logger.info("Removing {}", toRemove);
            registeredListeners.remove(toRemove);
        }
    }

    public static void startEvaluateConsumer(EfestoKafkaMessageListener toRegister) {
        logger.info("startEvaluateConsumer {}", toRegister);
        if (consumerThread != null) {
            logger.info("KieServiceNotificationConsumer already started");
            registeredListeners.add(toRegister);
        } else {
            logger.info("Starting KieServiceNotificationConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer(KieServiceNotificationConsumer.class.getSimpleName(), RUNTIMESERVICE_NOTIFICATION_TOPIC);
            registeredListeners = new HashSet<>();
            registeredListeners.add(toRegister);
            startEvaluateConsumer(consumer, registeredListeners);
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

}
