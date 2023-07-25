package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;


class ParseJsonInputRequestProducerTest {

    private static final String modelLocalUriIDString = "{\"model\":\"example\",\"basePath\":\"/some-id/instances/some-instance-id\",\"fullPath\":\"/example/some-id/instances/some-instance-id\"}";
    private static final String inputDataString = "inputData";
    private static final long messageId = 10L;

    @Test
    public void discoverProducerRunTest() {
        try (MockProducer<Long, JsonNode> parseJsonInputRequestProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(parseJsonInputRequestProducer.history()).isEmpty();
            ParseJsonInputRequestProducer.runProducer(parseJsonInputRequestProducer, modelLocalUriIDString, inputDataString);
            assertThat(parseJsonInputRequestProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = parseJsonInputRequestProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().traverse(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputRequestMessage.class);
        } catch (Exception e) {
            fail("discoverProducerRunTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = ParseJsonInputRequestProducer.getJsonNode(modelLocalUriIDString, inputDataString, messageId);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputRequestMessage.class);
    }

}