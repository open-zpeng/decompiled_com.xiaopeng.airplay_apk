package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public enum BrowseFlag {
    METADATA("BrowseMetadata"),
    DIRECT_CHILDREN("BrowseDirectChildren");
    
    private String protocolString;

    BrowseFlag(String protocolString) {
        this.protocolString = protocolString;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.protocolString;
    }

    public static BrowseFlag valueOrNullOf(String s) {
        BrowseFlag[] values;
        for (BrowseFlag browseFlag : values()) {
            if (browseFlag.toString().equals(s)) {
                return browseFlag;
            }
        }
        return null;
    }
}
