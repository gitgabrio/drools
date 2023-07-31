package org.kie.efesto.common.api.custom;

import java.io.Serializable;
import java.util.HashMap;

public class SerializableHashMap<K extends Serializable, V extends Serializable> extends HashMap<K, V> implements SerializableMap<K, V> {

    public static final SerializableMap EMPTY_MAP = new SerializableHashMap();
}
