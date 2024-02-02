package org.fourthline.cling.support.model.dlna.types;

import org.fourthline.cling.model.types.BytesRange;
/* loaded from: classes.dex */
public class AvailableSeekRangeType {
    private BytesRange bytesRange;
    private Mode modeFlag;
    private NormalPlayTimeRange normalPlayTimeRange;

    /* loaded from: classes.dex */
    public enum Mode {
        MODE_0,
        MODE_1
    }

    public AvailableSeekRangeType(Mode modeFlag, NormalPlayTimeRange nptRange) {
        this.modeFlag = modeFlag;
        this.normalPlayTimeRange = nptRange;
    }

    public AvailableSeekRangeType(Mode modeFlag, BytesRange byteRange) {
        this.modeFlag = modeFlag;
        this.bytesRange = byteRange;
    }

    public AvailableSeekRangeType(Mode modeFlag, NormalPlayTimeRange nptRange, BytesRange byteRange) {
        this.modeFlag = modeFlag;
        this.normalPlayTimeRange = nptRange;
        this.bytesRange = byteRange;
    }

    public NormalPlayTimeRange getNormalPlayTimeRange() {
        return this.normalPlayTimeRange;
    }

    public BytesRange getBytesRange() {
        return this.bytesRange;
    }

    public Mode getModeFlag() {
        return this.modeFlag;
    }
}
