package com.apple.dnssd;
/* loaded from: classes.dex */
public interface DNSRecord {
    void remove() throws DNSSDException;

    void update(int i, byte[] bArr, int i2) throws DNSSDException;
}
