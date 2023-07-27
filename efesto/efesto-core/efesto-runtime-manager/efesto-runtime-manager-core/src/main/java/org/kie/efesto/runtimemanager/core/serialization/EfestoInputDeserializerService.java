package org.kie.efesto.runtimemanager.core.serialization;

import com.fasterxml.jackson.databind.JsonDeserializer;
import org.kie.efesto.common.core.serialization.DeserializerService;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;

public class EfestoInputDeserializerService implements DeserializerService<EfestoInput> {

    @Override
    public Class<EfestoInput> type() {
        return EfestoInput.class;
    }

    @Override
    public JsonDeserializer<? extends EfestoInput> deser() {
        return new EfestoInputDeserializer();
    }
}
