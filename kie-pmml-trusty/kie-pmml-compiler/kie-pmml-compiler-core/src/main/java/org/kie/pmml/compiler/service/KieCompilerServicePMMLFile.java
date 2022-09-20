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
package org.kie.pmml.compiler.service;

import java.util.List;

import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContextImpl;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.pmml.api.compilation.PMMLCompilationContext;
import org.kie.pmml.compiler.PMMLCompilationContextImpl;

import static org.kie.pmml.commons.Constants.PMML_STRING;
import static org.kie.pmml.compiler.service.PMMLCompilerServicePMMLFile.getEfestoCompilationOutputPMML;

public class KieCompilerServicePMMLFile implements KieCompilerService<EfestoCompilationOutput, EfestoCompilationContext> {

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return toProcess instanceof EfestoFileResource && ((EfestoFileResource) toProcess).getModelType().equalsIgnoreCase(PMML_STRING);
    }

    @Override
    public List<EfestoCompilationOutput> processResource(EfestoResource toProcess, EfestoCompilationContext context) {
        return getEfestoCompilationOutputPMML((EfestoFileResource) toProcess, context);
    }

    @Override
    public String getModelType() {
        return "pmml";
    }
}
