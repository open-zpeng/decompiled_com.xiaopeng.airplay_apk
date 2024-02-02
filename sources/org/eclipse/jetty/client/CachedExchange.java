package org.eclipse.jetty.client;

import java.io.IOException;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Buffer;
/* loaded from: classes.dex */
public class CachedExchange extends HttpExchange {
    private final HttpFields _responseFields;
    private volatile int _responseStatus;

    public CachedExchange(boolean cacheHeaders) {
        this._responseFields = cacheHeaders ? new HttpFields() : null;
    }

    public synchronized int getResponseStatus() {
        if (getStatus() < 5) {
            throw new IllegalStateException("Response not received yet");
        }
        return this._responseStatus;
    }

    public synchronized HttpFields getResponseFields() {
        if (getStatus() < 6) {
            throw new IllegalStateException("Headers not completely received yet");
        }
        return this._responseFields;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.HttpExchange
    public synchronized void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        this._responseStatus = status;
        super.onResponseStatus(version, status, reason);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.HttpExchange
    public synchronized void onResponseHeader(Buffer name, Buffer value) throws IOException {
        if (this._responseFields != null) {
            this._responseFields.add(name, value.asImmutableBuffer());
        }
        super.onResponseHeader(name, value);
    }
}
