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
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

/**
 * Message published by <code>KafkaKieRuntimeService</code> to notify the <code>EfestoInput</code> evaluation
 */
public class EfestoKafkaRuntimeEvaluateInputResponseMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 7672828798629469119L;
    protected EfestoOutput efestoOutput;
    protected Long messageId;

    public EfestoKafkaRuntimeEvaluateInputResponseMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTRESPONSE);
    }

    public EfestoKafkaRuntimeEvaluateInputResponseMessage(EfestoOutput efestoOutput, long messageId) {
        this();
        this.efestoOutput = efestoOutput;
        this.messageId = messageId;
    }

    public EfestoOutput getEfestoOutput() {
        return efestoOutput;
    }

    public long getMessageId() {
        return messageId;
    }
}
