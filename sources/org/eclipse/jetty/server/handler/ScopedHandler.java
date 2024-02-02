package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
/* loaded from: classes.dex */
public abstract class ScopedHandler extends HandlerWrapper {
    private static final ThreadLocal<ScopedHandler> __outerScope = new ThreadLocal<>();
    protected ScopedHandler _nextScope;
    protected ScopedHandler _outerScope;

    public abstract void doHandle(String str, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException;

    public abstract void doScope(String str, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        try {
            this._outerScope = __outerScope.get();
            if (this._outerScope == null) {
                __outerScope.set(this);
            }
            super.doStart();
            this._nextScope = (ScopedHandler) getChildHandlerByClass(ScopedHandler.class);
        } finally {
            if (this._outerScope == null) {
                __outerScope.set(null);
            }
        }
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._outerScope == null) {
            doScope(target, baseRequest, request, response);
        } else {
            doHandle(target, baseRequest, request, response);
        }
    }

    public final void nextScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._nextScope != null) {
            this._nextScope.doScope(target, baseRequest, request, response);
        } else if (this._outerScope != null) {
            this._outerScope.doHandle(target, baseRequest, request, response);
        } else {
            doHandle(target, baseRequest, request, response);
        }
    }

    public final void nextHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._nextScope != null && this._nextScope == this._handler) {
            this._nextScope.doHandle(target, baseRequest, request, response);
        } else if (this._handler != null) {
            this._handler.handle(target, baseRequest, request, response);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean never() {
        return false;
    }
}
