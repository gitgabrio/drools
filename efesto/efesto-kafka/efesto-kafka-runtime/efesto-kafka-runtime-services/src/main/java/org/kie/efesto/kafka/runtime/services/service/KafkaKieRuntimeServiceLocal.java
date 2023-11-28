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
package org.kie.efesto.kafka.runtime.services.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.common.api.model.EfestoRuntimeContext;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.common.core.storage.ContextStorage;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.consumer.EvaluateInputRequestConsumer;
import org.kie.efesto.kafka.runtime.services.producer.EvaluateInputResponseProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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
            EfestoRuntimeContext runtimeContext = retrieveEfestoRuntimeContext(input.get());
            return wrappedService.evaluateInput(input.get(), runtimeContext);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getModelType() {
        return wrappedService.getModelType();
    }

    private EfestoRuntimeContext retrieveEfestoRuntimeContext(EfestoInput input) {
        ModelLocalUriId modelLocalUriId = input.getModelLocalUriId();
        EfestoRuntimeContext runtimeContext = ContextStorage.getEfestoRuntimeContext(modelLocalUriId);
        return runtimeContext != null ? runtimeContext : createNewEfestoRuntimeContext(modelLocalUriId);
    }

    private EfestoRuntimeContext createNewEfestoRuntimeContext(ModelLocalUriId modelLocalUriId) {
        EfestoRuntimeContext toReturn = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
        EfestoCompilationContext compilationContext = ContextStorage.getEfestoCompilationContext(modelLocalUriId);
        if (compilationContext != null) {
            Map<String, byte[]> generatedClasses = compilationContext.getGeneratedClasses(modelLocalUriId);
            if (generatedClasses != null) {
                toReturn.addGeneratedClasses(modelLocalUriId, generatedClasses);
            }
            Map<String, GeneratedResources> generatedResourcesMap = compilationContext.getGeneratedResourcesMap();
            if (generatedResourcesMap != null) {
                toReturn.getGeneratedResourcesMap().putAll(generatedResourcesMap);
            }
        }
        ContextStorage.putEfestoRuntimeContext(modelLocalUriId, toReturn);
        return toReturn;
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
