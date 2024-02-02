package org.fourthline.cling.support.model.dlna;
/* loaded from: classes.dex */
public enum DLNAOperations {
    NONE(0),
    RANGE(1),
    TIMESEEK(16);
    
    private int code;

    DLNAOperations(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static DLNAOperations valueOf(int code) {
        DLNAOperations[] values;
        for (DLNAOperations errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
