package org.kie.efesto.kafka.runtime.provider.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

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
        AbstractEfestoKafkaRuntimeMessage retrieved = getObjectMapper().readValue(template, AbstractEfestoKafkaRuntimeMessage.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(EfestoKafkaRuntimeCanManageInputResponseMessage.class);
        boolean canManageInput = ((EfestoKafkaRuntimeCanManageInputResponseMessage) retrieved).isCanManage();
        assertThat(canManageInput).isTrue();
        long messageId = ((EfestoKafkaRuntimeCanManageInputResponseMessage) retrieved).getMessageId();
        assertThat(messageId).isEqualTo(10L);
    }

}