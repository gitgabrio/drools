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
package org.kie.efesto.kafka.api.service;

import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

import java.util.Optional;

/**
 * This is the publicly available API, to be invoked by external, consumer code
 */
public interface KafkaRuntimeManager {


    /**
     * Return an <code>Optional&lt;EfestoOutput&gt;</code> from the given <b>modelLocalUriIdString</b> and <b>inputDataString</b>
     *
     * @param modelLocalUriIdString
     * @param inputDataString
     * @return
     */
    Optional<EfestoOutput> evaluateInput(String modelLocalUriIdString, String inputDataString);
}
