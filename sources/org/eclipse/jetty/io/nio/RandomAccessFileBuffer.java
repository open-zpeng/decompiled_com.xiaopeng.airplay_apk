package org.eclipse.jetty.io.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import org.eclipse.jetty.io.AbstractBuffer;
import org.eclipse.jetty.io.Buffer;
/* loaded from: classes.dex */
public class RandomAccessFileBuffer extends AbstractBuffer implements Buffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    final int _capacity;
    final FileChannel _channel;
    final RandomAccessFile _file;

    public RandomAccessFileBuffer(File file) throws FileNotFoundException {
        super(2, true);
        this._file = new RandomAccessFile(file, "rw");
        this._channel = this._file.getChannel();
        this._capacity = Integer.MAX_VALUE;
        setGetIndex(0);
        setPutIndex((int) file.length());
    }

    public RandomAccessFileBuffer(File file, int capacity) throws FileNotFoundException {
        super(2, true);
        this._capacity = capacity;
        this._file = new RandomAccessFile(file, "rw");
        this._channel = this._file.getChannel();
        setGetIndex(0);
        setPutIndex((int) file.length());
    }

    public RandomAccessFileBuffer(File file, int capacity, int access) throws FileNotFoundException {
        super(access, true);
        this._capacity = capacity;
        this._file = new RandomAccessFile(file, access == 2 ? "rw" : "r");
        this._channel = this._file.getChannel();
        setGetIndex(0);
        setPutIndex((int) file.length());
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte[] array() {
        return null;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int capacity() {
        return this._capacity;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void clear() {
        try {
            synchronized (this._file) {
                super.clear();
                this._file.setLength(0L);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public byte peek() {
        byte readByte;
        synchronized (this._file) {
            try {
                try {
                    if (this._get != this._file.getFilePointer()) {
                        this._file.seek(this._get);
                    }
                    readByte = this._file.readByte();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return readByte;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte peek(int index) {
        byte readByte;
        synchronized (this._file) {
            try {
                try {
                    this._file.seek(index);
                    readByte = this._file.readByte();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return readByte;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int peek(int index, byte[] b, int offset, int length) {
        int read;
        synchronized (this._file) {
            try {
                try {
                    this._file.seek(index);
                    read = this._file.read(b, offset, length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return read;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void poke(int index, byte b) {
        synchronized (this._file) {
            try {
                try {
                    this._file.seek(index);
                    this._file.writeByte(b);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, byte[] b, int offset, int length) {
        synchronized (this._file) {
            try {
                try {
                    this._file.seek(index);
                    this._file.write(b, offset, length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return length;
    }

    public int writeTo(WritableByteChannel channel, int index, int length) throws IOException {
        int transferTo;
        synchronized (this._file) {
            transferTo = (int) this._channel.transferTo(index, length, channel);
        }
        return transferTo;
    }
}
