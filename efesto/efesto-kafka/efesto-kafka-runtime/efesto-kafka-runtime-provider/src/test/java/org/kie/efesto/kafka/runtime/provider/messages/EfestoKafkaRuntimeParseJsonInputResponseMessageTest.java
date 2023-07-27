package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeParseJsonInputResponseMessageTest {

    private static final String template = "{\"efestoInput\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"inputData\":\"MockEfestoInputA\",\"firstLevelCacheKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"},\"secondLevelCacheKey\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"efestoClassKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"}}},\"messageId\":10,\"kind\":\"RUNTIMEPARSEJSONINPUTRESPONSE\"}";

    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoInput originalInput = new MockEfestoInputA();
        EfestoKafkaRuntimeParseJsonInputResponseMessage toSerialize = new EfestoKafkaRuntimeParseJsonInputResponseMessage(originalInput, 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeParseJsonInputResponseMessage.class);
        EfestoInput retrievedInput = ((EfestoKafkaRuntimeParseJsonInputResponseMessage) retrieved).getEfestoInput();
        assertThat(retrievedInput).isNotNull();
        EfestoInput expectedInput = new MockEfestoInputA();
        assertThat(retrievedInput).isEqualTo(expectedInput);
        long messageId = ((EfestoKafkaRuntimeParseJsonInputResponseMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}