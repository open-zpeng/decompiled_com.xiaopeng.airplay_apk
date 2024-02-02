package org.eclipse.jetty.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class MimeTypes {
    private static final int FORM_ENCODED_ORDINAL = 1;
    private static final int MESSAGE_HTTP_ORDINAL = 2;
    private static final int MULTIPART_BYTERANGES_ORDINAL = 3;
    private static final int TEXT_HTML_8859_1_ORDINAL = 8;
    private static final int TEXT_HTML_ORDINAL = 4;
    private static final int TEXT_HTML_UTF_8_ORDINAL = 11;
    private static final int TEXT_JSON_ORDINAL = 7;
    private static final int TEXT_JSON_UTF_8_ORDINAL = 14;
    private static final int TEXT_PLAIN_8859_1_ORDINAL = 9;
    private static final int TEXT_PLAIN_ORDINAL = 5;
    private static final int TEXT_PLAIN_UTF_8_ORDINAL = 12;
    private static final int TEXT_XML_8859_1_ORDINAL = 10;
    private static final int TEXT_XML_ORDINAL = 6;
    private static final int TEXT_XML_UTF_8_ORDINAL = 13;
    private Map _mimeMap;
    private static final Logger LOG = Log.getLogger(MimeTypes.class);
    private static int __index = 15;
    public static final BufferCache CACHE = new BufferCache();
    public static final String FORM_ENCODED = "application/x-www-form-urlencoded";
    public static final BufferCache.CachedBuffer FORM_ENCODED_BUFFER = CACHE.add(FORM_ENCODED, 1);
    public static final String MESSAGE_HTTP = "message/http";
    public static final BufferCache.CachedBuffer MESSAGE_HTTP_BUFFER = CACHE.add(MESSAGE_HTTP, 2);
    public static final String MULTIPART_BYTERANGES = "multipart/byteranges";
    public static final BufferCache.CachedBuffer MULTIPART_BYTERANGES_BUFFER = CACHE.add(MULTIPART_BYTERANGES, 3);
    public static final String TEXT_HTML = "text/html";
    public static final BufferCache.CachedBuffer TEXT_HTML_BUFFER = CACHE.add(TEXT_HTML, 4);
    public static final String TEXT_PLAIN = "text/plain";
    public static final BufferCache.CachedBuffer TEXT_PLAIN_BUFFER = CACHE.add(TEXT_PLAIN, 5);
    public static final String TEXT_XML = "text/xml";
    public static final BufferCache.CachedBuffer TEXT_XML_BUFFER = CACHE.add(TEXT_XML, 6);
    public static final String TEXT_JSON = "text/json";
    public static final BufferCache.CachedBuffer TEXT_JSON_BUFFER = CACHE.add(TEXT_JSON, 7);
    public static final String TEXT_HTML_8859_1 = "text/html;charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_HTML_8859_1_BUFFER = CACHE.add(TEXT_HTML_8859_1, 8);
    public static final String TEXT_PLAIN_8859_1 = "text/plain;charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_PLAIN_8859_1_BUFFER = CACHE.add(TEXT_PLAIN_8859_1, 9);
    public static final String TEXT_XML_8859_1 = "text/xml;charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_XML_8859_1_BUFFER = CACHE.add(TEXT_XML_8859_1, 10);
    public static final String TEXT_HTML_UTF_8 = "text/html;charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_HTML_UTF_8_BUFFER = CACHE.add(TEXT_HTML_UTF_8, 11);
    public static final String TEXT_PLAIN_UTF_8 = "text/plain;charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_PLAIN_UTF_8_BUFFER = CACHE.add(TEXT_PLAIN_UTF_8, 12);
    public static final String TEXT_XML_UTF_8 = "text/xml;charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_XML_UTF_8_BUFFER = CACHE.add(TEXT_XML_UTF_8, 13);
    public static final String TEXT_JSON_UTF_8 = "text/json;charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_JSON_UTF_8_BUFFER = CACHE.add(TEXT_JSON_UTF_8, 14);
    private static final String TEXT_HTML__8859_1 = "text/html; charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_HTML__8859_1_BUFFER = CACHE.add(TEXT_HTML__8859_1, 8);
    private static final String TEXT_PLAIN__8859_1 = "text/plain; charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_PLAIN__8859_1_BUFFER = CACHE.add(TEXT_PLAIN__8859_1, 9);
    private static final String TEXT_XML__8859_1 = "text/xml; charset=ISO-8859-1";
    public static final BufferCache.CachedBuffer TEXT_XML__8859_1_BUFFER = CACHE.add(TEXT_XML__8859_1, 10);
    private static final String TEXT_HTML__UTF_8 = "text/html; charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_HTML__UTF_8_BUFFER = CACHE.add(TEXT_HTML__UTF_8, 11);
    private static final String TEXT_PLAIN__UTF_8 = "text/plain; charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_PLAIN__UTF_8_BUFFER = CACHE.add(TEXT_PLAIN__UTF_8, 12);
    private static final String TEXT_XML__UTF_8 = "text/xml; charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_XML__UTF_8_BUFFER = CACHE.add(TEXT_XML__UTF_8, 13);
    private static final String TEXT_JSON__UTF_8 = "text/json; charset=UTF-8";
    public static final BufferCache.CachedBuffer TEXT_JSON__UTF_8_BUFFER = CACHE.add(TEXT_JSON__UTF_8, 14);
    private static final Map __dftMimeMap = new HashMap();
    private static final Map __encodings = new HashMap();

    static {
        try {
            ResourceBundle mime = ResourceBundle.getBundle("org/eclipse/jetty/http/mime");
            Enumeration i = mime.getKeys();
            while (i.hasMoreElements()) {
                String ext = i.nextElement();
                String m = mime.getString(ext);
                __dftMimeMap.put(StringUtil.asciiToLowerCase(ext), normalizeMimeType(m));
            }
        } catch (MissingResourceException e) {
            LOG.warn(e.toString(), new Object[0]);
            LOG.debug(e);
        }
        try {
            ResourceBundle encoding = ResourceBundle.getBundle("org/eclipse/jetty/http/encoding");
            Enumeration i2 = encoding.getKeys();
            while (i2.hasMoreElements()) {
                Buffer type = normalizeMimeType(i2.nextElement());
                __encodings.put(type, encoding.getString(type.toString()));
            }
        } catch (MissingResourceException e2) {
            LOG.warn(e2.toString(), new Object[0]);
            LOG.debug(e2);
        }
        TEXT_HTML_BUFFER.setAssociate(StringUtil.__ISO_8859_1, TEXT_HTML_8859_1_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("ISO_8859_1", TEXT_HTML_8859_1_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("iso-8859-1", TEXT_HTML_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate(StringUtil.__ISO_8859_1, TEXT_PLAIN_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("ISO_8859_1", TEXT_PLAIN_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("iso-8859-1", TEXT_PLAIN_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate(StringUtil.__ISO_8859_1, TEXT_XML_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate("ISO_8859_1", TEXT_XML_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate("iso-8859-1", TEXT_XML_8859_1_BUFFER);
        TEXT_HTML_BUFFER.setAssociate(StringUtil.__UTF8, TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate(StringUtil.__UTF8Alt, TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("utf8", TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("utf-8", TEXT_HTML_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate(StringUtil.__UTF8, TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate(StringUtil.__UTF8Alt, TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("utf8", TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("utf-8", TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate(StringUtil.__UTF8, TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate(StringUtil.__UTF8Alt, TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("utf8", TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("utf-8", TEXT_XML_UTF_8_BUFFER);
        TEXT_JSON_BUFFER.setAssociate(StringUtil.__UTF8, TEXT_JSON_UTF_8_BUFFER);
        TEXT_JSON_BUFFER.setAssociate(StringUtil.__UTF8Alt, TEXT_JSON_UTF_8_BUFFER);
        TEXT_JSON_BUFFER.setAssociate("utf8", TEXT_JSON_UTF_8_BUFFER);
        TEXT_JSON_BUFFER.setAssociate("utf-8", TEXT_JSON_UTF_8_BUFFER);
    }

    public synchronized Map getMimeMap() {
        return this._mimeMap;
    }

    public void setMimeMap(Map mimeMap) {
        if (mimeMap == null) {
            this._mimeMap = null;
            return;
        }
        Map m = new HashMap();
        for (Map.Entry entry : mimeMap.entrySet()) {
            m.put(entry.getKey(), normalizeMimeType(entry.getValue().toString()));
        }
        this._mimeMap = m;
    }

    public Buffer getMimeByExtension(String filename) {
        Buffer type = null;
        if (filename != null) {
            int i = -1;
            while (type == null) {
                i = filename.indexOf(".", i + 1);
                if (i < 0 || i >= filename.length()) {
                    break;
                }
                String ext = StringUtil.asciiToLowerCase(filename.substring(i + 1));
                if (this._mimeMap != null) {
                    type = (Buffer) this._mimeMap.get(ext);
                }
                if (type == null) {
                    type = (Buffer) __dftMimeMap.get(ext);
                }
            }
        }
        if (type == null) {
            if (this._mimeMap != null) {
                type = (Buffer) this._mimeMap.get("*");
            }
            if (type == null) {
                return (Buffer) __dftMimeMap.get("*");
            }
            return type;
        }
        return type;
    }

    public void addMimeMapping(String extension, String type) {
        if (this._mimeMap == null) {
            this._mimeMap = new HashMap();
        }
        this._mimeMap.put(StringUtil.asciiToLowerCase(extension), normalizeMimeType(type));
    }

    private static synchronized Buffer normalizeMimeType(String type) {
        Buffer b;
        synchronized (MimeTypes.class) {
            b = CACHE.get(type);
            if (b == null) {
                BufferCache bufferCache = CACHE;
                int i = __index;
                __index = i + 1;
                b = bufferCache.add(type, i);
            }
        }
        return b;
    }

    /* JADX WARN: Code restructure failed: missing block: B:109:0x00b7, code lost:
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String getCharsetFromContentType(org.eclipse.jetty.io.Buffer r9) {
        /*
            Method dump skipped, instructions count: 262
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.http.MimeTypes.getCharsetFromContentType(org.eclipse.jetty.io.Buffer):java.lang.String");
    }
}
