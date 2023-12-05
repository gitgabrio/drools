/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.compilation.service.service;

import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilationService;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.api.service.KafkaKieCompilationService;
import org.kie.efesto.kafka.compilation.gateway.messages.EfestoKafkaCompilationSourceRequestMessage;
import org.kie.efesto.kafka.compilation.service.consumer.CompilationSourceRequestConsumer;
import org.kie.efesto.kafka.compilation.service.producer.CompilationSourceResponseProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * This represents the <b>Kafka-embedded</b> <code>KieCompilationService</code>
 * that executes methods asynchronously over kafka-topic
 */
public class KafkaCompilationServiceLocal implements KafkaKieCompilationService, EfestoKafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCompilationServiceLocal.class);
    private final KieCompilationService wrappedService;
    private final String wrappedServiceName;

    public KafkaCompilationServiceLocal(KieCompilationService wrappedService) {
        this.wrappedService = wrappedService;
        wrappedServiceName = wrappedService.getClass().getSimpleName();
        CompilationSourceRequestConsumer.startSourceRequestConsumer(this);
    }

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        switch (received.getClass().getSimpleName()) {
            case "EfestoKafkaCompilationSourceRequestMessage":
                manageEfestoKafkaCompilationSourceRequestMessage((EfestoKafkaCompilationSourceRequestMessage) received);
                break;
            default:
                logger.debug("{}- Unmanaged message {}", wrappedServiceName, received);
        }
    }

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return wrappedService.canManageResource(toProcess);
    }

    @Override
    public List processResource(String toProcess) {
        return null;
    }

    @Override
    public String getModelType() {
        return wrappedService.getModelType();
    }

    @Override
    public Optional<String> getCompilationSource(String fileName) {
        return Optional.of(wrappedService.getCompilationSource(fileName));
    }

    private void manageEfestoKafkaCompilationSourceRequestMessage(EfestoKafkaCompilationSourceRequestMessage toManage) {
        logger.info("{}- manageEfestoKafkaCompilationSourceRequestMessage", wrappedServiceName);
        logger.debug("{}", toManage);
        Optional<String> source = getCompilationSource(toManage.getFileName());
        source.ifPresent(toPublish -> {
            logger.info("{}- Going to send EfestoKafkaCompilationEvaluateInputResponseMessage with {} {}", wrappedServiceName, toPublish, toManage.getMessageId());
            CompilationSourceResponseProducer.runProducer(toPublish, toManage.getMessageId());
        });
    }

}
