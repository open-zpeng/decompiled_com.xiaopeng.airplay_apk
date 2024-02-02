package org.fourthline.cling.support.model.dlna.types;

import org.fourthline.cling.model.types.BytesRange;
/* loaded from: classes.dex */
public class TimeSeekRangeType {
    private BytesRange bytesRange;
    private NormalPlayTimeRange normalPlayTimeRange;

    public TimeSeekRangeType(NormalPlayTimeRange nptRange) {
        this.normalPlayTimeRange = nptRange;
    }

    public TimeSeekRangeType(NormalPlayTimeRange nptRange, BytesRange byteRange) {
        this.normalPlayTimeRange = nptRange;
        this.bytesRange = byteRange;
    }

    public NormalPlayTimeRange getNormalPlayTimeRange() {
        return this.normalPlayTimeRange;
    }

    public BytesRange getBytesRange() {
        return this.bytesRange;
    }

    public void setBytesRange(BytesRange bytesRange) {
        this.bytesRange = bytesRange;
    }
}
