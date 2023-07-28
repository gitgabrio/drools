package org.kie.efesto.kafka.runtime.provider.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.runtime.provider.consumer.CanManageInputResponseConsumer;
import org.kie.efesto.kafka.runtime.provider.consumer.EvaluateInputResponseConsumer;
import org.kie.efesto.kafka.runtime.provider.consumer.ParseJsonInputResponseConsumer;
import org.kie.efesto.kafka.runtime.provider.listeners.EfestoKafkaRuntimeCanManageInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.provider.listeners.EfestoKafkaRuntimeEvaluateInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.provider.listeners.EfestoKafkaRuntimeParseJsonInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.provider.producer.CanManageInputRequestProducer;
import org.kie.efesto.kafka.runtime.provider.producer.EvaluateInputRequestProducer;
import org.kie.efesto.kafka.runtime.provider.producer.ParseJsonInputRequestProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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
        logger.info("canManageInput");
        logger.trace("{} {}", toEvaluate, context);
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            EfestoKafkaRuntimeCanManageInputResponseMessageListener listener = new EfestoKafkaRuntimeCanManageInputResponseMessageListener();
            logger.info("Starting CanManageInputResponseConsumer...");
            CanManageInputResponseConsumer.startEvaluateConsumer(listener);
            logger.info("Sending EfestoKafkaRuntimeCanManageInputRequestMessage...");
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
    public Optional<EfestoOutput> evaluateInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        logger.info("canManageInput");
        logger.trace("{} {}", toEvaluate, context);
        CompletableFuture<EfestoOutput> completableFuture = CompletableFuture.supplyAsync(() -> {
            EfestoKafkaRuntimeEvaluateInputResponseMessageListener listener = new EfestoKafkaRuntimeEvaluateInputResponseMessageListener();
            logger.info("Starting EvaluateInputResponseConsumer...");
            EvaluateInputResponseConsumer.startEvaluateConsumer(listener);
            logger.info("Sending EfestoKafkaRuntimeEvaluateInputResponseMessage...");
            long messageId = EvaluateInputRequestProducer.runProducer(toEvaluate);
            logger.info("messageId {}", messageId);
            EfestoOutput received = listener.getEfestoOutput(messageId);
            while (received == null) {
                try {
                    Thread.sleep(100);
                    received = listener.getEfestoOutput(messageId);
                } catch (InterruptedException e) {
                    //
                }
            }
            return received;
        });
        try {
            return Optional.of(completableFuture.get(30, TimeUnit.SECONDS));
        } catch (Exception e) {
            logger.warn("Failed to retrieve evaluateInput for {} {}", toEvaluate, context);
            return Optional.empty();
        }
    }

    @Override
    public String getModelType() {
        return modelType;
    }

    @Override
    public EfestoInput parseJsonInput(String modelLocalUriIdString, Serializable inputData) {
        logger.info("parseJsonInput");
        logger.trace("{} {}", modelLocalUriIdString, inputData);
        CompletableFuture<EfestoInput> completableFuture = CompletableFuture.supplyAsync(() -> {
            EfestoKafkaRuntimeParseJsonInputResponseMessageListener listener = new EfestoKafkaRuntimeParseJsonInputResponseMessageListener();
            logger.info("Starting ParseJsonInputResponseConsumer...");
            ParseJsonInputResponseConsumer.startEvaluateConsumer(listener);
            logger.info("Sending EfestoKafkaRuntimeParseJsonInputRequestMessage...");
            long messageId = ParseJsonInputRequestProducer.runProducer(modelLocalUriIdString, inputData);
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
            logger.warn("Failed to retrieve EfestoInput for {} {}", modelLocalUriIdString, inputData);
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
