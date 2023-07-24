package org.kie.efesto.kafka.runtime.provider.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;
import static org.kie.efesto.kafka.runtime.provider.consumer.KieServiceNotificationConsumer.receivedMessages;


class KieServicesNotificationConsumerTest {

    @Test
    public void notificationConsumerTest() throws JsonProcessingException {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_NOTIFICATION_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition);
        MockConsumer<Long, JsonNode> kieServicesNotificationConsumer = new MockConsumer<Long, JsonNode>(OffsetResetStrategy.EARLIEST);
        try  {
            KieServiceNotificationConsumer.startEvaluateConsumer(kieServicesNotificationConsumer);
            kieServicesNotificationConsumer.updateBeginningOffsets(startOffsets);
            kieServicesNotificationConsumer.assign(Collections.singleton(topicPartition));
            kieServicesNotificationConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeServiceNotificationMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter ++;
            }
            assertThat(receivedMessages).hasSize(1);
        } catch (Exception e) {
            fail("notificationConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition) throws JsonProcessingException {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNodeWithoutModelLocalUriId());
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId() throws JsonProcessingException {
        String notificationResponse = getObjectMapper().writeValueAsString(new EfestoKafkaRuntimeServiceNotificationMessage());
        return new JsonNodeFactory(true).textNode(notificationResponse);
    }

}