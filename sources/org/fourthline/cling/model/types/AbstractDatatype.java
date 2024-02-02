package org.fourthline.cling.model.types;

import java.lang.reflect.ParameterizedType;
import org.fourthline.cling.model.types.Datatype;
/* loaded from: classes.dex */
public abstract class AbstractDatatype<V> implements Datatype<V> {
    private Datatype.Builtin builtin;

    protected Class<V> getValueType() {
        return (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return getValueType().isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public V valueOf(String s) throws InvalidValueException {
        return null;
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public Datatype.Builtin getBuiltin() {
        return this.builtin;
    }

    public void setBuiltin(Datatype.Builtin builtin) {
        this.builtin = builtin;
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public String getString(V value) throws InvalidValueException {
        if (value == null) {
            return "";
        }
        if (!isValid(value)) {
            throw new InvalidValueException("Value is not valid: " + value);
        }
        return value.toString();
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public boolean isValid(V value) {
        return value == null || getValueType().isAssignableFrom(value.getClass());
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ")";
    }

    @Override // org.fourthline.cling.model.types.Datatype
    public String getDisplayString() {
        if (this instanceof CustomDatatype) {
            return ((CustomDatatype) this).getName();
        }
        if (getBuiltin() != null) {
            return getBuiltin().getDescriptorName();
        }
        return getValueType().getSimpleName();
    }
}
