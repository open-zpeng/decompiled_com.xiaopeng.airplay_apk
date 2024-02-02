package org.fourthline.cling.support.renderingcontrol;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public class RenderingControlException extends ActionException {
    public RenderingControlException(int errorCode, String message) {
        super(errorCode, message);
    }

    public RenderingControlException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RenderingControlException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RenderingControlException(ErrorCode errorCode) {
        super(errorCode);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public RenderingControlException(org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode r4, java.lang.String r5) {
        /*
            r3 = this;
            int r0 = r4.getCode()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = r4.getDescription()
            r1.append(r2)
            java.lang.String r2 = ". "
            r1.append(r2)
            r1.append(r5)
            java.lang.String r2 = "."
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r3.<init>(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.support.renderingcontrol.RenderingControlException.<init>(org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode, java.lang.String):void");
    }

    public RenderingControlException(RenderingControlErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDescription());
    }
}
