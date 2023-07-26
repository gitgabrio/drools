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
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputResponseMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class CanManageInputResponseProducerTest {


    @Test
    void canManageInputResponseProducerTest() {
        try (MockProducer<Long, JsonNode> parseJsonInputResponseProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(parseJsonInputResponseProducer.history()).isEmpty();
            CanManageInputResponseProducer.runProducer(parseJsonInputResponseProducer, true, 10L);
            assertThat(parseJsonInputResponseProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = parseJsonInputResponseProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputResponseMessage.class);
        } catch (Exception e) {
            fail("canManageInputResponseProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = CanManageInputResponseProducer.getJsonNode(true, 10L);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage responseMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(responseMessage).isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputResponseMessage.class);
        assertThat(responseMessage.getKind()).isEqualTo(EfestoKafkaMessagingType.RUNTIMECANMANAGEINPUTRESPONSE);
        assertThat(((EfestoKafkaRuntimeCanManageInputResponseMessage) responseMessage).isCanManage()).isEqualTo(true);
        assertThat(((EfestoKafkaRuntimeCanManageInputResponseMessage) responseMessage).getMessageId()).isEqualTo(10L);
    }
}