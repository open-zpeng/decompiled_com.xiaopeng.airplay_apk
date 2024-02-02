package org.eclipse.jetty.http.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public abstract class CompressedResponseWrapper extends HttpServletResponseWrapper {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_MIN_COMPRESS_SIZE = 256;
    private int _bufferSize;
    private AbstractCompressedStream _compressedStream;
    private long _contentLength;
    private String _etag;
    private Set<String> _mimeTypes;
    private int _minCompressSize;
    private boolean _noCompression;
    protected HttpServletRequest _request;
    private PrintWriter _writer;

    protected abstract AbstractCompressedStream newCompressedStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException;

    public CompressedResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this._bufferSize = DEFAULT_BUFFER_SIZE;
        this._minCompressSize = DEFAULT_MIN_COMPRESS_SIZE;
        this._contentLength = -1L;
        this._request = request;
    }

    public long getContentLength() {
        return this._contentLength;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public int getBufferSize() {
        return this._bufferSize;
    }

    public int getMinCompressSize() {
        return this._minCompressSize;
    }

    public String getETag() {
        return this._etag;
    }

    public HttpServletRequest getRequest() {
        return this._request;
    }

    public void setMimeTypes(Set<String> mimeTypes) {
        this._mimeTypes = mimeTypes;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void setBufferSize(int bufferSize) {
        this._bufferSize = bufferSize;
        if (this._compressedStream != null) {
            this._compressedStream.setBufferSize(bufferSize);
        }
    }

    public void setMinCompressSize(int minCompressSize) {
        this._minCompressSize = minCompressSize;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void setContentType(String ct) {
        int colon;
        super.setContentType(ct);
        if (!this._noCompression) {
            if (ct != null && (colon = ct.indexOf(";")) > 0) {
                ct = ct.substring(0, colon);
            }
            if (this._compressedStream == null || this._compressedStream.getOutputStream() == null) {
                if (this._mimeTypes != null || ct == null || !ct.contains(HttpHeaderValues.GZIP)) {
                    if (this._mimeTypes == null) {
                        return;
                    }
                    if (ct != null && this._mimeTypes.contains(StringUtil.asciiToLowerCase(ct))) {
                        return;
                    }
                }
                noCompression();
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        if (sc < 200 || sc == 204 || sc == 205 || sc >= 300) {
            noCompression();
        }
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void setStatus(int sc) {
        super.setStatus(sc);
        if (sc < 200 || sc == 204 || sc == 205 || sc >= 300) {
            noCompression();
        }
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void setContentLength(int length) {
        if (this._noCompression) {
            super.setContentLength(length);
        } else {
            setContentLength(length);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setContentLength(long length) {
        this._contentLength = length;
        if (this._compressedStream != null) {
            this._compressedStream.setContentLength();
        } else if (this._noCompression && this._contentLength >= 0) {
            HttpServletResponse response = (HttpServletResponse) getResponse();
            if (this._contentLength < 2147483647L) {
                response.setContentLength((int) this._contentLength);
            } else {
                response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(this._contentLength));
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void addHeader(String name, String value) {
        if ("content-length".equalsIgnoreCase(name)) {
            this._contentLength = Long.parseLong(value);
            if (this._compressedStream != null) {
                this._compressedStream.setContentLength();
            }
        } else if ("content-type".equalsIgnoreCase(name)) {
            setContentType(value);
        } else if ("content-encoding".equalsIgnoreCase(name)) {
            super.addHeader(name, value);
            if (!isCommitted()) {
                noCompression();
            }
        } else if ("etag".equalsIgnoreCase(name)) {
            this._etag = value;
        } else {
            super.addHeader(name, value);
        }
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void flushBuffer() throws IOException {
        if (this._writer != null) {
            this._writer.flush();
        }
        if (this._compressedStream != null) {
            this._compressedStream.flush();
        } else {
            getResponse().flushBuffer();
        }
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void reset() {
        super.reset();
        if (this._compressedStream != null) {
            this._compressedStream.resetBuffer();
        }
        this._writer = null;
        this._compressedStream = null;
        this._noCompression = false;
        this._contentLength = -1L;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void resetBuffer() {
        super.resetBuffer();
        if (this._compressedStream != null) {
            this._compressedStream.resetBuffer();
        }
        this._writer = null;
        this._compressedStream = null;
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void sendError(int sc, String msg) throws IOException {
        resetBuffer();
        super.sendError(sc, msg);
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void sendError(int sc) throws IOException {
        resetBuffer();
        super.sendError(sc);
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void sendRedirect(String location) throws IOException {
        resetBuffer();
        super.sendRedirect(location);
    }

    public void noCompression() {
        if (!this._noCompression) {
            setDeferredHeaders();
        }
        this._noCompression = true;
        if (this._compressedStream != null) {
            try {
                this._compressedStream.doNotCompress(false);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void finish() throws IOException {
        if (this._writer != null && !this._compressedStream.isClosed()) {
            this._writer.flush();
        }
        if (this._compressedStream != null) {
            this._compressedStream.finish();
        } else {
            setDeferredHeaders();
        }
    }

    private void setDeferredHeaders() {
        if (!isCommitted()) {
            if (this._contentLength >= 0) {
                if (this._contentLength < 2147483647L) {
                    super.setContentLength((int) this._contentLength);
                } else {
                    super.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(this._contentLength));
                }
            }
            if (this._etag != null) {
                super.setHeader(HttpHeaders.ETAG, this._etag);
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void setHeader(String name, String value) {
        if (this._noCompression) {
            super.setHeader(name, value);
        } else if ("content-length".equalsIgnoreCase(name)) {
            setContentLength(Long.parseLong(value));
        } else if ("content-type".equalsIgnoreCase(name)) {
            setContentType(value);
        } else if ("content-encoding".equalsIgnoreCase(name)) {
            super.setHeader(name, value);
            if (!isCommitted()) {
                noCompression();
            }
        } else if ("etag".equalsIgnoreCase(name)) {
            this._etag = value;
        } else {
            super.setHeader(name, value);
        }
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public boolean containsHeader(String name) {
        if (!this._noCompression && "etag".equalsIgnoreCase(name) && this._etag != null) {
            return true;
        }
        return super.containsHeader(name);
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public ServletOutputStream getOutputStream() throws IOException {
        if (this._compressedStream == null) {
            if (getResponse().isCommitted() || this._noCompression) {
                return getResponse().getOutputStream();
            }
            this._compressedStream = newCompressedStream(this._request, (HttpServletResponse) getResponse());
        } else if (this._writer != null) {
            throw new IllegalStateException("getWriter() called");
        }
        return this._compressedStream;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public PrintWriter getWriter() throws IOException {
        if (this._writer == null) {
            if (this._compressedStream != null) {
                throw new IllegalStateException("getOutputStream() called");
            }
            if (getResponse().isCommitted() || this._noCompression) {
                return getResponse().getWriter();
            }
            this._compressedStream = newCompressedStream(this._request, (HttpServletResponse) getResponse());
            this._writer = newWriter(this._compressedStream, getCharacterEncoding());
        }
        return this._writer;
    }

    @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
    public void setIntHeader(String name, int value) {
        if ("content-length".equalsIgnoreCase(name)) {
            this._contentLength = value;
            if (this._compressedStream != null) {
                this._compressedStream.setContentLength();
                return;
            }
            return;
        }
        super.setIntHeader(name, value);
    }

    protected PrintWriter newWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        return encoding == null ? new PrintWriter(out) : new PrintWriter(new OutputStreamWriter(out, encoding));
    }
}
