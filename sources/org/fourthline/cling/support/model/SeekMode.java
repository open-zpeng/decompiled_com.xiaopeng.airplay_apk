package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public enum SeekMode {
    TRACK_NR("TRACK_NR"),
    ABS_TIME("ABS_TIME"),
    REL_TIME("REL_TIME"),
    ABS_COUNT("ABS_COUNT"),
    REL_COUNT("REL_COUNT"),
    CHANNEL_FREQ("CHANNEL_FREQ"),
    TAPE_INDEX("TAPE-INDEX"),
    FRAME("FRAME");
    
    private String protocolString;

    SeekMode(String protocolString) {
        this.protocolString = protocolString;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.protocolString;
    }

    public static SeekMode valueOrExceptionOf(String s) throws IllegalArgumentException {
        SeekMode[] values;
        for (SeekMode seekMode : values()) {
            if (seekMode.protocolString.equals(s)) {
                return seekMode;
            }
        }
        throw new IllegalArgumentException("Invalid seek mode string: " + s);
    }
}
