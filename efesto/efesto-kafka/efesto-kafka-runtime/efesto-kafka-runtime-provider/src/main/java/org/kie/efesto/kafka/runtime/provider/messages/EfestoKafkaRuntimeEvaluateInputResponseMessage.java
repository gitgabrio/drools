package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

/**
 * Message published by <code>KafkaKieRuntimeService</code> to notify the <code>EfestoInput</code> evaluation
 */
public class EfestoKafkaRuntimeEvaluateInputResponseMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 7672828798629469119L;
    protected EfestoOutput efestoOutput;
    protected Long messageId;

    public EfestoKafkaRuntimeEvaluateInputResponseMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTRESPONSE);
    }

    public EfestoKafkaRuntimeEvaluateInputResponseMessage(EfestoOutput efestoOutput, long messageId) {
        this();
        this.efestoOutput = efestoOutput;
        this.messageId = messageId;
    }

    public EfestoOutput getEfestoOutput() {
        return efestoOutput;
    }

    public long getMessageId() {
        return messageId;
    }
}
