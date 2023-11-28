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
package org.kie.efesto.kafka.runtime.services.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class EvaluateInputResponseProducerTest {


    @Test
    void evaluateInputResponseProducerTest() {
        EfestoOutput efestoOutput = new MockEfestoOutput();
        try (MockProducer<Long, JsonNode> evaluateInputResponseProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(evaluateInputResponseProducer.history()).isEmpty();
            EvaluateInputResponseProducer.runProducer(evaluateInputResponseProducer, efestoOutput, 10L);
            assertThat(evaluateInputResponseProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = evaluateInputResponseProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputResponseMessage.class);
        } catch (Exception e) {
            fail("evaluateInputResponseProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        EfestoOutput efestoOutput = new MockEfestoOutput();
        JsonNode retrieved = EvaluateInputResponseProducer.getJsonNode(efestoOutput, 10L);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage responseMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(responseMessage).isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputResponseMessage.class);
        assertThat(responseMessage.getKind()).isEqualTo(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTRESPONSE);
        assertThat(((EfestoKafkaRuntimeEvaluateInputResponseMessage) responseMessage).getEfestoOutput()).isEqualTo(efestoOutput);
        assertThat(((EfestoKafkaRuntimeEvaluateInputResponseMessage) responseMessage).getMessageId()).isEqualTo(10L);
    }
}