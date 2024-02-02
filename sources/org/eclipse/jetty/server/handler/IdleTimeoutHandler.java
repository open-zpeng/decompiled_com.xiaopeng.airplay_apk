package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
/* loaded from: classes.dex */
public class IdleTimeoutHandler extends HandlerWrapper {
    private int _idleTimeoutMs = 1000;
    private boolean _applyToAsync = false;

    public boolean isApplyToAsync() {
        return this._applyToAsync;
    }

    public void setApplyToAsync(boolean applyToAsync) {
        this._applyToAsync = applyToAsync;
    }

    public long getIdleTimeoutMs() {
        return this._idleTimeoutMs;
    }

    public void setIdleTimeoutMs(int _idleTimeoutMs) {
        this._idleTimeoutMs = _idleTimeoutMs;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final int idle_timeout;
        AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
        final EndPoint endp = connection == null ? null : connection.getEndPoint();
        if (endp == null) {
            idle_timeout = -1;
        } else {
            idle_timeout = endp.getMaxIdleTime();
            endp.setMaxIdleTime(this._idleTimeoutMs);
        }
        try {
            super.handle(target, baseRequest, request, response);
            if (endp != null) {
                if (this._applyToAsync && request.isAsyncStarted()) {
                    request.getAsyncContext().addListener(new AsyncListener() { // from class: org.eclipse.jetty.server.handler.IdleTimeoutHandler.1
                        @Override // javax.servlet.AsyncListener
                        public void onTimeout(AsyncEvent event) throws IOException {
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onStartAsync(AsyncEvent event) throws IOException {
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onError(AsyncEvent event) throws IOException {
                            endp.setMaxIdleTime(idle_timeout);
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onComplete(AsyncEvent event) throws IOException {
                            endp.setMaxIdleTime(idle_timeout);
                        }
                    });
                } else {
                    endp.setMaxIdleTime(idle_timeout);
                }
            }
        } catch (Throwable th) {
            if (endp != null) {
                if (this._applyToAsync && request.isAsyncStarted()) {
                    request.getAsyncContext().addListener(new AsyncListener() { // from class: org.eclipse.jetty.server.handler.IdleTimeoutHandler.1
                        @Override // javax.servlet.AsyncListener
                        public void onTimeout(AsyncEvent event) throws IOException {
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onStartAsync(AsyncEvent event) throws IOException {
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onError(AsyncEvent event) throws IOException {
                            endp.setMaxIdleTime(idle_timeout);
                        }

                        @Override // javax.servlet.AsyncListener
                        public void onComplete(AsyncEvent event) throws IOException {
                            endp.setMaxIdleTime(idle_timeout);
                        }
                    });
                } else {
                    endp.setMaxIdleTime(idle_timeout);
                }
            }
            throw th;
        }
    }
}
