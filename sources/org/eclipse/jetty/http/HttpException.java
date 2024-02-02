package org.eclipse.jetty.http;

import java.io.IOException;
/* loaded from: classes.dex */
public class HttpException extends IOException {
    String _reason;
    int _status;

    public HttpException(int status) {
        this._status = status;
        this._reason = null;
    }

    public HttpException(int status, String reason) {
        this._status = status;
        this._reason = reason;
    }

    public HttpException(int status, String reason, Throwable rootCause) {
        this._status = status;
        this._reason = reason;
        initCause(rootCause);
    }

    public String getReason() {
        return this._reason;
    }

    public void setReason(String reason) {
        this._reason = reason;
    }

    public int getStatus() {
        return this._status;
    }

    public void setStatus(int status) {
        this._status = status;
    }

    @Override // java.lang.Throwable
    public String toString() {
        return "HttpException(" + this._status + "," + this._reason + "," + super.getCause() + ")";
    }
}
