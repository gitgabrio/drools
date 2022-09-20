/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.maven.plugin.executors;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.kie.efesto.common.api.io.IndexFile;
import org.kie.efesto.common.api.model.GeneratedClassResource;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.api.utils.SPIUtils;
import org.kie.maven.plugin.KieMavenPluginContext;
import org.kie.memorycompiler.JavaCompilerSettings;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.compilation.PMMLCompilationContext;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.maven.plugin.helpers.GenerateCodeHelper.createJavaCompilerSettings;
import static org.kie.maven.plugin.helpers.GenerateCodeHelper.getProjectClassLoader;
import static org.kie.maven.plugin.helpers.GenerateCodeHelper.writeClasses;

public class GenerateEfestoModelExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GenerateEfestoModelExecutor.class);

    private GenerateEfestoModelExecutor() {
    }

    public static void generateEfestoModel(final KieMavenPluginContext kieMavenPluginContext) throws MojoExecutionException {
        final MavenProject project = kieMavenPluginContext.getProject();
        final File outputDirectory = kieMavenPluginContext.getOutputDirectory();
        final File targetDirectory = kieMavenPluginContext.getTargetDirectory();
        if (!targetDirectory.exists()) {
            targetDirectory.mkdir();
        }
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }
        final List<org.apache.maven.model.Resource> resourcesDirectories =
                kieMavenPluginContext.getResourcesDirectories();
        final Log log = kieMavenPluginContext.getLog();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        JavaCompilerSettings javaCompilerSettings = createJavaCompilerSettings();
        URLClassLoader projectClassLoader = getProjectClassLoader(project, outputDirectory, javaCompilerSettings);

        Thread.currentThread().setContextClassLoader(projectClassLoader);

        try {
            Map<String, byte[]> compiledClassesMap = compileFiles(resourcesDirectories, projectClassLoader,
                                                                  outputDirectory, log);
            writeClasses(targetDirectory, compiledClassesMap);
        } catch (Exception e) {
            log.error(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            if (projectClassLoader != null) {
                try {
                    projectClassLoader.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }

        log.info("Efesto models successfully generated");
    }

    private static Map<String, byte[]> compileFiles(final List<org.apache.maven.model.Resource> resourcesDirectories,
                                                    final ClassLoader projectClassloader,
                                                    final File outputDirectory,
                                                    final Log log) throws MojoExecutionException {
        CompilationManager compilationManager =
                SPIUtils.getCompilationManager(true).orElseThrow(() -> new MojoExecutionException("Failed to load " +
                                                                                                          "CompilationManager"));

        KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(projectClassloader);

        final List<EfestoResource> efestoResources = getEfestoResources(resourcesDirectories, log);

        EfestoCompilationContext efestoCompilationContext = EfestoCompilationContext.buildWithParentClassLoader(memoryCompilerClassLoader);

        compilationManager.processResource(efestoCompilationContext, efestoResources.toArray(new EfestoResource[0]));

        Map<String, IndexFile> indexFilesCreated = efestoCompilationContext.createIndexFiles(outputDirectory.toPath());
        indexFilesCreated.forEach((key, value) -> logger.debug("IndexFile generated {} {}", key, value.toPath()));
        /*
        *         efestoResources.forEach(efestoResource -> {
            EfestoCompilationContext efestoCompilationContext = EfestoCompilationContext.buildWithParentClassLoader(memoryCompilerClassLoader);

            compilationManager.processResource(efestoCompilationContext, efestoResources.toArray(new EfestoResource[0]));

            Map<String, IndexFile> indexFilesCreated = efestoCompilationContext.createIndexFiles(outputDirectory.toPath());
            indexFilesCreated.forEach((key, value) -> logger.debug("IndexFile generated {} {}", key, value.toPath()));
            toReturn.putAll(getCodeFromEfestoCompilationContext(efestoCompilationContext));
        });
        return toReturn;*/
        return getCodeFromEfestoCompilationContext(efestoCompilationContext);
    }

    private static Map<String, byte[]> getCodeFromEfestoCompilationContext(EfestoCompilationContext efestoCompilationContext) {
        List<String>  generatedClasses = (List<String>) efestoCompilationContext.getGeneratedResourcesMap().values().stream()
                .flatMap((Function<GeneratedResources, Stream<String>>) generatedResources -> getGeneratedClassesFromGeneratedResources(generatedResources).stream())
                .collect(Collectors.toList());
        return generatedClasses.stream().collect(Collectors.toMap(fullClassName -> fullClassName,
                                                                  fullClassName -> getMappedCode(fullClassName,
                                                                                                 efestoCompilationContext)));
    }

    private static byte[] getMappedCode(String fullClassName, EfestoCompilationContext efestoCompilationContext) throws KiePMMLException {
        byte[] toReturn = efestoCompilationContext.getCode(fullClassName);
        if (toReturn == null) {
            throw new KiePMMLException(String.format("Failed to found %s in %s", fullClassName, efestoCompilationContext));
        }
        return toReturn;
    }

    private static List<String> getGeneratedClassesFromGeneratedResources(GeneratedResources generatedResources) {
        return generatedResources.stream()
                .filter(GeneratedClassResource.class::isInstance)
                .map(GeneratedClassResource.class::cast)
                .map(GeneratedClassResource::getFullClassName)
                .collect(Collectors.toList());
    }

    private static List<EfestoResource> getEfestoResources(final List<org.apache.maven.model.Resource> resourcesDirectories,
                                                           final Log log) throws MojoExecutionException {
        List<EfestoResource> toReturn = new ArrayList<>();
        for (org.apache.maven.model.Resource resourceDirectory : resourcesDirectories) {
            File directoryFile = new File(resourceDirectory.getDirectory());
            log.info("Looking for Efesto models in " + directoryFile.getPath());
            String errorMessageTemplate = null;
            if (!directoryFile.exists()) {
                errorMessageTemplate = "Resource path %s does not exists";
            } else if (!directoryFile.canRead()) {
                errorMessageTemplate = "Resource path %s is not readable";
            } else if (!directoryFile.isDirectory()) {
                errorMessageTemplate = "Resource path %s is not a directory";
            }
            if (errorMessageTemplate != null) {
                throw new MojoExecutionException(String.format(errorMessageTemplate, resourceDirectory));
            }
            toReturn.addAll(getEfestoResources(directoryFile));
        }
        if (toReturn.isEmpty()) {
            log.info("No Efesto Models found.");
        } else {
            log.info(String.format("Found %s Efesto models", toReturn.size()));
        }
        return toReturn;
    }

    private static List<EfestoResource> getEfestoResources(File resourceDirectory) throws MojoExecutionException {
        try (Stream<Path> stream = Files
                .walk(resourceDirectory.toPath(), Integer.MAX_VALUE)
                .filter(path -> path.toFile().isFile())) {
            return stream
                    .map(Path::toFile)
                    .map(EfestoFileResource::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
