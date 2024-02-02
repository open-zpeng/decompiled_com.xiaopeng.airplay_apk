package org.eclipse.jetty.client;

import java.io.IOException;
import java.net.Socket;
import javax.net.SocketFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.bio.SocketEndPoint;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
class SocketConnector extends AbstractLifeCycle implements HttpClient.Connector {
    private static final Logger LOG = Log.getLogger(SocketConnector.class);
    private final HttpClient _httpClient;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SocketConnector(HttpClient httpClient) {
        this._httpClient = httpClient;
    }

    @Override // org.eclipse.jetty.client.HttpClient.Connector
    public void startConnection(final HttpDestination destination) throws IOException {
        Socket socket = destination.isSecure() ? destination.getSslContextFactory().newSslSocket() : SocketFactory.getDefault().createSocket();
        socket.setSoTimeout(0);
        socket.setTcpNoDelay(true);
        Address address = destination.isProxied() ? destination.getProxy() : destination.getAddress();
        socket.connect(address.toSocketAddress(), this._httpClient.getConnectTimeout());
        EndPoint endpoint = new SocketEndPoint(socket);
        final AbstractHttpConnection connection = new BlockingHttpConnection(this._httpClient.getRequestBuffers(), this._httpClient.getResponseBuffers(), endpoint);
        connection.setDestination(destination);
        destination.onNewConnection(connection);
        this._httpClient.getThreadPool().dispatch(new Runnable() { // from class: org.eclipse.jetty.client.SocketConnector.1
            /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
                jadx.core.utils.exceptions.JadxRuntimeException: Can't find top splitter block for handler:B:9:0x0013
                	at jadx.core.utils.BlockUtils.getTopSplitterForHandler(BlockUtils.java:1234)
                	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:1018)
                	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:55)
                */
            /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:10:0x0014 -> B:20:0x003f). Please submit an issue!!! */
            @Override // java.lang.Runnable
            public void run() {
                /*
                    r4 = this;
                    r0 = 1
                    org.eclipse.jetty.client.AbstractHttpConnection r1 = r2     // Catch: java.lang.Throwable -> L1c java.io.IOException -> L1e
                L3:
                    org.eclipse.jetty.io.Connection r2 = r1.handle()     // Catch: java.lang.Throwable -> L1c java.io.IOException -> L1e
                    if (r2 == r1) goto Lb
                    r1 = r2
                    goto L3
                Lb:
                    org.eclipse.jetty.client.HttpDestination r1 = r3     // Catch: java.io.IOException -> L13
                    org.eclipse.jetty.client.AbstractHttpConnection r2 = r2     // Catch: java.io.IOException -> L13
                    r1.returnConnection(r2, r0)     // Catch: java.io.IOException -> L13
                L12:
                    goto L3f
                L13:
                    r0 = move-exception
                    org.eclipse.jetty.util.log.Logger r1 = org.eclipse.jetty.client.SocketConnector.access$000()
                    r1.debug(r0)
                    goto L3f
                L1c:
                    r1 = move-exception
                    goto L40
                L1e:
                    r1 = move-exception
                    boolean r2 = r1 instanceof java.io.InterruptedIOException     // Catch: java.lang.Throwable -> L1c
                    if (r2 == 0) goto L2b
                    org.eclipse.jetty.util.log.Logger r2 = org.eclipse.jetty.client.SocketConnector.access$000()     // Catch: java.lang.Throwable -> L1c
                    r2.ignore(r1)     // Catch: java.lang.Throwable -> L1c
                    goto L37
                L2b:
                    org.eclipse.jetty.util.log.Logger r2 = org.eclipse.jetty.client.SocketConnector.access$000()     // Catch: java.lang.Throwable -> L1c
                    r2.debug(r1)     // Catch: java.lang.Throwable -> L1c
                    org.eclipse.jetty.client.HttpDestination r2 = r3     // Catch: java.lang.Throwable -> L1c
                    r2.onException(r1)     // Catch: java.lang.Throwable -> L1c
                L37:
                    org.eclipse.jetty.client.HttpDestination r1 = r3     // Catch: java.io.IOException -> L13
                    org.eclipse.jetty.client.AbstractHttpConnection r2 = r2     // Catch: java.io.IOException -> L13
                    r1.returnConnection(r2, r0)     // Catch: java.io.IOException -> L13
                    goto L12
                L3f:
                    return
                L40:
                    org.eclipse.jetty.client.HttpDestination r2 = r3     // Catch: java.io.IOException -> L49
                    org.eclipse.jetty.client.AbstractHttpConnection r3 = r2     // Catch: java.io.IOException -> L49
                    r2.returnConnection(r3, r0)     // Catch: java.io.IOException -> L49
                    goto L51
                L49:
                    r0 = move-exception
                    org.eclipse.jetty.util.log.Logger r2 = org.eclipse.jetty.client.SocketConnector.access$000()
                    r2.debug(r0)
                L51:
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.client.SocketConnector.AnonymousClass1.run():void");
            }
        });
    }
}
