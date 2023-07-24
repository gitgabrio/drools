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
package org.kie.efesto.kafka.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    public static Thread getConsumerThread(Consumer<Long, JsonNode> consumer,
                                           int giveUp,
                                           String threadName,
                                           java.util.function.Consumer<ConsumerRecord<Long, JsonNode>> consumerRecordFunction) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                final AtomicInteger noRecordsCount = new AtomicInteger(0);
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(100));
                        if (consumerRecords.count() == 0) {
                            int currentNoRecordsCount = noRecordsCount.addAndGet(1);
                            if (currentNoRecordsCount > giveUp) {
//                            break;
                            } else {
                                continue;
                            }
                        }
                        consumerRecords.forEach(record -> consumeRecord(record, consumerRecordFunction, consumer));
                        consumer.commitAsync();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
//                consumer.close();
//                logger.info("DONE");
            }
        };
    }

    static void consumeRecord(ConsumerRecord<Long, JsonNode> toConsume, java.util.function.Consumer<ConsumerRecord<Long, JsonNode>> consumerRecordFunction, Consumer<Long, JsonNode> consumer) {
        try {
            logger.info("Consumer Record:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            consumerRecordFunction.accept(toConsume);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
