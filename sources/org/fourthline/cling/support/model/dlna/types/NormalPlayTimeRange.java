package org.fourthline.cling.support.model.dlna.types;

import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class NormalPlayTimeRange {
    public static final String PREFIX = "npt=";
    private NormalPlayTime timeDuration;
    private NormalPlayTime timeEnd;
    private NormalPlayTime timeStart;

    public NormalPlayTimeRange(long timeStart, long timeEnd) {
        this.timeStart = new NormalPlayTime(timeStart);
        this.timeEnd = new NormalPlayTime(timeEnd);
    }

    public NormalPlayTimeRange(NormalPlayTime timeStart, NormalPlayTime timeEnd) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public NormalPlayTimeRange(NormalPlayTime timeStart, NormalPlayTime timeEnd, NormalPlayTime timeDuration) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.timeDuration = timeDuration;
    }

    public NormalPlayTime getTimeStart() {
        return this.timeStart;
    }

    public NormalPlayTime getTimeEnd() {
        return this.timeEnd;
    }

    public NormalPlayTime getTimeDuration() {
        return this.timeDuration;
    }

    public String getString() {
        return getString(true);
    }

    public String getString(boolean includeDuration) {
        String s = PREFIX + this.timeStart.getString() + "-";
        if (this.timeEnd != null) {
            s = s + this.timeEnd.getString();
        }
        if (includeDuration) {
            StringBuilder sb = new StringBuilder();
            sb.append(s);
            sb.append("/");
            sb.append(this.timeDuration != null ? this.timeDuration.getString() : "*");
            return sb.toString();
        }
        return s;
    }

    public static NormalPlayTimeRange valueOf(String s) throws InvalidValueException {
        return valueOf(s, false);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x0041  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0050 A[ADDED_TO_REGION] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange valueOf(java.lang.String r7, boolean r8) throws org.fourthline.cling.model.types.InvalidValueException {
        /*
            java.lang.String r0 = "npt="
            boolean r0 = r7.startsWith(r0)
            if (r0 == 0) goto L63
            r0 = 0
            r1 = 0
            java.lang.String r2 = "npt="
            int r2 = r2.length()
            java.lang.String r2 = r7.substring(r2)
            java.lang.String r3 = "[-/]"
            java.lang.String[] r2 = r2.split(r3)
            int r3 = r2.length
            r4 = 1
            switch(r3) {
                case 1: goto L47;
                case 2: goto L39;
                case 3: goto L20;
                default: goto L1f;
            }
        L1f:
            goto L63
        L20:
            r3 = 2
            r5 = r2[r3]
            int r5 = r5.length()
            if (r5 == 0) goto L39
            r5 = r2[r3]
            java.lang.String r6 = "*"
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L39
            r3 = r2[r3]
            org.fourthline.cling.support.model.dlna.types.NormalPlayTime r1 = org.fourthline.cling.support.model.dlna.types.NormalPlayTime.valueOf(r3)
        L39:
            r3 = r2[r4]
            int r3 = r3.length()
            if (r3 == 0) goto L47
            r3 = r2[r4]
            org.fourthline.cling.support.model.dlna.types.NormalPlayTime r0 = org.fourthline.cling.support.model.dlna.types.NormalPlayTime.valueOf(r3)
        L47:
            r3 = 0
            r5 = r2[r3]
            int r5 = r5.length()
            if (r5 == 0) goto L63
            if (r8 == 0) goto L57
            if (r8 == 0) goto L63
            int r5 = r2.length
            if (r5 <= r4) goto L63
        L57:
            r3 = r2[r3]
            org.fourthline.cling.support.model.dlna.types.NormalPlayTime r3 = org.fourthline.cling.support.model.dlna.types.NormalPlayTime.valueOf(r3)
            org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange r4 = new org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange
            r4.<init>(r3, r0, r1)
            return r4
        L63:
            org.fourthline.cling.model.types.InvalidValueException r0 = new org.fourthline.cling.model.types.InvalidValueException
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Can't parse NormalPlayTimeRange: "
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange.valueOf(java.lang.String, boolean):org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange");
    }
}
