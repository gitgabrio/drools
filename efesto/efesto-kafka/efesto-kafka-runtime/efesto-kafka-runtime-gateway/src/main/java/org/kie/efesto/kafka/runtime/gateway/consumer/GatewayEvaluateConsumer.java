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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.gateway.managers.KafkaEfestoRuntimeManager;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.kie.efesto.kafka.runtime.gateway.producer.GatewayEvaluateProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.EVALUATE_TOPIC;
import static org.kie.efesto.kafka.api.ThreadUtils.getConsumeAndProduceAndListenThread;


/**
 * This receives messages from external service (i.e. not efesto)
 */
public class GatewayEvaluateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayEvaluateConsumer.class);

    private static Thread consumerThread;

    private static Set<EfestoKafkaMessageListener> registeredListeners;


    private GatewayEvaluateConsumer() {
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
            Consumer<Long, JsonNode> consumer = createConsumer();
            registeredListeners = new HashSet<>();
            registeredListeners.add(toRegister);
            startEvaluateConsumer(consumer, registeredListeners);
        }
    }

    public static void startEvaluateConsumer(Consumer<Long, JsonNode> consumer,
                                             Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("startEvaluateConsumer....");
        final int giveUp = 100;
        try {
            consumerThread = getConsumeAndProduceAndListenThread(consumer, giveUp, GatewayEvaluateConsumer.class.getSimpleName(),
                    GatewayEvaluateConsumer::consumeModel,
                    GatewayEvaluateConsumer::biFunction,
                    listeners);
            consumerThread.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static EfestoKafkaRuntimeEvaluateInputResponseMessage biFunction(ConsumerRecord<Long, JsonNode> toConsume, Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage> recordConsumer) {
        EfestoKafkaRuntimeEvaluateInputResponseMessage toReturn = (EfestoKafkaRuntimeEvaluateInputResponseMessage) recordConsumer.apply(toConsume);

        return toReturn;
    }

    static EfestoKafkaRuntimeEvaluateInputResponseMessage consumeModel(ConsumerRecord<Long, JsonNode> toConsume) {
        try {
            logger.info("Consume: ({})\n", toConsume);
            JsonNode jsonNode = toConsume.value();
            logger.info("JsonNode: ({})\n", jsonNode);
            String modelLocalUriIdString = jsonNode.get("modelLocalUriIdString").asText();
            modelLocalUriIdString = URLDecoder.decode(modelLocalUriIdString, StandardCharsets.UTF_8);
            String inputDataString = jsonNode.get("inputData").toString();
            logger.info("modelLocalUriIdString: ({})\n", modelLocalUriIdString);
            logger.info("inputDataString: ({})\n", inputDataString);
            EfestoOutput toPublish = KafkaEfestoRuntimeManager.evaluateModel(modelLocalUriIdString, inputDataString);
            logger.info("*******************************");
            logger.info("*******************************");
            logger.info("EfestoOutput: ({})\n", toPublish);
            logger.info("*******************************");
            logger.info("*******************************");
            EfestoKafkaRuntimeEvaluateInputResponseMessage toReturn = new EfestoKafkaRuntimeEvaluateInputResponseMessage(toPublish, -0L);
            GatewayEvaluateProducer.runProducer(toReturn);
            return toReturn;
        } catch (Exception e) {
            logger.error("Failed to retrieve EfestoOutput", e);
            return null;
        }
    }

    private static Consumer<Long, JsonNode> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                GatewayEvaluateConsumer.class.getSimpleName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, JsonNode> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(EVALUATE_TOPIC));
        return consumer;
    }


}
