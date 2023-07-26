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
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.producer.ParseJsonInputResponseProducer;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeLocalServiceProvider;
import org.kie.efesto.runtimemanager.api.exceptions.EfestoRuntimeManagerException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndProduceThread;

public class ParseJsonInputRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ParseJsonInputRequestConsumer.class);

    private static final KafkaRuntimeLocalServiceProvider localServiceProvider = new KafkaRuntimeLocalServiceProvider();

    private static final List<EfestoKafkaRuntimeParseJsonInputRequestMessage> receivedMessages = new ArrayList<>();

    private static Thread consumerThread;

    private ParseJsonInputRequestConsumer() {
    }

    public static void startEvaluateConsumer() {
        logger.info("startEvaluateConsumer");
        if (consumerThread != null) {
            logger.info("ParseJsonInputRequestConsumer already started");
        } else {
            logger.info("Starting ParseJsonInputRequestConsumer....");
            Consumer<Long, JsonNode> consumer = createConsumer();
            startEvaluateConsumer(consumer, ParseJsonInputRequestConsumer::parseJsonInput);
        }
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer, final java.util.function.Function<EfestoKafkaRuntimeParseJsonInputRequestMessage, Boolean> parseJsonInputProducer) {
        logger.info("starting consumer.... {}", consumer);
        final int giveUp = 100;
        try {
            consumerThread = getConsumeAndProduceThread(consumer, parseJsonInputProducer, giveUp, ParseJsonInputRequestConsumer.class.getSimpleName(),
                    ParseJsonInputRequestConsumer::consumeModelAndProduceRecord);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<EfestoKafkaRuntimeParseJsonInputRequestMessage> receivedMessages() {
        return Collections.unmodifiableList(receivedMessages);
    }

    static Object consumeModelAndProduceRecord(ConsumerRecord<Long, JsonNode> toConsume, final java.util.function.Function parseJsonInputProducer) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            EfestoKafkaRuntimeParseJsonInputRequestMessage parseJsonInputRequestMessage = getMessage(jsonNode);
            logger.info("parseJsonInputRequestMessage: ({})\n", parseJsonInputRequestMessage);
            receivedMessages.add(parseJsonInputRequestMessage);
            return parseJsonInputProducer.apply(parseJsonInputRequestMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to consume %s", toConsume);
            logger.error(errorMessage, e);
            throw new EfestoRuntimeManagerException(errorMessage, e);
        }
    }

    static boolean parseJsonInput(EfestoKafkaRuntimeParseJsonInputRequestMessage requestMessage) {
        logger.info("parseJsonInput {}", requestMessage);
        Optional<EfestoInput> retrievedEfestoInput = localServiceProvider.getKieRuntimeServices().stream()
                .map(kieRuntimeService -> parseJsonInput(kieRuntimeService, requestMessage.getModelLocalUriIdString(), requestMessage.getInputDataString()))
                .findFirst();
        retrievedEfestoInput.ifPresent(efestoInput -> {
            logger.info("Going to send EfestoKafkaRuntimeParseJsonInputResponseMessage with {} {}", efestoInput, requestMessage.getMessageId());
            ParseJsonInputResponseProducer.runProducer(efestoInput, requestMessage.getMessageId());
        });

        return retrievedEfestoInput.isPresent();
    }

    public static EfestoInput parseJsonInput(KieRuntimeService toQuery, String modelLocalUriIdString, String inputDataString) {
        logger.info("parseJsonInput {} {} {}", toQuery, modelLocalUriIdString, inputDataString);
        return toQuery.parseJsonInput(modelLocalUriIdString, inputDataString);
    }

    private static EfestoKafkaRuntimeParseJsonInputRequestMessage getMessage(JsonNode jsonNode) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonNode.toString(), EfestoKafkaRuntimeParseJsonInputRequestMessage.class);
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                ParseJsonInputRequestConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                EfestoLongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC));
        return consumer;
    }
}
