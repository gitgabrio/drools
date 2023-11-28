/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.runtime.services.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.serialization.EfestoLongDeserializer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;

public class EvaluateInputRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EvaluateInputRequestConsumer.class);

    private static final List<EfestoKafkaRuntimeEvaluateInputRequestMessage> receivedMessages = new ArrayList<>();

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private static Thread consumerThread;

    private EvaluateInputRequestConsumer() {
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
            logger.info("EvaluateInputRequestConsumer already started");
            registeredListeners.add(toRegister);
        } else {
            logger.info("Starting EvaluateInputRequestConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer();
            registeredListeners = new HashSet<>();
            registeredListeners.add(toRegister);
            startEvaluateConsumer(consumer, registeredListeners);
        }
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer, Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("starting consumer.... {}", consumer);
        final int giveUp = 100;
        try {
            consumerThread = getConsumeAndListenThread(consumer, giveUp, EvaluateInputRequestConsumer.class.getSimpleName(),
                    EvaluateInputRequestConsumer::consumeModel,
                    listeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeEvaluateInputRequestMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static EfestoKafkaRuntimeEvaluateInputRequestMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeEvaluateInputRequestMessage toReturn = getMessage(jsonNode);
            logger.info("EfestoKafkaRuntimeEvaluateInputRequestMessage: ({})\n", toReturn);
            receivedMessages.add(toReturn);
            return toReturn;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    private static EfestoKafkaRuntimeEvaluateInputRequestMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                EvaluateInputRequestConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                EfestoLongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC));
        return consumer;
    }
}
