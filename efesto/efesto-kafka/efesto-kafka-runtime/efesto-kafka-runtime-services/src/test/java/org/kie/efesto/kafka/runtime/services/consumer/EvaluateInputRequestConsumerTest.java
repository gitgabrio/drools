package org.kie.efesto.kafka.runtime.services.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeServiceLocalProvider;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.runtime.provider.service.KafkaRuntimeManagerUtils.rePopulateFirstLevelCache;
import static org.kie.efesto.kafka.runtime.services.consumer.EvaluateInputRequestConsumer.receivedMessages;
import static org.mockito.Mockito.*;


class EvaluateInputRequestConsumerTest {

    private static List<KafkaKieRuntimeService> KIERUNTIMESERVICES;

    @BeforeAll
    public static void setup() {
        KafkaRuntimeServiceProvider runtimeServiceLocal = KafkaSPIUtils.getRuntimeServiceProviders(true).stream().filter(KafkaRuntimeServiceLocalProvider.class::isInstance).findFirst().orElseThrow(() -> new RuntimeException("Failed to retrieve KafkaKieRuntimeServiceLocal"));
        KIERUNTIMESERVICES = runtimeServiceLocal.getKieRuntimeServices();
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
        rePopulateFirstLevelCache(KIERUNTIMESERVICES);
    }

    @Test
    public void evaluateInputRequestConsumerTest() throws JsonProcessingException {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition);
        MockConsumer<Long, JsonNode> evaluateInputRequestConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        EfestoKafkaMessageListener mockListener = mock(EfestoKafkaMessageListener.class);
        try {
            EvaluateInputRequestConsumer.startEvaluateConsumer(evaluateInputRequestConsumer, Collections.singleton(mockListener));
            evaluateInputRequestConsumer.updateBeginningOffsets(startOffsets);
            evaluateInputRequestConsumer.assign(Collections.singleton(topicPartition));
            evaluateInputRequestConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeEvaluateInputRequestMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            verify(mockListener, times(1)).onMessageReceived(receivedMessages.get(0));
        } catch (Exception e) {
            fail("evaluateInputRequestConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition) throws JsonProcessingException {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNodeWithoutModelLocalUriId());
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId() throws JsonProcessingException {
        EfestoInput template = new MockEfestoInputA();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(template.getModelLocalUriId());
        String inputDataString = getObjectMapper().writeValueAsString(template.getInputData());
        EfestoKafkaRuntimeEvaluateInputRequestMessage message = new EfestoKafkaRuntimeEvaluateInputRequestMessage(modelLocalUriIdString, inputDataString, 10L);
        return getObjectMapper().valueToTree(message);
    }

}