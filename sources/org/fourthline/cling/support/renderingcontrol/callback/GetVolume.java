package org.fourthline.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;
/* loaded from: classes.dex */
public abstract class GetVolume extends ActionCallback {
    private static Logger log = Logger.getLogger(GetVolume.class.getName());

    public abstract void received(ActionInvocation actionInvocation, int i);

    public GetVolume(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public GetVolume(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetVolume")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        boolean ok = true;
        int currentVolume = 0;
        try {
            currentVolume = Integer.valueOf(invocation.getOutput("CurrentVolume").getValue().toString()).intValue();
        } catch (Exception ex) {
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            invocation.setFailure(new ActionException(errorCode, "Can't parse ProtocolInfo response: " + ex, ex));
            failure(invocation, null);
            ok = false;
        }
        if (ok) {
            received(invocation, currentVolume);
        }
    }
}
