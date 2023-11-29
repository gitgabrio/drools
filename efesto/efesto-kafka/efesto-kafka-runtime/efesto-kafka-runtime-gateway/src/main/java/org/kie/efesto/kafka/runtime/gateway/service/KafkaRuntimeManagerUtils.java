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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.cache.EfestoIdentifierClassKey;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kie.efesto.common.core.utils.JSONUtils.getModelLocalUriIdObject;
import static org.kie.efesto.kafka.api.utils.KafkaSPIUtils.getRuntimeServiceProviders;

public class KafkaRuntimeManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeManagerUtils.class.getName());
    private static KafkaRuntimeServiceGatewayProviderImpl RUNTIME_SERVICE_PROVIDER_INSTANCE;

    protected static final Map<EfestoClassKey, List<KafkaKieRuntimeService>> firstLevelCache = new HashMap<>();
    protected static final Map<EfestoIdentifierClassKey, KafkaKieRuntimeService> secondLevelCache = new HashMap<>();

    private KafkaRuntimeManagerUtils() {
    }

    static {
        init();
    }


    public static KafkaRuntimeServiceGatewayProviderImpl getRuntimeServiceProvider() {
        // Check for multithreading
        if (RUNTIME_SERVICE_PROVIDER_INSTANCE == null) {
            RUNTIME_SERVICE_PROVIDER_INSTANCE = getRuntimeServiceProviders(true)
                    .stream()
                    .filter(serviceProvider -> serviceProvider instanceof KafkaRuntimeServiceGatewayProviderImpl)
                    .findFirst()
                    .map(KafkaRuntimeServiceGatewayProviderImpl.class::cast)
                    .orElseThrow(() -> new RuntimeException("Failed to find KafkaRuntimeServiceGatewayProviderImpl"));
        }
        return RUNTIME_SERVICE_PROVIDER_INSTANCE;
    }

    static void init() {
        logger.debug("init");
        populateFirstLevelCache(firstLevelCache);
    }

    public static void addKieRuntimeServiceToFirstLevelCache(final KafkaKieRuntimeService discoveredKieRuntimeService) {
        logger.info("addKieRuntimeServiceToFirstLevelCache");
        logger.debug("{}", discoveredKieRuntimeService);
        populateFirstLevelCache(discoveredKieRuntimeService, firstLevelCache);
    }

    static void populateFirstLevelCache(final Map<EfestoClassKey, List<KafkaKieRuntimeService>> toPopulate) {
        logger.debug("populateFirstLevelCache");
        logger.trace("{}", toPopulate);
        List<KafkaKieRuntimeService> discoveredKieRuntimeServices = getKieRuntimeServiceFromRuntimeServiceProviders();
        logger.trace("discoveredKieRuntimeServices {}", discoveredKieRuntimeServices);
        populateFirstLevelCache(discoveredKieRuntimeServices, toPopulate);
    }

    static void populateFirstLevelCache(final List<KafkaKieRuntimeService> discoveredKieRuntimeServices,
                                        final Map<EfestoClassKey, List<KafkaKieRuntimeService>> toPopulate) {
        logger.debug("populateFirstLevelCache");
        logger.trace("{} {}", discoveredKieRuntimeServices, toPopulate);
        for (KafkaKieRuntimeService kieRuntimeService : discoveredKieRuntimeServices) {
            populateFirstLevelCache(kieRuntimeService, toPopulate);
        }
    }

    static void populateFirstLevelCache(final KafkaKieRuntimeService kieRuntimeService,
                                        final Map<EfestoClassKey, List<KafkaKieRuntimeService>> toPopulate) {
        logger.debug("populateFirstLevelCache");
        logger.trace("{} {}", kieRuntimeService, toPopulate);
        EfestoClassKey efestoClassKey = kieRuntimeService.getEfestoClassKeyIdentifier();
        toPopulate.merge(efestoClassKey, new ArrayList<>(Collections.singletonList(kieRuntimeService)), (previous,
                                                                                                         toAdd) -> {
            List<KafkaKieRuntimeService> toReturn = new ArrayList<>();
            toReturn.addAll(previous);
            toReturn.addAll(toAdd);
            return toReturn;
        });
    }

    /**
     * Returns all for <code>KafkaKieRuntimeService</code> from all discovered <code>RuntimeServiceProvider</code>s
     */
    static List<KafkaKieRuntimeService> getKieRuntimeServiceFromRuntimeServiceProviders() {
        logger.debug("getKieRuntimeServiceFromRuntimeServiceProviders");
        List<KafkaRuntimeServiceProvider> runtimeServiceProviders = getRuntimeServiceProviders(true);
        logger.trace("runtimeServiceProviders {}", runtimeServiceProviders);
        return runtimeServiceProviders.stream()
                .flatMap((Function<KafkaRuntimeServiceProvider, Stream<KafkaKieRuntimeService>>) runtimeServiceProvider -> runtimeServiceProvider.getKieRuntimeServices().stream()).collect(Collectors.toList());
    }

    static void addKieRuntimeServiceToFirstLevelCache(KafkaKieRuntimeService toAdd, EfestoClassKey firstLevelClassKey) {
        List<KafkaKieRuntimeService> stored = firstLevelCache.get(firstLevelClassKey);
        if (stored == null) {
            stored = new ArrayList<>();
            firstLevelCache.put(firstLevelClassKey, stored);
        }
        stored.add(toAdd);
    }

    static Optional<EfestoOutput> getOptionalOutput(String modelLocalUriIdString, String inputDataString) {
        List<KafkaKieRuntimeService> discoveredServices = firstLevelCache.values().stream().flatMap((Function<List<KafkaKieRuntimeService>, Stream<KafkaKieRuntimeService>>) Collection::stream).collect(Collectors.toList());
        ModelLocalUriId modelLocalUriId;
        try {
            modelLocalUriId = getModelLocalUriIdObject(modelLocalUriIdString);
        } catch (JsonProcessingException e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as ModelLocalUriId", modelLocalUriIdString));
        }
        List<KafkaKieRuntimeService> modelSpecificServices = discoveredServices.stream()
                .filter(kieRuntimeService -> kieRuntimeService.getModelType().equals(modelLocalUriId.model()))
                .collect(Collectors.toList());
        return modelSpecificServices.stream()
                .map(kieRuntimeService -> kieRuntimeService.evaluateInput(modelLocalUriIdString, inputDataString))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EfestoOutput.class::cast)
                .findFirst();
    }

    /**
     * This is only for testing purpose.
     * Currently (To be fixed) only one service is expected for a given <b>modelLocalUri/input data</b> pair, otherwise an exception is thrown
     *
     * @param discoveredKieRuntimeServices
     */
    public static void rePopulateFirstLevelCache(final List<KafkaKieRuntimeService> discoveredKieRuntimeServices) {
        logger.info("rePopulateFirstLevelCache");
        logger.debug("{}", discoveredKieRuntimeServices);
        firstLevelCache.clear();
        populateFirstLevelCache(discoveredKieRuntimeServices, firstLevelCache);
    }


}
