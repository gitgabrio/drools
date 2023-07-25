package org.kie.efesto.kafka.runtime.services.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class ParseJsonInputResponseProducerTest {

    private static EfestoInput EFESTOINPUT;

    @BeforeAll
    public static void setup() {
        EFESTOINPUT = new MockEfestoInputA();
    }


    @Test
    void parseJsonInputResponseProducerTest() {
        try (MockProducer<Long, JsonNode> kieServiceNotificationProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(kieServiceNotificationProducer.history()).isEmpty();
            ParseJsonInputResponseProducer.runProducer(kieServiceNotificationProducer, EFESTOINPUT);
            assertThat(kieServiceNotificationProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = kieServiceNotificationProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputResponseMessage.class);
        } catch (Exception e) {
            fail("parseJsonInputResponseProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = ParseJsonInputResponseProducer.getJsonNode(EFESTOINPUT);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage responseMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(responseMessage).isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputResponseMessage.class);
        assertThat(responseMessage.getKind()).isEqualTo(EfestoKafkaMessagingType.RUNTIMEPARSEJSONINPUTRESPONSE);
        assertThat(((EfestoKafkaRuntimeParseJsonInputResponseMessage) responseMessage).getEfestoInput()).isEqualTo(EFESTOINPUT);
    }
}