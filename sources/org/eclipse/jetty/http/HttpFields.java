package org.eclipse.jetty.http;

import com.apple.dnssd.DNSSD;
import com.xpeng.airplay.service.NsdConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.BufferDateCache;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class HttpFields {
    private static final String[] DAYS;
    private static final String[] MONTHS;
    public static final String __01Jan1970;
    public static final Buffer __01Jan1970_BUFFER;
    public static final String __01Jan1970_COOKIE;
    public static final String __COOKIE_DELIM = "\"\\\n\r\t\f\b%+ ;=";
    private static ConcurrentMap<String, Buffer> __cache = null;
    private static int __cacheSize = 0;
    private static final ThreadLocal<DateGenerator> __dateGenerator;
    private static final ThreadLocal<DateParser> __dateParser;
    private static final String[] __dateReceiveFmt;
    private static final Float __one;
    private static final StringMap __qualities;
    public static final String __separators = ", \t";
    private static final Float __zero;
    private final ArrayList<Field> _fields = new ArrayList<>(20);
    private final HashMap<Buffer, Field> _names = new HashMap<>(32);
    private static final Logger LOG = Log.getLogger(HttpFields.class);
    public static final TimeZone __GMT = TimeZone.getTimeZone("GMT");
    public static final BufferDateCache __dateCache = new BufferDateCache("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    static {
        __GMT.setID("GMT");
        __dateCache.setTimeZone(__GMT);
        DAYS = new String[]{"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};
        __dateGenerator = new ThreadLocal<DateGenerator>() { // from class: org.eclipse.jetty.http.HttpFields.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public DateGenerator initialValue() {
                return new DateGenerator();
            }
        };
        __dateReceiveFmt = new String[]{"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd-MMM-yy HH:mm:ss", "EEE MMM dd HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm:ss", "EEE dd MMM yyyy HH:mm:ss zzz", "EEE dd MMM yyyy HH:mm:ss", "EEE MMM dd yyyy HH:mm:ss zzz", "EEE MMM dd yyyy HH:mm:ss", "EEE MMM-dd-yyyy HH:mm:ss zzz", "EEE MMM-dd-yyyy HH:mm:ss", "dd MMM yyyy HH:mm:ss zzz", "dd MMM yyyy HH:mm:ss", "dd-MMM-yy HH:mm:ss zzz", "dd-MMM-yy HH:mm:ss", "MMM dd HH:mm:ss yyyy zzz", "MMM dd HH:mm:ss yyyy", "EEE MMM dd HH:mm:ss yyyy zzz", "EEE, MMM dd HH:mm:ss yyyy zzz", "EEE, MMM dd HH:mm:ss yyyy", "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE dd-MMM-yy HH:mm:ss zzz", "EEE dd-MMM-yy HH:mm:ss"};
        __dateParser = new ThreadLocal<DateParser>() { // from class: org.eclipse.jetty.http.HttpFields.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public DateParser initialValue() {
                return new DateParser();
            }
        };
        __01Jan1970 = formatDate(0L);
        __01Jan1970_BUFFER = new ByteArrayBuffer(__01Jan1970);
        __01Jan1970_COOKIE = formatCookieDate(0L).trim();
        __cache = new ConcurrentHashMap();
        __cacheSize = Integer.getInteger("org.eclipse.jetty.http.HttpFields.CACHE", 2000).intValue();
        __one = new Float("1.0");
        __zero = new Float("0.0");
        __qualities = new StringMap();
        __qualities.put((String) null, (Object) __one);
        __qualities.put("1.0", (Object) __one);
        __qualities.put(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS, (Object) __one);
        __qualities.put("0.9", (Object) new Float("0.9"));
        __qualities.put("0.8", (Object) new Float("0.8"));
        __qualities.put("0.7", (Object) new Float("0.7"));
        __qualities.put("0.66", (Object) new Float("0.66"));
        __qualities.put("0.6", (Object) new Float("0.6"));
        __qualities.put("0.5", (Object) new Float("0.5"));
        __qualities.put("0.4", (Object) new Float("0.4"));
        __qualities.put("0.33", (Object) new Float("0.33"));
        __qualities.put("0.3", (Object) new Float("0.3"));
        __qualities.put("0.2", (Object) new Float("0.2"));
        __qualities.put("0.1", (Object) new Float("0.1"));
        __qualities.put("0", (Object) __zero);
        __qualities.put("0.0", (Object) __zero);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DateGenerator {
        private final StringBuilder buf;
        private final GregorianCalendar gc;

        private DateGenerator() {
            this.buf = new StringBuilder(32);
            this.gc = new GregorianCalendar(HttpFields.__GMT);
        }

        public String formatDate(long date) {
            this.buf.setLength(0);
            this.gc.setTimeInMillis(date);
            int day_of_week = this.gc.get(7);
            int day_of_month = this.gc.get(5);
            int month = this.gc.get(2);
            int year = this.gc.get(1);
            int century = year / 100;
            int hours = this.gc.get(11);
            int minutes = this.gc.get(12);
            int seconds = this.gc.get(13);
            this.buf.append(HttpFields.DAYS[day_of_week]);
            this.buf.append(',');
            this.buf.append(' ');
            StringUtil.append2digits(this.buf, day_of_month);
            this.buf.append(' ');
            this.buf.append(HttpFields.MONTHS[month]);
            this.buf.append(' ');
            StringUtil.append2digits(this.buf, century);
            StringUtil.append2digits(this.buf, year % 100);
            this.buf.append(' ');
            StringUtil.append2digits(this.buf, hours);
            this.buf.append(':');
            StringUtil.append2digits(this.buf, minutes);
            this.buf.append(':');
            StringUtil.append2digits(this.buf, seconds);
            this.buf.append(" GMT");
            return this.buf.toString();
        }

        public void formatCookieDate(StringBuilder buf, long date) {
            this.gc.setTimeInMillis(date);
            int day_of_week = this.gc.get(7);
            int day_of_month = this.gc.get(5);
            int month = this.gc.get(2);
            int year = this.gc.get(1) % 10000;
            int epoch = (int) ((date / 1000) % 86400);
            int seconds = epoch % 60;
            int epoch2 = epoch / 60;
            int minutes = epoch2 % 60;
            int hours = epoch2 / 60;
            buf.append(HttpFields.DAYS[day_of_week]);
            buf.append(',');
            buf.append(' ');
            StringUtil.append2digits(buf, day_of_month);
            buf.append('-');
            buf.append(HttpFields.MONTHS[month]);
            buf.append('-');
            StringUtil.append2digits(buf, year / 100);
            StringUtil.append2digits(buf, year % 100);
            buf.append(' ');
            StringUtil.append2digits(buf, hours);
            buf.append(':');
            StringUtil.append2digits(buf, minutes);
            buf.append(':');
            StringUtil.append2digits(buf, seconds);
            buf.append(" GMT");
        }
    }

    public static String formatDate(long date) {
        return __dateGenerator.get().formatDate(date);
    }

    public static void formatCookieDate(StringBuilder buf, long date) {
        __dateGenerator.get().formatCookieDate(buf, date);
    }

    public static String formatCookieDate(long date) {
        StringBuilder buf = new StringBuilder(28);
        formatCookieDate(buf, date);
        return buf.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DateParser {
        final SimpleDateFormat[] _dateReceive;

        private DateParser() {
            this._dateReceive = new SimpleDateFormat[HttpFields.__dateReceiveFmt.length];
        }

        long parse(String dateVal) {
            for (int i = 0; i < this._dateReceive.length; i++) {
                if (this._dateReceive[i] == null) {
                    this._dateReceive[i] = new SimpleDateFormat(HttpFields.__dateReceiveFmt[i], Locale.US);
                    this._dateReceive[i].setTimeZone(HttpFields.__GMT);
                }
                try {
                    Date date = (Date) this._dateReceive[i].parseObject(dateVal);
                    return date.getTime();
                } catch (Exception e) {
                }
            }
            if (dateVal.endsWith(" GMT")) {
                String val = dateVal.substring(0, dateVal.length() - 4);
                for (int i2 = 0; i2 < this._dateReceive.length; i2++) {
                    try {
                        Date date2 = (Date) this._dateReceive[i2].parseObject(val);
                        return date2.getTime();
                    } catch (Exception e2) {
                    }
                }
                return -1L;
            }
            return -1L;
        }
    }

    public static long parseDate(String date) {
        return __dateParser.get().parse(date);
    }

    private Buffer convertValue(String value) {
        Buffer buffer = __cache.get(value);
        if (buffer != null) {
            return buffer;
        }
        try {
            Buffer buffer2 = new ByteArrayBuffer(value, StringUtil.__ISO_8859_1);
            if (__cacheSize > 0) {
                if (__cache.size() > __cacheSize) {
                    __cache.clear();
                }
                Buffer b = __cache.putIfAbsent(value, buffer2);
                return b != null ? b : buffer2;
            }
            return buffer2;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getFieldNamesCollection() {
        List<String> list = new ArrayList<>(this._fields.size());
        Iterator i$ = this._fields.iterator();
        while (i$.hasNext()) {
            Field f = i$.next();
            if (f != null) {
                list.add(BufferUtil.to8859_1_String(f._name));
            }
        }
        return list;
    }

    public Enumeration<String> getFieldNames() {
        final Enumeration<?> buffers = Collections.enumeration(this._names.keySet());
        return new Enumeration<String>() { // from class: org.eclipse.jetty.http.HttpFields.3
            @Override // java.util.Enumeration
            public String nextElement() {
                return buffers.nextElement().toString();
            }

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return buffers.hasMoreElements();
            }
        };
    }

    public int size() {
        return this._fields.size();
    }

    public Field getField(int i) {
        return this._fields.get(i);
    }

    private Field getField(String name) {
        return this._names.get(HttpHeaders.CACHE.lookup(name));
    }

    private Field getField(Buffer name) {
        return this._names.get(HttpHeaders.CACHE.lookup(name));
    }

    public boolean containsKey(Buffer name) {
        return this._names.containsKey(HttpHeaders.CACHE.lookup(name));
    }

    public boolean containsKey(String name) {
        return this._names.containsKey(HttpHeaders.CACHE.lookup(name));
    }

    public String getStringField(String name) {
        Field field = getField(name);
        if (field == null) {
            return null;
        }
        return field.getValue();
    }

    public String getStringField(Buffer name) {
        Field field = getField(name);
        if (field == null) {
            return null;
        }
        return field.getValue();
    }

    public Buffer get(Buffer name) {
        Field field = getField(name);
        if (field == null) {
            return null;
        }
        return field._value;
    }

    public Collection<String> getValuesCollection(String name) {
        Field field = getField(name);
        if (field == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        while (field != null) {
            list.add(field.getValue());
            field = field._next;
        }
        return list;
    }

    public Enumeration<String> getValues(String name) {
        final Field field = getField(name);
        if (field == null) {
            List<String> empty = Collections.emptyList();
            return Collections.enumeration(empty);
        }
        return new Enumeration<String>() { // from class: org.eclipse.jetty.http.HttpFields.4
            Field f;

            {
                this.f = field;
            }

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return this.f != null;
            }

            @Override // java.util.Enumeration
            public String nextElement() throws NoSuchElementException {
                if (this.f == null) {
                    throw new NoSuchElementException();
                }
                Field n = this.f;
                this.f = this.f._next;
                return n.getValue();
            }
        };
    }

    public Enumeration<String> getValues(Buffer name) {
        final Field field = getField(name);
        if (field == null) {
            List<String> empty = Collections.emptyList();
            return Collections.enumeration(empty);
        }
        return new Enumeration<String>() { // from class: org.eclipse.jetty.http.HttpFields.5
            Field f;

            {
                this.f = field;
            }

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return this.f != null;
            }

            @Override // java.util.Enumeration
            public String nextElement() throws NoSuchElementException {
                if (this.f == null) {
                    throw new NoSuchElementException();
                }
                Field n = this.f;
                this.f = this.f._next;
                return n.getValue();
            }
        };
    }

    public Enumeration<String> getValues(String name, final String separators) {
        final Enumeration<String> e = getValues(name);
        if (e == null) {
            return null;
        }
        return new Enumeration<String>() { // from class: org.eclipse.jetty.http.HttpFields.6
            QuotedStringTokenizer tok = null;

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                if (this.tok == null || !this.tok.hasMoreElements()) {
                    while (e.hasMoreElements()) {
                        String value = (String) e.nextElement();
                        this.tok = new QuotedStringTokenizer(value, separators, false, false);
                        if (this.tok.hasMoreElements()) {
                            return true;
                        }
                    }
                    this.tok = null;
                    return false;
                }
                return true;
            }

            @Override // java.util.Enumeration
            public String nextElement() throws NoSuchElementException {
                if (!hasMoreElements()) {
                    throw new NoSuchElementException();
                }
                String next = (String) this.tok.nextElement();
                return next != null ? next.trim() : next;
            }
        };
    }

    public void put(String name, String value) {
        if (value == null) {
            remove(name);
            return;
        }
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = convertValue(value);
        put(n, v);
    }

    public void put(Buffer name, String value) {
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = convertValue(value);
        put(n, v);
    }

    public void put(Buffer name, Buffer value) {
        remove(name);
        if (value == null) {
            return;
        }
        if (!(name instanceof BufferCache.CachedBuffer)) {
            name = HttpHeaders.CACHE.lookup(name);
        }
        if (!(value instanceof BufferCache.CachedBuffer)) {
            value = HttpHeaderValues.CACHE.lookup(value).asImmutableBuffer();
        }
        Field field = new Field(name, value);
        this._fields.add(field);
        this._names.put(name, field);
    }

    public void put(String name, List<?> list) {
        if (list == null || list.size() == 0) {
            remove(name);
            return;
        }
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Object v = list.get(0);
        if (v != null) {
            put(n, HttpHeaderValues.CACHE.lookup(v.toString()));
        } else {
            remove(n);
        }
        if (list.size() > 1) {
            Iterator<?> iter = list.iterator();
            iter.next();
            while (iter.hasNext()) {
                Object v2 = iter.next();
                if (v2 != null) {
                    put(n, HttpHeaderValues.CACHE.lookup(v2.toString()));
                }
            }
        }
    }

    public void add(String name, String value) throws IllegalArgumentException {
        if (value == null) {
            return;
        }
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = convertValue(value);
        add(n, v);
    }

    public void add(Buffer name, Buffer value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("null value");
        }
        if (!(name instanceof BufferCache.CachedBuffer)) {
            name = HttpHeaders.CACHE.lookup(name);
        }
        Buffer name2 = name.asImmutableBuffer();
        if (!(value instanceof BufferCache.CachedBuffer) && HttpHeaderValues.hasKnownValues(HttpHeaders.CACHE.getOrdinal(name2))) {
            value = HttpHeaderValues.CACHE.lookup(value);
        }
        Buffer value2 = value.asImmutableBuffer();
        Field field = this._names.get(name2);
        Field last = null;
        for (Field field2 = field; field2 != null; field2 = field2._next) {
            last = field2;
        }
        Field field3 = new Field(name2, value2);
        this._fields.add(field3);
        if (last == null) {
            this._names.put(name2, field3);
        } else {
            last._next = field3;
        }
    }

    public void remove(String name) {
        remove(HttpHeaders.CACHE.lookup(name));
    }

    public void remove(Buffer name) {
        if (!(name instanceof BufferCache.CachedBuffer)) {
            name = HttpHeaders.CACHE.lookup(name);
        }
        for (Field field = this._names.remove(name); field != null; field = field._next) {
            this._fields.remove(field);
        }
    }

    public long getLongField(String name) throws NumberFormatException {
        Field field = getField(name);
        if (field == null) {
            return -1L;
        }
        return field.getLongValue();
    }

    public long getLongField(Buffer name) throws NumberFormatException {
        Field field = getField(name);
        if (field == null) {
            return -1L;
        }
        return field.getLongValue();
    }

    public long getDateField(String name) {
        String val;
        Field field = getField(name);
        if (field == null || (val = valueParameters(BufferUtil.to8859_1_String(field._value), null)) == null) {
            return -1L;
        }
        long date = __dateParser.get().parse(val);
        if (date == -1) {
            throw new IllegalArgumentException("Cannot convert date: " + val);
        }
        return date;
    }

    public void putLongField(Buffer name, long value) {
        Buffer v = BufferUtil.toBuffer(value);
        put(name, v);
    }

    public void putLongField(String name, long value) {
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = BufferUtil.toBuffer(value);
        put(n, v);
    }

    public void addLongField(String name, long value) {
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = BufferUtil.toBuffer(value);
        add(n, v);
    }

    public void addLongField(Buffer name, long value) {
        Buffer v = BufferUtil.toBuffer(value);
        add(name, v);
    }

    public void putDateField(Buffer name, long date) {
        String d = formatDate(date);
        Buffer v = new ByteArrayBuffer(d);
        put(name, v);
    }

    public void putDateField(String name, long date) {
        Buffer n = HttpHeaders.CACHE.lookup(name);
        putDateField(n, date);
    }

    public void addDateField(String name, long date) {
        String d = formatDate(date);
        Buffer n = HttpHeaders.CACHE.lookup(name);
        Buffer v = new ByteArrayBuffer(d);
        add(n, v);
    }

    public void addSetCookie(HttpCookie cookie) {
        addSetCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), cookie.getComment(), cookie.isSecure(), cookie.isHttpOnly(), cookie.getVersion());
    }

    public void addSetCookie(String name, String value, String domain, String path, long maxAge, String comment, boolean isSecure, boolean isHttpOnly, int version) {
        String val;
        String delim;
        String str = domain;
        String delim2 = __COOKIE_DELIM;
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Bad cookie name");
        }
        StringBuilder buf = new StringBuilder((int) DNSSD.REGISTRATION_DOMAINS);
        QuotedStringTokenizer.quoteIfNeeded(buf, name, __COOKIE_DELIM);
        buf.append('=');
        String start = buf.toString();
        boolean hasDomain = false;
        boolean hasPath = false;
        if (value != null && value.length() > 0) {
            QuotedStringTokenizer.quoteIfNeeded(buf, value, __COOKIE_DELIM);
        }
        if (comment != null && comment.length() > 0) {
            buf.append(";Comment=");
            QuotedStringTokenizer.quoteIfNeeded(buf, comment, __COOKIE_DELIM);
        }
        if (path != null && path.length() > 0) {
            hasPath = true;
            buf.append(";Path=");
            if (path.trim().startsWith("\"")) {
                buf.append(path);
            } else {
                QuotedStringTokenizer.quoteIfNeeded(buf, path, __COOKIE_DELIM);
            }
        }
        if (str != null && domain.length() > 0) {
            hasDomain = true;
            buf.append(";Domain=");
            QuotedStringTokenizer.quoteIfNeeded(buf, str.toLowerCase(Locale.ENGLISH), __COOKIE_DELIM);
        }
        if (maxAge >= 0) {
            buf.append(";Expires=");
            if (maxAge == 0) {
                buf.append(__01Jan1970_COOKIE);
            } else {
                formatCookieDate(buf, System.currentTimeMillis() + (1000 * maxAge));
            }
            if (version > 0) {
                buf.append(";Max-Age=");
                buf.append(maxAge);
            }
        }
        if (isSecure) {
            buf.append(";Secure");
        }
        if (isHttpOnly) {
            buf.append(";HttpOnly");
        }
        String name_value_params = buf.toString();
        Field field = getField(HttpHeaders.SET_COOKIE);
        Field field2 = field;
        Field last = null;
        while (field2 != null) {
            if (field2._value == null) {
                val = null;
            } else {
                val = field2._value.toString();
            }
            if (val != null && val.startsWith(start)) {
                if (!hasDomain && !val.contains("Domain")) {
                    delim = delim2;
                } else if (hasDomain) {
                    StringBuilder sb = new StringBuilder();
                    delim = delim2;
                    sb.append("Domain=");
                    sb.append(str);
                    if (!val.contains(sb.toString())) {
                        continue;
                        last = field2;
                        field2 = field2._next;
                        delim2 = delim;
                        str = domain;
                    }
                }
                if (hasPath || val.contains("Path")) {
                    if (hasPath) {
                        if (val.contains("Path=" + path)) {
                        }
                    } else {
                        continue;
                    }
                    last = field2;
                    field2 = field2._next;
                    delim2 = delim;
                    str = domain;
                }
                this._fields.remove(field2);
                if (last == null) {
                    this._names.put(HttpHeaders.SET_COOKIE_BUFFER, field2._next);
                } else {
                    last._next = field2._next;
                }
                add(HttpHeaders.SET_COOKIE_BUFFER, new ByteArrayBuffer(name_value_params));
                put(HttpHeaders.EXPIRES_BUFFER, __01Jan1970_BUFFER);
            }
            delim = delim2;
            last = field2;
            field2 = field2._next;
            delim2 = delim;
            str = domain;
        }
        add(HttpHeaders.SET_COOKIE_BUFFER, new ByteArrayBuffer(name_value_params));
        put(HttpHeaders.EXPIRES_BUFFER, __01Jan1970_BUFFER);
    }

    public void putTo(Buffer buffer) throws IOException {
        for (int i = 0; i < this._fields.size(); i++) {
            Field field = this._fields.get(i);
            if (field != null) {
                field.putTo(buffer);
            }
        }
        BufferUtil.putCRLF(buffer);
    }

    public String toString() {
        try {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < this._fields.size(); i++) {
                Field field = this._fields.get(i);
                if (field != null) {
                    String tmp = field.getName();
                    if (tmp != null) {
                        buffer.append(tmp);
                    }
                    buffer.append(": ");
                    String tmp2 = field.getValue();
                    if (tmp2 != null) {
                        buffer.append(tmp2);
                    }
                    buffer.append("\r\n");
                }
            }
            buffer.append("\r\n");
            return buffer.toString();
        } catch (Exception e) {
            LOG.warn(e);
            return e.toString();
        }
    }

    public void clear() {
        this._fields.clear();
        this._names.clear();
    }

    public void add(HttpFields fields) {
        if (fields == null) {
            return;
        }
        Enumeration e = fields.getFieldNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            Enumeration values = fields.getValues(name);
            while (values.hasMoreElements()) {
                add(name, values.nextElement());
            }
        }
    }

    public static String valueParameters(String value, Map<String, String> parameters) {
        if (value == null) {
            return null;
        }
        int i = value.indexOf(59);
        if (i < 0) {
            return value;
        }
        if (parameters == null) {
            return value.substring(0, i).trim();
        }
        StringTokenizer tok1 = new QuotedStringTokenizer(value.substring(i), ";", false, true);
        while (tok1.hasMoreTokens()) {
            String token = tok1.nextToken();
            StringTokenizer tok2 = new QuotedStringTokenizer(token, "= ");
            if (tok2.hasMoreTokens()) {
                String paramName = tok2.nextToken();
                String paramVal = tok2.hasMoreTokens() ? tok2.nextToken() : null;
                parameters.put(paramName, paramVal);
            }
        }
        return value.substring(0, i).trim();
    }

    public static Float getQuality(String value) {
        if (value == null) {
            return __zero;
        }
        int qe = value.indexOf(";");
        int qe2 = qe + 1;
        if (qe < 0 || qe2 == value.length()) {
            return __one;
        }
        int qe3 = qe2 + 1;
        if (value.charAt(qe2) == 113) {
            int qe4 = qe3 + 1;
            Map.Entry entry = __qualities.getEntry(value, qe4, value.length() - qe4);
            if (entry != null) {
                return (Float) entry.getValue();
            }
        }
        HashMap params = new HashMap(3);
        valueParameters(value, params);
        String qs = (String) params.get("q");
        Float q = (Float) __qualities.get(qs);
        if (q == null) {
            try {
                return new Float(qs);
            } catch (Exception e) {
                return __one;
            }
        }
        return q;
    }

    public static List qualityList(Enumeration e) {
        if (e == null || !e.hasMoreElements()) {
            return Collections.EMPTY_LIST;
        }
        Object list = null;
        Object qual = null;
        while (e.hasMoreElements()) {
            String v = e.nextElement().toString();
            Float q = getQuality(v);
            if (q.floatValue() >= 0.001d) {
                list = LazyList.add(list, v);
                qual = LazyList.add(qual, q);
            }
        }
        List vl = LazyList.getList(list, false);
        if (vl.size() < 2) {
            return vl;
        }
        List ql = LazyList.getList(qual, false);
        Float last = __zero;
        int i = vl.size();
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                Float q2 = (Float) ql.get(i2);
                if (last.compareTo(q2) > 0) {
                    Object tmp = vl.get(i2);
                    vl.set(i2, vl.get(i2 + 1));
                    vl.set(i2 + 1, tmp);
                    ql.set(i2, ql.get(i2 + 1));
                    ql.set(i2 + 1, q2);
                    last = __zero;
                    i2 = vl.size();
                } else {
                    last = q2;
                }
                i = i2;
            } else {
                ql.clear();
                return vl;
            }
        }
    }

    /* loaded from: classes.dex */
    public static final class Field {
        private Buffer _name;
        private Field _next;
        private Buffer _value;

        private Field(Buffer name, Buffer value) {
            this._name = name;
            this._value = value;
            this._next = null;
        }

        public void putTo(Buffer buffer) throws IOException {
            int o = this._name instanceof BufferCache.CachedBuffer ? ((BufferCache.CachedBuffer) this._name).getOrdinal() : -1;
            if (o >= 0) {
                buffer.put(this._name);
            } else {
                int s = this._name.getIndex();
                int e = this._name.putIndex();
                while (s < e) {
                    int s2 = s + 1;
                    byte b = this._name.peek(s);
                    if (b != 10 && b != 13 && b != 58) {
                        buffer.put(b);
                    }
                    s = s2;
                }
            }
            buffer.put(HttpTokens.COLON);
            buffer.put(HttpTokens.SPACE);
            int o2 = this._value instanceof BufferCache.CachedBuffer ? ((BufferCache.CachedBuffer) this._value).getOrdinal() : -1;
            if (o2 >= 0) {
                buffer.put(this._value);
            } else {
                int s3 = this._value.getIndex();
                int e2 = this._value.putIndex();
                while (s3 < e2) {
                    int s4 = s3 + 1;
                    byte b2 = this._value.peek(s3);
                    if (b2 != 10 && b2 != 13) {
                        buffer.put(b2);
                    }
                    s3 = s4;
                }
            }
            BufferUtil.putCRLF(buffer);
        }

        public String getName() {
            return BufferUtil.to8859_1_String(this._name);
        }

        Buffer getNameBuffer() {
            return this._name;
        }

        public int getNameOrdinal() {
            return HttpHeaders.CACHE.getOrdinal(this._name);
        }

        public String getValue() {
            return BufferUtil.to8859_1_String(this._value);
        }

        public Buffer getValueBuffer() {
            return this._value;
        }

        public int getValueOrdinal() {
            return HttpHeaderValues.CACHE.getOrdinal(this._value);
        }

        public int getIntValue() {
            return (int) getLongValue();
        }

        public long getLongValue() {
            return BufferUtil.toLong(this._value);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(getName());
            sb.append("=");
            sb.append(this._value);
            sb.append(this._next == null ? "" : "->");
            sb.append("]");
            return sb.toString();
        }
    }
}
