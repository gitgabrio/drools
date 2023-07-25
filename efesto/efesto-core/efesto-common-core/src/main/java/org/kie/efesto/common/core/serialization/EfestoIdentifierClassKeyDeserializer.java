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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.cache.EfestoIdentifierClassKey;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoIdentifierClassKeyDeserializer extends StdDeserializer<EfestoIdentifierClassKey> {

    private static final long serialVersionUID = -3468047979532504909L;

    public EfestoIdentifierClassKeyDeserializer() {
        this(null);
    }

    public EfestoIdentifierClassKeyDeserializer(Class<EfestoIdentifierClassKey> t) {
        super(t);
    }

    @Override
    public EfestoIdentifierClassKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        try {
            String modelLocalUriIdString = node.get("modelLocalUriId").toString();
            ModelLocalUriId modelLocalUriId = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
            String efestoClassKeyString = node.get("efestoClassKey").toString();
            EfestoClassKey efestoClassKey = getObjectMapper().readValue(efestoClassKeyString, EfestoClassKey.class);
            return new EfestoIdentifierClassKey(modelLocalUriId, efestoClassKey);
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to deserialize %s as EfestoIdentifierClassKey", node), e);
        }
    }
}
