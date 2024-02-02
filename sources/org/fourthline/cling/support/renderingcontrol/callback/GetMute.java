package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;
/* loaded from: classes.dex */
public abstract class GetMute extends ActionCallback {
    private static Logger log = Logger.getLogger(GetMute.class.getName());

    public abstract void received(ActionInvocation actionInvocation, boolean z);

    public GetMute(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public GetMute(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetMute")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        boolean currentMute = ((Boolean) invocation.getOutput("CurrentMute").getValue()).booleanValue();
        received(invocation, currentMute);
    }
}
