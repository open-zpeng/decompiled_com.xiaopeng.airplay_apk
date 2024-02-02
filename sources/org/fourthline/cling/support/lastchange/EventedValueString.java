package org.fourthline.cling.support.lastchange;

import java.util.Map;
import org.fourthline.cling.model.types.Datatype;
/* loaded from: classes.dex */
public class EventedValueString extends EventedValue<String> {
    public EventedValueString(String value) {
        super(value);
    }

    public EventedValueString(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected Datatype getDatatype() {
        return Datatype.Builtin.STRING.getDatatype();
    }
}
