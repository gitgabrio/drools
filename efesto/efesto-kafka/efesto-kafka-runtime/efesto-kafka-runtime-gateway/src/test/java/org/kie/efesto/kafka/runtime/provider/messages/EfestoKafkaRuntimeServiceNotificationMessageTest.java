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
import org.kie.efesto.common.api.cache.EfestoClassKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeServiceNotificationMessageTest {

    private static final String template = "{\"model\":\"test\",\"efestoClassKey\":{\"rawType\":\"java.util.List\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"java.util.List<java.lang.String>\"},\"kind\":\"RUNTIMESERVICENOTIFICATION\"}";


    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoKafkaRuntimeServiceNotificationMessage toSerialize = new EfestoKafkaRuntimeServiceNotificationMessage();
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        String expected = "{\"model\":null,\"efestoClassKey\":null,\"kind\":\"RUNTIMESERVICENOTIFICATION\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
        String model = "test";
        EfestoClassKey efestoClassKey = new EfestoClassKey(List.class, String.class);
        toSerialize = new EfestoKafkaRuntimeServiceNotificationMessage(model, efestoClassKey);
        retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"kind\":\"RUNTIMESERVICENOTIFICATION\"}";
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceNotificationMessage.class);
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) retrieved).getModel()).isNull();
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) retrieved).getEfestoClassKey()).isNull();
        retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceNotificationMessage.class);
        String expectedModel = "test";
        EfestoClassKey expectedEfestoClassKey = new EfestoClassKey(List.class, String.class);
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) retrieved).getModel()).isNotNull().isEqualTo(expectedModel);
        assertThat(((EfestoKafkaRuntimeServiceNotificationMessage) retrieved).getEfestoClassKey()).isNotNull().isEqualTo(expectedEfestoClassKey);
    }

}