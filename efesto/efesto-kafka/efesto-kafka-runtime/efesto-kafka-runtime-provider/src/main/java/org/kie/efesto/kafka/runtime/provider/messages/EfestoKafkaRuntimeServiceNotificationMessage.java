package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

import java.util.Objects;

/**
 * Message published by <code>KieRuntimeService</code>s to announce their availability
 */
public class EfestoKafkaRuntimeServiceNotificationMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    private String model;
    private EfestoClassKey efestoClassKey;

    public EfestoKafkaRuntimeServiceNotificationMessage() {
        super(EfestoKafkaMessagingType.RUNTIMESERVICENOTIFICATION);
    }

    public EfestoKafkaRuntimeServiceNotificationMessage(String model, EfestoClassKey efestoClassKey) {
        this();
        this.model = model;
        this.efestoClassKey = efestoClassKey;
    }

    public EfestoClassKey getEfestoClassKey() {
        return efestoClassKey;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "EfestoKafkaRuntimeServiceNotificationMessage{" +
                "model='" + model + '\'' +
                ", efestoClassKey=" + efestoClassKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfestoKafkaRuntimeServiceNotificationMessage that = (EfestoKafkaRuntimeServiceNotificationMessage) o;
        return Objects.equals(model, that.model) && Objects.equals(efestoClassKey, that.efestoClassKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, efestoClassKey);
    }
}
