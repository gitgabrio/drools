package org.kie.efesto.kafka.runtime.services.service;

import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaRuntimeServiceLocalProviderTest {

    @Test
    void initTest() {
        KafkaRuntimeServiceLocalProvider.init();
        assertThat(KafkaRuntimeServiceLocalProvider.kieRuntimeServices).isNotNull().isNotEmpty();
        KafkaRuntimeServiceLocalProvider.kieRuntimeServices.forEach(kieRuntimeService -> assertThat(kieRuntimeService).isExactlyInstanceOf(KafkaKieRuntimeServiceLocal.class));
    }

    @Test
    void getKieRuntimeServicesTest() {
        KafkaRuntimeServiceProvider provider = new KafkaRuntimeServiceLocalProvider();
        assertThat(provider.getKieRuntimeServices()).isNotNull().isNotEmpty();
    }
}