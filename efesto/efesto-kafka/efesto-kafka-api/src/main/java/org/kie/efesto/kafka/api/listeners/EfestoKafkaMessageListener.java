package org.kie.efesto.kafka.api.listeners;

import org.kie.efesto.kafka.api.messages.AbstractEfestoKafkaMessage;

/**
 * Listener used to get notified of received <code>AbstractEfestoKafkaMessage</code>
 */
public interface EfestoKafkaMessageListener {

    void notificationMessageReceived(AbstractEfestoKafkaMessage received);
}
