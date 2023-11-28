package org.kie.efesto.kafka.runtime.services.service;

import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaRuntimeServiceLocalProvider implements KafkaRuntimeServiceProvider {

    static List<KafkaKieRuntimeService> kieRuntimeServices;

    static {
        init();
    }

    static void init() {
        kieRuntimeServices = SPIUtils.getKieRuntimeServices(true).stream().map(KafkaKieRuntimeServiceLocal::new).collect(Collectors.toList());
    }

    @Override
    public List<KafkaKieRuntimeService> getKieRuntimeServices() {
        return kieRuntimeServices;
    }
}
