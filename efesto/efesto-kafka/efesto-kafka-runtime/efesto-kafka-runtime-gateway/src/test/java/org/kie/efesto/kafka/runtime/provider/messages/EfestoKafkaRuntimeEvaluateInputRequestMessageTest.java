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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeEvaluateInputRequestMessageTest {

    private static final String template = "{\"modelLocalUriIdString\":\"{\\\"model\\\":\\\"example\\\",\\\"basePath\\\":\\\"/some-id/instances/some-instance-id\\\",\\\"fullPath\\\":\\\"/example/some-id/instances/some-instance-id\\\"}\",\"inputDataString\":\"{\\\"approved\\\":true,\\\"applicantName\\\":\\\"John\\\"}\",\"messageId\":10,\"kind\":\"RUNTIMEEVALUATEINPUTREQUEST\"}";


    @Test
    void serializeTest() throws JsonProcessingException {
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriId = new ModelLocalUriId(parsed);
        String modelLocalUriIDString = getObjectMapper().writeValueAsString(modelLocalUriId);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("approved", true);
        inputData.put("applicantName", "John");
        String inputDataString = getObjectMapper().writeValueAsString(inputData);
        EfestoKafkaRuntimeEvaluateInputRequestMessage toSerialize = new EfestoKafkaRuntimeEvaluateInputRequestMessage(modelLocalUriIDString, inputDataString, 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
        String modelLocalUriIdString = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getModelLocalUriIdString();
        assertThat(modelLocalUriIdString).isNotNull().isNotEmpty();
        ModelLocalUriId modelLocalUriIdRetrieved = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriIdExpected = new ModelLocalUriId(parsed);
        assertThat(modelLocalUriIdRetrieved).isEqualTo(modelLocalUriIdExpected);
        String inputDataRetrieved = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getInputDataString();
        String inputDataStringExpected = "{\"approved\":true,\"applicantName\":\"John\"}";
        assertThat(inputDataRetrieved).isEqualTo(inputDataStringExpected);
        long messageId = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}