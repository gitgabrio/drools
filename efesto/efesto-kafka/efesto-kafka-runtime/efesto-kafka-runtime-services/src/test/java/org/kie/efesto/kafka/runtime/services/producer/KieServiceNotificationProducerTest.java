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
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.kafka.runtime.provider.messages.AbstractEfestoKafkaRuntimeMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceNotificationMessage;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeServiceLocalProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class KieServiceNotificationProducerTest {

    private static KafkaKieRuntimeService KIERUNTIMESERVICE;

    @BeforeAll
    public static void setup() {
        KafkaRuntimeServiceProvider runtimeServiceLocal = KafkaSPIUtils.getRuntimeServiceProviders(true).stream().filter(KafkaRuntimeServiceLocalProvider.class::isInstance).findFirst().orElseThrow(() -> new RuntimeException("Failed to retrieve KafkaKieRuntimeServiceLocal"));
        KIERUNTIMESERVICE = runtimeServiceLocal.getKieRuntimeServices().get(0);
        assertNotNull(KIERUNTIMESERVICE);
    }


    @Test
    void notificationProducerTest() {
        try (MockProducer<Long, JsonNode> kieServiceNotificationProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer())) {
            assertThat(kieServiceNotificationProducer.history()).isEmpty();
            KieServiceNotificationProducer.runProducer(kieServiceNotificationProducer, KIERUNTIMESERVICE);
            assertThat(kieServiceNotificationProducer.history()).hasSize(1);
            ProducerRecord<Long, JsonNode> retrieved = kieServiceNotificationProducer.history().get(0);
            assertThat(retrieved).isNotNull();
            AbstractEfestoKafkaRuntimeMessage abstractEfestoKafkaRuntimeMessage = getObjectMapper().readValue(retrieved.value().toString(), AbstractEfestoKafkaRuntimeMessage.class);
            assertThat(abstractEfestoKafkaRuntimeMessage).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceNotificationMessage.class);
        } catch (Exception e) {
            fail("notificationProducerTest failed", e);
        }
    }

    @Test
    void getJsonNodeTest() throws JsonProcessingException {
        JsonNode retrieved = KieServiceNotificationProducer.getJsonNode(KIERUNTIMESERVICE);
        assertNotNull(retrieved);
        AbstractEfestoKafkaRuntimeMessage notificationMessage = getObjectMapper().readValue(retrieved.toString(), AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(notificationMessage).isExactlyInstanceOf(EfestoKafkaRuntimeServiceNotificationMessage.class);
        assertThat(notificationMessage.getKind()).isEqualTo(EfestoKafkaMessagingType.RUNTIMESERVICENOTIFICATION);
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) notificationMessage).getModel()).isEqualTo(KIERUNTIMESERVICE.getModelType());
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) notificationMessage).getEfestoClassKey()).isEqualTo(KIERUNTIMESERVICE.getEfestoClassKeyIdentifier());
    }
}