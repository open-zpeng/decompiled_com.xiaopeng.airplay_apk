package org.fourthline.cling.support.lastchange;

import java.util.Map;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
/* loaded from: classes.dex */
public class EventedValueUnsignedIntegerTwoBytes extends EventedValue<UnsignedIntegerTwoBytes> {
    public EventedValueUnsignedIntegerTwoBytes(UnsignedIntegerTwoBytes value) {
        super(value);
    }

    public EventedValueUnsignedIntegerTwoBytes(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected Datatype getDatatype() {
        return Datatype.Builtin.UI2.getDatatype();
    }
}
