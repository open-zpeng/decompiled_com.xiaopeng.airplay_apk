package com.apple.dnssd;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
public class AppleDNSSD extends DNSSD {
    public static boolean hasAutoCallbacks;

    protected static native int InitLibrary(int i);

    protected native int ConstructName(String str, String str2, String str3, String[] strArr);

    protected native int GetIfIndexForName(String str);

    protected native String GetNameForIfIndex(int i);

    protected native void ReconfirmRecord(int i, int i2, String str, int i3, int i4, byte[] bArr);

    AppleDNSSD() {
    }

    static {
        System.loadLibrary("jdns_sd");
        int libInitResult = InitLibrary(2);
        if (libInitResult != 0) {
            throw new InternalError("cannot instantiate DNSSD: " + new AppleDNSSDException(libInitResult).getMessage());
        }
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDService _makeBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener client) throws DNSSDException {
        return new AppleBrowser(flags, ifIndex, regType, domain, client);
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDService _resolve(int flags, int ifIndex, String serviceName, String regType, String domain, ResolveListener client) throws DNSSDException {
        return new AppleResolver(flags, ifIndex, serviceName, regType, domain, client);
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDRegistration _register(int flags, int ifIndex, String serviceName, String regType, String domain, String host, int port, TXTRecord txtRecord, RegisterListener client) throws DNSSDException {
        return new AppleRegistration(flags, ifIndex, serviceName, regType, domain, host, port, txtRecord != null ? txtRecord.getRawBytes() : null, client);
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDRecordRegistrar _createRecordRegistrar(RegisterRecordListener listener) throws DNSSDException {
        return new AppleRecordRegistrar(listener);
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDService _queryRecord(int flags, int ifIndex, String serviceName, int rrtype, int rrclass, QueryListener client) throws DNSSDException {
        return new AppleQuery(flags, ifIndex, serviceName, rrtype, rrclass, client);
    }

    @Override // com.apple.dnssd.DNSSD
    protected DNSSDService _enumerateDomains(int flags, int ifIndex, DomainListener listener) throws DNSSDException {
        return new AppleDomainEnum(flags, ifIndex, listener);
    }

    @Override // com.apple.dnssd.DNSSD
    protected String _constructFullName(String serviceName, String regType, String domain) throws DNSSDException {
        String[] responseHolder = new String[1];
        int rc = ConstructName(serviceName, regType, domain, responseHolder);
        if (rc != 0) {
            throw new AppleDNSSDException(rc);
        }
        return responseHolder[0];
    }

    @Override // com.apple.dnssd.DNSSD
    protected void _reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata) {
        ReconfirmRecord(flags, ifIndex, fullName, rrtype, rrclass, rdata);
    }

    @Override // com.apple.dnssd.DNSSD
    protected String _getNameForIfIndex(int ifIndex) {
        return GetNameForIfIndex(ifIndex);
    }

    @Override // com.apple.dnssd.DNSSD
    protected int _getIfIndexForName(String ifName) {
        return GetIfIndexForName(ifName);
    }
}
