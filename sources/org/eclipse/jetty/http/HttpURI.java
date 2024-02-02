package org.eclipse.jetty.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.Utf8StringBuilder;
/* loaded from: classes.dex */
public class HttpURI {
    private static final int ASTERISK = 10;
    private static final int AUTH = 4;
    private static final int AUTH_OR_PATH = 1;
    private static final int IPV6 = 5;
    private static final int PARAM = 8;
    private static final int PATH = 7;
    private static final int PORT = 6;
    private static final int QUERY = 9;
    private static final int SCHEME_OR_PATH = 2;
    private static final int START = 0;
    private static final byte[] __empty = new byte[0];
    int _authority;
    boolean _encoded;
    int _end;
    int _fragment;
    int _host;
    int _param;
    boolean _partial;
    int _path;
    int _port;
    int _portValue;
    int _query;
    byte[] _raw;
    String _rawString;
    int _scheme;
    final Utf8StringBuilder _utf8b;

    public HttpURI() {
        this._partial = false;
        this._raw = __empty;
        this._encoded = false;
        this._utf8b = new Utf8StringBuilder(64);
    }

    public HttpURI(boolean parsePartialAuth) {
        this._partial = false;
        this._raw = __empty;
        this._encoded = false;
        this._utf8b = new Utf8StringBuilder(64);
        this._partial = parsePartialAuth;
    }

    public HttpURI(String raw) {
        this._partial = false;
        this._raw = __empty;
        this._encoded = false;
        this._utf8b = new Utf8StringBuilder(64);
        this._rawString = raw;
        try {
            byte[] b = raw.getBytes(StringUtil.__UTF8);
            parse(b, 0, b.length);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public HttpURI(byte[] raw, int offset, int length) {
        this._partial = false;
        this._raw = __empty;
        this._encoded = false;
        this._utf8b = new Utf8StringBuilder(64);
        parse2(raw, offset, length);
    }

    public HttpURI(URI uri) {
        this._partial = false;
        this._raw = __empty;
        this._encoded = false;
        this._utf8b = new Utf8StringBuilder(64);
        parse(uri.toASCIIString());
    }

    public void parse(String raw) {
        byte[] b = raw.getBytes();
        parse2(b, 0, b.length);
        this._rawString = raw;
    }

    public void parse(byte[] raw, int offset, int length) {
        this._rawString = null;
        parse2(raw, offset, length);
    }

    public void parseConnect(byte[] raw, int offset, int length) {
        this._rawString = null;
        this._encoded = false;
        this._raw = raw;
        int i = offset;
        int e = offset + length;
        int state = 4;
        this._end = offset + length;
        this._scheme = offset;
        this._authority = offset;
        this._host = offset;
        this._port = this._end;
        this._portValue = -1;
        this._path = this._end;
        this._param = this._end;
        this._query = this._end;
        this._fragment = this._end;
        while (true) {
            if (i < e) {
                char c = (char) (255 & this._raw[i]);
                int i2 = i + 1;
                switch (state) {
                    case 4:
                        if (c == ':') {
                            this._port = i;
                            break;
                        } else if (c == '[') {
                            state = 5;
                            break;
                        } else {
                            break;
                        }
                    case 5:
                        if (c == '/') {
                            throw new IllegalArgumentException("No closing ']' for " + StringUtil.toString(this._raw, offset, length, URIUtil.__CHARSET));
                        } else if (c == ']') {
                            state = 4;
                            break;
                        } else {
                            break;
                        }
                }
                i = i2;
            }
        }
        if (this._port < this._path) {
            this._portValue = TypeUtil.parseInt(this._raw, this._port + 1, (this._path - this._port) - 1, 10);
            this._path = offset;
            return;
        }
        throw new IllegalArgumentException("No port");
    }

    private void parse2(byte[] raw, int offset, int length) {
        this._encoded = false;
        this._raw = raw;
        int s = offset;
        int e = offset + length;
        int state = 0;
        int m = offset;
        this._end = offset + length;
        this._scheme = offset;
        this._authority = offset;
        this._host = offset;
        this._port = offset;
        this._portValue = -1;
        this._path = offset;
        this._param = this._end;
        this._query = this._end;
        this._fragment = this._end;
        while (s < e) {
            char c = (char) (this._raw[s] & 255);
            int i = s + 1;
            switch (state) {
                case 0:
                    m = s;
                    if (c == '#') {
                        this._param = s;
                        this._query = s;
                        this._fragment = s;
                        break;
                    } else if (c == '*') {
                        this._path = s;
                        state = 10;
                        break;
                    } else if (c != '/') {
                        if (c == ';') {
                            this._param = s;
                            state = 8;
                            break;
                        } else if (c == '?') {
                            this._param = s;
                            this._query = s;
                            state = 9;
                            break;
                        } else {
                            state = 2;
                            break;
                        }
                    } else {
                        state = 1;
                        break;
                    }
                case 1:
                    if ((this._partial || this._scheme != this._authority) && c == '/') {
                        this._host = i;
                        this._port = this._end;
                        this._path = this._end;
                        state = 4;
                        break;
                    } else if (c == ';' || c == '?' || c == '#') {
                        state = 7;
                        s = i - 1;
                        continue;
                    } else {
                        this._host = m;
                        this._port = m;
                        state = 7;
                        break;
                    }
                    break;
                case 2:
                    if (length > 6 && c == 't') {
                        if (this._raw[offset + 3] == 58) {
                            s = offset + 3;
                            i = offset + 4;
                            c = ':';
                        } else if (this._raw[offset + 4] == 58) {
                            s = offset + 4;
                            i = offset + 5;
                            c = ':';
                        } else if (this._raw[offset + 5] == 58) {
                            s = offset + 5;
                            i = offset + 6;
                            c = ':';
                        }
                    }
                    if (c == '#') {
                        this._param = s;
                        this._query = s;
                        this._fragment = s;
                        break;
                    } else if (c == '/') {
                        state = 7;
                        break;
                    } else if (c != '?') {
                        switch (c) {
                            case HttpHeaders.PROXY_CONNECTION_ORDINAL /* 58 */:
                                int i2 = i + 1;
                                m = i;
                                this._authority = m;
                                this._path = m;
                                if (((char) (this._raw[i2] & 255)) == '/') {
                                    state = 1;
                                } else {
                                    this._host = m;
                                    this._port = m;
                                    state = 7;
                                }
                                i = i2;
                                break;
                            case HttpHeaders.X_FORWARDED_PROTO_ORDINAL /* 59 */:
                                this._param = s;
                                state = 8;
                                break;
                        }
                    } else {
                        this._param = s;
                        this._query = s;
                        state = 9;
                        break;
                    }
                case 4:
                    if (c == '/') {
                        m = s;
                        this._path = m;
                        this._port = this._path;
                        state = 7;
                        break;
                    } else if (c == ':') {
                        this._port = s;
                        state = 6;
                        break;
                    } else if (c == '@') {
                        this._host = i;
                        break;
                    } else if (c == '[') {
                        state = 5;
                        break;
                    }
                    break;
                case 5:
                    if (c == '/') {
                        throw new IllegalArgumentException("No closing ']' for " + StringUtil.toString(this._raw, offset, length, URIUtil.__CHARSET));
                    } else if (c == ']') {
                        state = 4;
                        break;
                    }
                    break;
                case 6:
                    if (c == '/') {
                        m = s;
                        this._path = m;
                        if (this._port <= this._authority) {
                            this._port = this._path;
                        }
                        state = 7;
                        break;
                    }
                    break;
                case 7:
                    if (c == '#') {
                        this._param = s;
                        this._query = s;
                        this._fragment = s;
                        break;
                    } else if (c != '%') {
                        if (c == ';') {
                            this._param = s;
                            state = 8;
                            break;
                        } else if (c == '?') {
                            this._param = s;
                            this._query = s;
                            state = 9;
                            break;
                        }
                    } else {
                        this._encoded = true;
                        break;
                    }
                    break;
                case 8:
                    if (c == '#') {
                        this._query = s;
                        this._fragment = s;
                        break;
                    } else if (c == '?') {
                        this._query = s;
                        state = 9;
                        break;
                    }
                    break;
                case 9:
                    if (c == '#') {
                        this._fragment = s;
                        break;
                    }
                    break;
                case 10:
                    throw new IllegalArgumentException("only '*'");
            }
            s = i;
        }
        if (this._port < this._path) {
            this._portValue = TypeUtil.parseInt(this._raw, this._port + 1, (this._path - this._port) - 1, 10);
        }
    }

    private String toUtf8String(int offset, int length) {
        this._utf8b.reset();
        this._utf8b.append(this._raw, offset, length);
        return this._utf8b.toString();
    }

    public String getScheme() {
        if (this._scheme == this._authority) {
            return null;
        }
        int l = this._authority - this._scheme;
        if (l == 5 && this._raw[this._scheme] == 104 && this._raw[this._scheme + 1] == 116 && this._raw[this._scheme + 2] == 116 && this._raw[this._scheme + 3] == 112) {
            return "http";
        }
        if (l == 6 && this._raw[this._scheme] == 104 && this._raw[this._scheme + 1] == 116 && this._raw[this._scheme + 2] == 116 && this._raw[this._scheme + 3] == 112 && this._raw[this._scheme + 4] == 115) {
            return "https";
        }
        return toUtf8String(this._scheme, (this._authority - this._scheme) - 1);
    }

    public String getAuthority() {
        if (this._authority == this._path) {
            return null;
        }
        return toUtf8String(this._authority, this._path - this._authority);
    }

    public String getHost() {
        if (this._host == this._port) {
            return null;
        }
        return toUtf8String(this._host, this._port - this._host);
    }

    public int getPort() {
        return this._portValue;
    }

    public String getPath() {
        if (this._path == this._param) {
            return null;
        }
        return toUtf8String(this._path, this._param - this._path);
    }

    public String getDecodedPath() {
        if (this._path == this._param) {
            return null;
        }
        int length = this._param - this._path;
        boolean decoding = false;
        int i = this._path;
        while (i < this._param) {
            byte b = this._raw[i];
            if (b == 37) {
                if (!decoding) {
                    this._utf8b.reset();
                    this._utf8b.append(this._raw, this._path, i - this._path);
                    decoding = true;
                }
                if (i + 2 >= this._param) {
                    throw new IllegalArgumentException("Bad % encoding: " + this);
                } else if (this._raw[i + 1] == 117) {
                    if (i + 5 >= this._param) {
                        throw new IllegalArgumentException("Bad %u encoding: " + this);
                    }
                    try {
                        String unicode = new String(Character.toChars(TypeUtil.parseInt(this._raw, i + 2, 4, 16)));
                        this._utf8b.getStringBuilder().append(unicode);
                        i += 5;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    this._utf8b.append((byte) (255 & TypeUtil.parseInt(this._raw, i + 1, 2, 16)));
                    i += 2;
                }
            } else if (decoding) {
                this._utf8b.append(b);
            }
            i++;
        }
        if (!decoding) {
            return toUtf8String(this._path, length);
        }
        return this._utf8b.toString();
    }

    public String getDecodedPath(String encoding) {
        int n;
        if (this._path == this._param) {
            return null;
        }
        int length = this._param - this._path;
        byte[] bytes = null;
        int n2 = 0;
        int i = this._path;
        while (i < this._param) {
            byte b = this._raw[i];
            if (b == 37) {
                if (bytes == null) {
                    bytes = new byte[length];
                    System.arraycopy(this._raw, this._path, bytes, 0, n2);
                }
                if (i + 2 >= this._param) {
                    throw new IllegalArgumentException("Bad % encoding: " + this);
                } else if (this._raw[i + 1] == 117) {
                    if (i + 5 >= this._param) {
                        throw new IllegalArgumentException("Bad %u encoding: " + this);
                    }
                    try {
                        String unicode = new String(Character.toChars(TypeUtil.parseInt(this._raw, i + 2, 4, 16)));
                        byte[] encoded = unicode.getBytes(encoding);
                        System.arraycopy(encoded, 0, bytes, n2, encoded.length);
                        n2 += encoded.length;
                        i += 5;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    n = n2 + 1;
                    bytes[n2] = (byte) (255 & TypeUtil.parseInt(this._raw, i + 1, 2, 16));
                    i += 2;
                    n2 = n;
                }
            } else if (bytes == null) {
                n2++;
            } else {
                n = n2 + 1;
                bytes[n2] = b;
                n2 = n;
            }
            i++;
        }
        if (bytes == null) {
            return StringUtil.toString(this._raw, this._path, this._param - this._path, encoding);
        }
        return StringUtil.toString(bytes, 0, n2, encoding);
    }

    public String getPathAndParam() {
        if (this._path == this._query) {
            return null;
        }
        return toUtf8String(this._path, this._query - this._path);
    }

    public String getCompletePath() {
        if (this._path == this._end) {
            return null;
        }
        return toUtf8String(this._path, this._end - this._path);
    }

    public String getParam() {
        if (this._param == this._query) {
            return null;
        }
        return toUtf8String(this._param + 1, (this._query - this._param) - 1);
    }

    public String getQuery() {
        if (this._query == this._fragment) {
            return null;
        }
        return toUtf8String(this._query + 1, (this._fragment - this._query) - 1);
    }

    public String getQuery(String encoding) {
        if (this._query == this._fragment) {
            return null;
        }
        return StringUtil.toString(this._raw, this._query + 1, (this._fragment - this._query) - 1, encoding);
    }

    public boolean hasQuery() {
        return this._fragment > this._query;
    }

    public String getFragment() {
        if (this._fragment == this._end) {
            return null;
        }
        return toUtf8String(this._fragment + 1, (this._end - this._fragment) - 1);
    }

    public void decodeQueryTo(MultiMap parameters) {
        if (this._query == this._fragment) {
            return;
        }
        this._utf8b.reset();
        UrlEncoded.decodeUtf8To(this._raw, this._query + 1, (this._fragment - this._query) - 1, parameters, this._utf8b);
    }

    public void decodeQueryTo(MultiMap parameters, String encoding) throws UnsupportedEncodingException {
        if (this._query == this._fragment) {
            return;
        }
        if (encoding == null || StringUtil.isUTF8(encoding)) {
            UrlEncoded.decodeUtf8To(this._raw, this._query + 1, (this._fragment - this._query) - 1, parameters);
        } else {
            UrlEncoded.decodeTo(StringUtil.toString(this._raw, this._query + 1, (this._fragment - this._query) - 1, encoding), parameters, encoding);
        }
    }

    public void clear() {
        this._end = 0;
        this._fragment = 0;
        this._query = 0;
        this._param = 0;
        this._path = 0;
        this._port = 0;
        this._host = 0;
        this._authority = 0;
        this._scheme = 0;
        this._raw = __empty;
        this._rawString = "";
        this._encoded = false;
    }

    public String toString() {
        if (this._rawString == null) {
            this._rawString = toUtf8String(this._scheme, this._end - this._scheme);
        }
        return this._rawString;
    }

    public void writeTo(Utf8StringBuilder buf) {
        buf.append(this._raw, this._scheme, this._end - this._scheme);
    }
}
