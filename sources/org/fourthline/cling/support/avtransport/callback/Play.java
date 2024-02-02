package org.fourthline.cling.support.avtransport.callback;

import com.xpeng.airplay.service.NsdConstants;
import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public abstract class Play extends ActionCallback {
    private static Logger log = Logger.getLogger(Play.class.getName());

    public Play(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service, NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
    }

    public Play(Service service, String speed) {
        this(new UnsignedIntegerFourBytes(0L), service, speed);
    }

    public Play(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
    }

    public Play(UnsignedIntegerFourBytes instanceId, Service service, String speed) {
        super(new ActionInvocation(service.getAction("Play")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Speed", speed);
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}
