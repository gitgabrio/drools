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
package org.kie.pmml.commons.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.commons.testingutility.PMMLRuntimeContextTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class KiePMMLFactoryModelTest {

    private KiePMMLFactoryModel kiePMMLFactoryModel;

    @BeforeEach
    public void setup() {
        kiePMMLFactoryModel = new KiePMMLFactoryModel("", "", "", new HashMap<>());
    }

    @Test
    void getSourcesMap() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
            Map<String, String> retrieved = kiePMMLFactoryModel.getSourcesMap();
            retrieved.put("KEY", "VALUE");
        });
    }

    @Test
    void addSourceMap() {
        Map<String, String> retrieved = kiePMMLFactoryModel.getSourcesMap();
        assertThat(retrieved).isEmpty();
        kiePMMLFactoryModel.addSourceMap("KEY", "VALUE");
        retrieved = kiePMMLFactoryModel.getSourcesMap();
        assertThat(retrieved).containsKey("KEY");
        assertThat(retrieved.get("KEY")).isEqualTo("VALUE");
    }

    @Test
    void evaluate() {
        assertThatExceptionOfType(KiePMMLException.class).isThrownBy(() -> {
            kiePMMLFactoryModel.evaluate(Collections.emptyMap(), new PMMLRuntimeContextTest());
        });
    }
}