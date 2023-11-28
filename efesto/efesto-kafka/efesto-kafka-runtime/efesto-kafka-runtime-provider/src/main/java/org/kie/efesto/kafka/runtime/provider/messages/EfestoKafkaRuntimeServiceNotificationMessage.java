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

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

import java.util.Objects;

/**
 * Message published by <code>KieRuntimeService</code>s to announce their availability
 */
public class EfestoKafkaRuntimeServiceNotificationMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    private String model;
    private EfestoClassKey efestoClassKey;

    public EfestoKafkaRuntimeServiceNotificationMessage() {
        super(EfestoKafkaMessagingType.RUNTIMESERVICENOTIFICATION);
    }

    public EfestoKafkaRuntimeServiceNotificationMessage(String model, EfestoClassKey efestoClassKey) {
        this();
        this.model = model;
        this.efestoClassKey = efestoClassKey;
    }

    public EfestoClassKey getEfestoClassKey() {
        return efestoClassKey;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "EfestoKafkaRuntimeServiceNotificationMessage{" +
                "model='" + model + '\'' +
                ", efestoClassKey=" + efestoClassKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfestoKafkaRuntimeServiceNotificationMessage that = (EfestoKafkaRuntimeServiceNotificationMessage) o;
        return Objects.equals(model, that.model) && Objects.equals(efestoClassKey, that.efestoClassKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, efestoClassKey);
    }
}
