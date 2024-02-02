package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.http.Generator;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.Parser;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class BlockingHttpConnection extends AbstractHttpConnection {
    private static final Logger LOG = Log.getLogger(BlockingHttpConnection.class);

    public BlockingHttpConnection(Connector connector, EndPoint endpoint, Server server) {
        super(connector, endpoint, server);
    }

    public BlockingHttpConnection(Connector connector, EndPoint endpoint, Server server, Parser parser, Generator generator, Request request) {
        super(connector, endpoint, server, parser, generator, request);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractHttpConnection
    public void handleRequest() throws IOException {
        super.handleRequest();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.eclipse.jetty.server.AbstractHttpConnection, org.eclipse.jetty.io.Connection
    public Connection handle() throws IOException {
        EndPoint endPoint;
        Connection switched;
        Connection switched2;
        Connection connection = this;
        try {
            setCurrentConnection(this);
            while (this._endp.isOpen() && connection == this) {
                try {
                    if (!this._parser.isComplete() && !this._endp.isInputShutdown()) {
                        this._parser.parseAvailable();
                    }
                    if (this._generator.isCommitted() && !this._generator.isComplete() && !this._endp.isOutputShutdown()) {
                        this._generator.flushBuffer();
                    }
                    this._endp.flush();
                    if (this._parser.isComplete() && this._generator.isComplete()) {
                        reset();
                        if (this._response.getStatus() == 101 && (switched2 = (Connection) this._request.getAttribute("org.eclipse.jetty.io.Connection")) != null) {
                            connection = switched2;
                        }
                        if (!this._generator.isPersistent() && !this._endp.isOutputShutdown()) {
                            LOG.warn("Safety net oshut!!! Please open a bugzilla", new Object[0]);
                            this._endp.shutdownOutput();
                        }
                    }
                } catch (HttpException e) {
                    if (LOG.isDebugEnabled()) {
                        Logger logger = LOG;
                        logger.debug("uri=" + this._uri, new Object[0]);
                        Logger logger2 = LOG;
                        logger2.debug("fields=" + this._requestFields, new Object[0]);
                        LOG.debug(e);
                    }
                    this._generator.sendError(e.getStatus(), e.getReason(), null, true);
                    this._parser.reset();
                    this._endp.shutdownOutput();
                    if (this._parser.isComplete() && this._generator.isComplete()) {
                        reset();
                        if (this._response.getStatus() == 101 && (switched = (Connection) this._request.getAttribute("org.eclipse.jetty.io.Connection")) != null) {
                            connection = switched;
                        }
                        if (!this._generator.isPersistent() && !this._endp.isOutputShutdown()) {
                            LOG.warn("Safety net oshut!!! Please open a bugzilla", new Object[0]);
                            this._endp.shutdownOutput();
                        }
                    }
                    if (this._endp.isInputShutdown() && this._generator.isIdle() && !this._request.getAsyncContinuation().isSuspended()) {
                        endPoint = this._endp;
                    }
                }
                if (this._endp.isInputShutdown() && this._generator.isIdle() && !this._request.getAsyncContinuation().isSuspended()) {
                    endPoint = this._endp;
                    endPoint.close();
                }
            }
            return connection;
        } finally {
            setCurrentConnection(null);
            this._parser.returnBuffers();
            this._generator.returnBuffers();
        }
    }
}
