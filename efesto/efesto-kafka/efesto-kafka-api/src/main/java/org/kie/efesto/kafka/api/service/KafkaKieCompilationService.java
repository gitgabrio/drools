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
package org.kie.efesto.kafka.api.service;

import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;

import java.util.List;
import java.util.Optional;

/**
 * The compilation-related interface to be implemented by engine-plugin.
 * It will be looked for with SPI, so each engine should declare that implementation inside
 * <code>src/main/resources/META-INF/service/org.kie.efesto.compilationmanager.api.service.KafkaKieCompilationService</code> file
 */
public interface KafkaKieCompilationService<E extends EfestoCompilationOutput> {


    boolean canManageResource(EfestoResource toProcess);

    /**
     * Produce one <code>E</code> from the given <code>T</code>
     * <p>
     * Implementation are also required to generate a "mapping" class, i.e. a class specific for the given
     * model responsible to list all the other generated ones; engine-specific runtimes will look for such
     * class to know if it can manage given resource
     *
     * @param toProcess
     * @return
     */
    List<E> processResource(String toProcess);


    default boolean hasCompilationSource(String fileName) {
        return false;
    }

    /**
     * Retrieve the source of a given model file. Used to retrieve source from remote model (e.g. pmml model deployed in different jvm and looked for by dmn)
     *
     * @param fileName
     */
    default Optional<String> getCompilationSource(String fileName) {
        return Optional.empty();
    }

    /**
     * Return the model type that the CompilerService handles
     *
     * @return model type
     */
    String getModelType();
}
