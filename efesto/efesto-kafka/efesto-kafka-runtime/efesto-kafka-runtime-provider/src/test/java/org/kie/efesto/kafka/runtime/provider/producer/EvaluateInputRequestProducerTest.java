package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;


class EvaluateInputRequestProducerTest {

    private static final long messageId = 10L;

    private static String modelLocalUriIdString;
    private static String inputDataString;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        EfestoInput template = new MockEfestoInputA();
        modelLocalUriIdString = getObjectMapper().writeValueAsString(template.getModelLocalUriId());
        inputDataString = getObjectMapper().writeValueAsString(template.getInputData());
    }


    @Test
    public void evaluateInputRequestProducerTest() {
        try (MockProducer<Long, JsonNode> evaluateInputRequestProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(evaluateInputRequestProducer.history()).isEmpty();
            EvaluateInputRequestProducer.runProducer(evaluateInputRequestProducer, modelLocalUriIdString, inputDataString);
            assertThat(evaluateInputRequestProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = evaluateInputRequestProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
        } catch (Exception e) {
            fail("evaluateInputRequestProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = EvaluateInputRequestProducer.getJsonNode(modelLocalUriIdString, inputDataString, messageId);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
    }

}