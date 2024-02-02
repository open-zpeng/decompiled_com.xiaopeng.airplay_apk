package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public class TransportSettings {
    private PlayMode playMode;
    private RecordQualityMode recQualityMode;

    public TransportSettings() {
        this.playMode = PlayMode.NORMAL;
        this.recQualityMode = RecordQualityMode.NOT_IMPLEMENTED;
    }

    public TransportSettings(PlayMode playMode) {
        this.playMode = PlayMode.NORMAL;
        this.recQualityMode = RecordQualityMode.NOT_IMPLEMENTED;
        this.playMode = playMode;
    }

    public TransportSettings(PlayMode playMode, RecordQualityMode recQualityMode) {
        this.playMode = PlayMode.NORMAL;
        this.recQualityMode = RecordQualityMode.NOT_IMPLEMENTED;
        this.playMode = playMode;
        this.recQualityMode = recQualityMode;
    }

    public PlayMode getPlayMode() {
        return this.playMode;
    }

    public RecordQualityMode getRecQualityMode() {
        return this.recQualityMode;
    }
}
