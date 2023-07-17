package org.kie.pmml.evaluator.core.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMMLRequestData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PMMLRequestDataDeserializerTest {

    @Test
    void deserializeTest() throws IOException {
        String json = "{\"correlationId\":\"CORRELATION_ID\",\"modelName\":\"Forecast Score\",\"source\":null,\"requestParams\":[{\"correlationId\":\"CORRELATION_ID\",\"name\":\"worldcontinent\",\"capitalizedName\":\"Worldcontinent\",\"type\":\"java.lang.String\",\"value\":\"EUROPE\"},{\"correlationId\":\"CORRELATION_ID\",\"name\":\"precipitation\",\"capitalizedName\":\"Precipitation\",\"type\":\"java.lang.Boolean\",\"value\":true},{\"correlationId\":\"CORRELATION_ID\",\"name\":\"period\",\"capitalizedName\":\"Period\",\"type\":\"java.lang.String\",\"value\":\"SPRING\"},{\"correlationId\":\"CORRELATION_ID\",\"name\":\"humidity\",\"capitalizedName\":\"Humidity\",\"type\":\"java.lang.Double\",\"value\":25.0}],\"mappedRequestParams\":{\"worldcontinent\":{\"correlationId\":\"CORRELATION_ID\",\"name\":\"worldcontinent\",\"capitalizedName\":\"Worldcontinent\",\"type\":\"java.lang.String\",\"value\":\"EUROPE\"},\"precipitation\":{\"correlationId\":\"CORRELATION_ID\",\"name\":\"precipitation\",\"capitalizedName\":\"Precipitation\",\"type\":\"java.lang.Boolean\",\"value\":true},\"period\":{\"correlationId\":\"CORRELATION_ID\",\"name\":\"period\",\"capitalizedName\":\"Period\",\"type\":\"java.lang.String\",\"value\":\"SPRING\"},\"humidity\":{\"correlationId\":\"CORRELATION_ID\",\"name\":\"humidity\",\"capitalizedName\":\"Humidity\",\"type\":\"java.lang.Double\",\"value\":25.0}},\"compactCapitalizedModelName\":\"ForecastScore\"}";
        ObjectMapper mapper = new ObjectMapper();
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        PMMLRequestData retrieved = new PMMLRequestDataDeserializer().deserialize(parser, ctxt);
        assertNotNull(retrieved);
    }
}