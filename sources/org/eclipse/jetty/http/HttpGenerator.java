package org.eclipse.jetty.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class HttpGenerator extends AbstractGenerator {
    private static final int CHUNK_SPACE = 12;
    private static final byte[] CONNECTION_;
    private static final byte[] CONNECTION_CLOSE;
    private static final byte[] CONNECTION_KEEP_ALIVE;
    private static final byte[] CONTENT_LENGTH_0;
    private static final byte[] CRLF;
    private static final byte[] LAST_CHUNK;
    private static byte[] SERVER;
    private static final byte[] TRANSFER_ENCODING_CHUNKED;
    private boolean _bufferChunked;
    protected boolean _bypass;
    private boolean _needCRLF;
    private boolean _needEOC;
    private static final Logger LOG = Log.getLogger(HttpGenerator.class);
    private static final Status[] __status = new Status[508];

    static {
        int versionLength = HttpVersions.HTTP_1_1_BUFFER.length();
        for (int i = 0; i < __status.length; i++) {
            HttpStatus.Code code = HttpStatus.getCode(i);
            if (code != null) {
                String reason = code.getMessage();
                byte[] bytes = new byte[versionLength + 5 + reason.length() + 2];
                HttpVersions.HTTP_1_1_BUFFER.peek(0, bytes, 0, versionLength);
                bytes[versionLength + 0] = HttpTokens.SPACE;
                bytes[versionLength + 1] = (byte) ((i / 100) + 48);
                bytes[versionLength + 2] = (byte) (((i % 100) / 10) + 48);
                bytes[versionLength + 3] = (byte) (48 + (i % 10));
                bytes[versionLength + 4] = HttpTokens.SPACE;
                for (int j = 0; j < reason.length(); j++) {
                    bytes[versionLength + 5 + j] = (byte) reason.charAt(j);
                }
                int j2 = versionLength + 5;
                bytes[j2 + reason.length()] = HttpTokens.CARRIAGE_RETURN;
                bytes[versionLength + 6 + reason.length()] = 10;
                __status[i] = new Status();
                __status[i]._reason = new ByteArrayBuffer(bytes, versionLength + 5, (bytes.length - versionLength) - 7, 0);
                __status[i]._schemeCode = new ByteArrayBuffer(bytes, 0, versionLength + 5, 0);
                __status[i]._responseLine = new ByteArrayBuffer(bytes, 0, bytes.length, 0);
            }
        }
        LAST_CHUNK = new byte[]{48, HttpTokens.CARRIAGE_RETURN, 10, HttpTokens.CARRIAGE_RETURN, 10};
        CONTENT_LENGTH_0 = StringUtil.getBytes("Content-Length: 0\r\n");
        CONNECTION_KEEP_ALIVE = StringUtil.getBytes("Connection: keep-alive\r\n");
        CONNECTION_CLOSE = StringUtil.getBytes("Connection: close\r\n");
        CONNECTION_ = StringUtil.getBytes("Connection: ");
        CRLF = StringUtil.getBytes("\r\n");
        TRANSFER_ENCODING_CHUNKED = StringUtil.getBytes("Transfer-Encoding: chunked\r\n");
        SERVER = StringUtil.getBytes("Server: Jetty(7.0.x)\r\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Status {
        Buffer _reason;
        Buffer _responseLine;
        Buffer _schemeCode;

        private Status() {
        }
    }

    public static Buffer getReasonBuffer(int code) {
        Status status = code < __status.length ? __status[code] : null;
        if (status != null) {
            return status._reason;
        }
        return null;
    }

    public static void setServerVersion(String version) {
        SERVER = StringUtil.getBytes("Server: Jetty(" + version + ")\r\n");
    }

    public HttpGenerator(Buffers buffers, EndPoint io) {
        super(buffers, io);
        this._bypass = false;
        this._needCRLF = false;
        this._needEOC = false;
        this._bufferChunked = false;
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator, org.eclipse.jetty.http.Generator
    public void reset() {
        if (this._persistent != null && !this._persistent.booleanValue() && this._endp != null && !this._endp.isOutputShutdown()) {
            try {
                this._endp.shutdownOutput();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
        super.reset();
        if (this._buffer != null) {
            this._buffer.clear();
        }
        if (this._header != null) {
            this._header.clear();
        }
        if (this._content != null) {
            this._content = null;
        }
        this._bypass = false;
        this._needCRLF = false;
        this._needEOC = false;
        this._bufferChunked = false;
        this._method = null;
        this._uri = null;
        this._noContent = false;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void addContent(Buffer content, boolean last) throws IOException {
        if (this._noContent) {
            throw new IllegalStateException("NO CONTENT");
        }
        if (this._last || this._state == 4) {
            LOG.warn("Ignoring extra content {}", content);
            content.clear();
            return;
        }
        this._last = last;
        if ((this._content != null && this._content.length() > 0) || this._bufferChunked) {
            if (this._endp.isOutputShutdown()) {
                throw new EofException();
            }
            flushBuffer();
            if (this._content != null && this._content.length() > 0) {
                if (this._bufferChunked) {
                    Buffer nc = this._buffers.getBuffer(this._content.length() + 12 + content.length());
                    nc.put(this._content);
                    nc.put(HttpTokens.CRLF);
                    BufferUtil.putHexInt(nc, content.length());
                    nc.put(HttpTokens.CRLF);
                    nc.put(content);
                    content = nc;
                } else {
                    Buffer nc2 = this._buffers.getBuffer(this._content.length() + content.length());
                    nc2.put(this._content);
                    nc2.put(content);
                    content = nc2;
                }
            }
        }
        this._content = content;
        this._contentWritten += content.length();
        if (this._head) {
            content.clear();
            this._content = null;
        } else if (this._endp != null && ((this._buffer == null || this._buffer.length() == 0) && this._content.length() > 0 && (this._last || (isCommitted() && this._content.length() > 1024)))) {
            this._bypass = true;
        } else if (!this._bufferChunked) {
            if (this._buffer == null) {
                this._buffer = this._buffers.getBuffer();
            }
            int len = this._buffer.put(this._content);
            this._content.skip(len);
            if (this._content.length() == 0) {
                this._content = null;
            }
        }
    }

    public void sendResponse(Buffer response) throws IOException {
        if (this._noContent || this._state != 0 || ((this._content != null && this._content.length() > 0) || this._bufferChunked || this._head)) {
            throw new IllegalStateException();
        }
        this._last = true;
        this._content = response;
        this._bypass = true;
        this._state = 3;
        long length = response.length();
        this._contentWritten = length;
        this._contentLength = length;
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator
    public int prepareUncheckedAddContent() throws IOException {
        if (this._noContent || this._last || this._state == 4) {
            return -1;
        }
        Buffer content = this._content;
        if ((content != null && content.length() > 0) || this._bufferChunked) {
            flushBuffer();
            if ((content != null && content.length() > 0) || this._bufferChunked) {
                throw new IllegalStateException("FULL");
            }
        }
        if (this._buffer == null) {
            this._buffer = this._buffers.getBuffer();
        }
        this._contentWritten -= this._buffer.length();
        if (this._head) {
            return Integer.MAX_VALUE;
        }
        return this._buffer.space() - (this._contentLength == -2 ? 12 : 0);
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator, org.eclipse.jetty.http.Generator
    public boolean isBufferFull() {
        return super.isBufferFull() || this._bufferChunked || this._bypass || (this._contentLength == -2 && this._buffer != null && this._buffer.space() < 12);
    }

    public void send1xx(int code) throws IOException {
        if (this._state != 0) {
            return;
        }
        if (code < 100 || code > 199) {
            throw new IllegalArgumentException("!1xx");
        }
        Status status = __status[code];
        if (status == null) {
            throw new IllegalArgumentException(code + "?");
        }
        if (this._header == null) {
            this._header = this._buffers.getHeader();
        }
        this._header.put(status._responseLine);
        this._header.put(HttpTokens.CRLF);
        while (this._header.length() > 0) {
            try {
                int len = this._endp.flush(this._header);
                if (len < 0 || !this._endp.isOpen()) {
                    throw new EofException();
                } else if (len == 0) {
                    Thread.sleep(100L);
                }
            } catch (InterruptedException e) {
                LOG.debug(e);
                throw new InterruptedIOException(e.toString());
            }
        }
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator
    public boolean isRequest() {
        return this._method != null;
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator
    public boolean isResponse() {
        return this._method == null;
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator, org.eclipse.jetty.http.Generator
    public void completeHeader(HttpFields fields, boolean allContentAdded) throws IOException {
        boolean keep_alive;
        long j;
        boolean keep_alive2;
        int s;
        int s2;
        HttpFields httpFields = fields;
        if (this._state != 0) {
            return;
        }
        if (isResponse() && this._status == 0) {
            throw new EofException();
        }
        if (this._last && !allContentAdded) {
            throw new IllegalStateException("last?");
        }
        this._last |= allContentAdded;
        if (this._header == null) {
            this._header = this._buffers.getHeader();
        }
        boolean has_server = false;
        try {
            int i = 48;
            int i2 = 1;
            if (isRequest()) {
                this._persistent = true;
                if (this._version == 9) {
                    this._contentLength = 0L;
                    this._header.put(this._method);
                    this._header.put(HttpTokens.SPACE);
                    this._header.put(this._uri.getBytes(StringUtil.__UTF8));
                    this._header.put(HttpTokens.CRLF);
                    this._state = 3;
                    this._noContent = true;
                    return;
                }
                this._header.put(this._method);
                this._header.put(HttpTokens.SPACE);
                this._header.put(this._uri.getBytes(StringUtil.__UTF8));
                this._header.put(HttpTokens.SPACE);
                this._header.put(this._version == 10 ? HttpVersions.HTTP_1_0_BUFFER : HttpVersions.HTTP_1_1_BUFFER);
                this._header.put(HttpTokens.CRLF);
            } else if (this._version == 9) {
                this._persistent = false;
                this._contentLength = -1L;
                this._state = 2;
                return;
            } else {
                if (this._persistent == null) {
                    this._persistent = Boolean.valueOf(this._version > 10);
                }
                Status status = this._status < __status.length ? __status[this._status] : null;
                if (status == null) {
                    this._header.put(HttpVersions.HTTP_1_1_BUFFER);
                    this._header.put(HttpTokens.SPACE);
                    this._header.put((byte) ((this._status / 100) + 48));
                    this._header.put((byte) (((this._status % 100) / 10) + 48));
                    this._header.put((byte) ((this._status % 10) + 48));
                    this._header.put(HttpTokens.SPACE);
                    if (this._reason == null) {
                        this._header.put((byte) ((this._status / 100) + 48));
                        this._header.put((byte) (((this._status % 100) / 10) + 48));
                        this._header.put((byte) ((this._status % 10) + 48));
                    } else {
                        this._header.put(this._reason);
                    }
                    this._header.put(HttpTokens.CRLF);
                } else if (this._reason == null) {
                    this._header.put(status._responseLine);
                } else {
                    this._header.put(status._schemeCode);
                    this._header.put(this._reason);
                    this._header.put(HttpTokens.CRLF);
                }
                if (this._status < 200 && this._status >= 100) {
                    this._noContent = true;
                    this._content = null;
                    if (this._buffer != null) {
                        this._buffer.clear();
                    }
                    if (this._status != 101) {
                        this._header.put(HttpTokens.CRLF);
                        this._state = 2;
                        return;
                    }
                } else if (this._status == 204 || this._status == 304) {
                    this._noContent = true;
                    this._content = null;
                    if (this._buffer != null) {
                        this._buffer.clear();
                    }
                }
            }
            if (this._status >= 200 && this._date != null) {
                this._header.put(HttpHeaders.DATE_BUFFER);
                this._header.put(HttpTokens.COLON);
                this._header.put(HttpTokens.SPACE);
                this._header.put(this._date);
                this._header.put(CRLF);
            }
            HttpFields.Field content_length = null;
            HttpFields.Field transfer_encoding = null;
            boolean keep_alive3 = false;
            boolean close = false;
            boolean content_type = false;
            StringBuilder connection = null;
            if (httpFields != null) {
                int s3 = fields.size();
                boolean has_server2 = false;
                int f = 0;
                while (true) {
                    int s4 = s3;
                    if (f >= s4) {
                        break;
                    }
                    try {
                        HttpFields.Field field = httpFields.getField(f);
                        if (field != null) {
                            int nameOrdinal = field.getNameOrdinal();
                            if (nameOrdinal != i2) {
                                if (nameOrdinal == 5) {
                                    keep_alive2 = keep_alive3;
                                    if (this._version == 11) {
                                        transfer_encoding = field;
                                    }
                                    s = s4;
                                    keep_alive3 = keep_alive2;
                                } else if (nameOrdinal == 12) {
                                    content_length = field;
                                    this._contentLength = field.getLongValue();
                                    keep_alive2 = keep_alive3;
                                    if (this._contentLength < this._contentWritten || (this._last && this._contentLength != this._contentWritten)) {
                                        content_length = null;
                                    }
                                    field.putTo(this._header);
                                } else if (nameOrdinal == 16) {
                                    if (BufferUtil.isPrefix(MimeTypes.MULTIPART_BYTERANGES_BUFFER, field.getValueBuffer())) {
                                        this._contentLength = -4L;
                                    }
                                    content_type = true;
                                    field.putTo(this._header);
                                    s = s4;
                                } else if (nameOrdinal != i) {
                                    field.putTo(this._header);
                                } else if (getSendServerVersion()) {
                                    try {
                                        field.putTo(this._header);
                                        s = s4;
                                        has_server2 = true;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        e = e;
                                        throw new RuntimeException("Header>" + this._header.capacity(), e);
                                    }
                                }
                                s = s4;
                                keep_alive3 = keep_alive2;
                            } else {
                                keep_alive2 = keep_alive3;
                                if (isRequest()) {
                                    field.putTo(this._header);
                                }
                                int connection_value = field.getValueOrdinal();
                                if (connection_value != -1) {
                                    if (connection_value != 1) {
                                        if (connection_value != 5) {
                                            if (connection_value != 11) {
                                                if (connection == null) {
                                                    connection = new StringBuilder();
                                                } else {
                                                    connection.append(',');
                                                }
                                                connection.append(field.getValue());
                                            } else if (isResponse()) {
                                                field.putTo(this._header);
                                            }
                                        } else if (this._version == 10) {
                                            keep_alive3 = true;
                                            if (isResponse()) {
                                                this._persistent = true;
                                            }
                                            s = s4;
                                        }
                                        s = s4;
                                    }
                                    close = true;
                                    if (isResponse()) {
                                        this._persistent = false;
                                    }
                                    if (!this._persistent.booleanValue() && isResponse() && this._contentLength == -3) {
                                        this._contentLength = -1L;
                                    }
                                    s = s4;
                                } else {
                                    String[] values = field.getValue().split(",");
                                    boolean close2 = close;
                                    int i3 = 0;
                                    while (values != null && i3 < values.length) {
                                        BufferCache.CachedBuffer cb = HttpHeaderValues.CACHE.get(values[i3].trim());
                                        if (cb != null) {
                                            int ordinal = cb.getOrdinal();
                                            s2 = s4;
                                            if (ordinal == 1) {
                                                close2 = true;
                                                if (isResponse()) {
                                                    this._persistent = false;
                                                }
                                                keep_alive2 = false;
                                                if (!this._persistent.booleanValue() && isResponse() && this._contentLength == -3) {
                                                    this._contentLength = -1L;
                                                }
                                            } else if (ordinal != 5) {
                                                if (connection == null) {
                                                    connection = new StringBuilder();
                                                } else {
                                                    connection.append(',');
                                                }
                                                connection.append(values[i3]);
                                            } else if (this._version == 10) {
                                                keep_alive2 = true;
                                                if (isResponse()) {
                                                    this._persistent = true;
                                                }
                                            }
                                        } else {
                                            s2 = s4;
                                            if (connection == null) {
                                                connection = new StringBuilder();
                                            } else {
                                                connection.append(',');
                                            }
                                            connection.append(values[i3]);
                                        }
                                        i3++;
                                        s4 = s2;
                                    }
                                    s = s4;
                                    close = close2;
                                }
                                keep_alive3 = keep_alive2;
                            }
                            f++;
                            s3 = s;
                            httpFields = fields;
                            i2 = 1;
                            i = 48;
                        }
                        keep_alive2 = keep_alive3;
                        s = s4;
                        keep_alive3 = keep_alive2;
                        f++;
                        s3 = s;
                        httpFields = fields;
                        i2 = 1;
                        i = 48;
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        e = e2;
                    }
                }
                keep_alive = keep_alive3;
                has_server = has_server2;
            } else {
                keep_alive = false;
            }
            switch ((int) this._contentLength) {
                case -3:
                    if (isResponse() && this._noContent) {
                        this._contentLength = 0L;
                        this._contentWritten = 0L;
                        break;
                    } else if (this._last) {
                        this._contentLength = this._contentWritten;
                        if (content_length == null && ((isResponse() || this._contentLength > 0 || content_type) && !this._noContent)) {
                            this._header.put(HttpHeaders.CONTENT_LENGTH_BUFFER);
                            this._header.put(HttpTokens.COLON);
                            this._header.put(HttpTokens.SPACE);
                            BufferUtil.putDecLong(this._header, this._contentLength);
                            this._header.put(HttpTokens.CRLF);
                            break;
                        }
                    } else {
                        if (this._persistent.booleanValue() && this._version >= 11) {
                            j = -2;
                            this._contentLength = j;
                            if (isRequest() && this._contentLength == -1) {
                                this._contentLength = 0L;
                                this._noContent = true;
                                break;
                            }
                        }
                        j = -1;
                        this._contentLength = j;
                        if (isRequest()) {
                            this._contentLength = 0L;
                            this._noContent = true;
                        }
                    }
                    break;
                case -1:
                    this._persistent = Boolean.valueOf(isRequest());
                    break;
                case 0:
                    if (content_length == null && isResponse() && this._status >= 200 && this._status != 204 && this._status != 304) {
                        this._header.put(CONTENT_LENGTH_0);
                        break;
                    }
                    break;
            }
            if (this._contentLength == -2) {
                if (transfer_encoding == null || 2 == transfer_encoding.getValueOrdinal()) {
                    this._header.put(TRANSFER_ENCODING_CHUNKED);
                } else {
                    String c = transfer_encoding.getValue();
                    if (!c.endsWith(HttpHeaderValues.CHUNKED)) {
                        throw new IllegalArgumentException("BAD TE");
                    }
                    transfer_encoding.putTo(this._header);
                }
            }
            if (this._contentLength == -1) {
                keep_alive = false;
                this._persistent = false;
            }
            if (isResponse()) {
                if (!this._persistent.booleanValue() && (close || this._version > 10)) {
                    this._header.put(CONNECTION_CLOSE);
                    if (connection != null) {
                        this._header.setPutIndex(this._header.putIndex() - 2);
                        this._header.put((byte) 44);
                        this._header.put(connection.toString().getBytes());
                        this._header.put(CRLF);
                    }
                } else if (keep_alive) {
                    this._header.put(CONNECTION_KEEP_ALIVE);
                    if (connection != null) {
                        this._header.setPutIndex(this._header.putIndex() - 2);
                        this._header.put((byte) 44);
                        this._header.put(connection.toString().getBytes());
                        this._header.put(CRLF);
                    }
                } else if (connection != null) {
                    this._header.put(CONNECTION_);
                    this._header.put(connection.toString().getBytes());
                    this._header.put(CRLF);
                }
            }
            if (!has_server && this._status > 199 && getSendServerVersion()) {
                this._header.put(SERVER);
            }
            this._header.put(HttpTokens.CRLF);
            this._state = 2;
        } catch (ArrayIndexOutOfBoundsException e3) {
            e = e3;
        }
    }

    @Override // org.eclipse.jetty.http.AbstractGenerator, org.eclipse.jetty.http.Generator
    public void complete() throws IOException {
        if (this._state == 4) {
            return;
        }
        super.complete();
        if (this._state < 3) {
            this._state = 3;
            if (this._contentLength == -2) {
                this._needEOC = true;
            }
        }
        flushBuffer();
    }

    /* JADX WARN: Code restructure failed: missing block: B:75:0x012a, code lost:
        return r0;
     */
    @Override // org.eclipse.jetty.http.AbstractGenerator, org.eclipse.jetty.http.Generator
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public int flushBuffer() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 346
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.http.HttpGenerator.flushBuffer():int");
    }

    private int flushMask() {
        int i = 0;
        int i2 = ((this._header == null || this._header.length() <= 0) ? 0 : 4) | ((this._buffer == null || this._buffer.length() <= 0) ? 0 : 2);
        if (this._bypass && this._content != null && this._content.length() > 0) {
            i = 1;
        }
        return i2 | i;
    }

    private void prepareBuffers() {
        int size;
        if (!this._bufferChunked) {
            if (!this._bypass && this._content != null && this._content.length() > 0 && this._buffer != null && this._buffer.space() > 0) {
                int len = this._buffer.put(this._content);
                this._content.skip(len);
                if (this._content.length() == 0) {
                    this._content = null;
                }
            }
            if (this._contentLength == -2) {
                if (this._bypass && ((this._buffer == null || this._buffer.length() == 0) && this._content != null)) {
                    int size2 = this._content.length();
                    this._bufferChunked = true;
                    if (this._header == null) {
                        this._header = this._buffers.getHeader();
                    }
                    if (this._needCRLF) {
                        if (this._header.length() > 0) {
                            throw new IllegalStateException("EOC");
                        }
                        this._header.put(HttpTokens.CRLF);
                        this._needCRLF = false;
                    }
                    BufferUtil.putHexInt(this._header, size2);
                    this._header.put(HttpTokens.CRLF);
                    this._needCRLF = true;
                } else if (this._buffer != null && (size = this._buffer.length()) > 0) {
                    this._bufferChunked = true;
                    if (this._buffer.getIndex() == 12) {
                        this._buffer.poke(this._buffer.getIndex() - 2, HttpTokens.CRLF, 0, 2);
                        this._buffer.setGetIndex(this._buffer.getIndex() - 2);
                        BufferUtil.prependHexInt(this._buffer, size);
                        if (this._needCRLF) {
                            this._buffer.poke(this._buffer.getIndex() - 2, HttpTokens.CRLF, 0, 2);
                            this._buffer.setGetIndex(this._buffer.getIndex() - 2);
                            this._needCRLF = false;
                        }
                    } else {
                        if (this._header == null) {
                            this._header = this._buffers.getHeader();
                        }
                        if (this._needCRLF) {
                            if (this._header.length() > 0) {
                                throw new IllegalStateException("EOC");
                            }
                            this._header.put(HttpTokens.CRLF);
                            this._needCRLF = false;
                        }
                        BufferUtil.putHexInt(this._header, size);
                        this._header.put(HttpTokens.CRLF);
                    }
                    if (this._buffer.space() >= 2) {
                        this._buffer.put(HttpTokens.CRLF);
                    } else {
                        this._needCRLF = true;
                    }
                }
                if (this._needEOC && (this._content == null || this._content.length() == 0)) {
                    if (this._header == null && this._buffer == null) {
                        this._header = this._buffers.getHeader();
                    }
                    if (this._needCRLF) {
                        if (this._buffer == null && this._header != null && this._header.space() >= HttpTokens.CRLF.length) {
                            this._header.put(HttpTokens.CRLF);
                            this._needCRLF = false;
                        } else if (this._buffer != null && this._buffer.space() >= HttpTokens.CRLF.length) {
                            this._buffer.put(HttpTokens.CRLF);
                            this._needCRLF = false;
                        }
                    }
                    if (!this._needCRLF && this._needEOC) {
                        if (this._buffer == null && this._header != null && this._header.space() >= LAST_CHUNK.length) {
                            if (!this._head) {
                                this._header.put(LAST_CHUNK);
                                this._bufferChunked = true;
                            }
                            this._needEOC = false;
                        } else if (this._buffer != null && this._buffer.space() >= LAST_CHUNK.length) {
                            if (!this._head) {
                                this._buffer.put(LAST_CHUNK);
                                this._bufferChunked = true;
                            }
                            this._needEOC = false;
                        }
                    }
                }
            }
        }
        if (this._content != null && this._content.length() == 0) {
            this._content = null;
        }
    }

    public int getBytesBuffered() {
        return (this._header == null ? 0 : this._header.length()) + (this._buffer == null ? 0 : this._buffer.length()) + (this._content != null ? this._content.length() : 0);
    }

    public boolean isEmpty() {
        return (this._header == null || this._header.length() == 0) && (this._buffer == null || this._buffer.length() == 0) && (this._content == null || this._content.length() == 0);
    }

    public String toString() {
        Buffer header = this._header;
        Buffer buffer = this._buffer;
        Buffer content = this._content;
        Object[] objArr = new Object[5];
        objArr[0] = getClass().getSimpleName();
        objArr[1] = Integer.valueOf(this._state);
        objArr[2] = Integer.valueOf(header == null ? -1 : header.length());
        objArr[3] = Integer.valueOf(buffer == null ? -1 : buffer.length());
        objArr[4] = Integer.valueOf(content != null ? content.length() : -1);
        return String.format("%s{s=%d,h=%d,b=%d,c=%d}", objArr);
    }
}
