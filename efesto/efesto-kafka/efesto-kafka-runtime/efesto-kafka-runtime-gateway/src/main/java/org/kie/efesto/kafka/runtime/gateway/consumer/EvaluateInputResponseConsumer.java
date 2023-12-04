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
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createConsumer;

public class EvaluateInputResponseConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EvaluateInputResponseConsumer.class);

    private static final List<EfestoKafkaRuntimeEvaluateInputResponseMessage> receivedMessages = new ArrayList<>();

    private static Thread consumerThread;

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private EvaluateInputResponseConsumer() {
    }

    public static void removeListener(EfestoKafkaMessageListener toRemove) {
        logger.info("removeListener {}", toRemove);
        if (registeredListeners != null) {
            logger.info("Removing {}", toRemove);
            registeredListeners.remove(toRemove);
        }
    }

    public static void startEvaluateConsumer(EfestoKafkaMessageListener toRegister) {
        logger.info("startEvaluateConsumer");
        if (consumerThread != null) {
            logger.info("EvaluateInputResponseConsumer already started");
            registeredListeners.add(toRegister);
        } else {
            logger.info("Starting EvaluateInputResponseConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer(EvaluateInputResponseConsumer.class.getSimpleName(), RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC);
            registeredListeners = new HashSet<>();
            registeredListeners.add(toRegister);
            startEvaluateConsumer(consumer, registeredListeners);
        }
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer,
                                             Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("starting consumer.... {}", consumer);
        receivedMessages.clear();
        try {
            consumerThread = getConsumeAndListenThread(consumer, EvaluateInputResponseConsumer.class.getSimpleName(),
                    EvaluateInputResponseConsumer::consumeModel,
                    listeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeEvaluateInputResponseMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static EfestoKafkaRuntimeEvaluateInputResponseMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeEvaluateInputResponseMessage toReturn = getMessage(jsonNode);
            logger.info("EfestoKafkaRuntimeEvaluateInputResponseMessage: ({})\n", toReturn);
            receivedMessages.add(toReturn);
            return toReturn;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    private static EfestoKafkaRuntimeEvaluateInputResponseMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeEvaluateInputResponseMessage.class);
    }

}
