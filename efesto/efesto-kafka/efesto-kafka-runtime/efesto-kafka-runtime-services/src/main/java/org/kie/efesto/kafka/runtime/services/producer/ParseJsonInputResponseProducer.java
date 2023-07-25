package org.kie.efesto.kafka.runtime.services.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC;

public class ParseJsonInputResponseProducer {

    private static final Logger logger = LoggerFactory.getLogger(ParseJsonInputResponseProducer.class);

    private static final AtomicLong COUNTER = new AtomicLong();


    public static void runProducer(EfestoInput toPublish) {
        logger.info("runProducer");
        final Producer<Long, JsonNode> producer = createProducer();
        runProducer(producer, toPublish);
    }

    public static void runProducer(final Producer<Long, JsonNode> producer, EfestoInput toPublish) {
        logger.info("runProducer {}", producer);
        long time = System.currentTimeMillis();

        try {
            JsonNode jsonNode = getJsonNode(toPublish);
            final ProducerRecord<Long, JsonNode> record =
                    new ProducerRecord<>(RUNTIMESERVICE_PARSEJSONINPUTRESPONSE_TOPIC, COUNTER.incrementAndGet(), jsonNode);

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

    static JsonNode getJsonNode(EfestoInput toPublish) {
        EfestoKafkaRuntimeParseJsonInputResponseMessage responseMessage = new EfestoKafkaRuntimeParseJsonInputResponseMessage(toPublish);
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        return mapper.valueToTree(responseMessage);
    }

    private static Producer<Long, JsonNode> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, ParseJsonInputResponseProducer.class.getSimpleName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

}
