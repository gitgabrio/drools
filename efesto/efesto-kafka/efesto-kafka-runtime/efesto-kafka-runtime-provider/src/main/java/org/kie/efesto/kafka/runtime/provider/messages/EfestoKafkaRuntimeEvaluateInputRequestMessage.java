package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;

/**
 * Message published by
 * {@link org.kie.efesto.kafka.runtime.provider.service.KafkaKieRuntimeServiceGateway#evaluateInput(EfestoInput, EfestoRuntimeContext)}
 * to eventually retrieve an <code>EfestoOutput</code>
 */
public class EfestoKafkaRuntimeEvaluateInputRequestMessage extends AbstractEfestoKafkaRuntimeMessage {


    private static final long serialVersionUID = -3245575207429193772L;
    protected EfestoInput efestoInput;
    protected long messageId;

    public EfestoKafkaRuntimeEvaluateInputRequestMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTREQUEST);
    }

    public EfestoKafkaRuntimeEvaluateInputRequestMessage(EfestoInput efestoInput, long messageId) {
        this();
        this.efestoInput = efestoInput;
        this.messageId = messageId;
    }

    public EfestoInput getEfestoInput() {
        return efestoInput;
    }

    public long getMessageId() {
        return messageId;
    }
}
