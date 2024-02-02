package org.eclipse.jetty.http;

import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.BuffersFactory;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
/* loaded from: classes.dex */
public class HttpBuffersImpl extends AbstractLifeCycle implements HttpBuffers {
    private Buffers _requestBuffers;
    private Buffers _responseBuffers;
    private int _requestBufferSize = 16384;
    private int _requestHeaderSize = 6144;
    private int _responseBufferSize = 32768;
    private int _responseHeaderSize = 6144;
    private int _maxBuffers = 1024;
    private Buffers.Type _requestBufferType = Buffers.Type.BYTE_ARRAY;
    private Buffers.Type _requestHeaderType = Buffers.Type.BYTE_ARRAY;
    private Buffers.Type _responseBufferType = Buffers.Type.BYTE_ARRAY;
    private Buffers.Type _responseHeaderType = Buffers.Type.BYTE_ARRAY;

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getRequestBufferSize() {
        return this._requestBufferSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestBufferSize(int requestBufferSize) {
        this._requestBufferSize = requestBufferSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getRequestHeaderSize() {
        return this._requestHeaderSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestHeaderSize(int requestHeaderSize) {
        this._requestHeaderSize = requestHeaderSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getResponseBufferSize() {
        return this._responseBufferSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseBufferSize(int responseBufferSize) {
        this._responseBufferSize = responseBufferSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getResponseHeaderSize() {
        return this._responseHeaderSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseHeaderSize(int responseHeaderSize) {
        this._responseHeaderSize = responseHeaderSize;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getRequestBufferType() {
        return this._requestBufferType;
    }

    public void setRequestBufferType(Buffers.Type requestBufferType) {
        this._requestBufferType = requestBufferType;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getRequestHeaderType() {
        return this._requestHeaderType;
    }

    public void setRequestHeaderType(Buffers.Type requestHeaderType) {
        this._requestHeaderType = requestHeaderType;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getResponseBufferType() {
        return this._responseBufferType;
    }

    public void setResponseBufferType(Buffers.Type responseBufferType) {
        this._responseBufferType = responseBufferType;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getResponseHeaderType() {
        return this._responseHeaderType;
    }

    public void setResponseHeaderType(Buffers.Type responseHeaderType) {
        this._responseHeaderType = responseHeaderType;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestBuffers(Buffers requestBuffers) {
        this._requestBuffers = requestBuffers;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseBuffers(Buffers responseBuffers) {
        this._responseBuffers = responseBuffers;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._requestBuffers = BuffersFactory.newBuffers(this._requestHeaderType, this._requestHeaderSize, this._requestBufferType, this._requestBufferSize, this._requestBufferType, getMaxBuffers());
        this._responseBuffers = BuffersFactory.newBuffers(this._responseHeaderType, this._responseHeaderSize, this._responseBufferType, this._responseBufferSize, this._responseBufferType, getMaxBuffers());
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._requestBuffers = null;
        this._responseBuffers = null;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers getRequestBuffers() {
        return this._requestBuffers;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers getResponseBuffers() {
        return this._responseBuffers;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setMaxBuffers(int maxBuffers) {
        this._maxBuffers = maxBuffers;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getMaxBuffers() {
        return this._maxBuffers;
    }

    public String toString() {
        return this._requestBuffers + "/" + this._responseBuffers;
    }
}
