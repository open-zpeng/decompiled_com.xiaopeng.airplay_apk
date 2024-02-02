package com.apple.dnssd;
/* loaded from: classes.dex */
public abstract class DNSSD {
    public static final int ALL_INTERFACES = 0;
    public static final int BROWSE_DOMAINS = 64;
    public static final int DEFAULT = 4;
    public static final int LOCALHOST_ONLY = -1;
    public static final int MAX_DOMAIN_NAME = 1009;
    public static final int MORE_COMING = 1;
    public static final int NO_AUTO_RENAME = 8;
    public static final int REGISTRATION_DOMAINS = 128;
    public static final int SHARED = 16;
    public static final int UNIQUE = 32;
    protected static DNSSD fInstance;

    protected abstract String _constructFullName(String str, String str2, String str3) throws DNSSDException;

    protected abstract DNSSDRecordRegistrar _createRecordRegistrar(RegisterRecordListener registerRecordListener) throws DNSSDException;

    protected abstract DNSSDService _enumerateDomains(int i, int i2, DomainListener domainListener) throws DNSSDException;

    protected abstract int _getIfIndexForName(String str);

    protected abstract String _getNameForIfIndex(int i);

    protected abstract DNSSDService _makeBrowser(int i, int i2, String str, String str2, BrowseListener browseListener) throws DNSSDException;

    protected abstract DNSSDService _queryRecord(int i, int i2, String str, int i3, int i4, QueryListener queryListener) throws DNSSDException;

    protected abstract void _reconfirmRecord(int i, int i2, String str, int i3, int i4, byte[] bArr);

    protected abstract DNSSDRegistration _register(int i, int i2, String str, String str2, String str3, String str4, int i3, TXTRecord tXTRecord, RegisterListener registerListener) throws DNSSDException;

    protected abstract DNSSDService _resolve(int i, int i2, String str, String str2, String str3, ResolveListener resolveListener) throws DNSSDException;

    public static DNSSDService browse(int flags, int ifIndex, String regType, String domain, BrowseListener listener) throws DNSSDException {
        return getInstance()._makeBrowser(flags, ifIndex, regType, domain, listener);
    }

    public static DNSSDService browse(String regType, BrowseListener listener) throws DNSSDException {
        return browse(0, 0, regType, "", listener);
    }

    public static DNSSDService resolve(int flags, int ifIndex, String serviceName, String regType, String domain, ResolveListener listener) throws DNSSDException {
        return getInstance()._resolve(flags, ifIndex, serviceName, regType, domain, listener);
    }

    public static DNSSDRegistration register(int flags, int ifIndex, String serviceName, String regType, String domain, String host, int port, TXTRecord txtRecord, RegisterListener listener) throws DNSSDException {
        return getInstance()._register(flags, ifIndex, serviceName, regType, domain, host, port, txtRecord, listener);
    }

    public static DNSSDRegistration register(String serviceName, String regType, int port, RegisterListener listener) throws DNSSDException {
        return register(0, 0, serviceName, regType, null, null, port, null, listener);
    }

    public static DNSSDRecordRegistrar createRecordRegistrar(RegisterRecordListener listener) throws DNSSDException {
        return getInstance()._createRecordRegistrar(listener);
    }

    public static DNSSDService queryRecord(int flags, int ifIndex, String serviceName, int rrtype, int rrclass, QueryListener listener) throws DNSSDException {
        return getInstance()._queryRecord(flags, ifIndex, serviceName, rrtype, rrclass, listener);
    }

    public static DNSSDService enumerateDomains(int flags, int ifIndex, DomainListener listener) throws DNSSDException {
        return getInstance()._enumerateDomains(flags, ifIndex, listener);
    }

    public static String constructFullName(String serviceName, String regType, String domain) throws DNSSDException {
        return getInstance()._constructFullName(serviceName, regType, domain);
    }

    public static void reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata) {
        getInstance()._reconfirmRecord(flags, ifIndex, fullName, rrtype, rrclass, rdata);
    }

    public static String getNameForIfIndex(int ifIndex) {
        return getInstance()._getNameForIfIndex(ifIndex);
    }

    public static int getIfIndexForName(String ifName) {
        return getInstance()._getIfIndexForName(ifName);
    }

    protected static final DNSSD getInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getDNSSDInstance"));
        }
        return fInstance;
    }

    static {
        try {
            String name = System.getProperty("com.apple.dnssd.DNSSD");
            if (name == null) {
                name = "com.apple.dnssd.AppleDNSSD";
            }
            fInstance = (DNSSD) Class.forName(name).newInstance();
        } catch (Exception e) {
            throw new InternalError("cannot instantiate DNSSD" + e);
        }
    }
}
