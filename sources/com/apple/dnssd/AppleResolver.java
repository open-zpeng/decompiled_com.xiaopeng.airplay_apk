package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleResolver extends AppleService {
    protected native int CreateResolver(int i, int i2, String str, String str2, String str3);

    public AppleResolver(int flags, int ifIndex, String serviceName, String regType, String domain, ResolveListener client) throws DNSSDException {
        super(client);
        ThrowOnErr(CreateResolver(flags, ifIndex, serviceName, regType, domain));
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }
}
