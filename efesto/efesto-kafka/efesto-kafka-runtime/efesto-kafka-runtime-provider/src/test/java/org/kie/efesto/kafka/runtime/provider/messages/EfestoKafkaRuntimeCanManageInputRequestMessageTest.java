package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.kie.efesto.kafka.runtime.provider.model.EfestoKafkaRuntimeContextImpl;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeCanManageInputRequestMessageTest {


    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoInput input = new MockEfestoInputA();
        EfestoKafkaRuntimeCanManageInputRequestMessage toSerialize = new EfestoKafkaRuntimeCanManageInputRequestMessage(input, 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        String expected = "{\"efestoInput\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"inputData\":\"MockEfestoInputA\",\"firstLevelCacheKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"},\"secondLevelCacheKey\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"efestoClassKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"}}},\"messageId\":10,\"kind\":\"RUNTIMECANMANAGEINPUTREQUEST\"}";
        assertThat(retrieved).isNotNull().isEqualTo(expected);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"efestoInput\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"inputData\":\"MockEfestoInputA\",\"firstLevelCacheKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"},\"secondLevelCacheKey\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"efestoClassKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"}}},\"messageId\":10,\"kind\":\"RUNTIMECANMANAGEINPUTREQUEST\"}";
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        AbstractEfestoKafkaRuntimeMessage retrieved = mapper.readValue(toDeserialize, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputRequestMessage.class);
        EfestoInput efestoInput = ((EfestoKafkaRuntimeCanManageInputRequestMessage) retrieved).getEfestoInput();
        assertThat(efestoInput).isNotNull().isEqualTo(new MockEfestoInputA());
        long messageId = ((EfestoKafkaRuntimeCanManageInputRequestMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}