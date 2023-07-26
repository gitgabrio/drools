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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.serialization.EfestoLongDeserializer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputRequestMessage;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_CANMANAGEINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndListenThread;

public class CanManageInputRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CanManageInputRequestConsumer.class);

    private static final List<EfestoKafkaRuntimeCanManageInputRequestMessage> receivedMessages = new ArrayList<>();

    private static Set<EfestoKafkaMessageListener> registeredListeners;

    private static Thread consumerThread;

    private CanManageInputRequestConsumer() {
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
            logger.info("CanManageInputRequestConsumer already started");
            registeredListeners.add(toRegister);
        } else {
            logger.info("Starting CanManageInputRequestConsumer....");
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
            consumerThread = getConsumeAndListenThread(consumer, giveUp, CanManageInputRequestConsumer.class.getSimpleName(),
                    CanManageInputRequestConsumer::consumeModel,
                    listeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeCanManageInputRequestMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static EfestoKafkaRuntimeCanManageInputRequestMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeCanManageInputRequestMessage toReturn = getMessage(jsonNode);
            logger.info("EfestoKafkaRuntimeCanManageInputRequestMessage: ({})\n", toReturn);
            receivedMessages.add(toReturn);
            return toReturn;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    private static EfestoKafkaRuntimeCanManageInputRequestMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        return mapper.readValue(jsonNode.toString(), EfestoKafkaRuntimeCanManageInputRequestMessage.class);
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                CanManageInputRequestConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                EfestoLongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(RUNTIMESERVICE_CANMANAGEINPUTREQUEST_TOPIC));
        return consumer;
    }
}
