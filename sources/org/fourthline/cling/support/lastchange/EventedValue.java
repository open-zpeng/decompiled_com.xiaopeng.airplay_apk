package org.fourthline.cling.support.lastchange;

import java.util.Map;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.shared.AbstractMap;
/* loaded from: classes.dex */
public abstract class EventedValue<V> {
    protected final V value;

    protected abstract Datatype getDatatype();

    public EventedValue(V value) {
        this.value = value;
    }

    public EventedValue(Map.Entry<String, String>[] attributes) {
        try {
            this.value = valueOf(attributes);
        } catch (InvalidValueException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public V getValue() {
        return this.value;
    }

    public Map.Entry<String, String>[] getAttributes() {
        return new Map.Entry[]{new AbstractMap.SimpleEntry("val", toString())};
    }

    protected V valueOf(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        V v = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("val")) {
                v = valueOf(attribute.getValue());
            }
        }
        return v;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public V valueOf(String s) throws InvalidValueException {
        return (V) getDatatype().valueOf(s);
    }

    public String toString() {
        return getDatatype().getString(getValue());
    }
}
