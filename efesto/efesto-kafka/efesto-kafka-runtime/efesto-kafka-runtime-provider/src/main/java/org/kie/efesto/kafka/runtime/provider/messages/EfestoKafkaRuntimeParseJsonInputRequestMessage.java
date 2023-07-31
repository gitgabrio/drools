package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

import java.io.Serializable;

/**
 * Message published by
 * {@link org.kie.efesto.kafka.runtime.provider.service.KafkaKieRuntimeServiceGateway#parseJsonInput(String, Serializable)}
 * to eventually retrieve a <code>EfestoInput</code>
 */
public class EfestoKafkaRuntimeParseJsonInputRequestMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected String modelLocalUriIdString;
    protected Serializable inputData;
    protected long messageId;

    public EfestoKafkaRuntimeParseJsonInputRequestMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEPARSEJSONINPUTREQUEST);
    }

    public EfestoKafkaRuntimeParseJsonInputRequestMessage(String modelLocalUriIdString, Serializable inputData, long messageId) {
        this();
        this.modelLocalUriIdString = modelLocalUriIdString;
        this.inputData = inputData;
        this.messageId = messageId;
    }

    public String getModelLocalUriIdString() {
        return modelLocalUriIdString;
    }

    public Serializable getInputData() {
        return inputData;
    }

    public long getMessageId() {
        return messageId;
    }
}
