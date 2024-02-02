package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Response implements HttpServletResponse {
    public static final String HTTP_ONLY_COMMENT = "__HTTP_ONLY__";
    private static final Logger LOG = Log.getLogger(Response.class);
    public static final int NONE = 0;
    public static final String SET_INCLUDE_HEADER_PREFIX = "org.eclipse.jetty.server.include.";
    public static final int STREAM = 1;
    public static final int WRITER = 2;
    private static final int __MIN_BUFFER_SIZE = 1;
    private BufferCache.CachedBuffer _cachedMimeType;
    private String _characterEncoding;
    private final AbstractHttpConnection _connection;
    private String _contentType;
    private boolean _explicitEncoding;
    private Locale _locale;
    private String _mimeType;
    private volatile int _outputState;
    private String _reason;
    private int _status = 200;
    private PrintWriter _writer;

    public static Response getResponse(HttpServletResponse response) {
        if (response instanceof Response) {
            return (Response) response;
        }
        return AbstractHttpConnection.getCurrentConnection().getResponse();
    }

    public Response(AbstractHttpConnection connection) {
        this._connection = connection;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void recycle() {
        this._status = 200;
        this._reason = null;
        this._locale = null;
        this._mimeType = null;
        this._cachedMimeType = null;
        this._characterEncoding = null;
        this._explicitEncoding = false;
        this._contentType = null;
        this._writer = null;
        this._outputState = 0;
    }

    public void addCookie(HttpCookie cookie) {
        this._connection.getResponseFields().addSetCookie(cookie);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addCookie(Cookie cookie) {
        String comment = cookie.getComment();
        boolean http_only = false;
        if (comment != null) {
            int i = comment.indexOf(HTTP_ONLY_COMMENT);
            if (i >= 0) {
                http_only = true;
                comment = comment.replace(HTTP_ONLY_COMMENT, "").trim();
                if (comment.length() == 0) {
                    comment = null;
                }
            }
        }
        this._connection.getResponseFields().addSetCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), comment, cookie.getSecure(), http_only || cookie.isHttpOnly(), cookie.getVersion());
    }

    @Override // javax.servlet.http.HttpServletResponse
    public boolean containsHeader(String name) {
        return this._connection.getResponseFields().containsKey(name);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeURL(String url) {
        Request request = this._connection.getRequest();
        SessionManager sessionManager = request.getSessionManager();
        if (sessionManager == null) {
            return url;
        }
        HttpURI uri = null;
        if (sessionManager.isCheckingRemoteSessionIdEncoding() && URIUtil.hasScheme(url)) {
            uri = new HttpURI(url);
            String path = uri.getPath();
            String path2 = path == null ? "" : path;
            int port = uri.getPort();
            if (port < 0) {
                port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            if (!request.getServerName().equalsIgnoreCase(uri.getHost()) || request.getServerPort() != port || !path2.startsWith(request.getContextPath())) {
                return url;
            }
        }
        String sessionURLPrefix = sessionManager.getSessionIdPathParameterNamePrefix();
        if (sessionURLPrefix == null) {
            return url;
        }
        if (url == null) {
            return null;
        }
        if ((sessionManager.isUsingCookies() && request.isRequestedSessionIdFromCookie()) || !sessionManager.isUsingURLs()) {
            int prefix = url.indexOf(sessionURLPrefix);
            if (prefix != -1) {
                int suffix = url.indexOf("?", prefix);
                if (suffix < 0) {
                    suffix = url.indexOf("#", prefix);
                }
                if (suffix <= prefix) {
                    return url.substring(0, prefix);
                }
                return url.substring(0, prefix) + url.substring(suffix);
            }
            return url;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return url;
        }
        if (!sessionManager.isValid(session)) {
            return url;
        }
        String id = sessionManager.getNodeId(session);
        if (uri == null) {
            uri = new HttpURI(url);
        }
        int prefix2 = url.indexOf(sessionURLPrefix);
        if (prefix2 != -1) {
            int suffix2 = url.indexOf("?", prefix2);
            if (suffix2 < 0) {
                suffix2 = url.indexOf("#", prefix2);
            }
            if (suffix2 <= prefix2) {
                return url.substring(0, sessionURLPrefix.length() + prefix2) + id;
            }
            return url.substring(0, sessionURLPrefix.length() + prefix2) + id + url.substring(suffix2);
        }
        int suffix3 = url.indexOf(63);
        if (suffix3 < 0) {
            suffix3 = url.indexOf(35);
        }
        if (suffix3 < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            sb.append((("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) && uri.getPath() == null) ? "/" : "");
            sb.append(sessionURLPrefix);
            sb.append(id);
            return sb.toString();
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(url.substring(0, suffix3));
        sb2.append((("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) && uri.getPath() == null) ? "/" : "");
        sb2.append(sessionURLPrefix);
        sb2.append(id);
        sb2.append(url.substring(suffix3));
        return sb2.toString();
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override // javax.servlet.http.HttpServletResponse
    @Deprecated
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override // javax.servlet.http.HttpServletResponse
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendError(int code, String message) throws IOException {
        if (this._connection.isIncluding()) {
            return;
        }
        if (isCommitted()) {
            Logger logger = LOG;
            logger.warn("Committed before " + code + " " + message, new Object[0]);
        }
        resetBuffer();
        this._characterEncoding = null;
        setHeader(HttpHeaders.EXPIRES, null);
        setHeader(HttpHeaders.LAST_MODIFIED, null);
        setHeader(HttpHeaders.CACHE_CONTROL, null);
        setHeader(HttpHeaders.CONTENT_TYPE, null);
        setHeader(HttpHeaders.CONTENT_LENGTH, null);
        this._outputState = 0;
        setStatus(code, message);
        if (message == null) {
            message = HttpStatus.getMessage(code);
        }
        if (code != 204 && code != 304 && code != 206 && code >= 200) {
            Request request = this._connection.getRequest();
            ErrorHandler error_handler = null;
            ContextHandler.Context context = request.getContext();
            if (context != null) {
                error_handler = context.getContextHandler().getErrorHandler();
            }
            if (error_handler == null) {
                error_handler = (ErrorHandler) this._connection.getConnector().getServer().getBean(ErrorHandler.class);
            }
            if (error_handler != null) {
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, new Integer(code));
                request.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
                request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());
                request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME, request.getServletName());
                error_handler.handle(null, this._connection.getRequest(), this._connection.getRequest(), this);
            } else {
                setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
                setContentType(MimeTypes.TEXT_HTML_8859_1);
                ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(2048);
                if (message != null) {
                    message = StringUtil.replace(StringUtil.replace(StringUtil.replace(message, "&", "&amp;"), "<", "&lt;"), ">", "&gt;");
                }
                String uri = request.getRequestURI();
                if (uri != null) {
                    uri = StringUtil.replace(StringUtil.replace(StringUtil.replace(uri, "&", "&amp;"), "<", "&lt;"), ">", "&gt;");
                }
                writer.write("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html;charset=ISO-8859-1\"/>\n");
                writer.write("<title>Error ");
                writer.write(Integer.toString(code));
                writer.write(' ');
                if (message == null) {
                    message = HttpStatus.getMessage(code);
                }
                writer.write(message);
                writer.write("</title>\n</head>\n<body>\n<h2>HTTP ERROR: ");
                writer.write(Integer.toString(code));
                writer.write("</h2>\n<p>Problem accessing ");
                writer.write(uri);
                writer.write(". Reason:\n<pre>    ");
                writer.write(message);
                writer.write("</pre>");
                writer.write("</p>\n");
                if (this._connection.getServer().getSendServerVersion()) {
                    writer.write("<hr /><i><small>Powered by Jetty:// ");
                    writer.write(Server.getVersion());
                    writer.write("</small></i>");
                }
                for (int i = 0; i < 20; i++) {
                    writer.write("\n                                                ");
                }
                writer.write("\n</body>\n</html>\n");
                writer.flush();
                setContentLength(writer.size());
                writer.writeTo(getOutputStream());
                writer.destroy();
            }
        } else if (code != 206) {
            this._connection.getRequestFields().remove(HttpHeaders.CONTENT_TYPE_BUFFER);
            this._connection.getRequestFields().remove(HttpHeaders.CONTENT_LENGTH_BUFFER);
            this._characterEncoding = null;
            this._mimeType = null;
            this._cachedMimeType = null;
        }
        complete();
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendError(int sc) throws IOException {
        if (sc == -1) {
            this._connection.getEndPoint().close();
        } else if (sc == 102) {
            sendProcessing();
        } else {
            sendError(sc, null);
        }
    }

    public void sendProcessing() throws IOException {
        if (this._connection.isExpecting102Processing() && !isCommitted()) {
            ((HttpGenerator) this._connection.getGenerator()).send1xx(HttpStatus.PROCESSING_102);
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendRedirect(String location) throws IOException {
        String location2;
        if (this._connection.isIncluding()) {
            return;
        }
        if (location == null) {
            throw new IllegalArgumentException();
        }
        if (!URIUtil.hasScheme(location)) {
            StringBuilder buf = this._connection.getRequest().getRootURL();
            if (location.startsWith("/")) {
                location2 = URIUtil.canonicalPath(location);
            } else {
                String path = this._connection.getRequest().getRequestURI();
                String parent = path.endsWith("/") ? path : URIUtil.parentPath(path);
                location2 = URIUtil.canonicalPath(URIUtil.addPaths(parent, location));
                if (!location2.startsWith("/")) {
                    buf.append('/');
                }
            }
            if (location2 == null) {
                throw new IllegalStateException("path cannot be above root");
            }
            buf.append(location2);
            location = buf.toString();
        }
        resetBuffer();
        setHeader(HttpHeaders.LOCATION, location);
        setStatus(302);
        complete();
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setDateHeader(String name, long date) {
        if (!this._connection.isIncluding()) {
            this._connection.getResponseFields().putDateField(name, date);
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addDateHeader(String name, long date) {
        if (!this._connection.isIncluding()) {
            this._connection.getResponseFields().addDateField(name, date);
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setHeader(String name, String value) {
        if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
            setContentType(value);
            return;
        }
        if (this._connection.isIncluding()) {
            if (name.startsWith(SET_INCLUDE_HEADER_PREFIX)) {
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            } else {
                return;
            }
        }
        this._connection.getResponseFields().put(name, value);
        if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
            if (value == null) {
                this._connection._generator.setContentLength(-1L);
            } else {
                this._connection._generator.setContentLength(Long.parseLong(value));
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public Collection<String> getHeaderNames() {
        HttpFields fields = this._connection.getResponseFields();
        return fields.getFieldNamesCollection();
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String getHeader(String name) {
        return this._connection.getResponseFields().getStringField(name);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public Collection<String> getHeaders(String name) {
        HttpFields fields = this._connection.getResponseFields();
        Collection<String> i = fields.getValuesCollection(name);
        if (i == null) {
            return Collections.EMPTY_LIST;
        }
        return i;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addHeader(String name, String value) {
        if (this._connection.isIncluding()) {
            if (name.startsWith(SET_INCLUDE_HEADER_PREFIX)) {
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            } else {
                return;
            }
        }
        if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
            setContentType(value);
            return;
        }
        this._connection.getResponseFields().add(name, value);
        if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
            this._connection._generator.setContentLength(Long.parseLong(value));
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setIntHeader(String name, int value) {
        if (!this._connection.isIncluding()) {
            this._connection.getResponseFields().putLongField(name, value);
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                this._connection._generator.setContentLength(value);
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addIntHeader(String name, int value) {
        if (!this._connection.isIncluding()) {
            this._connection.getResponseFields().addLongField(name, value);
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                this._connection._generator.setContentLength(value);
            }
        }
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setStatus(int sc) {
        setStatus(sc, null);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setStatus(int sc, String sm) {
        if (sc <= 0) {
            throw new IllegalArgumentException();
        }
        if (!this._connection.isIncluding()) {
            this._status = sc;
            this._reason = sm;
        }
    }

    @Override // javax.servlet.ServletResponse
    public String getCharacterEncoding() {
        if (this._characterEncoding == null) {
            this._characterEncoding = StringUtil.__ISO_8859_1;
        }
        return this._characterEncoding;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getSetCharacterEncoding() {
        return this._characterEncoding;
    }

    @Override // javax.servlet.ServletResponse
    public String getContentType() {
        return this._contentType;
    }

    @Override // javax.servlet.ServletResponse
    public ServletOutputStream getOutputStream() throws IOException {
        if (this._outputState != 0 && this._outputState != 1) {
            throw new IllegalStateException("WRITER");
        }
        ServletOutputStream out = this._connection.getOutputStream();
        this._outputState = 1;
        return out;
    }

    public boolean isWriting() {
        return this._outputState == 2;
    }

    public boolean isOutputing() {
        return this._outputState != 0;
    }

    @Override // javax.servlet.ServletResponse
    public PrintWriter getWriter() throws IOException {
        if (this._outputState != 0 && this._outputState != 2) {
            throw new IllegalStateException("STREAM");
        }
        if (this._writer == null) {
            String encoding = this._characterEncoding;
            if (encoding == null) {
                if (this._cachedMimeType != null) {
                    encoding = MimeTypes.getCharsetFromContentType(this._cachedMimeType);
                }
                if (encoding == null) {
                    encoding = StringUtil.__ISO_8859_1;
                }
                setCharacterEncoding(encoding);
            }
            this._writer = this._connection.getPrintWriter(encoding);
        }
        this._outputState = 2;
        return this._writer;
    }

    @Override // javax.servlet.ServletResponse
    public void setCharacterEncoding(String encoding) {
        BufferCache.CachedBuffer content_type;
        if (!this._connection.isIncluding() && this._outputState == 0 && !isCommitted()) {
            this._explicitEncoding = true;
            if (encoding == null) {
                if (this._characterEncoding != null) {
                    this._characterEncoding = null;
                    if (this._cachedMimeType != null) {
                        this._contentType = this._cachedMimeType.toString();
                    } else if (this._mimeType != null) {
                        this._contentType = this._mimeType;
                    } else {
                        this._contentType = null;
                    }
                    if (this._contentType == null) {
                        this._connection.getResponseFields().remove(HttpHeaders.CONTENT_TYPE_BUFFER);
                        return;
                    } else {
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    }
                }
                return;
            }
            this._characterEncoding = encoding;
            if (this._contentType != null) {
                int i0 = this._contentType.indexOf(59);
                if (i0 < 0) {
                    this._contentType = null;
                    if (this._cachedMimeType != null && (content_type = this._cachedMimeType.getAssociate(this._characterEncoding)) != null) {
                        this._contentType = content_type.toString();
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, content_type);
                    }
                    if (this._contentType == null) {
                        this._contentType = this._mimeType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    }
                    return;
                }
                int i1 = this._contentType.indexOf("charset=", i0);
                if (i1 < 0) {
                    this._contentType += ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                } else {
                    int i8 = i1 + 8;
                    int i2 = this._contentType.indexOf(" ", i8);
                    if (i2 < 0) {
                        this._contentType = this._contentType.substring(0, i8) + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                    } else {
                        this._contentType = this._contentType.substring(0, i8) + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ") + this._contentType.substring(i2);
                    }
                }
                this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
            }
        }
    }

    @Override // javax.servlet.ServletResponse
    public void setContentLength(int len) {
        if (isCommitted() || this._connection.isIncluding()) {
            return;
        }
        this._connection._generator.setContentLength(len);
        if (len > 0) {
            this._connection.getResponseFields().putLongField(HttpHeaders.CONTENT_LENGTH, len);
            if (this._connection._generator.isAllContentWritten()) {
                if (this._outputState == 2) {
                    this._writer.close();
                } else if (this._outputState == 1) {
                    try {
                        getOutputStream().close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void setLongContentLength(long len) {
        if (isCommitted() || this._connection.isIncluding()) {
            return;
        }
        this._connection._generator.setContentLength(len);
        this._connection.getResponseFields().putLongField(HttpHeaders.CONTENT_LENGTH, len);
    }

    @Override // javax.servlet.ServletResponse
    public void setContentType(String contentType) {
        String str;
        if (isCommitted() || this._connection.isIncluding()) {
            return;
        }
        if (contentType == null) {
            if (this._locale == null) {
                this._characterEncoding = null;
            }
            this._mimeType = null;
            this._cachedMimeType = null;
            this._contentType = null;
            this._connection.getResponseFields().remove(HttpHeaders.CONTENT_TYPE_BUFFER);
            return;
        }
        int i0 = contentType.indexOf(59);
        if (i0 > 0) {
            this._mimeType = contentType.substring(0, i0).trim();
            this._cachedMimeType = MimeTypes.CACHE.get(this._mimeType);
            int i1 = contentType.indexOf("charset=", i0 + 1);
            if (i1 >= 0) {
                this._explicitEncoding = true;
                int i8 = i1 + 8;
                int i2 = contentType.indexOf(32, i8);
                if (this._outputState == 2) {
                    if ((i1 == i0 + 1 && i2 < 0) || (i1 == i0 + 2 && i2 < 0 && contentType.charAt(i0 + 1) == ' ')) {
                        if (this._cachedMimeType != null) {
                            BufferCache.CachedBuffer content_type = this._cachedMimeType.getAssociate(this._characterEncoding);
                            if (content_type != null) {
                                this._contentType = content_type.toString();
                                this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, content_type);
                                return;
                            }
                            this._contentType = this._mimeType + ";charset=" + this._characterEncoding;
                            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                            return;
                        }
                        this._contentType = this._mimeType + ";charset=" + this._characterEncoding;
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    } else if (i2 < 0) {
                        this._contentType = contentType.substring(0, i1) + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    } else {
                        this._contentType = contentType.substring(0, i1) + contentType.substring(i2) + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    }
                } else if ((i1 == i0 + 1 && i2 < 0) || (i1 == i0 + 2 && i2 < 0 && contentType.charAt(i0 + 1) == ' ')) {
                    this._cachedMimeType = MimeTypes.CACHE.get(this._mimeType);
                    this._characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8));
                    if (this._cachedMimeType != null) {
                        BufferCache.CachedBuffer content_type2 = this._cachedMimeType.getAssociate(this._characterEncoding);
                        if (content_type2 != null) {
                            this._contentType = content_type2.toString();
                            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, content_type2);
                            return;
                        }
                        this._contentType = contentType;
                        this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                        return;
                    }
                    this._contentType = contentType;
                    this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                    return;
                } else if (i2 > 0) {
                    this._characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8, i2));
                    this._contentType = contentType;
                    this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                    return;
                } else {
                    this._characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8));
                    this._contentType = contentType;
                    this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                    return;
                }
            }
            this._cachedMimeType = null;
            if (this._characterEncoding == null) {
                str = contentType;
            } else {
                str = contentType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
            }
            this._contentType = str;
            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
            return;
        }
        this._mimeType = contentType;
        this._cachedMimeType = MimeTypes.CACHE.get(this._mimeType);
        if (this._characterEncoding != null) {
            if (this._cachedMimeType != null) {
                BufferCache.CachedBuffer content_type3 = this._cachedMimeType.getAssociate(this._characterEncoding);
                if (content_type3 != null) {
                    this._contentType = content_type3.toString();
                    this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, content_type3);
                    return;
                }
                this._contentType = this._mimeType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
                this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
                return;
            }
            this._contentType = contentType + ";charset=" + QuotedStringTokenizer.quoteIfNeeded(this._characterEncoding, ";= ");
            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
        } else if (this._cachedMimeType != null) {
            this._contentType = this._cachedMimeType.toString();
            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._cachedMimeType);
        } else {
            this._contentType = contentType;
            this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
        }
    }

    @Override // javax.servlet.ServletResponse
    public void setBufferSize(int size) {
        if (isCommitted() || getContentCount() > 0) {
            throw new IllegalStateException("Committed or content written");
        }
        if (size <= 0) {
            size = 1;
        }
        this._connection.getGenerator().increaseContentBufferSize(size);
    }

    @Override // javax.servlet.ServletResponse
    public int getBufferSize() {
        return this._connection.getGenerator().getContentBufferSize();
    }

    @Override // javax.servlet.ServletResponse
    public void flushBuffer() throws IOException {
        this._connection.flushResponse();
    }

    @Override // javax.servlet.ServletResponse
    public void reset() {
        resetBuffer();
        fwdReset();
        this._status = 200;
        this._reason = null;
        HttpFields response_fields = this._connection.getResponseFields();
        response_fields.clear();
        String connection = this._connection.getRequestFields().getStringField(HttpHeaders.CONNECTION_BUFFER);
        if (connection != null) {
            String[] values = connection.split(",");
            for (int i = 0; values != null && i < values.length; i++) {
                BufferCache.CachedBuffer cb = HttpHeaderValues.CACHE.get(values[0].trim());
                if (cb != null) {
                    int ordinal = cb.getOrdinal();
                    if (ordinal == 1) {
                        response_fields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                    } else if (ordinal != 5) {
                        if (ordinal == 8) {
                            response_fields.put(HttpHeaders.CONNECTION_BUFFER, "TE");
                        }
                    } else if (HttpVersions.HTTP_1_0.equalsIgnoreCase(this._connection.getRequest().getProtocol())) {
                        response_fields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.KEEP_ALIVE);
                    }
                }
            }
        }
    }

    public void reset(boolean preserveCookies) {
        if (!preserveCookies) {
            reset();
            return;
        }
        HttpFields response_fields = this._connection.getResponseFields();
        ArrayList<String> cookieValues = new ArrayList<>(5);
        Enumeration<String> vals = response_fields.getValues(HttpHeaders.SET_COOKIE);
        while (vals.hasMoreElements()) {
            cookieValues.add(vals.nextElement());
        }
        reset();
        Iterator i$ = cookieValues.iterator();
        while (i$.hasNext()) {
            String v = i$.next();
            response_fields.add(HttpHeaders.SET_COOKIE, v);
        }
    }

    public void fwdReset() {
        resetBuffer();
        this._writer = null;
        this._outputState = 0;
    }

    @Override // javax.servlet.ServletResponse
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Committed");
        }
        this._connection.getGenerator().resetBuffer();
    }

    @Override // javax.servlet.ServletResponse
    public boolean isCommitted() {
        return this._connection.isResponseCommitted();
    }

    @Override // javax.servlet.ServletResponse
    public void setLocale(Locale locale) {
        String charset;
        if (locale == null || isCommitted() || this._connection.isIncluding()) {
            return;
        }
        this._locale = locale;
        this._connection.getResponseFields().put(HttpHeaders.CONTENT_LANGUAGE_BUFFER, locale.toString().replace('_', '-'));
        if (!this._explicitEncoding && this._outputState == 0 && this._connection.getRequest().getContext() != null && (charset = this._connection.getRequest().getContext().getContextHandler().getLocaleEncoding(locale)) != null && charset.length() > 0) {
            this._characterEncoding = charset;
            String type = getContentType();
            if (type != null) {
                this._characterEncoding = charset;
                int semi = type.indexOf(59);
                if (semi < 0) {
                    this._mimeType = type;
                    this._contentType = type + ";charset=" + charset;
                } else {
                    this._mimeType = type.substring(0, semi);
                    String str = this._mimeType + ";charset=" + charset;
                    this._mimeType = str;
                    this._contentType = str;
                }
                this._cachedMimeType = MimeTypes.CACHE.get(this._mimeType);
                this._connection.getResponseFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, this._contentType);
            }
        }
    }

    @Override // javax.servlet.ServletResponse
    public Locale getLocale() {
        if (this._locale == null) {
            return Locale.getDefault();
        }
        return this._locale;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public int getStatus() {
        return this._status;
    }

    public String getReason() {
        return this._reason;
    }

    public void complete() throws IOException {
        this._connection.completeResponse();
    }

    public long getContentCount() {
        if (this._connection == null || this._connection.getGenerator() == null) {
            return -1L;
        }
        return this._connection.getGenerator().getContentWritten();
    }

    public HttpFields getHttpFields() {
        return this._connection.getResponseFields();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ");
        sb.append(this._status);
        sb.append(" ");
        sb.append(this._reason == null ? "" : this._reason);
        sb.append(System.getProperty("line.separator"));
        sb.append(this._connection.getResponseFields().toString());
        return sb.toString();
    }

    /* loaded from: classes.dex */
    private static class NullOutput extends ServletOutputStream {
        private NullOutput() {
        }

        @Override // java.io.OutputStream
        public void write(int b) throws IOException {
        }

        @Override // javax.servlet.ServletOutputStream
        public void print(String s) throws IOException {
        }

        @Override // javax.servlet.ServletOutputStream
        public void println(String s) throws IOException {
        }

        @Override // java.io.OutputStream
        public void write(byte[] b, int off, int len) throws IOException {
        }
    }
}
