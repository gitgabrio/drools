package org.kie.efesto.kafka.compilation.gateway.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

public class EfestoKafkaCompilationSourceRequestMessage extends AbstractEfestoKafkaCompilationMessage {

    private static final long serialVersionUID = -3317609519791386488L;
    private String model;
    private String fileName;
    protected long messageId;

    public EfestoKafkaCompilationSourceRequestMessage() {
        super(EfestoKafkaMessagingType.COMPILATIONSERVICENOTIFICATION);
    }

    public EfestoKafkaCompilationSourceRequestMessage(String model, String fileName, long messageId) {
        this();
        this.model = model;
        this.fileName = fileName;
        this.messageId = messageId;
    }

    public String getModel() {
        return model;
    }

    public String getFileName() {
        return fileName;
    }

    public long getMessageId() {
        return messageId;
    }
}
