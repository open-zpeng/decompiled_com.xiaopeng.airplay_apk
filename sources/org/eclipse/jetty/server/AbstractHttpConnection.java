package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import org.eclipse.jetty.http.EncodedHttpURI;
import org.eclipse.jetty.http.Generator;
import org.eclipse.jetty.http.HttpBuffers;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.http.Parser;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.UncheckedPrintWriter;
import org.eclipse.jetty.server.nio.NIOConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public abstract class AbstractHttpConnection extends AbstractConnection {
    private static final int UNKNOWN = -2;
    private Object _associatedObject;
    private String _charset;
    protected final Connector _connector;
    private boolean _delayedHandling;
    private boolean _earlyEOF;
    private boolean _expect;
    private boolean _expect100Continue;
    private boolean _expect102Processing;
    protected final Generator _generator;
    private boolean _head;
    private boolean _host;
    protected volatile ServletInputStream _in;
    int _include;
    protected volatile Output _out;
    protected final Parser _parser;
    protected volatile PrintWriter _printWriter;
    protected final Request _request;
    protected final HttpFields _requestFields;
    private int _requests;
    protected final Response _response;
    protected final HttpFields _responseFields;
    protected final Server _server;
    protected final HttpURI _uri;
    private int _version;
    protected volatile OutputWriter _writer;
    private static final Logger LOG = Log.getLogger(AbstractHttpConnection.class);
    private static final ThreadLocal<AbstractHttpConnection> __currentConnection = new ThreadLocal<>();

    @Override // org.eclipse.jetty.io.Connection
    public abstract Connection handle() throws IOException;

    public static AbstractHttpConnection getCurrentConnection() {
        return __currentConnection.get();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static void setCurrentConnection(AbstractHttpConnection connection) {
        __currentConnection.set(connection);
    }

    public AbstractHttpConnection(Connector connector, EndPoint endpoint, Server server) {
        super(endpoint);
        this._version = -2;
        this._expect = false;
        this._expect100Continue = false;
        this._expect102Processing = false;
        this._head = false;
        this._host = false;
        this._delayedHandling = false;
        this._earlyEOF = false;
        this._uri = StringUtil.__UTF8.equals(URIUtil.__CHARSET) ? new HttpURI() : new EncodedHttpURI(URIUtil.__CHARSET);
        this._connector = connector;
        HttpBuffers ab = (HttpBuffers) this._connector;
        this._parser = newHttpParser(ab.getRequestBuffers(), endpoint, new RequestHandler());
        this._requestFields = new HttpFields();
        this._responseFields = new HttpFields();
        this._request = new Request(this);
        this._response = new Response(this);
        this._generator = newHttpGenerator(ab.getResponseBuffers(), endpoint);
        this._generator.setSendServerVersion(server.getSendServerVersion());
        this._server = server;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractHttpConnection(Connector connector, EndPoint endpoint, Server server, Parser parser, Generator generator, Request request) {
        super(endpoint);
        this._version = -2;
        this._expect = false;
        this._expect100Continue = false;
        this._expect102Processing = false;
        this._head = false;
        this._host = false;
        this._delayedHandling = false;
        this._earlyEOF = false;
        this._uri = URIUtil.__CHARSET.equals(StringUtil.__UTF8) ? new HttpURI() : new EncodedHttpURI(URIUtil.__CHARSET);
        this._connector = connector;
        this._parser = parser;
        this._requestFields = new HttpFields();
        this._responseFields = new HttpFields();
        this._request = request;
        this._response = new Response(this);
        this._generator = generator;
        this._generator.setSendServerVersion(server.getSendServerVersion());
        this._server = server;
    }

    protected HttpParser newHttpParser(Buffers requestBuffers, EndPoint endpoint, HttpParser.EventHandler requestHandler) {
        return new HttpParser(requestBuffers, endpoint, requestHandler);
    }

    protected HttpGenerator newHttpGenerator(Buffers responseBuffers, EndPoint endPoint) {
        return new HttpGenerator(responseBuffers, endPoint);
    }

    public Parser getParser() {
        return this._parser;
    }

    public int getRequests() {
        return this._requests;
    }

    public Server getServer() {
        return this._server;
    }

    public Object getAssociatedObject() {
        return this._associatedObject;
    }

    public void setAssociatedObject(Object associatedObject) {
        this._associatedObject = associatedObject;
    }

    public Connector getConnector() {
        return this._connector;
    }

    public HttpFields getRequestFields() {
        return this._requestFields;
    }

    public HttpFields getResponseFields() {
        return this._responseFields;
    }

    public boolean isConfidential(Request request) {
        return this._connector != null && this._connector.isConfidential(request);
    }

    public boolean isIntegral(Request request) {
        return this._connector != null && this._connector.isIntegral(request);
    }

    public boolean getResolveNames() {
        return this._connector.getResolveNames();
    }

    public Request getRequest() {
        return this._request;
    }

    public Response getResponse() {
        return this._response;
    }

    public ServletInputStream getInputStream() throws IOException {
        if (this._expect100Continue) {
            if (((HttpParser) this._parser).getHeaderBuffer() == null || ((HttpParser) this._parser).getHeaderBuffer().length() < 2) {
                if (this._generator.isCommitted()) {
                    throw new IllegalStateException("Committed before 100 Continues");
                }
                ((HttpGenerator) this._generator).send1xx(100);
            }
            this._expect100Continue = false;
        }
        if (this._in == null) {
            this._in = new HttpInput(this);
        }
        return this._in;
    }

    public ServletOutputStream getOutputStream() {
        if (this._out == null) {
            this._out = new Output();
        }
        return this._out;
    }

    public PrintWriter getPrintWriter(String encoding) {
        getOutputStream();
        if (this._writer == null) {
            this._writer = new OutputWriter();
            if (this._server.isUncheckedPrintWriter()) {
                this._printWriter = new UncheckedPrintWriter(this._writer);
            } else {
                this._printWriter = new PrintWriter(this._writer) { // from class: org.eclipse.jetty.server.AbstractHttpConnection.1
                    @Override // java.io.PrintWriter, java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
                    public void close() {
                        synchronized (this.lock) {
                            try {
                                this.out.close();
                            } catch (IOException e) {
                                setError();
                            }
                        }
                    }
                };
            }
        }
        this._writer.setCharacterEncoding(encoding);
        return this._printWriter;
    }

    public boolean isResponseCommitted() {
        return this._generator.isCommitted();
    }

    public boolean isEarlyEOF() {
        return this._earlyEOF;
    }

    public void reset() {
        this._parser.reset();
        this._parser.returnBuffers();
        this._requestFields.clear();
        this._request.recycle();
        this._generator.reset();
        this._generator.returnBuffers();
        this._responseFields.clear();
        this._response.recycle();
        this._uri.clear();
        this._writer = null;
        this._earlyEOF = false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Code restructure failed: missing block: B:107:0x023d, code lost:
        if (r17._server != null) goto L129;
     */
    /* JADX WARN: Code restructure failed: missing block: B:125:0x027f, code lost:
        if (r17._server != null) goto L129;
     */
    /* JADX WARN: Code restructure failed: missing block: B:126:0x0281, code lost:
        r2 = true;
     */
    /* JADX WARN: Removed duplicated region for block: B:187:0x035b  */
    /* JADX WARN: Removed duplicated region for block: B:190:0x036c  */
    /* JADX WARN: Removed duplicated region for block: B:245:0x0059 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:258:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void handleRequest() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 1147
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.AbstractHttpConnection.handleRequest():void");
    }

    public void commitResponse(boolean last) throws IOException {
        if (!this._generator.isCommitted()) {
            this._generator.setResponse(this._response.getStatus(), this._response.getReason());
            try {
                if (this._expect100Continue && this._response.getStatus() != 100) {
                    this._generator.setPersistent(false);
                }
                this._generator.completeHeader(this._responseFields, last);
            } catch (RuntimeException e) {
                Logger logger = LOG;
                logger.warn("header full: " + e, new Object[0]);
                this._response.reset();
                this._generator.reset();
                this._generator.setResponse(500, null);
                this._generator.completeHeader(this._responseFields, true);
                this._generator.complete();
                throw new HttpException(500);
            }
        }
        if (last) {
            this._generator.complete();
        }
    }

    public void completeResponse() throws IOException {
        if (!this._generator.isCommitted()) {
            this._generator.setResponse(this._response.getStatus(), this._response.getReason());
            try {
                this._generator.completeHeader(this._responseFields, true);
            } catch (RuntimeException e) {
                Logger logger = LOG;
                logger.warn("header full: " + e, new Object[0]);
                LOG.debug(e);
                this._response.reset();
                this._generator.reset();
                this._generator.setResponse(500, null);
                this._generator.completeHeader(this._responseFields, true);
                this._generator.complete();
                throw new HttpException(500);
            }
        }
        this._generator.complete();
    }

    public void flushResponse() throws IOException {
        try {
            commitResponse(false);
            this._generator.flushBuffer();
        } catch (IOException e) {
            if (!(e instanceof EofException)) {
                throw new EofException(e);
            }
        }
    }

    public Generator getGenerator() {
        return this._generator;
    }

    public boolean isIncluding() {
        return this._include > 0;
    }

    public void include() {
        this._include++;
    }

    public void included() {
        this._include--;
        if (this._out != null) {
            this._out.reopen();
        }
    }

    @Override // org.eclipse.jetty.io.Connection
    public boolean isIdle() {
        return this._generator.isIdle() && (this._parser.isIdle() || this._delayedHandling);
    }

    @Override // org.eclipse.jetty.io.Connection
    public boolean isSuspended() {
        return this._request.getAsyncContinuation().isSuspended();
    }

    @Override // org.eclipse.jetty.io.Connection
    public void onClose() {
        LOG.debug("closed {}", this);
    }

    public boolean isExpecting100Continues() {
        return this._expect100Continue;
    }

    public boolean isExpecting102Processing() {
        return this._expect102Processing;
    }

    public int getMaxIdleTime() {
        if (this._connector.isLowResources() && this._endp.getMaxIdleTime() == this._connector.getMaxIdleTime()) {
            return this._connector.getLowResourceMaxIdleTime();
        }
        if (this._endp.getMaxIdleTime() > 0) {
            return this._endp.getMaxIdleTime();
        }
        return this._connector.getMaxIdleTime();
    }

    @Override // org.eclipse.jetty.io.AbstractConnection
    public String toString() {
        return String.format("%s,g=%s,p=%s,r=%d", super.toString(), this._generator, this._parser, Integer.valueOf(this._requests));
    }

    protected void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException {
        Buffer uri2 = uri.asImmutableBuffer();
        this._host = false;
        this._expect = false;
        this._expect100Continue = false;
        this._expect102Processing = false;
        this._delayedHandling = false;
        this._charset = null;
        if (this._request.getTimeStamp() == 0) {
            this._request.setTimeStamp(System.currentTimeMillis());
        }
        this._request.setMethod(method.toString());
        try {
            this._head = false;
            int ordinal = HttpMethods.CACHE.getOrdinal(method);
            if (ordinal == 3) {
                this._head = true;
                this._uri.parse(uri2.array(), uri2.getIndex(), uri2.length());
            } else if (ordinal == 8) {
                this._uri.parseConnect(uri2.array(), uri2.getIndex(), uri2.length());
            } else {
                this._uri.parse(uri2.array(), uri2.getIndex(), uri2.length());
            }
            this._request.setUri(this._uri);
            if (version == null) {
                this._request.setProtocol("");
                this._version = 9;
                return;
            }
            Buffer version2 = HttpVersions.CACHE.get(version);
            if (version2 == null) {
                throw new HttpException(400, null);
            }
            this._version = HttpVersions.CACHE.getOrdinal(version2);
            if (this._version <= 0) {
                this._version = 10;
            }
            this._request.setProtocol(version2.toString());
        } catch (Exception e) {
            LOG.debug(e);
            if (e instanceof HttpException) {
                throw ((HttpException) e);
            }
            throw new HttpException(400, null, e);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0019, code lost:
        if (r0 != 40) goto L11;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void parsedHeader(org.eclipse.jetty.io.Buffer r7, org.eclipse.jetty.io.Buffer r8) throws java.io.IOException {
        /*
            r6 = this;
            org.eclipse.jetty.http.HttpHeaders r0 = org.eclipse.jetty.http.HttpHeaders.CACHE
            int r0 = r0.getOrdinal(r7)
            r1 = 16
            if (r0 == r1) goto L88
            r1 = 21
            if (r0 == r1) goto L81
            r1 = 24
            r2 = 1
            if (r0 == r1) goto L21
            r1 = 27
            if (r0 == r1) goto L1d
            r1 = 40
            if (r0 == r1) goto L81
            goto L94
        L1d:
            r6._host = r2
            goto L94
        L21:
            int r1 = r6._version
            r3 = 11
            if (r1 < r3) goto L94
            org.eclipse.jetty.http.HttpHeaderValues r1 = org.eclipse.jetty.http.HttpHeaderValues.CACHE
            org.eclipse.jetty.io.Buffer r8 = r1.lookup(r8)
            org.eclipse.jetty.http.HttpHeaderValues r1 = org.eclipse.jetty.http.HttpHeaderValues.CACHE
            int r1 = r1.getOrdinal(r8)
            switch(r1) {
                case 6: goto L49;
                case 7: goto L42;
                default: goto L36;
            }
        L36:
            java.lang.String r1 = r8.toString()
            java.lang.String r3 = ","
            java.lang.String[] r1 = r1.split(r3)
            r3 = 0
            goto L50
        L42:
            org.eclipse.jetty.http.Generator r1 = r6._generator
            boolean r1 = r1 instanceof org.eclipse.jetty.http.HttpGenerator
            r6._expect102Processing = r1
            goto L94
        L49:
            org.eclipse.jetty.http.Generator r1 = r6._generator
            boolean r1 = r1 instanceof org.eclipse.jetty.http.HttpGenerator
            r6._expect100Continue = r1
            goto L94
        L50:
            if (r1 == 0) goto L94
            int r4 = r1.length
            if (r3 >= r4) goto L94
            org.eclipse.jetty.http.HttpHeaderValues r4 = org.eclipse.jetty.http.HttpHeaderValues.CACHE
            r5 = r1[r3]
            java.lang.String r5 = r5.trim()
            org.eclipse.jetty.io.BufferCache$CachedBuffer r4 = r4.get(r5)
            if (r4 != 0) goto L66
            r6._expect = r2
            goto L7e
        L66:
            int r5 = r4.getOrdinal()
            switch(r5) {
                case 6: goto L77;
                case 7: goto L70;
                default: goto L6d;
            }
        L6d:
            r6._expect = r2
            goto L7e
        L70:
            org.eclipse.jetty.http.Generator r5 = r6._generator
            boolean r5 = r5 instanceof org.eclipse.jetty.http.HttpGenerator
            r6._expect102Processing = r5
            goto L7e
        L77:
            org.eclipse.jetty.http.Generator r5 = r6._generator
            boolean r5 = r5 instanceof org.eclipse.jetty.http.HttpGenerator
            r6._expect100Continue = r5
        L7e:
            int r3 = r3 + 1
            goto L50
        L81:
            org.eclipse.jetty.http.HttpHeaderValues r1 = org.eclipse.jetty.http.HttpHeaderValues.CACHE
            org.eclipse.jetty.io.Buffer r8 = r1.lookup(r8)
            goto L94
        L88:
            org.eclipse.jetty.io.BufferCache r1 = org.eclipse.jetty.http.MimeTypes.CACHE
            org.eclipse.jetty.io.Buffer r8 = r1.lookup(r8)
            java.lang.String r1 = org.eclipse.jetty.http.MimeTypes.getCharsetFromContentType(r8)
            r6._charset = r1
        L94:
            org.eclipse.jetty.http.HttpFields r1 = r6._requestFields
            r1.add(r7, r8)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.AbstractHttpConnection.parsedHeader(org.eclipse.jetty.io.Buffer, org.eclipse.jetty.io.Buffer):void");
    }

    protected void headerComplete() throws IOException {
        if (this._endp.isOutputShutdown()) {
            this._endp.close();
            return;
        }
        this._requests++;
        this._generator.setVersion(this._version);
        switch (this._version) {
            case 10:
                this._generator.setHead(this._head);
                if (this._parser.isPersistent()) {
                    this._responseFields.add(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE_BUFFER);
                    this._generator.setPersistent(true);
                } else if (HttpMethods.CONNECT.equals(this._request.getMethod())) {
                    this._generator.setPersistent(true);
                    this._parser.setPersistent(true);
                    if (this._parser instanceof HttpParser) {
                        ((HttpParser) this._parser).setState(0);
                    }
                }
                if (this._server.getSendDateHeader()) {
                    this._generator.setDate(this._request.getTimeStampBuffer());
                    break;
                }
                break;
            case 11:
                this._generator.setHead(this._head);
                if (!this._parser.isPersistent()) {
                    this._responseFields.add(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                    this._generator.setPersistent(false);
                }
                if (this._server.getSendDateHeader()) {
                    this._generator.setDate(this._request.getTimeStampBuffer());
                }
                if (!this._host) {
                    LOG.debug("!host {}", this);
                    this._generator.setResponse(400, null);
                    this._responseFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                    this._generator.completeHeader(this._responseFields, true);
                    this._generator.complete();
                    return;
                } else if (this._expect) {
                    LOG.debug("!expectation {}", this);
                    this._generator.setResponse(417, null);
                    this._responseFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                    this._generator.completeHeader(this._responseFields, true);
                    this._generator.complete();
                    return;
                }
                break;
        }
        if (this._charset != null) {
            this._request.setCharacterEncodingUnchecked(this._charset);
        }
        if ((((HttpParser) this._parser).getContentLength() <= 0 && !((HttpParser) this._parser).isChunking()) || this._expect100Continue) {
            handleRequest();
        } else {
            this._delayedHandling = true;
        }
    }

    protected void content(Buffer buffer) throws IOException {
        if (this._delayedHandling) {
            this._delayedHandling = false;
            handleRequest();
        }
    }

    public void messageComplete(long contentLength) throws IOException {
        if (this._delayedHandling) {
            this._delayedHandling = false;
            handleRequest();
        }
    }

    public void earlyEOF() {
        this._earlyEOF = true;
    }

    /* loaded from: classes.dex */
    private class RequestHandler extends HttpParser.EventHandler {
        private RequestHandler() {
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException {
            AbstractHttpConnection.this.startRequest(method, uri, version);
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void parsedHeader(Buffer name, Buffer value) throws IOException {
            AbstractHttpConnection.this.parsedHeader(name, value);
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void headerComplete() throws IOException {
            AbstractHttpConnection.this.headerComplete();
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void content(Buffer ref) throws IOException {
            AbstractHttpConnection.this.content(ref);
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void messageComplete(long contentLength) throws IOException {
            AbstractHttpConnection.this.messageComplete(contentLength);
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void startResponse(Buffer version, int status, Buffer reason) {
            if (AbstractHttpConnection.LOG.isDebugEnabled()) {
                Logger logger = AbstractHttpConnection.LOG;
                logger.debug("Bad request!: " + version + " " + status + " " + reason, new Object[0]);
            }
        }

        @Override // org.eclipse.jetty.http.HttpParser.EventHandler
        public void earlyEOF() {
            AbstractHttpConnection.this.earlyEOF();
        }
    }

    /* loaded from: classes.dex */
    public class Output extends HttpOutput {
        Output() {
            super(AbstractHttpConnection.this);
        }

        @Override // org.eclipse.jetty.server.HttpOutput, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (isClosed()) {
                return;
            }
            if (!AbstractHttpConnection.this.isIncluding() && !this._generator.isCommitted()) {
                AbstractHttpConnection.this.commitResponse(true);
            } else {
                AbstractHttpConnection.this.flushResponse();
            }
            super.close();
        }

        @Override // org.eclipse.jetty.server.HttpOutput, java.io.OutputStream, java.io.Flushable
        public void flush() throws IOException {
            if (!this._generator.isCommitted()) {
                AbstractHttpConnection.this.commitResponse(false);
            }
            super.flush();
        }

        @Override // org.eclipse.jetty.server.HttpOutput, javax.servlet.ServletOutputStream
        public void print(String s) throws IOException {
            if (isClosed()) {
                throw new IOException("Closed");
            }
            PrintWriter writer = AbstractHttpConnection.this.getPrintWriter(null);
            writer.print(s);
        }

        public void sendResponse(Buffer response) throws IOException {
            ((HttpGenerator) this._generator).sendResponse(response);
        }

        public void sendContent(Object content) throws IOException {
            Resource resource = null;
            if (isClosed()) {
                throw new IOException("Closed");
            }
            if (this._generator.isWritten()) {
                throw new IllegalStateException("!empty");
            }
            if (content instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) content;
                Buffer contentType = httpContent.getContentType();
                if (contentType != null && !AbstractHttpConnection.this._responseFields.containsKey(HttpHeaders.CONTENT_TYPE_BUFFER)) {
                    String enc = AbstractHttpConnection.this._response.getSetCharacterEncoding();
                    if (enc == null) {
                        AbstractHttpConnection.this._responseFields.add(HttpHeaders.CONTENT_TYPE_BUFFER, contentType);
                    } else if (contentType instanceof BufferCache.CachedBuffer) {
                        BufferCache.CachedBuffer content_type = ((BufferCache.CachedBuffer) contentType).getAssociate(enc);
                        if (content_type != null) {
                            AbstractHttpConnection.this._responseFields.put(HttpHeaders.CONTENT_TYPE_BUFFER, content_type);
                        } else {
                            HttpFields httpFields = AbstractHttpConnection.this._responseFields;
                            Buffer buffer = HttpHeaders.CONTENT_TYPE_BUFFER;
                            httpFields.put(buffer, contentType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(enc, ";= "));
                        }
                    } else {
                        HttpFields httpFields2 = AbstractHttpConnection.this._responseFields;
                        Buffer buffer2 = HttpHeaders.CONTENT_TYPE_BUFFER;
                        httpFields2.put(buffer2, contentType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(enc, ";= "));
                    }
                }
                if (httpContent.getContentLength() > 0) {
                    AbstractHttpConnection.this._responseFields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, httpContent.getContentLength());
                }
                Buffer lm = httpContent.getLastModified();
                long lml = httpContent.getResource().lastModified();
                if (lm != null) {
                    AbstractHttpConnection.this._responseFields.put(HttpHeaders.LAST_MODIFIED_BUFFER, lm);
                } else if (httpContent.getResource() != null && lml != -1) {
                    AbstractHttpConnection.this._responseFields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, lml);
                }
                Buffer etag = httpContent.getETag();
                if (etag != null) {
                    AbstractHttpConnection.this._responseFields.put(HttpHeaders.ETAG_BUFFER, etag);
                }
                boolean direct = (AbstractHttpConnection.this._connector instanceof NIOConnector) && ((NIOConnector) AbstractHttpConnection.this._connector).getUseDirectBuffers() && !(AbstractHttpConnection.this._connector instanceof SslConnector);
                content = direct ? httpContent.getDirectBuffer() : httpContent.getIndirectBuffer();
                if (content == null) {
                    content = httpContent.getInputStream();
                }
            } else if (content instanceof Resource) {
                resource = (Resource) content;
                AbstractHttpConnection.this._responseFields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, resource.lastModified());
                content = resource.getInputStream();
            }
            if (content instanceof Buffer) {
                this._generator.addContent((Buffer) content, true);
                AbstractHttpConnection.this.commitResponse(true);
            } else if (content instanceof InputStream) {
                InputStream in = (InputStream) content;
                try {
                    int max = this._generator.prepareUncheckedAddContent();
                    Buffer buffer3 = this._generator.getUncheckedBuffer();
                    int len = buffer3.readFrom(in, max);
                    while (len >= 0 && !AbstractHttpConnection.this._endp.isOutputShutdown()) {
                        this._generator.completeUncheckedAddContent();
                        AbstractHttpConnection.this._out.flush();
                        int max2 = this._generator.prepareUncheckedAddContent();
                        Buffer buffer4 = this._generator.getUncheckedBuffer();
                        len = buffer4.readFrom(in, max2);
                    }
                    this._generator.completeUncheckedAddContent();
                    AbstractHttpConnection.this._out.flush();
                    if (resource != null) {
                        resource.release();
                    } else {
                        in.close();
                    }
                } catch (Throwable th) {
                    if (resource != null) {
                        resource.release();
                    } else {
                        in.close();
                    }
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("unknown content type?");
            }
        }
    }

    /* loaded from: classes.dex */
    public class OutputWriter extends HttpWriter {
        OutputWriter() {
            super(AbstractHttpConnection.this._out);
        }
    }
}
