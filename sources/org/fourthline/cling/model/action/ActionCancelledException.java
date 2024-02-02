package org.fourthline.cling.model.action;

import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public class ActionCancelledException extends ActionException {
    public ActionCancelledException(InterruptedException cause) {
        super(ErrorCode.ACTION_FAILED, "Action execution interrupted", cause);
    }
}
