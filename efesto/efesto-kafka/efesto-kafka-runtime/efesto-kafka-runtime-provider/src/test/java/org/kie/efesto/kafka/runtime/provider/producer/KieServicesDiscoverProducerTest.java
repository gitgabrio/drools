package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceDiscoverMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;


class KieServicesDiscoverProducerTest {

    @Test
    public void discoverProducerRunTest() {
        try (MockProducer<Long, JsonNode> kieServicesDiscoverProducer = new MockProducer<Long, JsonNode>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(kieServicesDiscoverProducer.history()).isEmpty();
            KieServicesDiscoverProducer.runProducer(kieServicesDiscoverProducer);
            assertThat(kieServicesDiscoverProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = kieServicesDiscoverProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().traverse(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
        } catch (Exception e) {
            fail("discoverProducerRunTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = KieServicesDiscoverProducer.getJsonNode();
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
    }

}