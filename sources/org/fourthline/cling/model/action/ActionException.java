package org.fourthline.cling.model.action;

import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public class ActionException extends Exception {
    private int errorCode;

    public ActionException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ActionException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ActionException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getDescription());
    }

    public ActionException(ErrorCode errorCode, String message) {
        this(errorCode, message, true);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public ActionException(org.fourthline.cling.model.types.ErrorCode r4, java.lang.String r5, boolean r6) {
        /*
            r3 = this;
            int r0 = r4.getCode()
            if (r6 == 0) goto L24
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
            goto L25
        L24:
            r1 = r5
        L25:
            r3.<init>(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.model.action.ActionException.<init>(org.fourthline.cling.model.types.ErrorCode, java.lang.String, boolean):void");
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public ActionException(org.fourthline.cling.model.types.ErrorCode r4, java.lang.String r5, java.lang.Throwable r6) {
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
            r3.<init>(r0, r1, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.model.action.ActionException.<init>(org.fourthline.cling.model.types.ErrorCode, java.lang.String, java.lang.Throwable):void");
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
