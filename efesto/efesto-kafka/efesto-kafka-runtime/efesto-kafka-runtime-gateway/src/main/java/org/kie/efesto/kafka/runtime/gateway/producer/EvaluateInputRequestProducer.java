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
package org.kie.efesto.kafka.runtime.gateway.producer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createProducer;

public class EvaluateInputRequestProducer {

    private static final Logger logger = LoggerFactory.getLogger(EvaluateInputRequestProducer.class);

    private static final AtomicLong COUNTER = new AtomicLong();


    public static long runProducer(String modelLocalUriIdString, String inputDataString) {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer(EvaluateInputRequestProducer.class.getSimpleName());
        return runProducer(producer, modelLocalUriIdString, inputDataString);
    }

    public static long runProducer(final Producer<Long, JsonNode> producer, String modelLocalUriIdString, String inputDataString) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();

        try {
            long messageId = COUNTER.incrementAndGet();
            JsonNode jsonNode = getJsonNode(modelLocalUriIdString, inputDataString, messageId);
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC, messageId, jsonNode);

            RecordMetadata metadata = producer.send(record).get();

            long elapsedTime = System.currentTimeMillis() - time;
            logger.info("sent record(key={} value={}) " +
                            "meta(partition={}, offset={}) time={}\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
            return messageId;
        } catch (Exception e) {
            throw new KieEfestoCommonException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    static JsonNode getJsonNode(String modelLocalUriIdString, String inputDataString, long messageId) {
        EfestoKafkaRuntimeEvaluateInputRequestMessage requestMessage = new EfestoKafkaRuntimeEvaluateInputRequestMessage(modelLocalUriIdString, inputDataString, messageId);
        return getObjectMapper().valueToTree(requestMessage);
    }

}
