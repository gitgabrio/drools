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
package org.kie.efesto.runtimemanager.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.efesto.common.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.DistributedRuntimeManager;
import org.kie.efesto.runtimemanager.api.service.LocalRuntimeManager;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getDistributedRuntimeManagers;
import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getLocalRuntimeManager;
import static org.kie.efesto.runtimemanager.core.service.RuntimeManagerUtils.getOptionalInput;

/**
 * This is the implementation of the publicly available API, to be invoked by external, consumer code
 * It acts as aggregator of the different <code>LocalRuntimeManager</code>s and <code>DistributedRuntimeManager</code>s founds
 */
public class RuntimeManagerImpl implements RuntimeManager {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerImpl.class.getName());
    private static LocalRuntimeManager localRuntimeManager = getLocalRuntimeManager(true).orElse(null);
    private static List<DistributedRuntimeManager> distributedRuntimeManagers = getDistributedRuntimeManagers(true);
    private static final ObjectMapper mapper = getObjectMapper();

    @Override
    public Collection<EfestoOutput> evaluateInput(EfestoRuntimeContext context, EfestoInput... toEvaluate) {
        logger.info("evaluateInput");
        logger.debug("{} {}", context, toEvaluate);
        Map<EfestoInput, Collection<EfestoOutput>> evaluatedInputMap = evaluateFromLocalManager(context, toEvaluate);
        Set<EfestoInput> inputList = Arrays.stream(toEvaluate).collect(Collectors.toSet());
        inputList.removeAll(evaluatedInputMap.keySet());
        inputList.forEach(efestoInput -> {
            Optional<EfestoOutput> output = getOutputFromDistributedManagers(efestoInput);
            output.ifPresent(efestoOutput -> evaluatedInputMap.put(efestoInput, Collections.singletonList(efestoOutput)));
        });
        List<EfestoOutput> toReturn = evaluatedInputMap.values().stream().flatMap((Function<Collection<EfestoOutput>, Stream<EfestoOutput>>) efestoOutputs -> efestoOutputs.stream())
                .collect(Collectors.toList());
        return toReturn;
    }

    @Override
    public Optional<EfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
        logger.info("evaluateInput");
        logger.debug("{} {}", modelLocalUriIdString, inputDataString);
        Optional<EfestoOutput> toReturn = getOptionalInput(modelLocalUriIdString, inputDataString)
                .flatMap((Function<EfestoInput, Optional<EfestoOutput>>) input -> getOutputFromLocalManager(input));
        if (toReturn.isPresent()) {
            return toReturn;
        } else {
            return getOutputFromDistributedManagers(modelLocalUriIdString, inputDataString);
        }
    }

    private Map<EfestoInput, Collection<EfestoOutput>> evaluateFromLocalManager(EfestoRuntimeContext context, EfestoInput... toEvaluate) {
        logger.info("evaluateFromLocalManager");
        logger.debug("{} {}", context, toEvaluate);
        Map<EfestoInput, Collection<EfestoOutput>> toReturn = new HashMap<>();
        if (localRuntimeManager != null) {
            for (EfestoInput efestoInput : toEvaluate) {
                Collection<EfestoOutput> retrieved = localRuntimeManager.evaluateInput(context, efestoInput);
                if (!retrieved.isEmpty()) {
                    toReturn.put(efestoInput, retrieved);
                }
            }
        }
        return toReturn;
    }

    private Optional<EfestoOutput> getOutputFromLocalManager(EfestoInput efestoInput) {
        logger.info("getOutputFromLocalManager");
        logger.debug("{}", efestoInput);
        if (localRuntimeManager != null) {
            EfestoRuntimeContext runtimeContext = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
            Collection<EfestoOutput> retrieved = localRuntimeManager.evaluateInput(runtimeContext, efestoInput);
            if (!retrieved.isEmpty()) {
                return Optional.of(retrieved.iterator().next());
            }
        }
        return Optional.empty();
    }


    private Optional<EfestoOutput> getOutputFromDistributedManagers(EfestoInput efestoInput) {
        logger.info("getOutputFromDistributedManagers");
        logger.debug("{}", efestoInput);
        try {
            String modelLocalUriIdString = mapper.writeValueAsString(efestoInput.getModelLocalUriId());
            String inputDataString = mapper.writeValueAsString(efestoInput.getInputData());
            return getOutputFromDistributedManagers(modelLocalUriIdString, inputDataString);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize {} - it won't be evaluated!", efestoInput);
            return Optional.empty();
        }
    }

    private Optional<EfestoOutput> getOutputFromDistributedManagers(String modelLocalUriIdString, String inputDataString) {
        logger.info("getOutputFromDistributedManagers");
        logger.debug("{} {}", modelLocalUriIdString, inputDataString);
        return distributedRuntimeManagers.parallelStream().map(
                        distributedRuntimeManager -> distributedRuntimeManager.evaluateInput(modelLocalUriIdString, inputDataString))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}
