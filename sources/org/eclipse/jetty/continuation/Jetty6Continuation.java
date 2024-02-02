package org.eclipse.jetty.continuation;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import org.eclipse.jetty.continuation.ContinuationFilter;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
/* loaded from: classes.dex */
public class Jetty6Continuation implements ContinuationFilter.FilteredContinuation {
    private static final Logger LOG = Log.getLogger(Jetty6Continuation.class.getName());
    private static final ContinuationThrowable __exception = new ContinuationThrowable();
    private final org.mortbay.util.ajax.Continuation _j6Continuation;
    private List<ContinuationListener> _listeners;
    private final ServletRequest _request;
    private ServletResponse _response;
    private Throwable _retry;
    private int _timeout;
    private boolean _initial = true;
    private volatile boolean _completed = false;
    private volatile boolean _resumed = false;
    private volatile boolean _expired = false;
    private boolean _responseWrapped = false;

    public Jetty6Continuation(ServletRequest request, org.mortbay.util.ajax.Continuation continuation) {
        if (!ContinuationFilter._initialized) {
            LOG.warn("!ContinuationFilter installed", (Object) null, (Object) null);
            throw new IllegalStateException("!ContinuationFilter installed");
        }
        this._request = request;
        this._j6Continuation = continuation;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void addContinuationListener(ContinuationListener listener) {
        if (this._listeners == null) {
            this._listeners = new ArrayList();
        }
        this._listeners.add(listener);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void complete() {
        synchronized (this) {
            if (this._resumed) {
                throw new IllegalStateException();
            }
            this._completed = true;
            if (this._j6Continuation.isPending()) {
                this._j6Continuation.resume();
            }
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public Object getAttribute(String name) {
        return this._request.getAttribute(name);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void removeAttribute(String name) {
        this._request.removeAttribute(name);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void setAttribute(String name, Object attribute) {
        this._request.setAttribute(name, attribute);
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public ServletResponse getServletResponse() {
        return this._response;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isExpired() {
        return this._expired;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isInitial() {
        return this._initial;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResumed() {
        return this._resumed;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isSuspended() {
        return this._retry != null;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void resume() {
        synchronized (this) {
            if (this._completed) {
                throw new IllegalStateException();
            }
            this._resumed = true;
            if (this._j6Continuation.isPending()) {
                this._j6Continuation.resume();
            }
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void setTimeout(long timeoutMs) {
        this._timeout = timeoutMs > 2147483647L ? Integer.MAX_VALUE : (int) timeoutMs;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend(ServletResponse response) {
        try {
            this._response = response;
            this._responseWrapped = this._response instanceof ServletResponseWrapper;
            this._resumed = false;
            this._expired = false;
            this._completed = false;
            this._j6Continuation.suspend(this._timeout);
        } catch (Throwable retry) {
            this._retry = retry;
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend() {
        try {
            this._response = null;
            this._responseWrapped = false;
            this._resumed = false;
            this._expired = false;
            this._completed = false;
            this._j6Continuation.suspend(this._timeout);
        } catch (Throwable retry) {
            this._retry = retry;
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResponseWrapped() {
        return this._responseWrapped;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void undispatch() {
        if (isSuspended()) {
            if (ContinuationFilter.__debug) {
                throw new ContinuationThrowable();
            }
            throw __exception;
        }
        throw new IllegalStateException("!suspended");
    }

    @Override // org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation
    public boolean enter(ServletResponse response) {
        this._response = response;
        this._expired = !this._j6Continuation.isResumed();
        if (this._initial) {
            return true;
        }
        this._j6Continuation.reset();
        if (this._expired && this._listeners != null) {
            for (ContinuationListener l : this._listeners) {
                l.onTimeout(this);
            }
        }
        return !this._completed;
    }

    @Override // org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation
    public boolean exit() {
        this._initial = false;
        Throwable th = this._retry;
        this._retry = null;
        if (th instanceof Error) {
            throw ((Error) th);
        }
        if (th instanceof RuntimeException) {
            throw ((RuntimeException) th);
        }
        if (this._listeners != null) {
            for (ContinuationListener l : this._listeners) {
                l.onComplete(this);
            }
            return true;
        }
        return true;
    }
}
