package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
/* loaded from: classes.dex */
public class HandlerWrapper extends AbstractHandlerContainer {
    protected Handler _handler;

    public Handler getHandler() {
        return this._handler;
    }

    @Override // org.eclipse.jetty.server.HandlerContainer
    public Handler[] getHandlers() {
        return this._handler == null ? new Handler[0] : new Handler[]{this._handler};
    }

    public void setHandler(Handler handler) {
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        Handler old_handler = this._handler;
        this._handler = handler;
        if (handler != null) {
            handler.setServer(getServer());
        }
        if (getServer() != null) {
            getServer().getContainer().update(this, old_handler, handler, "handler");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        if (this._handler != null) {
            this._handler.start();
        }
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        if (this._handler != null) {
            this._handler.stop();
        }
        super.doStop();
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._handler != null && isStarted()) {
            this._handler.handle(target, baseRequest, request, response);
        }
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        Server old_server = getServer();
        if (server == old_server) {
            return;
        }
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        super.setServer(server);
        Handler h = getHandler();
        if (h != null) {
            h.setServer(server);
        }
        if (server != null && server != old_server) {
            server.getContainer().update(this, (Object) null, this._handler, "handler");
        }
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer
    protected Object expandChildren(Object list, Class byClass) {
        return expandHandler(this._handler, list, byClass);
    }

    public <H extends Handler> H getNestedHandlerByClass(Class<H> byclass) {
        HandlerWrapper h = this;
        while (h != null) {
            if (byclass.isInstance(h)) {
                return h;
            }
            Handler w = h.getHandler();
            if (w instanceof HandlerWrapper) {
                h = (HandlerWrapper) w;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Destroyable
    public void destroy() {
        if (!isStopped()) {
            throw new IllegalStateException("!STOPPED");
        }
        Handler child = getHandler();
        if (child != null) {
            setHandler(null);
            child.destroy();
        }
        super.destroy();
    }
}
