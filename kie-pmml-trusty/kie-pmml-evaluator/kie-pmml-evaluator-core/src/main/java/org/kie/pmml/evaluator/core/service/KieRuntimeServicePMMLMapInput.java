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
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.core.utils.JSONUtils;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.pmml.api.identifiers.AbstractModelLocalUriIdPmml;
import org.kie.pmml.evaluator.core.model.EfestoInputPMML;
import org.kie.pmml.evaluator.core.model.EfestoOutputPMML;
import org.kie.pmml.evaluator.core.serialization.AbstractModelLocalUriIdPmmlDeSerializer;
import org.kie.pmml.evaluator.core.serialization.AbstractModelLocalUriIdPmmlSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.kie.efesto.common.core.utils.JSONUtils.getInputData;
import static org.kie.pmml.commons.Constants.PMML_STRING;
import static org.kie.pmml.evaluator.core.utils.PMMLRuntimeHelper.canManageEfestoInput;
import static org.kie.pmml.evaluator.core.utils.PMMLRuntimeHelper.executeEfestoInputFromMap;

public class KieRuntimeServicePMMLMapInput implements KieRuntimeService<Map<String, Object>, PMML4Result,
        EfestoInput<Map<String, Object>>, EfestoOutputPMML, EfestoRuntimeContext> {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JSONUtils.getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(AbstractModelLocalUriIdPmml.class, new AbstractModelLocalUriIdPmmlDeSerializer());
        toRegister.addSerializer(AbstractModelLocalUriIdPmml.class, new AbstractModelLocalUriIdPmmlSerializer());
        objectMapper.registerModule(toRegister);
    }

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return new EfestoClassKey(BaseEfestoInput.class, HashMap.class);
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return canManageEfestoInput(toEvaluate, context);
    }

    @Override
    public Optional<EfestoOutputPMML> evaluateInput(EfestoInput<Map<String, Object>> toEvaluate,
                                                    EfestoRuntimeContext context) {
        return executeEfestoInputFromMap(toEvaluate, context);
    }

    @Override
    public String getModelType() {
        return PMML_STRING;
    }

    @Override
    public BaseEfestoInput<Map<String, Object>> parseJsonInput(String modelLocalUriIdString, String inputDataString) {
        ModelLocalUriId modelLocalUriId;
        try {
            modelLocalUriId = objectMapper.readValue(modelLocalUriIdString, AbstractModelLocalUriIdPmml.class);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as AbstractModelLocalUriIdPmml", modelLocalUriIdString));
        }
        Map<String, Object> inputData;
        try {
            inputData = getInputData(inputDataString);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as Map<String, Object>", inputDataString));
        }
        return new BaseEfestoInput<>(modelLocalUriId, inputData);
    }

}
