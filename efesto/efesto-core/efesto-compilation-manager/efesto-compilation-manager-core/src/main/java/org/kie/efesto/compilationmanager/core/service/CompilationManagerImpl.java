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
package org.kie.efesto.compilationmanager.core.service;

import org.kie.efesto.common.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.api.service.DistributedCompilationManager;
import org.kie.efesto.compilationmanager.api.service.LocalCompilationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.kie.efesto.compilationmanager.api.utils.SPIUtils.getDistributedCompilationManagers;
import static org.kie.efesto.compilationmanager.api.utils.SPIUtils.getLocalCompilationManager;

/**
 * This is the implementation of the publicly available API, to be invoked by external, consumer code
 * It acts as aggregator of the different <code>LocalCompilationManager</code>s and <code>DistributedCompilationManager</code>s founds
 */
public class CompilationManagerImpl implements CompilationManager {

    private static final Logger logger = LoggerFactory.getLogger(CompilationManagerImpl.class.getName());

    private static LocalCompilationManager localCompilationManager = getLocalCompilationManager(true).orElse(null);
    private static List<DistributedCompilationManager> distributedCompilationManagers = getDistributedCompilationManagers(true);

    @Override
    public void processResource(EfestoCompilationContext context, EfestoResource... toProcess) {
        processFromLocalManager(context, toProcess);
    }

    @Override
    public Optional<String> getCompilationSource(String fileName) {
        Optional<String> fromLocalManager = getCompilationSourceFromLocalManager(fileName);
        return fromLocalManager.isPresent() ? fromLocalManager : getCompilationSourceFromDistributedManager(fileName);
    }

    private void processFromLocalManager(EfestoCompilationContext context, EfestoResource... toProcess) {
        logger.info("processResourceFromLocalManager");
        logger.debug("{} {}", context, toProcess);
        if (localCompilationManager != null) {
            for (EfestoResource efestoResource : toProcess) {
                localCompilationManager.processResource(context, efestoResource);
            }
        }
    }

    private Optional<String> getCompilationSourceFromLocalManager(String fileName) {
        logger.info("getCompilationSourceFromLocalManager");
        logger.debug("{}", fileName);
        return localCompilationManager != null ? localCompilationManager.getCompilationSource(fileName) : Optional.empty();
    }

    private Optional<String> getCompilationSourceFromDistributedManager(String fileName) {
        logger.info("getCompilationSourceFromDistributedManager");
        logger.debug("{}", fileName);
        return distributedCompilationManagers.parallelStream().map(
                        distributedRuntimeManager -> distributedRuntimeManager.getCompilationSource(fileName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }


}
