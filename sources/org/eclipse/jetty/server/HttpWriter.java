package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.eclipse.jetty.http.AbstractGenerator;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class HttpWriter extends Writer {
    public static final int MAX_OUTPUT_CHARS = 512;
    private static final int WRITE_CONV = 0;
    private static final int WRITE_ISO1 = 1;
    private static final int WRITE_UTF8 = 2;
    final AbstractGenerator _generator;
    final HttpOutput _out;
    int _surrogate = 0;
    int _writeMode;

    public HttpWriter(HttpOutput out) {
        this._out = out;
        this._generator = this._out._generator;
    }

    public void setCharacterEncoding(String encoding) {
        if (encoding == null || StringUtil.__ISO_8859_1.equalsIgnoreCase(encoding)) {
            this._writeMode = 1;
        } else if (StringUtil.__UTF8.equalsIgnoreCase(encoding)) {
            this._writeMode = 2;
        } else {
            this._writeMode = 0;
            if (this._out._characterEncoding == null || !this._out._characterEncoding.equalsIgnoreCase(encoding)) {
                this._out._converter = null;
            }
        }
        this._out._characterEncoding = encoding;
        if (this._out._bytes == null) {
            this._out._bytes = new ByteArrayOutputStream2(MAX_OUTPUT_CHARS);
        }
    }

    @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this._out.close();
    }

    @Override // java.io.Writer, java.io.Flushable
    public void flush() throws IOException {
        this._out.flush();
    }

    @Override // java.io.Writer
    public void write(String s, int offset, int length) throws IOException {
        while (length > 512) {
            write(s, offset, MAX_OUTPUT_CHARS);
            offset += MAX_OUTPUT_CHARS;
            length -= 512;
        }
        if (this._out._chars == null) {
            this._out._chars = new char[MAX_OUTPUT_CHARS];
        }
        char[] chars = this._out._chars;
        s.getChars(offset, offset + length, chars, 0);
        write(chars, 0, length);
    }

    /* JADX WARN: Removed duplicated region for block: B:100:0x017c A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x0179 A[SYNTHETIC] */
    @Override // java.io.Writer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void write(char[] r13, int r14, int r15) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 470
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.HttpWriter.write(char[], int, int):void");
    }

    private Writer getConverter() throws IOException {
        if (this._out._converter == null) {
            this._out._converter = new OutputStreamWriter(this._out._bytes, this._out._characterEncoding);
        }
        return this._out._converter;
    }
}
