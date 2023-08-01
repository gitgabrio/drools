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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.identifiers.AbstractModelLocalUriIdPmml;
import org.kie.pmml.evaluator.api.model.EfestoOutputPMML;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.pmml.TestingHelper.*;
import static org.kie.pmml.commons.CommonTestingUtility.getModelLocalUriIdFromPmmlIdFactory;
import static org.kie.pmml.commons.Constants.PMML_FILE_NAME;

class KieRuntimeServicePMMLRequestDataTest {
    private static final String MODEL_NAME = "TestMod";
    private static final String FILE_NAME = "FileName";
    private static KieRuntimeServicePMMLRequestData kieRuntimeServicePMMLRequestData;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private ModelLocalUriId modelLocalUriId;

    @BeforeAll
    public static void setup() {
        kieRuntimeServicePMMLRequestData = new KieRuntimeServicePMMLRequestData();
        memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageEfestoInput() {
        modelLocalUriId = getModelLocalUriIdFromPmmlIdFactory(FILE_NAME, MODEL_NAME);
        EfestoRuntimeContext runtimeContext = getEfestoContext(memoryCompilerClassLoader);
        PMMLRequestData pmmlRequestData = new PMMLRequestData();
        EfestoInput<PMMLRequestData> inputPMML = new BaseEfestoInput<>(modelLocalUriId, pmmlRequestData);
        assertThat(kieRuntimeServicePMMLRequestData.canManageInput(inputPMML, runtimeContext)).isTrue();
    }

    @Test
    void evaluateCorrectInput() {
        modelLocalUriId = getModelLocalUriIdFromPmmlIdFactory(FILE_NAME, MODEL_NAME);
        PMMLRequestData pmmlRequestData = getPMMLRequestDataWithInputData(MODEL_NAME, FILE_NAME);
        EfestoInput<PMMLRequestData> efestoInput = new BaseEfestoInput<>(modelLocalUriId, pmmlRequestData);
        Optional<EfestoOutputPMML> retrieved = kieRuntimeServicePMMLRequestData.evaluateInput(efestoInput,
                getEfestoContext(memoryCompilerClassLoader));
        assertThat(retrieved).isNotNull().isPresent();
    }

    @Test
    void evaluateWrongIdentifier() {
        modelLocalUriId = getModelLocalUriIdFromPmmlIdFactory(FILE_NAME, "wrongmodel");
        PMMLRequestData pmmlRequestData = getPMMLRequestData(MODEL_NAME, FILE_NAME);
        EfestoInput<PMMLRequestData> efestoInput = new BaseEfestoInput<>(modelLocalUriId, pmmlRequestData);
        Optional<EfestoOutputPMML> retrieved = kieRuntimeServicePMMLRequestData.evaluateInput(efestoInput,
                getEfestoContext(memoryCompilerClassLoader));
        assertThat(retrieved).isNotNull().isNotPresent();
    }

    @Test
    void evaluatePMMLRuntimeContext() {
        modelLocalUriId = getModelLocalUriIdFromPmmlIdFactory(FILE_NAME, MODEL_NAME);
        EfestoRuntimeContext runtimeContext =
                getPMMLContext(FILE_NAME, MODEL_NAME, memoryCompilerClassLoader);
        PMMLRequestData pmmlRequestData = getPMMLRequestData(MODEL_NAME, FILE_NAME);
        EfestoInput<PMMLRequestData> efestoInput = new BaseEfestoInput<>(modelLocalUriId, pmmlRequestData);

        Optional<EfestoOutputPMML> retrieved = kieRuntimeServicePMMLRequestData.evaluateInput(efestoInput, runtimeContext);
        assertThat(retrieved).isNotNull().isPresent();
    }

    @Test
    void pmmlInputRequestDataTest() throws JsonProcessingException {
        String json = "{\"model\":\"pmml\",\"basePath\":\"/LoanApprovalRegression/LoanApprovalRegression\",\"fullPath\":\"/pmml/LoanApprovalRegression/LoanApprovalRegression\",\"fileName\":\"LoanApprovalRegression\"}\"}";
        AbstractModelLocalUriIdPmml modelLocalUriId = getObjectMapper().readValue(json, AbstractModelLocalUriIdPmml.class);
        String retrieved = getObjectMapper().writeValueAsString(modelLocalUriId);
        System.out.println(retrieved);

        String modelName = "LoanApprovalRegression";
        String fileName = "LoanApprovalRegression";
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("approved", true);
        inputData.put("applicantName", "John");
        PMMLRequestData pmmlRequestData = getPMMLRequestDataLocal(modelName, fileName, inputData);
        retrieved = getObjectMapper().writeValueAsString(pmmlRequestData);
        System.out.println(retrieved);
    }

    @Test
    void parseJsonInputTestCorrect() {
        String modelLocalUriIdString = "{\"model\":\"pmml\",\"basePath\":\"/LoanApprovalRegression/LoanApprovalRegression\",\"fullPath\":\"/pmml/LoanApprovalRegression/LoanApprovalRegression\",\"fileName\":\"LoanApprovalRegression\"}";
        String inputDataString = "{\n" +
                "  \"correlationId\": \"CORRELATION_ID\",\n" +
                "  \"modelName\": \"LoanApprovalRegression\",\n" +
                "  \"source\": \"null\",\n" +
                "  \"requestParams\": [\n" +
                "    {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"approved\",\n" +
                "      \"capitalizedName\": \"Approved\",\n" +
                "      \"type\": \"java.lang.Boolean\",\n" +
                "      \"value\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"applicantName\",\n" +
                "      \"capitalizedName\": \"ApplicantName\",\n" +
                "      \"type\": \"java.lang.String\",\n" +
                "      \"value\": \"John\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"_pmml_file_name_\",\n" +
                "      \"capitalizedName\": \"_pmml_file_name_\",\n" +
                "      \"type\": \"java.lang.String\",\n" +
                "      \"value\": \"LoanApprovalRegression\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"compactCapitalizedModelName\": \"LoanApprovalRegression\",\n" +
                "  \"mappedRequestParams\": {\n" +
                "    \"approved\": {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"approved\",\n" +
                "      \"capitalizedName\": \"Approved\",\n" +
                "      \"type\": \"java.lang.Boolean\",\n" +
                "      \"value\": true\n" +
                "    },\n" +
                "    \"applicantName\": {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"applicantName\",\n" +
                "      \"capitalizedName\": \"ApplicantName\",\n" +
                "      \"type\": \"java.lang.String\",\n" +
                "      \"value\": \"John\"\n" +
                "    },\n" +
                "    \"_pmml_file_name_\": {\n" +
                "      \"correlationId\": \"CORRELATION_ID\",\n" +
                "      \"name\": \"_pmml_file_name_\",\n" +
                "      \"capitalizedName\": \"_pmml_file_name_\",\n" +
                "      \"type\": \"java.lang.String\",\n" +
                "      \"value\": \"LoanApprovalRegression\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Optional<EfestoInput<PMMLRequestData>> retrieved = kieRuntimeServicePMMLRequestData.parseJsonInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull().isPresent();
    }

    private PMMLRequestData getPMMLRequestDataLocal(String modelName, String fileName, Map<String, Object> inputData) {
        final PMMLRequestData toReturn = new PMMLRequestData();
        toReturn.setModelName(modelName);
        toReturn.setCorrelationId("CORRELATION_ID");
        inputData.forEach(toReturn::addRequestParam);
        toReturn.addRequestParam(PMML_FILE_NAME, fileName);
        return toReturn;
    }
}