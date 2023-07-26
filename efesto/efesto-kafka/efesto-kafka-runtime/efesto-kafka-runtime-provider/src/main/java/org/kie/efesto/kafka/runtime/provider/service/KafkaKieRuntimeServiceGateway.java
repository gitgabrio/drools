package org.kie.efesto.kafka.runtime.provider.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.runtime.provider.consumer.CanManageInputResponseConsumer;
import org.kie.efesto.kafka.runtime.provider.consumer.ParseJsonInputResponseConsumer;
import org.kie.efesto.kafka.runtime.provider.listeners.EfestoKafkaRuntimeCanManageInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.provider.listeners.EfestoKafkaRuntimeParseJsonInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.provider.producer.CanManageInputRequestProducer;
import org.kie.efesto.kafka.runtime.provider.producer.ParseJsonInputRequestProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * This represents the <b>kafka-gateway</b> of <code>KieRuntimeService</code>
 * that executes methods asynchronously over kafka-topic
 */
public class KafkaKieRuntimeServiceGateway implements KieRuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaKieRuntimeServiceGateway.class);
    private final String modelType;
    private final EfestoClassKey getEfestoClassKeyIdentifier;

    public KafkaKieRuntimeServiceGateway(String modelType, EfestoClassKey getEfestoClassKeyIdentifier) {
        this.modelType = modelType;
        this.getEfestoClassKeyIdentifier = getEfestoClassKeyIdentifier;
    }

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return getEfestoClassKeyIdentifier;
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        logger.info("parseJsonInput");
        logger.trace("{} {}", toEvaluate, context);
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            logger.info("Sending EfestoKafkaRuntimeParseJsonInputRequestMessage...");
            EfestoKafkaRuntimeCanManageInputResponseMessageListener listener = new EfestoKafkaRuntimeCanManageInputResponseMessageListener();
            logger.info("Starting CanManageInputResponseConsumer...");
            CanManageInputResponseConsumer.startEvaluateConsumer(listener);
            long messageId = CanManageInputRequestProducer.runProducer(toEvaluate);
            logger.info("messageId {}", messageId);
            Boolean received = listener.getIsCanManage(messageId);
            while (received == null) {
                try {
                    Thread.sleep(100);
                    received = listener.getIsCanManage(messageId);
                } catch (InterruptedException e) {
                    //
                }
            }
            return received;
        });
        try {
            return completableFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failed to retrieve canManageInput for {} {}", toEvaluate, context);
            return false;
        }
    }

    @Override
    public Optional evaluateInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        // TOBE IMPLEMENTED OVER topic
        return null;
    }

    @Override
    public String getModelType() {
        return modelType;
    }

    @Override
    public EfestoInput parseJsonInput(String modelLocalUriIdString, String inputDataString) {
        logger.info("parseJsonInput");
        logger.trace("{} {}", modelLocalUriIdString, inputDataString);
        CompletableFuture<EfestoInput> completableFuture = CompletableFuture.supplyAsync(() -> {
            EfestoKafkaRuntimeParseJsonInputResponseMessageListener listener = new EfestoKafkaRuntimeParseJsonInputResponseMessageListener();
            logger.info("Starting ParseJsonInputResponseConsumer...");
            ParseJsonInputResponseConsumer.startEvaluateConsumer(listener);
            logger.info("Sending EfestoKafkaRuntimeParseJsonInputRequestMessage...");
            long messageId = ParseJsonInputRequestProducer.runProducer(modelLocalUriIdString, inputDataString);
            logger.info("messageId {}", messageId);
            EfestoInput received = listener.getEfestoInput(messageId);
            while (received == null) {
                try {
                    Thread.sleep(100);
                    received = listener.getEfestoInput(messageId);
                } catch (InterruptedException e) {
                    //
                }
            }
            return received;
        });
        try {
            return completableFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failed to retrieve EfestoInput for {} {}", modelLocalUriIdString, inputDataString);
            return null;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaKieRuntimeServiceGateway that = (KafkaKieRuntimeServiceGateway) o;
        return Objects.equals(modelType, that.modelType) && Objects.equals(getEfestoClassKeyIdentifier, that.getEfestoClassKeyIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType, getEfestoClassKeyIdentifier);
    }

}
