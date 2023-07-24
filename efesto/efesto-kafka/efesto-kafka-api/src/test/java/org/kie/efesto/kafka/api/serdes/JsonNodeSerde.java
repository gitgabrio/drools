package org.kie.efesto.kafka.api.serdes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;

public final class JsonNodeSerde extends Serdes.WrapperSerde<JsonNode> {
    public JsonNodeSerde() {
        super(new JsonSerializer(), new JsonDeserializer());
    }
}
