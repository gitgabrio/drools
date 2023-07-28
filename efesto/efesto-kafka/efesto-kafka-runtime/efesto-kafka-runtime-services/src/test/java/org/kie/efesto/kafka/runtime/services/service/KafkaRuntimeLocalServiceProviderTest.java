package org.kie.efesto.kafka.runtime.services.service;

import org.junit.jupiter.api.Test;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.service.RuntimeServiceProvider;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaRuntimeLocalServiceProviderTest {

    @Test
    void initTest() {
        KafkaRuntimeLocalServiceProvider.init();
        assertThat(KafkaRuntimeLocalServiceProvider.kieRuntimeServices).isNotNull().isNotEmpty();
        KafkaRuntimeLocalServiceProvider.kieRuntimeServices.forEach(kieRuntimeService -> assertThat(kieRuntimeService).isExactlyInstanceOf(KafkaKieRuntimeService.class));
    }

    @Test
    void getKieRuntimeServicesTest() {
        RuntimeServiceProvider provider = new KafkaRuntimeLocalServiceProvider();
        assertThat(provider.getKieRuntimeServices()).isNotNull().isNotEmpty();
    }
}