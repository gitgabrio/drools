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
package org.kie.efesto.runtimemanager.core.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.kie.efesto.common.core.utils.JSONUtils;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

import java.io.IOException;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoOutputSerializer extends StdSerializer<EfestoOutput> {

    private static final long serialVersionUID = 5014755163979962781L;

    public EfestoOutputSerializer() {
        this(null);
    }

    public EfestoOutputSerializer(Class<EfestoOutput> t) {
        super(t);
    }

    @Override
    public void serialize(EfestoOutput value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("modelLocalUriId");
        gen.writeObject(value.getModelLocalUriId());
        gen.writeFieldName("outputData");
        gen.writeObject(value.getOutputData());
        gen.writeStringField("kind", value.getClass().getCanonicalName());
        gen.writeEndObject();
    }


}
