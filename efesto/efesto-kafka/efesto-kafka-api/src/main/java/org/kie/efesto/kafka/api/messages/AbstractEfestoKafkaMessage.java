package org.kie.efesto.kafka.api.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = EfestoKafkaMessagingType.class,
        property = "kind",
        visible = true)
public abstract class AbstractEfestoKafkaMessage implements Serializable {

    private static final long serialVersionUID = 6390584478643456223L;

    @JsonProperty("kind")
    protected EfestoKafkaMessagingType kind;


    protected AbstractEfestoKafkaMessage() {
    }

    protected AbstractEfestoKafkaMessage(EfestoKafkaMessagingType kind) {
        this.kind = kind;
    }

    public EfestoKafkaMessagingType getKind() {
        return kind;
    }


}