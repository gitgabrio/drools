package org.kie.pmml.evaluator.core.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kie.pmml.api.identifiers.AbstractModelLocalUriIdPmml;
import org.kie.pmml.api.identifiers.LocalComponentIdPmml;
import org.kie.pmml.api.identifiers.LocalComponentIdRedirectPmml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AbstractModelLocalUriIdPmmlDeserializerTest {

    @Test
    void deserializeLocalComponentIdPmmlTest() throws IOException {
        String json = "{\"model\":\"pmml\",\"basePath\":\"/LoanApprovalRegression/LoanApprovalRegression\",\"fullPath\":\"/pmml/LoanApprovalRegression/LoanApprovalRegression\",\"fileName\":\"LoanApprovalRegression\"}\"}";
        ObjectMapper mapper = new ObjectMapper();
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        AbstractModelLocalUriIdPmml retrieved = new AbstractModelLocalUriIdPmmlDeserializer().deserialize(parser, ctxt);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof LocalComponentIdPmml);
        assertEquals("LoanApprovalRegression", retrieved.getFileName());
        assertEquals("LoanApprovalRegression", ((LocalComponentIdPmml)retrieved).name());
        assertEquals("pmml", ((LocalComponentIdPmml)retrieved).model());
    }

    @Test
    void deserializeLocalComponentIdRedirectPmmlTest() throws IOException {
        String json = "{\"model\":\"pmml\",\"basePath\":\"/LoanApprovalRegression/LoanApprovalRegression\",\"fullPath\":\"/dmn/LoanApprovalRegression/LoanApprovalRegression\",\"fileName\":\"LoanApprovalRegression\"}\"}";
        ObjectMapper mapper = new ObjectMapper();
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        AbstractModelLocalUriIdPmml retrieved = new AbstractModelLocalUriIdPmmlDeserializer().deserialize(parser, ctxt);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof LocalComponentIdRedirectPmml);
        assertEquals("LoanApprovalRegression", retrieved.getFileName());
        assertEquals("LoanApprovalRegression", retrieved.name());
        assertEquals("dmn", retrieved.model());
        assertEquals("dmn", ((LocalComponentIdRedirectPmml) retrieved).getRedirectModel());
    }
}