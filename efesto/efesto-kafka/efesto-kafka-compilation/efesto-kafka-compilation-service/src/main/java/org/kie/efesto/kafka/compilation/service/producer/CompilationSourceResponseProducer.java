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
package org.kie.efesto.kafka.compilation.service.producer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.compilationmanager.api.model.EfestoCallableOutputModelContainer;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.kafka.compilation.gateway.messages.EfestoKafkaCompilationSourceResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.COMPILATIONSERVICE_SOURCERESPONSE_TOPIC;
import static org.kie.efesto.kafka.api.utils.KafkaUtils.createProducer;

public class CompilationSourceResponseProducer {

    private static final Logger logger = LoggerFactory.getLogger(CompilationSourceResponseProducer.class);

    private static final AtomicLong COUNTER = new AtomicLong();


    public static void runProducer(EfestoCompilationOutput toPublish, long messageId) {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer(CompilationSourceResponseProducer.class.getSimpleName());
        runProducer(producer, toPublish, messageId);
    }

    public static void runProducer(final Producer<Long, JsonNode> producer, EfestoCompilationOutput toPublish, long messageId) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();

        try {
            JsonNode jsonNode = getJsonNode(toPublish, messageId);
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(COMPILATIONSERVICE_SOURCERESPONSE_TOPIC, COUNTER.incrementAndGet(), jsonNode);
            RecordMetadata metadata = producer.send(record).get();
            long elapsedTime = System.currentTimeMillis() - time;
            logger.info("sent record(key={} value={}) " +
                            "meta(partition={}, offset={}) time={}\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
        } catch (Exception e) {
            throw new KieEfestoCommonException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    static JsonNode getJsonNode(EfestoCompilationOutput compilationOutput, long messageId) {
        if (compilationOutput instanceof EfestoCallableOutputModelContainer) {
            EfestoCallableOutputModelContainer callableOutputModelContainer = (EfestoCallableOutputModelContainer) compilationOutput;
            ModelLocalUriId modelLocalUriId = callableOutputModelContainer.getModelLocalUriId();
            EfestoKafkaCompilationSourceResponseMessage responseMessage = new EfestoKafkaCompilationSourceResponseMessage(modelLocalUriId, callableOutputModelContainer.getModelSource(), messageId);
            return getObjectMapper().valueToTree(responseMessage);
        }
        return null;
    }

}
