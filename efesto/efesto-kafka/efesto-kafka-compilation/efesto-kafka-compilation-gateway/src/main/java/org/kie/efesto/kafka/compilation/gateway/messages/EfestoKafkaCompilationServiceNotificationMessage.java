/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.compilation.gateway.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

import java.util.Objects;

/**
 * Message published by <code>KieCompilationService</code>s to announce their availability
 */
public class EfestoKafkaCompilationServiceNotificationMessage extends AbstractEfestoKafkaCompilationMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    private String model;

    public EfestoKafkaCompilationServiceNotificationMessage() {
        super(EfestoKafkaMessagingType.COMPILATIONSERVICENOTIFICATION);
    }

    public EfestoKafkaCompilationServiceNotificationMessage(String model) {
        this();
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "EfestoKafkaCompilationServiceNotificationMessage{" +
                "model='" + model + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfestoKafkaCompilationServiceNotificationMessage that = (EfestoKafkaCompilationServiceNotificationMessage) o;
        return Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model);
    }
}
