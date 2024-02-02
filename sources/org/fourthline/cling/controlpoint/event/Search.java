package org.fourthline.cling.controlpoint.event;

import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
/* loaded from: classes.dex */
public class Search {
    protected int mxSeconds;
    protected UpnpHeader searchType;

    public Search() {
        this.searchType = new STAllHeader();
        this.mxSeconds = MXHeader.DEFAULT_VALUE.intValue();
    }

    public Search(UpnpHeader searchType) {
        this.searchType = new STAllHeader();
        this.mxSeconds = MXHeader.DEFAULT_VALUE.intValue();
        this.searchType = searchType;
    }

    public Search(UpnpHeader searchType, int mxSeconds) {
        this.searchType = new STAllHeader();
        this.mxSeconds = MXHeader.DEFAULT_VALUE.intValue();
        this.searchType = searchType;
        this.mxSeconds = mxSeconds;
    }

    public Search(int mxSeconds) {
        this.searchType = new STAllHeader();
        this.mxSeconds = MXHeader.DEFAULT_VALUE.intValue();
        this.mxSeconds = mxSeconds;
    }

    public UpnpHeader getSearchType() {
        return this.searchType;
    }

    public int getMxSeconds() {
        return this.mxSeconds;
    }
}
