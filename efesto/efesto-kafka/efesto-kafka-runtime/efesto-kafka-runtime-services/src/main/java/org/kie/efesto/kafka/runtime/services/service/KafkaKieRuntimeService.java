package org.kie.efesto.kafka.runtime.services.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputRequestMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.consumer.CanManageInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.consumer.EvaluateInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.consumer.ParseJsonInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.producer.CanManageInputResponseProducer;
import org.kie.efesto.kafka.runtime.services.producer.EvaluateInputResponseProducer;
import org.kie.efesto.kafka.runtime.services.producer.ParseJsonInputResponseProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;

/**
 * This represents the <b>Kafka-embedded</b> <code>KieRuntimeService</code>
 * that executes methods asynchronously over kafka-topic
 */
public class KafkaKieRuntimeService implements KieRuntimeService, EfestoKafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaKieRuntimeService.class);
    private final KieRuntimeService wrappedService;
    private final String wrappedServiceName;

    public KafkaKieRuntimeService(KieRuntimeService wrappedService) {
        this.wrappedService = wrappedService;
        wrappedServiceName = wrappedService.getClass().getSimpleName();
        ParseJsonInputRequestConsumer.startEvaluateConsumer(this);
        CanManageInputRequestConsumer.startEvaluateConsumer(this);
        EvaluateInputRequestConsumer.startEvaluateConsumer(this);
    }

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        switch (received.getClass().getSimpleName()) {
            case "EfestoKafkaRuntimeParseJsonInputRequestMessage":
                manageEfestoKafkaRuntimeParseJsonInputRequestMessage((EfestoKafkaRuntimeParseJsonInputRequestMessage) received);
                break;
            case "EfestoKafkaRuntimeCanManageInputRequestMessage":
                manageEfestoKafkaRuntimeCanManageInputRequestMessage((EfestoKafkaRuntimeCanManageInputRequestMessage) received);
                break;
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
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return wrappedService.canManageInput(toEvaluate, context);
    }

    @Override
    public Optional<EfestoOutput> evaluateInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return wrappedService.evaluateInput(toEvaluate, context);
    }

    @Override
    public String getModelType() {
        return wrappedService.getModelType();
    }

    @Override
    public EfestoInput parseJsonInput(String modelLocalUriIdString, Serializable inputData) {
        return wrappedService.parseJsonInput(modelLocalUriIdString, inputData);
    }

    private void manageEfestoKafkaRuntimeParseJsonInputRequestMessage(EfestoKafkaRuntimeParseJsonInputRequestMessage toManage) {
        logger.info("{}- manageEfestoKafkaRuntimeParseJsonInputRequestMessage", wrappedServiceName);
        logger.debug("{}", toManage);
        EfestoInput efestoInput = parseJsonInput(toManage.getModelLocalUriIdString(), toManage.getInputData());
        if (efestoInput != null) {
            logger.info("{}- Going to send EfestoKafkaRuntimeParseJsonInputResponseMessage with {} {}", wrappedServiceName, efestoInput, toManage.getMessageId());
            ParseJsonInputResponseProducer.runProducer(efestoInput, toManage.getMessageId());
        }
    }

    private void manageEfestoKafkaRuntimeCanManageInputRequestMessage(EfestoKafkaRuntimeCanManageInputRequestMessage toManage) {
        logger.info("{}- manageEfestoKafkaRuntimeCanManageInputRequestMessage", wrappedServiceName);
        logger.debug("{}", toManage);
        EfestoRuntimeContext runtimeContext = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
        boolean canManage = canManageInput(toManage.getEfestoInput(), runtimeContext);
        logger.info("{}- Going to send EfestoKafkaRuntimeCanManageInputResponseMessage with {} {}", wrappedServiceName, canManage, toManage.getMessageId());
        CanManageInputResponseProducer.runProducer(canManage, toManage.getMessageId());
    }

    private void manageEfestoKafkaRuntimeEvaluateInputRequestMessage(EfestoKafkaRuntimeEvaluateInputRequestMessage toManage) {
        logger.info("{}- manageEfestoKafkaRuntimeEvaluateInputRequestMessage", wrappedServiceName);
        logger.debug("{}", toManage);
        EfestoRuntimeContext runtimeContext = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
        Optional<EfestoOutput> efestoOutput = evaluateInput(toManage.getEfestoInput(), runtimeContext);
        efestoOutput.ifPresent(toPublish -> {
            logger.info("{}- Going to send EfestoKafkaRuntimeEvaluateInputResponseMessage with {} {}", wrappedServiceName, toPublish, toManage.getMessageId());
            EvaluateInputResponseProducer.runProducer(toPublish, toManage.getMessageId());
        });
    }

}
