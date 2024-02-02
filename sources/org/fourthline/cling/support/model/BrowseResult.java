package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class BrowseResult {
    protected UnsignedIntegerFourBytes containerUpdateID;
    protected UnsignedIntegerFourBytes count;
    protected String result;
    protected UnsignedIntegerFourBytes totalMatches;

    public BrowseResult(String result, UnsignedIntegerFourBytes count, UnsignedIntegerFourBytes totalMatches, UnsignedIntegerFourBytes containerUpdateID) {
        this.result = result;
        this.count = count;
        this.totalMatches = totalMatches;
        this.containerUpdateID = containerUpdateID;
    }

    public BrowseResult(String result, long count, long totalMatches) {
        this(result, count, totalMatches, 0L);
    }

    public BrowseResult(String result, long count, long totalMatches, long updatedId) {
        this(result, new UnsignedIntegerFourBytes(count), new UnsignedIntegerFourBytes(totalMatches), new UnsignedIntegerFourBytes(updatedId));
    }

    public String getResult() {
        return this.result;
    }

    public UnsignedIntegerFourBytes getCount() {
        return this.count;
    }

    public long getCountLong() {
        return this.count.getValue().longValue();
    }

    public UnsignedIntegerFourBytes getTotalMatches() {
        return this.totalMatches;
    }

    public long getTotalMatchesLong() {
        return this.totalMatches.getValue().longValue();
    }

    public UnsignedIntegerFourBytes getContainerUpdateID() {
        return this.containerUpdateID;
    }

    public long getContainerUpdateIDLong() {
        return this.containerUpdateID.getValue().longValue();
    }
}
