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
package org.kie.efesto.kafka.runtime.service.utils;

import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.kafka.runtime.service.consumer.KieServicesDiscoverConsumer;
import org.kie.efesto.kafka.runtime.service.service.KafkaRuntimeServiceLocalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaRuntimeManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeManagerUtils.class);

    private static List<KafkaKieRuntimeService> KIERUNTIMESERVICES;

    public static void startRuntime() {
        logger.info("Strating KieServicesDiscoverConsumer...");
        KieServicesDiscoverConsumer.startEvaluateConsumer();
        KafkaRuntimeServiceLocalProvider runtimeServiceProvider = KafkaSPIUtils.getRuntimeServiceProviders(true)
                .stream()
                .filter(serviceProvider -> serviceProvider instanceof KafkaRuntimeServiceLocalProvider)
                .findFirst()
                .map(KafkaRuntimeServiceLocalProvider.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to find KafkaRuntimeServiceLocalProvider"));
        KIERUNTIMESERVICES = runtimeServiceProvider.getKieRuntimeServices()
                .stream()
                .filter(KafkaKieRuntimeService.class::isInstance)
                .map(KafkaKieRuntimeService.class::cast)
                .collect(Collectors.toList());
        logger.info("Discovered {} KieRuntimeServices", KIERUNTIMESERVICES.size());
        KIERUNTIMESERVICES.forEach(kieRuntimeService -> logger.info("Discovered {}", kieRuntimeService));
    }
}
