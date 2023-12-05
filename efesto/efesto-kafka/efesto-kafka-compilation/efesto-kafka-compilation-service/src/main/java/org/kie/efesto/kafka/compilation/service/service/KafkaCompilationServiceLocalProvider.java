package org.kie.efesto.kafka.compilation.service.service;

import org.kie.efesto.kafka.api.service.KafkaKieCompilationService;
import org.kie.efesto.kafka.api.service.KafkaCompilationServiceProvider;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaCompilationServiceLocalProvider implements KafkaCompilationServiceProvider {

    static List<KafkaKieCompilationService> kieCompilationServices;

    static {
        init();
    }

    static void init() {
        kieCompilationServices = SPIUtils.getKieCompilationServices(true).stream().map(KafkaCompilationServiceLocal::new).collect(Collectors.toList());
    }

    @Override
    public List<KafkaKieCompilationService> getKieCompilationServices() {
        return kieCompilationServices;
    }
}
