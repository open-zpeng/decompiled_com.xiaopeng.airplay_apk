package org.fourthline.cling.support.lastchange;

import java.lang.Enum;
import java.util.Map;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public abstract class EventedValueEnumArray<E extends Enum> extends EventedValue<E[]> {
    protected abstract E[] enumValueOf(String[] strArr);

    public EventedValueEnumArray(E[] e) {
        super(e);
    }

    public EventedValueEnumArray(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public E[] valueOf(String s) throws InvalidValueException {
        return enumValueOf(ModelUtil.fromCommaSeparatedList(s));
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public String toString() {
        return ModelUtil.toCommaSeparatedList(getValue());
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected Datatype getDatatype() {
        return null;
    }
}
