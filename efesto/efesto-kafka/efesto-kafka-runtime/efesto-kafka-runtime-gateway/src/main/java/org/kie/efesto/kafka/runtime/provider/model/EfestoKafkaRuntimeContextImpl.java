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
package org.kie.efesto.kafka.runtime.provider.model;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.listener.EfestoListener;
import org.kie.efesto.common.api.model.EfestoRuntimeContext;
import org.kie.efesto.common.api.model.GeneratedResources;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Currently empty
 *
 * @param <T>
 */
public class EfestoKafkaRuntimeContextImpl<T extends EfestoListener> implements EfestoRuntimeContext<T> {

    @Override
    public Set<T> getEfestoListeners() {
        return Collections.emptySet();
    }

    @Override
    public void addEfestoListener(T toAdd) {

    }

    @Override
    public void removeEfestoListener(T toRemove) {
    }

    @Override
    public Map<String, GeneratedResources> getGeneratedResourcesMap() {
        return Collections.emptyMap();
    }

    @Override
    public void addGeneratedResources(String model, GeneratedResources generatedResources) {

    }

    @Override
    public Map<String, byte[]> getGeneratedClasses(ModelLocalUriId modelLocalUriId) {
        return Collections.emptyMap();
    }

    @Override
    public void addGeneratedClasses(ModelLocalUriId modelLocalUriId, Map<String, byte[]> generatedClasses) {
    }

    @Override
    public boolean containsKey(ModelLocalUriId localUri) {
        return false;
    }

    @Override
    public Set<ModelLocalUriId> localUriIdKeySet() {
        return Collections.emptySet();
    }
}
