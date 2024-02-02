package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.Collection;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChangeDelegator;
/* loaded from: classes.dex */
public class LastChangeAwareServiceManager<T extends LastChangeDelegator> extends DefaultServiceManager<T> {
    protected final LastChangeParser lastChangeParser;

    public LastChangeAwareServiceManager(LocalService<T> localService, LastChangeParser lastChangeParser) {
        this(localService, null, lastChangeParser);
    }

    public LastChangeAwareServiceManager(LocalService<T> localService, Class<T> serviceClass, LastChangeParser lastChangeParser) {
        super(localService, serviceClass);
        this.lastChangeParser = lastChangeParser;
    }

    protected LastChangeParser getLastChangeParser() {
        return this.lastChangeParser;
    }

    public void fireLastChange() {
        lock();
        try {
            ((LastChangeDelegator) getImplementation()).getLastChange().fire(getPropertyChangeSupport());
        } finally {
            unlock();
        }
    }

    @Override // org.fourthline.cling.model.DefaultServiceManager
    protected Collection<StateVariableValue> readInitialEventedStateVariableValues() throws Exception {
        LastChange lc = new LastChange(getLastChangeParser());
        UnsignedIntegerFourBytes[] ids = ((LastChangeDelegator) getImplementation()).getCurrentInstanceIds();
        if (ids.length > 0) {
            for (UnsignedIntegerFourBytes instanceId : ids) {
                ((LastChangeDelegator) getImplementation()).appendCurrentState(lc, instanceId);
            }
        } else {
            ((LastChangeDelegator) getImplementation()).appendCurrentState(lc, new UnsignedIntegerFourBytes(0L));
        }
        StateVariable variable = getService().getStateVariable("LastChange");
        Collection<StateVariableValue> values = new ArrayList<>();
        values.add(new StateVariableValue(variable, lc.toString()));
        return values;
    }
}
