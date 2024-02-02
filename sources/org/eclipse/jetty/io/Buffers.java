package org.eclipse.jetty.io;
/* loaded from: classes.dex */
public interface Buffers {

    /* loaded from: classes.dex */
    public enum Type {
        BYTE_ARRAY,
        DIRECT,
        INDIRECT
    }

    Buffer getBuffer();

    Buffer getBuffer(int i);

    Buffer getHeader();

    void returnBuffer(Buffer buffer);
}
