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
package org.kie.efesto.kafka.runtime.provider.service;

import org.drools.util.FileUtils;
import org.junit.jupiter.api.*;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KafkaRuntimeManagerUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRuntimeManagerUtilsTest.class.getName());

    private static KafkaKieRuntimeService kieRuntimeServiceA;
    private static KafkaKieRuntimeService kieRuntimeServiceB;
    private static KafkaKieRuntimeService kieRuntimeServiceC;

    private static KafkaKieRuntimeService kieRuntimeServiceA_cloned;

    private static EfestoClassKey efestoClassKeyA;
    private static EfestoClassKey efestoClassKeyB;
    private static EfestoClassKey efestoClassKeyC;

    private static ModelLocalUriId modelLocalUri;

    public static KafkaKieRuntimeService baseInputService = new BaseInputService();

    public static KafkaKieRuntimeService baseInputExtenderService = new BaseInputExtenderService();

    @BeforeAll
    static void setUp() {
        kieRuntimeServiceA = mock(KafkaKieRuntimeServiceGateway.class);
        efestoClassKeyA = new EfestoClassKey(String.class);
        when(kieRuntimeServiceA.getEfestoClassKeyIdentifier()).thenReturn(efestoClassKeyA);
        kieRuntimeServiceB = mock(KafkaKieRuntimeServiceGateway.class);
        efestoClassKeyB = new EfestoClassKey(String.class);
        when(kieRuntimeServiceB.getEfestoClassKeyIdentifier()).thenReturn(efestoClassKeyB);
        kieRuntimeServiceC = mock(KafkaKieRuntimeServiceGateway.class);
        efestoClassKeyC = new EfestoClassKey(List.class, String.class);
        when(kieRuntimeServiceC.getEfestoClassKeyIdentifier()).thenReturn(efestoClassKeyC);
        kieRuntimeServiceA_cloned = mock(KafkaKieRuntimeServiceGateway.class);
        when(kieRuntimeServiceA_cloned.getEfestoClassKeyIdentifier()).thenReturn(efestoClassKeyA);

        // setup
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        modelLocalUri = new ModelLocalUriId(parsed);
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

    @Test
    @DisplayName("populateFirstLevelCache")
    void populateFirstLevelCache() {
        List<KafkaKieRuntimeService> discoveredKieRuntimeServices = Arrays.asList(kieRuntimeServiceA, kieRuntimeServiceB,
                kieRuntimeServiceC,
                kieRuntimeServiceA_cloned);
        final Map<EfestoClassKey, List<KafkaKieRuntimeService>> toPopulate = new HashMap<>();
        KafkaRuntimeManagerUtils.populateFirstLevelCache(discoveredKieRuntimeServices, toPopulate);
        assertThat(toPopulate).hasSize(2);
        assertThat(toPopulate).containsKeys(efestoClassKeyA, efestoClassKeyB, efestoClassKeyC); // efestoClassKeyA and efestoClassKeyB  are equals
        List<KafkaKieRuntimeService> servicesA = toPopulate.get(efestoClassKeyA);
        List<KafkaKieRuntimeService> servicesB = toPopulate.get(efestoClassKeyB);
        assertThat(servicesA).isEqualTo(servicesB);
        assertThat(servicesA).hasSize(3);
        assertThat(servicesA).contains(kieRuntimeServiceA, kieRuntimeServiceB, kieRuntimeServiceA_cloned);
        List<KafkaKieRuntimeService> servicesC = toPopulate.get(efestoClassKeyC);
        assertThat(servicesC).containsExactly(kieRuntimeServiceC);
    }

    @Test
    @DisplayName("addKieRuntimeServiceToFirstLevelCache")
    void addKieRuntimeServiceToFirstLevelCache() {
        List<KafkaKieRuntimeService> discoveredKieRuntimeServices = Collections.singletonList(kieRuntimeServiceA);
        final Map<EfestoClassKey, List<KafkaKieRuntimeService>> toPopulate = new HashMap<>();
        KafkaRuntimeManagerUtils.populateFirstLevelCache(discoveredKieRuntimeServices, toPopulate);
        assertThat(toPopulate).hasSize(1);
        assertThat(toPopulate).containsKeys(efestoClassKeyA);
        List<KafkaKieRuntimeService> servicesA = toPopulate.get(efestoClassKeyA);
        assertThat(servicesA).containsExactly(kieRuntimeServiceA);

        KafkaRuntimeManagerUtils.firstLevelCache.putAll(toPopulate);
        KafkaRuntimeManagerUtils.addKieRuntimeServiceToFirstLevelCache(kieRuntimeServiceA_cloned, efestoClassKeyA);
        servicesA = KafkaRuntimeManagerUtils.firstLevelCache.get(efestoClassKeyA);
        assertThat(servicesA).containsExactly(kieRuntimeServiceA, kieRuntimeServiceA_cloned);

    }

    @Test
    void getOptionalOutputPresent() throws IOException {
        List<KafkaKieRuntimeService> discoveredKieRuntimeServices = Arrays.asList(baseInputService, baseInputExtenderService);
        KafkaRuntimeManagerUtils.populateFirstLevelCache(discoveredKieRuntimeServices, KafkaRuntimeManagerUtils.firstLevelCache);
        String fileName = "BaseEfestoInputModelLocalUriId.json";
        String modelLocalUriIdString = FileUtils.getFileContent(fileName);
        String inputDataString = "inputDataString";
        Optional<EfestoOutput> retrieved = KafkaRuntimeManagerUtils.getOptionalOutput(modelLocalUriIdString,
                inputDataString);
        assertThat(retrieved).isNotNull().isPresent().get().isExactlyInstanceOf(MockEfestoOutput.class);
        MockEfestoOutput retrievedOutput = (MockEfestoOutput) retrieved.get();
        assertThat(retrievedOutput.getOutputData()).isEqualTo(inputDataString);
        fileName = "BaseEfestoInputExtenderModelLocalUriId.json";
        modelLocalUriIdString = FileUtils.getFileContent(fileName);
        retrieved = KafkaRuntimeManagerUtils.getOptionalOutput(modelLocalUriIdString,
                inputDataString);
        assertThat(retrieved).isNotNull().isPresent().get().isExactlyInstanceOf(MockEfestoOutput.class);
        retrievedOutput = (MockEfestoOutput) retrieved.get();
        assertThat(retrievedOutput.getOutputData()).isEqualTo(inputDataString);
    }

    @Test
    void getOptionalOutputNotPresent() throws IOException {
        String fileName = "NotExistingEfestoInputModelLocalUriId.json";
        String modelLocalUriIdString = FileUtils.getFileContent(fileName);
        String inputDataString = "inputDataString";
        Optional<EfestoOutput> retrieved = KafkaRuntimeManagerUtils.getOptionalOutput(modelLocalUriIdString,
                inputDataString);
        assertThat(retrieved).isNotNull().isNotPresent();
    }

    static class BaseEfestoInputExtender extends BaseEfestoInput<String> {

        public BaseEfestoInputExtender(ModelLocalUriId modelLocalUriId, String inputData) {
            super(modelLocalUriId, inputData);
        }
    }

    static class BaseInputService implements KafkaKieRuntimeService<MockEfestoOutput> {

        @Override
        public EfestoClassKey getEfestoClassKeyIdentifier() {
            // This should always return an unmatchable key
            return new EfestoClassKey(BaseEfestoInput.class, String.class);
        }

        @Override
        public Optional<MockEfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
            ModelLocalUriId modelLocalUriId;
            try {
                modelLocalUriId = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to read %s as ModelLocalUriId", modelLocalUriIdString));
            }
            return Optional.of(new MockEfestoOutput(modelLocalUriId, inputDataString));
        }

        @Override
        public String getModelType() {
            return "BaseEfestoInput";
        }
    }

    static class BaseInputExtenderService implements KafkaKieRuntimeService<MockEfestoOutput> {

        @Override
        public EfestoClassKey getEfestoClassKeyIdentifier() {
            // THis should always return an unmatchable key
            return new EfestoClassKey(BaseEfestoInputExtender.class, String.class);
        }

        @Override
        public Optional<MockEfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString) {
            ModelLocalUriId modelLocalUriId;
            try {
                modelLocalUriId = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to read %s as ModelLocalUriId", modelLocalUriIdString));
            }
            return Optional.of(new MockEfestoOutput(modelLocalUriId, inputDataString));
        }

        @Override
        public String getModelType() {
            return "BaseEfestoInputExtender";
        }
    }
}