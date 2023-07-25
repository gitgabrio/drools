package org.kie.efesto.kafka.runtime.services.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputRequestMessage;
import org.kie.efesto.kafka.runtime.services.producer.ParseJsonInputResponseProducer;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;
import static org.kie.efesto.kafka.api.KafkaConstants.RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC;
import static org.kie.efesto.kafka.runtime.services.consumer.ParseJsonInputRequestConsumer.parseJsonInput;
import static org.kie.efesto.kafka.runtime.services.consumer.ParseJsonInputRequestConsumer.receivedMessages;


class ParseJsonInputRequestConsumerTest {

    private static List<KieRuntimeService> KIERUNTIMESERVICES;

    @BeforeAll
    public static void setup() {
        KIERUNTIMESERVICES = SPIUtils.getKieRuntimeServices(true);
        assertThat(KIERUNTIMESERVICES).isNotNull().isNotEmpty();
    }


    @Test
    public void parseJsonInputConsumerTest() {
        TopicPartition topicPartition = new TopicPartition(RUNTIMESERVICE_PARSEJSONINPUTREQUEST_TOPIC, 0);
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        startOffsets.put(topicPartition, 0L);
        ConsumerRecord<Long, JsonNode> consumerRecord = getConsumerRecordWithoutModelLocalUriId(topicPartition);
        MockConsumer<Long, JsonNode> parseJsonInputRequestConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        MockProducer<Long, JsonNode> parseJsonInputResponseProducer = new MockProducer<>(true, new LongSerializer(), new JsonSerializer());
        BiFunction parseJsonInputProducer = (BiFunction<String, String, Boolean>) (modelLocalUriIdString, inputDataString) -> {
            Optional<EfestoInput> retrievedEfestoInput = KIERUNTIMESERVICES.stream().map(kieRuntimeService -> parseJsonInput(kieRuntimeService, modelLocalUriIdString, inputDataString))
                    .findFirst();
            retrievedEfestoInput.ifPresent(efestoInput -> ParseJsonInputResponseProducer.runProducer(parseJsonInputResponseProducer, efestoInput));
            return retrievedEfestoInput.isPresent();
        };
        try {
            assertThat(parseJsonInputResponseProducer.history()).isEmpty();
            ParseJsonInputRequestConsumer.startEvaluateConsumer(parseJsonInputRequestConsumer, parseJsonInputProducer);
            parseJsonInputRequestConsumer.updateBeginningOffsets(startOffsets);
            parseJsonInputRequestConsumer.assign(Collections.singleton(topicPartition));
            parseJsonInputRequestConsumer.addRecord(consumerRecord);
            List<EfestoKafkaRuntimeParseJsonInputRequestMessage> receivedMessages = receivedMessages();
            int counter = 0;
            while (receivedMessages.isEmpty() && counter < 10) {
                receivedMessages = receivedMessages();
                Thread.sleep(100);
                counter++;
            }
            assertThat(receivedMessages).hasSize(1);
            assertThat(parseJsonInputResponseProducer.history()).hasSize(1);
        } catch (Exception e) {
            fail("parseJsonInputConsumerTest failed", e);
        }
    }

    private ConsumerRecord<Long, JsonNode> getConsumerRecordWithoutModelLocalUriId(TopicPartition topicPartition) {
        return new ConsumerRecord<>(topicPartition.topic(), topicPartition.partition(), 0L, 1L, getJsonNodeWithoutModelLocalUriId());
    }

    private static JsonNode getJsonNodeWithoutModelLocalUriId() {
        String modelLocalUriIDString = "{\"model\":\"example\",\"basePath\":\"/some-id/instances/some-instance-id\",\"fullPath\":\"/example/some-id/instances/some-instance-id\"}";
        EfestoKafkaRuntimeParseJsonInputRequestMessage message = new EfestoKafkaRuntimeParseJsonInputRequestMessage(modelLocalUriIDString, "inputDataString");
        return getObjectMapper().valueToTree(message);
    }

}