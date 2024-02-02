package org.fourthline.cling.support.model.dlna;
/* loaded from: classes.dex */
public enum DLNAConversionIndicator {
    NONE(0),
    TRANSCODED(1);
    
    private int code;

    DLNAConversionIndicator(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static DLNAConversionIndicator valueOf(int code) {
        DLNAConversionIndicator[] values;
        for (DLNAConversionIndicator errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
