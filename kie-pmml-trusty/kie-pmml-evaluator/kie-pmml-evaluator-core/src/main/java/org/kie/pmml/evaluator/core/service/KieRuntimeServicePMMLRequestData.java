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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.core.utils.JSONUtils;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.pmml.api.identifiers.AbstractModelLocalUriIdPmml;
import org.kie.pmml.evaluator.core.model.EfestoOutputPMML;
import org.kie.pmml.evaluator.core.serialization.PMMLRequestDataDeserializer;

import java.util.Optional;

import static org.kie.pmml.commons.Constants.PMML_STRING;
import static org.kie.pmml.evaluator.core.utils.PMMLRuntimeHelper.canManageEfestoInput;
import static org.kie.pmml.evaluator.core.utils.PMMLRuntimeHelper.executeEfestoInput;

public class KieRuntimeServicePMMLRequestData implements KieRuntimeService<PMMLRequestData, PMML4Result,
        EfestoInput<PMMLRequestData>, EfestoOutputPMML, EfestoRuntimeContext> {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JSONUtils.getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(PMMLRequestData.class, new PMMLRequestDataDeserializer());
        objectMapper.registerModule(toRegister);
    }

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return new EfestoClassKey(BaseEfestoInput.class, PMMLRequestData.class);
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return canManageEfestoInput(toEvaluate, context);
    }

    @Override
    public Optional<EfestoOutputPMML> evaluateInput(EfestoInput<PMMLRequestData> toEvaluate,
                                                    EfestoRuntimeContext context) {
        return executeEfestoInput(toEvaluate, context);
    }

    @Override
    public String getModelType() {
        return PMML_STRING;
    }

    @Override
    public BaseEfestoInput<PMMLRequestData> parseJsonInput(String modelLocalUriIdString, String inputDataString) {
        ModelLocalUriId modelLocalUriId;
        try {
            modelLocalUriId = objectMapper.readValue(modelLocalUriIdString, AbstractModelLocalUriIdPmml.class);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as AbstractModelLocalUriIdPmml", modelLocalUriIdString));
        }
        PMMLRequestData inputData;
        try {
            inputData = objectMapper.readValue(inputDataString, PMMLRequestData.class);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as PMMLRequestData", inputDataString));
        }
        return new BaseEfestoInput<>(modelLocalUriId, inputData);
    }
}
