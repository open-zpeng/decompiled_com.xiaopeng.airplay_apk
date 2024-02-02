package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class SearchResult {
    protected UnsignedIntegerFourBytes containerUpdateID;
    protected UnsignedIntegerFourBytes count;
    protected String result;
    protected UnsignedIntegerFourBytes totalMatches;

    public SearchResult(String result, UnsignedIntegerFourBytes count, UnsignedIntegerFourBytes totalMatches, UnsignedIntegerFourBytes containerUpdateID) {
        this.result = result;
        this.count = count;
        this.totalMatches = totalMatches;
        this.containerUpdateID = containerUpdateID;
    }

    public SearchResult(String result, long count, long totalMatches) {
        this(result, count, totalMatches, 0L);
    }

    public SearchResult(String result, long count, long totalMatches, long updateID) {
        this(result, new UnsignedIntegerFourBytes(count), new UnsignedIntegerFourBytes(totalMatches), new UnsignedIntegerFourBytes(updateID));
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
