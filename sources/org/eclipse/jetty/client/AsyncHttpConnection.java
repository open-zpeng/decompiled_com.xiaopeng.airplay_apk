package org.eclipse.jetty.client;

import java.io.IOException;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AsyncHttpConnection extends AbstractHttpConnection implements AsyncConnection {
    private static final Logger LOG = Log.getLogger(AsyncHttpConnection.class);
    private final AsyncEndPoint _asyncEndp;
    private boolean _requestComplete;
    private Buffer _requestContentChunk;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AsyncHttpConnection(Buffers requestBuffers, Buffers responseBuffers, EndPoint endp) {
        super(requestBuffers, responseBuffers, endp);
        this._asyncEndp = (AsyncEndPoint) endp;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.AbstractHttpConnection
    public void reset() throws IOException {
        this._requestComplete = false;
        super.reset();
    }

    /* JADX WARN: Removed duplicated region for block: B:263:0x016b A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:265:0x0006 A[SYNTHETIC] */
    @Override // org.eclipse.jetty.client.AbstractHttpConnection, org.eclipse.jetty.io.Connection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public org.eclipse.jetty.io.Connection handle() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 1040
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.client.AsyncHttpConnection.handle():org.eclipse.jetty.io.Connection");
    }

    @Override // org.eclipse.jetty.io.nio.AsyncConnection
    public void onInputShutdown() throws IOException {
        if (this._generator.isIdle()) {
            this._endp.shutdownOutput();
        }
    }

    @Override // org.eclipse.jetty.client.AbstractHttpConnection
    public boolean send(HttpExchange ex) throws IOException {
        boolean sent = super.send(ex);
        if (sent) {
            this._asyncEndp.asyncDispatch();
        }
        return sent;
    }
}
