package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleRecordRegistrar extends AppleService implements DNSSDRecordRegistrar {
    protected native int CreateConnection();

    protected native int RegisterRecord(int i, int i2, String str, int i3, int i4, byte[] bArr, int i5, AppleDNSRecord appleDNSRecord);

    public AppleRecordRegistrar(RegisterRecordListener listener) throws DNSSDException {
        super(listener);
        ThrowOnErr(CreateConnection());
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }

    @Override // com.apple.dnssd.DNSSDRecordRegistrar
    public DNSRecord registerRecord(int flags, int ifIndex, String fullname, int rrtype, int rrclass, byte[] rdata, int ttl) throws DNSSDException {
        AppleDNSRecord newRecord = new AppleDNSRecord(this);
        ThrowOnErr(RegisterRecord(flags, ifIndex, fullname, rrtype, rrclass, rdata, ttl, newRecord));
        return newRecord;
    }
}
