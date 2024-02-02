package org.eclipse.jetty.server;

import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.fourthline.cling.model.message.header.ContentRangeHeader;
/* loaded from: classes.dex */
public class InclusiveByteRange {
    private static final Logger LOG = Log.getLogger(InclusiveByteRange.class);
    long first;
    long last;

    public InclusiveByteRange(long first, long last) {
        this.first = 0L;
        this.last = 0L;
        this.first = first;
        this.last = last;
    }

    public long getFirst() {
        return this.first;
    }

    public long getLast() {
        return this.last;
    }

    public static List satisfiableRanges(Enumeration headers, long size) {
        long first;
        long last;
        int d;
        Object satRanges = null;
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            StringTokenizer tok = new StringTokenizer(header, "=,", false);
            Object satRanges2 = satRanges;
            String t = null;
            while (true) {
                String t2 = t;
                try {
                    if (tok.hasMoreTokens()) {
                        try {
                            t2 = tok.nextToken().trim();
                            first = -1;
                            last = -1;
                            d = t2.indexOf(45);
                        } catch (NumberFormatException e) {
                            LOG.warn("Bad range format: {}", t2);
                            LOG.ignore(e);
                        }
                        if (d >= 0 && t2.indexOf("-", d + 1) < 0) {
                            if (d == 0) {
                                if (d + 1 < t2.length()) {
                                    last = Long.parseLong(t2.substring(d + 1).trim());
                                } else {
                                    LOG.warn("Bad range format: {}", t2);
                                    t = t2;
                                }
                            } else if (d + 1 < t2.length()) {
                                first = Long.parseLong(t2.substring(0, d).trim());
                                last = Long.parseLong(t2.substring(d + 1).trim());
                            } else {
                                first = Long.parseLong(t2.substring(0, d).trim());
                            }
                            if ((first != -1 || last != -1) && (first == -1 || last == -1 || first <= last)) {
                                if (first < size) {
                                    InclusiveByteRange range = new InclusiveByteRange(first, last);
                                    satRanges2 = LazyList.add(satRanges2, range);
                                }
                                t = t2;
                            }
                        }
                        if (HttpHeaderValues.BYTES.equals(t2)) {
                            t = t2;
                        } else {
                            LOG.warn("Bad range format: {}", t2);
                            break;
                        }
                    }
                } catch (Exception e2) {
                    LOG.warn("Bad range format: {}", t2);
                    LOG.ignore(e2);
                }
            }
            satRanges = satRanges2;
        }
        return LazyList.getList(satRanges, true);
    }

    public long getFirst(long size) {
        if (this.first < 0) {
            long tf = size - this.last;
            if (tf < 0) {
                return 0L;
            }
            return tf;
        }
        return this.first;
    }

    public long getLast(long size) {
        if (this.first < 0) {
            return size - 1;
        }
        if (this.last < 0 || this.last >= size) {
            return size - 1;
        }
        return this.last;
    }

    public long getSize(long size) {
        return (getLast(size) - getFirst(size)) + 1;
    }

    public String toHeaderRangeString(long size) {
        StringBuilder sb = new StringBuilder(40);
        sb.append(ContentRangeHeader.PREFIX);
        sb.append(getFirst(size));
        sb.append('-');
        sb.append(getLast(size));
        sb.append("/");
        sb.append(size);
        return sb.toString();
    }

    public static String to416HeaderRangeString(long size) {
        StringBuilder sb = new StringBuilder(40);
        sb.append("bytes */");
        sb.append(size);
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(60);
        sb.append(Long.toString(this.first));
        sb.append(":");
        sb.append(Long.toString(this.last));
        return sb.toString();
    }
}
