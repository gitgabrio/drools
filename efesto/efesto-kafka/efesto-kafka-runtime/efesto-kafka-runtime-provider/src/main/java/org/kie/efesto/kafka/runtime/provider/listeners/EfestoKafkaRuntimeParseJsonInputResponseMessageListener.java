package org.kie.efesto.kafka.runtime.provider.listeners;

import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeParseJsonInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;

import java.util.HashMap;
import java.util.Map;

public class EfestoKafkaRuntimeParseJsonInputResponseMessageListener implements EfestoKafkaMessageListener {
    private Map<Long, EfestoKafkaRuntimeParseJsonInputResponseMessage> receivedResponseMessages = new HashMap<>();

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        if (received instanceof EfestoKafkaRuntimeParseJsonInputResponseMessage) {
            receivedResponseMessages.put(((EfestoKafkaRuntimeParseJsonInputResponseMessage) received).getMessageId(), (EfestoKafkaRuntimeParseJsonInputResponseMessage) received);
        }
    }

    public EfestoInput getEfestoInput(long messageId) {
        return receivedResponseMessages.containsKey(messageId) ? receivedResponseMessages.get(messageId).getEfestoInput() : null;
    }
}
