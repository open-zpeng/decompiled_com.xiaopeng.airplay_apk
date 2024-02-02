package sun.net.httpserver;

import com.apple.dnssd.DNSSD;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpTokens;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ExchangeImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    Map<String, Object> attributes;
    boolean close;
    boolean closed;
    HttpConnection connection;
    String method;
    HttpPrincipal principal;
    Request req;
    int reqContentLen;
    Headers reqHdrs;
    InputStream ris;
    OutputStream ros;
    long rspContentLen;
    boolean sentHeaders;
    Thread thread;
    InputStream uis;
    LeftOverInputStream uis_orig;
    OutputStream uos;
    PlaceholderOutputStream uos_orig;
    URI uri;
    static TimeZone tz = TimeZone.getTimeZone("GMT");
    static DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    boolean http10 = false;
    int rcode = -1;
    private byte[] rspbuf = new byte[DNSSD.REGISTRATION_DOMAINS];
    Headers rspHdrs = new Headers();
    ServerImpl server = getServerImpl();

    static {
        df.setTimeZone(tz);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ExchangeImpl(String str, URI uri, Request request, int i, HttpConnection httpConnection) throws IOException {
        this.req = request;
        this.reqHdrs = request.headers();
        this.method = str;
        this.uri = uri;
        this.connection = httpConnection;
        this.reqContentLen = i;
        this.ros = request.outputStream();
        this.ris = request.inputStream();
        this.server.startExchange();
    }

    public Headers getRequestHeaders() {
        return new UnmodifiableHeaders(this.reqHdrs);
    }

    public Headers getResponseHeaders() {
        return this.rspHdrs;
    }

    public URI getRequestURI() {
        return this.uri;
    }

    public String getRequestMethod() {
        return this.method;
    }

    public HttpContextImpl getHttpContext() {
        return this.connection.getHttpContext();
    }

    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            if (this.uis_orig != null && this.uos != null) {
                if (!this.uos_orig.isWrapped()) {
                    this.connection.close();
                    return;
                }
                if (!this.uis_orig.isClosed()) {
                    this.uis_orig.close();
                }
                this.uos.close();
                return;
            }
            this.connection.close();
        } catch (IOException e) {
            this.connection.close();
        }
    }

    public InputStream getRequestBody() {
        if (this.uis != null) {
            return this.uis;
        }
        if (this.reqContentLen == -1) {
            this.uis_orig = new ChunkedInputStream(this, this.ris);
            this.uis = this.uis_orig;
        } else {
            this.uis_orig = new FixedLengthInputStream(this, this.ris, this.reqContentLen);
            this.uis = this.uis_orig;
        }
        return this.uis;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LeftOverInputStream getOriginalInputStream() {
        return this.uis_orig;
    }

    public int getResponseCode() {
        return this.rcode;
    }

    public OutputStream getResponseBody() {
        if (this.uos == null) {
            this.uos_orig = new PlaceholderOutputStream(null);
            this.uos = this.uos_orig;
        }
        return this.uos;
    }

    PlaceholderOutputStream getPlaceholderResponseBody() {
        getResponseBody();
        return this.uos_orig;
    }

    public void sendResponseHeaders(int i, long j) throws IOException {
        if (this.sentHeaders) {
            throw new IOException("headers already sent");
        }
        this.rcode = i;
        String str = "HTTP/1.1 " + i + Code.msg(i) + "\r\n";
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(this.ros);
        PlaceholderOutputStream placeholderResponseBody = getPlaceholderResponseBody();
        boolean z = false;
        bufferedOutputStream.write(bytes(str, 0), 0, str.length());
        this.rspHdrs.set(HttpHeaders.DATE, df.format(new Date()));
        if (j == 0) {
            if (this.http10) {
                placeholderResponseBody.setWrappedStream(new UndefLengthOutputStream(this, this.ros));
                this.close = true;
            } else {
                this.rspHdrs.set("Transfer-encoding", HttpHeaderValues.CHUNKED);
                placeholderResponseBody.setWrappedStream(new ChunkedOutputStream(this, this.ros));
            }
        } else {
            if (j == -1) {
                j = 0;
                z = true;
            }
            if (this.rspHdrs.getFirst("Content-length") == null) {
                this.rspHdrs.set("Content-length", Long.toString(j));
            }
            placeholderResponseBody.setWrappedStream(new FixedLengthOutputStream(this, this.ros, j));
        }
        write(this.rspHdrs, bufferedOutputStream);
        this.rspContentLen = j;
        bufferedOutputStream.flush();
        this.sentHeaders = true;
        if (z) {
            this.server.addEvent(new WriteFinishedEvent(this));
            this.closed = true;
        }
        this.server.logReply(i, this.req.requestLine(), null);
    }

    void write(Headers headers, OutputStream outputStream) throws IOException {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            for (String str : entry.getValue()) {
                int length = key.length();
                byte[] bytes = bytes(key, 2);
                int i = length + 1;
                bytes[length] = HttpTokens.COLON;
                bytes[i] = HttpTokens.SPACE;
                outputStream.write(bytes, 0, i + 1);
                byte[] bytes2 = bytes(str, 2);
                int length2 = str.length();
                int i2 = length2 + 1;
                bytes2[length2] = HttpTokens.CARRIAGE_RETURN;
                bytes2[i2] = 10;
                outputStream.write(bytes2, 0, i2 + 1);
            }
        }
        outputStream.write(13);
        outputStream.write(10);
    }

    private byte[] bytes(String str, int i) {
        int length = str.length() + i;
        if (length > this.rspbuf.length) {
            this.rspbuf = new byte[2 * (this.rspbuf.length + (length - this.rspbuf.length))];
        }
        char[] charArray = str.toCharArray();
        for (int i2 = 0; i2 < charArray.length; i2++) {
            this.rspbuf[i2] = (byte) charArray[i2];
        }
        return this.rspbuf;
    }

    public InetSocketAddress getRemoteAddress() {
        Socket socket = this.connection.getChannel().socket();
        return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
    }

    public InetSocketAddress getLocalAddress() {
        Socket socket = this.connection.getChannel().socket();
        return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
    }

    public String getProtocol() {
        String requestLine = this.req.requestLine();
        return requestLine.substring(requestLine.lastIndexOf(32) + 1);
    }

    public SSLSession getSSLSession() {
        SSLEngine sSLEngine = this.connection.getSSLEngine();
        if (sSLEngine == null) {
            return null;
        }
        return sSLEngine.getSession();
    }

    public Object getAttribute(String str) {
        if (str == null) {
            throw new NullPointerException("null name parameter");
        }
        if (this.attributes == null) {
            this.attributes = getHttpContext().getAttributes();
        }
        return this.attributes.get(str);
    }

    public void setAttribute(String str, Object obj) {
        if (str == null) {
            throw new NullPointerException("null name parameter");
        }
        if (this.attributes == null) {
            this.attributes = getHttpContext().getAttributes();
        }
        this.attributes.put(str, obj);
    }

    public void setStreams(InputStream inputStream, OutputStream outputStream) {
        if (inputStream != null) {
            this.uis = inputStream;
        }
        if (outputStream != null) {
            this.uos = outputStream;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpConnection getConnection() {
        return this.connection;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ServerImpl getServerImpl() {
        return getHttpContext().getServerImpl();
    }

    public HttpPrincipal getPrincipal() {
        return this.principal;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPrincipal(HttpPrincipal httpPrincipal) {
        this.principal = httpPrincipal;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ExchangeImpl get(HttpExchange httpExchange) {
        if (httpExchange instanceof HttpExchangeImpl) {
            return ((HttpExchangeImpl) httpExchange).getExchangeImpl();
        }
        return ((HttpsExchangeImpl) httpExchange).getExchangeImpl();
    }
}
