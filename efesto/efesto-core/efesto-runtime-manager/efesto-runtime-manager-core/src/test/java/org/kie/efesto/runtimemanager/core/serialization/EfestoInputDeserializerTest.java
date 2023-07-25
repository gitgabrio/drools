package org.kie.efesto.runtimemanager.core.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.core.utils.JSONUtils.getObjectMapper;

class EfestoInputDeserializerTest {

    @Test
    void deserializeTest() throws JsonProcessingException {
        String toDeserialize = "{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"inputData\":\"MockEfestoInputA\",\"firstLevelCacheKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"},\"secondLevelCacheKey\":{\"modelLocalUriId\":{\"model\":\"MockEfestoInputA\",\"basePath\":\"/org.kie.efesto.runtimemanager.core.mocks\",\"fullPath\":\"/MockEfestoInputA/org.kie.efesto.runtimemanager.core.mocks\"},\"efestoClassKey\":{\"rawType\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA\",\"actualTypeArguments\":[\"java.lang.String\"],\"ownerType\":null,\"typeName\":\"org.kie.efesto.runtimemanager.core.mocks.MockEfestoInputA<java.lang.String>\"}}}";
        ObjectMapper mapper = getObjectMapper();
        SimpleModule toRegister = new SimpleModule();
        toRegister.addDeserializer(EfestoInput.class, new EfestoInputDeserializer());
        mapper.registerModule(toRegister);
        EfestoInput retrieved = mapper.readValue(toDeserialize, EfestoInput.class);
        assertThat(retrieved).isNotNull().isExactlyInstanceOf(MockEfestoInputA.class);


    }
}