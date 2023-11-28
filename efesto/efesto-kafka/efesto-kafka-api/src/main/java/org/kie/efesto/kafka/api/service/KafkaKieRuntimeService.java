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
package org.kie.efesto.kafka.api.service;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

import java.util.Optional;

public interface KafkaKieRuntimeService<E extends EfestoOutput> {

    /**
     * Every <code>KieRuntimeService</code> is responsible to provide the <code>EfestoClassKey</code> that is supposed to manage.
     * The <code>EfestoClassKey</code> is used as key in the <code>RuntimeManagerImpl.firstLevelCache</code> and it is provided in the <code>EfestoInput</code>.
     * It represents the actual class of provided <code>EfestoInput</code> and its generic type(s)
     *
     * @return
     */
    EfestoClassKey getEfestoClassKeyIdentifier();

    /**
     * Return the model type that the RuntimeService handles
     *
     * @return model type
     */
    String getModelType();

    /**
     * Produce one <code>EfestoOutput</code> from the given <b>modelLocalUriIdString</b> and <b>inputDataString</b>
     *
     * @param modelLocalUriIdString
     * @param inputDataString
     * @return
     */
    Optional<E> evaluateInput(String modelLocalUriIdString, String inputDataString);
}
