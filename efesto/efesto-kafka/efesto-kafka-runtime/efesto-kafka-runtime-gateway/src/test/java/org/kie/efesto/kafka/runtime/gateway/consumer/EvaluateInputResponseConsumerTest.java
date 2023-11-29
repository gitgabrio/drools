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
package org.kie.efesto.kafka.runtime.gateway.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.runtime.gateway.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoOutput;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC;
import static org.kie.efesto.kafka.runtime.gateway.consumer.EvaluateInputResponseConsumer.receivedMessages;
import static org.mockito.Mockito.*;


class EvaluateInputResponseConsumerTest {

    @Test
    public void evaluateInputResponseConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecord(topicPartition);
        MockConsumer<Long, JsonNode> evaluateInputResponseConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        EfestoKafkaMessageListener mockListener = mock(EfestoKafkaMessageListener.class);
        try {
            EvaluateInputResponseConsumer.startEvaluateConsumer(evaluateInputResponseConsumer, Collections.singleton(mockListener));
            evaluateInputResponseConsumer.updateBeginningOffsets(startOffsets);
            evaluateInputResponseConsumer.assign(Collections.singleton(topicPartition));
            evaluateInputResponseConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeEvaluateInputResponseMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            verify(mockListener, times(1)).onMessageReceived(receivedMessages.get(0));
        } catch (Exception e) {
            fail("evaluateInputResponseConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecord(TopicPartition topicPartition) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNode());
    }

    private static JsonNode getJsonNode() {
        EfestoKafkaRuntimeEvaluateInputResponseMessage toSerialize = new EfestoKafkaRuntimeEvaluateInputResponseMessage(new MockEfestoOutput(), 10L);
        return getObjectMapper().valueToTree(toSerialize);
    }

}