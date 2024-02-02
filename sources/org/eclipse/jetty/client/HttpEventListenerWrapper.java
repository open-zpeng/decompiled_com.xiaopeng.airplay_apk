package org.eclipse.jetty.client;

import java.io.IOException;
import org.eclipse.jetty.io.Buffer;
/* loaded from: classes.dex */
public class HttpEventListenerWrapper implements HttpEventListener {
    boolean _delegatingRequests;
    boolean _delegatingResponses;
    boolean _delegationResult;
    HttpEventListener _listener;
    private Buffer _reason;
    private int _status;
    private Buffer _version;

    public HttpEventListenerWrapper() {
        this._delegationResult = true;
        this._listener = null;
        this._delegatingRequests = false;
        this._delegatingResponses = false;
    }

    public HttpEventListenerWrapper(HttpEventListener eventListener, boolean delegating) {
        this._delegationResult = true;
        this._listener = eventListener;
        this._delegatingRequests = delegating;
        this._delegatingResponses = delegating;
    }

    public HttpEventListener getEventListener() {
        return this._listener;
    }

    public void setEventListener(HttpEventListener listener) {
        this._listener = listener;
    }

    public boolean isDelegatingRequests() {
        return this._delegatingRequests;
    }

    public boolean isDelegatingResponses() {
        return this._delegatingResponses;
    }

    public void setDelegatingRequests(boolean delegating) {
        this._delegatingRequests = delegating;
    }

    public void setDelegatingResponses(boolean delegating) {
        this._delegatingResponses = delegating;
    }

    public void setDelegationResult(boolean result) {
        this._delegationResult = result;
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onConnectionFailed(Throwable ex) {
        if (this._delegatingRequests) {
            this._listener.onConnectionFailed(ex);
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onException(Throwable ex) {
        if (this._delegatingRequests || this._delegatingResponses) {
            this._listener.onException(ex);
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onExpire() {
        if (this._delegatingRequests || this._delegatingResponses) {
            this._listener.onExpire();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onRequestCommitted() throws IOException {
        if (this._delegatingRequests) {
            this._listener.onRequestCommitted();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onRequestComplete() throws IOException {
        if (this._delegatingRequests) {
            this._listener.onRequestComplete();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onResponseComplete() throws IOException {
        if (this._delegatingResponses) {
            if (!this._delegationResult) {
                this._listener.onResponseStatus(this._version, this._status, this._reason);
            }
            this._listener.onResponseComplete();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onResponseContent(Buffer content) throws IOException {
        if (this._delegatingResponses) {
            this._listener.onResponseContent(content);
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onResponseHeader(Buffer name, Buffer value) throws IOException {
        if (this._delegatingResponses) {
            this._listener.onResponseHeader(name, value);
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onResponseHeaderComplete() throws IOException {
        if (this._delegatingResponses) {
            this._listener.onResponseHeaderComplete();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (this._delegatingResponses) {
            this._listener.onResponseStatus(version, status, reason);
            return;
        }
        this._version = version;
        this._status = status;
        this._reason = reason;
    }

    @Override // org.eclipse.jetty.client.HttpEventListener
    public void onRetry() {
        if (this._delegatingRequests) {
            this._listener.onRetry();
        }
    }
}
