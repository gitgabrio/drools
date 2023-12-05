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
package org.kie.efesto.compilationmanager.core.utils;

import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.io.IndexFile;
import org.kie.efesto.common.api.model.*;
import org.kie.efesto.common.api.utils.CollectionUtils;
import org.kie.efesto.common.core.storage.ContextStorage;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilationServiceException;
import org.kie.efesto.compilationmanager.api.model.*;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.api.service.KieCompilationService;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextImpl;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.kie.efesto.common.api.constants.Constants.INDEXFILE_DIRECTORY_PROPERTY;
import static org.kie.efesto.common.api.utils.MemoryFileUtils.getFileFromFileNameOrFilePath;
import static org.kie.efesto.common.core.utils.JSONUtils.getGeneratedResourcesObject;
import static org.kie.efesto.common.core.utils.JSONUtils.writeGeneratedResourcesObject;
import static org.kie.efesto.compilationmanager.api.utils.SPIUtils.*;

public class CompilationManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(CompilationManagerUtils.class.getName());
    private static final String DEFAULT_INDEXFILE_DIRECTORY = "./target/classes";
    private static final CompilationManager compilationManager;

    static {
        compilationManager = SPIUtils.getCompilationManager(false).orElseThrow(() -> new RuntimeException("Failed to retrieve CompilationManager"));
    }


    private CompilationManagerUtils() {
    }

    public static ModelLocalUriId compileModel(File toCompile) {
        EfestoFileResource efestoResource = new EfestoFileResource(toCompile);
        return compileModel(efestoResource);
    }

    public static ModelLocalUriId compileModel(String toCompile, String fileName) {
        EfestoInputStreamResource efestoResource = new EfestoInputStreamResource(new ByteArrayInputStream(toCompile.getBytes(StandardCharsets.UTF_8)),
                fileName);
        return compileModel(efestoResource);
    }

    public static Optional<String> getCompilationSource(String fileName) {
        return getKieCompilerServiceWithCompilationSource(fileName, false)
                .map(service -> service.getCompilationSource(fileName));
    }

    /**
     * Process resources and populate generatedResources into context without writing to IndexFile
     * @param toProcess the resource to process
     * @param context the compilation context
     */
    public static void processResourceWithContext(EfestoResource toProcess, EfestoCompilationContext context) {
        Optional<KieCompilationService> retrieved = getKieCompilerService(toProcess, false);
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieCompilationService for {}, trying in context classloader", toProcess.getClass());
            retrieved = getKieCompilerServiceFromEfestoCompilationContext(toProcess, context);
        }
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieCompilationService for {}", toProcess.getClass());
            return;
        }
        processResources(retrieved.get(), toProcess, context);
    }

    public static Optional<IndexFile> getExistingIndexFile(String model) {
        String parentPath = System.getProperty(INDEXFILE_DIRECTORY_PROPERTY, DEFAULT_INDEXFILE_DIRECTORY);
        IndexFile toReturn = new IndexFile(parentPath, model);
        return getFileFromFileNameOrFilePath(toReturn.getName(), toReturn.getAbsolutePath()).map(IndexFile::new);
    }

    static ModelLocalUriId compileModel(EfestoResource efestoResource) {
        EfestoCompilationContextImpl compilationContext = (EfestoCompilationContextImpl) EfestoCompilationContextUtils.buildWithParentClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            compilationManager.processResource(compilationContext, efestoResource);
            ModelLocalUriId toReturn = getModelLocalUriIdFromGeneratedResourcesMap(compilationContext.getGeneratedResourcesMap());
            ContextStorage.putEfestoCompilationContext(toReturn, compilationContext);
            return toReturn;
        } catch (Exception e) {
            logger.error("Failed to process {}", efestoResource, e);
            throw new KieEfestoCommonException(e);
        }
    }

    static void processResources(KieCompilationService kieCompilationService, EfestoResource toProcess, EfestoCompilationContext context) {
        List<EfestoCompilationOutput> efestoCompilationOutputList = kieCompilationService.processResource(toProcess, context);
        for (EfestoCompilationOutput compilationOutput : efestoCompilationOutputList) {
            if (compilationOutput instanceof EfestoCallableOutput) {
                populateContext(context, (EfestoCallableOutput) compilationOutput);
                if (compilationOutput instanceof EfestoCallableOutputClassesContainer) {
                    EfestoCallableOutputClassesContainer classesContainer =
                            (EfestoCallableOutputClassesContainer) compilationOutput;
                    context.loadClasses(classesContainer.getCompiledClassesMap());
                    context.addGeneratedClasses(classesContainer.getModelLocalUriId().asModelLocalUriId(),
                            classesContainer.getCompiledClassesMap());
                }
            }
            if (compilationOutput instanceof EfestoResource) {
                processResourceWithContext((EfestoResource) compilationOutput, context);
            }
        }
    }

    static IndexFile getIndexFile(EfestoCallableOutput compilationOutput) {
        String parentPath = System.getProperty(INDEXFILE_DIRECTORY_PROPERTY, DEFAULT_INDEXFILE_DIRECTORY);
        IndexFile toReturn = new IndexFile(parentPath, compilationOutput.getModelLocalUriId().model());
        return getExistingIndexFile(compilationOutput.getModelLocalUriId().model()).orElseGet(() -> createIndexFile(toReturn));
    }

    static ModelLocalUriId getModelLocalUriIdFromGeneratedResourcesMap(Map<String, GeneratedResources> generatedResourcesMap) {
        List<GeneratedResource> generatedResources =
                generatedResourcesMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        GeneratedExecutableResource generatedExecutableResource = CollectionUtils.findAtMostOne(generatedResources,
                        generatedResource -> generatedResource instanceof GeneratedExecutableResource,
                        (s1, s2) -> new KieCompilationServiceException("Found more than one GeneratedExecutableResource: " + s1 + " and " + s2))
                .map(GeneratedExecutableResource.class::cast)
                .orElseThrow(() -> new KieCompilationServiceException("Failed to retrieve a GeneratedExecutableResource"));
        return generatedExecutableResource.getModelLocalUriId();
    }

    private static IndexFile createIndexFile(IndexFile toCreate) {
        try {
            logger.debug("Writing file {} {}", toCreate.getAbsolutePath(), toCreate.getName());
            if (!toCreate.createNewFile()) {
                throw new KieCompilationServiceException("Failed to create (" + toCreate.getAbsolutePath() + ") " + toCreate.getName());
            }
        } catch (IOException e) {
            String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : e.getClass().getName();
            logger.error("Failed to create {} {} due to {}", toCreate.getAbsolutePath(), toCreate.getName(), errorMessage);
            throw new KieCompilationServiceException("Failed to create (" + toCreate.getAbsolutePath() + ") " + toCreate.getName(), e);
        }
        return toCreate;
    }

    static void populateIndexFile(IndexFile toPopulate, EfestoCompilationOutput compilationOutput) {
        try {
            GeneratedResources generatedResources = getGeneratedResourcesObject(toPopulate);
            populateGeneratedResources(generatedResources, compilationOutput);
            writeGeneratedResourcesObject(generatedResources, toPopulate);
        } catch (Exception e) {
            throw new KieCompilationServiceException(e);
        }
    }

    static void populateContext(EfestoCompilationContext context, EfestoCallableOutput compilationOutput) {
        try {
            String model = compilationOutput.getModelLocalUriId().model();
            GeneratedResources generatedResources = (GeneratedResources) context.getGeneratedResourcesMap().computeIfAbsent(model, key -> new GeneratedResources());
            populateGeneratedResources(generatedResources, compilationOutput);
        } catch (Exception e) {
            throw new KieCompilationServiceException(e);
        }
    }

    static void populateGeneratedResources(GeneratedResources toPopulate, EfestoCompilationOutput compilationOutput) {
        toPopulate.add(getGeneratedResource(compilationOutput));
        if (compilationOutput instanceof EfestoClassesContainer) {
            toPopulate.addAll(getGeneratedResources((EfestoClassesContainer) compilationOutput));
        }
    }

    static GeneratedResource getGeneratedResource(EfestoCompilationOutput compilationOutput) {
        if (compilationOutput instanceof EfestoRedirectOutput) {
            return new GeneratedRedirectResource(((EfestoRedirectOutput) compilationOutput).getModelLocalUriId(),
                    ((EfestoRedirectOutput) compilationOutput).getTargetEngine());
        } else if (compilationOutput instanceof EfestoCallableOutputModelContainer) {
            return new GeneratedModelResource(((EfestoCallableOutputModelContainer) compilationOutput).getModelLocalUriId(), ((EfestoCallableOutputModelContainer) compilationOutput).getModelSource());
        } else if (compilationOutput instanceof EfestoCallableOutput) {
            return new GeneratedExecutableResource(((EfestoCallableOutput) compilationOutput).getModelLocalUriId(), ((EfestoCallableOutput) compilationOutput).getFullClassNames());
        } else {
            throw new KieCompilationServiceException("Unmanaged type " + compilationOutput.getClass().getName());
        }
    }

    static List<GeneratedResource> getGeneratedResources(EfestoClassesContainer finalOutput) {
        List<GeneratedResource> toReturn = new ArrayList<>();
        for (String key : finalOutput.getCompiledClassesMap().keySet()) {
            toReturn.add(getGeneratedClassResource(key));
        }
        return toReturn;
    }

    static GeneratedClassResource getGeneratedClassResource(String fullClassName) {
        return new GeneratedClassResource(fullClassName);
    }

}
