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
package org.kie.efesto.kafka.compilation.service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.compilation.gateway.messages.EfestoKafkaCompilationSourceRequestMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.COMPILATIONSERVICE_SOURCEREQUEST_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createConsumer;

public class CompilationSourceRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CompilationSourceRequestConsumer.class);

    private static final List<EfestoKafkaCompilationSourceRequestMessage> receivedMessages = new ArrayList<>();

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private static Thread consumerThread;

    private CompilationSourceRequestConsumer() {
    }

    public static void removeListener(EfestoKafkaMessageListener toRemove) {
        logger.info("removeListener {}", toRemove);
        if (registeredListeners != null) {
            logger.info("Removing {}", toRemove);
            registeredListeners.remove(toRemove);
        }
    }

    public static void startSourceRequestConsumer(EfestoKafkaMessageListener toRegister) {
        logger.info("startEvaluateConsumer {}", toRegister);
        if (consumerThread != null) {
            logger.info("CompilationSourceRequestConsumer already started");
            registeredListeners.add(toRegister);
        } else {
            logger.info("Starting CompilationSourceRequestConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer(CompilationSourceRequestConsumer.class.getSimpleName(), COMPILATIONSERVICE_SOURCEREQUEST_TOPIC);
            registeredListeners = new HashSet<>();
            registeredListeners.add(toRegister);
            startSourceRequestConsumer(consumer, registeredListeners);
        }
    }

    public static void startSourceRequestConsumer(Consumer<Long, JsonNode> consumer, Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("starting consumer.... {}", consumer);
        try {
            consumerThread = getConsumeAndListenThread(consumer, CompilationSourceRequestConsumer.class.getSimpleName(),
                    CompilationSourceRequestConsumer::consumeModel,
                    listeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaCompilationSourceRequestMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static EfestoKafkaCompilationSourceRequestMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaCompilationSourceRequestMessage toReturn = getMessage(jsonNode);
            logger.info("EfestoKafkaCompilationSourceRequestMessage: ({})\n", toReturn);
            receivedMessages.add(toReturn);
            return toReturn;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    private static EfestoKafkaCompilationSourceRequestMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaCompilationSourceRequestMessage.class);
    }

}
