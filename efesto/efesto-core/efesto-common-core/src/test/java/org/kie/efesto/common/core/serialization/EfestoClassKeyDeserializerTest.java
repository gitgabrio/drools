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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.cache.EfestoClassKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class EfestoClassKeyDeserializerTest {

    @Test
    void deserializeNoParameter() throws IOException {
        EfestoClassKey keyString = new EfestoClassKey(String.class);
        ObjectMapper mapper = getObjectMapper();
        String json = mapper.writeValueAsString(keyString);
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        EfestoClassKey retrieved = new EfestoClassKeyDeserializer().deserialize(parser, ctxt);
        assertThat(retrieved).isEqualTo(keyString);
    }

    @Test
    void deserializeSingleParameter() throws IOException {
        EfestoClassKey keyListString = new EfestoClassKey(List.class, String.class);
        ObjectMapper mapper = getObjectMapper();
        String json = mapper.writeValueAsString(keyListString);
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        EfestoClassKey retrieved = new EfestoClassKeyDeserializer().deserialize(parser, ctxt);
        assertThat(retrieved).isEqualTo(keyListString);
    }

    @Test
    void deserializeMultipleParameters() throws IOException {
        EfestoClassKey keyMapStringLong = new EfestoClassKey(Map.class, String.class, Long.class);
        ObjectMapper mapper = getObjectMapper();
        String json = mapper.writeValueAsString(keyMapStringLong);
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        EfestoClassKey retrieved = new EfestoClassKeyDeserializer().deserialize(parser, ctxt);
        assertThat(retrieved).isEqualTo(keyMapStringLong);
    }
}