package org.eclipse.jetty.http;

import java.io.IOException;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.View;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractGenerator implements Generator {
    private static final Logger LOG = Log.getLogger(AbstractGenerator.class);
    public static final byte[] NO_BYTES = new byte[0];
    public static final int STATE_CONTENT = 2;
    public static final int STATE_END = 4;
    public static final int STATE_FLUSHING = 3;
    public static final int STATE_HEADER = 0;
    protected Buffer _buffer;
    protected final Buffers _buffers;
    protected Buffer _content;
    protected Buffer _date;
    protected final EndPoint _endp;
    protected Buffer _header;
    protected Buffer _method;
    protected Buffer _reason;
    private boolean _sendServerVersion;
    protected String _uri;
    protected int _state = 0;
    protected int _status = 0;
    protected int _version = 11;
    protected long _contentWritten = 0;
    protected long _contentLength = -3;
    protected boolean _last = false;
    protected boolean _head = false;
    protected boolean _noContent = false;
    protected Boolean _persistent = null;

    @Override // org.eclipse.jetty.http.Generator
    public abstract void completeHeader(HttpFields httpFields, boolean z) throws IOException;

    @Override // org.eclipse.jetty.http.Generator
    public abstract int flushBuffer() throws IOException;

    public abstract boolean isRequest();

    public abstract boolean isResponse();

    public abstract int prepareUncheckedAddContent() throws IOException;

    public AbstractGenerator(Buffers buffers, EndPoint io) {
        this._buffers = buffers;
        this._endp = io;
    }

    public boolean isOpen() {
        return this._endp.isOpen();
    }

    @Override // org.eclipse.jetty.http.Generator
    public void reset() {
        this._state = 0;
        this._status = 0;
        this._version = 11;
        this._reason = null;
        this._last = false;
        this._head = false;
        this._noContent = false;
        this._persistent = null;
        this._contentWritten = 0L;
        this._contentLength = -3L;
        this._date = null;
        this._content = null;
        this._method = null;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void returnBuffers() {
        if (this._buffer != null && this._buffer.length() == 0) {
            this._buffers.returnBuffer(this._buffer);
            this._buffer = null;
        }
        if (this._header != null && this._header.length() == 0) {
            this._buffers.returnBuffer(this._header);
            this._header = null;
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public void resetBuffer() {
        if (this._state >= 3) {
            throw new IllegalStateException("Flushed");
        }
        this._last = false;
        this._persistent = null;
        this._contentWritten = 0L;
        this._contentLength = -3L;
        this._content = null;
        if (this._buffer != null) {
            this._buffer.clear();
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public int getContentBufferSize() {
        if (this._buffer == null) {
            this._buffer = this._buffers.getBuffer();
        }
        return this._buffer.capacity();
    }

    @Override // org.eclipse.jetty.http.Generator
    public void increaseContentBufferSize(int contentBufferSize) {
        if (this._buffer == null) {
            this._buffer = this._buffers.getBuffer();
        }
        if (contentBufferSize > this._buffer.capacity()) {
            Buffer nb = this._buffers.getBuffer(contentBufferSize);
            nb.put(this._buffer);
            this._buffers.returnBuffer(this._buffer);
            this._buffer = nb;
        }
    }

    public Buffer getUncheckedBuffer() {
        return this._buffer;
    }

    public boolean getSendServerVersion() {
        return this._sendServerVersion;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setSendServerVersion(boolean sendServerVersion) {
        this._sendServerVersion = sendServerVersion;
    }

    public int getState() {
        return this._state;
    }

    public boolean isState(int state) {
        return this._state == state;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isComplete() {
        return this._state == 4;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isIdle() {
        return this._state == 0 && this._method == null && this._status == 0;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isCommitted() {
        return this._state != 0;
    }

    public boolean isHead() {
        return this._head;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setContentLength(long value) {
        if (value < 0) {
            this._contentLength = -3L;
        } else {
            this._contentLength = value;
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setHead(boolean head) {
        this._head = head;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isPersistent() {
        return this._persistent != null ? this._persistent.booleanValue() : isRequest() || this._version > 10;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setPersistent(boolean persistent) {
        this._persistent = Boolean.valueOf(persistent);
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setVersion(int version) {
        if (this._state != 0) {
            throw new IllegalStateException("STATE!=START " + this._state);
        }
        this._version = version;
        if (this._version == 9 && this._method != null) {
            this._noContent = true;
        }
    }

    public int getVersion() {
        return this._version;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setDate(Buffer timeStampBuffer) {
        this._date = timeStampBuffer;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setRequest(String method, String uri) {
        if (method == null || HttpMethods.GET.equals(method)) {
            this._method = HttpMethods.GET_BUFFER;
        } else {
            this._method = HttpMethods.CACHE.lookup(method);
        }
        this._uri = uri;
        if (this._version == 9) {
            this._noContent = true;
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public void setResponse(int status, String reason) {
        if (this._state != 0) {
            throw new IllegalStateException("STATE!=START");
        }
        this._method = null;
        this._status = status;
        if (reason != null) {
            byte[] iso8859 = StringUtil.getBytes(reason);
            int len = iso8859.length;
            if (len > 1024) {
                len = 1024;
            }
            this._reason = new ByteArrayBuffer(len);
            for (int i = 0; i < len; i++) {
                byte b = iso8859[i];
                if (b != 13 && b != 10) {
                    this._reason.put(b);
                } else {
                    this._reason.put(HttpTokens.SPACE);
                }
            }
        }
    }

    void uncheckedAddContent(int b) {
        this._buffer.put((byte) b);
    }

    public void completeUncheckedAddContent() {
        if (this._noContent) {
            if (this._buffer != null) {
                this._buffer.clear();
                return;
            }
            return;
        }
        this._contentWritten += this._buffer.length();
        if (this._head) {
            this._buffer.clear();
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isBufferFull() {
        if (this._buffer == null || this._buffer.space() != 0) {
            return this._content != null && this._content.length() > 0;
        }
        if (this._buffer.length() == 0 && !this._buffer.isImmutable()) {
            this._buffer.compact();
        }
        return this._buffer.space() == 0;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isWritten() {
        return this._contentWritten > 0;
    }

    @Override // org.eclipse.jetty.http.Generator
    public boolean isAllContentWritten() {
        return this._contentLength >= 0 && this._contentWritten >= this._contentLength;
    }

    @Override // org.eclipse.jetty.http.Generator
    public void complete() throws IOException {
        if (this._state == 0) {
            throw new IllegalStateException("State==HEADER");
        }
        if (this._contentLength >= 0 && this._contentLength != this._contentWritten && !this._head) {
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("ContentLength written==" + this._contentWritten + " != contentLength==" + this._contentLength, new Object[0]);
            }
            this._persistent = false;
        }
    }

    public void flush(long maxIdleTime) throws IOException {
        long now = System.currentTimeMillis();
        long end = now + maxIdleTime;
        Buffer content = this._content;
        Buffer buffer = this._buffer;
        if ((content != null && content.length() > 0) || ((buffer != null && buffer.length() > 0) || isBufferFull())) {
            flushBuffer();
            while (now < end) {
                if ((content != null && content.length() > 0) || (buffer != null && buffer.length() > 0)) {
                    if (!this._endp.isOpen() || this._endp.isOutputShutdown()) {
                        throw new EofException();
                    }
                    blockForOutput(end - now);
                    now = System.currentTimeMillis();
                } else {
                    return;
                }
            }
        }
    }

    @Override // org.eclipse.jetty.http.Generator
    public void sendError(int code, String reason, String content, boolean close) throws IOException {
        String str;
        if (close) {
            this._persistent = false;
        }
        if (isCommitted()) {
            LOG.debug("sendError on committed: {} {}", Integer.valueOf(code), reason);
            return;
        }
        LOG.debug("sendError: {} {}", Integer.valueOf(code), reason);
        setResponse(code, reason);
        if (content != null) {
            completeHeader(null, false);
            addContent(new View(new ByteArrayBuffer(content)), true);
        } else if (code >= 400) {
            completeHeader(null, false);
            StringBuilder sb = new StringBuilder();
            sb.append("Error: ");
            if (reason == null) {
                str = "" + code;
            } else {
                str = reason;
            }
            sb.append(str);
            addContent(new View(new ByteArrayBuffer(sb.toString())), true);
        } else {
            completeHeader(null, true);
        }
        complete();
    }

    @Override // org.eclipse.jetty.http.Generator
    public long getContentWritten() {
        return this._contentWritten;
    }

    public void blockForOutput(long maxIdleTime) throws IOException {
        if (this._endp.isBlocking()) {
            try {
                flushBuffer();
            } catch (IOException e) {
                this._endp.close();
                throw e;
            }
        } else if (!this._endp.blockWritable(maxIdleTime)) {
            this._endp.close();
            throw new EofException("timeout");
        } else {
            flushBuffer();
        }
    }
}
