package org.eclipse.jetty.server.handler;

import java.io.IOException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractHandler extends AggregateLifeCycle implements Handler {
    private static final Logger LOG = Log.getLogger(AbstractHandler.class);
    private Server _server;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        LOG.debug("starting {}", this);
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        LOG.debug("stopping {}", this);
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        Server old_server = this._server;
        if (old_server != null && old_server != server) {
            old_server.getContainer().removeBean(this);
        }
        this._server = server;
        if (this._server != null && this._server != old_server) {
            this._server.getContainer().addBean(this);
        }
    }

    @Override // org.eclipse.jetty.server.Handler
    public Server getServer() {
        return this._server;
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Destroyable
    public void destroy() {
        if (!isStopped()) {
            throw new IllegalStateException("!STOPPED");
        }
        super.destroy();
        if (this._server != null) {
            this._server.getContainer().removeBean(this);
        }
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle
    public void dumpThis(Appendable out) throws IOException {
        out.append(toString()).append(" - ").append(getState()).append('\n');
    }
}
