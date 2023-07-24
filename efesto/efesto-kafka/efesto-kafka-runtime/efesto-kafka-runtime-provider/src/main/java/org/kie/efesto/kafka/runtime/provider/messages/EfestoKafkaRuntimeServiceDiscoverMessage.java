package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

/**
 * Message published by <code>KafkaRuntimeServiceProvider</code> to discover <code>KieRuntimeService</code>s availability
 */
public class EfestoKafkaRuntimeServiceDiscoverMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected ModelLocalUriId modelLocalUriId;

    public EfestoKafkaRuntimeServiceDiscoverMessage() {
        super(EfestoKafkaMessagingType.RUNTIMESERVICEDISCOVER);
    }

    public EfestoKafkaRuntimeServiceDiscoverMessage(ModelLocalUriId modelLocalUriId) {
        this();
        this.modelLocalUriId = modelLocalUriId;
    }

    public ModelLocalUriId getModelLocalUriId() {
        return modelLocalUriId;
    }
}
