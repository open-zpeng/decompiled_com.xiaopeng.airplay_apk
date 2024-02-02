package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class Event {
    protected List<InstanceID> instanceIDs;

    public Event() {
        this.instanceIDs = new ArrayList();
    }

    public Event(List<InstanceID> instanceIDs) {
        this.instanceIDs = new ArrayList();
        this.instanceIDs = instanceIDs;
    }

    public List<InstanceID> getInstanceIDs() {
        return this.instanceIDs;
    }

    public InstanceID getInstanceID(UnsignedIntegerFourBytes id) {
        for (InstanceID instanceID : this.instanceIDs) {
            if (instanceID.getId().equals(id)) {
                return instanceID;
            }
        }
        return null;
    }

    public void clear() {
        this.instanceIDs = new ArrayList();
    }

    public void setEventedValue(UnsignedIntegerFourBytes id, EventedValue ev) {
        InstanceID instanceID = null;
        for (InstanceID i : getInstanceIDs()) {
            if (i.getId().equals(id)) {
                instanceID = i;
            }
        }
        if (instanceID == null) {
            instanceID = new InstanceID(id);
            getInstanceIDs().add(instanceID);
        }
        Iterator<EventedValue> it = instanceID.getValues().iterator();
        while (it.hasNext()) {
            EventedValue existingEv = it.next();
            if (existingEv.getClass().equals(ev.getClass())) {
                it.remove();
            }
        }
        instanceID.getValues().add(ev);
    }

    public <EV extends EventedValue> EV getEventedValue(UnsignedIntegerFourBytes id, Class<EV> type) {
        for (InstanceID instanceID : getInstanceIDs()) {
            if (instanceID.getId().equals(id)) {
                Iterator<EventedValue> it = instanceID.getValues().iterator();
                while (it.hasNext()) {
                    EV ev = (EV) it.next();
                    if (ev.getClass().equals(type)) {
                        return ev;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public boolean hasChanges() {
        for (InstanceID instanceID : this.instanceIDs) {
            if (instanceID.getValues().size() > 0) {
                return true;
            }
        }
        return false;
    }
}
