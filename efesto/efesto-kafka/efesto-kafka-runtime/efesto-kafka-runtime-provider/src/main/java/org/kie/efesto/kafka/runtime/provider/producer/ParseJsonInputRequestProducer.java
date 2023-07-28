package org.kie.efesto.kafka.runtime.provider.producer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC;

public class ParseJsonInputRequestProducer {

    private static final Logger logger = LoggerFactory.getLogger(ParseJsonInputRequestProducer.class);

    private static final AtomicLong COUNTER = new AtomicLong();


    public static long runProducer(String modelLocalUriIdString, Serializable inputData) {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer();
        return runProducer(producer, modelLocalUriIdString, inputData);
    }

    public static long runProducer(final Producer<Long, JsonNode> producer, String modelLocalUriIdString, Serializable inputData) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();

        try {
            long messageId = COUNTER.incrementAndGet();
            JsonNode jsonNode = getJsonNode(modelLocalUriIdString, inputData, messageId);
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC, messageId, jsonNode);

            RecordMetadata metadata = producer.send(record).get();

            long elapsedTime = System.currentTimeMillis() - time;
            logger.info("sent record(key={} value={}) " +
                            "meta(partition={}, offset={}) time={}\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
            return messageId;
        } catch (Exception e) {
            throw new KieEfestoCommonException(e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    static JsonNode getJsonNode(String modelLocalUriIdString, Serializable inputData, long messageId) {
        EfestoKafkaRuntimeParseJsonInputRequestMessage requestMessage = new EfestoKafkaRuntimeParseJsonInputRequestMessage(modelLocalUriIdString, inputData, messageId);
        return getObjectMapper().valueToTree(requestMessage);
    }

    private static Producer<Long, JsonNode> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, ParseJsonInputRequestProducer.class.getSimpleName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

}
