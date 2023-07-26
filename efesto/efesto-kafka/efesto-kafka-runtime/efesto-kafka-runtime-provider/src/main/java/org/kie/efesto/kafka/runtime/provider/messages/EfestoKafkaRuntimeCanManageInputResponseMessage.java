package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

/**
 * Message published by <code>KafkaKieRuntimeService</code> to notify if it can manage a given <code>EfestoInput</code>
 */
public class EfestoKafkaRuntimeCanManageInputResponseMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected boolean canManage;
    protected Long messageId;

    public EfestoKafkaRuntimeCanManageInputResponseMessage() {
        super(EfestoKafkaMessagingType.RUNTIMECANMANAGEINPUTRESPONSE);
    }

    public EfestoKafkaRuntimeCanManageInputResponseMessage(boolean canManage, long messageId) {
        this();
        this.canManage = canManage;
        this.messageId = messageId;
    }

    public boolean isCanManage() {
        return canManage;
    }

    public long getMessageId() {
        return messageId;
    }
}
