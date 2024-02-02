package org.fourthline.cling.support.contentdirectory;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public class ContentDirectoryException extends ActionException {
    public ContentDirectoryException(int errorCode, String message) {
        super(errorCode, message);
    }

    public ContentDirectoryException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ContentDirectoryException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ContentDirectoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public ContentDirectoryException(org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode r4, java.lang.String r5) {
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
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.support.contentdirectory.ContentDirectoryException.<init>(org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode, java.lang.String):void");
    }

    public ContentDirectoryException(ContentDirectoryErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDescription());
    }
}
