package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.consumer.ParseJsonInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.producer.ParseJsonInputResponseProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.mocks.MockKieRuntimeServiceA;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC;

/**
 * This is a full IT. The class under test is inside <code>efesto-kafka-runtime-provider</code> module, but it requires
 * classes defined in the current module
 */
class KafkaKieRuntimeServiceGatewayTest {

    private static KafkaKieRuntimeServiceGateway KAFKAKIERUNTIMESERVICEGATEWAY;
    private static List<KieRuntimeService> KIERUNTIMESERVICES;

    @BeforeAll
    public static void setup() {
        KIERUNTIMESERVICES = SPIUtils.getKieRuntimeServices(true);
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
        MockKieRuntimeServiceA mockService = new MockKieRuntimeServiceA();
        KAFKAKIERUNTIMESERVICEGATEWAY = new KafkaKieRuntimeServiceGateway(mockService.getModelType(), mockService.getEfestoClassKeyIdentifier());
    }

    @Test
    void parseJsonInputExistingTest() throws JsonProcessingException {
        // TODO:
        // There is no ParseJsonInputRequestConsumer nor ParseJsonInputResponseProducer here
        // we have to mock them for the test to work

        ///
        ParseJsonInputRequestConsumer.startEvaluateConsumer();
        ModelLocalUriId modelLocalUriId = new MockEfestoInputA().getModelLocalUriId();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(modelLocalUriId);
        String inputDataString = "inputData";
        EfestoInput retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull();

    }

    private void mockSetup() {
        MockProducer<Long, JsonNode> parseJsonInputResponseProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer());
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        MockConsumer<Long, JsonNode> parseJsonInputRequestConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        Function parseJsonInputProducer = (Function<EfestoKafkaRuntimeParseJsonInputRequestMessage, Boolean>) (requestMessage) -> {
            Optional<EfestoInput> retrievedEfestoInput = KIERUNTIMESERVICES.stream()
                    .map(kieRuntimeService -> ParseJsonInputRequestConsumer.parseJsonInput(kieRuntimeService, requestMessage.getModelLocalUriIdString(), requestMessage.getInputDataString()))
                    .findFirst();
            retrievedEfestoInput.ifPresent(efestoInput -> ParseJsonInputResponseProducer.runProducer(parseJsonInputResponseProducer, efestoInput, 10L));
            return retrievedEfestoInput.isPresent();
        };
        ParseJsonInputRequestConsumer.startEvaluateConsumer(parseJsonInputRequestConsumer, parseJsonInputProducer);
    }
}