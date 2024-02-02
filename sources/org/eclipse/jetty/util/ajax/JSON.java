package org.eclipse.jetty.util.ajax;

import com.xpeng.airplay.service.NsdConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.seamless.xhtml.XHTML;
/* loaded from: classes.dex */
public class JSON {
    private Map<String, Convertor> _convertors = new ConcurrentHashMap();
    private int _stringBufferSize = 1024;
    static final Logger LOG = Log.getLogger(JSON.class);
    public static final JSON DEFAULT = new JSON();

    /* loaded from: classes.dex */
    public interface Convertible {
        void fromJSON(Map map);

        void toJSON(Output output);
    }

    /* loaded from: classes.dex */
    public interface Convertor {
        Object fromJSON(Map map);

        void toJSON(Object obj, Output output);
    }

    /* loaded from: classes.dex */
    public interface Generator {
        void addJSON(Appendable appendable);
    }

    /* loaded from: classes.dex */
    public interface Output {
        void add(Object obj);

        void add(String str, double d);

        void add(String str, long j);

        void add(String str, Object obj);

        void add(String str, boolean z);

        void addClass(Class cls);
    }

    /* loaded from: classes.dex */
    public interface Source {
        boolean hasNext();

        char next();

        char peek();

        char[] scratchBuffer();
    }

    public int getStringBufferSize() {
        return this._stringBufferSize;
    }

    public void setStringBufferSize(int stringBufferSize) {
        this._stringBufferSize = stringBufferSize;
    }

    public static void registerConvertor(Class forClass, Convertor convertor) {
        DEFAULT.addConvertor(forClass, convertor);
    }

    public static JSON getDefault() {
        return DEFAULT;
    }

    @Deprecated
    public static void setDefault(JSON json) {
    }

    public static String toString(Object object) {
        StringBuilder buffer = new StringBuilder(DEFAULT.getStringBufferSize());
        DEFAULT.append(buffer, object);
        return buffer.toString();
    }

    public static String toString(Map object) {
        StringBuilder buffer = new StringBuilder(DEFAULT.getStringBufferSize());
        DEFAULT.appendMap(buffer, object);
        return buffer.toString();
    }

    public static String toString(Object[] array) {
        StringBuilder buffer = new StringBuilder(DEFAULT.getStringBufferSize());
        DEFAULT.appendArray(buffer, array);
        return buffer.toString();
    }

    public static Object parse(String s) {
        return DEFAULT.parse((Source) new StringSource(s), false);
    }

    public static Object parse(String s, boolean stripOuterComment) {
        return DEFAULT.parse(new StringSource(s), stripOuterComment);
    }

    public static Object parse(Reader in) throws IOException {
        return DEFAULT.parse((Source) new ReaderSource(in), false);
    }

    public static Object parse(Reader in, boolean stripOuterComment) throws IOException {
        return DEFAULT.parse(new ReaderSource(in), stripOuterComment);
    }

    @Deprecated
    public static Object parse(InputStream in) throws IOException {
        return DEFAULT.parse((Source) new StringSource(IO.toString(in)), false);
    }

    @Deprecated
    public static Object parse(InputStream in, boolean stripOuterComment) throws IOException {
        return DEFAULT.parse(new StringSource(IO.toString(in)), stripOuterComment);
    }

    public String toJSON(Object object) {
        StringBuilder buffer = new StringBuilder(getStringBufferSize());
        append(buffer, object);
        return buffer.toString();
    }

    public Object fromJSON(String json) {
        Source source = new StringSource(json);
        return parse(source);
    }

    @Deprecated
    public void append(StringBuffer buffer, Object object) {
        append((Appendable) buffer, object);
    }

    public void append(Appendable buffer, Object object) {
        try {
            if (object == null) {
                buffer.append("null");
            } else if (object instanceof Map) {
                appendMap(buffer, (Map) object);
            } else if (object instanceof String) {
                appendString(buffer, (String) object);
            } else if (object instanceof Number) {
                appendNumber(buffer, (Number) object);
            } else if (object instanceof Boolean) {
                appendBoolean(buffer, (Boolean) object);
            } else if (object.getClass().isArray()) {
                appendArray(buffer, object);
            } else if (object instanceof Character) {
                appendString(buffer, object.toString());
            } else if (object instanceof Convertible) {
                appendJSON(buffer, (Convertible) object);
            } else if (object instanceof Generator) {
                appendJSON(buffer, (Generator) object);
            } else {
                Convertor convertor = getConvertor(object.getClass());
                if (convertor != null) {
                    appendJSON(buffer, convertor, object);
                } else if (object instanceof Collection) {
                    appendArray(buffer, (Collection) object);
                } else {
                    appendString(buffer, object.toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendNull(StringBuffer buffer) {
        appendNull((Appendable) buffer);
    }

    public void appendNull(Appendable buffer) {
        try {
            buffer.append("null");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendJSON(StringBuffer buffer, Convertor convertor, Object object) {
        appendJSON((Appendable) buffer, convertor, object);
    }

    public void appendJSON(Appendable buffer, final Convertor convertor, final Object object) {
        appendJSON(buffer, new Convertible() { // from class: org.eclipse.jetty.util.ajax.JSON.1
            @Override // org.eclipse.jetty.util.ajax.JSON.Convertible
            public void fromJSON(Map object2) {
            }

            @Override // org.eclipse.jetty.util.ajax.JSON.Convertible
            public void toJSON(Output out) {
                convertor.toJSON(object, out);
            }
        });
    }

    @Deprecated
    public void appendJSON(StringBuffer buffer, Convertible converter) {
        appendJSON((Appendable) buffer, converter);
    }

    public void appendJSON(Appendable buffer, Convertible converter) {
        ConvertableOutput out = new ConvertableOutput(buffer);
        converter.toJSON(out);
        out.complete();
    }

    @Deprecated
    public void appendJSON(StringBuffer buffer, Generator generator) {
        generator.addJSON(buffer);
    }

    public void appendJSON(Appendable buffer, Generator generator) {
        generator.addJSON(buffer);
    }

    @Deprecated
    public void appendMap(StringBuffer buffer, Map<?, ?> map) {
        appendMap((Appendable) buffer, map);
    }

    public void appendMap(Appendable buffer, Map<?, ?> map) {
        try {
            if (map == null) {
                appendNull(buffer);
                return;
            }
            buffer.append('{');
            Iterator<?> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<?, ?> entry = iter.next();
                QuotedStringTokenizer.quote(buffer, entry.getKey().toString());
                buffer.append(':');
                append(buffer, entry.getValue());
                if (iter.hasNext()) {
                    buffer.append(',');
                }
            }
            buffer.append('}');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendArray(StringBuffer buffer, Collection collection) {
        appendArray((Appendable) buffer, collection);
    }

    public void appendArray(Appendable buffer, Collection collection) {
        try {
            if (collection == null) {
                appendNull(buffer);
                return;
            }
            buffer.append('[');
            boolean first = true;
            for (Object obj : collection) {
                if (!first) {
                    buffer.append(',');
                }
                first = false;
                append(buffer, obj);
            }
            buffer.append(']');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendArray(StringBuffer buffer, Object array) {
        appendArray((Appendable) buffer, array);
    }

    public void appendArray(Appendable buffer, Object array) {
        try {
            if (array == null) {
                appendNull(buffer);
                return;
            }
            buffer.append('[');
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                if (i != 0) {
                    buffer.append(',');
                }
                append(buffer, Array.get(array, i));
            }
            buffer.append(']');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendBoolean(StringBuffer buffer, Boolean b) {
        appendBoolean((Appendable) buffer, b);
    }

    public void appendBoolean(Appendable buffer, Boolean b) {
        try {
            if (b == null) {
                appendNull(buffer);
            } else {
                buffer.append(b.booleanValue() ? NsdConstants.AIRPLAY_TXT_VALUE_DA : "false");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendNumber(StringBuffer buffer, Number number) {
        appendNumber((Appendable) buffer, number);
    }

    public void appendNumber(Appendable buffer, Number number) {
        try {
            if (number == null) {
                appendNull(buffer);
            } else {
                buffer.append(String.valueOf(number));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void appendString(StringBuffer buffer, String string) {
        appendString((Appendable) buffer, string);
    }

    public void appendString(Appendable buffer, String string) {
        if (string == null) {
            appendNull(buffer);
        } else {
            QuotedStringTokenizer.quote(buffer, string);
        }
    }

    protected String toString(char[] buffer, int offset, int length) {
        return new String(buffer, offset, length);
    }

    protected Map<String, Object> newMap() {
        return new HashMap();
    }

    protected Object[] newArray(int size) {
        return new Object[size];
    }

    protected JSON contextForArray() {
        return this;
    }

    protected JSON contextFor(String field) {
        return this;
    }

    protected Object convertTo(Class type, Map map) {
        if (type != null && Convertible.class.isAssignableFrom(type)) {
            try {
                Convertible conv = (Convertible) type.newInstance();
                conv.fromJSON(map);
                return conv;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Convertor convertor = getConvertor(type);
        if (convertor != null) {
            return convertor.fromJSON(map);
        }
        return map;
    }

    public void addConvertor(Class forClass, Convertor convertor) {
        this._convertors.put(forClass.getName(), convertor);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0040, code lost:
        r0 = r0.getSuperclass();
        r1 = r6._convertors.get(r0.getName());
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected org.eclipse.jetty.util.ajax.JSON.Convertor getConvertor(java.lang.Class r7) {
        /*
            r6 = this;
            r0 = r7
            java.util.Map<java.lang.String, org.eclipse.jetty.util.ajax.JSON$Convertor> r1 = r6._convertors
            java.lang.String r2 = r0.getName()
            java.lang.Object r1 = r1.get(r2)
            org.eclipse.jetty.util.ajax.JSON$Convertor r1 = (org.eclipse.jetty.util.ajax.JSON.Convertor) r1
            if (r1 != 0) goto L19
            org.eclipse.jetty.util.ajax.JSON r2 = org.eclipse.jetty.util.ajax.JSON.DEFAULT
            if (r6 == r2) goto L19
            org.eclipse.jetty.util.ajax.JSON r2 = org.eclipse.jetty.util.ajax.JSON.DEFAULT
            org.eclipse.jetty.util.ajax.JSON$Convertor r1 = r2.getConvertor(r0)
        L19:
            if (r1 != 0) goto L52
            java.lang.Class<java.lang.Object> r2 = java.lang.Object.class
            if (r0 == r2) goto L52
            java.lang.Class[] r2 = r0.getInterfaces()
            r3 = 0
        L24:
            if (r1 != 0) goto L3e
            if (r2 == 0) goto L3e
            int r4 = r2.length
            if (r3 >= r4) goto L3e
            java.util.Map<java.lang.String, org.eclipse.jetty.util.ajax.JSON$Convertor> r4 = r6._convertors
            int r5 = r3 + 1
            r3 = r2[r3]
            java.lang.String r3 = r3.getName()
            java.lang.Object r3 = r4.get(r3)
            r1 = r3
            org.eclipse.jetty.util.ajax.JSON$Convertor r1 = (org.eclipse.jetty.util.ajax.JSON.Convertor) r1
            r3 = r5
            goto L24
        L3e:
            if (r1 != 0) goto L51
            java.lang.Class r0 = r0.getSuperclass()
            java.util.Map<java.lang.String, org.eclipse.jetty.util.ajax.JSON$Convertor> r4 = r6._convertors
            java.lang.String r5 = r0.getName()
            java.lang.Object r4 = r4.get(r5)
            r1 = r4
            org.eclipse.jetty.util.ajax.JSON$Convertor r1 = (org.eclipse.jetty.util.ajax.JSON.Convertor) r1
        L51:
            goto L19
        L52:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.ajax.JSON.getConvertor(java.lang.Class):org.eclipse.jetty.util.ajax.JSON$Convertor");
    }

    public void addConvertorFor(String name, Convertor convertor) {
        this._convertors.put(name, convertor);
    }

    public Convertor getConvertorFor(String name) {
        Convertor convertor = this._convertors.get(name);
        if (convertor == null && this != DEFAULT) {
            return DEFAULT.getConvertorFor(name);
        }
        return convertor;
    }

    public Object parse(Source source, boolean stripOuterComment) {
        int comment_state = 0;
        if (!stripOuterComment) {
            return parse(source);
        }
        int strip_state = 1;
        Object o = null;
        while (source.hasNext()) {
            char c = source.peek();
            if (comment_state == 1) {
                if (c != '*') {
                    if (c == '/') {
                        comment_state = -1;
                    }
                } else {
                    comment_state = 2;
                    if (strip_state == 1) {
                        comment_state = 0;
                        strip_state = 2;
                    }
                }
            } else if (comment_state > 1) {
                if (c != '*') {
                    if (c == '/') {
                        if (comment_state == 3) {
                            comment_state = 0;
                            if (strip_state == 2) {
                                return o;
                            }
                        } else {
                            comment_state = 2;
                        }
                    } else {
                        comment_state = 2;
                    }
                } else {
                    comment_state = 3;
                }
            } else if (comment_state < 0) {
                if (c == '\n' || c == '\r') {
                    comment_state = 0;
                }
            } else if (!Character.isWhitespace(c)) {
                if (c == '/') {
                    comment_state = 1;
                } else if (c == '*') {
                    comment_state = 3;
                } else if (o == null) {
                    o = parse(source);
                }
            }
            source.next();
        }
        return o;
    }

    public Object parse(Source source) {
        int comment_state = 0;
        while (source.hasNext()) {
            char c = source.peek();
            if (comment_state == 1) {
                if (c == '*') {
                    comment_state = 2;
                } else if (c == '/') {
                    comment_state = -1;
                }
            } else if (comment_state > 1) {
                if (c == '*') {
                    comment_state = 3;
                } else if (c == '/') {
                    if (comment_state == 3) {
                        comment_state = 0;
                    } else {
                        comment_state = 2;
                    }
                } else {
                    comment_state = 2;
                }
            } else if (comment_state < 0) {
                if (c == '\n' || c == '\r') {
                    comment_state = 0;
                }
            } else if (c == '\"') {
                return parseString(source);
            } else {
                if (c == '-') {
                    return parseNumber(source);
                }
                if (c == '/') {
                    comment_state = 1;
                } else if (c == 'N') {
                    complete("NaN", source);
                    return null;
                } else if (c != '[') {
                    if (c == 'f') {
                        complete("false", source);
                        return Boolean.FALSE;
                    } else if (c == 'n') {
                        complete("null", source);
                        return null;
                    } else if (c == '{') {
                        return parseObject(source);
                    } else {
                        switch (c) {
                            case 't':
                                complete(NsdConstants.AIRPLAY_TXT_VALUE_DA, source);
                                return Boolean.TRUE;
                            case 'u':
                                complete("undefined", source);
                                return null;
                            default:
                                if (Character.isDigit(c)) {
                                    return parseNumber(source);
                                }
                                if (Character.isWhitespace(c)) {
                                    break;
                                } else {
                                    return handleUnknown(source, c);
                                }
                        }
                    }
                } else {
                    return parseArray(source);
                }
            }
            source.next();
        }
        return null;
    }

    protected Object handleUnknown(Source source, char c) {
        throw new IllegalStateException("unknown char '" + c + "'(" + ((int) c) + ") in " + source);
    }

    protected Object parseObject(Source source) {
        if (source.next() != '{') {
            throw new IllegalStateException();
        }
        Map<String, Object> map = newMap();
        char next = seekTo("\"}", source);
        while (true) {
            if (!source.hasNext()) {
                break;
            } else if (next == '}') {
                source.next();
                break;
            } else {
                String name = parseString(source);
                seekTo(':', source);
                source.next();
                Object value = contextFor(name).parse(source);
                map.put(name, value);
                seekTo(",}", source);
                char next2 = source.next();
                if (next2 == '}') {
                    break;
                }
                next = seekTo("\"}", source);
            }
        }
        String xclassname = (String) map.get("x-class");
        if (xclassname != null) {
            Convertor c = getConvertorFor(xclassname);
            if (c != null) {
                return c.fromJSON(map);
            }
            LOG.warn("No Convertor for x-class '{}'", xclassname);
        }
        String classname = (String) map.get(XHTML.ATTR.CLASS);
        if (classname != null) {
            try {
                return convertTo(Loader.loadClass(JSON.class, classname), map);
            } catch (ClassNotFoundException e) {
                LOG.warn("No Class for '{}'", classname);
            }
        }
        return map;
    }

    protected Object parseArray(Source source) {
        if (source.next() != '[') {
            throw new IllegalStateException();
        }
        ArrayList list = null;
        Object item = null;
        int size = 0;
        boolean coma = true;
        while (source.hasNext()) {
            char c = source.peek();
            if (c != ',') {
                if (c == ']') {
                    source.next();
                    switch (size) {
                        case 0:
                            return newArray(0);
                        case 1:
                            Object array = newArray(1);
                            Array.set(array, 0, item);
                            return array;
                        default:
                            return list.toArray(newArray(list.size()));
                    }
                } else if (Character.isWhitespace(c)) {
                    source.next();
                } else {
                    coma = false;
                    int size2 = size + 1;
                    if (size == 0) {
                        item = contextForArray().parse(source);
                    } else if (list == null) {
                        list = new ArrayList();
                        list.add(item);
                        Object item2 = contextForArray().parse(source);
                        list.add(item2);
                        item = null;
                    } else {
                        Object item3 = contextForArray().parse(source);
                        list.add(item3);
                        item = null;
                    }
                    size = size2;
                }
            } else if (coma) {
                throw new IllegalStateException();
            } else {
                coma = true;
                source.next();
            }
        }
        throw new IllegalStateException("unexpected end of array");
    }

    protected String parseString(Source source) {
        boolean z;
        int i;
        if (source.next() == '\"') {
            boolean escape = false;
            StringBuilder b = null;
            char[] scratch = source.scratchBuffer();
            char c = 'r';
            char c2 = 'n';
            if (scratch != null) {
                boolean escape2 = false;
                int i2 = 0;
                char uc = 0;
                while (true) {
                    if (source.hasNext()) {
                        if (i2 >= scratch.length) {
                            b = new StringBuilder(scratch.length * 2);
                            b.append(scratch, 0, i2);
                        } else {
                            char c3 = source.next();
                            if (escape2) {
                                z = false;
                                if (c3 == '\"') {
                                    i = i2 + 1;
                                    scratch[i2] = '\"';
                                } else if (c3 == '/') {
                                    i = i2 + 1;
                                    scratch[i2] = '/';
                                } else if (c3 == '\\') {
                                    i = i2 + 1;
                                    scratch[i2] = '\\';
                                } else if (c3 == 'b') {
                                    i = i2 + 1;
                                    scratch[i2] = '\b';
                                } else if (c3 == 'f') {
                                    i = i2 + 1;
                                    scratch[i2] = '\f';
                                } else if (c3 == c2) {
                                    i = i2 + 1;
                                    scratch[i2] = '\n';
                                } else if (c3 != c) {
                                    switch (c3) {
                                        case 't':
                                            i = i2 + 1;
                                            scratch[i2] = '\t';
                                            break;
                                        case 'u':
                                            char uc2 = (char) ((TypeUtil.convertHexDigit((byte) source.next()) << 12) + (TypeUtil.convertHexDigit((byte) source.next()) << 8) + (TypeUtil.convertHexDigit((byte) source.next()) << 4) + TypeUtil.convertHexDigit((byte) source.next()));
                                            i = i2 + 1;
                                            scratch[i2] = uc2;
                                            uc = uc2;
                                            break;
                                        default:
                                            i = i2 + 1;
                                            scratch[i2] = c3;
                                            break;
                                    }
                                } else {
                                    i = i2 + 1;
                                    scratch[i2] = '\r';
                                }
                                i2 = i;
                            } else if (c3 == '\\') {
                                z = true;
                            } else if (c3 == '\"') {
                                return toString(scratch, 0, i2);
                            } else {
                                scratch[i2] = c3;
                                i2++;
                                c = 'r';
                                c2 = 'n';
                            }
                            escape2 = z;
                            c = 'r';
                            c2 = 'n';
                        }
                    }
                }
                if (b == null) {
                    return toString(scratch, 0, i2);
                }
                escape = escape2;
            } else {
                b = new StringBuilder(getStringBufferSize());
            }
            boolean escape3 = escape;
            StringBuilder builder = b;
            while (source.hasNext()) {
                char c4 = source.next();
                if (escape3) {
                    escape3 = false;
                    if (c4 == '\"') {
                        builder.append('\"');
                    } else if (c4 == '/') {
                        builder.append('/');
                    } else if (c4 == '\\') {
                        builder.append('\\');
                    } else if (c4 == 'b') {
                        builder.append('\b');
                    } else if (c4 == 'f') {
                        builder.append('\f');
                    } else if (c4 == 'n') {
                        builder.append('\n');
                    } else if (c4 != 'r') {
                        switch (c4) {
                            case 't':
                                builder.append('\t');
                                break;
                            case 'u':
                                char uc3 = source.next();
                                builder.append((char) ((TypeUtil.convertHexDigit((byte) uc3) << 12) + (TypeUtil.convertHexDigit((byte) source.next()) << 8) + (TypeUtil.convertHexDigit((byte) source.next()) << 4) + TypeUtil.convertHexDigit((byte) source.next())));
                                break;
                            default:
                                builder.append(c4);
                                break;
                        }
                    } else {
                        builder.append('\r');
                    }
                } else if (c4 == '\\') {
                    escape3 = true;
                } else if (c4 == '\"') {
                    return builder.toString();
                } else {
                    builder.append(c4);
                }
            }
            return builder.toString();
        }
        throw new IllegalStateException();
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x005d  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0069 A[LOOP:1: B:29:0x0069->B:38:0x0080, LOOP_START] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Number parseNumber(org.eclipse.jetty.util.ajax.JSON.Source r11) {
        /*
            r10 = this;
            r0 = 0
            r1 = 0
            r3 = 0
        L4:
            boolean r4 = r11.hasNext()
            r5 = 101(0x65, float:1.42E-43)
            r6 = 69
            r7 = 43
            if (r4 == 0) goto L5b
            char r4 = r11.peek()
            if (r4 == r7) goto L47
            if (r4 == r6) goto L2e
            if (r4 == r5) goto L2e
            switch(r4) {
                case 45: goto L47;
                case 46: goto L2e;
                default: goto L1d;
            }
        L1d:
            switch(r4) {
                case 48: goto L21;
                case 49: goto L21;
                case 50: goto L21;
                case 51: goto L21;
                case 52: goto L21;
                case 53: goto L21;
                case 54: goto L21;
                case 55: goto L21;
                case 56: goto L21;
                case 57: goto L21;
                default: goto L20;
            }
        L20:
            goto L5b
        L21:
            r5 = 10
            long r5 = r5 * r1
            int r7 = r4 + (-48)
            long r7 = (long) r7
            long r5 = r5 + r7
            r11.next()
            r1 = r5
            goto L52
        L2e:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r9 = 16
            r8.<init>(r9)
            r3 = r8
            if (r0 == 0) goto L3d
            r8 = 45
            r3.append(r8)
        L3d:
            r3.append(r1)
            r3.append(r4)
            r11.next()
            goto L5b
        L47:
            r5 = 0
            int r5 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r5 != 0) goto L53
            r0 = 1
            r11.next()
        L52:
            goto L4
        L53:
            java.lang.IllegalStateException r5 = new java.lang.IllegalStateException
            java.lang.String r6 = "bad number"
            r5.<init>(r6)
            throw r5
        L5b:
            if (r3 != 0) goto L69
            if (r0 == 0) goto L63
            r4 = -1
            long r4 = r4 * r1
            goto L64
        L63:
            r4 = r1
        L64:
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            return r4
        L69:
            boolean r4 = r11.hasNext()
            if (r4 == 0) goto L88
            char r4 = r11.peek()
            if (r4 == r7) goto L80
            if (r4 == r6) goto L80
            if (r4 == r5) goto L80
            switch(r4) {
                case 45: goto L80;
                case 46: goto L80;
                default: goto L7c;
            }
        L7c:
            switch(r4) {
                case 48: goto L80;
                case 49: goto L80;
                case 50: goto L80;
                case 51: goto L80;
                case 52: goto L80;
                case 53: goto L80;
                case 54: goto L80;
                case 55: goto L80;
                case 56: goto L80;
                case 57: goto L80;
                default: goto L7f;
            }
        L7f:
            goto L88
        L80:
            r3.append(r4)
            r11.next()
            goto L69
        L88:
            java.lang.Double r4 = new java.lang.Double
            java.lang.String r5 = r3.toString()
            r4.<init>(r5)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.ajax.JSON.parseNumber(org.eclipse.jetty.util.ajax.JSON$Source):java.lang.Number");
    }

    protected void seekTo(char seek, Source source) {
        while (source.hasNext()) {
            char c = source.peek();
            if (c == seek) {
                return;
            }
            if (!Character.isWhitespace(c)) {
                throw new IllegalStateException("Unexpected '" + c + " while seeking '" + seek + "'");
            }
            source.next();
        }
        throw new IllegalStateException("Expected '" + seek + "'");
    }

    protected char seekTo(String seek, Source source) {
        while (source.hasNext()) {
            char c = source.peek();
            if (seek.indexOf(c) >= 0) {
                return c;
            }
            if (!Character.isWhitespace(c)) {
                throw new IllegalStateException("Unexpected '" + c + "' while seeking one of '" + seek + "'");
            }
            source.next();
        }
        throw new IllegalStateException("Expected one of '" + seek + "'");
    }

    protected static void complete(String seek, Source source) {
        int i = 0;
        while (source.hasNext() && i < seek.length()) {
            char c = source.next();
            int i2 = i + 1;
            if (c == seek.charAt(i)) {
                i = i2;
            } else {
                throw new IllegalStateException("Unexpected '" + c + " while seeking  \"" + seek + "\"");
            }
        }
        if (i < seek.length()) {
            throw new IllegalStateException("Expected \"" + seek + "\"");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class ConvertableOutput implements Output {
        private final Appendable _buffer;
        char c;

        private ConvertableOutput(Appendable buffer) {
            this.c = '{';
            this._buffer = buffer;
        }

        public void complete() {
            try {
                if (this.c == '{') {
                    this._buffer.append("{}");
                } else if (this.c != 0) {
                    this._buffer.append("}");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void add(Object obj) {
            if (this.c == 0) {
                throw new IllegalStateException();
            }
            JSON.this.append(this._buffer, obj);
            this.c = (char) 0;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void addClass(Class type) {
            try {
                if (this.c == 0) {
                    throw new IllegalStateException();
                }
                this._buffer.append(this.c);
                this._buffer.append("\"class\":");
                JSON.this.append(this._buffer, type.getName());
                this.c = ',';
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void add(String name, Object value) {
            try {
                if (this.c == 0) {
                    throw new IllegalStateException();
                }
                this._buffer.append(this.c);
                QuotedStringTokenizer.quote(this._buffer, name);
                this._buffer.append(':');
                JSON.this.append(this._buffer, value);
                this.c = ',';
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void add(String name, double value) {
            try {
                if (this.c == 0) {
                    throw new IllegalStateException();
                }
                this._buffer.append(this.c);
                QuotedStringTokenizer.quote(this._buffer, name);
                this._buffer.append(':');
                JSON.this.appendNumber(this._buffer, Double.valueOf(value));
                this.c = ',';
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void add(String name, long value) {
            try {
                if (this.c == 0) {
                    throw new IllegalStateException();
                }
                this._buffer.append(this.c);
                QuotedStringTokenizer.quote(this._buffer, name);
                this._buffer.append(':');
                JSON.this.appendNumber(this._buffer, Long.valueOf(value));
                this.c = ',';
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Output
        public void add(String name, boolean value) {
            try {
                if (this.c == 0) {
                    throw new IllegalStateException();
                }
                this._buffer.append(this.c);
                QuotedStringTokenizer.quote(this._buffer, name);
                this._buffer.append(':');
                JSON.this.appendBoolean(this._buffer, value ? Boolean.TRUE : Boolean.FALSE);
                this.c = ',';
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class StringSource implements Source {
        private int index;
        private char[] scratch;
        private final String string;

        public StringSource(String s) {
            this.string = s;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public boolean hasNext() {
            if (this.index < this.string.length()) {
                return true;
            }
            this.scratch = null;
            return false;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char next() {
            String str = this.string;
            int i = this.index;
            this.index = i + 1;
            return str.charAt(i);
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char peek() {
            return this.string.charAt(this.index);
        }

        public String toString() {
            return this.string.substring(0, this.index) + "|||" + this.string.substring(this.index);
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char[] scratchBuffer() {
            if (this.scratch == null) {
                this.scratch = new char[this.string.length()];
            }
            return this.scratch;
        }
    }

    /* loaded from: classes.dex */
    public static class ReaderSource implements Source {
        private int _next = -1;
        private Reader _reader;
        private char[] scratch;

        public ReaderSource(Reader r) {
            this._reader = r;
        }

        public void setReader(Reader reader) {
            this._reader = reader;
            this._next = -1;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public boolean hasNext() {
            getNext();
            if (this._next < 0) {
                this.scratch = null;
                return false;
            }
            return true;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char next() {
            getNext();
            char c = (char) this._next;
            this._next = -1;
            return c;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char peek() {
            getNext();
            return (char) this._next;
        }

        private void getNext() {
            if (this._next < 0) {
                try {
                    this._next = this._reader.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Source
        public char[] scratchBuffer() {
            if (this.scratch == null) {
                this.scratch = new char[1024];
            }
            return this.scratch;
        }
    }

    /* loaded from: classes.dex */
    public static class Literal implements Generator {
        private String _json;

        public Literal(String json) {
            if (JSON.LOG.isDebugEnabled()) {
                JSON.parse(json);
            }
            this._json = json;
        }

        public String toString() {
            return this._json;
        }

        @Override // org.eclipse.jetty.util.ajax.JSON.Generator
        public void addJSON(Appendable buffer) {
            try {
                buffer.append(this._json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
