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
package org.kie.pmml.evaluator.core.service;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.identifiers.ReflectiveAppRoot;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.AbstractEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoMapInputDTO;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.identifiers.PmmlIdFactory;
import org.kie.pmml.api.runtime.PMMLRuntimeContext;
import org.kie.pmml.evaluator.core.model.EfestoInputPMML;
import org.kie.pmml.evaluator.core.model.EfestoOutputPMMLMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.pmml.TestingHelper.getEfestoContext;
import static org.kie.pmml.TestingHelper.getInputData;
import static org.kie.pmml.TestingHelper.getPMMLContext;
import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedClassName;

class KieRuntimeServicePMMLMapInputTest {

    private static final String MODEL_NAME = "TestMod";
    private static final String FILE_NAME = "FileName";
    private static KieRuntimeServicePMMLMapInput kieRuntimeServicePMMLMapInput;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private static ModelLocalUriId modelLocalUriId;

    @BeforeAll
    public static void setup() {
        kieRuntimeServicePMMLMapInput = new KieRuntimeServicePMMLMapInput();
        memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        modelLocalUriId = new ReflectiveAppRoot("")
                .get(PmmlIdFactory.class)
                .get(FILE_NAME, getSanitizedClassName(MODEL_NAME));
    }

    @Test
    void canManageEfestoInputPMMLMap() {
        EfestoRuntimeContext runtimeContext = getEfestoContext(memoryCompilerClassLoader);
        EfestoMapInputDTO efestoMapInputDTO = new EfestoMapInputDTO(null, null, new HashMap<>(), null, null, null);

        EfestoInput<EfestoMapInputDTO> inputPMML = new AbstractEfestoInput<>(modelLocalUriId, efestoMapInputDTO) {
        };
        assertThat(kieRuntimeServicePMMLMapInput.canManageInput(inputPMML, runtimeContext)).isTrue();
    }

    @Test
    void canManageEfestoInputPMML() {
        PMMLRuntimeContext context = getPMMLContext(FILE_NAME, MODEL_NAME, memoryCompilerClassLoader);
        AbstractEfestoInput darInputPMML = new EfestoInputPMML(modelLocalUriId, context);
        assertThat(kieRuntimeServicePMMLMapInput.canManageInput(darInputPMML, context)).isFalse();
    }

    @Test
    void canManageEfestoInput() {
        EfestoRuntimeContext runtimeContext = getEfestoContext(memoryCompilerClassLoader);
        PMMLRequestData pmmlRequestData = new PMMLRequestData();
        EfestoInput<PMMLRequestData> inputPMML = new AbstractEfestoInput<>(modelLocalUriId, pmmlRequestData) {
        };
        assertThat(kieRuntimeServicePMMLMapInput.canManageInput(inputPMML, runtimeContext)).isFalse();
    }

    @Test
    void evaluateCorrectInput() {
        EfestoMapInputDTO efestoMapInputDTO = new EfestoMapInputDTO(null, null, getInputData(), null, null, null);

        AbstractEfestoInput<EfestoMapInputDTO> inputPMML = new AbstractEfestoInput<>(modelLocalUriId,
                                                                                     efestoMapInputDTO) {
        };
        Optional<EfestoOutputPMMLMap> retrieved = kieRuntimeServicePMMLMapInput.evaluateInput(inputPMML,
                                                                                              getEfestoContext(memoryCompilerClassLoader));
        assertThat(retrieved).isNotNull().isPresent();
    }

    @Test
    void evaluateWrongIdentifier() {
        ModelLocalUriId modelLocalUriId = new ReflectiveAppRoot("")
                .get(PmmlIdFactory.class)
                .get(FILE_NAME, getSanitizedClassName("wrongmodel"));
        EfestoMapInputDTO efestoMapInputDTO =  new EfestoMapInputDTO(null, null, getInputData(), null, null, null);

        AbstractEfestoInput<EfestoMapInputDTO> efestoInput = new AbstractEfestoInput<>(modelLocalUriId,
                                                                                       efestoMapInputDTO) {
        };
        Optional<EfestoOutputPMMLMap> retrieved = kieRuntimeServicePMMLMapInput.evaluateInput(efestoInput,
                                                                                              getEfestoContext(memoryCompilerClassLoader));
        assertThat(retrieved).isNotNull().isNotPresent();
    }

    @Test
    void evaluateWrongEfestoRuntimeContext() {
        EfestoRuntimeContext runtimeContext =
                getPMMLContext(FILE_NAME, MODEL_NAME, memoryCompilerClassLoader);
        EfestoMapInputDTO efestoMapInputDTO = new EfestoMapInputDTO(null, null, getInputData(), null, null, null);

        AbstractEfestoInput<EfestoMapInputDTO> efestoInput = new AbstractEfestoInput<>(modelLocalUriId,
                                                                                       efestoMapInputDTO) {
        };
        KieRuntimeServiceException thrown = assertThrows(
                KieRuntimeServiceException.class,
                () -> kieRuntimeServicePMMLMapInput.evaluateInput(efestoInput,
                                                                  runtimeContext),
                "Expected evaluateInput() to throw, but it didn't"
        );
        String expectedMessage = "Unexpected PMMLRuntimeContext received";
        assertThat(thrown.getMessage()).isEqualTo(expectedMessage);
    }
}