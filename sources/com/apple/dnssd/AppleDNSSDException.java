package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleDNSSDException extends DNSSDException {
    protected int fErrorCode;

    public AppleDNSSDException(int errorCode) {
        this.fErrorCode = errorCode;
    }

    @Override // com.apple.dnssd.DNSSDException
    public int getErrorCode() {
        return this.fErrorCode;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        String[] kMessages = {"UNKNOWN", "NO_SUCH_NAME", "NO_MEMORY", "BAD_PARAM", "BAD_REFERENCE", "BAD_STATE", "BAD_FLAGS", "UNSUPPORTED", "NOT_INITIALIZED", "NO_CACHE", "ALREADY_REGISTERED", "NAME_CONFLICT", "INVALID", "FIREWALL", "INCOMPATIBLE", "BAD_INTERFACE_INDEX", "REFUSED", "NOSUCHRECORD", "NOAUTH", "NOSUCHKEY", "NATTRAVERSAL", "DOUBLENAT", "BADTIME", "BADSIG", "BADKEY", "TRANSIENT", "SERVICENOTRUNNING", "NATPORTMAPPINGUNSUPPORTED", "NATPORTMAPPINGDISABLED"};
        if (this.fErrorCode <= -65537 && this.fErrorCode > DNSSDException.UNKNOWN - kMessages.length) {
            return "DNS-SD Error " + String.valueOf(this.fErrorCode) + ": " + kMessages[DNSSDException.UNKNOWN - this.fErrorCode];
        }
        return super.getMessage() + "(" + String.valueOf(this.fErrorCode) + ")";
    }
}
