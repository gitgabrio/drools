package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.LocalUri;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeServiceDiscoverMessageTest {


    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoKafkaRuntimeServiceDiscoverMessage toSerialize = new EfestoKafkaRuntimeServiceDiscoverMessage();
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        String expected = "{\"modelLocalUriId\":null,\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId modelLocalUriId = new ModelLocalUriId(parsed);
        toSerialize = new EfestoKafkaRuntimeServiceDiscoverMessage(modelLocalUriId);
        retrieved = getObjectMapper().writeValueAsString(toSerialize);
        expected = "{\"modelLocalUriId\":{\"model\":\"example\",\"basePath\":\"/some-id/instances/some-instance-id\",\"fullPath\":\"/example/some-id/instances/some-instance-id\"},\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
        assertThat(((EfestoKafkaRuntimeServiceDiscoverMessage)retrieved).getModelLocalUriId()).isNull();
        toDeserialize = "{\"modelLocalUriId\":{\"model\":\"example\",\"basePath\":\"/some-id/instances/some-instance-id\",\"fullPath\":\"/example/some-id/instances/some-instance-id\"},\"kind\":\"RUNTIMESERVICEDISCOVER\"}";
        retrieved = getObjectMapper().readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeServiceDiscoverMessage.class);
        String path = "/example/some-id/instances/some-instance-id";
        LocalUri parsed = LocalUri.parse(path);
        ModelLocalUriId expectedModelLocalUriId = new ModelLocalUriId(parsed);
        assertThat(((EfestoKafkaRuntimeServiceDiscoverMessage)retrieved).getModelLocalUriId()).isNotNull().isEqualTo(expectedModelLocalUriId);
    }

}