package org.eclipse.jetty.http.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
/* loaded from: classes.dex */
public abstract class AbstractCompressedStream extends ServletOutputStream {
    protected ByteArrayOutputStream2 _bOut;
    protected boolean _closed;
    protected DeflaterOutputStream _compressedOutputStream;
    protected boolean _doNotCompress;
    private final String _encoding;
    protected OutputStream _out;
    protected final HttpServletResponse _response;
    protected final String _vary;
    protected final CompressedResponseWrapper _wrapper;

    protected abstract DeflaterOutputStream createStream() throws IOException;

    public AbstractCompressedStream(String encoding, HttpServletRequest request, CompressedResponseWrapper wrapper, String vary) throws IOException {
        this._encoding = encoding;
        this._wrapper = wrapper;
        this._response = (HttpServletResponse) wrapper.getResponse();
        this._vary = vary;
        if (this._wrapper.getMinCompressSize() == 0) {
            doCompress();
        }
    }

    public void resetBuffer() {
        if (this._response.isCommitted() || this._compressedOutputStream != null) {
            throw new IllegalStateException("Committed");
        }
        this._closed = false;
        this._out = null;
        this._bOut = null;
        this._doNotCompress = false;
    }

    public void setBufferSize(int bufferSize) {
        if (this._bOut != null && this._bOut.getBuf().length < bufferSize) {
            ByteArrayOutputStream2 b = new ByteArrayOutputStream2(bufferSize);
            b.write(this._bOut.getBuf(), 0, this._bOut.size());
            this._bOut = b;
        }
    }

    public void setContentLength() {
        if (this._doNotCompress) {
            long length = this._wrapper.getContentLength();
            if (length >= 0) {
                if (length < 2147483647L) {
                    this._response.setContentLength((int) length);
                } else {
                    this._response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
                }
            }
        }
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        if (this._out == null || this._bOut != null) {
            long length = this._wrapper.getContentLength();
            if (length > 0 && length < this._wrapper.getMinCompressSize()) {
                doNotCompress(false);
            } else {
                doCompress();
            }
        }
        this._out.flush();
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this._closed) {
            return;
        }
        if (this._wrapper.getRequest().getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
            flush();
            return;
        }
        if (this._bOut != null) {
            long length = this._wrapper.getContentLength();
            if (length < 0) {
                length = this._bOut.getCount();
                this._wrapper.setContentLength(length);
            }
            if (length < this._wrapper.getMinCompressSize()) {
                doNotCompress(false);
            } else {
                doCompress();
            }
        } else if (this._out == null) {
            doNotCompress(false);
        }
        if (this._compressedOutputStream != null) {
            this._compressedOutputStream.close();
        } else {
            this._out.close();
        }
        this._closed = true;
    }

    public void finish() throws IOException {
        if (!this._closed) {
            if (this._out == null || this._bOut != null) {
                long length = this._wrapper.getContentLength();
                if (length >= 0 && length < this._wrapper.getMinCompressSize()) {
                    doNotCompress(false);
                } else {
                    doCompress();
                }
            }
            if (this._compressedOutputStream != null && !this._closed) {
                this._closed = true;
                this._compressedOutputStream.close();
            }
        }
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        checkOut(1);
        this._out.write(b);
    }

    @Override // java.io.OutputStream
    public void write(byte[] b) throws IOException {
        checkOut(b.length);
        this._out.write(b);
    }

    @Override // java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        checkOut(len);
        this._out.write(b, off, len);
    }

    public void doCompress() throws IOException {
        if (this._compressedOutputStream == null) {
            if (this._response.isCommitted()) {
                throw new IllegalStateException();
            }
            if (this._encoding != null) {
                setHeader(HttpHeaders.CONTENT_ENCODING, this._encoding);
                if (this._response.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
                    addHeader(HttpHeaders.VARY, this._vary);
                    DeflaterOutputStream createStream = createStream();
                    this._compressedOutputStream = createStream;
                    this._out = createStream;
                    if (this._out != null) {
                        if (this._bOut != null) {
                            this._out.write(this._bOut.getBuf(), 0, this._bOut.getCount());
                            this._bOut = null;
                        }
                        String etag = this._wrapper.getETag();
                        if (etag != null) {
                            setHeader(HttpHeaders.ETAG, etag.substring(0, etag.length() - 1) + '-' + this._encoding + '\"');
                            return;
                        }
                        return;
                    }
                }
            }
            doNotCompress(true);
        }
    }

    public void doNotCompress(boolean sendVary) throws IOException {
        if (this._compressedOutputStream != null) {
            throw new IllegalStateException("Compressed output stream is already assigned.");
        }
        if (this._out == null || this._bOut != null) {
            if (sendVary) {
                addHeader(HttpHeaders.VARY, this._vary);
            }
            if (this._wrapper.getETag() != null) {
                setHeader(HttpHeaders.ETAG, this._wrapper.getETag());
            }
            this._doNotCompress = true;
            this._out = this._response.getOutputStream();
            setContentLength();
            if (this._bOut != null) {
                this._out.write(this._bOut.getBuf(), 0, this._bOut.getCount());
            }
            this._bOut = null;
        }
    }

    private void checkOut(int lengthToWrite) throws IOException {
        if (this._closed) {
            throw new IOException("CLOSED");
        }
        if (this._out == null) {
            if (lengthToWrite > this._wrapper.getBufferSize()) {
                long length = this._wrapper.getContentLength();
                if (length >= 0 && length < this._wrapper.getMinCompressSize()) {
                    doNotCompress(false);
                    return;
                } else {
                    doCompress();
                    return;
                }
            }
            ByteArrayOutputStream2 byteArrayOutputStream2 = new ByteArrayOutputStream2(this._wrapper.getBufferSize());
            this._bOut = byteArrayOutputStream2;
            this._out = byteArrayOutputStream2;
        } else if (this._bOut != null && lengthToWrite >= this._bOut.getBuf().length - this._bOut.getCount()) {
            long length2 = this._wrapper.getContentLength();
            if (length2 >= 0 && length2 < this._wrapper.getMinCompressSize()) {
                doNotCompress(false);
            } else {
                doCompress();
            }
        }
    }

    public OutputStream getOutputStream() {
        return this._out;
    }

    public boolean isClosed() {
        return this._closed;
    }

    protected PrintWriter newWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        return encoding == null ? new PrintWriter(out) : new PrintWriter(new OutputStreamWriter(out, encoding));
    }

    protected void addHeader(String name, String value) {
        this._response.addHeader(name, value);
    }

    protected void setHeader(String name, String value) {
        this._response.setHeader(name, value);
    }
}
