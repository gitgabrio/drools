package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.runtime.provider.model.EfestoKafkaRuntimeContextImpl;
import org.kie.efesto.kafka.runtime.services.consumer.CanManageInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeLocalServiceProvider;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputD;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoOutput;
import org.kie.efesto.runtimemanager.core.mocks.MockKieRuntimeServiceA;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.*;
import static org.kie.efesto.runtimemanager.core.service.RuntimeManagerUtils.rePopulateFirstLevelCache;

/**
 * This is a full IT. The class under test is inside <code>efesto-kafka-runtime-provider</code> module, but it requires
 * classes defined in the current module
 */
class KafkaKieRuntimeServiceGatewayTest {

    private static KafkaKieRuntimeServiceGateway KAFKAKIERUNTIMESERVICEGATEWAY;
    private static List<KieRuntimeService> KIERUNTIMESERVICES;
    private static AdminClient ADMIN_CLIENT;

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
        ADMIN_CLIENT = createAdminClient();
    }

    @BeforeEach
    public void init() {
        ADMIN_CLIENT.deleteTopics(Arrays.asList(RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC,
                RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC,
                RUNTIMESERVICE_CANMANAGEINPUTREQUEST_TOPIC,
                RUNTIMESERVICE_CANMANAGEINPUTRESPONSE_TOPIC));
    }

    @Disabled("It needs full Kafka env")
    @Test
    void evaluateInputExistingTest() {
        Optional<EfestoOutput> retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.evaluateInput(new MockEfestoInputA(), new EfestoKafkaRuntimeContextImpl());
        assertThat(retrieved).isNotNull().isPresent().containsInstanceOf(MockEfestoOutput.class);
    }

    @Disabled("It needs full Kafka env")
    @Test
    void evaluateInputNotExistingTest() {
        Optional<EfestoOutput> retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.evaluateInput(new MockEfestoInputD(), new EfestoKafkaRuntimeContextImpl());
        assertThat(retrieved).isNotNull().isNotPresent();
    }

    @Disabled("It needs full Kafka env")
    @Test
    void canManageInputExistingTest() {
        boolean retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.canManageInput(new MockEfestoInputA(), new EfestoKafkaRuntimeContextImpl());
        assertThat(retrieved).isTrue();
    }

    @Disabled("It needs full Kafka env")
    @Test
    void canManageInputNotExistingTest() {
        boolean retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.canManageInput(new MockEfestoInputD(), new EfestoKafkaRuntimeContextImpl());
        assertThat(retrieved).isFalse();
    }

    @Disabled("It needs full Kafka env")
    @Test
    void parseJsonInputExistingTest() throws JsonProcessingException {
        ModelLocalUriId modelLocalUriId = new MockEfestoInputA().getModelLocalUriId();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(modelLocalUriId);
        String inputDataString = "inputData";
        EfestoInput retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull();
    }

    @Disabled("It needs full Kafka env")
    @Test
    void parseJsonInputNotExistingTest() throws JsonProcessingException {
        ModelLocalUriId modelLocalUriId = new MockEfestoInputD().getModelLocalUriId();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(modelLocalUriId);
        String inputDataString = "inputData";
        EfestoInput retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNull();
    }

    private static AdminClient createAdminClient() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                CanManageInputRequestConsumer.class.getSimpleName());
        return KafkaAdminClient.create(props);
    }

}