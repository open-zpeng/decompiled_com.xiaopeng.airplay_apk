package org.eclipse.jetty.server.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class ContextHandler extends ScopedHandler implements Attributes, Server.Graceful {
    public static final String MANAGED_ATTRIBUTES = "org.eclipse.jetty.server.context.ManagedAttributes";
    private static final int __AVAILABLE = 1;
    private static final int __SHUTDOWN = 2;
    private static final int __STOPPED = 0;
    private static final int __UNAVAILABLE = 3;
    private final CopyOnWriteArrayList<AliasCheck> _aliasChecks;
    private boolean _aliasesAllowed;
    private boolean _allowNullPathInfo;
    private final AttributesMap _attributes;
    private volatile int _availability;
    private boolean _available;
    private Resource _baseResource;
    private ClassLoader _classLoader;
    private boolean _compactPath;
    private Set<String> _connectors;
    private Object _contextAttributeListeners;
    private final AttributesMap _contextAttributes;
    private Object _contextListeners;
    private String _contextPath;
    private String _displayName;
    private Object _durableListeners;
    private ErrorHandler _errorHandler;
    private EventListener[] _eventListeners;
    private final Map<String, String> _initParams;
    private Map<String, String> _localeEncodingMap;
    private Logger _logger;
    private Map<String, Object> _managedAttributes;
    private int _maxFormContentSize;
    private int _maxFormKeys;
    private MimeTypes _mimeTypes;
    private String[] _protectedTargets;
    private Object _requestAttributeListeners;
    private Object _requestListeners;
    protected Context _scontext;
    private boolean _shutdown;
    private String[] _vhosts;
    private String[] _welcomeFiles;
    private static final Logger LOG = Log.getLogger(ContextHandler.class);
    private static final ThreadLocal<Context> __context = new ThreadLocal<>();

    /* loaded from: classes.dex */
    public interface AliasCheck {
        boolean check(String str, Resource resource);
    }

    public static Context getCurrentContext() {
        return __context.get();
    }

    public ContextHandler() {
        this._contextPath = "/";
        this._maxFormKeys = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormKeys", -1).intValue();
        this._maxFormContentSize = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormContentSize", -1).intValue();
        this._compactPath = false;
        this._aliasesAllowed = false;
        this._aliasChecks = new CopyOnWriteArrayList<>();
        this._shutdown = false;
        this._available = true;
        this._scontext = new Context();
        this._attributes = new AttributesMap();
        this._contextAttributes = new AttributesMap();
        this._initParams = new HashMap();
        addAliasCheck(new ApproveNonExistentDirectoryAliases());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ContextHandler(Context context) {
        this._contextPath = "/";
        this._maxFormKeys = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormKeys", -1).intValue();
        this._maxFormContentSize = Integer.getInteger("org.eclipse.jetty.server.Request.maxFormContentSize", -1).intValue();
        this._compactPath = false;
        this._aliasesAllowed = false;
        this._aliasChecks = new CopyOnWriteArrayList<>();
        this._shutdown = false;
        this._available = true;
        this._scontext = context;
        this._attributes = new AttributesMap();
        this._contextAttributes = new AttributesMap();
        this._initParams = new HashMap();
        addAliasCheck(new ApproveNonExistentDirectoryAliases());
    }

    public ContextHandler(String contextPath) {
        this();
        setContextPath(contextPath);
    }

    public ContextHandler(HandlerContainer parent, String contextPath) {
        this();
        setContextPath(contextPath);
        if (parent instanceof HandlerWrapper) {
            ((HandlerWrapper) parent).setHandler(this);
        } else if (parent instanceof HandlerCollection) {
            ((HandlerCollection) parent).addHandler(this);
        }
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        dump(out, indent, Collections.singletonList(new CLDump(getClassLoader())), TypeUtil.asList(getHandlers()), getBeans(), this._initParams.entrySet(), this._attributes.getAttributeEntrySet(), this._contextAttributes.getAttributeEntrySet());
    }

    public Context getServletContext() {
        return this._scontext;
    }

    public boolean getAllowNullPathInfo() {
        return this._allowNullPathInfo;
    }

    public void setAllowNullPathInfo(boolean allowNullPathInfo) {
        this._allowNullPathInfo = allowNullPathInfo;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        if (this._errorHandler != null) {
            Server old_server = getServer();
            if (old_server != null && old_server != server) {
                old_server.getContainer().update((Object) this, (Object) this._errorHandler, (Object) null, "error", true);
            }
            super.setServer(server);
            if (server != null && server != old_server) {
                server.getContainer().update((Object) this, (Object) null, (Object) this._errorHandler, "error", true);
            }
            this._errorHandler.setServer(server);
            return;
        }
        super.setServer(server);
    }

    public void setVirtualHosts(String[] vhosts) {
        if (vhosts == null) {
            this._vhosts = vhosts;
            return;
        }
        this._vhosts = new String[vhosts.length];
        for (int i = 0; i < vhosts.length; i++) {
            this._vhosts[i] = normalizeHostname(vhosts[i]);
        }
    }

    public void addVirtualHosts(String[] virtualHosts) {
        List<String> currentVirtualHosts;
        if (virtualHosts == null) {
            return;
        }
        if (this._vhosts != null) {
            currentVirtualHosts = new ArrayList<>(Arrays.asList(this._vhosts));
        } else {
            currentVirtualHosts = new ArrayList<>();
        }
        for (String str : virtualHosts) {
            String normVhost = normalizeHostname(str);
            if (!currentVirtualHosts.contains(normVhost)) {
                currentVirtualHosts.add(normVhost);
            }
        }
        this._vhosts = (String[]) currentVirtualHosts.toArray(new String[0]);
    }

    public void removeVirtualHosts(String[] virtualHosts) {
        if (virtualHosts == null || this._vhosts == null || this._vhosts.length == 0) {
            return;
        }
        List<String> existingVirtualHosts = new ArrayList<>(Arrays.asList(this._vhosts));
        for (String str : virtualHosts) {
            String toRemoveVirtualHost = normalizeHostname(str);
            if (existingVirtualHosts.contains(toRemoveVirtualHost)) {
                existingVirtualHosts.remove(toRemoveVirtualHost);
            }
        }
        if (existingVirtualHosts.isEmpty()) {
            this._vhosts = null;
        } else {
            this._vhosts = (String[]) existingVirtualHosts.toArray(new String[0]);
        }
    }

    public String[] getVirtualHosts() {
        return this._vhosts;
    }

    public String[] getConnectorNames() {
        if (this._connectors == null || this._connectors.size() == 0) {
            return null;
        }
        return (String[]) this._connectors.toArray(new String[this._connectors.size()]);
    }

    public void setConnectorNames(String[] connectors) {
        if (connectors == null || connectors.length == 0) {
            this._connectors = null;
        } else {
            this._connectors = new HashSet(Arrays.asList(connectors));
        }
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Object getAttribute(String name) {
        return this._attributes.getAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Enumeration getAttributeNames() {
        return AttributesMap.getAttributeNamesCopy(this._attributes);
    }

    public Attributes getAttributes() {
        return this._attributes;
    }

    public ClassLoader getClassLoader() {
        return this._classLoader;
    }

    public String getClassPath() {
        if (this._classLoader == null || !(this._classLoader instanceof URLClassLoader)) {
            return null;
        }
        URLClassLoader loader = (URLClassLoader) this._classLoader;
        URL[] urls = loader.getURLs();
        StringBuilder classpath = new StringBuilder();
        for (URL url : urls) {
            try {
                Resource resource = newResource(url);
                File file = resource.getFile();
                if (file != null && file.exists()) {
                    if (classpath.length() > 0) {
                        classpath.append(File.pathSeparatorChar);
                    }
                    classpath.append(file.getAbsolutePath());
                }
            } catch (IOException e) {
                LOG.debug(e);
            }
        }
        int i = classpath.length();
        if (i == 0) {
            return null;
        }
        return classpath.toString();
    }

    public String getContextPath() {
        return this._contextPath;
    }

    public String getInitParameter(String name) {
        return this._initParams.get(name);
    }

    public String setInitParameter(String name, String value) {
        return this._initParams.put(name, value);
    }

    public Enumeration getInitParameterNames() {
        return Collections.enumeration(this._initParams.keySet());
    }

    public Map<String, String> getInitParams() {
        return this._initParams;
    }

    public String getDisplayName() {
        return this._displayName;
    }

    public EventListener[] getEventListeners() {
        return this._eventListeners;
    }

    public void setEventListeners(EventListener[] eventListeners) {
        this._contextListeners = null;
        this._contextAttributeListeners = null;
        this._requestListeners = null;
        this._requestAttributeListeners = null;
        this._eventListeners = eventListeners;
        for (int i = 0; eventListeners != null && i < eventListeners.length; i++) {
            EventListener listener = this._eventListeners[i];
            if (listener instanceof ServletContextListener) {
                this._contextListeners = LazyList.add(this._contextListeners, listener);
            }
            if (listener instanceof ServletContextAttributeListener) {
                this._contextAttributeListeners = LazyList.add(this._contextAttributeListeners, listener);
            }
            if (listener instanceof ServletRequestListener) {
                this._requestListeners = LazyList.add(this._requestListeners, listener);
            }
            if (listener instanceof ServletRequestAttributeListener) {
                this._requestAttributeListeners = LazyList.add(this._requestAttributeListeners, listener);
            }
        }
    }

    public void addEventListener(EventListener listener) {
        if (!isStarted() && !isStarting()) {
            this._durableListeners = LazyList.add(this._durableListeners, listener);
        }
        setEventListeners((EventListener[]) LazyList.addToArray(getEventListeners(), listener, EventListener.class));
    }

    public void restrictEventListener(EventListener listener) {
    }

    public boolean isShutdown() {
        boolean z;
        synchronized (this) {
            z = !this._shutdown;
        }
        return z;
    }

    @Override // org.eclipse.jetty.server.Server.Graceful
    public void setShutdown(boolean shutdown) {
        synchronized (this) {
            this._shutdown = shutdown;
            this._availability = isRunning() ? this._shutdown ? 2 : this._available ? 1 : 3 : 0;
        }
    }

    public boolean isAvailable() {
        boolean z;
        synchronized (this) {
            z = this._available;
        }
        return z;
    }

    public void setAvailable(boolean available) {
        synchronized (this) {
            this._available = available;
            this._availability = isRunning() ? this._shutdown ? 2 : this._available ? 1 : 3 : 0;
        }
    }

    public Logger getLogger() {
        return this._logger;
    }

    public void setLogger(Logger logger) {
        this._logger = logger;
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    protected void doStart() throws Exception {
        this._availability = 0;
        if (this._contextPath == null) {
            throw new IllegalStateException("Null contextPath");
        }
        this._logger = Log.getLogger(getDisplayName() == null ? getContextPath() : getDisplayName());
        ClassLoader old_classloader = null;
        Thread current_thread = null;
        Context old_context = null;
        try {
            if (this._classLoader != null) {
                current_thread = Thread.currentThread();
                old_classloader = current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(this._classLoader);
            }
            if (this._mimeTypes == null) {
                this._mimeTypes = new MimeTypes();
            }
            old_context = __context.get();
            __context.set(this._scontext);
            startContext();
            synchronized (this) {
                this._availability = this._shutdown ? 2 : this._available ? 1 : 3;
            }
        } finally {
            __context.set(old_context);
            if (this._classLoader != null) {
                current_thread.setContextClassLoader(old_classloader);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void startContext() throws Exception {
        String managedAttributes = this._initParams.get(MANAGED_ATTRIBUTES);
        if (managedAttributes != null) {
            this._managedAttributes = new HashMap();
            String[] attributes = managedAttributes.split(",");
            for (String attribute : attributes) {
                this._managedAttributes.put(attribute, null);
            }
            Enumeration e = this._scontext.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                Object value = this._scontext.getAttribute(name);
                checkManagedAttribute(name, value);
            }
        }
        super.doStart();
        if (this._errorHandler != null) {
            this._errorHandler.start();
        }
        if (this._contextListeners != null) {
            ServletContextEvent event = new ServletContextEvent(this._scontext);
            for (int i = 0; i < LazyList.size(this._contextListeners); i++) {
                callContextInitialized((ServletContextListener) LazyList.get(this._contextListeners, i), event);
            }
        }
    }

    public void callContextInitialized(ServletContextListener l, ServletContextEvent e) {
        l.contextInitialized(e);
    }

    public void callContextDestroyed(ServletContextListener l, ServletContextEvent e) {
        l.contextDestroyed(e);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._availability = 0;
        ClassLoader old_classloader = null;
        Thread current_thread = null;
        Context old_context = __context.get();
        __context.set(this._scontext);
        try {
            if (this._classLoader != null) {
                current_thread = Thread.currentThread();
                old_classloader = current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(this._classLoader);
            }
            super.doStop();
            if (this._contextListeners != null) {
                ServletContextEvent event = new ServletContextEvent(this._scontext);
                int i = LazyList.size(this._contextListeners);
                while (true) {
                    int i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    ((ServletContextListener) LazyList.get(this._contextListeners, i2)).contextDestroyed(event);
                    i = i2;
                }
            }
            setEventListeners((EventListener[]) LazyList.toArray(this._durableListeners, EventListener.class));
            this._durableListeners = null;
            if (this._errorHandler != null) {
                this._errorHandler.stop();
            }
            Enumeration e = this._scontext.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                checkManagedAttribute(name, null);
            }
            LOG.info("stopped {}", this);
            __context.set(old_context);
            if (this._classLoader != null) {
                current_thread.setContextClassLoader(old_classloader);
            }
            this._contextAttributes.clearAttributes();
        } catch (Throwable th) {
            LOG.info("stopped {}", this);
            __context.set(old_context);
            if (this._classLoader != null) {
                current_thread.setContextClassLoader(old_classloader);
            }
            throw th;
        }
    }

    public boolean checkContext(String target, Request baseRequest, HttpServletResponse response) throws IOException, ServletException {
        String connector;
        DispatcherType dispatch = baseRequest.getDispatcherType();
        int i = this._availability;
        if (i != 0) {
            switch (i) {
                case 2:
                    break;
                case 3:
                    baseRequest.setHandled(true);
                    response.sendError(503);
                    return false;
                default:
                    if (DispatcherType.REQUEST.equals(dispatch) && baseRequest.isHandled()) {
                        return false;
                    }
                    if (this._vhosts != null && this._vhosts.length > 0) {
                        String vhost = normalizeHostname(baseRequest.getServerName());
                        boolean match = false;
                        int i2 = 0;
                        while (true) {
                            int i3 = i2;
                            if (!match && i3 < this._vhosts.length) {
                                String contextVhost = this._vhosts[i3];
                                if (contextVhost != null) {
                                    if (contextVhost.startsWith("*.")) {
                                        boolean match2 = contextVhost.regionMatches(true, 2, vhost, vhost.indexOf(".") + 1, contextVhost.length() - 2);
                                        match = match2;
                                    } else {
                                        boolean match3 = contextVhost.equalsIgnoreCase(vhost);
                                        match = match3;
                                    }
                                }
                                i2 = i3 + 1;
                            }
                        }
                        if (!match) {
                            return false;
                        }
                    }
                    if (this._connectors == null || this._connectors.size() <= 0 || ((connector = AbstractHttpConnection.getCurrentConnection().getConnector().getName()) != null && this._connectors.contains(connector))) {
                        if (this._contextPath.length() > 1) {
                            if (target.startsWith(this._contextPath)) {
                                if (target.length() <= this._contextPath.length() || target.charAt(this._contextPath.length()) == '/') {
                                    if (!this._allowNullPathInfo && this._contextPath.length() == target.length()) {
                                        baseRequest.setHandled(true);
                                        if (baseRequest.getQueryString() == null) {
                                            response.sendRedirect(URIUtil.addPaths(baseRequest.getRequestURI(), "/"));
                                        } else {
                                            response.sendRedirect(URIUtil.addPaths(baseRequest.getRequestURI(), "/") + "?" + baseRequest.getQueryString());
                                        }
                                        return false;
                                    }
                                    return true;
                                }
                                return false;
                            }
                            return false;
                        }
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String target2;
        String target3;
        String pathInfo;
        if (LOG.isDebugEnabled()) {
            LOG.debug("scope {}|{}|{} @ {}", baseRequest.getContextPath(), baseRequest.getServletPath(), baseRequest.getPathInfo(), this);
        }
        String old_context_path = null;
        String old_servlet_path = null;
        String old_path_info = null;
        ClassLoader old_classloader = null;
        Thread current_thread = null;
        String pathInfo2 = target;
        DispatcherType dispatch = baseRequest.getDispatcherType();
        Context old_context = baseRequest.getContext();
        if (old_context != this._scontext) {
            if (DispatcherType.REQUEST.equals(dispatch) || DispatcherType.ASYNC.equals(dispatch) || (DispatcherType.ERROR.equals(dispatch) && baseRequest.getAsyncContinuation().isExpired())) {
                if (this._compactPath) {
                    target3 = URIUtil.compactPath(target);
                } else {
                    target3 = target;
                }
                if (!checkContext(target3, baseRequest, response)) {
                    return;
                }
                if (target3.length() > this._contextPath.length()) {
                    if (this._contextPath.length() > 1) {
                        target3 = target3.substring(this._contextPath.length());
                    }
                    pathInfo = target3;
                } else if (this._contextPath.length() == 1) {
                    target3 = "/";
                    pathInfo = "/";
                } else {
                    target3 = "/";
                    pathInfo = null;
                }
                pathInfo2 = pathInfo;
            } else {
                target3 = target;
            }
            if (this._classLoader != null) {
                current_thread = Thread.currentThread();
                old_classloader = current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(this._classLoader);
            }
            target2 = target3;
        } else {
            target2 = target;
        }
        try {
            old_context_path = baseRequest.getContextPath();
            old_servlet_path = baseRequest.getServletPath();
            old_path_info = baseRequest.getPathInfo();
            baseRequest.setContext(this._scontext);
            __context.set(this._scontext);
            if (!DispatcherType.INCLUDE.equals(dispatch) && target2.startsWith("/")) {
                if (this._contextPath.length() == 1) {
                    baseRequest.setContextPath("");
                } else {
                    baseRequest.setContextPath(this._contextPath);
                }
                baseRequest.setServletPath(null);
                baseRequest.setPathInfo(pathInfo2);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("context={}|{}|{} @ {}", baseRequest.getContextPath(), baseRequest.getServletPath(), baseRequest.getPathInfo(), this);
            }
            if (never()) {
                nextScope(target2, baseRequest, request, response);
            } else if (this._nextScope != null) {
                this._nextScope.doScope(target2, baseRequest, request, response);
            } else if (this._outerScope != null) {
                this._outerScope.doHandle(target2, baseRequest, request, response);
            } else {
                doHandle(target2, baseRequest, request, response);
            }
        } finally {
            if (old_context != this._scontext) {
                if (this._classLoader != null) {
                    current_thread.setContextClassLoader(old_classloader);
                }
                baseRequest.setContext(old_context);
                __context.set(old_context);
                baseRequest.setContextPath(old_context_path);
                baseRequest.setServletPath(old_servlet_path);
                baseRequest.setPathInfo(old_path_info);
            }
        }
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DispatcherType dispatch = baseRequest.getDispatcherType();
        boolean new_context = baseRequest.takeNewContext();
        try {
            if (new_context) {
                try {
                    if (this._requestAttributeListeners != null) {
                        int s = LazyList.size(this._requestAttributeListeners);
                        for (int i = 0; i < s; i++) {
                            baseRequest.addEventListener((EventListener) LazyList.get(this._requestAttributeListeners, i));
                        }
                    }
                    if (this._requestListeners != null) {
                        int s2 = LazyList.size(this._requestListeners);
                        ServletRequestEvent sre = new ServletRequestEvent(this._scontext, request);
                        for (int i2 = 0; i2 < s2; i2++) {
                            ((ServletRequestListener) LazyList.get(this._requestListeners, i2)).requestInitialized(sre);
                        }
                    }
                } catch (HttpException e) {
                    LOG.debug(e);
                    baseRequest.setHandled(true);
                    response.sendError(e.getStatus(), e.getReason());
                    if (!new_context) {
                        return;
                    }
                    if (this._requestListeners != null) {
                        ServletRequestEvent sre2 = new ServletRequestEvent(this._scontext, request);
                        int i3 = LazyList.size(this._requestListeners);
                        while (true) {
                            int i4 = i3 - 1;
                            if (i3 <= 0) {
                                break;
                            }
                            ((ServletRequestListener) LazyList.get(this._requestListeners, i4)).requestDestroyed(sre2);
                            i3 = i4;
                        }
                    }
                    if (this._requestAttributeListeners == null) {
                        return;
                    }
                    int i5 = LazyList.size(this._requestAttributeListeners);
                    while (true) {
                        int i6 = i5 - 1;
                        if (i5 <= 0) {
                            return;
                        }
                        baseRequest.removeEventListener((EventListener) LazyList.get(this._requestAttributeListeners, i6));
                        i5 = i6;
                    }
                }
            }
            if (DispatcherType.REQUEST.equals(dispatch) && isProtectedTarget(target)) {
                throw new HttpException(404);
            }
            if (never()) {
                nextHandle(target, baseRequest, request, response);
            } else if (this._nextScope != null && this._nextScope == this._handler) {
                this._nextScope.doHandle(target, baseRequest, request, response);
            } else if (this._handler != null) {
                this._handler.handle(target, baseRequest, request, response);
            }
            if (!new_context) {
                return;
            }
            if (this._requestListeners != null) {
                ServletRequestEvent sre3 = new ServletRequestEvent(this._scontext, request);
                int i7 = LazyList.size(this._requestListeners);
                while (true) {
                    int i8 = i7 - 1;
                    if (i7 <= 0) {
                        break;
                    }
                    ((ServletRequestListener) LazyList.get(this._requestListeners, i8)).requestDestroyed(sre3);
                    i7 = i8;
                }
            }
            if (this._requestAttributeListeners == null) {
                return;
            }
            int i9 = LazyList.size(this._requestAttributeListeners);
            while (true) {
                int i10 = i9 - 1;
                if (i9 <= 0) {
                    return;
                }
                baseRequest.removeEventListener((EventListener) LazyList.get(this._requestAttributeListeners, i10));
                i9 = i10;
            }
        } catch (Throwable th) {
            if (new_context) {
                if (this._requestListeners != null) {
                    ServletRequestEvent sre4 = new ServletRequestEvent(this._scontext, request);
                    int i11 = LazyList.size(this._requestListeners);
                    while (true) {
                        int i12 = i11 - 1;
                        if (i11 <= 0) {
                            break;
                        }
                        ((ServletRequestListener) LazyList.get(this._requestListeners, i12)).requestDestroyed(sre4);
                        i11 = i12;
                    }
                }
                if (this._requestAttributeListeners != null) {
                    int i13 = LazyList.size(this._requestAttributeListeners);
                    while (true) {
                        int i14 = i13 - 1;
                        if (i13 <= 0) {
                            break;
                        }
                        baseRequest.removeEventListener((EventListener) LazyList.get(this._requestAttributeListeners, i14));
                        i13 = i14;
                    }
                }
            }
            throw th;
        }
    }

    public void handle(Runnable runnable) {
        ClassLoader old_classloader = null;
        Thread current_thread = null;
        Context old_context = null;
        try {
            old_context = __context.get();
            __context.set(this._scontext);
            if (this._classLoader != null) {
                current_thread = Thread.currentThread();
                old_classloader = current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(this._classLoader);
            }
            runnable.run();
        } finally {
            __context.set(old_context);
            if (old_classloader != null) {
                current_thread.setContextClassLoader(old_classloader);
            }
        }
    }

    public boolean isProtectedTarget(String target) {
        if (target == null || this._protectedTargets == null) {
            return false;
        }
        while (target.startsWith("//")) {
            target = URIUtil.compactPath(target);
        }
        boolean isProtected = false;
        for (int i = 0; !isProtected && i < this._protectedTargets.length; i++) {
            isProtected = StringUtil.startsWithIgnoreCase(target, this._protectedTargets[i]);
        }
        return isProtected;
    }

    public void setProtectedTargets(String[] targets) {
        if (targets == null) {
            this._protectedTargets = null;
            return;
        }
        this._protectedTargets = new String[targets.length];
        System.arraycopy(targets, 0, this._protectedTargets, 0, targets.length);
    }

    public String[] getProtectedTargets() {
        if (this._protectedTargets == null) {
            return null;
        }
        String[] tmp = new String[this._protectedTargets.length];
        System.arraycopy(this._protectedTargets, 0, tmp, 0, this._protectedTargets.length);
        return tmp;
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void removeAttribute(String name) {
        checkManagedAttribute(name, null);
        this._attributes.removeAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void setAttribute(String name, Object value) {
        checkManagedAttribute(name, value);
        this._attributes.setAttribute(name, value);
    }

    public void setAttributes(Attributes attributes) {
        this._attributes.clearAttributes();
        this._attributes.addAll(attributes);
        Enumeration e = this._attributes.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            checkManagedAttribute(name, attributes.getAttribute(name));
        }
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void clearAttributes() {
        Enumeration e = this._attributes.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            checkManagedAttribute(name, null);
        }
        this._attributes.clearAttributes();
    }

    public void checkManagedAttribute(String name, Object value) {
        if (this._managedAttributes != null && this._managedAttributes.containsKey(name)) {
            setManagedAttribute(name, value);
        }
    }

    public void setManagedAttribute(String name, Object value) {
        Object old = this._managedAttributes.put(name, value);
        getServer().getContainer().update((Object) this, old, value, name, true);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this._classLoader = classLoader;
    }

    public void setContextPath(String contextPath) {
        if (contextPath != null && contextPath.length() > 1 && contextPath.endsWith("/")) {
            throw new IllegalArgumentException("ends with /");
        }
        this._contextPath = contextPath;
        if (getServer() != null) {
            if (getServer().isStarting() || getServer().isStarted()) {
                Handler[] contextCollections = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
                for (int h = 0; contextCollections != null && h < contextCollections.length; h++) {
                    ((ContextHandlerCollection) contextCollections[h]).mapContexts();
                }
            }
        }
    }

    public void setDisplayName(String servletContextName) {
        this._displayName = servletContextName;
    }

    public Resource getBaseResource() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource;
    }

    public String getResourceBase() {
        if (this._baseResource == null) {
            return null;
        }
        return this._baseResource.toString();
    }

    public void setBaseResource(Resource base) {
        this._baseResource = base;
    }

    public void setResourceBase(String resourceBase) {
        try {
            setBaseResource(newResource(resourceBase));
        } catch (Exception e) {
            LOG.warn(e.toString(), new Object[0]);
            LOG.debug(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }

    public boolean isAliases() {
        return this._aliasesAllowed;
    }

    public void setAliases(boolean aliases) {
        this._aliasesAllowed = aliases;
    }

    public MimeTypes getMimeTypes() {
        if (this._mimeTypes == null) {
            this._mimeTypes = new MimeTypes();
        }
        return this._mimeTypes;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this._mimeTypes = mimeTypes;
    }

    public void setWelcomeFiles(String[] files) {
        this._welcomeFiles = files;
    }

    public String[] getWelcomeFiles() {
        return this._welcomeFiles;
    }

    public ErrorHandler getErrorHandler() {
        return this._errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler != null) {
            errorHandler.setServer(getServer());
        }
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object) this._errorHandler, (Object) errorHandler, "errorHandler", true);
        }
        this._errorHandler = errorHandler;
    }

    public int getMaxFormContentSize() {
        return this._maxFormContentSize;
    }

    public void setMaxFormContentSize(int maxSize) {
        this._maxFormContentSize = maxSize;
    }

    public int getMaxFormKeys() {
        return this._maxFormKeys;
    }

    public void setMaxFormKeys(int max) {
        this._maxFormKeys = max;
    }

    public boolean isCompactPath() {
        return this._compactPath;
    }

    public void setCompactPath(boolean compactPath) {
        this._compactPath = compactPath;
    }

    public String toString() {
        String p;
        String[] vhosts = getVirtualHosts();
        StringBuilder b = new StringBuilder();
        Package pkg = getClass().getPackage();
        if (pkg != null && (p = pkg.getName()) != null && p.length() > 0) {
            String[] ss = p.split("\\.");
            for (String s : ss) {
                b.append(s.charAt(0));
                b.append('.');
            }
        }
        b.append(getClass().getSimpleName());
        b.append('{');
        b.append(getContextPath());
        b.append(',');
        b.append(getBaseResource());
        if (vhosts != null && vhosts.length > 0) {
            b.append(',');
            b.append(vhosts[0]);
        }
        b.append('}');
        return b.toString();
    }

    public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }
        if (this._classLoader == null) {
            return Loader.loadClass(getClass(), className);
        }
        return this._classLoader.loadClass(className);
    }

    public void addLocaleEncoding(String locale, String encoding) {
        if (this._localeEncodingMap == null) {
            this._localeEncodingMap = new HashMap();
        }
        this._localeEncodingMap.put(locale, encoding);
    }

    public String getLocaleEncoding(String locale) {
        if (this._localeEncodingMap == null) {
            return null;
        }
        String encoding = this._localeEncodingMap.get(locale);
        return encoding;
    }

    public String getLocaleEncoding(Locale locale) {
        if (this._localeEncodingMap == null) {
            return null;
        }
        String encoding = this._localeEncodingMap.get(locale.toString());
        if (encoding == null) {
            return this._localeEncodingMap.get(locale.getLanguage());
        }
        return encoding;
    }

    public Resource getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException(path);
        }
        if (this._baseResource == null) {
            return null;
        }
        try {
            String path2 = URIUtil.canonicalPath(path);
            Resource resource = this._baseResource.addPath(path2);
            if (checkAlias(path2, resource)) {
                return resource;
            }
            return null;
        } catch (Exception e) {
            LOG.ignore(e);
            return null;
        }
    }

    public boolean checkAlias(String path, Resource resource) {
        if (this._aliasesAllowed || resource.getAlias() == null) {
            return true;
        }
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Aliased resource: " + resource + "~=" + resource.getAlias(), new Object[0]);
        }
        Iterator<AliasCheck> i = this._aliasChecks.iterator();
        while (i.hasNext()) {
            AliasCheck check = i.next();
            if (check.check(path, resource)) {
                if (LOG.isDebugEnabled()) {
                    Logger logger2 = LOG;
                    logger2.debug("Aliased resource: " + resource + " approved by " + check, new Object[0]);
                }
                return true;
            }
        }
        return false;
    }

    public Resource newResource(URL url) throws IOException {
        return Resource.newResource(url);
    }

    public Resource newResource(String urlOrPath) throws IOException {
        return Resource.newResource(urlOrPath);
    }

    public Set<String> getResourcePaths(String path) {
        try {
            String path2 = URIUtil.canonicalPath(path);
            Resource resource = getResource(path2);
            if (resource != null && resource.exists()) {
                if (!path2.endsWith("/")) {
                    path2 = path2 + "/";
                }
                String[] l = resource.list();
                if (l != null) {
                    HashSet<String> set = new HashSet<>();
                    for (int i = 0; i < l.length; i++) {
                        set.add(path2 + l[i]);
                    }
                    return set;
                }
            }
        } catch (Exception e) {
            LOG.ignore(e);
        }
        return Collections.emptySet();
    }

    private String normalizeHostname(String host) {
        if (host == null) {
            return null;
        }
        if (host.endsWith(".")) {
            return host.substring(0, host.length() - 1);
        }
        return host;
    }

    public void addAliasCheck(AliasCheck check) {
        this._aliasChecks.add(check);
    }

    public List<AliasCheck> getAliasChecks() {
        return this._aliasChecks;
    }

    /* loaded from: classes.dex */
    public class Context implements ServletContext {
        private static final String __unimplmented = "Unimplemented - use org.eclipse.jetty.servlet.ServletContextHandler";
        protected int _majorVersion = 3;
        protected int _minorVersion = 0;
        protected boolean _enabled = true;

        /* JADX INFO: Access modifiers changed from: protected */
        public Context() {
        }

        public ContextHandler getContextHandler() {
            return ContextHandler.this;
        }

        /* JADX WARN: Code restructure failed: missing block: B:32:0x009c, code lost:
            if (r0 > r6) goto L40;
         */
        @Override // javax.servlet.ServletContext
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public javax.servlet.ServletContext getContext(java.lang.String r22) {
            /*
                Method dump skipped, instructions count: 357
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.handler.ContextHandler.Context.getContext(java.lang.String):javax.servlet.ServletContext");
        }

        @Override // javax.servlet.ServletContext
        public int getMajorVersion() {
            return 3;
        }

        @Override // javax.servlet.ServletContext
        public String getMimeType(String file) {
            Buffer mime;
            if (ContextHandler.this._mimeTypes == null || (mime = ContextHandler.this._mimeTypes.getMimeByExtension(file)) == null) {
                return null;
            }
            return mime.toString();
        }

        @Override // javax.servlet.ServletContext
        public int getMinorVersion() {
            return 0;
        }

        @Override // javax.servlet.ServletContext
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override // javax.servlet.ServletContext
        public RequestDispatcher getRequestDispatcher(String uriInContext) {
            if (uriInContext == null || !uriInContext.startsWith("/")) {
                return null;
            }
            String query = null;
            try {
                int q = uriInContext.indexOf(63);
                if (q > 0) {
                    query = uriInContext.substring(q + 1);
                    uriInContext = uriInContext.substring(0, q);
                }
                String pathInContext = URIUtil.canonicalPath(URIUtil.decodePath(uriInContext));
                if (pathInContext != null) {
                    String uri = URIUtil.addPaths(getContextPath(), uriInContext);
                    ContextHandler context = ContextHandler.this;
                    return new Dispatcher(context, uri, pathInContext, query);
                }
            } catch (Exception e) {
                ContextHandler.LOG.ignore(e);
            }
            return null;
        }

        @Override // javax.servlet.ServletContext
        public String getRealPath(String path) {
            File file;
            if (path == null) {
                return null;
            }
            if (path.length() == 0) {
                path = "/";
            } else if (path.charAt(0) != '/') {
                path = "/" + path;
            }
            try {
                Resource resource = ContextHandler.this.getResource(path);
                if (resource != null && (file = resource.getFile()) != null) {
                    return file.getCanonicalPath();
                }
            } catch (Exception e) {
                ContextHandler.LOG.ignore(e);
            }
            return null;
        }

        @Override // javax.servlet.ServletContext
        public URL getResource(String path) throws MalformedURLException {
            Resource resource = ContextHandler.this.getResource(path);
            if (resource != null && resource.exists()) {
                return resource.getURL();
            }
            return null;
        }

        @Override // javax.servlet.ServletContext
        public InputStream getResourceAsStream(String path) {
            try {
                URL url = getResource(path);
                if (url == null) {
                    return null;
                }
                Resource r = Resource.newResource(url);
                return r.getInputStream();
            } catch (Exception e) {
                ContextHandler.LOG.ignore(e);
                return null;
            }
        }

        @Override // javax.servlet.ServletContext
        public Set getResourcePaths(String path) {
            return ContextHandler.this.getResourcePaths(path);
        }

        @Override // javax.servlet.ServletContext
        public String getServerInfo() {
            return "jetty/" + Server.getVersion();
        }

        @Override // javax.servlet.ServletContext
        @Deprecated
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @Override // javax.servlet.ServletContext
        @Deprecated
        public Enumeration getServletNames() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        @Override // javax.servlet.ServletContext
        @Deprecated
        public Enumeration getServlets() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        @Override // javax.servlet.ServletContext
        public void log(Exception exception, String msg) {
            ContextHandler.this._logger.warn(msg, exception);
        }

        @Override // javax.servlet.ServletContext
        public void log(String msg) {
            ContextHandler.this._logger.info(msg, new Object[0]);
        }

        @Override // javax.servlet.ServletContext
        public void log(String message, Throwable throwable) {
            ContextHandler.this._logger.warn(message, throwable);
        }

        @Override // javax.servlet.ServletContext
        public String getInitParameter(String name) {
            return ContextHandler.this.getInitParameter(name);
        }

        @Override // javax.servlet.ServletContext
        public Enumeration getInitParameterNames() {
            return ContextHandler.this.getInitParameterNames();
        }

        @Override // javax.servlet.ServletContext
        public synchronized Object getAttribute(String name) {
            Object o;
            o = ContextHandler.this.getAttribute(name);
            if (o == null && ContextHandler.this._contextAttributes != null) {
                o = ContextHandler.this._contextAttributes.getAttribute(name);
            }
            return o;
        }

        @Override // javax.servlet.ServletContext
        public synchronized Enumeration getAttributeNames() {
            HashSet<String> set;
            set = new HashSet<>();
            if (ContextHandler.this._contextAttributes != null) {
                Enumeration<String> e = ContextHandler.this._contextAttributes.getAttributeNames();
                while (e.hasMoreElements()) {
                    set.add(e.nextElement());
                }
            }
            Enumeration<String> e2 = ContextHandler.this._attributes.getAttributeNames();
            while (e2.hasMoreElements()) {
                set.add(e2.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override // javax.servlet.ServletContext
        public synchronized void setAttribute(String name, Object value) {
            ContextHandler.this.checkManagedAttribute(name, value);
            Object old_value = ContextHandler.this._contextAttributes.getAttribute(name);
            if (value == null) {
                ContextHandler.this._contextAttributes.removeAttribute(name);
            } else {
                ContextHandler.this._contextAttributes.setAttribute(name, value);
            }
            if (ContextHandler.this._contextAttributeListeners != null) {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(ContextHandler.this._scontext, name, old_value == null ? value : old_value);
                for (int i = 0; i < LazyList.size(ContextHandler.this._contextAttributeListeners); i++) {
                    ServletContextAttributeListener l = (ServletContextAttributeListener) LazyList.get(ContextHandler.this._contextAttributeListeners, i);
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

        @Override // javax.servlet.ServletContext
        public synchronized void removeAttribute(String name) {
            ContextHandler.this.checkManagedAttribute(name, null);
            if (ContextHandler.this._contextAttributes == null) {
                ContextHandler.this._attributes.removeAttribute(name);
                return;
            }
            Object old_value = ContextHandler.this._contextAttributes.getAttribute(name);
            ContextHandler.this._contextAttributes.removeAttribute(name);
            if (old_value != null && ContextHandler.this._contextAttributeListeners != null) {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(ContextHandler.this._scontext, name, old_value);
                for (int i = 0; i < LazyList.size(ContextHandler.this._contextAttributeListeners); i++) {
                    ((ServletContextAttributeListener) LazyList.get(ContextHandler.this._contextAttributeListeners, i)).attributeRemoved(event);
                }
            }
        }

        @Override // javax.servlet.ServletContext
        public String getServletContextName() {
            String name = ContextHandler.this.getDisplayName();
            if (name == null) {
                return ContextHandler.this.getContextPath();
            }
            return name;
        }

        @Override // javax.servlet.ServletContext
        public String getContextPath() {
            if (ContextHandler.this._contextPath == null || !ContextHandler.this._contextPath.equals("/")) {
                return ContextHandler.this._contextPath;
            }
            return "";
        }

        public String toString() {
            return "ServletContext@" + ContextHandler.this.toString();
        }

        @Override // javax.servlet.ServletContext
        public boolean setInitParameter(String name, String value) {
            if (ContextHandler.this.getInitParameter(name) != null) {
                return false;
            }
            ContextHandler.this.getInitParams().put(name, value);
            return true;
        }

        @Override // javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, String className) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public FilterRegistration getFilterRegistration(String filterName) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public ServletRegistration getServletRegistration(String servletName) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public SessionCookieConfig getSessionCookieConfig() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        @Override // javax.servlet.ServletContext
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // javax.servlet.ServletContext
        public void addListener(String className) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            try {
                addListener((Class<? extends EventListener>) (ContextHandler.this._classLoader == null ? Loader.loadClass(ContextHandler.class, className) : ContextHandler.this._classLoader.loadClass(className)));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override // javax.servlet.ServletContext
        public <T extends EventListener> void addListener(T t) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ContextHandler.this.addEventListener(t);
            ContextHandler.this.restrictEventListener(t);
        }

        @Override // javax.servlet.ServletContext
        public void addListener(Class<? extends EventListener> listenerClass) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            try {
                EventListener e = createListener(listenerClass);
                ContextHandler.this.addEventListener(e);
                ContextHandler.this.restrictEventListener(e);
            } catch (ServletException e2) {
                throw new IllegalArgumentException(e2);
            }
        }

        @Override // javax.servlet.ServletContext
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            } catch (InstantiationException e2) {
                throw new ServletException(e2);
            }
        }

        @Override // javax.servlet.ServletContext
        public ClassLoader getClassLoader() {
            AccessController.checkPermission(new RuntimePermission("getClassLoader"));
            return ContextHandler.this._classLoader;
        }

        @Override // javax.servlet.ServletContext
        public int getEffectiveMajorVersion() {
            return this._majorVersion;
        }

        @Override // javax.servlet.ServletContext
        public int getEffectiveMinorVersion() {
            return this._minorVersion;
        }

        public void setEffectiveMajorVersion(int v) {
            this._majorVersion = v;
        }

        public void setEffectiveMinorVersion(int v) {
            this._minorVersion = v;
        }

        @Override // javax.servlet.ServletContext
        public JspConfigDescriptor getJspConfigDescriptor() {
            ContextHandler.LOG.warn(__unimplmented, new Object[0]);
            return null;
        }

        public void setJspConfigDescriptor(JspConfigDescriptor d) {
        }

        @Override // javax.servlet.ServletContext
        public void declareRoles(String... roleNames) {
            if (!ContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
        }

        public void setEnabled(boolean enabled) {
            this._enabled = enabled;
        }

        public boolean isEnabled() {
            return this._enabled;
        }
    }

    /* loaded from: classes.dex */
    private static class CLDump implements Dumpable {
        final ClassLoader _loader;

        CLDump(ClassLoader loader) {
            this._loader = loader;
        }

        @Override // org.eclipse.jetty.util.component.Dumpable
        public String dump() {
            return AggregateLifeCycle.dump(this);
        }

        @Override // org.eclipse.jetty.util.component.Dumpable
        public void dump(Appendable out, String indent) throws IOException {
            Object parent;
            out.append(String.valueOf(this._loader)).append("\n");
            if (this._loader != null && (parent = this._loader.getParent()) != null) {
                if (!(parent instanceof Dumpable)) {
                    parent = new CLDump((ClassLoader) parent);
                }
                if (this._loader instanceof URLClassLoader) {
                    AggregateLifeCycle.dump(out, indent, TypeUtil.asList(((URLClassLoader) this._loader).getURLs()), Collections.singleton(parent));
                } else {
                    AggregateLifeCycle.dump(out, indent, Collections.singleton(parent));
                }
            }
        }
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class ApproveSameSuffixAliases implements AliasCheck {
        public ApproveSameSuffixAliases() {
            ContextHandler.LOG.warn("ApproveSameSuffixAlias is not safe for production", new Object[0]);
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.AliasCheck
        public boolean check(String path, Resource resource) {
            int dot = path.lastIndexOf(46);
            if (dot < 0) {
                return false;
            }
            String suffix = path.substring(dot);
            return resource.toString().endsWith(suffix);
        }
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class ApprovePathPrefixAliases implements AliasCheck {
        public ApprovePathPrefixAliases() {
            ContextHandler.LOG.warn("ApprovePathPrefixAliases is not safe for production", new Object[0]);
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.AliasCheck
        public boolean check(String path, Resource resource) {
            int slash = path.lastIndexOf(47);
            if (slash < 0 || slash == path.length() - 1) {
                return false;
            }
            String suffix = path.substring(slash);
            return resource.toString().endsWith(suffix);
        }
    }

    /* loaded from: classes.dex */
    public static class ApproveNonExistentDirectoryAliases implements AliasCheck {
        @Override // org.eclipse.jetty.server.handler.ContextHandler.AliasCheck
        public boolean check(String path, Resource resource) {
            if (resource.exists()) {
                return false;
            }
            String a = resource.getAlias().toString();
            String r = resource.getURL().toString();
            return a.length() > r.length() ? a.startsWith(r) && a.length() == r.length() + 1 && a.endsWith("/") : r.startsWith(a) && r.length() == a.length() + 1 && r.endsWith("/");
        }
    }
}
