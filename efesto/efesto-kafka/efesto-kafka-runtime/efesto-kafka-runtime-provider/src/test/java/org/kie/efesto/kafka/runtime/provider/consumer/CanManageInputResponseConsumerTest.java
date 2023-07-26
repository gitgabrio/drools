package org.kie.efesto.kafka.runtime.provider.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_CANMANAGEINPUTRESPONSE_TOPIC;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC;
import static org.kie.efesto.kafka.runtime.provider.consumer.CanManageInputResponseConsumer.receivedMessages;
import static org.mockito.Mockito.*;


class CanManageInputResponseConsumerTest {

    @Test
    public void canManageInputResponseConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_CANMANAGEINPUTRESPONSE_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecord(topicPartition);
        MockConsumer<Long, JsonNode> canManageInputResponseConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        EfestoKafkaMessageListener mockListener = mock(EfestoKafkaMessageListener.class);
        try {
            CanManageInputResponseConsumer.startEvaluateConsumer(canManageInputResponseConsumer, Collections.singleton(mockListener));
            canManageInputResponseConsumer.updateBeginningOffsets(startOffsets);
            canManageInputResponseConsumer.assign(Collections.singleton(topicPartition));
            canManageInputResponseConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeCanManageInputResponseMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            verify(mockListener, times(1)).onMessageReceived(receivedMessages.get(0));
        } catch (Exception e) {
            fail("notificationConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecord(TopicPartition topicPartition) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNode());
    }

    private static JsonNode getJsonNode() {
        EfestoKafkaRuntimeCanManageInputResponseMessage toSerialize = new EfestoKafkaRuntimeCanManageInputResponseMessage(true, 10L);
        return getObjectMapper().valueToTree(toSerialize);
    }

}