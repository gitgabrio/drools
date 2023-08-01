package org.kie.efesto.kafka.runtime.services.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceDiscoverMessage;
import org.kie.efesto.kafka.runtime.services.producer.KieServiceNotificationProducer;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeServiceLocalProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_DISCOVER_TOPIC;
import static org.kie.efesto.kafka.runtime.services.consumer.KieServicesDiscoverConsumer.receivedMessages;


class KieServicesDiscoverConsumerTest {

    private static List<KafkaKieRuntimeService> KIERUNTIMESERVICES;

    @BeforeAll
    public static void setup() {
        KafkaRuntimeServiceProvider runtimeServiceLocal = KafkaSPIUtils.getRuntimeServiceProviders(true).stream().filter(KafkaRuntimeServiceLocalProvider.class::isInstance).findFirst().orElseThrow(() -> new RuntimeException("Failed to retrieve KafkaKieRuntimeServiceLocal"));
        KIERUNTIMESERVICES = runtimeServiceLocal.getKieRuntimeServices();
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
    }

    @Test
    public void notificationConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_DISCOVER_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition);
        MockConsumer<Long, JsonNode> kieServicesDiscoverConsumer = new MockConsumer<Long, JsonNode>(OffsetResetStrategy.EARLIEST);
        Map<KafkaKieRuntimeService, MockProducer<Long, JsonNode>> serviceMockProducerMap = KIERUNTIMESERVICES.stream().collect(Collectors.toMap(kieRuntimeService -> kieRuntimeService,
                kieRuntimeService -> new MockProducer<>(true, new LongSerializer(), new JsonSerializer())));
        Supplier kieServiceNotificationSupplier = () -> {
            serviceMockProducerMap.forEach((kieRuntimeService, longJsonNodeMockProducer) -> KieServiceNotificationProducer.runProducer(longJsonNodeMockProducer, kieRuntimeService));
            return KIERUNTIMESERVICES.size();
        };
        try {
            KieServicesDiscoverConsumer.startEvaluateConsumer(kieServicesDiscoverConsumer, kieServiceNotificationSupplier);
            kieServicesDiscoverConsumer.updateBeginningOffsets(startOffsets);
            kieServicesDiscoverConsumer.assign(Collections.singleton(topicPartition));
            kieServicesDiscoverConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeServiceDiscoverMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            serviceMockProducerMap.values().forEach(kieServiceNotificationProducer -> assertThat(kieServiceNotificationProducer.history()).hasSize(1));
        } catch (Exception e) {
            fail("notificationConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNodeWithoutModelLocalUriId());
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId() {
        return getObjectMapper().valueToTree(new EfestoKafkaRuntimeServiceDiscoverMessage());
    }

}