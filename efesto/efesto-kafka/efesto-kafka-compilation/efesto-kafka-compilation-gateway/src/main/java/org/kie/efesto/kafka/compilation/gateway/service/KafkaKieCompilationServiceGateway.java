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
package org.kie.efesto.kafka.compilation.gateway.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.kafka.api.service.KafkaKieCompilationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * This represents the <b>kafka-gateway</b> of <code>KieCompilationService</code>
 * that executes methods asynchronously over kafka-topic
 */
public class KafkaKieCompilationServiceGateway implements KafkaKieCompilationService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaKieCompilationServiceGateway.class);
    private final String modelType;

    public KafkaKieCompilationServiceGateway(String modelType) {
        this.modelType = modelType;
    }

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return false;
    }

    @Override
    public List processResource(String toProcess) {
        return null;
    }

    @Override
    public boolean hasCompilationSource(String fileName) {
        return KafkaKieCompilationService.super.hasCompilationSource(fileName);
    }

    @Override
    public String getCompilationSource(String fileName) {
        return KafkaKieCompilationService.super.getCompilationSource(fileName);
    }

    @Override
    public String getModelType() {
        return modelType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaKieCompilationServiceGateway that = (KafkaKieCompilationServiceGateway) o;
        return Objects.equals(modelType, that.modelType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType);
    }

}
