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
package org.kie.efesto.runtimemanager.api.utils;

import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoLocalRuntimeContext;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.api.service.RuntimeServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kie.efesto.common.api.utils.CollectionUtils.findAtMostOne;

public class SPIUtils {

    private SPIUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(SPIUtils.class.getName());

    private static final ServiceLoader<RuntimeManager> runtimeManagerLoader = ServiceLoader.load(RuntimeManager.class);

    private static final ServiceLoader<RuntimeServiceProvider> runtimeServiceProvidersLoader = ServiceLoader.load(RuntimeServiceProvider.class);

    private static final ServiceLoader<KieRuntimeService> kieRuntimeServiceLoader = ServiceLoader.load(KieRuntimeService.class);

    private static List<RuntimeServiceProvider> runtimeServiceProviders = getRuntimeServiceProviders(runtimeServiceProvidersLoader);

    private static List<KieRuntimeService> kieRuntimeServices = getKieRuntimeServices(kieRuntimeServiceLoader);

    public static List<KieRuntimeService> getLocalDiscoveredKieRuntimeServices() {
        return kieRuntimeServices;
    }

    public static Optional<KieRuntimeService> getKieRuntimeService(List<KieRuntimeService> discoveredServices, EfestoInput<?> input, EfestoRuntimeContext context) {
        if (logger.isTraceEnabled()) {
            logger.trace("getKieRuntimeService {} {} {}", discoveredServices, input, context);
        }
        return findAtMostOne(discoveredServices, service -> service.canManageInput(input, context),
                (s1, s2) -> new KieRuntimeServiceException("Found more than one compiler services: " + s1 + " and " + s2));
    }

//    public static Optional<KieRuntimeService> getKieRuntimeServiceFromEfestoRuntimeContext(EfestoInput<?> input, EfestoRuntimeContext context) {
//        if ( logger.isTraceEnabled() ) {
//            logger.trace("getKieRuntimeServiceFromEfestoRuntimeContext {} {}", input, context);
//        }
//        ServiceLoader<KieRuntimeService> contextServiceLoader = context.getKieRuntimeService();
//        return findAtMostOne(contextServiceLoader, service -> input.getFirstLevelCacheKey().equals(service.getEfestoClassKeyIdentifier()),
//                             (s1, s2) -> new KieRuntimeServiceException("Found more than one runtime services: " + s1 + " and " + s2));
//    }

    public static List<RuntimeServiceProvider> getRuntimeServiceProviders(boolean refresh) {
        if (logger.isTraceEnabled()) {
            logger.trace("getRuntimeServiceProviders {}", refresh);
        }
        if (!refresh) {
            return runtimeServiceProviders;
        }
        return runtimeServiceProviders = getRuntimeServiceProviders(getProviders(refresh));
    }

    public static List<KieRuntimeService> getKieRuntimeServices(boolean refresh) {
        if (logger.isTraceEnabled()) {
            logger.trace("getKieRuntimeServices {}", refresh);
        }
        if (!refresh) {
            return kieRuntimeServices;
        }
        return kieRuntimeServices = getKieRuntimeServices(getServices(refresh));
    }

    public static Optional<RuntimeManager> getRuntimeManager(boolean refresh) {
        logger.debug("getRuntimeManager {}", refresh);
        Iterable<RuntimeManager> managers = getManagers(refresh);
        return managers.iterator().hasNext() ? Optional.of(managers.iterator().next()) : Optional.empty();
    }

    private static List<RuntimeServiceProvider> getRuntimeServiceProviders(Iterable<RuntimeServiceProvider> serviceIterable) {
        List<RuntimeServiceProvider> toReturn = new ArrayList<>();
        serviceIterable.forEach(toReturn::add);
        if (logger.isTraceEnabled()) {
            logger.trace("toReturn {} {}", toReturn, toReturn.size());
            toReturn.forEach(provider -> logger.trace("{}", provider));
        }
        return toReturn;
    }

    private static List<KieRuntimeService> getKieRuntimeServices(Iterable<KieRuntimeService> serviceIterable) {
        List<KieRuntimeService> toReturn = new ArrayList<>();
        serviceIterable.forEach(toReturn::add);
        if (logger.isTraceEnabled()) {
            logger.trace("toReturn {} {}", toReturn, toReturn.size());
            toReturn.forEach(provider -> logger.trace("{}", provider));
        }
        return toReturn;
    }

    private static Iterable<KieRuntimeService> getServices(boolean refresh) {
        if (refresh) {
            kieRuntimeServiceLoader.reload();
        }
        return kieRuntimeServiceLoader;
    }

    private static Iterable<RuntimeManager> getManagers(boolean refresh) {
        if (refresh) {
            runtimeManagerLoader.reload();
        }
        return runtimeManagerLoader;
    }

    private static Iterable<RuntimeServiceProvider> getProviders(boolean refresh) {
        if (refresh) {
            runtimeServiceProvidersLoader.reload();
        }
        return runtimeServiceProvidersLoader;
    }

    public static Set<String> collectModelTypes(EfestoLocalRuntimeContext context) {
        List<KieRuntimeService> kieRuntimeServices = getKieRuntimeServices(false);
        Set<String> modelTypes = new HashSet<>();
        for (KieRuntimeService kieRuntimeService : kieRuntimeServices) {
            modelTypes.add(kieRuntimeService.getModelType());
        }
        ServiceLoader<KieRuntimeService> serviceLoader = context.getKieRuntimeService();
        for (KieRuntimeService kieRuntimeService : serviceLoader) {
            modelTypes.add(kieRuntimeService.getModelType());
        }
        return modelTypes;
    }
}
