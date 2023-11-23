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
package org.kie.efesto.runtimemanager.api.model;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import java.util.Objects;

public abstract class AbstractEfestoOutput<T> implements EfestoOutput<T> {

    private ModelLocalUriId modelLocalUriId;
    private T outputData;

    protected AbstractEfestoOutput() {
        // Serialization

    }

    protected AbstractEfestoOutput(ModelLocalUriId modelLocalUriId, T outputData) {
        this.modelLocalUriId = modelLocalUriId;
        this.outputData = outputData;
    }

    @Override
    public ModelLocalUriId getModelLocalUriId() {
        return modelLocalUriId;
    }

    @Override
    public T getOutputData() {
        return outputData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEfestoOutput<?> that = (AbstractEfestoOutput<?>) o;
        return Objects.equals(modelLocalUriId, that.modelLocalUriId) && Objects.equals(outputData, that.outputData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelLocalUriId, outputData);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "modelLocalUriId=" + modelLocalUriId +
                ", outputData=" + outputData +
                '}';
    }
}
