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
package org.kie.efesto.kafka.runtime.provider.storage;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.core.model.EfestoLocalRuntimeContextImpl;

import java.util.HashMap;
import java.util.Map;

public class ContextStorage {

    private static final Map<ModelLocalUriId, EfestoLocalRuntimeContextImpl> RUNTIME_CONTEXT_MAP = new HashMap<>();

    public static void putEfestoRuntimeContext(ModelLocalUriId modelLocalUriId, EfestoLocalRuntimeContextImpl runtimeContext) {
        RUNTIME_CONTEXT_MAP.put(modelLocalUriId, runtimeContext);
    }

    public static EfestoLocalRuntimeContextImpl getEfestoRuntimeContext(ModelLocalUriId modelLocalUriId) {
        return RUNTIME_CONTEXT_MAP.get(modelLocalUriId);
    }

}
