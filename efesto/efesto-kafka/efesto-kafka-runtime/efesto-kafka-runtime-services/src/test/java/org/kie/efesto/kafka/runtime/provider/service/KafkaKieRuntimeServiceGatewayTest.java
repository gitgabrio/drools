package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeLocalServiceProvider;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputD;
import org.kie.efesto.runtimemanager.core.mocks.MockKieRuntimeServiceA;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.runtimemanager.core.service.RuntimeManagerUtils.rePopulateFirstLevelCache;

/**
 * This is a full IT. The class under test is inside <code>efesto-kafka-runtime-provider</code> module, but it requires
 * classes defined in the current module
 */
class KafkaKieRuntimeServiceGatewayTest {

    private static KafkaKieRuntimeServiceGateway KAFKAKIERUNTIMESERVICEGATEWAY;
    private static List<KieRuntimeService> KIERUNTIMESERVICES;

    @BeforeAll
    public static void setup() {
        KafkaRuntimeLocalServiceProvider runtimeServiceProvider = SPIUtils.getRuntimeServiceProviders(true)
                .stream()
                .filter(serviceProvider -> serviceProvider instanceof KafkaRuntimeLocalServiceProvider)
                .findFirst()
                .map(KafkaRuntimeLocalServiceProvider.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to find KafkaRuntimeLocalServiceProvider"));
        KIERUNTIMESERVICES = runtimeServiceProvider.getKieRuntimeServices();
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
        rePopulateFirstLevelCache(KIERUNTIMESERVICES);
        MockKieRuntimeServiceA mockService = new MockKieRuntimeServiceA();
        KAFKAKIERUNTIMESERVICEGATEWAY = new KafkaKieRuntimeServiceGateway(mockService.getModelType(), mockService.getEfestoClassKeyIdentifier());
    }

    @Test
    void parseJsonInputExistingTest() throws JsonProcessingException {
        ModelLocalUriId modelLocalUriId = new MockEfestoInputA().getModelLocalUriId();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(modelLocalUriId);
        String inputDataString = "inputData";
        EfestoInput retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void parseJsonInputNotExistingTest() throws JsonProcessingException {
        ModelLocalUriId modelLocalUriId = new MockEfestoInputD().getModelLocalUriId();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(modelLocalUriId);
        String inputDataString = "inputData";
        EfestoInput retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNull();
    }

}