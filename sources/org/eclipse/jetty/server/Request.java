package org.eclipse.jetty.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.DirectNIOBuffer;
import org.eclipse.jetty.io.nio.IndirectNIOBuffer;
import org.eclipse.jetty.io.nio.NIOBuffer;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.MultiPartInputStream;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Request implements HttpServletRequest {
    private static final int _STREAM = 1;
    private static final String __ASYNC_FWD = "org.eclipse.asyncfwd";
    public static final String __MULTIPART_CONFIG_ELEMENT = "org.eclipse.multipartConfig";
    public static final String __MULTIPART_CONTEXT = "org.eclipse.multiPartContext";
    public static final String __MULTIPART_INPUT_STREAM = "org.eclipse.multiPartInputStream";
    private static final int __NONE = 0;
    private static final int __READER = 2;
    private volatile Attributes _attributes;
    private Authentication _authentication;
    private MultiMap<String> _baseParameters;
    private String _characterEncoding;
    protected AbstractHttpConnection _connection;
    private ContextHandler.Context _context;
    private String _contextPath;
    private CookieCutter _cookies;
    private long _dispatchTime;
    private DispatcherType _dispatcherType;
    private EndPoint _endp;
    private String _method;
    private MultiPartInputStream _multiPartInputStream;
    private boolean _newContext;
    private MultiMap<String> _parameters;
    private boolean _paramsExtracted;
    private String _pathInfo;
    private int _port;
    private String _queryEncoding;
    private String _queryString;
    private BufferedReader _reader;
    private String _readerEncoding;
    private String _remoteAddr;
    private String _remoteHost;
    private Object _requestAttributeListeners;
    private String _requestURI;
    private String _requestedSessionId;
    private Map<Object, HttpSession> _savedNewSessions;
    private UserIdentity.Scope _scope;
    private String _serverName;
    private String _servletPath;
    private HttpSession _session;
    private SessionManager _sessionManager;
    private long _timeStamp;
    private Buffer _timeStampBuffer;
    private HttpURI _uri;
    private static final Logger LOG = Log.getLogger(Request.class);
    private static final Collection __defaultLocale = Collections.singleton(Locale.getDefault());
    protected final AsyncContinuation _async = new AsyncContinuation();
    private boolean _asyncSupported = true;
    private boolean _cookiesExtracted = false;
    private boolean _dns = false;
    private boolean _handled = false;
    private int _inputState = 0;
    private String _protocol = HttpVersions.HTTP_1_1;
    private boolean _requestedSessionIdFromCookie = false;
    private String _scheme = "http";

    /* loaded from: classes.dex */
    public static class MultiPartCleanerListener implements ServletRequestListener {
        @Override // javax.servlet.ServletRequestListener
        public void requestDestroyed(ServletRequestEvent sre) {
            MultiPartInputStream mpis = (MultiPartInputStream) sre.getServletRequest().getAttribute(Request.__MULTIPART_INPUT_STREAM);
            if (mpis != null) {
                ContextHandler.Context context = (ContextHandler.Context) sre.getServletRequest().getAttribute(Request.__MULTIPART_CONTEXT);
                if (context == sre.getServletContext()) {
                    try {
                        mpis.deleteParts();
                    } catch (MultiException e) {
                        sre.getServletContext().log("Errors deleting multipart tmp files", e);
                    }
                }
            }
        }

        @Override // javax.servlet.ServletRequestListener
        public void requestInitialized(ServletRequestEvent sre) {
        }
    }

    public static Request getRequest(HttpServletRequest request) {
        if (request instanceof Request) {
            return (Request) request;
        }
        return AbstractHttpConnection.getCurrentConnection().getRequest();
    }

    public Request() {
    }

    public Request(AbstractHttpConnection connection) {
        setConnection(connection);
    }

    public void addEventListener(EventListener listener) {
        if (listener instanceof ServletRequestAttributeListener) {
            this._requestAttributeListeners = LazyList.add(this._requestAttributeListeners, listener);
        }
        if (listener instanceof ContinuationListener) {
            throw new IllegalArgumentException(listener.getClass().toString());
        }
        if (listener instanceof AsyncListener) {
            throw new IllegalArgumentException(listener.getClass().toString());
        }
    }

    public void extractParameters() {
        int content_length;
        MultiMap<String> multiMap;
        if (this._baseParameters == null) {
            this._baseParameters = new MultiMap<>(16);
        }
        if (this._paramsExtracted) {
            if (multiMap == null) {
                return;
            }
            return;
        }
        this._paramsExtracted = true;
        try {
            if (this._uri != null && this._uri.hasQuery()) {
                if (this._queryEncoding == null) {
                    this._uri.decodeQueryTo(this._baseParameters);
                } else {
                    try {
                        this._uri.decodeQueryTo(this._baseParameters, this._queryEncoding);
                    } catch (UnsupportedEncodingException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.warn(e);
                        } else {
                            LOG.warn(e.toString(), new Object[0]);
                        }
                    }
                }
            }
            String encoding = getCharacterEncoding();
            String content_type = getContentType();
            if (content_type != null && content_type.length() > 0) {
                content_type = HttpFields.valueParameters(content_type, null);
                if (MimeTypes.FORM_ENCODED.equalsIgnoreCase(content_type) && this._inputState == 0 && ((HttpMethods.POST.equals(getMethod()) || HttpMethods.PUT.equals(getMethod())) && (content_length = getContentLength()) != 0)) {
                    int maxFormContentSize = -1;
                    int maxFormKeys = -1;
                    try {
                        if (this._context != null) {
                            maxFormContentSize = this._context.getContextHandler().getMaxFormContentSize();
                            maxFormKeys = this._context.getContextHandler().getMaxFormKeys();
                        }
                        if (maxFormContentSize < 0) {
                            Object obj = this._connection.getConnector().getServer().getAttribute("org.eclipse.jetty.server.Request.maxFormContentSize");
                            if (obj == null) {
                                maxFormContentSize = 200000;
                            } else if (obj instanceof Number) {
                                Number size = (Number) obj;
                                maxFormContentSize = size.intValue();
                            } else if (obj instanceof String) {
                                maxFormContentSize = Integer.valueOf((String) obj).intValue();
                            }
                        }
                        if (maxFormKeys < 0) {
                            Object obj2 = this._connection.getConnector().getServer().getAttribute("org.eclipse.jetty.server.Request.maxFormKeys");
                            if (obj2 == null) {
                                maxFormKeys = 1000;
                            } else if (obj2 instanceof Number) {
                                Number keys = (Number) obj2;
                                maxFormKeys = keys.intValue();
                            } else if (obj2 instanceof String) {
                                maxFormKeys = Integer.valueOf((String) obj2).intValue();
                            }
                        }
                        if (content_length > maxFormContentSize && maxFormContentSize > 0) {
                            throw new IllegalStateException("Form too large " + content_length + ">" + maxFormContentSize);
                        }
                        InputStream in = getInputStream();
                        UrlEncoded.decodeTo(in, this._baseParameters, encoding, content_length < 0 ? maxFormContentSize : -1, maxFormKeys);
                    } catch (IOException e2) {
                        if (LOG.isDebugEnabled()) {
                            LOG.warn(e2);
                        } else {
                            LOG.warn(e2.toString(), new Object[0]);
                        }
                    }
                }
            }
            if (this._parameters == null) {
                this._parameters = this._baseParameters;
            } else if (this._parameters != this._baseParameters) {
                for (Map.Entry<String, Object> entry : this._baseParameters.entrySet()) {
                    String name = entry.getKey();
                    Object values = entry.getValue();
                    for (int i = 0; i < LazyList.size(values); i++) {
                        this._parameters.add(name, LazyList.get(values, i));
                    }
                }
            }
            if (content_type != null && content_type.length() > 0 && content_type.startsWith("multipart/form-data") && getAttribute(__MULTIPART_CONFIG_ELEMENT) != null) {
                try {
                    getParts();
                } catch (IOException e3) {
                    if (LOG.isDebugEnabled()) {
                        LOG.warn(e3);
                    } else {
                        LOG.warn(e3.toString(), new Object[0]);
                    }
                } catch (ServletException e4) {
                    if (LOG.isDebugEnabled()) {
                        LOG.warn(e4);
                    } else {
                        LOG.warn(e4.toString(), new Object[0]);
                    }
                }
            }
            if (this._parameters == null) {
                this._parameters = this._baseParameters;
            }
        } finally {
            if (this._parameters == null) {
                this._parameters = this._baseParameters;
            }
        }
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext getAsyncContext() {
        if (this._async.isInitial() && !this._async.isAsyncStarted()) {
            throw new IllegalStateException(this._async.getStatusString());
        }
        return this._async;
    }

    public AsyncContinuation getAsyncContinuation() {
        return this._async;
    }

    @Override // javax.servlet.ServletRequest
    public Object getAttribute(String name) {
        if ("org.eclipse.jetty.io.EndPoint.maxIdleTime".equalsIgnoreCase(name)) {
            return new Long(getConnection().getEndPoint().getMaxIdleTime());
        }
        Object attr = this._attributes == null ? null : this._attributes.getAttribute(name);
        if (attr == null && Continuation.ATTRIBUTE.equals(name)) {
            return this._async;
        }
        return attr;
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration getAttributeNames() {
        if (this._attributes == null) {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
        return AttributesMap.getAttributeNamesCopy(this._attributes);
    }

    public Attributes getAttributes() {
        if (this._attributes == null) {
            this._attributes = new AttributesMap();
        }
        return this._attributes;
    }

    public Authentication getAuthentication() {
        return this._authentication;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getAuthType() {
        if (this._authentication instanceof Authentication.Deferred) {
            setAuthentication(((Authentication.Deferred) this._authentication).authenticate(this));
        }
        if (this._authentication instanceof Authentication.User) {
            return ((Authentication.User) this._authentication).getAuthMethod();
        }
        return null;
    }

    @Override // javax.servlet.ServletRequest
    public String getCharacterEncoding() {
        return this._characterEncoding;
    }

    public AbstractHttpConnection getConnection() {
        return this._connection;
    }

    @Override // javax.servlet.ServletRequest
    public int getContentLength() {
        return (int) this._connection.getRequestFields().getLongField(HttpHeaders.CONTENT_LENGTH_BUFFER);
    }

    public long getContentRead() {
        if (this._connection == null || this._connection.getParser() == null) {
            return -1L;
        }
        return ((HttpParser) this._connection.getParser()).getContentRead();
    }

    @Override // javax.servlet.ServletRequest
    public String getContentType() {
        return this._connection.getRequestFields().getStringField(HttpHeaders.CONTENT_TYPE_BUFFER);
    }

    public ContextHandler.Context getContext() {
        return this._context;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getContextPath() {
        return this._contextPath;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Cookie[] getCookies() {
        if (this._cookiesExtracted) {
            if (this._cookies == null) {
                return null;
            }
            return this._cookies.getCookies();
        }
        this._cookiesExtracted = true;
        Enumeration enm = this._connection.getRequestFields().getValues(HttpHeaders.COOKIE_BUFFER);
        if (enm != null) {
            if (this._cookies == null) {
                this._cookies = new CookieCutter();
            }
            while (enm.hasMoreElements()) {
                String c = enm.nextElement();
                this._cookies.addCookieField(c);
            }
        }
        if (this._cookies == null) {
            return null;
        }
        return this._cookies.getCookies();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public long getDateHeader(String name) {
        return this._connection.getRequestFields().getDateField(name);
    }

    @Override // javax.servlet.ServletRequest
    public DispatcherType getDispatcherType() {
        return this._dispatcherType;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getHeader(String name) {
        return this._connection.getRequestFields().getStringField(name);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Enumeration getHeaderNames() {
        return this._connection.getRequestFields().getFieldNames();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Enumeration getHeaders(String name) {
        Enumeration e = this._connection.getRequestFields().getValues(name);
        if (e == null) {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
        return e;
    }

    public int getInputState() {
        return this._inputState;
    }

    @Override // javax.servlet.ServletRequest
    public ServletInputStream getInputStream() throws IOException {
        if (this._inputState != 0 && this._inputState != 1) {
            throw new IllegalStateException("READER");
        }
        this._inputState = 1;
        return this._connection.getInputStream();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public int getIntHeader(String name) {
        return (int) this._connection.getRequestFields().getLongField(name);
    }

    @Override // javax.servlet.ServletRequest
    public String getLocalAddr() {
        if (this._endp == null) {
            return null;
        }
        return this._endp.getLocalAddr();
    }

    @Override // javax.servlet.ServletRequest
    public Locale getLocale() {
        Enumeration enm = this._connection.getRequestFields().getValues(HttpHeaders.ACCEPT_LANGUAGE, HttpFields.__separators);
        if (enm == null || !enm.hasMoreElements()) {
            return Locale.getDefault();
        }
        List acceptLanguage = HttpFields.qualityList(enm);
        if (acceptLanguage.size() == 0) {
            return Locale.getDefault();
        }
        int size = acceptLanguage.size();
        if (size > 0) {
            String language = HttpFields.valueParameters((String) acceptLanguage.get(0), null);
            String country = "";
            int dash = language.indexOf(45);
            if (dash > -1) {
                country = language.substring(dash + 1).trim();
                language = language.substring(0, dash).trim();
            }
            return new Locale(language, country);
        }
        return Locale.getDefault();
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration getLocales() {
        Enumeration enm = this._connection.getRequestFields().getValues(HttpHeaders.ACCEPT_LANGUAGE, HttpFields.__separators);
        if (enm == null || !enm.hasMoreElements()) {
            return Collections.enumeration(__defaultLocale);
        }
        List acceptLanguage = HttpFields.qualityList(enm);
        if (acceptLanguage.size() == 0) {
            return Collections.enumeration(__defaultLocale);
        }
        int size = acceptLanguage.size();
        Object langs = null;
        for (int i = 0; i < size; i++) {
            String language = HttpFields.valueParameters((String) acceptLanguage.get(i), null);
            String country = "";
            int dash = language.indexOf(45);
            if (dash > -1) {
                country = language.substring(dash + 1).trim();
                language = language.substring(0, dash).trim();
            }
            langs = LazyList.add(LazyList.ensureSize(langs, size), new Locale(language, country));
        }
        int i2 = LazyList.size(langs);
        if (i2 == 0) {
            return Collections.enumeration(__defaultLocale);
        }
        return Collections.enumeration(LazyList.getList(langs));
    }

    @Override // javax.servlet.ServletRequest
    public String getLocalName() {
        if (this._endp == null) {
            return null;
        }
        if (this._dns) {
            return this._endp.getLocalHost();
        }
        String local = this._endp.getLocalAddr();
        if (local != null && local.indexOf(58) >= 0) {
            return "[" + local + "]";
        }
        return local;
    }

    @Override // javax.servlet.ServletRequest
    public int getLocalPort() {
        if (this._endp == null) {
            return 0;
        }
        return this._endp.getLocalPort();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getMethod() {
        return this._method;
    }

    @Override // javax.servlet.ServletRequest
    public String getParameter(String name) {
        if (!this._paramsExtracted) {
            extractParameters();
        }
        return (String) this._parameters.getValue(name, 0);
    }

    @Override // javax.servlet.ServletRequest
    public Map getParameterMap() {
        if (!this._paramsExtracted) {
            extractParameters();
        }
        return Collections.unmodifiableMap(this._parameters.toStringArrayMap());
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration getParameterNames() {
        if (!this._paramsExtracted) {
            extractParameters();
        }
        return Collections.enumeration(this._parameters.keySet());
    }

    public MultiMap<String> getParameters() {
        return this._parameters;
    }

    @Override // javax.servlet.ServletRequest
    public String[] getParameterValues(String name) {
        if (!this._paramsExtracted) {
            extractParameters();
        }
        List<Object> vals = this._parameters.getValues(name);
        if (vals == null) {
            return null;
        }
        return (String[]) vals.toArray(new String[vals.size()]);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getPathInfo() {
        return this._pathInfo;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getPathTranslated() {
        if (this._pathInfo == null || this._context == null) {
            return null;
        }
        return this._context.getRealPath(this._pathInfo);
    }

    @Override // javax.servlet.ServletRequest
    public String getProtocol() {
        return this._protocol;
    }

    public String getQueryEncoding() {
        return this._queryEncoding;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getQueryString() {
        if (this._queryString == null && this._uri != null) {
            if (this._queryEncoding == null) {
                this._queryString = this._uri.getQuery();
            } else {
                this._queryString = this._uri.getQuery(this._queryEncoding);
            }
        }
        return this._queryString;
    }

    @Override // javax.servlet.ServletRequest
    public BufferedReader getReader() throws IOException {
        if (this._inputState != 0 && this._inputState != 2) {
            throw new IllegalStateException("STREAMED");
        }
        if (this._inputState == 2) {
            return this._reader;
        }
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = StringUtil.__ISO_8859_1;
        }
        if (this._reader == null || !encoding.equalsIgnoreCase(this._readerEncoding)) {
            final ServletInputStream in = getInputStream();
            this._readerEncoding = encoding;
            this._reader = new BufferedReader(new InputStreamReader(in, encoding)) { // from class: org.eclipse.jetty.server.Request.1
                @Override // java.io.BufferedReader, java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
                public void close() throws IOException {
                    in.close();
                }
            };
        }
        this._inputState = 2;
        return this._reader;
    }

    @Override // javax.servlet.ServletRequest
    public String getRealPath(String path) {
        if (this._context == null) {
            return null;
        }
        return this._context.getRealPath(path);
    }

    @Override // javax.servlet.ServletRequest
    public String getRemoteAddr() {
        if (this._remoteAddr != null) {
            return this._remoteAddr;
        }
        if (this._endp == null) {
            return null;
        }
        return this._endp.getRemoteAddr();
    }

    @Override // javax.servlet.ServletRequest
    public String getRemoteHost() {
        if (this._dns) {
            if (this._remoteHost != null) {
                return this._remoteHost;
            }
            if (this._endp == null) {
                return null;
            }
            return this._endp.getRemoteHost();
        }
        return getRemoteAddr();
    }

    @Override // javax.servlet.ServletRequest
    public int getRemotePort() {
        if (this._endp == null) {
            return 0;
        }
        return this._endp.getRemotePort();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getRemoteUser() {
        Principal p = getUserPrincipal();
        if (p == null) {
            return null;
        }
        return p.getName();
    }

    @Override // javax.servlet.ServletRequest
    public RequestDispatcher getRequestDispatcher(String path) {
        String relTo;
        String path2 = URIUtil.compactPath(path);
        if (path2 == null || this._context == null) {
            return null;
        }
        if (!path2.startsWith("/")) {
            String relTo2 = URIUtil.addPaths(this._servletPath, this._pathInfo);
            int slash = relTo2.lastIndexOf("/");
            if (slash > 1) {
                relTo = relTo2.substring(0, slash + 1);
            } else {
                relTo = "/";
            }
            path2 = URIUtil.addPaths(relTo, path2);
        }
        return this._context.getRequestDispatcher(path2);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getRequestedSessionId() {
        return this._requestedSessionId;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getRequestURI() {
        if (this._requestURI == null && this._uri != null) {
            this._requestURI = this._uri.getPathAndParam();
        }
        return this._requestURI;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer(48);
        synchronized (url) {
            String scheme = getScheme();
            int port = getServerPort();
            url.append(scheme);
            url.append("://");
            url.append(getServerName());
            if (this._port > 0 && ((scheme.equalsIgnoreCase("http") && port != 80) || (scheme.equalsIgnoreCase("https") && port != 443))) {
                url.append(':');
                url.append(this._port);
            }
            url.append(getRequestURI());
        }
        return url;
    }

    public Response getResponse() {
        return this._connection._response;
    }

    public StringBuilder getRootURL() {
        StringBuilder url = new StringBuilder(48);
        String scheme = getScheme();
        int port = getServerPort();
        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if (port > 0 && ((scheme.equalsIgnoreCase("http") && port != 80) || (scheme.equalsIgnoreCase("https") && port != 443))) {
            url.append(':');
            url.append(port);
        }
        return url;
    }

    @Override // javax.servlet.ServletRequest
    public String getScheme() {
        return this._scheme;
    }

    @Override // javax.servlet.ServletRequest
    public String getServerName() {
        if (this._serverName != null) {
            return this._serverName;
        }
        if (this._uri == null) {
            throw new IllegalStateException("No uri");
        }
        this._serverName = this._uri.getHost();
        this._port = this._uri.getPort();
        if (this._serverName != null) {
            return this._serverName;
        }
        Buffer hostPort = this._connection.getRequestFields().get(HttpHeaders.HOST_BUFFER);
        if (hostPort != null) {
            int i = hostPort.putIndex();
            while (true) {
                int i2 = i - 1;
                if (i <= hostPort.getIndex()) {
                    break;
                }
                char ch = (char) (255 & hostPort.peek(i2));
                if (ch == ':') {
                    this._serverName = BufferUtil.to8859_1_String(hostPort.peek(hostPort.getIndex(), i2 - hostPort.getIndex()));
                    try {
                        this._port = BufferUtil.toInt(hostPort.peek(i2 + 1, (hostPort.putIndex() - i2) - 1));
                    } catch (NumberFormatException e) {
                        try {
                            if (this._connection != null) {
                                this._connection._generator.sendError(400, "Bad Host header", null, true);
                            }
                        } catch (IOException e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                    return this._serverName;
                } else if (ch == ']') {
                    break;
                } else {
                    i = i2;
                }
            }
            if (this._serverName == null || this._port < 0) {
                this._serverName = BufferUtil.to8859_1_String(hostPort);
                this._port = 0;
            }
            return this._serverName;
        }
        if (this._connection != null) {
            this._serverName = getLocalName();
            this._port = getLocalPort();
            if (this._serverName != null && !StringUtil.ALL_INTERFACES.equals(this._serverName)) {
                return this._serverName;
            }
        }
        try {
            this._serverName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e2) {
            LOG.ignore(e2);
        }
        return this._serverName;
    }

    @Override // javax.servlet.ServletRequest
    public int getServerPort() {
        if (this._port <= 0) {
            if (this._serverName == null) {
                getServerName();
            }
            if (this._port <= 0) {
                if (this._serverName != null && this._uri != null) {
                    this._port = this._uri.getPort();
                } else {
                    this._port = this._endp == null ? 0 : this._endp.getLocalPort();
                }
            }
        }
        if (this._port <= 0) {
            if (getScheme().equalsIgnoreCase("https")) {
                return 443;
            }
            return 80;
        }
        return this._port;
    }

    @Override // javax.servlet.ServletRequest
    public ServletContext getServletContext() {
        return this._context;
    }

    public String getServletName() {
        if (this._scope != null) {
            return this._scope.getName();
        }
        return null;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public String getServletPath() {
        if (this._servletPath == null) {
            this._servletPath = "";
        }
        return this._servletPath;
    }

    public ServletResponse getServletResponse() {
        return this._connection.getResponse();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public HttpSession getSession(boolean create) {
        if (this._session != null) {
            if (this._sessionManager != null && !this._sessionManager.isValid(this._session)) {
                this._session = null;
            } else {
                return this._session;
            }
        }
        if (create) {
            if (this._sessionManager == null) {
                throw new IllegalStateException("No SessionManager");
            }
            this._session = this._sessionManager.newHttpSession(this);
            HttpCookie cookie = this._sessionManager.getSessionCookie(this._session, getContextPath(), isSecure());
            if (cookie != null) {
                this._connection.getResponse().addCookie(cookie);
            }
            return this._session;
        }
        return null;
    }

    public SessionManager getSessionManager() {
        return this._sessionManager;
    }

    public long getTimeStamp() {
        return this._timeStamp;
    }

    public Buffer getTimeStampBuffer() {
        if (this._timeStampBuffer == null && this._timeStamp > 0) {
            this._timeStampBuffer = HttpFields.__dateCache.formatBuffer(this._timeStamp);
        }
        return this._timeStampBuffer;
    }

    public HttpURI getUri() {
        return this._uri;
    }

    public UserIdentity getUserIdentity() {
        if (this._authentication instanceof Authentication.Deferred) {
            setAuthentication(((Authentication.Deferred) this._authentication).authenticate(this));
        }
        if (this._authentication instanceof Authentication.User) {
            return ((Authentication.User) this._authentication).getUserIdentity();
        }
        return null;
    }

    public UserIdentity getResolvedUserIdentity() {
        if (this._authentication instanceof Authentication.User) {
            return ((Authentication.User) this._authentication).getUserIdentity();
        }
        return null;
    }

    public UserIdentity.Scope getUserIdentityScope() {
        return this._scope;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Principal getUserPrincipal() {
        if (this._authentication instanceof Authentication.Deferred) {
            setAuthentication(((Authentication.Deferred) this._authentication).authenticate(this));
        }
        if (this._authentication instanceof Authentication.User) {
            UserIdentity user = ((Authentication.User) this._authentication).getUserIdentity();
            return user.getUserPrincipal();
        }
        return null;
    }

    public long getDispatchTime() {
        return this._dispatchTime;
    }

    public boolean isHandled() {
        return this._handled;
    }

    @Override // javax.servlet.ServletRequest
    public boolean isAsyncStarted() {
        return this._async.isAsyncStarted();
    }

    @Override // javax.servlet.ServletRequest
    public boolean isAsyncSupported() {
        return this._asyncSupported;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean isRequestedSessionIdFromCookie() {
        return this._requestedSessionId != null && this._requestedSessionIdFromCookie;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean isRequestedSessionIdFromUrl() {
        return (this._requestedSessionId == null || this._requestedSessionIdFromCookie) ? false : true;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean isRequestedSessionIdFromURL() {
        return (this._requestedSessionId == null || this._requestedSessionIdFromCookie) ? false : true;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean isRequestedSessionIdValid() {
        HttpSession session;
        return (this._requestedSessionId == null || (session = getSession(false)) == null || !this._sessionManager.getSessionIdManager().getClusterId(this._requestedSessionId).equals(this._sessionManager.getClusterId(session))) ? false : true;
    }

    @Override // javax.servlet.ServletRequest
    public boolean isSecure() {
        return this._connection.isConfidential(this);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean isUserInRole(String role) {
        if (this._authentication instanceof Authentication.Deferred) {
            setAuthentication(((Authentication.Deferred) this._authentication).authenticate(this));
        }
        if (this._authentication instanceof Authentication.User) {
            return ((Authentication.User) this._authentication).isUserInRole(this._scope, role);
        }
        return false;
    }

    public HttpSession recoverNewSession(Object key) {
        if (this._savedNewSessions == null) {
            return null;
        }
        return this._savedNewSessions.get(key);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void recycle() {
        if (this._inputState == 2) {
            try {
                int r = this._reader.read();
                while (r != -1) {
                    r = this._reader.read();
                }
            } catch (Exception e) {
                LOG.ignore(e);
                this._reader = null;
            }
        }
        setAuthentication(Authentication.NOT_CHECKED);
        this._async.recycle();
        this._asyncSupported = true;
        this._handled = false;
        if (this._context != null) {
            throw new IllegalStateException("Request in context!");
        }
        if (this._attributes != null) {
            this._attributes.clearAttributes();
        }
        this._characterEncoding = null;
        this._contextPath = null;
        if (this._cookies != null) {
            this._cookies.reset();
        }
        this._cookiesExtracted = false;
        this._context = null;
        this._serverName = null;
        this._method = null;
        this._pathInfo = null;
        this._port = 0;
        this._protocol = HttpVersions.HTTP_1_1;
        this._queryEncoding = null;
        this._queryString = null;
        this._requestedSessionId = null;
        this._requestedSessionIdFromCookie = false;
        this._session = null;
        this._sessionManager = null;
        this._requestURI = null;
        this._scope = null;
        this._scheme = "http";
        this._servletPath = null;
        this._timeStamp = 0L;
        this._timeStampBuffer = null;
        this._uri = null;
        if (this._baseParameters != null) {
            this._baseParameters.clear();
        }
        this._parameters = null;
        this._paramsExtracted = false;
        this._inputState = 0;
        if (this._savedNewSessions != null) {
            this._savedNewSessions.clear();
        }
        this._savedNewSessions = null;
        this._multiPartInputStream = null;
    }

    @Override // javax.servlet.ServletRequest
    public void removeAttribute(String name) {
        Object old_value = this._attributes == null ? null : this._attributes.getAttribute(name);
        if (this._attributes != null) {
            this._attributes.removeAttribute(name);
        }
        if (old_value != null && this._requestAttributeListeners != null) {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(this._context, this, name, old_value);
            int size = LazyList.size(this._requestAttributeListeners);
            for (int i = 0; i < size; i++) {
                EventListener listener = (ServletRequestAttributeListener) LazyList.get(this._requestAttributeListeners, i);
                if (listener instanceof ServletRequestAttributeListener) {
                    ServletRequestAttributeListener l = (ServletRequestAttributeListener) listener;
                    l.attributeRemoved(event);
                }
            }
        }
    }

    public void removeEventListener(EventListener listener) {
        this._requestAttributeListeners = LazyList.remove(this._requestAttributeListeners, listener);
    }

    public void saveNewSession(Object key, HttpSession session) {
        if (this._savedNewSessions == null) {
            this._savedNewSessions = new HashMap();
        }
        this._savedNewSessions.put(key, session);
    }

    public void setAsyncSupported(boolean supported) {
        this._asyncSupported = supported;
    }

    @Override // javax.servlet.ServletRequest
    public void setAttribute(String name, Object value) {
        Object old_value = this._attributes == null ? null : this._attributes.getAttribute(name);
        if (name.startsWith("org.eclipse.jetty.")) {
            if ("org.eclipse.jetty.server.Request.queryEncoding".equals(name)) {
                setQueryEncoding(value != null ? value.toString() : null);
            } else if ("org.eclipse.jetty.server.sendContent".equals(name)) {
                try {
                    ((AbstractHttpConnection.Output) getServletResponse().getOutputStream()).sendContent(value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ("org.eclipse.jetty.server.ResponseBuffer".equals(name)) {
                try {
                    ByteBuffer byteBuffer = (ByteBuffer) value;
                    synchronized (byteBuffer) {
                        NIOBuffer buffer = byteBuffer.isDirect() ? new DirectNIOBuffer(byteBuffer, true) : new IndirectNIOBuffer(byteBuffer, true);
                        ((AbstractHttpConnection.Output) getServletResponse().getOutputStream()).sendResponse(buffer);
                    }
                } catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
            } else if ("org.eclipse.jetty.io.EndPoint.maxIdleTime".equalsIgnoreCase(name)) {
                try {
                    getConnection().getEndPoint().setMaxIdleTime(Integer.valueOf(value.toString()).intValue());
                } catch (IOException e3) {
                    throw new RuntimeException(e3);
                }
            }
        }
        if (this._attributes == null) {
            this._attributes = new AttributesMap();
        }
        this._attributes.setAttribute(name, value);
        if (this._requestAttributeListeners != null) {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(this._context, this, name, old_value == null ? value : old_value);
            int size = LazyList.size(this._requestAttributeListeners);
            for (int i = 0; i < size; i++) {
                EventListener listener = (ServletRequestAttributeListener) LazyList.get(this._requestAttributeListeners, i);
                if (listener instanceof ServletRequestAttributeListener) {
                    ServletRequestAttributeListener l = (ServletRequestAttributeListener) listener;
                    if (old_value == null) {
                        l.attributeAdded(event);
                    } else if (value == null) {
                        l.attributeRemoved(event);
                    } else {
                        l.attributeReplaced(event);
                    }
                }
            }
        }
    }

    public void setAttributes(Attributes attributes) {
        this._attributes = attributes;
    }

    public void setAuthentication(Authentication authentication) {
        this._authentication = authentication;
    }

    @Override // javax.servlet.ServletRequest
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        if (this._inputState != 0) {
            return;
        }
        this._characterEncoding = encoding;
        if (!StringUtil.isUTF8(encoding)) {
            "".getBytes(encoding);
        }
    }

    public void setCharacterEncodingUnchecked(String encoding) {
        this._characterEncoding = encoding;
    }

    protected final void setConnection(AbstractHttpConnection connection) {
        this._connection = connection;
        this._async.setConnection(connection);
        this._endp = connection.getEndPoint();
        this._dns = connection.getResolveNames();
    }

    public void setContentType(String contentType) {
        this._connection.getRequestFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, contentType);
    }

    public void setContext(ContextHandler.Context context) {
        this._newContext = this._context != context;
        this._context = context;
    }

    public boolean takeNewContext() {
        boolean nc = this._newContext;
        this._newContext = false;
        return nc;
    }

    public void setContextPath(String contextPath) {
        this._contextPath = contextPath;
    }

    public void setCookies(Cookie[] cookies) {
        if (this._cookies == null) {
            this._cookies = new CookieCutter();
        }
        this._cookies.setCookies(cookies);
    }

    public void setDispatcherType(DispatcherType type) {
        this._dispatcherType = type;
    }

    public void setHandled(boolean h) {
        this._handled = h;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public void setParameters(MultiMap<String> parameters) {
        this._parameters = parameters == null ? this._baseParameters : parameters;
        if (this._paramsExtracted && this._parameters == null) {
            throw new IllegalStateException();
        }
    }

    public void setPathInfo(String pathInfo) {
        this._pathInfo = pathInfo;
    }

    public void setProtocol(String protocol) {
        this._protocol = protocol;
    }

    public void setQueryEncoding(String queryEncoding) {
        this._queryEncoding = queryEncoding;
        this._queryString = null;
    }

    public void setQueryString(String queryString) {
        this._queryString = queryString;
        this._queryEncoding = null;
    }

    public void setRemoteAddr(String addr) {
        this._remoteAddr = addr;
    }

    public void setRemoteHost(String host) {
        this._remoteHost = host;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this._requestedSessionId = requestedSessionId;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdCookie) {
        this._requestedSessionIdFromCookie = requestedSessionIdCookie;
    }

    public void setRequestURI(String requestURI) {
        this._requestURI = requestURI;
    }

    public void setScheme(String scheme) {
        this._scheme = scheme;
    }

    public void setServerName(String host) {
        this._serverName = host;
    }

    public void setServerPort(int port) {
        this._port = port;
    }

    public void setServletPath(String servletPath) {
        this._servletPath = servletPath;
    }

    public void setSession(HttpSession session) {
        this._session = session;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this._sessionManager = sessionManager;
    }

    public void setTimeStamp(long ts) {
        this._timeStamp = ts;
    }

    public void setUri(HttpURI uri) {
        this._uri = uri;
    }

    public void setUserIdentityScope(UserIdentity.Scope scope) {
        this._scope = scope;
    }

    public void setDispatchTime(long value) {
        this._dispatchTime = value;
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext startAsync() throws IllegalStateException {
        if (!this._asyncSupported) {
            throw new IllegalStateException("!asyncSupported");
        }
        this._async.startAsync();
        return this._async;
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (!this._asyncSupported) {
            throw new IllegalStateException("!asyncSupported");
        }
        this._async.startAsync(this._context, servletRequest, servletResponse);
        return this._async;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._handled ? "[" : "(");
        sb.append(getMethod());
        sb.append(" ");
        sb.append(this._uri);
        sb.append(this._handled ? "]@" : ")@");
        sb.append(hashCode());
        sb.append(" ");
        sb.append(super.toString());
        return sb.toString();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        if (this._authentication instanceof Authentication.Deferred) {
            setAuthentication(((Authentication.Deferred) this._authentication).authenticate(this, response));
            return !(this._authentication instanceof Authentication.ResponseSent);
        }
        response.sendError(401);
        return false;
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Part getPart(String name) throws IOException, ServletException {
        getParts();
        return this._multiPartInputStream.getPart(name);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public Collection<Part> getParts() throws IOException, ServletException {
        if (getContentType() == null || !getContentType().startsWith("multipart/form-data")) {
            throw new ServletException("Content-Type != multipart/form-data");
        }
        if (this._multiPartInputStream == null) {
            this._multiPartInputStream = (MultiPartInputStream) getAttribute(__MULTIPART_INPUT_STREAM);
        }
        if (this._multiPartInputStream == null) {
            MultipartConfigElement config = (MultipartConfigElement) getAttribute(__MULTIPART_CONFIG_ELEMENT);
            if (config == null) {
                throw new IllegalStateException("No multipart config for servlet");
            }
            this._multiPartInputStream = new MultiPartInputStream(getInputStream(), getContentType(), config, this._context != null ? (File) this._context.getAttribute(ServletContext.TEMPDIR) : null);
            setAttribute(__MULTIPART_INPUT_STREAM, this._multiPartInputStream);
            setAttribute(__MULTIPART_CONTEXT, this._context);
            Collection<Part> parts = this._multiPartInputStream.getParts();
            for (Part p : parts) {
                MultiPartInputStream.MultiPart mp = (MultiPartInputStream.MultiPart) p;
                if (mp.getContentDispositionFilename() == null) {
                    String charset = null;
                    if (mp.getContentType() != null) {
                        charset = MimeTypes.getCharsetFromContentType(new ByteArrayBuffer(mp.getContentType()));
                    }
                    ByteArrayOutputStream os = null;
                    InputStream is = mp.getInputStream();
                    try {
                        os = new ByteArrayOutputStream();
                        IO.copy(is, os);
                        String content = new String(os.toByteArray(), charset == null ? StringUtil.__UTF8 : charset);
                        getParameter("");
                        getParameters().add(mp.getName(), content);
                    } finally {
                        IO.close((OutputStream) os);
                        IO.close(is);
                    }
                }
            }
        }
        return this._multiPartInputStream.getParts();
    }

    @Override // javax.servlet.http.HttpServletRequest
    public void login(String username, String password) throws ServletException {
        if (this._authentication instanceof Authentication.Deferred) {
            this._authentication = ((Authentication.Deferred) this._authentication).login(username, password, this);
            if (this._authentication == null) {
                throw new ServletException();
            }
            return;
        }
        throw new ServletException("Authenticated as " + this._authentication);
    }

    @Override // javax.servlet.http.HttpServletRequest
    public void logout() throws ServletException {
        if (this._authentication instanceof Authentication.User) {
            ((Authentication.User) this._authentication).logout();
        }
        this._authentication = Authentication.UNAUTHENTICATED;
    }

    public void mergeQueryString(String query) {
        MultiMap<String> parameters = new MultiMap<>();
        UrlEncoded.decodeTo(query, parameters, StringUtil.__UTF8);
        boolean merge_old_query = false;
        if (!this._paramsExtracted) {
            extractParameters();
        }
        if (this._parameters != null && this._parameters.size() > 0) {
            for (Map.Entry<String, Object> entry : this._parameters.entrySet()) {
                String name = entry.getKey();
                if (parameters.containsKey(name)) {
                    merge_old_query = true;
                }
                Object values = entry.getValue();
                for (int i = 0; i < LazyList.size(values); i++) {
                    parameters.add(name, LazyList.get(values, i));
                }
            }
        }
        if (this._queryString != null && this._queryString.length() > 0) {
            if (merge_old_query) {
                StringBuilder overridden_query_string = new StringBuilder();
                MultiMap<String> overridden_old_query = new MultiMap<>();
                UrlEncoded.decodeTo(this._queryString, overridden_old_query, getQueryEncoding());
                MultiMap<String> overridden_new_query = new MultiMap<>();
                UrlEncoded.decodeTo(query, overridden_new_query, StringUtil.__UTF8);
                for (Map.Entry<String, Object> entry2 : overridden_old_query.entrySet()) {
                    String name2 = entry2.getKey();
                    if (!overridden_new_query.containsKey(name2)) {
                        Object values2 = entry2.getValue();
                        for (int i2 = 0; i2 < LazyList.size(values2); i2++) {
                            overridden_query_string.append("&");
                            overridden_query_string.append(name2);
                            overridden_query_string.append("=");
                            overridden_query_string.append(LazyList.get(values2, i2));
                        }
                    }
                }
                query = query + ((Object) overridden_query_string);
            } else {
                query = query + "&" + this._queryString;
            }
        }
        setParameters(parameters);
        setQueryString(query);
    }
}
