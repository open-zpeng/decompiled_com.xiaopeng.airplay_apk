package com.apple.dnssd;
/* compiled from: DNSSD.java */
/* loaded from: classes.dex */
class AppleService implements DNSSDService, Runnable {
    protected BaseListener fListener;
    protected long fNativeContext = 0;

    protected native int BlockForData();

    protected native synchronized void HaltOperation();

    protected native int ProcessResults();

    public AppleService(BaseListener listener) {
        this.fListener = listener;
    }

    @Override // com.apple.dnssd.DNSSDService
    public void stop() {
        HaltOperation();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void ThrowOnErr(int rc) throws DNSSDException {
        if (rc != 0) {
            throw new AppleDNSSDException(rc);
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        while (true) {
            int result = BlockForData();
            synchronized (this) {
                if (this.fNativeContext != 0) {
                    if (result != 0) {
                        int result2 = ProcessResults();
                        if (this.fNativeContext != 0) {
                            if (result2 != 0) {
                                this.fListener.operationFailed(this, result2);
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }
}
