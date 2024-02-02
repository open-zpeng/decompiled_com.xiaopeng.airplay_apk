package org.eclipse.jetty.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.eclipse.jetty.server.HttpWriter;
/* loaded from: classes.dex */
public class QuotedStringTokenizer extends StringTokenizer {
    private static final String __delim = "\t\n\r";
    private static final char[] escapes = new char[32];
    private String _delim;
    private boolean _double;
    private boolean _hasToken;
    private int _i;
    private int _lastStart;
    private boolean _returnDelimiters;
    private boolean _returnQuotes;
    private boolean _single;
    private String _string;
    private StringBuffer _token;

    public QuotedStringTokenizer(String str, String delim, boolean returnDelimiters, boolean returnQuotes) {
        super("");
        this._delim = __delim;
        this._returnQuotes = false;
        this._returnDelimiters = false;
        this._hasToken = false;
        this._i = 0;
        this._lastStart = 0;
        this._double = true;
        this._single = true;
        this._string = str;
        if (delim != null) {
            this._delim = delim;
        }
        this._returnDelimiters = returnDelimiters;
        this._returnQuotes = returnQuotes;
        if (this._delim.indexOf(39) >= 0 || this._delim.indexOf(34) >= 0) {
            throw new Error("Can't use quotes as delimiters: " + this._delim);
        }
        this._token = new StringBuffer(this._string.length() > 1024 ? HttpWriter.MAX_OUTPUT_CHARS : this._string.length() / 2);
    }

    public QuotedStringTokenizer(String str, String delim, boolean returnDelimiters) {
        this(str, delim, returnDelimiters, false);
    }

    public QuotedStringTokenizer(String str, String delim) {
        this(str, delim, false, false);
    }

    public QuotedStringTokenizer(String str) {
        this(str, null, false, false);
    }

    @Override // java.util.StringTokenizer
    public boolean hasMoreTokens() {
        if (this._hasToken) {
            return true;
        }
        this._lastStart = this._i;
        int state = 0;
        boolean escape = false;
        while (this._i < this._string.length()) {
            String str = this._string;
            int i = this._i;
            this._i = i + 1;
            char c = str.charAt(i);
            switch (state) {
                case 0:
                    if (this._delim.indexOf(c) >= 0) {
                        if (!this._returnDelimiters) {
                            break;
                        } else {
                            this._token.append(c);
                            this._hasToken = true;
                            return true;
                        }
                    } else if (c == '\'' && this._single) {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 2;
                        break;
                    } else if (c == '\"' && this._double) {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 3;
                        break;
                    } else {
                        this._token.append(c);
                        this._hasToken = true;
                        state = 1;
                        break;
                    }
                case 1:
                    this._hasToken = true;
                    if (this._delim.indexOf(c) >= 0) {
                        if (this._returnDelimiters) {
                            this._i--;
                        }
                        return this._hasToken;
                    } else if (c == '\'' && this._single) {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 2;
                        break;
                    } else if (c == '\"' && this._double) {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 3;
                        break;
                    } else {
                        this._token.append(c);
                        break;
                    }
                case 2:
                    this._hasToken = true;
                    if (escape) {
                        escape = false;
                        this._token.append(c);
                        break;
                    } else if (c == '\'') {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 1;
                        break;
                    } else if (c == '\\') {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        escape = true;
                        break;
                    } else {
                        this._token.append(c);
                        break;
                    }
                case 3:
                    this._hasToken = true;
                    if (escape) {
                        escape = false;
                        this._token.append(c);
                        break;
                    } else if (c == '\"') {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        state = 1;
                        break;
                    } else if (c == '\\') {
                        if (this._returnQuotes) {
                            this._token.append(c);
                        }
                        escape = true;
                        break;
                    } else {
                        this._token.append(c);
                        break;
                    }
            }
        }
        return this._hasToken;
    }

    @Override // java.util.StringTokenizer
    public String nextToken() throws NoSuchElementException {
        if (!hasMoreTokens() || this._token == null) {
            throw new NoSuchElementException();
        }
        String t = this._token.toString();
        this._token.setLength(0);
        this._hasToken = false;
        return t;
    }

    @Override // java.util.StringTokenizer
    public String nextToken(String delim) throws NoSuchElementException {
        this._delim = delim;
        this._i = this._lastStart;
        this._token.setLength(0);
        this._hasToken = false;
        return nextToken();
    }

    @Override // java.util.StringTokenizer, java.util.Enumeration
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    @Override // java.util.StringTokenizer, java.util.Enumeration
    public Object nextElement() throws NoSuchElementException {
        return nextToken();
    }

    @Override // java.util.StringTokenizer
    public int countTokens() {
        return -1;
    }

    public static String quoteIfNeeded(String s, String delim) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return "\"\"";
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '\"' || c == '\'' || Character.isWhitespace(c) || delim.indexOf(c) >= 0) {
                StringBuffer b = new StringBuffer(s.length() + 8);
                quote(b, s);
                return b.toString();
            }
        }
        return s;
    }

    public static String quote(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return "\"\"";
        }
        StringBuffer b = new StringBuffer(s.length() + 8);
        quote(b, s);
        return b.toString();
    }

    static {
        Arrays.fill(escapes, (char) 65535);
        escapes[8] = 'b';
        escapes[9] = 't';
        escapes[10] = 'n';
        escapes[12] = 'f';
        escapes[13] = 'r';
    }

    public static void quote(Appendable buffer, String input) {
        try {
            buffer.append('\"');
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c >= ' ') {
                    if (c == '\"' || c == '\\') {
                        buffer.append('\\');
                    }
                    buffer.append(c);
                } else {
                    char escape = escapes[c];
                    if (escape == 65535) {
                        buffer.append('\\').append('u').append('0').append('0');
                        if (c < 16) {
                            buffer.append('0');
                        }
                        buffer.append(Integer.toString(c, 16));
                    } else {
                        buffer.append('\\').append(escape);
                    }
                }
            }
            buffer.append('\"');
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static boolean quoteIfNeeded(Appendable buf, String s, String delim) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (delim.indexOf(c) >= 0) {
                quote(buf, s);
                return true;
            }
        }
        try {
            buf.append(s);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String unquoteOnly(String s) {
        return unquoteOnly(s, false);
    }

    public static String unquoteOnly(String s, boolean lenient) {
        if (s == null) {
            return null;
        }
        if (s.length() < 2) {
            return s;
        }
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);
        if (first != last || (first != '\"' && first != '\'')) {
            return s;
        }
        StringBuilder b = new StringBuilder(s.length() - 2);
        boolean escape = false;
        for (int i = 1; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                if (lenient && !isValidEscaping(c)) {
                    b.append('\\');
                }
                b.append(c);
            } else if (c == '\\') {
                escape = true;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    public static String unquote(String s) {
        return unquote(s, false);
    }

    public static String unquote(String s, boolean lenient) {
        if (s == null) {
            return null;
        }
        if (s.length() < 2) {
            return s;
        }
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);
        if (first != last || (first != '\"' && first != '\'')) {
            return s;
        }
        StringBuilder b = new StringBuilder(s.length() - 2);
        boolean escape = false;
        int i = 1;
        while (i < s.length() - 1) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                if (c == '\"') {
                    b.append('\"');
                } else if (c != '/') {
                    if (c == '\\') {
                        b.append('\\');
                    } else if (c == 'b') {
                        b.append('\b');
                    } else if (c == 'f') {
                        b.append('\f');
                    } else if (c == 'n') {
                        b.append('\n');
                    } else if (c == 'r') {
                        b.append('\r');
                    } else {
                        switch (c) {
                            case 't':
                                b.append('\t');
                                continue;
                            case 'u':
                                int i2 = i + 1;
                                int i3 = i2 + 1;
                                int convertHexDigit = (TypeUtil.convertHexDigit((byte) s.charAt(i)) << 24) + (TypeUtil.convertHexDigit((byte) s.charAt(i2)) << 16);
                                int i4 = i3 + 1;
                                b.append((char) (convertHexDigit + (TypeUtil.convertHexDigit((byte) s.charAt(i3)) << 8) + TypeUtil.convertHexDigit((byte) s.charAt(i4))));
                                i = i4 + 1;
                                continue;
                            default:
                                if (lenient && !isValidEscaping(c)) {
                                    b.append('\\');
                                }
                                b.append(c);
                                continue;
                        }
                    }
                } else {
                    b.append('/');
                }
            } else if (c == '\\') {
                escape = true;
            } else {
                b.append(c);
            }
            i++;
        }
        return b.toString();
    }

    private static boolean isValidEscaping(char c) {
        return c == 'n' || c == 'r' || c == 't' || c == 'f' || c == 'b' || c == '\\' || c == '/' || c == '\"' || c == 'u';
    }

    public boolean getDouble() {
        return this._double;
    }

    public void setDouble(boolean d) {
        this._double = d;
    }

    public boolean getSingle() {
        return this._single;
    }

    public void setSingle(boolean single) {
        this._single = single;
    }
}
