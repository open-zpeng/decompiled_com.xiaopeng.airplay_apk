package org.eclipse.jetty.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jetty.http.HttpTokens;
/* loaded from: classes.dex */
public class MultiPartOutputStream extends FilterOutputStream {
    private String boundary;
    private byte[] boundaryBytes;
    private boolean inPart;
    private static final byte[] __CRLF = {HttpTokens.CARRIAGE_RETURN, 10};
    private static final byte[] __DASHDASH = {45, 45};
    public static String MULTIPART_MIXED = "multipart/mixed";
    public static String MULTIPART_X_MIXED_REPLACE = "multipart/x-mixed-replace";

    public MultiPartOutputStream(OutputStream out) throws IOException {
        super(out);
        this.inPart = false;
        this.boundary = "jetty" + System.identityHashCode(this) + Long.toString(System.currentTimeMillis(), 36);
        this.boundaryBytes = this.boundary.getBytes(StringUtil.__ISO_8859_1);
        this.inPart = false;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.inPart) {
            this.out.write(__CRLF);
        }
        this.out.write(__DASHDASH);
        this.out.write(this.boundaryBytes);
        this.out.write(__DASHDASH);
        this.out.write(__CRLF);
        this.inPart = false;
        super.close();
    }

    public String getBoundary() {
        return this.boundary;
    }

    public OutputStream getOut() {
        return this.out;
    }

    public void startPart(String contentType) throws IOException {
        if (this.inPart) {
            this.out.write(__CRLF);
        }
        this.inPart = true;
        this.out.write(__DASHDASH);
        this.out.write(this.boundaryBytes);
        this.out.write(__CRLF);
        if (contentType != null) {
            OutputStream outputStream = this.out;
            outputStream.write(("Content-Type: " + contentType).getBytes(StringUtil.__ISO_8859_1));
        }
        this.out.write(__CRLF);
        this.out.write(__CRLF);
    }

    public void startPart(String contentType, String[] headers) throws IOException {
        if (this.inPart) {
            this.out.write(__CRLF);
        }
        this.inPart = true;
        this.out.write(__DASHDASH);
        this.out.write(this.boundaryBytes);
        this.out.write(__CRLF);
        if (contentType != null) {
            OutputStream outputStream = this.out;
            outputStream.write(("Content-Type: " + contentType).getBytes(StringUtil.__ISO_8859_1));
        }
        this.out.write(__CRLF);
        for (int i = 0; headers != null && i < headers.length; i++) {
            this.out.write(headers[i].getBytes(StringUtil.__ISO_8859_1));
            this.out.write(__CRLF);
        }
        this.out.write(__CRLF);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }
}
