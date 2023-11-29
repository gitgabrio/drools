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
package org.kie.efesto.kafka.runtime.gateway.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.gateway.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;


class EvaluateInputRequestProducerTest {

    private static final long messageId = 10L;

    private static String modelLocalUriIdString;
    private static String inputDataString;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        EfestoInput template = new MockEfestoInputA();
        modelLocalUriIdString = getObjectMapper().writeValueAsString(template.getModelLocalUriId());
        inputDataString = getObjectMapper().writeValueAsString(template.getInputData());
    }


    @Test
    public void evaluateInputRequestProducerTest() {
        try (MockProducer<Long, JsonNode> evaluateInputRequestProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(evaluateInputRequestProducer.history()).isEmpty();
            EvaluateInputRequestProducer.runProducer(evaluateInputRequestProducer, modelLocalUriIdString, inputDataString);
            assertThat(evaluateInputRequestProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = evaluateInputRequestProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
        } catch (Exception e) {
            fail("evaluateInputRequestProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = EvaluateInputRequestProducer.getJsonNode(modelLocalUriIdString, inputDataString, messageId);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
    }

}