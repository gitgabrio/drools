/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.efesto.kafka.api.serialization;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EfestoLongDeserializer extends LongDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(EfestoLongDeserializer.class);

    @Override
    public Long deserialize(String topic, byte[] data) {
        try {
            if (data.length > 8) {
                data = Arrays.copyOfRange(data, 0, 8);
            } else if (data.length < 8) {
                byte[] newArray = new byte[8];
                int destPosition = newArray.length - data.length;
                System.arraycopy(data, 0, newArray, destPosition, data.length);
                data = newArray;
            }
            return super.deserialize(topic, data);
        } catch (SerializationException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }


}
