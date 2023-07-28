/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.efesto.runtimemanager.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.cache.EfestoIdentifierClassKey;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.service.RuntimeServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kie.efesto.common.api.utils.CollectionUtils.findAtMostOne;
import static org.kie.efesto.common.core.utils.JSONUtils.getModelLocalUriIdObject;
import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;
import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getRuntimeServiceProviders;

public class RuntimeManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerUtils.class.getName());

    protected static final Map<EfestoClassKey, List<KieRuntimeService>> firstLevelCache = new HashMap<>();
    protected static final Map<EfestoIdentifierClassKey, KieRuntimeService> secondLevelCache = new HashMap<>();

    private RuntimeManagerUtils() {
    }

    static {
        init();
    }

    static void init() {
        logger.debug("init");
        populateFirstLevelCache(firstLevelCache);
    }

    static void populateFirstLevelCache(final Map<EfestoClassKey, List<KieRuntimeService>> toPopulate) {
        logger.debug("populateFirstLevelCache");
        logger.trace("{}", toPopulate);
        List<KieRuntimeService> discoveredKieRuntimeServices = getKieRuntimeServiceFromRuntimeServiceProviders();
        logger.trace("discoveredKieRuntimeServices {}", discoveredKieRuntimeServices);
        populateFirstLevelCache(discoveredKieRuntimeServices, toPopulate);
    }

    static void populateFirstLevelCache(final List<KieRuntimeService> discoveredKieRuntimeServices,
                                        final Map<EfestoClassKey, List<KieRuntimeService>> toPopulate) {
        logger.debug("populateFirstLevelCache");
        logger.trace("{} {}", discoveredKieRuntimeServices, toPopulate);
        discoveredKieRuntimeServices.forEach(kieRuntimeService -> {
            EfestoClassKey efestoClassKey = kieRuntimeService.getEfestoClassKeyIdentifier();
            toPopulate.merge(efestoClassKey, new ArrayList<>(Collections.singletonList(kieRuntimeService)), (previous,
                                                                                                             toAdd) -> {
                List<KieRuntimeService> toReturn = new ArrayList<>();
                toReturn.addAll(previous);
                toReturn.addAll(toAdd);
                return toReturn;
            });
        });
    }

    /**
     * Retrieves a <code>KieRuntimeService</code> from caches
     *
     * @param context
     * @param input
     * @return
     */
    static Optional<KieRuntimeService> getKieRuntimeServiceLocal(EfestoRuntimeContext context, EfestoInput input) {
        logger.debug("getKieRuntimeServiceLocal");
        logger.trace("{} {}", context, input);
        KieRuntimeService cachedKieRuntimeService = getKieRuntimeServiceFromSecondLevelCache(input);
        logger.trace("cachedKieRuntimeService {}", cachedKieRuntimeService);
        if (cachedKieRuntimeService != null) {
            return Optional.of(cachedKieRuntimeService);
        }
        Optional<KieRuntimeService> retrieved = getKieRuntimeServiceFromFirstLevelCache(context, input);
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", input.getModelLocalUriId());
        } else {
            secondLevelCache.put(input.getSecondLevelCacheKey(), retrieved.get());
        }
        return retrieved;
    }

    /**
     * Looks for <code>KieRuntimeService</code> inside the <b>secondLevelCache</b> by <code>EfestoIdentifierClassKey</code>
     *
     * @param input
     * @return The found <code>KieRuntimeService</code>, or <code>null</code>
     */
    static KieRuntimeService getKieRuntimeServiceFromSecondLevelCache(EfestoInput input) {
        return secondLevelCache.get(input.getSecondLevelCacheKey());
    }

    /**
     * Looks for <code>KieRuntimeService</code> inside the <b>firstLevelCache</b> by <code>EfestoClassKey</code>
     *
     * @param context
     * @param input
     * @return <code>Optional</code> of found <code>KieRuntimeService</code>, or <code>Optional.empty()</code>
     */
    static Optional<KieRuntimeService> getKieRuntimeServiceFromFirstLevelCache(EfestoRuntimeContext context,
                                                                               EfestoInput input) {
        logger.debug("getKieRuntimeServiceFromFirstLevelCache");
        logger.trace("{} {}", context, input);
        List<KieRuntimeService> discoveredServices = firstLevelCache.get(input.getFirstLevelCacheKey());
        logger.trace("firstLevelCache.keySet() {}", firstLevelCache.keySet());
        logger.trace("discoveredServices {}", discoveredServices);
        return (discoveredServices != null && !discoveredServices.isEmpty()) ?
                getKieRuntimeService(discoveredServices, input, context) :
                Optional.empty();
    }

    /**
     * Returns all for <code>KieRuntimeService</code> from all discovered <code>RuntimeServiceProvider</code>s
     */
    static List<KieRuntimeService> getKieRuntimeServiceFromRuntimeServiceProviders() {
        logger.debug("getKieRuntimeServiceFromRuntimeServiceProviders");
        List<RuntimeServiceProvider> runtimeServiceProviders = getRuntimeServiceProviders(true);
        logger.trace("runtimeServiceProviders {}", runtimeServiceProviders);
        return runtimeServiceProviders.stream().flatMap((Function<RuntimeServiceProvider, Stream<KieRuntimeService>>) runtimeServiceProvider -> runtimeServiceProvider.getKieRuntimeServices().stream()).collect(Collectors.toList());
    }

    static void addKieRuntimeServiceToFirstLevelCache(KieRuntimeService toAdd, EfestoClassKey firstLevelClassKey) {
        List<KieRuntimeService> stored = firstLevelCache.get(firstLevelClassKey);
        if (stored == null) {
            stored = new ArrayList<>();
            firstLevelCache.put(firstLevelClassKey, stored);
        }
        stored.add(toAdd);
    }

    static Optional<EfestoOutput> getOptionalOutput(EfestoRuntimeContext context, EfestoInput input) {
        Optional<KieRuntimeService> retrieved = getKieRuntimeServiceLocal(context, input);
        return retrieved.isPresent() ? retrieved.flatMap(kieRuntimeService -> kieRuntimeService.evaluateInput(input,
                context)) : Optional.empty();
    }

    static Optional<EfestoInput> getOptionalBaseEfestoInput(String modelLocalUriIdString, String inputDataString) {
        List<KieRuntimeService> discoveredServices = firstLevelCache.values().stream().flatMap((Function<List<KieRuntimeService>, Stream<KieRuntimeService>>) kieRuntimeServices -> kieRuntimeServices.stream()).collect(Collectors.toList());
        ModelLocalUriId modelLocalUriId;
        try {
             modelLocalUriId = getModelLocalUriIdObject(modelLocalUriIdString);
        } catch (JsonProcessingException e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as ModelLocalUriId", modelLocalUriIdString));
        }
        return findAtMostOne(discoveredServices.stream()
                .filter(kieRuntimeService -> kieRuntimeService.getModelType().equals(modelLocalUriId.model()))
                .map(kieRuntimeService -> kieRuntimeService.parseJsonInput(modelLocalUriIdString, inputDataString))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()),
                (s1, s2) -> new KieRuntimeServiceException(String.format("Found more than one runtime services for %s - %s: %s and %s",
                        modelLocalUriIdString,
                        inputDataString,
                        s1,
                        s2)));
    }

    /**
     * This is only for testing purpose.
     * Currently (To be fixed) only one service is expected for a given <b>modelLocalUri/input data</b> pair, otherwise an exception is thrown
     * {@link RuntimeManagerUtils#getOptionalBaseEfestoInput(String, String)}
     * @param discoveredKieRuntimeServices
     */
    public static void rePopulateFirstLevelCache(final List<KieRuntimeService> discoveredKieRuntimeServices) {
        logger.info("rePopulateFirstLevelCache");
        logger.debug("{}", discoveredKieRuntimeServices);
        firstLevelCache.clear();
        populateFirstLevelCache(discoveredKieRuntimeServices, firstLevelCache);
    }


}
