package org.kie.efesto.kafka.compilation.gateway.messages;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

public class EfestoKafkaCompilationSourceResponseMessage extends AbstractEfestoKafkaCompilationMessage {

    private static final long serialVersionUID = -3317609519791386488L;
    private ModelLocalUriId modelLocalUriId;
    private String source;
    protected Long messageId;

    public EfestoKafkaCompilationSourceResponseMessage() {
        super(EfestoKafkaMessagingType.COMPILATIONSERVICENOTIFICATION);
    }

    public EfestoKafkaCompilationSourceResponseMessage(ModelLocalUriId modelLocalUriId, String source, long messageId) {
        this();
        this.modelLocalUriId = modelLocalUriId;
        this.source = source;
        this.messageId = messageId;
    }

    public ModelLocalUriId getModelLocalUriId() {
        return modelLocalUriId;
    }

    public String getSource() {
        return source;
    }

    public long getMessageId() {
        return messageId;
    }
}
