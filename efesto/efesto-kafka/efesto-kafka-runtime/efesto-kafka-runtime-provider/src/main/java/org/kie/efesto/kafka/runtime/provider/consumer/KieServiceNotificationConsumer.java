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
import org.kie.efesto.kafka.api.serialization.EfestoLongDeserializer;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeThread;

public class KieServiceNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KieServiceNotificationConsumer.class);

    private static final List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages = new ArrayList<>();

    private KieServiceNotificationConsumer() {
    }

    public static void startEvaluateConsumer() {
        logger.info("starting consumer....");
        Consumer<Long, JsonNode> consumer = createConsumer();
        startEvaluateConsumer(consumer);
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer) {
        logger.info("starting consumer.... {}", consumer);
        final int giveUp = 100;
        receivedMessages.clear();
        try {
            Thread thread = getConsumeThread(consumer, giveUp, KieServiceNotificationConsumer.class.getSimpleName(),
                    KieServiceNotificationConsumer::consumeModel);
            thread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }


    static void consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeServiceNotificationMessage notificationMessage = getMessage(jsonNode);
            logger.info("notificationMessage: ({})\n", notificationMessage);
            receivedMessages.add(notificationMessage);
//            String modelLocalUriIdString = jsonNode.get("modelLocalUriIdString").asText();
//            modelLocalUriIdString = URLDecoder.decode(modelLocalUriIdString, StandardCharsets.UTF_8);
//            String inputDataString = jsonNode.get("inputData").toString();
//            logger.info("modelLocalUriIdString: ({})\n", modelLocalUriIdString);
//            logger.info("inputData: ({})\n", inputDataString);
//            EfestoOutput retrieved = EfestoRuntimeManager.evaluateModel(modelLocalUriIdString, inputDataString);
//            logger.info("EfestoOutput: ({})\n", retrieved);
//            runProducer(toConsume.key(), retrieved);
        } catch (Exception e) {
            logger.error("Failed to consume {}", toConsume, e);
        }
    }

    private static EfestoKafkaRuntimeServiceNotificationMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        logger.info("getMessage: ({})\n", jsonNode);
        String jsonNodeString = jsonNode.asText();
        return getObjectMapper().readValue(jsonNodeString, EfestoKafkaRuntimeServiceNotificationMessage.class);
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
