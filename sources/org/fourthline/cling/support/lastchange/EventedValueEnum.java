package org.fourthline.cling.support.lastchange;

import java.lang.Enum;
import java.util.Map;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public abstract class EventedValueEnum<E extends Enum> extends EventedValue<E> {
    protected abstract E enumValueOf(String str);

    public EventedValueEnum(E e) {
        super(e);
    }

    public EventedValueEnum(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public E valueOf(String s) throws InvalidValueException {
        return enumValueOf(s);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public String toString() {
        return ((Enum) getValue()).name();
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected Datatype getDatatype() {
        return null;
    }
}
