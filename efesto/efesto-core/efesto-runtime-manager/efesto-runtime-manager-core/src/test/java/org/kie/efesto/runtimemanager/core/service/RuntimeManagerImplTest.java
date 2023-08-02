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

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.common.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputB;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputC;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputD;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class RuntimeManagerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerImplTest.class.getName());


    private static RuntimeManagerImpl runtimeManager;
    private static EfestoRuntimeContext context;

    private static final List<Class<? extends EfestoInput>> MANAGED_Efesto_INPUTS =
            Arrays.asList(MockEfestoInputA.class,
                    MockEfestoInputB.class,
                    MockEfestoInputC.class);

    @BeforeAll
    static void setUp() {
        runtimeManager = new RuntimeManagerImpl();
        context = EfestoRuntimeContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        RuntimeManagerUtils.secondLevelCache.clear();
        RuntimeManagerUtils.firstLevelCache.clear();
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
    @ValueSource(classes = {MockEfestoInputA.class,
            MockEfestoInputB.class,
            MockEfestoInputC.class})
    void evaluateInput(Class<? extends EfestoInput> managedInput) {
        RuntimeManagerUtils.init();
        try {
            EfestoInput toProcess = managedInput.getDeclaredConstructor().newInstance();
            Collection<EfestoOutput> retrieved = runtimeManager.evaluateInput(context, toProcess);
            assertThat(retrieved).isNotNull().hasSize(1);
        } catch (Exception e) {
            fail("Failed assertion on evaluateInput", e);
        }
        Collection<EfestoOutput> retrieved = runtimeManager.evaluateInput(context,
                new MockEfestoInputD());
        assertThat(retrieved).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("evaluateInputs")
    void evaluateInputs() {
        RuntimeManagerUtils.init();
        List<EfestoInput> toProcess = new ArrayList<>();
        MANAGED_Efesto_INPUTS.forEach(managedInput -> {
            try {
                EfestoInput toAdd = managedInput.getDeclaredConstructor().newInstance();
                toProcess.add(toAdd);
            } catch (Exception e) {
                fail("Failed assertion on evaluateInput", e);
            }
        });
        toProcess.add(new MockEfestoInputD());
        Collection<EfestoOutput> retrieved = runtimeManager.evaluateInput(context,
                toProcess.toArray(new EfestoInput[0]));
        assertThat(retrieved).isNotNull().hasSize(MANAGED_Efesto_INPUTS.size());
    }
}