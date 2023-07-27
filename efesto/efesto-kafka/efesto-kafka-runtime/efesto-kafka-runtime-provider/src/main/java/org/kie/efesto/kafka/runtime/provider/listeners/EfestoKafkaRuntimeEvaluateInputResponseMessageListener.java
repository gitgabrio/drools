package org.kie.efesto.kafka.runtime.provider.listeners;

import org.kie.efesto.kafka.api.listeners.EfestoKafkaMessageListener;
import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;
import org.kie.efesto.kafka.runtime.provider.messages.EfestoKafkaRuntimeEvaluateInputResponseMessage;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

import java.util.HashMap;
import java.util.Map;

public class EfestoKafkaRuntimeEvaluateInputResponseMessageListener implements EfestoKafkaMessageListener {
    private Map<Long, EfestoKafkaRuntimeEvaluateInputResponseMessage> receivedResponseMessages = new HashMap<>();

    @Override
    public void onMessageReceived(AbstractEfestoKafkaMessage received) {
        if (received instanceof EfestoKafkaRuntimeEvaluateInputResponseMessage) {
            receivedResponseMessages.put(((EfestoKafkaRuntimeEvaluateInputResponseMessage) received).getMessageId(), (EfestoKafkaRuntimeEvaluateInputResponseMessage) received);
        }
    }

    public EfestoOutput getEfestoOutput(long messageId) {
        return receivedResponseMessages.containsKey(messageId) ? receivedResponseMessages.get(messageId).getEfestoOutput() : null;
    }
}
