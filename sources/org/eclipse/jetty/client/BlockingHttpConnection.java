package org.eclipse.jetty.client;

import java.io.IOException;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class BlockingHttpConnection extends AbstractHttpConnection {
    private static final Logger LOG = Log.getLogger(BlockingHttpConnection.class);
    private boolean _expired;
    private boolean _requestComplete;
    private Buffer _requestContentChunk;

    /* JADX INFO: Access modifiers changed from: package-private */
    public BlockingHttpConnection(Buffers requestBuffers, Buffers responseBuffers, EndPoint endPoint) {
        super(requestBuffers, responseBuffers, endPoint);
        this._expired = false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.AbstractHttpConnection
    public void reset() throws IOException {
        this._requestComplete = false;
        this._expired = false;
        super.reset();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.AbstractHttpConnection
    public void exchangeExpired(HttpExchange exchange) {
        synchronized (this) {
            super.exchangeExpired(exchange);
            this._expired = true;
            notifyAll();
        }
    }

    @Override // org.eclipse.jetty.io.AbstractConnection, org.eclipse.jetty.io.Connection
    public void onIdleExpired(long idleForMs) {
        try {
            LOG.debug("onIdleExpired {}ms {} {}", Long.valueOf(idleForMs), this, this._endp);
            this._expired = true;
            this._endp.close();
        } catch (IOException e) {
            LOG.ignore(e);
            try {
                this._endp.close();
            } catch (IOException e2) {
                LOG.ignore(e2);
            }
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x0049, code lost:
        throw new java.lang.InterruptedException();
     */
    /* JADX WARN: Removed duplicated region for block: B:289:0x0168 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:291:0x0006 A[SYNTHETIC] */
    @Override // org.eclipse.jetty.client.AbstractHttpConnection, org.eclipse.jetty.io.Connection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public org.eclipse.jetty.io.Connection handle() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 1027
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.client.BlockingHttpConnection.handle():org.eclipse.jetty.io.Connection");
    }

    @Override // org.eclipse.jetty.client.AbstractHttpConnection
    public boolean send(HttpExchange ex) throws IOException {
        boolean sent = super.send(ex);
        if (sent) {
            synchronized (this) {
                notifyAll();
            }
        }
        return sent;
    }
}
