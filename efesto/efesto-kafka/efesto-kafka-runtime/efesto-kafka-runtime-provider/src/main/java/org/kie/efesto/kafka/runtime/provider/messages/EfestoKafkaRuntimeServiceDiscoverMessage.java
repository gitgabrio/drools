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

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

/**
 * Message published by <code>KafkaRuntimeServiceProvider</code> to discover <code>KieRuntimeService</code>s availability
 */
public class EfestoKafkaRuntimeServiceDiscoverMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected ModelLocalUriId modelLocalUriId;

    public EfestoKafkaRuntimeServiceDiscoverMessage() {
        super(EfestoKafkaMessagingType.RUNTIMESERVICEDISCOVER);
    }

    public EfestoKafkaRuntimeServiceDiscoverMessage(ModelLocalUriId modelLocalUriId) {
        this();
        this.modelLocalUriId = modelLocalUriId;
    }

    public ModelLocalUriId getModelLocalUriId() {
        return modelLocalUriId;
    }
}
