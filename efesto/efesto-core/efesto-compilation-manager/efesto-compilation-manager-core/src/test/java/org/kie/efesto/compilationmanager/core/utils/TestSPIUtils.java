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
package org.kie.efesto.compilationmanager.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.kie.efesto.compilationmanager.api.service.KieCompilationService;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;
import org.kie.efesto.compilationmanager.core.mocks.MockEfestoRedirectOutputA;
import org.kie.efesto.compilationmanager.core.mocks.MockEfestoRedirectOutputB;
import org.kie.efesto.compilationmanager.core.mocks.MockEfestoRedirectOutputC;
import org.kie.efesto.compilationmanager.core.mocks.MockEfestoRedirectOutputD;
import org.kie.efesto.compilationmanager.core.mocks.MockKieCompilationServiceAB;
import org.kie.efesto.compilationmanager.core.mocks.MockKieCompilationServiceC;
import org.kie.efesto.compilationmanager.core.mocks.MockKieCompilationServiceE;

import static org.assertj.core.api.Assertions.assertThat;

class TestSPIUtils {

    private static final List<Class<? extends KieCompilationService>> KIE_COMPILER_SERVICES = Arrays.asList(MockKieCompilationServiceAB.class, MockKieCompilationServiceC.class, MockKieCompilationServiceE.class);

    @Test
    void getKieCompilerService() {
        Optional<KieCompilationService> retrieved = SPIUtils.getKieCompilerService(new MockEfestoRedirectOutputA(), false);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get() instanceof MockKieCompilationServiceAB).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockEfestoRedirectOutputB(), false);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get() instanceof MockKieCompilationServiceAB).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockEfestoRedirectOutputC(), false);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get() instanceof MockKieCompilationServiceC).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockEfestoRedirectOutputD(), false);
        assertThat(retrieved).isNotPresent();
    }

    @Test
    void getKieCompilerServices() {
        List<KieCompilationService> retrieved = SPIUtils.getKieCompilerServices(false);
        assertThat(retrieved).isNotNull().hasSize(KIE_COMPILER_SERVICES.size());
    }
}