package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
/* loaded from: classes.dex */
public class HandlerCollection extends AbstractHandlerContainer {
    private volatile Handler[] _handlers;
    private final boolean _mutableWhenRunning;
    private boolean _parallelStart;

    public HandlerCollection() {
        this._parallelStart = false;
        this._mutableWhenRunning = false;
    }

    public HandlerCollection(boolean mutableWhenRunning) {
        this._parallelStart = false;
        this._mutableWhenRunning = mutableWhenRunning;
    }

    @Override // org.eclipse.jetty.server.HandlerContainer
    public Handler[] getHandlers() {
        return this._handlers;
    }

    public void setHandlers(Handler[] handlers) {
        if (!this._mutableWhenRunning && isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        Handler[] old_handlers = this._handlers == null ? null : (Handler[]) this._handlers.clone();
        this._handlers = handlers;
        Server server = getServer();
        MultiException mex = new MultiException();
        for (int i = 0; handlers != null && i < handlers.length; i++) {
            if (handlers[i].getServer() != server) {
                handlers[i].setServer(server);
            }
        }
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object[]) old_handlers, (Object[]) handlers, "handler");
        }
        for (int i2 = 0; old_handlers != null && i2 < old_handlers.length; i2++) {
            if (old_handlers[i2] != null) {
                try {
                    if (old_handlers[i2].isStarted()) {
                        old_handlers[i2].stop();
                    }
                } catch (Throwable e) {
                    mex.add(e);
                }
            }
        }
        mex.ifExceptionThrowRuntime();
    }

    public boolean isParallelStart() {
        return this._parallelStart;
    }

    public void setParallelStart(boolean parallelStart) {
        this._parallelStart = parallelStart;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._handlers != null && isStarted()) {
            MultiException mex = null;
            for (int i = 0; i < this._handlers.length; i++) {
                try {
                    this._handlers[i].handle(target, baseRequest, request, response);
                } catch (IOException e) {
                    throw e;
                } catch (RuntimeException e2) {
                    throw e2;
                } catch (Exception e3) {
                    if (mex == null) {
                        mex = new MultiException();
                    }
                    mex.add(e3);
                }
            }
            if (mex != null) {
                if (mex.size() == 1) {
                    throw new ServletException(mex.getThrowable(0));
                }
                throw new ServletException(mex);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        final MultiException mex = new MultiException();
        if (this._handlers != null) {
            int i = 0;
            if (!this._parallelStart) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this._handlers.length) {
                        break;
                    }
                    try {
                        this._handlers[i2].start();
                    } catch (Throwable e) {
                        mex.add(e);
                    }
                    i = i2 + 1;
                }
            } else {
                final CountDownLatch latch = new CountDownLatch(this._handlers.length);
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                while (true) {
                    final int i3 = i;
                    if (i3 >= this._handlers.length) {
                        break;
                    }
                    getServer().getThreadPool().dispatch(new Runnable() { // from class: org.eclipse.jetty.server.handler.HandlerCollection.1
                        @Override // java.lang.Runnable
                        public void run() {
                            ClassLoader orig = Thread.currentThread().getContextClassLoader();
                            try {
                                Thread.currentThread().setContextClassLoader(loader);
                                HandlerCollection.this._handlers[i3].start();
                            } finally {
                                try {
                                } finally {
                                }
                            }
                        }
                    });
                    i = i3 + 1;
                }
                latch.await();
            }
        }
        super.doStart();
        mex.ifExceptionThrow();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        MultiException mex = new MultiException();
        try {
            super.doStop();
        } catch (Throwable e) {
            mex.add(e);
        }
        if (this._handlers != null) {
            int i = this._handlers.length;
            while (true) {
                int i2 = i - 1;
                if (i <= 0) {
                    break;
                }
                try {
                    this._handlers[i2].stop();
                } catch (Throwable e2) {
                    mex.add(e2);
                }
                i = i2;
            }
        }
        mex.ifExceptionThrow();
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        Server old_server = getServer();
        super.setServer(server);
        Handler[] h = getHandlers();
        for (int i = 0; h != null && i < h.length; i++) {
            h[i].setServer(server);
        }
        if (server != null && server != old_server) {
            server.getContainer().update((Object) this, (Object[]) null, (Object[]) this._handlers, "handler");
        }
    }

    public void addHandler(Handler handler) {
        setHandlers((Handler[]) LazyList.addToArray(getHandlers(), handler, Handler.class));
    }

    public void removeHandler(Handler handler) {
        Handler[] handlers = getHandlers();
        if (handlers != null && handlers.length > 0) {
            setHandlers((Handler[]) LazyList.removeFromArray(handlers, handler));
        }
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer
    protected Object expandChildren(Object list, Class byClass) {
        Handler[] handlers = getHandlers();
        for (int i = 0; handlers != null && i < handlers.length; i++) {
            list = expandHandler(handlers[i], list, byClass);
        }
        return list;
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Destroyable
    public void destroy() {
        if (!isStopped()) {
            throw new IllegalStateException("!STOPPED");
        }
        Handler[] children = getChildHandlers();
        setHandlers(null);
        for (Handler child : children) {
            child.destroy();
        }
        super.destroy();
    }
}
