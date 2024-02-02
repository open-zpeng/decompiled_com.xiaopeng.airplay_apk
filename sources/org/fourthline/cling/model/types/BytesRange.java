package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class BytesRange {
    public static final String PREFIX = "bytes=";
    private Long byteLength;
    private Long firstByte;
    private Long lastByte;

    public BytesRange(Long firstByte, Long lastByte) {
        this.firstByte = firstByte;
        this.lastByte = lastByte;
        this.byteLength = null;
    }

    public BytesRange(Long firstByte, Long lastByte, Long byteLength) {
        this.firstByte = firstByte;
        this.lastByte = lastByte;
        this.byteLength = byteLength;
    }

    public Long getFirstByte() {
        return this.firstByte;
    }

    public Long getLastByte() {
        return this.lastByte;
    }

    public Long getByteLength() {
        return this.byteLength;
    }

    public String getString() {
        return getString(false, null);
    }

    public String getString(boolean includeDuration) {
        return getString(includeDuration, null);
    }

    public String getString(boolean includeDuration, String rangePrefix) {
        String s = rangePrefix != null ? rangePrefix : PREFIX;
        if (this.firstByte != null) {
            s = s + this.firstByte.toString();
        }
        String s2 = s + "-";
        if (this.lastByte != null) {
            s2 = s2 + this.lastByte.toString();
        }
        if (includeDuration) {
            StringBuilder sb = new StringBuilder();
            sb.append(s2);
            sb.append("/");
            sb.append(this.byteLength != null ? this.byteLength.toString() : "*");
            return sb.toString();
        }
        return s2;
    }

    public static BytesRange valueOf(String s) throws InvalidValueException {
        return valueOf(s, null);
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x004e  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0061  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x006d A[ADDED_TO_REGION] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static org.fourthline.cling.model.types.BytesRange valueOf(java.lang.String r7, java.lang.String r8) throws org.fourthline.cling.model.types.InvalidValueException {
        /*
            if (r8 == 0) goto L4
            r0 = r8
            goto L6
        L4:
            java.lang.String r0 = "bytes="
        L6:
            boolean r0 = r7.startsWith(r0)
            if (r0 == 0) goto L75
            r0 = 0
            r1 = 0
            r2 = 0
            if (r8 == 0) goto L13
            r3 = r8
            goto L15
        L13:
            java.lang.String r3 = "bytes="
        L15:
            int r3 = r3.length()
            java.lang.String r3 = r7.substring(r3)
            java.lang.String r4 = "[-/]"
            java.lang.String[] r3 = r3.split(r4)
            int r4 = r3.length
            switch(r4) {
                case 1: goto L58;
                case 2: goto L45;
                case 3: goto L28;
                default: goto L27;
            }
        L27:
            goto L75
        L28:
            r4 = 2
            r5 = r3[r4]
            int r5 = r5.length()
            if (r5 == 0) goto L45
            r5 = r3[r4]
            java.lang.String r6 = "*"
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L45
            r4 = r3[r4]
            long r4 = java.lang.Long.parseLong(r4)
            java.lang.Long r2 = java.lang.Long.valueOf(r4)
        L45:
            r4 = 1
            r5 = r3[r4]
            int r5 = r5.length()
            if (r5 == 0) goto L58
            r4 = r3[r4]
            long r4 = java.lang.Long.parseLong(r4)
            java.lang.Long r1 = java.lang.Long.valueOf(r4)
        L58:
            r4 = 0
            r5 = r3[r4]
            int r5 = r5.length()
            if (r5 == 0) goto L6b
            r4 = r3[r4]
            long r4 = java.lang.Long.parseLong(r4)
            java.lang.Long r0 = java.lang.Long.valueOf(r4)
        L6b:
            if (r0 != 0) goto L6f
            if (r1 == 0) goto L75
        L6f:
            org.fourthline.cling.model.types.BytesRange r4 = new org.fourthline.cling.model.types.BytesRange
            r4.<init>(r0, r1, r2)
            return r4
        L75:
            org.fourthline.cling.model.types.InvalidValueException r0 = new org.fourthline.cling.model.types.InvalidValueException
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Can't parse Bytes Range: "
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.model.types.BytesRange.valueOf(java.lang.String, java.lang.String):org.fourthline.cling.model.types.BytesRange");
    }
}
