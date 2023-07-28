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
package org.kie.efesto.runtimemanager.core.mocks;

import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;

import java.io.Serializable;
import java.util.Optional;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public abstract class AbstractMockKieRuntimeService<T extends AbstractMockEfestoInput> implements KieRuntimeService<String, String, T, MockEfestoOutput, EfestoRuntimeContext> {


    protected ModelLocalUriId modelLocalUriId;

    protected AbstractMockKieRuntimeService(ModelLocalUriId modelLocalUriId) {
        this.modelLocalUriId = modelLocalUriId;
    }

    abstract EfestoInput getMockedEfestoInput();

    @Override
    public Optional<MockEfestoOutput> evaluateInput(T toEvaluate, EfestoRuntimeContext context) {
        if (!canManageInput(toEvaluate, context)) {
            return Optional.empty();
        }
        return Optional.of(new MockEfestoOutput());
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return toEvaluate.getModelLocalUriId().equals(modelLocalUriId);
    }

    @Override
    public EfestoInput parseJsonInput(String modelLocalUriIdString, Serializable inputData) {
        try {
            ModelLocalUriId requested = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
            return requested.equals(modelLocalUriId) ? getMockedEfestoInput() : null;
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to parse %s as ModelLocalUriId", modelLocalUriIdString));
        }
    }

}
