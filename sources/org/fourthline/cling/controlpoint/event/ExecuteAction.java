package org.fourthline.cling.controlpoint.event;

import org.fourthline.cling.controlpoint.ActionCallback;
/* loaded from: classes.dex */
public class ExecuteAction {
    protected ActionCallback callback;

    public ExecuteAction(ActionCallback callback) {
        this.callback = callback;
    }

    public ActionCallback getCallback() {
        return this.callback;
    }
}
