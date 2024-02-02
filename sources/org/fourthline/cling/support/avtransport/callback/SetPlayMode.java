package org.fourthline.cling.support.avtransport.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.PlayMode;
/* loaded from: classes.dex */
public abstract class SetPlayMode extends ActionCallback {
    private static Logger log = Logger.getLogger(SetPlayMode.class.getName());

    public SetPlayMode(Service service, PlayMode playMode) {
        this(new UnsignedIntegerFourBytes(0L), service, playMode);
    }

    public SetPlayMode(UnsignedIntegerFourBytes instanceId, Service service, PlayMode playMode) {
        super(new ActionInvocation(service.getAction("SetPlayMode")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("NewPlayMode", playMode.toString());
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}
