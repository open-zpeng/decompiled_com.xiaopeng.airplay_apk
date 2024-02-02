package org.fourthline.cling.model.action;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.profile.RemoteClientInfo;
/* loaded from: classes.dex */
public class RemoteActionInvocation extends ActionInvocation {
    protected final RemoteClientInfo remoteClientInfo;

    public RemoteActionInvocation(Action action, ActionArgumentValue[] input, ActionArgumentValue[] output, RemoteClientInfo remoteClientInfo) {
        super(action, input, output, null);
        this.remoteClientInfo = remoteClientInfo;
    }

    public RemoteActionInvocation(Action action, RemoteClientInfo remoteClientInfo) {
        super(action);
        this.remoteClientInfo = remoteClientInfo;
    }

    public RemoteActionInvocation(ActionException failure, RemoteClientInfo remoteClientInfo) {
        super(failure);
        this.remoteClientInfo = remoteClientInfo;
    }

    public RemoteClientInfo getRemoteClientInfo() {
        return this.remoteClientInfo;
    }
}
