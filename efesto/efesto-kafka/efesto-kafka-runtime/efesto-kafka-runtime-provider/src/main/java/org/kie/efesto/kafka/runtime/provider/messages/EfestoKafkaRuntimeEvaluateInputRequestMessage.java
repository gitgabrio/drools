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
    protected String modelLocalUriIdString;
    protected String inputDataString;
    protected long messageId;

    public EfestoKafkaRuntimeEvaluateInputRequestMessage() {
        super(EfestoKafkaMessagingType.RUNTIMEEVALUATEINPUTREQUEST);
    }

    public EfestoKafkaRuntimeEvaluateInputRequestMessage(String modelLocalUriIdString, String inputData, long messageId) {
        this();
        this.modelLocalUriIdString = modelLocalUriIdString;
        this.inputDataString = inputData;
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
