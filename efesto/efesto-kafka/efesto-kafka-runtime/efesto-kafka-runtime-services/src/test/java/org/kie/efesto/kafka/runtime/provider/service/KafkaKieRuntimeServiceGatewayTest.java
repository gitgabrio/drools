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
package org.kie.efesto.kafka.runtime.provider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.*;
import org.kie.efesto.kafka.api.service.KafkaKieRuntimeService;
import org.kie.efesto.kafka.api.utils.KafkaSPIUtils;
import org.kie.efesto.kafka.runtime.services.service.KafkaRuntimeServiceLocalProvider;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputD;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoOutput;
import org.kie.efesto.runtimemanager.core.mocks.MockKieRuntimeServiceA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.*;
import static org.kie.efesto.kafka.runtime.provider.service.KafkaRuntimeManagerUtils.rePopulateFirstLevelCache;

/**
 * This is a full IT. The class under test is inside <code>efesto-kafka-runtime-provider</code> module, but it requires
 * classes defined in the current module
 */
class KafkaKieRuntimeServiceGatewayTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaKieRuntimeServiceGatewayTest.class);

    private static KafkaKieRuntimeServiceGateway KAFKAKIERUNTIMESERVICEGATEWAY;
    private static List<KafkaKieRuntimeService> KIERUNTIMESERVICES;
    private static AdminClient ADMIN_CLIENT;

    @BeforeAll
    public static void setup() {
        KafkaRuntimeServiceLocalProvider runtimeServiceProvider = KafkaSPIUtils.getRuntimeServiceProviders(true)
                .stream()
                .filter(serviceProvider -> serviceProvider instanceof KafkaRuntimeServiceLocalProvider)
                .findFirst()
                .map(KafkaRuntimeServiceLocalProvider.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to find KafkaRuntimeLocalServiceProvider"));
        KIERUNTIMESERVICES = runtimeServiceProvider.getKieRuntimeServices();
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
        rePopulateFirstLevelCache(KIERUNTIMESERVICES);
        MockKieRuntimeServiceA mockService = new MockKieRuntimeServiceA();
        KAFKAKIERUNTIMESERVICEGATEWAY = new KafkaKieRuntimeServiceGateway(mockService.getModelType(), mockService.getEfestoClassKeyIdentifier());
        ADMIN_CLIENT = createAdminClient();
    }

    @AfterAll
    public static void shutdown() {
        ADMIN_CLIENT.close();
    }

    @BeforeEach
    public void init() {
        List<String> topics = Arrays.asList(RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC,
                RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC);
        topics.forEach(topic -> {
            TopicPartition topicPartition = new TopicPartition(topic, 1);
            Map<TopicPartition, RecordsToDelete> recordsToDelete = new HashMap<>();
            recordsToDelete.put(topicPartition, RecordsToDelete.beforeOffset(9999));
            DeleteRecordsResult result = ADMIN_CLIENT.deleteRecords(recordsToDelete);
            Map<TopicPartition, KafkaFuture<DeletedRecords>> lowWatermarks = result.lowWatermarks();
            try {
                for (Map.Entry<TopicPartition, KafkaFuture<DeletedRecords>> entry : lowWatermarks.entrySet()) {
                    logger.info(entry.getKey().topic() + " " + entry.getKey().partition() + " " + entry.getValue().get().lowWatermark());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    @Disabled("It needs full Kafka env")
    @Test
    void evaluateInputExistingTest() throws JsonProcessingException {
        EfestoInput template = new MockEfestoInputA();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(template.getModelLocalUriId());
        String inputDataString = getObjectMapper().writeValueAsString(template.getInputData());
        Optional<EfestoOutput> retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.evaluateInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull().isPresent().containsInstanceOf(MockEfestoOutput.class);
    }

    @Disabled("It needs full Kafka env")
    @Test
    void evaluateInputNotExistingTest() throws JsonProcessingException {
        EfestoInput template = new MockEfestoInputD();
        String modelLocalUriIdString = getObjectMapper().writeValueAsString(template.getModelLocalUriId());
        String inputDataString = getObjectMapper().writeValueAsString(template.getInputData());
        Optional<EfestoOutput> retrieved = KAFKAKIERUNTIMESERVICEGATEWAY.evaluateInput(modelLocalUriIdString, inputDataString);
        assertThat(retrieved).isNotNull().isNotPresent();
    }

    private static AdminClient createAdminClient() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                KafkaKieRuntimeServiceGatewayTest.class.getSimpleName());
        return KafkaAdminClient.create(props);
    }

}