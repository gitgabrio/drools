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
package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.mocks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class KafkaRuntimeManagerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeManagerImplTest.class.getName());
    private static final ObjectMapper mapper = getObjectMapper();

    private static KafkaRuntimeManagerImpl runtimeManager;
    // private static EfestoRuntimeContext context;

    private static final List<Class<? extends EfestoInput>> MANAGED_Efesto_INPUTS =
            Arrays.asList(MockEfestoInputA.class,
                    MockEfestoInputB.class,
                    MockEfestoInputC.class);

    @BeforeAll
    static void setUp() {
        runtimeManager = new KafkaRuntimeManagerImpl();
        //   context = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        KafkaRuntimeManagerUtils.secondLevelCache.clear();
        KafkaRuntimeManagerUtils.firstLevelCache.clear();
        Method testMethod = testInfo.getTestMethod().orElseThrow(() -> new RuntimeException("Missing method in TestInfo"));
        String content;
        if (testInfo.getDisplayName() != null && !testInfo.getDisplayName().isEmpty()) {
            content = testInfo.getDisplayName();
        } else {
            String methodName = testMethod.getName();
            String parameters = Arrays.stream(testMethod.getParameters()).map(Parameter::toString).toString();
            content = String.format("%s %s", methodName, parameters);
        }
        logger.info(String.format("About to execute  %s ", content));

    }

    @ParameterizedTest(name = "evaluateInput{0}")
    @MethodSource("hashMapProvider")
    void evaluateInput(Map<EfestoInput, KafkaKieRuntimeService> inputServiceMap) {
        KafkaRuntimeManagerUtils.init();
        KafkaRuntimeManagerUtils.rePopulateFirstLevelCache(List.copyOf(inputServiceMap.values()));
        inputServiceMap.forEach((efestoInput, kafkaKieRuntimeService) -> {
            try {
                String modelLocalUriIdString = mapper.writeValueAsString(efestoInput.getModelLocalUriId());
                String inputDataString = mapper.writeValueAsString(efestoInput.getInputData());
                Optional<EfestoOutput> retrieved = runtimeManager.evaluateInput(modelLocalUriIdString, inputDataString);
                assertThat(retrieved).isNotNull().isPresent();
            } catch (Exception e) {
                fail("Failed assertion on evaluateInput", e);
            }
        });
        try {
            EfestoInput toProcess = new MockEfestoInputD();
            String modelLocalUriIdString = mapper.writeValueAsString(toProcess.getModelLocalUriId());
            String inputDataString = mapper.writeValueAsString(toProcess.getInputData());
            Optional<EfestoOutput> retrieved = runtimeManager.evaluateInput(modelLocalUriIdString, inputDataString);
            assertThat(retrieved).isNotNull().isNotPresent();
        } catch (Exception e) {
            fail("Failed assertion on evaluateInput", e);
        }
    }

    static Stream<Map<EfestoInput, KafkaKieRuntimeService>> hashMapProvider() {
        return Stream.of(
                Map.of(new MockEfestoInputA(), getKafkaKieRuntimeService(new MockKieRuntimeServiceA()),
                        new MockEfestoInputB(), getKafkaKieRuntimeService(new MockKieRuntimeServiceB()),
                        new MockEfestoInputC(), getKafkaKieRuntimeService(new MockKieRuntimeServiceC()))
        );
    }

    private static KafkaKieRuntimeService getKafkaKieRuntimeService(KieRuntimeService kieRuntimeService) {
        return new KafkaKieRuntimeService() {

            @Override
            public EfestoClassKey getEfestoClassKeyIdentifier() {
                return kieRuntimeService.getEfestoClassKeyIdentifier();
            }

            @Override
            public String getModelType() {
                return kieRuntimeService.getModelType();
            }

            @Override
            public Optional evaluateInput(String modelLocalUriIdString, String inputDataString) {
                return Optional.of(new MockEfestoOutput());
            }
        };
    }


}