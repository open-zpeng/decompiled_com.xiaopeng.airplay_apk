package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.gzip.AbstractCompressedStream;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class GzipHandler extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(GzipHandler.class);
    protected Set<String> _excluded;
    protected Set<String> _mimeTypes;
    protected int _bufferSize = CompressedResponseWrapper.DEFAULT_BUFFER_SIZE;
    protected int _minGzipSize = CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE;
    protected String _vary = "Accept-Encoding, User-Agent";

    public Set<String> getMimeTypes() {
        return this._mimeTypes;
    }

    public void setMimeTypes(Set<String> mimeTypes) {
        this._mimeTypes = mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        if (mimeTypes != null) {
            this._mimeTypes = new HashSet();
            StringTokenizer tok = new StringTokenizer(mimeTypes, ",", false);
            while (tok.hasMoreTokens()) {
                this._mimeTypes.add(tok.nextToken());
            }
        }
    }

    public Set<String> getExcluded() {
        return this._excluded;
    }

    public void setExcluded(Set<String> excluded) {
        this._excluded = excluded;
    }

    public void setExcluded(String excluded) {
        if (excluded != null) {
            this._excluded = new HashSet();
            StringTokenizer tok = new StringTokenizer(excluded, ",", false);
            while (tok.hasMoreTokens()) {
                this._excluded.add(tok.nextToken());
            }
        }
    }

    public String getVary() {
        return this._vary;
    }

    public void setVary(String vary) {
        this._vary = vary;
    }

    public int getBufferSize() {
        return this._bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this._bufferSize = bufferSize;
    }

    public int getMinGzipSize() {
        return this._minGzipSize;
    }

    public void setMinGzipSize(int minGzipSize) {
        this._minGzipSize = minGzipSize;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this._handler != null && isStarted()) {
            String ae = request.getHeader("accept-encoding");
            if (ae != null && ae.indexOf(HttpHeaderValues.GZIP) >= 0 && !response.containsHeader(HttpHeaders.CONTENT_ENCODING) && !HttpMethods.HEAD.equalsIgnoreCase(request.getMethod())) {
                if (this._excluded != null) {
                    String ua = request.getHeader(HttpHeaders.USER_AGENT);
                    if (this._excluded.contains(ua)) {
                        this._handler.handle(target, baseRequest, request, response);
                        return;
                    }
                }
                final CompressedResponseWrapper wrappedResponse = newGzipResponseWrapper(request, response);
                try {
                    this._handler.handle(target, baseRequest, request, wrappedResponse);
                    Continuation continuation = ContinuationSupport.getContinuation(request);
                    if (continuation.isSuspended() && continuation.isResponseWrapped()) {
                        continuation.addContinuationListener(new ContinuationListener() { // from class: org.eclipse.jetty.server.handler.GzipHandler.1
                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onComplete(Continuation continuation2) {
                                try {
                                    wrappedResponse.finish();
                                } catch (IOException e) {
                                    GzipHandler.LOG.warn(e);
                                }
                            }

                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onTimeout(Continuation continuation2) {
                            }
                        });
                        return;
                    } else if (0 != 0 && !response.isCommitted()) {
                        wrappedResponse.resetBuffer();
                        wrappedResponse.noCompression();
                        return;
                    } else {
                        wrappedResponse.finish();
                        return;
                    }
                } catch (Throwable th) {
                    Continuation continuation2 = ContinuationSupport.getContinuation(request);
                    if (continuation2.isSuspended() && continuation2.isResponseWrapped()) {
                        continuation2.addContinuationListener(new ContinuationListener() { // from class: org.eclipse.jetty.server.handler.GzipHandler.1
                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onComplete(Continuation continuation22) {
                                try {
                                    wrappedResponse.finish();
                                } catch (IOException e) {
                                    GzipHandler.LOG.warn(e);
                                }
                            }

                            @Override // org.eclipse.jetty.continuation.ContinuationListener
                            public void onTimeout(Continuation continuation22) {
                            }
                        });
                    } else if (1 != 0 && !response.isCommitted()) {
                        wrappedResponse.resetBuffer();
                        wrappedResponse.noCompression();
                    } else {
                        wrappedResponse.finish();
                    }
                    throw th;
                }
            }
            this._handler.handle(target, baseRequest, request, response);
        }
    }

    protected CompressedResponseWrapper newGzipResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
        return new CompressedResponseWrapper(request, response) { // from class: org.eclipse.jetty.server.handler.GzipHandler.2
            {
                super.setMimeTypes(GzipHandler.this._mimeTypes);
                super.setBufferSize(GzipHandler.this._bufferSize);
                super.setMinCompressSize(GzipHandler.this._minGzipSize);
            }

            @Override // org.eclipse.jetty.http.gzip.CompressedResponseWrapper
            protected AbstractCompressedStream newCompressedStream(HttpServletRequest request2, HttpServletResponse response2) throws IOException {
                return new AbstractCompressedStream(HttpHeaderValues.GZIP, request2, this, GzipHandler.this._vary) { // from class: org.eclipse.jetty.server.handler.GzipHandler.2.1
                    @Override // org.eclipse.jetty.http.gzip.AbstractCompressedStream
                    protected DeflaterOutputStream createStream() throws IOException {
                        return new GZIPOutputStream(this._response.getOutputStream(), GzipHandler.this._bufferSize);
                    }
                };
            }

            @Override // org.eclipse.jetty.http.gzip.CompressedResponseWrapper
            protected PrintWriter newWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
                return GzipHandler.this.newWriter(out, encoding);
            }
        };
    }

    protected PrintWriter newWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        return encoding == null ? new PrintWriter(out) : new PrintWriter(new OutputStreamWriter(out, encoding));
    }
}
