package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleDomainEnum extends AppleService {
    protected native int BeginEnum(int i, int i2);

    public AppleDomainEnum(int flags, int ifIndex, DomainListener client) throws DNSSDException {
        super(client);
        ThrowOnErr(BeginEnum(flags, ifIndex));
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }
}
