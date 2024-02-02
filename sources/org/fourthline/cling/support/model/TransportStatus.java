package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public enum TransportStatus {
    OK,
    ERROR_OCCURRED,
    CUSTOM;
    
    String value = name();

    TransportStatus() {
    }

    public String getValue() {
        return this.value;
    }

    public TransportStatus setValue(String value) {
        this.value = value;
        return this;
    }

    public static TransportStatus valueOrCustomOf(String s) {
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            return CUSTOM.setValue(s);
        }
    }
}
