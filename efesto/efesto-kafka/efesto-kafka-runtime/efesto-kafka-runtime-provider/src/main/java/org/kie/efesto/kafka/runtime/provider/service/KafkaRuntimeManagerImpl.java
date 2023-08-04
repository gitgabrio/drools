package org.kie.efesto.kafka.runtime.provider.service;

import org.kie.efesto.kafka.api.service.KafkaRuntimeManager;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

import java.util.Optional;

import static org.kie.efesto.kafka.runtime.provider.service.KafkaRuntimeManagerUtils.getOptionalOutput;

public class KafkaRuntimeManagerImpl implements KafkaRuntimeManager {

    @Override
    public Optional<EfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
        return getOptionalOutput(modelLocalUriIdString, inputDataString);
    }
}
