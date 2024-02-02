package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class InstanceID {
    protected UnsignedIntegerFourBytes id;
    protected List<EventedValue> values;

    public InstanceID(UnsignedIntegerFourBytes id) {
        this(id, new ArrayList());
    }

    public InstanceID(UnsignedIntegerFourBytes id, List<EventedValue> values) {
        this.values = new ArrayList();
        this.id = id;
        this.values = values;
    }

    public UnsignedIntegerFourBytes getId() {
        return this.id;
    }

    public List<EventedValue> getValues() {
        return this.values;
    }
}
