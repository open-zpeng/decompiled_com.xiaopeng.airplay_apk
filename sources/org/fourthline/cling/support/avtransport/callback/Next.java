package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public abstract class Next extends ActionCallback {
    private static Logger log = Logger.getLogger(Next.class.getName());

    protected Next(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    protected Next(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public Next(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public Next(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("Next")));
        getActionInvocation().setInput("InstanceID", instanceId);
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}
