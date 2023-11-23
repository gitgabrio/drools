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
package org.kie.pmml.evaluator.api.model;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.pmml.api.runtime.PMMLRuntimeContext;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class EfestoInputPMML extends BaseEfestoInput<PMMLRuntimeContext> {

    public EfestoInputPMML(ModelLocalUriId modelLocalUriId, PMMLRuntimeContext inputData) {
        super(modelLocalUriId, inputData);
    }

    /**
     * Returns the first-level cache key for the current <code>EfestoInput</code>
     * @return
     */
    @Override
    public EfestoClassKey getFirstLevelCacheKey() {
        List<Type> generics = Collections.singletonList(PMMLRuntimeContext.class);
        return new EfestoClassKey(this.getClass(), generics.toArray(new Type[0]));
    }

}
