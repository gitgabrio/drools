package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeEvaluateInputRequestMessageTest {

    private static final String template = "{\"modelLocalUriIdString\":\"{\\\"model\\\":\\\"example\\\",\\\"basePath\\\":\\\"/some-id/instances/some-instance-id\\\",\\\"fullPath\\\":\\\"/example/some-id/instances/some-instance-id\\\"}\",\"inputDataString\":\"inputDataString\",\"messageId\":10,\"kind\":\"RUNTIMEEVALUATEINPUTREQUEST\"}";


    @Test
    void serializeTest() throws JsonProcessingException {
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriId = new ModelLocalUriId(parsed);
        String modelLocalUriIDString = getObjectMapper().writeValueAsString(modelLocalUriId);
        EfestoKafkaRuntimeEvaluateInputRequestMessage toSerialize = new EfestoKafkaRuntimeEvaluateInputRequestMessage(modelLocalUriIDString, "inputDataString", 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeEvaluateInputRequestMessage.class);
        String modelLocalUriIdString = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getModelLocalUriIdString();
        assertThat(modelLocalUriIdString).isNotNull().isNotEmpty();
        ModelLocalUriId modelLocalUriIdRetrieved = getObjectMapper().readValue(modelLocalUriIdString, ModelLocalUriId.class);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriIdExpected = new ModelLocalUriId(parsed);
        assertThat(modelLocalUriIdRetrieved).isEqualTo(modelLocalUriIdExpected);
        String inputDataRetrieved = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getInputDataString();
        String inputDataStringExpected = "inputDataString";
        assertThat(inputDataRetrieved).isEqualTo(inputDataStringExpected);
        long messageId = ((EfestoKafkaRuntimeEvaluateInputRequestMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}