package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleDNSRecord implements DNSRecord {
    protected AppleService fOwner;
    protected long fRecord = 0;

    protected native int Remove();

    protected native int Update(int i, byte[] bArr, int i2);

    public AppleDNSRecord(AppleService owner) {
        this.fOwner = owner;
    }

    @Override // com.apple.dnssd.DNSRecord
    public void update(int flags, byte[] rData, int ttl) throws DNSSDException {
        ThrowOnErr(Update(flags, rData, ttl));
    }

    @Override // com.apple.dnssd.DNSRecord
    public void remove() throws DNSSDException {
        ThrowOnErr(Remove());
    }

    protected void ThrowOnErr(int rc) throws DNSSDException {
        if (rc != 0) {
            throw new AppleDNSSDException(rc);
        }
    }
}
