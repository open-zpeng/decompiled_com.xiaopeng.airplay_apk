package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AsyncHttpConnection extends AbstractHttpConnection implements AsyncConnection {
    private final AsyncEndPoint _asyncEndp;
    private boolean _readInterested;
    private int _total_no_progress;
    private static final int NO_PROGRESS_INFO = Integer.getInteger("org.mortbay.jetty.NO_PROGRESS_INFO", 100).intValue();
    private static final int NO_PROGRESS_CLOSE = Integer.getInteger("org.mortbay.jetty.NO_PROGRESS_CLOSE", 200).intValue();
    private static final Logger LOG = Log.getLogger(AsyncHttpConnection.class);

    public AsyncHttpConnection(Connector connector, EndPoint endpoint, Server server) {
        super(connector, endpoint, server);
        this._readInterested = true;
        this._asyncEndp = (AsyncEndPoint) endpoint;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.eclipse.jetty.server.AbstractHttpConnection, org.eclipse.jetty.io.Connection
    public Connection handle() throws IOException {
        Connection switched;
        boolean complete;
        Connection switched2;
        Connection connection = this;
        boolean some_progress = false;
        boolean progress = true;
        try {
            setCurrentConnection(this);
            this._asyncEndp.setCheckForIdle(false);
            while (progress && connection == this) {
                progress = false;
                try {
                    if (this._request._async.isAsync()) {
                        if (this._request._async.isDispatchable()) {
                            handleRequest();
                        }
                    } else if (!this._parser.isComplete() && this._parser.parseAvailable()) {
                        progress = true;
                    }
                    if (this._generator.isCommitted() && !this._generator.isComplete() && !this._endp.isOutputShutdown() && !this._request.getAsyncContinuation().isAsyncStarted() && this._generator.flushBuffer() > 0) {
                        progress = true;
                    }
                    this._endp.flush();
                    if (this._asyncEndp.hasProgressed()) {
                        progress = true;
                    }
                    some_progress |= progress;
                    boolean parserComplete = this._parser.isComplete();
                    boolean generatorComplete = this._generator.isComplete();
                    complete = parserComplete && generatorComplete;
                    if (parserComplete) {
                        if (generatorComplete) {
                            progress = true;
                            if (this._response.getStatus() == 101 && (switched2 = (Connection) this._request.getAttribute("org.eclipse.jetty.io.Connection")) != null) {
                                connection = switched2;
                            }
                            reset();
                            if (!this._generator.isPersistent() && !this._endp.isOutputShutdown()) {
                                LOG.warn("Safety net oshut!!!  IF YOU SEE THIS, PLEASE RAISE BUGZILLA", new Object[0]);
                                this._endp.shutdownOutput();
                            }
                        } else {
                            this._readInterested = false;
                            LOG.debug("Disabled read interest while writing response {}", this._endp);
                        }
                    }
                } catch (HttpException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("uri=" + this._uri, new Object[0]);
                        LOG.debug("fields=" + this._requestFields, new Object[0]);
                        LOG.debug(e);
                    }
                    progress = true;
                    this._generator.sendError(e.getStatus(), e.getReason(), null, true);
                    some_progress |= true;
                    boolean parserComplete2 = this._parser.isComplete();
                    boolean generatorComplete2 = this._generator.isComplete();
                    boolean complete2 = parserComplete2 && generatorComplete2;
                    if (parserComplete2) {
                        if (generatorComplete2) {
                            progress = true;
                            if (this._response.getStatus() == 101 && (switched = (Connection) this._request.getAttribute("org.eclipse.jetty.io.Connection")) != null) {
                                connection = switched;
                            }
                            reset();
                            if (!this._generator.isPersistent() && !this._endp.isOutputShutdown()) {
                                LOG.warn("Safety net oshut!!!  IF YOU SEE THIS, PLEASE RAISE BUGZILLA", new Object[0]);
                                this._endp.shutdownOutput();
                            }
                        } else {
                            this._readInterested = false;
                            LOG.debug("Disabled read interest while writing response {}", this._endp);
                        }
                    }
                    if (!complete2 && this._request.getAsyncContinuation().isAsyncStarted()) {
                        LOG.debug("suspended {}", this);
                    }
                }
                if (!complete && this._request.getAsyncContinuation().isAsyncStarted()) {
                    LOG.debug("suspended {}", this);
                    progress = false;
                }
            }
            setCurrentConnection(null);
            if (!this._request.getAsyncContinuation().isAsyncStarted()) {
                this._parser.returnBuffers();
                this._generator.returnBuffers();
                this._asyncEndp.setCheckForIdle(true);
            }
            if (some_progress) {
                this._total_no_progress = 0;
            } else {
                this._total_no_progress++;
                if (NO_PROGRESS_INFO > 0 && this._total_no_progress % NO_PROGRESS_INFO == 0 && (NO_PROGRESS_CLOSE <= 0 || this._total_no_progress < NO_PROGRESS_CLOSE)) {
                    LOG.info("EndPoint making no progress: " + this._total_no_progress + " " + this._endp + " " + this, new Object[0]);
                }
                if (NO_PROGRESS_CLOSE > 0 && this._total_no_progress == NO_PROGRESS_CLOSE) {
                    LOG.warn("Closing EndPoint making no progress: " + this._total_no_progress + " " + this._endp + " " + this, new Object[0]);
                    if (this._endp instanceof SelectChannelEndPoint) {
                        ((SelectChannelEndPoint) this._endp).getChannel().close();
                    }
                }
            }
            return connection;
        } catch (Throwable th) {
            setCurrentConnection(null);
            if (!this._request.getAsyncContinuation().isAsyncStarted()) {
                this._parser.returnBuffers();
                this._generator.returnBuffers();
                this._asyncEndp.setCheckForIdle(true);
            }
            if (some_progress) {
                this._total_no_progress = 0;
            } else {
                this._total_no_progress++;
                if (NO_PROGRESS_INFO > 0 && this._total_no_progress % NO_PROGRESS_INFO == 0 && (NO_PROGRESS_CLOSE <= 0 || this._total_no_progress < NO_PROGRESS_CLOSE)) {
                    LOG.info("EndPoint making no progress: " + this._total_no_progress + " " + this._endp + " " + this, new Object[0]);
                }
                if (NO_PROGRESS_CLOSE > 0 && this._total_no_progress == NO_PROGRESS_CLOSE) {
                    LOG.warn("Closing EndPoint making no progress: " + this._total_no_progress + " " + this._endp + " " + this, new Object[0]);
                    if (this._endp instanceof SelectChannelEndPoint) {
                        ((SelectChannelEndPoint) this._endp).getChannel().close();
                    }
                }
            }
            throw th;
        }
    }

    @Override // org.eclipse.jetty.io.nio.AsyncConnection
    public void onInputShutdown() throws IOException {
        if (this._generator.isIdle() && !this._request.getAsyncContinuation().isSuspended()) {
            this._endp.close();
        }
        if (this._parser.isIdle()) {
            this._parser.setPersistent(false);
        }
    }

    @Override // org.eclipse.jetty.server.AbstractHttpConnection
    public void reset() {
        this._readInterested = true;
        LOG.debug("Enabled read interest {}", this._endp);
        super.reset();
    }

    @Override // org.eclipse.jetty.server.AbstractHttpConnection, org.eclipse.jetty.io.Connection
    public boolean isSuspended() {
        return !this._readInterested || super.isSuspended();
    }
}
