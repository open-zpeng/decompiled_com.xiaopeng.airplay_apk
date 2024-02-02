package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class HttpConnection {
    SocketChannel chan;
    boolean closed = false;
    HttpContextImpl context;
    SSLEngine engine;
    InputStream i;
    Logger logger;
    String protocol;
    InputStream raw;
    OutputStream rawout;
    int remaining;
    SelectionKey selectionKey;
    SSLContext sslContext;
    SSLStreams sslStreams;
    long time;

    public String toString() {
        if (this.chan != null) {
            return this.chan.toString();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setChannel(SocketChannel socketChannel) {
        this.chan = socketChannel;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setContext(HttpContextImpl httpContextImpl) {
        this.context = httpContextImpl;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setParameters(InputStream inputStream, OutputStream outputStream, SocketChannel socketChannel, SSLEngine sSLEngine, SSLStreams sSLStreams, SSLContext sSLContext, String str, HttpContextImpl httpContextImpl, InputStream inputStream2) {
        this.context = httpContextImpl;
        this.i = inputStream;
        this.rawout = outputStream;
        this.raw = inputStream2;
        this.protocol = str;
        this.engine = sSLEngine;
        this.chan = socketChannel;
        this.sslContext = sSLContext;
        this.sslStreams = sSLStreams;
        this.logger = httpContextImpl.getLogger();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SocketChannel getChannel() {
        return this.chan;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.logger != null && this.chan != null) {
            Logger logger = this.logger;
            logger.finest("Closing connection: " + this.chan.toString());
        }
        if (!this.chan.isOpen()) {
            ServerImpl.dprint("Channel already closed");
            return;
        }
        try {
            if (this.raw != null) {
                this.raw.close();
            }
        } catch (IOException e) {
            ServerImpl.dprint(e);
        }
        try {
            if (this.rawout != null) {
                this.rawout.close();
            }
        } catch (IOException e2) {
            ServerImpl.dprint(e2);
        }
        try {
            if (this.sslStreams != null) {
                this.sslStreams.close();
            }
        } catch (IOException e3) {
            ServerImpl.dprint(e3);
        }
        try {
            this.chan.close();
        } catch (IOException e4) {
            ServerImpl.dprint(e4);
        }
    }

    void setRemaining(int i) {
        this.remaining = i;
    }

    int getRemaining() {
        return this.remaining;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SelectionKey getSelectionKey() {
        return this.selectionKey;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public InputStream getInputStream() {
        return this.i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public OutputStream getRawOutputStream() {
        return this.rawout;
    }

    String getProtocol() {
        return this.protocol;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SSLEngine getSSLEngine() {
        return this.engine;
    }

    SSLContext getSSLContext() {
        return this.sslContext;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpContextImpl getHttpContext() {
        return this.context;
    }
}
