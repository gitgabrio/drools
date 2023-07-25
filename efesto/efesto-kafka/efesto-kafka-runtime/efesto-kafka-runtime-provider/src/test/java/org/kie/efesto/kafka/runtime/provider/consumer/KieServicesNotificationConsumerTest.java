package org.kie.efesto.kafka.runtime.provider.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;
import static org.kie.efesto.kafka.runtime.provider.consumer.KieServiceNotificationConsumer.receivedMessages;
import static org.mockito.Mockito.*;


class KieServicesNotificationConsumerTest {

    @Test
    public void notificationConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_NOTIFICATION_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition);
        MockConsumer<Long, JsonNode> kieServicesNotificationConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        EfestoKafkaMessageListener mockListener = mock(EfestoKafkaMessageListener.class);
        try {
            KieServiceNotificationConsumer.startEvaluateConsumer(kieServicesNotificationConsumer, mockListener);
            kieServicesNotificationConsumer.updateBeginningOffsets(startOffsets);
            kieServicesNotificationConsumer.assign(Collections.singleton(topicPartition));
            kieServicesNotificationConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            verify(mockListener, times(1)).notificationMessageReceived(receivedMessages.get(0));
        } catch (Exception e) {
            fail("notificationConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNodeWithoutModelLocalUriId());
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId() {
        return getObjectMapper().valueToTree(new EfestoKafkaRuntimeServiceNotificationMessage());
    }

}