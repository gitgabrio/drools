package org.kie.efesto.kafka.runtime.provider.model;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.listener.EfestoListener;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.common.api.model.EfestoRuntimeContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Currently empty
 *
 * @param <T>
 */
public class EfestoKafkaRuntimeContextImpl<T extends EfestoListener> implements EfestoRuntimeContext<T> {

    @Override
    public Set<T> getEfestoListeners() {
        return Collections.emptySet();
    }

    @Override
    public void addEfestoListener(T toAdd) {

    }

    @Override
    public void removeEfestoListener(T toRemove) {
    }

    @Override
    public Map<String, GeneratedResources> getGeneratedResourcesMap() {
        return Collections.emptyMap();
    }

    @Override
    public void addGeneratedResources(String model, GeneratedResources generatedResources) {

    }

    @Override
    public Map<String, byte[]> getGeneratedClasses(ModelLocalUriId modelLocalUriId) {
        return Collections.emptyMap();
    }

    @Override
    public void addGeneratedClasses(ModelLocalUriId modelLocalUriId, Map<String, byte[]> generatedClasses) {
    }

    @Override
    public boolean containsKey(ModelLocalUriId localUri) {
        return false;
    }

    @Override
    public Set<ModelLocalUriId> localUriIdKeySet() {
        return Collections.emptySet();
    }
}
