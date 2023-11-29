/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.runtime.gateway.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.runtime.gateway.consumer.EvaluateInputResponseConsumer;
import org.kie.efesto.kafka.runtime.gateway.listeners.EfestoKafkaRuntimeEvaluateInputResponseMessageListener;
import org.kie.efesto.kafka.runtime.gateway.producer.EvaluateInputRequestProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
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
public class KafkaKieRuntimeServiceGateway implements KafkaKieRuntimeService {

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
    public Optional<EfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
        logger.info("canManageInput");
        logger.trace("{} {}", modelLocalUriIdString, inputDataString);
        CompletableFuture<EfestoOutput> completableFuture = CompletableFuture.supplyAsync(() -> {
            EfestoKafkaRuntimeEvaluateInputResponseMessageListener listener = new EfestoKafkaRuntimeEvaluateInputResponseMessageListener();
            logger.info("Starting EvaluateInputResponseConsumer...");
            EvaluateInputResponseConsumer.startEvaluateConsumer(listener);
            logger.info("Sending EfestoKafkaRuntimeEvaluateInputResponseMessage...");
            long messageId = EvaluateInputRequestProducer.runProducer(modelLocalUriIdString, inputDataString);
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
            logger.warn("Failed to retrieve evaluateInput for {} {}", modelLocalUriIdString, inputDataString);
            return Optional.empty();
        }
    }

    @Override
    public String getModelType() {
        return modelType;
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
