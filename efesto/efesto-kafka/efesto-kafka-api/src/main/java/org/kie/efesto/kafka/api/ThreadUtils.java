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
package org.kie.efesto.kafka.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ThreadUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    /**
     * Thread to be used when the "Consume" execution involve the invocation of a "Produce"
     *
     * @param consumer
     * @param producerConsumer
     * @param threadName
     * @param consumeAndProduceFunction
     * @return
     */
    public static Thread getConsumeAndProduceThread(Consumer<Long, JsonNode> consumer,
                                                    java.util.function.Supplier producerConsumer,
                                                    String threadName,
                                                    java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.Supplier> consumeAndProduceFunction) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeAndProduceRecord(record, producerConsumer, consumeAndProduceFunction));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Thread to be used when the "Consume" execution involve the invocation of a "Produce"
     *
     * @param consumer
     * @param producerConsumer
     * @param threadName
     * @param consumeAndProduceFunction
     * @return
     */
    public static Thread getConsumeAndProduceThread(Consumer<Long, JsonNode> consumer,
                                                    final java.util.function.BiFunction producerConsumer,
                                                    String threadName,
                                                    java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.BiFunction> consumeAndProduceFunction) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeAndProduceRecord(record, producerConsumer, consumeAndProduceFunction));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Thread to be used when the "Consume" execution involve the invocation of a "Produce"
     *
     * @param consumer
     * @param producerConsumer
     * @param threadName
     * @param consumeAndProduceFunction
     * @return
     */
    public static Thread getConsumeAndProduceThread(Consumer<Long, JsonNode> consumer,
                                                    final java.util.function.Function producerConsumer,
                                                    String threadName,
                                                    java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.Function> consumeAndProduceFunction) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeAndProduceRecord(record, producerConsumer, consumeAndProduceFunction));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Thread to be used when there is only the "Consume" part to be executed
     *
     * @param consumer
     * @param threadName
     * @param consumeRecordFunction
     * @return
     */
    public static Thread getConsumeThread(Consumer<Long, JsonNode> consumer,
                                          String threadName,
                                          java.util.function.Consumer<ConsumerRecord<Long, JsonNode>> consumeRecordFunction) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeRecord(record, consumeRecordFunction));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Thread to be used when there is a "Listener" listening on the "Consumer" part, anmd also a "Produce" has to be invoked on received message
     *
     * @param consumer
     * @param threadName
     * @param consumerRecordFunction    The function to transform a <code>ConsumerRecord</code> to an <code>AbstractEfestoKafkaMessage</code>
     * @param consumeAndProduceFunction The bi-function that consume the record (using the previous one, produce a record, and return an <code>AbstractEfestoKafkaMessage</code>
     * @param listeners
     * @return
     */
    public static Thread getConsumeAndProduceAndListenThread(Consumer<Long, JsonNode> consumer,
                                                             String threadName,
                                                             Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage> consumerRecordFunction,
                                                             BiFunction<ConsumerRecord<Long, JsonNode>, Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage>, AbstractEfestoKafkaMessage> consumeAndProduceFunction,
                                                             Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeAndProduceAndListenRecord(record, consumerRecordFunction, consumeAndProduceFunction, listeners));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Thread to be used when there is a "Listener" listening on the "Consumer" part
     *
     * @param consumer
     * @param threadName
     * @param listeners
     * @return
     */
    public static Thread getConsumeAndListenThread(Consumer<Long, JsonNode> consumer,
                                                   String threadName,
                                                   Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage> consumerRecordFunction,
                                                   Collection<EfestoKafkaMessageListener> listeners) {
        logger.info("Retrieving thread for {}", threadName);
        return new Thread(threadName) {
            @Override
            public void run() {
                while (true) {
                    try {
                        final ConsumerRecords<Long, JsonNode> consumerRecords =
                                consumer.poll(Duration.ofMillis(1000));
                        if (consumerRecords.count() > 0) {
                            logger.info("Consuming {} records from {}", consumerRecords.count(), threadName);
                            consumerRecords.forEach(record -> consumeAndListenRecord(record, consumerRecordFunction, listeners));
                            consumer.commitAsync();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    static void consumeRecord(ConsumerRecord<Long, JsonNode> toConsume, java.util.function.Consumer<ConsumerRecord<Long, JsonNode>> consumerRecordFunction) {
        try {
            logger.info("Consumer Record:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            consumerRecordFunction.accept(toConsume);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    static void consumeAndListenRecord(ConsumerRecord<Long, JsonNode> toConsume,
                                       java.util.function.Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage> consumerRecordFunction,
                                       Collection<EfestoKafkaMessageListener> listeners) {
        try {
            logger.info("Consumer Record:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            AbstractEfestoKafkaMessage message = consumerRecordFunction.apply(toConsume);
            if (message != null) {
                listeners.forEach(listener -> listener.onMessageReceived(message));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    static void consumeAndProduceAndListenRecord(ConsumerRecord<Long, JsonNode> toConsume,
                                                 Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage> producerConsumer,
                                                 BiFunction<ConsumerRecord<Long, JsonNode>, Function<ConsumerRecord<Long, JsonNode>, AbstractEfestoKafkaMessage>, AbstractEfestoKafkaMessage> consumeAndProduceFunction,
                                                 Collection<EfestoKafkaMessageListener> listeners) {
        try {
            logger.info("consumeAndProduceAndListenRecord:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            AbstractEfestoKafkaMessage message = consumeAndProduceFunction.apply(toConsume, producerConsumer);
            if (message != null) {
                listeners.forEach(listener -> listener.onMessageReceived(message));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static void consumeAndProduceRecord(ConsumerRecord<Long, JsonNode> toConsume,
                                        java.util.function.Supplier producerConsumer,
                                        java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.Supplier> consumeAndProduceFunction) {
        try {
            logger.info("consumeAndProduceRecordFunction:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            consumeAndProduceFunction.accept(toConsume, producerConsumer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static void consumeAndProduceRecord(ConsumerRecord<Long, JsonNode> toConsume,
                                        final java.util.function.BiFunction producerConsumer,
                                        java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.BiFunction> consumeAndProduceFunction) {
        try {
            logger.info("consumeAndProduceRecordFunction:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            consumeAndProduceFunction.accept(toConsume, producerConsumer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static void consumeAndProduceRecord(ConsumerRecord<Long, JsonNode> toConsume,
                                        final java.util.function.Function producerConsumer,
                                        java.util.function.BiConsumer<ConsumerRecord<Long, JsonNode>, java.util.function.Function> consumeAndProduceFunction) {
        try {
            logger.info("consumeAndProduceRecordFunction:({}, {}, {}, {})\n",
                    toConsume.key(), toConsume.value(),
                    toConsume.partition(), toConsume.offset());
            consumeAndProduceFunction.accept(toConsume, producerConsumer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
