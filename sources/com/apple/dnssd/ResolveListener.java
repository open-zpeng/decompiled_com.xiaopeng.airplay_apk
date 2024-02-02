package com.apple.dnssd;
/* loaded from: classes.dex */
public interface ResolveListener extends BaseListener {
    void serviceResolved(DNSSDService dNSSDService, int i, int i2, String str, String str2, int i3, TXTRecord tXTRecord);
}
