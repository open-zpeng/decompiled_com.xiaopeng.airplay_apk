package org.eclipse.jetty.continuation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
/* loaded from: classes.dex */
public class Servlet3Continuation implements Continuation {
    private static final ContinuationThrowable __exception = new ContinuationThrowable();
    private AsyncContext _context;
    private final ServletRequest _request;
    private ServletResponse _response;
    private List<AsyncListener> _listeners = new ArrayList();
    private volatile boolean _initial = true;
    private volatile boolean _resumed = false;
    private volatile boolean _expired = false;
    private volatile boolean _responseWrapped = false;
    private long _timeoutMs = -1;

    public Servlet3Continuation(ServletRequest request) {
        this._request = request;
        this._listeners.add(new AsyncListener() { // from class: org.eclipse.jetty.continuation.Servlet3Continuation.1
            @Override // javax.servlet.AsyncListener
            public void onComplete(AsyncEvent event) throws IOException {
            }

            @Override // javax.servlet.AsyncListener
            public void onError(AsyncEvent event) throws IOException {
            }

            @Override // javax.servlet.AsyncListener
            public void onStartAsync(AsyncEvent event) throws IOException {
                event.getAsyncContext().addListener(this);
            }

            @Override // javax.servlet.AsyncListener
            public void onTimeout(AsyncEvent event) throws IOException {
                Servlet3Continuation.this._initial = false;
                event.getAsyncContext().dispatch();
            }
        });
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void addContinuationListener(final ContinuationListener listener) {
        AsyncListener wrapped = new AsyncListener() { // from class: org.eclipse.jetty.continuation.Servlet3Continuation.2
            @Override // javax.servlet.AsyncListener
            public void onComplete(AsyncEvent event) throws IOException {
                listener.onComplete(Servlet3Continuation.this);
            }

            @Override // javax.servlet.AsyncListener
            public void onError(AsyncEvent event) throws IOException {
                listener.onComplete(Servlet3Continuation.this);
            }

            @Override // javax.servlet.AsyncListener
            public void onStartAsync(AsyncEvent event) throws IOException {
                event.getAsyncContext().addListener(this);
            }

            @Override // javax.servlet.AsyncListener
            public void onTimeout(AsyncEvent event) throws IOException {
                Servlet3Continuation.this._expired = true;
                listener.onTimeout(Servlet3Continuation.this);
            }
        };
        if (this._context != null) {
            this._context.addListener(wrapped);
        } else {
            this._listeners.add(wrapped);
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void complete() {
        AsyncContext context = this._context;
        if (context == null) {
            throw new IllegalStateException();
        }
        this._context.complete();
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
        return this._initial && this._request.getDispatcherType() != DispatcherType.ASYNC;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResumed() {
        return this._resumed;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isSuspended() {
        return this._request.isAsyncStarted();
    }

    public void keepWrappers() {
        this._responseWrapped = true;
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void resume() {
        AsyncContext context = this._context;
        if (context == null) {
            throw new IllegalStateException();
        }
        this._resumed = true;
        this._context.dispatch();
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void setTimeout(long timeoutMs) {
        this._timeoutMs = timeoutMs;
        if (this._context != null) {
            this._context.setTimeout(timeoutMs);
        }
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend(ServletResponse response) {
        this._response = response;
        this._responseWrapped = response instanceof ServletResponseWrapper;
        this._resumed = false;
        this._expired = false;
        this._context = this._request.startAsync();
        this._context.setTimeout(this._timeoutMs);
        for (AsyncListener listener : this._listeners) {
            this._context.addListener(listener);
        }
        this._listeners.clear();
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public void suspend() {
        this._resumed = false;
        this._expired = false;
        this._context = this._request.startAsync();
        this._context.setTimeout(this._timeoutMs);
        for (AsyncListener listener : this._listeners) {
            this._context.addListener(listener);
        }
        this._listeners.clear();
    }

    @Override // org.eclipse.jetty.continuation.Continuation
    public boolean isResponseWrapped() {
        return this._responseWrapped;
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
    public void undispatch() {
        if (isSuspended()) {
            if (ContinuationFilter.__debug) {
                throw new ContinuationThrowable();
            }
            throw __exception;
        }
        throw new IllegalStateException("!suspended");
    }
}
