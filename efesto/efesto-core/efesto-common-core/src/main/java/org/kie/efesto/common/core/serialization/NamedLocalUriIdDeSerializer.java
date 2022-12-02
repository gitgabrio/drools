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
package org.kie.efesto.common.core.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;

public class NamedLocalUriIdDeSerializer extends StdDeserializer<NamedLocalUriId> {

    private static final long serialVersionUID = -3468047979532504909L;

    public NamedLocalUriIdDeSerializer() {
        this(null);
    }

    public NamedLocalUriIdDeSerializer(Class<NamedLocalUriId> t) {
        super(t);
    }

    @Override
    public NamedLocalUriId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String path = node.get("fullPath").asText();
        String fileName = node.get("fileName").asText();
        String modelName = node.get("modelName").asText();
        return new NamedLocalUriId(LocalUri.parse(path), fileName, modelName);
    }
}
