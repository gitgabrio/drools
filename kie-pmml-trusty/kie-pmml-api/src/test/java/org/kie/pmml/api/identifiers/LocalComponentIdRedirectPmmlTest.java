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
package org.kie.pmml.api.identifiers;

import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.LocalId;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.LocalUriId;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.api.identifiers.LocalUri.SLASH;

class LocalComponentIdRedirectPmmlTest {

    private static final String redirectModel = "redirectModel";
    private static final String fileName = "fileName";
    private static final String modelName = "modelName";

    @Test
    void equals() {
        LocalComponentIdRedirectPmml original = new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName);
        LocalUriId compare = new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName);
        assertThat(original.equals(compare)).isTrue();
        String path = original.fullPath();
        LocalUri parsed = LocalUri.parse(path);
        compare = new NamedLocalUriId(parsed, fileName, modelName);
        assertThat(original.equals(compare)).isTrue();
    }

    @Test
    void prefix() {
        String retrieved =
                new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName).asLocalUri().toUri().getPath();
        String expected = SLASH + redirectModel + SLASH;
        assertThat(retrieved).startsWith(expected);
    }

    @Test
    void getRedirectModel() {
        LocalComponentIdRedirectPmml retrieved = new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName);
        assertThat(retrieved.getRedirectModel()).isEqualTo(redirectModel);
    }

    @Test
    void getFileName() {
        LocalComponentIdRedirectPmml retrieved = new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName);
        assertThat(retrieved.fileName()).isEqualTo(fileName);
    }

    @Test
    void name() {
        LocalComponentIdRedirectPmml retrieved = new LocalComponentIdRedirectPmml(redirectModel, fileName, modelName);
        assertThat(retrieved.modelName()).isEqualTo(modelName);
    }

    @Test
    void toLocalId() {
        LocalComponentIdRedirectPmml LocalComponentIdRedirectPmml = new LocalComponentIdRedirectPmml(redirectModel,
                                                                                                     fileName, modelName);
        LocalId retrieved = LocalComponentIdRedirectPmml.toLocalId();
        assertThat(retrieved).isEqualTo(LocalComponentIdRedirectPmml);
    }
}