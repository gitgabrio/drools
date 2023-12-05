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
package org.kie.efesto.kafka.compilation.gateway.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

@JsonSubTypes({
        @JsonSubTypes.Type(value = EfestoKafkaCompilationServiceDiscoverMessage.class, name = "COMPILATIONSERVICEDISCOVER"),
        @JsonSubTypes.Type(value = EfestoKafkaCompilationServiceNotificationMessage.class, name = "COMPILATIONSERVICENOTIFICATION"),
        @JsonSubTypes.Type(value = EfestoKafkaCompilationSourceRequestMessage.class, name = "COMPILATIONSERVICESOURCEREQUEST"),
        @JsonSubTypes.Type(value = EfestoKafkaCompilationSourceResponseMessage.class, name = "COMPILATIONSERVICESOURCERESPONSE")
})
public abstract class AbstractEfestoKafkaCompilationMessage extends AbstractEfestoKafkaMessage {

    private static final long serialVersionUID = -5977489041620927028L;

    public AbstractEfestoKafkaCompilationMessage(EfestoKafkaMessagingType kind) {
        super(kind);
    }
}
