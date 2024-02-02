package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public enum RecordMediumWriteStatus {
    WRITABLE,
    PROTECTED,
    NOT_WRITABLE,
    UNKNOWN,
    NOT_IMPLEMENTED;

    public static RecordMediumWriteStatus valueOrUnknownOf(String s) {
        if (s == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
