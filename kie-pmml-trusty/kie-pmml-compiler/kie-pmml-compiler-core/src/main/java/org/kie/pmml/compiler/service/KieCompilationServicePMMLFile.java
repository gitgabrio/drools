/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.pmml.compiler.service;

import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.common.core.storage.ContextStorage;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilationServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilationService;
import org.kie.pmml.api.identifiers.KiePmmlComponentRoot;
import org.kie.pmml.api.identifiers.LocalCompilationSourceIdPmml;
import org.kie.pmml.api.identifiers.PmmlIdFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.kie.pmml.commons.Constants.PMML_STRING;
import static org.kie.pmml.compiler.service.PMMLCompilerServicePMMLFile.getEfestoCompilationOutputPMML;

public class KieCompilationServicePMMLFile implements KieCompilationService<EfestoCompilationOutput, EfestoCompilationContext> {

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return toProcess instanceof EfestoFileResource && ((EfestoFileResource) toProcess).getModelType().equalsIgnoreCase(PMML_STRING);
    }

    @Override
    public List<EfestoCompilationOutput> processResource(EfestoResource toProcess, EfestoCompilationContext context) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilationServiceException(String.format("%s can not process %s",
                    this.getClass().getName(),
                    toProcess.getClass().getName()));
        }
        try {
            File pmmlFile = ((EfestoFileResource) toProcess).getContent();
            String fileName = pmmlFile.getName();
            String modelSource = Files.readString(pmmlFile.toPath());
            LocalCompilationSourceIdPmml localCompilationSourceIdPmml = new EfestoAppRoot()
                    .get(KiePmmlComponentRoot.class)
                    .get(PmmlIdFactory.class)
                    .get(fileName);
            ContextStorage.putEfestoCompilationSource(localCompilationSourceIdPmml, modelSource);
        } catch (Exception e) {
            // TODO
        }

        return getEfestoCompilationOutputPMML((EfestoFileResource) toProcess, context);
    }

    public boolean hasCompilationSource(String fileName) {
        LocalCompilationSourceIdPmml localCompilationSourceIdPmml = new EfestoAppRoot()
                .get(KiePmmlComponentRoot.class)
                .get(PmmlIdFactory.class)
                .get(fileName);
        return ContextStorage.getEfestoCompilationContext(localCompilationSourceIdPmml) != null;
    }

    @Override
    public String getCompilationSource(String fileName) {
        LocalCompilationSourceIdPmml localCompilationSourceIdPmml = new EfestoAppRoot()
                .get(KiePmmlComponentRoot.class)
                .get(PmmlIdFactory.class)
                .get(fileName);
        return ContextStorage.getEfestoCompilationSource(localCompilationSourceIdPmml);
    }

    @Override
    public String getModelType() {
        return PMML_STRING;
    }
}
