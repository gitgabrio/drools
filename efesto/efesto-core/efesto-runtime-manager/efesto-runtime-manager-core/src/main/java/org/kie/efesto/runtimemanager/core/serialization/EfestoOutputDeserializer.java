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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.AbstractEfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;

import java.io.IOException;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoOutputDeserializer extends StdDeserializer<EfestoOutput> {

    private static final long serialVersionUID = 5014755163979962781L;

    private static final RuntimeManager runtimeManager = SPIUtils.getRuntimeManager(true).orElseThrow(() -> new KieRuntimeServiceException("Failed to retrieve an instance of RuntimeManager"));

    public EfestoOutputDeserializer() {
        this(null);
    }

    public EfestoOutputDeserializer(Class<EfestoOutput> t) {
        super(t);
    }

    @Override
    public EfestoOutput deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        try {
            String kind = node.get("kind").asText();
            Class<?> actualClass = Class.forName(kind);
            ((ObjectNode)node).remove("kind");
            String cleanedNode = node.toString();
            return (EfestoOutput) getObjectMapper().readValue(cleanedNode, actualClass);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to deserialize %s as EfestoIdentifierClassKey", node), e);
        }
    }


}
