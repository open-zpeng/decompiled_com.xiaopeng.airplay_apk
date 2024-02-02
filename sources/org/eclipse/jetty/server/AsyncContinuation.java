package org.eclipse.jetty.server;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public class AsyncContinuation implements AsyncContext, Continuation {
    private static final long DEFAULT_TIMEOUT = 30000;
    private static final int __ASYNCSTARTED = 2;
    private static final int __ASYNCWAIT = 4;
    private static final int __COMPLETED = 9;
    private static final int __COMPLETING = 7;
    private static final int __DISPATCHED = 1;
    private static final int __IDLE = 0;
    private static final int __REDISPATCH = 5;
    private static final int __REDISPATCHED = 6;
    private static final int __REDISPATCHING = 3;
    private static final int __UNCOMPLETED = 8;
    private List<AsyncListener> _asyncListeners;
    protected AbstractHttpConnection _connection;
    private volatile boolean _continuation;
    private List<ContinuationListener> _continuationListeners;
    private AsyncEventState _event;
    private volatile long _expireAt;
    private boolean _expired;
    private List<AsyncListener> _lastAsyncListeners;
    private volatile boolean _responseWrapped;
    private boolean _resumed;
    private static final Logger LOG = Log.getLogger(AsyncContinuation.class);
    private static final ContinuationThrowable __exception = new ContinuationThrowable();
    private long _timeoutMs = DEFAULT_TIMEOUT;
    private int _state = 0;
    private boolean _initial = true;

    /* JADX INFO: Access modifiers changed from: protected */
    public void setConnection(AbstractHttpConnection connection) {
        synchronized (this) {
            this._connection = connection;
        }
    }

    @Override // javax.servlet.AsyncContext
    public void addListener(AsyncListener listener) {
        synchronized (this) {
            if (this._asyncListeners == null) {
                this._asyncListeners = new ArrayList();
            }
            this._asyncListeners.add(listener);
        }
    }

    @Override // javax.servlet.AsyncContext
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        synchronized (this) {
            if (this._asyncListeners == null) {
                this._asyncListeners = new ArrayList();
            }
            this._asyncListeners.add(listener);
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void addContinuationListener(ContinuationListener listener) {
        synchronized (this) {
            if (this._continuationListeners == null) {
                this._continuationListeners = new ArrayList();
            }
            this._continuationListeners.add(listener);
        }
    }

    @Override // javax.servlet.AsyncContext, org.eclipse.jetty.continuation.Continuation
    public void setTimeout(long ms) {
        synchronized (this) {
            this._timeoutMs = ms;
        }
    }

    @Override // javax.servlet.AsyncContext
    public long getTimeout() {
        long j;
        synchronized (this) {
            j = this._timeoutMs;
        }
        return j;
    }

    public AsyncEventState getAsyncEventState() {
        AsyncEventState asyncEventState;
        synchronized (this) {
            asyncEventState = this._event;
        }
        return asyncEventState;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResponseWrapped() {
        return this._responseWrapped;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isInitial() {
        boolean z;
        synchronized (this) {
            z = this._initial;
        }
        return z;
    }

    public boolean isContinuation() {
        return this._continuation;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isSuspended() {
        synchronized (this) {
            int i = this._state;
            if (i != 7) {
                switch (i) {
                    case 2:
                    case 3:
                    case 4:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
    }

    public boolean isSuspending() {
        synchronized (this) {
            int i = this._state;
            if (i == 2 || i == 4) {
                return true;
            }
            return false;
        }
    }

    public boolean isDispatchable() {
        synchronized (this) {
            int i = this._state;
            if (i != 3) {
                switch (i) {
                    case 5:
                    case 6:
                    case 7:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
    }

    public String toString() {
        String str;
        synchronized (this) {
            str = super.toString() + "@" + getStatusString();
        }
        return str;
    }

    public String getStatusString() {
        String str;
        String sb;
        synchronized (this) {
            StringBuilder sb2 = new StringBuilder();
            if (this._state == 0) {
                str = "IDLE";
            } else if (this._state == 1) {
                str = "DISPATCHED";
            } else if (this._state == 2) {
                str = "ASYNCSTARTED";
            } else if (this._state == 4) {
                str = "ASYNCWAIT";
            } else if (this._state == 3) {
                str = "REDISPATCHING";
            } else if (this._state == 5) {
                str = "REDISPATCH";
            } else if (this._state == 6) {
                str = "REDISPATCHED";
            } else if (this._state == 7) {
                str = "COMPLETING";
            } else if (this._state == 8) {
                str = "UNCOMPLETED";
            } else if (this._state == 9) {
                str = "COMPLETE";
            } else {
                str = "UNKNOWN?" + this._state;
            }
            sb2.append(str);
            sb2.append(this._initial ? ",initial" : "");
            sb2.append(this._resumed ? ",resumed" : "");
            sb2.append(this._expired ? ",expired" : "");
            sb = sb2.toString();
        }
        return sb;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean handling() {
        synchronized (this) {
            this._continuation = false;
            int i = this._state;
            if (i != 0) {
                if (i != 7) {
                    switch (i) {
                        case 4:
                            return false;
                        case 5:
                            this._state = 6;
                            return true;
                        default:
                            throw new IllegalStateException(getStatusString());
                    }
                }
                this._state = 8;
                return false;
            }
            this._initial = true;
            this._state = 1;
            if (this._lastAsyncListeners != null) {
                this._lastAsyncListeners.clear();
            }
            if (this._asyncListeners != null) {
                this._asyncListeners.clear();
            } else {
                this._asyncListeners = this._lastAsyncListeners;
                this._lastAsyncListeners = null;
            }
            return true;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:23:0x0058 A[Catch: all -> 0x0082, TryCatch #0 {, blocks: (B:3:0x0001, B:8:0x000a, B:9:0x0013, B:10:0x0014, B:12:0x001d, B:14:0x0025, B:16:0x002d, B:19:0x0036, B:21:0x0049, B:23:0x0058, B:24:0x005d, B:20:0x0042), top: B:40:0x0001 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void doSuspend(javax.servlet.ServletContext r5, javax.servlet.ServletRequest r6, javax.servlet.ServletResponse r7) {
        /*
            r4 = this;
            monitor-enter(r4)
            int r0 = r4._state     // Catch: java.lang.Throwable -> L82
            r1 = 1
            if (r0 == r1) goto L14
            r1 = 6
            if (r0 != r1) goto La
            goto L14
        La:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException     // Catch: java.lang.Throwable -> L82
            java.lang.String r1 = r4.getStatusString()     // Catch: java.lang.Throwable -> L82
            r0.<init>(r1)     // Catch: java.lang.Throwable -> L82
            throw r0     // Catch: java.lang.Throwable -> L82
        L14:
            r0 = 0
            r4._resumed = r0     // Catch: java.lang.Throwable -> L82
            r4._expired = r0     // Catch: java.lang.Throwable -> L82
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            if (r0 == 0) goto L42
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            javax.servlet.ServletRequest r0 = r0.getSuppliedRequest()     // Catch: java.lang.Throwable -> L82
            if (r6 != r0) goto L42
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            javax.servlet.ServletResponse r0 = r0.getSuppliedResponse()     // Catch: java.lang.Throwable -> L82
            if (r7 != r0) goto L42
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            javax.servlet.ServletContext r0 = r0.getServletContext()     // Catch: java.lang.Throwable -> L82
            if (r5 == r0) goto L36
            goto L42
        L36:
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            r1 = 0
            org.eclipse.jetty.server.AsyncContinuation.AsyncEventState.access$002(r0, r1)     // Catch: java.lang.Throwable -> L82
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = r4._event     // Catch: java.lang.Throwable -> L82
            org.eclipse.jetty.server.AsyncContinuation.AsyncEventState.access$102(r0, r1)     // Catch: java.lang.Throwable -> L82
            goto L49
        L42:
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r0 = new org.eclipse.jetty.server.AsyncContinuation$AsyncEventState     // Catch: java.lang.Throwable -> L82
            r0.<init>(r5, r6, r7)     // Catch: java.lang.Throwable -> L82
            r4._event = r0     // Catch: java.lang.Throwable -> L82
        L49:
            r0 = 2
            r4._state = r0     // Catch: java.lang.Throwable -> L82
            java.util.List<javax.servlet.AsyncListener> r0 = r4._lastAsyncListeners     // Catch: java.lang.Throwable -> L82
            java.util.List<javax.servlet.AsyncListener> r1 = r4._asyncListeners     // Catch: java.lang.Throwable -> L82
            r4._lastAsyncListeners = r1     // Catch: java.lang.Throwable -> L82
            r4._asyncListeners = r0     // Catch: java.lang.Throwable -> L82
            java.util.List<javax.servlet.AsyncListener> r1 = r4._asyncListeners     // Catch: java.lang.Throwable -> L82
            if (r1 == 0) goto L5d
            java.util.List<javax.servlet.AsyncListener> r1 = r4._asyncListeners     // Catch: java.lang.Throwable -> L82
            r1.clear()     // Catch: java.lang.Throwable -> L82
        L5d:
            monitor-exit(r4)     // Catch: java.lang.Throwable -> L82
            java.util.List<javax.servlet.AsyncListener> r0 = r4._lastAsyncListeners
            if (r0 == 0) goto L81
            java.util.List<javax.servlet.AsyncListener> r0 = r4._lastAsyncListeners
            java.util.Iterator r0 = r0.iterator()
        L68:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L81
            java.lang.Object r1 = r0.next()
            javax.servlet.AsyncListener r1 = (javax.servlet.AsyncListener) r1
            org.eclipse.jetty.server.AsyncContinuation$AsyncEventState r2 = r4._event     // Catch: java.lang.Exception -> L7a
            r1.onStartAsync(r2)     // Catch: java.lang.Exception -> L7a
            goto L80
        L7a:
            r2 = move-exception
            org.eclipse.jetty.util.log.Logger r3 = org.eclipse.jetty.server.AsyncContinuation.LOG
            r3.warn(r2)
        L80:
            goto L68
        L81:
            return
        L82:
            r0 = move-exception
            monitor-exit(r4)     // Catch: java.lang.Throwable -> L82
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.AsyncContinuation.doSuspend(javax.servlet.ServletContext, javax.servlet.ServletRequest, javax.servlet.ServletResponse):void");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean unhandle() {
        synchronized (this) {
            switch (this._state) {
                case 0:
                    throw new IllegalStateException(getStatusString());
                case 1:
                case 6:
                    this._state = 8;
                    return true;
                case 2:
                    this._initial = false;
                    this._state = 4;
                    scheduleTimeout();
                    if (this._state == 4) {
                        return true;
                    }
                    if (this._state == 7) {
                        this._state = 8;
                        return true;
                    }
                    this._initial = false;
                    this._state = 6;
                    return false;
                case 3:
                    this._initial = false;
                    this._state = 6;
                    return false;
                case 4:
                case 5:
                default:
                    throw new IllegalStateException(getStatusString());
                case 7:
                    this._initial = false;
                    this._state = 8;
                    return true;
            }
        }
    }

    @Override // javax.servlet.AsyncContext
    public void dispatch() {
        synchronized (this) {
            int i = this._state;
            if (i == 2) {
                this._state = 3;
                this._resumed = true;
                return;
            }
            switch (i) {
                case 4:
                    boolean dispatch = !this._expired;
                    this._state = 5;
                    this._resumed = true;
                    if (dispatch) {
                        cancelTimeout();
                        scheduleDispatch();
                        return;
                    }
                    return;
                case 5:
                    return;
                default:
                    throw new IllegalStateException(getStatusString());
            }
        }
    }

    protected void expired() {
        synchronized (this) {
            List<AsyncListener> aListeners = null;
            try {
                try {
                    int i = this._state;
                    if (i == 2 || i == 4) {
                        List<ContinuationListener> cListeners = this._continuationListeners;
                        try {
                            aListeners = this._asyncListeners;
                            this._expired = true;
                            if (aListeners != null) {
                                for (AsyncListener listener : aListeners) {
                                    try {
                                        listener.onTimeout(this._event);
                                    } catch (Exception e) {
                                        LOG.debug(e);
                                        this._connection.getRequest().setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
                                    }
                                }
                            }
                            if (cListeners != null) {
                                for (ContinuationListener listener2 : cListeners) {
                                    try {
                                        listener2.onTimeout(this);
                                    } catch (Exception e2) {
                                        LOG.warn(e2);
                                    }
                                }
                            }
                            synchronized (this) {
                                int i2 = this._state;
                                if (i2 == 2 || i2 == 4) {
                                    dispatch();
                                } else if (!this._continuation) {
                                    this._expired = false;
                                }
                            }
                            scheduleDispatch();
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    @Override // javax.servlet.AsyncContext, org.eclipse.jetty.continuation.Continuation
    public void complete() {
        synchronized (this) {
            int i = this._state;
            if (i != 4) {
                if (i != 6) {
                    switch (i) {
                        case 1:
                            break;
                        case 2:
                            this._state = 7;
                            return;
                        default:
                            throw new IllegalStateException(getStatusString());
                    }
                }
                throw new IllegalStateException(getStatusString());
            }
            this._state = 7;
            boolean dispatch = !this._expired;
            if (dispatch) {
                cancelTimeout();
                scheduleDispatch();
            }
        }
    }

    public void errorComplete() {
        synchronized (this) {
            int i = this._state;
            if (i != 7) {
                switch (i) {
                    case 2:
                    case 3:
                        this._state = 7;
                        this._resumed = false;
                        return;
                    default:
                        throw new IllegalStateException(getStatusString());
                }
            }
        }
    }

    @Override // javax.servlet.AsyncContext
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doComplete(Throwable ex) {
        synchronized (this) {
            List<AsyncListener> aListeners = null;
            try {
                try {
                    if (this._state == 8) {
                        this._state = 9;
                        List<ContinuationListener> cListeners = this._continuationListeners;
                        try {
                            aListeners = this._asyncListeners;
                            if (aListeners != null) {
                                for (AsyncListener listener : aListeners) {
                                    if (ex != null) {
                                        try {
                                            this._event.getSuppliedRequest().setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);
                                            this._event.getSuppliedRequest().setAttribute(RequestDispatcher.ERROR_MESSAGE, ex.getMessage());
                                            listener.onError(this._event);
                                        } catch (Exception e) {
                                            LOG.warn(e);
                                        }
                                    } else {
                                        listener.onComplete(this._event);
                                    }
                                }
                            }
                            if (cListeners != null) {
                                for (ContinuationListener listener2 : cListeners) {
                                    try {
                                        listener2.onComplete(this);
                                    } catch (Exception e2) {
                                        LOG.warn(e2);
                                    }
                                }
                                return;
                            }
                            return;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                    throw new IllegalStateException(getStatusString());
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void recycle() {
        synchronized (this) {
            int i = this._state;
            if (i == 1 || i == 6) {
                throw new IllegalStateException(getStatusString());
            }
            this._state = 0;
            this._initial = true;
            this._resumed = false;
            this._expired = false;
            this._responseWrapped = false;
            cancelTimeout();
            this._timeoutMs = DEFAULT_TIMEOUT;
            this._continuationListeners = null;
        }
    }

    public void cancel() {
        synchronized (this) {
            cancelTimeout();
            this._continuationListeners = null;
        }
    }

    protected void scheduleDispatch() {
        EndPoint endp = this._connection.getEndPoint();
        if (!endp.isBlocking()) {
            ((AsyncEndPoint) endp).asyncDispatch();
        }
    }

    protected void scheduleTimeout() {
        EndPoint endp = this._connection.getEndPoint();
        if (this._timeoutMs > 0) {
            if (endp.isBlocking()) {
                synchronized (this) {
                    this._expireAt = System.currentTimeMillis() + this._timeoutMs;
                    long wait = this._timeoutMs;
                    while (this._expireAt > 0 && wait > 0 && this._connection.getServer().isRunning()) {
                        try {
                            wait(wait);
                        } catch (InterruptedException e) {
                            LOG.ignore(e);
                        }
                        wait = this._expireAt - System.currentTimeMillis();
                    }
                    if (this._expireAt > 0 && wait <= 0 && this._connection.getServer().isRunning()) {
                        expired();
                    }
                }
                return;
            }
            ((AsyncEndPoint) endp).scheduleTimeout(this._event._timeout, this._timeoutMs);
        }
    }

    protected void cancelTimeout() {
        EndPoint endp = this._connection.getEndPoint();
        if (endp.isBlocking()) {
            synchronized (this) {
                this._expireAt = 0L;
                notifyAll();
            }
            return;
        }
        AsyncEventState event = this._event;
        if (event == null) {
            return;
        }
        ((AsyncEndPoint) endp).cancelTimeout(event._timeout);
    }

    public boolean isCompleting() {
        boolean z;
        synchronized (this) {
            z = this._state == 7;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isUncompleted() {
        boolean z;
        synchronized (this) {
            z = this._state == 8;
        }
        return z;
    }

    public boolean isComplete() {
        boolean z;
        synchronized (this) {
            z = this._state == 9;
        }
        return z;
    }

    public boolean isAsyncStarted() {
        synchronized (this) {
            switch (this._state) {
                case 2:
                case 3:
                case 4:
                case 5:
                    return true;
                default:
                    return false;
            }
        }
    }

    public boolean isAsync() {
        synchronized (this) {
            switch (this._state) {
                case 0:
                case 1:
                case 8:
                case 9:
                    return false;
                default:
                    return true;
            }
        }
    }

    @Override // javax.servlet.AsyncContext
    public void dispatch(ServletContext context, String path) {
        this._event._dispatchContext = context;
        this._event.setPath(path);
        dispatch();
    }

    @Override // javax.servlet.AsyncContext
    public void dispatch(String path) {
        this._event.setPath(path);
        dispatch();
    }

    public Request getBaseRequest() {
        return this._connection.getRequest();
    }

    @Override // javax.servlet.AsyncContext
    public ServletRequest getRequest() {
        if (this._event != null) {
            return this._event.getSuppliedRequest();
        }
        return this._connection.getRequest();
    }

    @Override // javax.servlet.AsyncContext
    public ServletResponse getResponse() {
        if (this._responseWrapped && this._event != null && this._event.getSuppliedResponse() != null) {
            return this._event.getSuppliedResponse();
        }
        return this._connection.getResponse();
    }

    @Override // javax.servlet.AsyncContext
    public void start(final Runnable run) {
        final AsyncEventState event = this._event;
        if (event != null) {
            this._connection.getServer().getThreadPool().dispatch(new Runnable() { // from class: org.eclipse.jetty.server.AsyncContinuation.1
                @Override // java.lang.Runnable
                public void run() {
                    ((ContextHandler.Context) event.getServletContext()).getContextHandler().handle(run);
                }
            });
        }
    }

    @Override // javax.servlet.AsyncContext
    public boolean hasOriginalRequestAndResponse() {
        boolean z;
        synchronized (this) {
            z = this._event != null && this._event.getSuppliedRequest() == this._connection._request && this._event.getSuppliedResponse() == this._connection._response;
        }
        return z;
    }

    public ContextHandler getContextHandler() {
        AsyncEventState event = this._event;
        if (event != null) {
            return ((ContextHandler.Context) event.getServletContext()).getContextHandler();
        }
        return null;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResumed() {
        boolean z;
        synchronized (this) {
            z = this._resumed;
        }
        return z;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isExpired() {
        boolean z;
        synchronized (this) {
            z = this._expired;
        }
        return z;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void resume() {
        dispatch();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startAsync(ServletContext context, ServletRequest request, ServletResponse response) {
        synchronized (this) {
            this._responseWrapped = !(response instanceof Response);
            doSuspend(context, request, response);
            if (request instanceof HttpServletRequest) {
                this._event._pathInContext = URIUtil.addPaths(((HttpServletRequest) request).getServletPath(), ((HttpServletRequest) request).getPathInfo());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startAsync() {
        this._responseWrapped = false;
        this._continuation = false;
        doSuspend(this._connection.getRequest().getServletContext(), this._connection.getRequest(), this._connection.getResponse());
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend(ServletResponse response) {
        this._continuation = true;
        this._responseWrapped = response instanceof Response ? false : true;
        doSuspend(this._connection.getRequest().getServletContext(), this._connection.getRequest(), response);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend() {
        this._responseWrapped = false;
        this._continuation = true;
        doSuspend(this._connection.getRequest().getServletContext(), this._connection.getRequest(), this._connection.getResponse());
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public ServletResponse getServletResponse() {
        if (this._responseWrapped && this._event != null && this._event.getSuppliedResponse() != null) {
            return this._event.getSuppliedResponse();
        }
        return this._connection.getResponse();
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public Object getAttribute(String name) {
        return this._connection.getRequest().getAttribute(name);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void removeAttribute(String name) {
        this._connection.getRequest().removeAttribute(name);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void setAttribute(String name, Object attribute) {
        this._connection.getRequest().setAttribute(name, attribute);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void undispatch() {
        if (isSuspended()) {
            if (LOG.isDebugEnabled()) {
                throw new ContinuationThrowable();
            }
            throw __exception;
        }
        throw new IllegalStateException("!suspended");
    }

    /* loaded from: classes.dex */
    public class AsyncTimeout extends Timeout.Task implements Runnable {
        public AsyncTimeout() {
        }

        @Override // org.eclipse.jetty.util.thread.Timeout.Task
        public void expired() {
            AsyncContinuation.this.expired();
        }

        @Override // java.lang.Runnable
        public void run() {
            AsyncContinuation.this.expired();
        }
    }

    /* loaded from: classes.dex */
    public class AsyncEventState extends AsyncEvent {
        private ServletContext _dispatchContext;
        private String _pathInContext;
        private final ServletContext _suspendedContext;
        private Timeout.Task _timeout;

        public AsyncEventState(ServletContext context, ServletRequest request, ServletResponse response) {
            super(AsyncContinuation.this, request, response);
            this._timeout = new AsyncTimeout();
            this._suspendedContext = context;
            Request r = AsyncContinuation.this._connection.getRequest();
            if (r.getAttribute(AsyncContext.ASYNC_REQUEST_URI) == null) {
                String uri = (String) r.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
                if (uri != null) {
                    r.setAttribute(AsyncContext.ASYNC_REQUEST_URI, uri);
                    r.setAttribute(AsyncContext.ASYNC_CONTEXT_PATH, r.getAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH));
                    r.setAttribute(AsyncContext.ASYNC_SERVLET_PATH, r.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH));
                    r.setAttribute(AsyncContext.ASYNC_PATH_INFO, r.getAttribute(RequestDispatcher.FORWARD_PATH_INFO));
                    r.setAttribute(AsyncContext.ASYNC_QUERY_STRING, r.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING));
                    return;
                }
                r.setAttribute(AsyncContext.ASYNC_REQUEST_URI, r.getRequestURI());
                r.setAttribute(AsyncContext.ASYNC_CONTEXT_PATH, r.getContextPath());
                r.setAttribute(AsyncContext.ASYNC_SERVLET_PATH, r.getServletPath());
                r.setAttribute(AsyncContext.ASYNC_PATH_INFO, r.getPathInfo());
                r.setAttribute(AsyncContext.ASYNC_QUERY_STRING, r.getQueryString());
            }
        }

        public ServletContext getSuspendedContext() {
            return this._suspendedContext;
        }

        public ServletContext getDispatchContext() {
            return this._dispatchContext;
        }

        public ServletContext getServletContext() {
            return this._dispatchContext == null ? this._suspendedContext : this._dispatchContext;
        }

        public void setPath(String path) {
            this._pathInContext = path;
        }

        public String getPath() {
            return this._pathInContext;
        }
    }
}
