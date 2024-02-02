package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleBrowser extends AppleService {
    protected native int CreateBrowser(int i, int i2, String str, String str2);

    public AppleBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener client) throws DNSSDException {
        super(client);
        ThrowOnErr(CreateBrowser(flags, ifIndex, regType, domain));
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }
}
