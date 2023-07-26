package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;
import org.kie.efesto.runtimemanager.core.serialization.EfestoInputDeserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

public class EfestoKafkaRuntimeCanManageInputResponseMessageTest {

    private static final String template = "{\"canManage\":true,\"messageId\":10,\"kind\":\"RUNTIMECANMANAGEINPUTRESPONSE\"}";


    @Test
    void serializeTest() throws JsonProcessingException {
        EfestoKafkaRuntimeCanManageInputResponseMessage toSerialize = new EfestoKafkaRuntimeCanManageInputResponseMessage(true, 10L);
        String retrieved = getObjectMapper().writeValueAsString(toSerialize);
        assertThat(retrieved).isNotNull().isEqualTo(template);
    }

    @Test
    void deserializeTest() throws JsonProcessingException {
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        AbstractEfestoKafkaRuntimeMessage retrieved = mapper.readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputResponseMessage.class);
        boolean canManageInput = ((EfestoKafkaRuntimeCanManageInputResponseMessage) retrieved).isCanManage();
        assertThat(canManageInput).isTrue();
        long messageId = ((EfestoKafkaRuntimeCanManageInputResponseMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}