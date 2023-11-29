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
package org.kie.efesto.kafka.runtime.gateway.managers;

import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.kafka.runtime.gateway.service.KafkaRuntimeServiceGatewayProviderImpl;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.kie.efesto.kafka.runtime.gateway.service.KafkaRuntimeManagerUtils.getRuntimeServiceProvider;

public class KafkaEfestoRuntimeManager {

    private static final KafkaRuntimeServiceGatewayProviderImpl runtimeServiceProvider = getRuntimeServiceProvider();

    private static final Logger logger = LoggerFactory.getLogger(KafkaEfestoRuntimeManager.class);

    private static final String CHECK_CLASSPATH = "check classpath and dependencies!";


    public static EfestoOutput evaluateModel(String modelLocalUriIdString, String inputDataString) {
        try {
            Optional<EfestoOutput> retrieved = getOutput(modelLocalUriIdString, inputDataString);
            if (retrieved.isEmpty()) {
                throw new KieEfestoCommonException("Failed to retrieve result for modelLocalUriId");
            } else {
                return retrieved.get();
            }
        } catch (Exception t) {
            String errorMessage = String.format("Evaluation error for %s %s due to %s: please %s",
                    modelLocalUriIdString,
                    inputDataString,
                    t.getMessage(),
                    CHECK_CLASSPATH);
            logger.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage, t);
        }
    }

    private static Optional<EfestoOutput> getOutput(String modelLocalUriIdString, String inputDataString) {
        return runtimeServiceProvider.getKieRuntimeServices()
                        .parallelStream()
                        .map(service -> service.evaluateInput(modelLocalUriIdString, inputDataString))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(obj -> (EfestoOutput) obj)
                        .findFirst();
    }

    private KafkaEfestoRuntimeManager() {
    }

}
