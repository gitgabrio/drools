package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeParseJsonInputRequestMessageTest {


    @Test
    void serializeTest() throws JsonProcessingException {
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriId = new ModelLocalUriId(parsed);
        String modelLocalUriIDString = getObjectMapper().writeValueAsString(modelLocalUriId);
        EfestoKafkaRuntimeParseJsonInputRequestMessage toSerialize = new EfestoKafkaRuntimeParseJsonInputRequestMessage(modelLocalUriIDString, "inputDataString", 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        String expected = "{\"modelLocalUriIdString\":\"{\\\"model\\\":\\\"example\\\",\\\"basePath\\\":\\\"/some-id/instances/some-instance-id\\\",\\\"fullPath\\\":\\\"/example/some-id/instances/some-instance-id\\\"}\",\"inputDataString\":\"inputDataString\",\"messageId\":10,\"kind\":\"RUNTIMEPARSEJSONINPUTREQUEST\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"modelLocalUriIdString\":\"{\\\"model\\\":\\\"example\\\",\\\"basePath\\\":\\\"/some-id/instances/some-instance-id\\\",\\\"fullPath\\\":\\\"/example/some-id/instances/some-instance-id\\\"}\",\"inputDataString\":\"inputDataString\",\"messageId\":10,\"kind\":\"RUNTIMEPARSEJSONINPUTREQUEST\"}";
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputRequestMessage.class);
        String modelLocalUriIdString = ((EfestoKafkaRuntimeParseJsonInputRequestMessage) retrieved).getModelLocalUriIdString();
        assertThat(modelLocalUriIdString).isNotNull().isNotEmpty();
        ModelLocalUriId modelLocalUriIdRetrieved = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriIdExpected = new ModelLocalUriId(parsed);
        assertThat(modelLocalUriIdRetrieved).isEqualTo(modelLocalUriIdExpected);
        String inputDataStringRetrieved = ((EfestoKafkaRuntimeParseJsonInputRequestMessage) retrieved).getInputDataString();
        assertThat(inputDataStringRetrieved).isNotNull().isNotEmpty();
        String inputDataStringExpected = "inputDataString";
        assertThat(inputDataStringRetrieved).isEqualTo(inputDataStringExpected);
        long messageId = ((EfestoKafkaRuntimeParseJsonInputRequestMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}