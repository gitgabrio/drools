package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;

/**
 * Message published by <code>KafkaKieRuntimeService</code> to notify the <code>EfestoInput</code> generation
 */
public class EfestoKafkaRuntimeParseJsonInputResponseMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected EfestoInput efestoInput;
    protected Long messageId;

    public EfestoKafkaRuntimeParseJsonInputResponseMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEPARSEJSONINPUTRESPONSE);
    }

    public EfestoKafkaRuntimeParseJsonInputResponseMessage(EfestoInput efestoInput, long messageId) {
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
