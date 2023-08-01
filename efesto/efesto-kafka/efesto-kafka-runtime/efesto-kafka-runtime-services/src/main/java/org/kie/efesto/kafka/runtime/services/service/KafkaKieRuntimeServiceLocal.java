package org.kie.efesto.kafka.runtime.services.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.consumer.EvaluateInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.producer.EvaluateInputResponseProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This represents the <b>Kafka-embedded</b> <code>KieRuntimeService</code>
 * that executes methods asynchronously over kafka-topic
 */
public class KafkaKieRuntimeServiceLocal implements KafkaKieRuntimeService, EfestoKafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaKieRuntimeServiceLocal.class);
    private final KieRuntimeService wrappedService;
    private final String wrappedServiceName;

    public KafkaKieRuntimeServiceLocal(KieRuntimeService wrappedService) {
        this.wrappedService = wrappedService;
        wrappedServiceName = wrappedService.getClass().getSimpleName();
        EvaluateInputRequestConsumer.startEvaluateConsumer(this);
    }

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        switch (received.getClass().getSimpleName()) {
            case "EfestoKafkaRuntimeEvaluateInputRequestMessage":
                manageEfestoKafkaRuntimeEvaluateInputRequestMessage((EfestoKafkaRuntimeEvaluateInputRequestMessage) received);
                break;
            default:
                logger.debug("{}- Unmanaged message {}", wrappedServiceName, received);
        }
    }

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return wrappedService.getEfestoClassKeyIdentifier();
    }

    @Override
    public Optional<EfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
        Optional<EfestoInput> input = wrappedService.parseJsonInput(modelLocalUriIdString, inputDataString);
        if (input.isPresent()) {
            EfestoRuntimeContext runtimeContext = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
            return wrappedService.evaluateInput(input.get(), runtimeContext);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getModelType() {
        return wrappedService.getModelType();
    }

    private void manageEfestoKafkaRuntimeEvaluateInputRequestMessage(EfestoKafkaRuntimeEvaluateInputRequestMessage toManage) {
        logger.info("{}- manageEfestoKafkaRuntimeEvaluateInputRequestMessage", wrappedServiceName);
        logger.debug("{}", toManage);
        Optional<EfestoOutput> efestoOutput = evaluateInput(toManage.getModelLocalUriIdString(), toManage.getInputDataString());
        efestoOutput.ifPresent(toPublish -> {
            logger.info("{}- Going to send EfestoKafkaRuntimeEvaluateInputResponseMessage with {} {}", wrappedServiceName, toPublish, toManage.getMessageId());
            EvaluateInputResponseProducer.runProducer(toPublish, toManage.getMessageId());
        });
    }

}
