package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public abstract class Stop extends ActionCallback {
    private static Logger log = Logger.getLogger(Stop.class.getName());

    public Stop(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public Stop(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("Stop")));
        getActionInvocation().setInput("InstanceID", instanceId);
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}
