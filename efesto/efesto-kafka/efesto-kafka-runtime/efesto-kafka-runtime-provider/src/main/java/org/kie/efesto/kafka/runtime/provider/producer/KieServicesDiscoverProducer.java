package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeServiceDiscoverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_DISCOVER_TOPIC;

public class KieServicesDiscoverProducer {

    private static final Logger logger = LoggerFactory.getLogger(KieServicesDiscoverProducer.class);

    private static final AtomicLong COUNTER = new AtomicLong();


    public static void runProducer() {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer();
        runProducer(producer);
    }

    public static void runProducer(final Producer<Long, JsonNode> producer) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();
        try {
            JsonNode jsonNode = getJsonNode();
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(RUNTIMESERVICE_DISCOVER_TOPIC, COUNTER.incrementAndGet(), jsonNode);

            RecordMetadata metadata = producer.send(record).get();

            long elapsedTime = System.currentTimeMillis() - time;
            logger.info("sent record(key={} value={}) " +
                            "meta(partition={}, offset={}) time={}\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
        } catch (Exception e) {
            throw new KieEfestoCommonException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    static JsonNode getJsonNode() {
        return getObjectMapper().valueToTree(new EfestoKafkaRuntimeServiceDiscoverMessage());
    }

    private static Producer<Long, JsonNode> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KieServicesDiscoverProducer.class.getSimpleName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class.getName());
        return new KafkaProducer<>(props);
    }


}
