package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleQuery extends AppleService {
    protected native int CreateQuery(int i, int i2, String str, int i3, int i4);

    public AppleQuery(int flags, int ifIndex, String serviceName, int rrtype, int rrclass, QueryListener client) throws DNSSDException {
        super(client);
        ThrowOnErr(CreateQuery(flags, ifIndex, serviceName, rrtype, rrclass));
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }
}
