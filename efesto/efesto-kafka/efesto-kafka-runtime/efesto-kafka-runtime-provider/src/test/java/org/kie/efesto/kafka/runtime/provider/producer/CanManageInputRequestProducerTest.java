package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputRequestMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;


class CanManageInputRequestProducerTest {

    private static final long messageId = 10L;

    @Test
    public void canManageInputRequestProducerTest() {
        try (MockProducer<Long, JsonNode> canManageInputRequestProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(canManageInputRequestProducer.history()).isEmpty();
            CanManageInputRequestProducer.runProducer(canManageInputRequestProducer, new MockEfestoInputA());
            assertThat(canManageInputRequestProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = canManageInputRequestProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputRequestMessage.class);
        } catch (Exception e) {
            fail("canManageInputRequestProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = CanManageInputRequestProducer.getJsonNode(new MockEfestoInputA(), messageId);
        assertNotNull(retrieved);
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = mapper.readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputRequestMessage.class);
    }

}