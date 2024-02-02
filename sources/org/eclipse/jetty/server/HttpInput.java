package org.eclipse.jetty.server;

import java.io.IOException;
import javax.servlet.ServletInputStream;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.EofException;
/* loaded from: classes.dex */
public class HttpInput extends ServletInputStream {
    protected final AbstractHttpConnection _connection;
    protected final HttpParser _parser;

    public HttpInput(AbstractHttpConnection connection) {
        this._connection = connection;
        this._parser = (HttpParser) connection.getParser();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        byte[] bytes = new byte[1];
        int read = read(bytes, 0, 1);
        if (read < 0) {
            return -1;
        }
        return bytes[0] & 255;
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        Buffer content = this._parser.blockForContent(this._connection.getMaxIdleTime());
        if (content != null) {
            int l = content.get(b, off, len);
            return l;
        } else if (!this._connection.isEarlyEOF()) {
            return -1;
        } else {
            throw new EofException("early EOF");
        }
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return this._parser.available();
    }
}
