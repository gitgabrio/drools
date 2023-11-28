/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeServiceDiscoverMessageTest {

    private static final String template = "{\"modelLocalUriId\":{\"model\":\"example\",\"basePath\":\"/some-id/instances/some-instance-id\",\"fullPath\":\"/example/some-id/instances/some-instance-id\"},\"kind\":\"RUNTIMESERVICEDISCOVER\"}";


    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoKafkaRuntimeServiceDiscoverMessage toSerialize = new EfestoKafkaRuntimeServiceDiscoverMessage();
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        String expected = "{\"modelLocalUriId\":null,\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriId = new ModelLocalUriId(parsed);
        toSerialize = new EfestoKafkaRuntimeServiceDiscoverMessage(modelLocalUriId);
        retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
        assertThat(((EfestoKafkaRuntimeServiceDiscoverMessage)retrieved).getModelLocalUriId()).isNull();
        retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId expectedModelLocalUriId = new ModelLocalUriId(parsed);
        assertThat(((EfestoKafkaRuntimeServiceDiscoverMessage)retrieved).getModelLocalUriId()).isNotNull().isEqualTo(expectedModelLocalUriId);
    }

}