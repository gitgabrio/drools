package org.kie.efesto.kafka.runtime.provider.listeners;

import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeCanManageInputResponseMessage;

import java.util.HashMap;
import java.util.Map;

public class EfestoKafkaRuntimeCanManageInputResponseMessageListener implements EfestoKafkaMessageListener {
    private Map<Long, EfestoKafkaRuntimeCanManageInputResponseMessage> receivedResponseMessages = new HashMap<>();

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        if (received instanceof EfestoKafkaRuntimeCanManageInputResponseMessage) {
            receivedResponseMessages.put(((EfestoKafkaRuntimeCanManageInputResponseMessage) received).getMessageId(), (EfestoKafkaRuntimeCanManageInputResponseMessage) received);
        }
    }

    public Boolean getIsCanManage(long messageId) {
        return receivedResponseMessages.containsKey(messageId) ? receivedResponseMessages.get(messageId).isCanManage() : null;
    }
}
