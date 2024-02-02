package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;
/* loaded from: classes.dex */
public abstract class SetMute extends ActionCallback {
    private static Logger log = Logger.getLogger(SetMute.class.getName());

    public SetMute(Service service, boolean desiredMute) {
        this(new UnsignedIntegerFourBytes(0L), service, desiredMute);
    }

    public SetMute(UnsignedIntegerFourBytes instanceId, Service service, boolean desiredMute) {
        super(new ActionInvocation(service.getAction("SetMute")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("DesiredMute", Boolean.valueOf(desiredMute));
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        log.fine("Executed successfully");
    }
}
