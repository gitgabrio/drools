package org.kie.efesto.common.api.custom;

import java.io.Serializable;
import java.util.Map;

public interface SerializableMap<K extends Serializable, V extends Serializable> extends Map<K, V>, Serializable {
}
