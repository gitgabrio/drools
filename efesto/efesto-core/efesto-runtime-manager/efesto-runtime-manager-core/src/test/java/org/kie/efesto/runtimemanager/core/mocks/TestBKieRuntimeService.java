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

import java.util.Optional;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;

// This service is required to find IndexFile "testb" in classpath
public class TestBKieRuntimeService<T extends AbstractMockEfestoInput> implements KieRuntimeService<String, String, T, MockEfestoOutput, EfestoRuntimeContext> {

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        // THis should always return an unmatchable key
        return new EfestoClassKey(TestBKieRuntimeService.class);
    }

    @Override
    public Optional<MockEfestoOutput> evaluateInput(T toEvaluate, EfestoRuntimeContext context) {
        return Optional.empty();
    }

    @Override
    public String getModelType() {
        return "testb";
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return false;
    }

    @Override
    public BaseEfestoInput<String> parseJsonInput(String modelLocalUriIdString, String inputDataString) {
        return null;
    }
}
