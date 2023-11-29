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
package org.kie.efesto.kafka.runtime.gateway.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

/**
 * Message published by
 * {@link org.kie.efesto.kafka.runtime.gateway.service.KafkaKieRuntimeServiceGateway#evaluateInput(String, String)}
 * to eventually retrieve an <code>EfestoOutput</code>
 */
public class EfestoKafkaRuntimeEvaluateInputRequestMessage extends AbstractEfestoKafkaRuntimeMessage {


    private static final long serialVersionUID = -3245575207429193772L;
    protected String modelLocalUriIdString;
    protected String inputDataString;
    protected long messageId;

    public EfestoKafkaRuntimeEvaluateInputRequestMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTREQUEST);
    }

    public EfestoKafkaRuntimeEvaluateInputRequestMessage(String modelLocalUriIdString, String inputData, long messageId) {
        this();
        this.modelLocalUriIdString = modelLocalUriIdString;
        this.inputDataString = inputData;
        this.messageId = messageId;
    }

    public String getModelLocalUriIdString() {
        return modelLocalUriIdString;
    }

    public String getInputDataString() {
        return inputDataString;
    }

    public long getMessageId() {
        return messageId;
    }
}
