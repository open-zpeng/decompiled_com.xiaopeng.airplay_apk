package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class RequestLogHandler extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(RequestLogHandler.class);
    private RequestLog _requestLog;

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, final Request baseRequest, HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        AsyncContinuation continuation = baseRequest.getAsyncContinuation();
        if (!continuation.isInitial()) {
            baseRequest.setDispatchTime(System.currentTimeMillis());
        }
        try {
            super.handle(target, baseRequest, request, response);
            if (this._requestLog != null && baseRequest.getDispatcherType().equals(DispatcherType.REQUEST)) {
                if (continuation.isAsync()) {
                    if (continuation.isInitial()) {
                        continuation.addContinuationListener(new ContinuationListener() { // from class: org.eclipse.jetty.server.handler.RequestLogHandler.1
                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onTimeout(Continuation continuation2) {
                            }

                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onComplete(Continuation continuation2) {
                                RequestLogHandler.this._requestLog.log(baseRequest, (Response) response);
                            }
                        });
                        return;
                    }
                    return;
                }
                this._requestLog.log(baseRequest, (Response) response);
            }
        } catch (Throwable th) {
            if (this._requestLog != null && baseRequest.getDispatcherType().equals(DispatcherType.REQUEST)) {
                if (continuation.isAsync()) {
                    if (continuation.isInitial()) {
                        continuation.addContinuationListener(new ContinuationListener() { // from class: org.eclipse.jetty.server.handler.RequestLogHandler.1
                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onTimeout(Continuation continuation2) {
                            }

                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onComplete(Continuation continuation2) {
                                RequestLogHandler.this._requestLog.log(baseRequest, (Response) response);
                            }
                        });
                    }
                } else {
                    this._requestLog.log(baseRequest, (Response) response);
                }
            }
            throw th;
        }
    }

    public void setRequestLog(RequestLog requestLog) {
        try {
            if (this._requestLog != null) {
                this._requestLog.stop();
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object) this._requestLog, (Object) requestLog, "logimpl", true);
        }
        this._requestLog = requestLog;
        try {
            if (isStarted() && this._requestLog != null) {
                this._requestLog.start();
            }
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        if (this._requestLog != null) {
            if (getServer() != null && getServer() != server) {
                getServer().getContainer().update((Object) this, (Object) this._requestLog, (Object) null, "logimpl", true);
            }
            super.setServer(server);
            if (server != null && server != getServer()) {
                server.getContainer().update((Object) this, (Object) null, (Object) this._requestLog, "logimpl", true);
                return;
            }
            return;
        }
        super.setServer(server);
    }

    public RequestLog getRequestLog() {
        return this._requestLog;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        if (this._requestLog == null) {
            LOG.warn("!RequestLog", new Object[0]);
            this._requestLog = new NullRequestLog();
        }
        super.doStart();
        this._requestLog.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        this._requestLog.stop();
        if (this._requestLog instanceof NullRequestLog) {
            this._requestLog = null;
        }
    }

    /* loaded from: classes.dex */
    private static class NullRequestLog extends AbstractLifeCycle implements RequestLog {
        private NullRequestLog() {
        }

        @Override // org.eclipse.jetty.server.RequestLog
        public void log(Request request, Response response) {
        }
    }
}
