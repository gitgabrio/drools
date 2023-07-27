package org.kie.efesto.kafka.runtime.provider.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC;
import static org.kie.efesto.kafka.runtime.provider.consumer.ParseJsonInputResponseConsumer.receivedMessages;
import static org.mockito.Mockito.*;


class ParseJsonInputResponseConsumerTest {

    @Test
    public void parseJsonInputResponseConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecord(topicPartition);
        MockConsumer<Long, JsonNode> parseJsonInputResponseConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        EfestoKafkaMessageListener mockListener = mock(EfestoKafkaMessageListener.class);
        try {
            ParseJsonInputResponseConsumer.startEvaluateConsumer(parseJsonInputResponseConsumer, Collections.singleton(mockListener));
            parseJsonInputResponseConsumer.updateBeginningOffsets(startOffsets);
            parseJsonInputResponseConsumer.assign(Collections.singleton(topicPartition));
            parseJsonInputResponseConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeParseJsonInputResponseMessage> receivedMessages = receivedMessages();
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
        EfestoInput originalInput = new MockEfestoInputA();
        EfestoKafkaRuntimeParseJsonInputResponseMessage toSerialize = new EfestoKafkaRuntimeParseJsonInputResponseMessage(originalInput, 10L);
        return getObjectMapper().valueToTree(toSerialize);
    }

}