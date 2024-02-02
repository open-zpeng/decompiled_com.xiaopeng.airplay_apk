package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleRegistration extends AppleService implements DNSSDRegistration {
    protected native int AddRecord(int i, int i2, byte[] bArr, int i3, AppleDNSRecord appleDNSRecord);

    protected native int BeginRegister(int i, int i2, String str, String str2, String str3, String str4, int i3, byte[] bArr);

    public AppleRegistration(int flags, int ifIndex, String serviceName, String regType, String domain, String host, int port, byte[] txtRecord, RegisterListener client) throws DNSSDException {
        super(client);
        ThrowOnErr(BeginRegister(ifIndex, flags, serviceName, regType, domain, host, port, txtRecord));
        if (!AppleDNSSD.hasAutoCallbacks) {
            new Thread(this).start();
        }
    }

    @Override // com.apple.dnssd.DNSSDRegistration
    public DNSRecord addRecord(int flags, int rrType, byte[] rData, int ttl) throws DNSSDException {
        AppleDNSRecord newRecord = new AppleDNSRecord(this);
        ThrowOnErr(AddRecord(flags, rrType, rData, ttl, newRecord));
        return newRecord;
    }

    @Override // com.apple.dnssd.DNSSDRegistration
    public DNSRecord getTXTRecord() throws DNSSDException {
        return new AppleDNSRecord(this);
    }
}
