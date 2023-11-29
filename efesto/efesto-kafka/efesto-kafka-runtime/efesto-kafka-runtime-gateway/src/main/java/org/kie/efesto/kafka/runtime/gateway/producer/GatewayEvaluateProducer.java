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
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.EVALUATED_TOPIC;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createProducer;

public class GatewayEvaluateProducer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayEvaluateProducer.class);

    public static long runProducer(EfestoKafkaRuntimeEvaluateInputResponseMessage evaluateResponse) {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer(GatewayEvaluateProducer.class.getSimpleName());
        return runProducer(producer, evaluateResponse);
    }

    static long runProducer(final Producer<Long, JsonNode> producer, EfestoKafkaRuntimeEvaluateInputResponseMessage evaluateResponse) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();

        try {
            JsonNode jsonNode = getJsonNode(evaluateResponse);
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(EVALUATED_TOPIC, evaluateResponse.getMessageId(), jsonNode);

            RecordMetadata metadata = producer.send(record).get();

            long elapsedTime = System.currentTimeMillis() - time;
            logger.info("sent record(key={} value={}) " +
                            "meta(partition={}, offset={}) time={}\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
            return evaluateResponse.getMessageId();
        } catch (Exception e) {
            throw new KieEfestoCommonException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    static JsonNode getJsonNode(EfestoKafkaRuntimeEvaluateInputResponseMessage evaluateResponse) {
        return getObjectMapper().valueToTree(evaluateResponse);
    }

}
