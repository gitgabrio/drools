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
package org.kie.efesto.kafka.api.utils;

import org.kie.efesto.kafka.api.service.KafkaRuntimeServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class KafkaSPIUtils {

    private KafkaSPIUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(KafkaSPIUtils.class.getName());

    private static final ServiceLoader<KafkaRuntimeServiceProvider> runtimeServiceProvidersLoader = ServiceLoader.load(KafkaRuntimeServiceProvider.class);

    private static List<KafkaRuntimeServiceProvider> runtimeServiceProviders = getRuntimeServiceProviders(runtimeServiceProvidersLoader);

    public static List<KafkaRuntimeServiceProvider> getRuntimeServiceProviders(boolean refresh) {
        if (logger.isTraceEnabled()) {
            logger.trace("getRuntimeServiceProviders {}", refresh);
        }
        if (!refresh) {
            return runtimeServiceProviders;
        }
        return runtimeServiceProviders = getRuntimeServiceProviders(getProviders(refresh));
    }

    private static List<KafkaRuntimeServiceProvider> getRuntimeServiceProviders(Iterable<KafkaRuntimeServiceProvider> serviceIterable) {
        List<KafkaRuntimeServiceProvider> toReturn = new ArrayList<>();
        serviceIterable.forEach(toReturn::add);
        if (logger.isTraceEnabled()) {
            logger.trace("toReturn {} {}", toReturn, toReturn.size());
            toReturn.forEach(provider -> logger.trace("{}", provider));
        }
        return toReturn;
    }

    private static Iterable<KafkaRuntimeServiceProvider> getProviders(boolean refresh) {
        if (refresh) {
            runtimeServiceProvidersLoader.reload();
        }
        return runtimeServiceProvidersLoader;
    }

}
