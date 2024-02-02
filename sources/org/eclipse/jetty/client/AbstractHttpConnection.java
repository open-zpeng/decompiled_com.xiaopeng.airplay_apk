package org.eclipse.jetty.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.client.security.Authentication;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.View;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public abstract class AbstractHttpConnection extends AbstractConnection implements Dumpable {
    private static final Logger LOG = Log.getLogger(AbstractHttpConnection.class);
    protected Buffer _connectionHeader;
    protected HttpDestination _destination;
    protected volatile HttpExchange _exchange;
    protected HttpGenerator _generator;
    protected boolean _http11;
    private AtomicBoolean _idle;
    private final Timeout.Task _idleTimeout;
    protected HttpParser _parser;
    protected HttpExchange _pipeline;
    protected boolean _reserved;
    protected int _status;

    @Override // org.eclipse.jetty.io.Connection
    public abstract Connection handle() throws IOException;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AbstractHttpConnection(Buffers requestBuffers, Buffers responseBuffers, EndPoint endp) {
        super(endp);
        this._http11 = true;
        this._idleTimeout = new ConnectionIdleTask();
        this._idle = new AtomicBoolean(false);
        this._generator = new HttpGenerator(requestBuffers, endp);
        this._parser = new HttpParser(responseBuffers, endp, new Handler());
    }

    public void setReserved(boolean reserved) {
        this._reserved = reserved;
    }

    public boolean isReserved() {
        return this._reserved;
    }

    public HttpDestination getDestination() {
        return this._destination;
    }

    public void setDestination(HttpDestination destination) {
        this._destination = destination;
    }

    public boolean send(HttpExchange ex) throws IOException {
        LOG.debug("Send {} on {}", ex, this);
        synchronized (this) {
            if (this._exchange != null) {
                if (this._pipeline != null) {
                    throw new IllegalStateException(this + " PIPELINED!!!  _exchange=" + this._exchange);
                }
                this._pipeline = ex;
                return true;
            }
            this._exchange = ex;
            this._exchange.associate(this);
            if (!this._endp.isOpen()) {
                this._exchange.disassociate();
                this._exchange = null;
                return false;
            }
            this._exchange.setStatus(2);
            adjustIdleTimeout();
            return true;
        }
    }

    private void adjustIdleTimeout() throws IOException {
        long timeout = this._exchange.getTimeout();
        if (timeout <= 0) {
            timeout = this._destination.getHttpClient().getTimeout();
        }
        long endPointTimeout = this._endp.getMaxIdleTime();
        if (timeout > 0 && timeout > endPointTimeout) {
            this._endp.setMaxIdleTime(2 * ((int) timeout));
        }
    }

    @Override // org.eclipse.jetty.io.Connection
    public boolean isIdle() {
        boolean z;
        synchronized (this) {
            z = this._exchange == null;
        }
        return z;
    }

    @Override // org.eclipse.jetty.io.Connection
    public boolean isSuspended() {
        return false;
    }

    @Override // org.eclipse.jetty.io.Connection
    public void onClose() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void commitRequest() throws IOException {
        synchronized (this) {
            this._status = 0;
            if (this._exchange.getStatus() != 2) {
                throw new IllegalStateException();
            }
            this._exchange.setStatus(3);
            this._generator.setVersion(this._exchange.getVersion());
            String method = this._exchange.getMethod();
            String uri = this._exchange.getRequestURI();
            if (this._destination.isProxied()) {
                if (!HttpMethods.CONNECT.equals(method) && uri.startsWith("/")) {
                    boolean secure = this._destination.isSecure();
                    String host = this._destination.getAddress().getHost();
                    int port = this._destination.getAddress().getPort();
                    StringBuilder absoluteURI = new StringBuilder();
                    absoluteURI.append(secure ? "https" : "http");
                    absoluteURI.append("://");
                    absoluteURI.append(host);
                    if ((!secure || port != 443) && (secure || port != 80)) {
                        absoluteURI.append(":");
                        absoluteURI.append(port);
                    }
                    absoluteURI.append(uri);
                    uri = absoluteURI.toString();
                }
                Authentication auth = this._destination.getProxyAuthentication();
                if (auth != null) {
                    auth.setCredentials(this._exchange);
                }
            }
            this._generator.setRequest(method, uri);
            this._parser.setHeadResponse(HttpMethods.HEAD.equalsIgnoreCase(method));
            HttpFields requestHeaders = this._exchange.getRequestFields();
            if (this._exchange.getVersion() >= 11 && !requestHeaders.containsKey(HttpHeaders.HOST_BUFFER)) {
                requestHeaders.add(HttpHeaders.HOST_BUFFER, this._destination.getHostHeader());
            }
            Buffer requestContent = this._exchange.getRequestContent();
            if (requestContent != null) {
                requestHeaders.putLongField(HttpHeaders.CONTENT_LENGTH, requestContent.length());
                this._generator.completeHeader(requestHeaders, false);
                this._generator.addContent(new View(requestContent), true);
                this._exchange.setStatus(4);
            } else {
                InputStream requestContentStream = this._exchange.getRequestContentSource();
                if (requestContentStream != null) {
                    this._generator.completeHeader(requestHeaders, false);
                } else {
                    requestHeaders.remove(HttpHeaders.CONTENT_LENGTH);
                    this._generator.completeHeader(requestHeaders, true);
                    this._exchange.setStatus(4);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reset() throws IOException {
        this._connectionHeader = null;
        this._parser.reset();
        this._generator.reset();
        this._http11 = true;
    }

    /* loaded from: classes.dex */
    private class Handler extends HttpParser.EventHandler {
        private Handler() {
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void startRequest(Buffer method, Buffer url, Buffer version) throws IOException {
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void startResponse(Buffer version, int status, Buffer reason) throws IOException {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange == null) {
                AbstractHttpConnection.LOG.warn("No exchange for response", new Object[0]);
                AbstractHttpConnection.this._endp.close();
                return;
            }
            if (status == 100 || status == 102) {
                exchange.setEventListener(new NonFinalResponseListener(exchange));
            } else if (status == 200 && HttpMethods.CONNECT.equalsIgnoreCase(exchange.getMethod())) {
                AbstractHttpConnection.this._parser.setHeadResponse(true);
            }
            AbstractHttpConnection.this._http11 = HttpVersions.HTTP_1_1_BUFFER.equals(version);
            AbstractHttpConnection.this._status = status;
            exchange.getEventListener().onResponseStatus(version, status, reason);
            exchange.setStatus(5);
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void parsedHeader(Buffer name, Buffer value) throws IOException {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange != null) {
                if (HttpHeaders.CACHE.getOrdinal(name) == 1) {
                    AbstractHttpConnection.this._connectionHeader = HttpHeaderValues.CACHE.lookup(value);
                }
                exchange.getEventListener().onResponseHeader(name, value);
            }
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void headerComplete() throws IOException {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange != null) {
                exchange.setStatus(6);
                if (HttpMethods.CONNECT.equalsIgnoreCase(exchange.getMethod())) {
                    AbstractHttpConnection.this._parser.setPersistent(true);
                }
            }
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void content(Buffer ref) throws IOException {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange != null) {
                exchange.getEventListener().onResponseContent(ref);
            }
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void messageComplete(long contextLength) throws IOException {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange != null) {
                exchange.setStatus(7);
            }
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void earlyEOF() {
            HttpExchange exchange = AbstractHttpConnection.this._exchange;
            if (exchange != null && !exchange.isDone() && exchange.setStatus(9)) {
                exchange.getEventListener().onException(new EofException("early EOF"));
            }
        }
    }

    @Override // org.eclipse.jetty.io.AbstractConnection
    public String toString() {
        Object[] objArr = new Object[4];
        objArr[0] = super.toString();
        objArr[1] = this._destination == null ? "?.?.?.?:??" : this._destination.getAddress();
        objArr[2] = this._generator;
        objArr[3] = this._parser;
        return String.format("%s %s g=%s p=%s", objArr);
    }

    public String toDetailString() {
        return toString() + " ex=" + this._exchange + " idle for " + this._idleTimeout.getAge();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0022, code lost:
        if (r7._parser.isState(1) != false) goto L21;
     */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0031  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x003f  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0049  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void close() throws java.io.IOException {
        /*
            r7 = this;
            org.eclipse.jetty.client.HttpExchange r0 = r7._exchange
            r1 = 1
            if (r0 == 0) goto L64
            boolean r2 = r0.isDone()
            if (r2 != 0) goto L64
            int r2 = r0.getStatus()
            switch(r2) {
                case 6: goto L14;
                case 7: goto L13;
                case 8: goto L13;
                case 9: goto L13;
                case 10: goto L13;
                case 11: goto L13;
                default: goto L12;
            }
        L12:
            goto L25
        L13:
            goto L64
        L14:
            org.eclipse.jetty.io.EndPoint r2 = r7._endp
            boolean r2 = r2.isInputShutdown()
            if (r2 == 0) goto L25
            org.eclipse.jetty.http.HttpParser r2 = r7._parser
            boolean r2 = r2.isState(r1)
            if (r2 == 0) goto L25
            goto L64
        L25:
            java.lang.String r2 = r0.toString()
            org.eclipse.jetty.io.EndPoint r3 = r7._endp
            boolean r3 = r3.isOpen()
            if (r3 == 0) goto L3f
            org.eclipse.jetty.io.EndPoint r3 = r7._endp
            boolean r3 = r3.isInputShutdown()
            if (r3 == 0) goto L3c
            java.lang.String r3 = "half closed: "
            goto L41
        L3c:
            java.lang.String r3 = "local close: "
            goto L41
        L3f:
            java.lang.String r3 = "closed: "
        L41:
            r4 = 9
            boolean r4 = r0.setStatus(r4)
            if (r4 == 0) goto L64
            org.eclipse.jetty.client.HttpEventListener r4 = r0.getEventListener()
            org.eclipse.jetty.io.EofException r5 = new org.eclipse.jetty.io.EofException
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r3)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            r5.<init>(r6)
            r4.onException(r5)
        L64:
            org.eclipse.jetty.io.EndPoint r2 = r7._endp
            boolean r2 = r2.isOpen()
            if (r2 == 0) goto L76
            org.eclipse.jetty.io.EndPoint r2 = r7._endp
            r2.close()
            org.eclipse.jetty.client.HttpDestination r2 = r7._destination
            r2.returnConnection(r7, r1)
        L76:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.client.AbstractHttpConnection.close():void");
    }

    public void setIdleTimeout() {
        synchronized (this) {
            if (this._idle.compareAndSet(false, true)) {
                this._destination.getHttpClient().scheduleIdle(this._idleTimeout);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public boolean cancelIdleTimeout() {
        synchronized (this) {
            if (this._idle.compareAndSet(true, false)) {
                this._destination.getHttpClient().cancel(this._idleTimeout);
                return true;
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void exchangeExpired(HttpExchange exchange) {
        synchronized (this) {
            if (this._exchange == exchange) {
                try {
                    this._destination.returnConnection(this, true);
                } catch (IOException x) {
                    LOG.ignore(x);
                }
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        return AggregateLifeCycle.dump(this);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        synchronized (this) {
            out.append(String.valueOf(this)).append("\n");
            AggregateLifeCycle.dump(out, indent, Collections.singletonList(this._endp));
        }
    }

    /* loaded from: classes.dex */
    private class ConnectionIdleTask extends Timeout.Task {
        private ConnectionIdleTask() {
        }

        @Override // org.eclipse.jetty.util.thread.Timeout.Task
        public void expired() {
            if (AbstractHttpConnection.this._idle.compareAndSet(true, false)) {
                AbstractHttpConnection.this._destination.returnIdleConnection(AbstractHttpConnection.this);
            }
        }
    }

    /* loaded from: classes.dex */
    private class NonFinalResponseListener implements HttpEventListener {
        final HttpExchange _exchange;
        final HttpEventListener _next;

        public NonFinalResponseListener(HttpExchange exchange) {
            this._exchange = exchange;
            this._next = exchange.getEventListener();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRequestCommitted() throws IOException {
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRequestComplete() throws IOException {
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseHeader(Buffer name, Buffer value) throws IOException {
            this._next.onResponseHeader(name, value);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseHeaderComplete() throws IOException {
            this._next.onResponseHeaderComplete();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseContent(Buffer content) throws IOException {
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseComplete() throws IOException {
            this._exchange.setEventListener(this._next);
            this._exchange.setStatus(4);
            AbstractHttpConnection.this._parser.reset();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onConnectionFailed(Throwable ex) {
            this._exchange.setEventListener(this._next);
            this._next.onConnectionFailed(ex);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onException(Throwable ex) {
            this._exchange.setEventListener(this._next);
            this._next.onException(ex);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onExpire() {
            this._exchange.setEventListener(this._next);
            this._next.onExpire();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRetry() {
            this._exchange.setEventListener(this._next);
            this._next.onRetry();
        }
    }
}
