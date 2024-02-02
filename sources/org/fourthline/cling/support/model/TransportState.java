package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public enum TransportState {
    STOPPED,
    PLAYING,
    TRANSITIONING,
    PAUSED_PLAYBACK,
    PAUSED_RECORDING,
    RECORDING,
    NO_MEDIA_PRESENT,
    CUSTOM;
    
    String value = name();

    TransportState() {
    }

    public String getValue() {
        return this.value;
    }

    public TransportState setValue(String value) {
        this.value = value;
        return this;
    }

    public static TransportState valueOrCustomOf(String s) {
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            return CUSTOM.setValue(s);
        }
    }
}
