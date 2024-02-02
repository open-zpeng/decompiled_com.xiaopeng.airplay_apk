package org.eclipse.jetty.io.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.eclipse.jetty.io.AbstractBuffer;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class DirectNIOBuffer extends AbstractBuffer implements NIOBuffer {
    private static final Logger LOG = Log.getLogger(DirectNIOBuffer.class);
    protected final ByteBuffer _buf;
    private ReadableByteChannel _in;
    private InputStream _inStream;
    private WritableByteChannel _out;
    private OutputStream _outStream;

    public DirectNIOBuffer(int size) {
        super(2, false);
        this._buf = ByteBuffer.allocateDirect(size);
        this._buf.position(0);
        this._buf.limit(this._buf.capacity());
    }

    public DirectNIOBuffer(ByteBuffer buffer, boolean immutable) {
        super(immutable ? 0 : 2, false);
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException();
        }
        this._buf = buffer;
        setGetIndex(buffer.position());
        setPutIndex(buffer.limit());
    }

    public DirectNIOBuffer(File file) throws IOException {
        super(1, false);
        FileInputStream fis = null;
        FileChannel fc = null;
        try {
            fis = new FileInputStream(file);
            fc = fis.getChannel();
            this._buf = fc.map(FileChannel.MapMode.READ_ONLY, 0L, file.length());
            setGetIndex(0);
            setPutIndex((int) file.length());
            this._access = 0;
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException e) {
                    LOG.ignore(e);
                }
            }
            IO.close((InputStream) fis);
        }
    }

    @Override // org.eclipse.jetty.io.nio.NIOBuffer
    public boolean isDirect() {
        return true;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte[] array() {
        return null;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int capacity() {
        return this._buf.capacity();
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte peek(int position) {
        return this._buf.get(position);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int peek(int index, byte[] b, int offset, int length) {
        int l = length;
        if ((index + l <= capacity() || (l = capacity() - index) != 0) && l >= 0) {
            try {
                this._buf.position(index);
                this._buf.get(b, offset, l);
                return l;
            } finally {
                this._buf.position(0);
            }
        }
        return -1;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void poke(int index, byte b) {
        if (isReadOnly()) {
            throw new IllegalStateException("READONLY");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index<0: " + index + "<0");
        } else if (index > capacity()) {
            throw new IllegalArgumentException("index>capacity(): " + index + ">" + capacity());
        } else {
            this._buf.put(index, b);
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, Buffer src) {
        if (isReadOnly()) {
            throw new IllegalStateException("READONLY");
        }
        byte[] array = src.array();
        if (array != null) {
            return poke(index, array, src.getIndex(), src.length());
        }
        Buffer src_buf = src.buffer();
        if (src_buf instanceof DirectNIOBuffer) {
            ByteBuffer src_bytebuf = ((DirectNIOBuffer) src_buf)._buf;
            if (src_bytebuf == this._buf) {
                src_bytebuf = this._buf.duplicate();
            }
            try {
                this._buf.position(index);
                int space = this._buf.remaining();
                int length = src.length();
                if (length > space) {
                    length = space;
                }
                src_bytebuf.position(src.getIndex());
                src_bytebuf.limit(src.getIndex() + length);
                this._buf.put(src_bytebuf);
                return length;
            } finally {
                this._buf.position(0);
                src_bytebuf.limit(src_bytebuf.capacity());
                src_bytebuf.position(0);
            }
        }
        return super.poke(index, src);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, byte[] b, int offset, int length) {
        if (isReadOnly()) {
            throw new IllegalStateException("READONLY");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index<0: " + index + "<0");
        } else if (index + length > capacity() && (length = capacity() - index) < 0) {
            throw new IllegalArgumentException("index>capacity(): " + index + ">" + capacity());
        } else {
            try {
                this._buf.position(index);
                int space = this._buf.remaining();
                if (length > space) {
                    length = space;
                }
                if (length > 0) {
                    this._buf.put(b, offset, length);
                }
                return length;
            } finally {
                this._buf.position(0);
            }
        }
    }

    @Override // org.eclipse.jetty.io.nio.NIOBuffer
    public ByteBuffer getByteBuffer() {
        return this._buf;
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0049, code lost:
        r9._in = null;
        r9._inStream = r10;
     */
    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public int readFrom(java.io.InputStream r10, int r11) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 217
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.io.nio.DirectNIOBuffer.readFrom(java.io.InputStream, int):int");
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void writeTo(OutputStream out) throws IOException {
        if (this._out == null || !this._out.isOpen() || out != this._outStream) {
            this._out = Channels.newChannel(out);
            this._outStream = out;
        }
        synchronized (this._buf) {
            int loop = 0;
            while (hasContent() && this._out.isOpen()) {
                try {
                    this._buf.position(getIndex());
                    this._buf.limit(putIndex());
                    int len = this._out.write(this._buf);
                    if (len < 0) {
                        break;
                    } else if (len > 0) {
                        skip(len);
                        loop = 0;
                    } else {
                        int loop2 = loop + 1;
                        if (loop > 1) {
                            break;
                        }
                        loop = loop2;
                    }
                } catch (IOException e) {
                    this._out = null;
                    this._outStream = null;
                    throw e;
                }
            }
            if (this._out != null && !this._out.isOpen()) {
                this._out = null;
                this._outStream = null;
            }
            this._buf.position(0);
            this._buf.limit(this._buf.capacity());
        }
    }
}
