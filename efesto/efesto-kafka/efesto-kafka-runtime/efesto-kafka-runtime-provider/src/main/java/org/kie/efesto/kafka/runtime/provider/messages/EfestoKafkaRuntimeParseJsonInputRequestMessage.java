package org.kie.efesto.kafka.runtime.provider.messages;

import org.kie.efesto.kafka.api.messages.EfestoKafkaMessagingType;

/**
 * Message published by
 * {@link org.kie.efesto.kafka.runtime.provider.service.KafkaKieRuntimeServiceGateway#parseJsonInput(String, String)}
 * to eventually retrieve a <code>EfestoInput</code>
 */
public class EfestoKafkaRuntimeParseJsonInputRequestMessage extends AbstractEfestoKafkaRuntimeMessage {

    private static final long serialVersionUID = 3682343133142322558L;


    protected String modelLocalUriIdString;
    protected String inputDataString;
    protected long messageId;

    public EfestoKafkaRuntimeParseJsonInputRequestMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEPARSEJSONINPUTREQUEST);
    }

    public EfestoKafkaRuntimeParseJsonInputRequestMessage(String modelLocalUriIdString, String inputDataString, long messageId) {
        this();
        this.modelLocalUriIdString = modelLocalUriIdString;
        this.inputDataString = inputDataString;
        this.messageId = messageId;
    }

    public String getModelLocalUriIdString() {
        return modelLocalUriIdString;
    }

    public String getInputDataString() {
        return inputDataString;
    }

    public long getMessageId() {
        return messageId;
    }
}
