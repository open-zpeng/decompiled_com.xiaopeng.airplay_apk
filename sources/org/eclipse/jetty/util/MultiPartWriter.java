package org.eclipse.jetty.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
/* loaded from: classes.dex */
public class MultiPartWriter extends FilterWriter {
    public static String MULTIPART_MIXED = MultiPartOutputStream.MULTIPART_MIXED;
    public static String MULTIPART_X_MIXED_REPLACE = MultiPartOutputStream.MULTIPART_X_MIXED_REPLACE;
    private static final String __CRLF = "\r\n";
    private static final String __DASHDASH = "--";
    private String boundary;
    private boolean inPart;

    public MultiPartWriter(Writer out) throws IOException {
        super(out);
        this.inPart = false;
        this.boundary = "jetty" + System.identityHashCode(this) + Long.toString(System.currentTimeMillis(), 36);
        this.inPart = false;
    }

    @Override // java.io.FilterWriter, java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.inPart) {
            this.out.write("\r\n");
        }
        this.out.write(__DASHDASH);
        this.out.write(this.boundary);
        this.out.write(__DASHDASH);
        this.out.write("\r\n");
        this.inPart = false;
        super.close();
    }

    public String getBoundary() {
        return this.boundary;
    }

    public void startPart(String contentType) throws IOException {
        if (this.inPart) {
            this.out.write("\r\n");
        }
        this.out.write(__DASHDASH);
        this.out.write(this.boundary);
        this.out.write("\r\n");
        this.out.write("Content-Type: ");
        this.out.write(contentType);
        this.out.write("\r\n");
        this.out.write("\r\n");
        this.inPart = true;
    }

    public void endPart() throws IOException {
        if (this.inPart) {
            this.out.write("\r\n");
        }
        this.inPart = false;
    }

    public void startPart(String contentType, String[] headers) throws IOException {
        if (this.inPart) {
            this.out.write("\r\n");
        }
        this.out.write(__DASHDASH);
        this.out.write(this.boundary);
        this.out.write("\r\n");
        this.out.write("Content-Type: ");
        this.out.write(contentType);
        this.out.write("\r\n");
        for (int i = 0; headers != null && i < headers.length; i++) {
            this.out.write(headers[i]);
            this.out.write("\r\n");
        }
        this.out.write("\r\n");
        this.inPart = true;
    }
}
