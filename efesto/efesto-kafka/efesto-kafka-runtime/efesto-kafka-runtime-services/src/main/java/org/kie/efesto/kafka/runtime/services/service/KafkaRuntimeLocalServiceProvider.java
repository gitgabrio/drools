package org.kie.efesto.kafka.runtime.services.service;

import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.service.RuntimeServiceProvider;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaRuntimeLocalServiceProvider implements RuntimeServiceProvider {

    static List<KieRuntimeService> kieRuntimeServices;

    static {
        init();
    }

    static void init() {
        kieRuntimeServices = SPIUtils.getKieRuntimeServices(true).stream().map(kieRuntimeService -> new KafkaKieRuntimeService(kieRuntimeService)).collect(Collectors.toList());
    }

    @Override
    public List<KieRuntimeService> getKieRuntimeServices() {
        return kieRuntimeServices;
    }
}
