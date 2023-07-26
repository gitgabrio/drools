package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceDiscoverMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_NOTIFICATION_TOPIC;

class KafkaRuntimeServiceProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeServiceProviderTest.class.getName());


    private KafkaRuntimeServiceProvider kafkaRuntimeServiceProvider;
    private MockConsumer<Long, JsonNode> kieServicesNotificationConsumer;
    private MockProducer<Long, JsonNode> kieServicesDiscoverProducer;
    private EfestoKafkaRuntimeServiceNotificationMessage efestoKafkaRuntimeServiceNotificationMessage;

    @BeforeEach
    public void setUp() {
        kieServicesNotificationConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        kieServicesDiscoverProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer());
        kafkaRuntimeServiceProvider = new KafkaRuntimeServiceProvider(kieServicesNotificationConsumer, kieServicesDiscoverProducer);
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_NOTIFICATION_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        efestoKafkaRuntimeServiceNotificationMessage = new EfestoKafkaRuntimeServiceNotificationMessage("test_model", new EfestoClassKey(List.class, String.class));
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition, 1L, efestoKafkaRuntimeServiceNotificationMessage);
        kieServicesNotificationConsumer.updateBeginningOffsets(startOffsets);
        kieServicesNotificationConsumer.assign(Collections.singleton(topicPartition));
        kieServicesNotificationConsumer.addRecord(consumerRecord);
    }

    @Test
    void getKieRuntimeServicesTest() {
        try {
            List<KieRuntimeService> kieRuntimeServices = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            int counter = 0;
            while (kieRuntimeServices.isEmpty() && counter < 10) {
                kieRuntimeServices = kafkaRuntimeServiceProvider.getKieRuntimeServices();
                Thread.sleep(100);
                counter++;
            }
            assertThat(kieRuntimeServices).hasSize(1);
        } catch (Exception e) {
            fail("getKieRuntimeServicesTest failed", e);
        }
    }

    @Test
    void notificationMessageReceivedNotNotificationTest() {
        try {
            List<KieRuntimeService> retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            int counter = 0;
            while (retrieved.isEmpty() && counter < 10) {
                retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
                Thread.sleep(100);
                counter++;
            }
            assertThat(retrieved).hasSize(1);
            AbstractEfestoKafkaMessage received = new EfestoKafkaRuntimeServiceDiscoverMessage();
            kafkaRuntimeServiceProvider.onMessageReceived(received);
            retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            assertThat(retrieved).hasSize(1);
        } catch (Exception e) {
            fail("notificationMessageReceivedNotNotificationTest failed", e);
        }
    }

    @Test
    void notificationMessageReceivedNewNotificationTest() {
        try {
            List<KieRuntimeService> retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            int counter = 0;
            while (retrieved.isEmpty() && counter < 10) {
                retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
                Thread.sleep(100);
                counter++;
            }
            assertThat(retrieved).hasSize(1);
            AbstractEfestoKafkaMessage received = new EfestoKafkaRuntimeServiceNotificationMessage("NEW_MODEL", new EfestoClassKey(String.class));
            kafkaRuntimeServiceProvider.onMessageReceived(received);
            retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            assertThat(retrieved).hasSize(2);
        } catch (Exception e) {
            fail("notificationMessageReceivedNewNotificationTest failed", e);
        }
    }

    @Test
    void notificationMessageReceivedAlreadyExistingNotificationTest() {
        try {
            List<KieRuntimeService> retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            int counter = 0;
            while (retrieved.isEmpty() && counter < 10) {
                retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
                Thread.sleep(100);
                counter++;
            }
            assertThat(retrieved).hasSize(1);
            kafkaRuntimeServiceProvider.onMessageReceived(efestoKafkaRuntimeServiceNotificationMessage);
            retrieved = kafkaRuntimeServiceProvider.getKieRuntimeServices();
            assertThat(retrieved).hasSize(1);
        } catch (Exception e) {
            fail("notificationMessageReceivedAlreadyExistingNotificationTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition, long key, EfestoKafkaRuntimeServiceNotificationMessage message) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, key, getJsonNodeWithoutModelLocalUriId(message));
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId(EfestoKafkaRuntimeServiceNotificationMessage message) {
        return getObjectMapper().valueToTree(message);
    }
}