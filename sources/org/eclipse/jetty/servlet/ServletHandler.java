package org.eclipse.jetty.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletSecurityElement;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.HttpWriter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServletRequestHttpWrapper;
import org.eclipse.jetty.server.ServletResponseHttpWrapper;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ScopedHandler;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ServletHandler extends ScopedHandler {
    private static final Logger LOG = Log.getLogger(ServletHandler.class);
    private static final Logger LOG_UNHANDLED = LOG.getLogger("unhandled");
    public static final String __DEFAULT_SERVLET = "default";
    private ServletContextHandler _contextHandler;
    private FilterMapping[] _filterMappings;
    private MultiMap<String> _filterNameMappings;
    private List<FilterMapping> _filterPathMappings;
    private IdentityService _identityService;
    private ContextHandler.Context _servletContext;
    private ServletMapping[] _servletMappings;
    private PathMap _servletPathMap;
    private FilterHolder[] _filters = new FilterHolder[0];
    private int _matchBeforeIndex = -1;
    private int _matchAfterIndex = -1;
    private boolean _filterChainsCached = true;
    private int _maxFilterChainsCacheSize = HttpWriter.MAX_OUTPUT_CHARS;
    private boolean _startWithUnavailable = false;
    private ServletHolder[] _servlets = new ServletHolder[0];
    private final Map<String, FilterHolder> _filterNameMap = new HashMap();
    private final Map<String, ServletHolder> _servletNameMap = new HashMap();
    protected final ConcurrentMap<String, FilterChain>[] _chainCache = new ConcurrentMap[31];
    protected final Queue<String>[] _chainLRU = new Queue[31];

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        Server old = getServer();
        if (old != null && old != server) {
            getServer().getContainer().update((Object) this, (Object[]) this._filters, (Object[]) null, "filter", true);
            getServer().getContainer().update((Object) this, (Object[]) this._filterMappings, (Object[]) null, "filterMapping", true);
            getServer().getContainer().update((Object) this, (Object[]) this._servlets, (Object[]) null, "servlet", true);
            getServer().getContainer().update((Object) this, (Object[]) this._servletMappings, (Object[]) null, "servletMapping", true);
        }
        super.setServer(server);
        if (server != null && old != server) {
            server.getContainer().update((Object) this, (Object[]) null, (Object[]) this._filters, "filter", true);
            server.getContainer().update((Object) this, (Object[]) null, (Object[]) this._filterMappings, "filterMapping", true);
            server.getContainer().update((Object) this, (Object[]) null, (Object[]) this._servlets, "servlet", true);
            server.getContainer().update((Object) this, (Object[]) null, (Object[]) this._servletMappings, "servletMapping", true);
        }
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    protected synchronized void doStart() throws Exception {
        SecurityHandler security_handler;
        this._servletContext = ContextHandler.getCurrentContext();
        this._contextHandler = (ServletContextHandler) (this._servletContext == null ? null : this._servletContext.getContextHandler());
        if (this._contextHandler != null && (security_handler = (SecurityHandler) this._contextHandler.getChildHandlerByClass(SecurityHandler.class)) != null) {
            this._identityService = security_handler.getIdentityService();
        }
        updateNameMappings();
        updateMappings();
        if (this._filterChainsCached) {
            this._chainCache[1] = new ConcurrentHashMap();
            this._chainCache[2] = new ConcurrentHashMap();
            this._chainCache[4] = new ConcurrentHashMap();
            this._chainCache[8] = new ConcurrentHashMap();
            this._chainCache[16] = new ConcurrentHashMap();
            this._chainLRU[1] = new ConcurrentLinkedQueue();
            this._chainLRU[2] = new ConcurrentLinkedQueue();
            this._chainLRU[4] = new ConcurrentLinkedQueue();
            this._chainLRU[8] = new ConcurrentLinkedQueue();
            this._chainLRU[16] = new ConcurrentLinkedQueue();
        }
        super.doStart();
        if (this._contextHandler == null || !(this._contextHandler instanceof ServletContextHandler)) {
            initialize();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x00b0 A[Catch: all -> 0x0131, TRY_LEAVE, TryCatch #1 {, blocks: (B:3:0x0001, B:5:0x0013, B:8:0x001a, B:12:0x002a, B:14:0x0036, B:15:0x0047, B:17:0x004d, B:19:0x0065, B:22:0x006a, B:11:0x0023, B:24:0x0073, B:26:0x0090, B:29:0x0096, B:31:0x009d, B:33:0x00b0, B:36:0x00b7, B:40:0x00c7, B:42:0x00d3, B:43:0x00e4, B:45:0x00ea, B:47:0x0102, B:50:0x0107, B:39:0x00c0, B:52:0x0110), top: B:60:0x0001, inners: #0, #2 }] */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected synchronized void doStop() throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 308
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.servlet.ServletHandler.doStop():void");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public IdentityService getIdentityService() {
        return this._identityService;
    }

    public Object getContextLog() {
        return null;
    }

    public FilterMapping[] getFilterMappings() {
        return this._filterMappings;
    }

    public FilterHolder[] getFilters() {
        return this._filters;
    }

    public PathMap.Entry getHolderEntry(String pathInContext) {
        if (this._servletPathMap == null) {
            return null;
        }
        return this._servletPathMap.getMatch(pathInContext);
    }

    public ServletContext getServletContext() {
        return this._servletContext;
    }

    public ServletMapping[] getServletMappings() {
        return this._servletMappings;
    }

    public ServletMapping getServletMapping(String pattern) {
        if (this._servletMappings == null) {
            return null;
        }
        ServletMapping[] arr$ = this._servletMappings;
        ServletMapping theMapping = null;
        for (ServletMapping m : arr$) {
            String[] paths = m.getPathSpecs();
            if (paths != null) {
                ServletMapping theMapping2 = theMapping;
                for (String path : paths) {
                    if (pattern.equals(path)) {
                        theMapping2 = m;
                    }
                }
                theMapping = theMapping2;
            }
        }
        return theMapping;
    }

    public ServletHolder[] getServlets() {
        return this._servlets;
    }

    public ServletHolder getServlet(String name) {
        return this._servletNameMap.get(name);
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String old_servlet_path = baseRequest.getServletPath();
        String old_path_info = baseRequest.getPathInfo();
        DispatcherType type = baseRequest.getDispatcherType();
        ServletHolder servlet_holder = null;
        UserIdentity.Scope old_scope = null;
        if (target.startsWith("/")) {
            PathMap.Entry entry = getHolderEntry(target);
            if (entry != null) {
                servlet_holder = (ServletHolder) entry.getValue();
                String servlet_path_spec = (String) entry.getKey();
                String servlet_path = entry.getMapped() != null ? entry.getMapped() : PathMap.pathMatch(servlet_path_spec, target);
                String path_info = PathMap.pathInfo(servlet_path_spec, target);
                if (DispatcherType.INCLUDE.equals(type)) {
                    baseRequest.setAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH, servlet_path);
                    baseRequest.setAttribute(RequestDispatcher.INCLUDE_PATH_INFO, path_info);
                } else {
                    baseRequest.setServletPath(servlet_path);
                    baseRequest.setPathInfo(path_info);
                }
            }
        } else {
            ServletHolder servlet_holder2 = this._servletNameMap.get(target);
            servlet_holder = servlet_holder2;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("servlet {}|{}|{} -> {}", baseRequest.getContextPath(), baseRequest.getServletPath(), baseRequest.getPathInfo(), servlet_holder);
        }
        try {
            old_scope = baseRequest.getUserIdentityScope();
            baseRequest.setUserIdentityScope(servlet_holder);
            if (never()) {
                nextScope(target, baseRequest, request, response);
            } else if (this._nextScope != null) {
                this._nextScope.doScope(target, baseRequest, request, response);
            } else if (this._outerScope != null) {
                this._outerScope.doHandle(target, baseRequest, request, response);
            } else {
                doHandle(target, baseRequest, request, response);
            }
        } finally {
            if (old_scope != null) {
                baseRequest.setUserIdentityScope(old_scope);
            }
            if (!DispatcherType.INCLUDE.equals(type)) {
                baseRequest.setServletPath(old_servlet_path);
                baseRequest.setPathInfo(old_path_info);
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DispatcherType type = baseRequest.getDispatcherType();
        ServletHolder servlet_holder = (ServletHolder) baseRequest.getUserIdentityScope();
        FilterChain chain = null;
        if (target.startsWith("/")) {
            if (servlet_holder != null && this._filterMappings != null && this._filterMappings.length > 0) {
                chain = getFilterChain(baseRequest, target, servlet_holder);
            }
        } else if (servlet_holder != null && this._filterMappings != null && this._filterMappings.length > 0) {
            chain = getFilterChain(baseRequest, null, servlet_holder);
        }
        FilterChain chain2 = chain;
        LOG.debug("chain={}", chain2);
        try {
            try {
                try {
                    if (servlet_holder != null) {
                        ServletRequest servletRequest = request;
                        boolean z = servletRequest instanceof ServletRequestHttpWrapper;
                        ServletRequest req = servletRequest;
                        if (z) {
                            req = ((ServletRequestHttpWrapper) servletRequest).getRequest();
                        }
                        ServletResponse servletResponse = response;
                        boolean z2 = servletResponse instanceof ServletResponseHttpWrapper;
                        ServletResponse res = servletResponse;
                        if (z2) {
                            res = ((ServletResponseHttpWrapper) servletResponse).getResponse();
                        }
                        if (chain2 != null) {
                            chain2.doFilter(req, res);
                        } else {
                            servlet_holder.handle(baseRequest, req, res);
                        }
                    } else if (getHandler() == null) {
                        notFound(request, response);
                    } else {
                        nextHandle(target, baseRequest, request, response);
                    }
                    if (servlet_holder != null) {
                        baseRequest.setHandled(true);
                    }
                    if (0 == 0 || !request.isAsyncStarted()) {
                        return;
                    }
                } catch (Exception e) {
                    if (!DispatcherType.REQUEST.equals(type) && !DispatcherType.ASYNC.equals(type)) {
                        if (e instanceof IOException) {
                            throw ((IOException) e);
                        }
                        if (e instanceof RuntimeException) {
                            throw ((RuntimeException) e);
                        }
                        if (e instanceof ServletException) {
                            throw ((ServletException) e);
                        }
                    }
                    Throwable th = e;
                    if (th instanceof UnavailableException) {
                        LOG.debug(th);
                    } else if (th instanceof ServletException) {
                        LOG.warn(th);
                        Throwable cause = ((ServletException) th).getRootCause();
                        if (cause != null) {
                            th = cause;
                        }
                    }
                    if (th instanceof HttpException) {
                        throw ((HttpException) th);
                    }
                    if (th instanceof RuntimeIOException) {
                        throw ((RuntimeIOException) th);
                    }
                    if (th instanceof EofException) {
                        throw ((EofException) th);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.warn(request.getRequestURI(), th);
                        LOG.debug(request.toString(), new Object[0]);
                    } else {
                        if (!(th instanceof IOException) && !(th instanceof UnavailableException)) {
                            LOG.warn("Error Processing URI: {} - ({}) {}", request.getRequestURI(), th.getClass().getName(), th.getMessage());
                            if (LOG_UNHANDLED.isDebugEnabled()) {
                                LOG_UNHANDLED.debug(request.getRequestURI(), th);
                            }
                        }
                        LOG.debug(request.getRequestURI(), th);
                    }
                    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE, th.getClass());
                    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, th);
                    if (response.isCommitted()) {
                        Logger logger = LOG;
                        logger.debug("Response already committed for handling " + th, new Object[0]);
                    } else if (th instanceof UnavailableException) {
                        UnavailableException ue = (UnavailableException) th;
                        if (ue.isPermanent()) {
                            response.sendError(404);
                        } else {
                            response.sendError(503);
                        }
                    } else {
                        response.sendError(500);
                    }
                    if (servlet_holder != null) {
                        baseRequest.setHandled(true);
                    }
                    if (th == null || !request.isAsyncStarted()) {
                        return;
                    }
                } catch (ContinuationThrowable e2) {
                    throw e2;
                }
            } catch (Error e3) {
                if (!DispatcherType.REQUEST.equals(type) && !DispatcherType.ASYNC.equals(type)) {
                    throw e3;
                }
                Logger logger2 = LOG;
                logger2.warn("Error for " + request.getRequestURI(), e3);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(request.toString(), new Object[0]);
                }
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE, e3.getClass());
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e3);
                if (response.isCommitted()) {
                    LOG.debug("Response already committed for handling ", e3);
                } else {
                    response.sendError(500);
                }
                if (servlet_holder != null) {
                    baseRequest.setHandled(true);
                }
                if (e3 == null || !request.isAsyncStarted()) {
                    return;
                }
            } catch (EofException e4) {
                throw e4;
            } catch (RuntimeIOException e5) {
                throw e5;
            }
            ((AsyncContinuation) request.getAsyncContext()).errorComplete();
        } catch (Throwable th2) {
            if (servlet_holder != null) {
                baseRequest.setHandled(true);
            }
            if (0 != 0 && request.isAsyncStarted()) {
                ((AsyncContinuation) request.getAsyncContext()).errorComplete();
            }
            throw th2;
        }
    }

    protected FilterChain getFilterChain(Request baseRequest, String pathInContext, ServletHolder servletHolder) {
        FilterChain chain;
        String key = pathInContext == null ? servletHolder.getName() : pathInContext;
        int dispatch = FilterMapping.dispatch(baseRequest.getDispatcherType());
        if (this._filterChainsCached && this._chainCache != null && (chain = this._chainCache[dispatch].get(key)) != null) {
            return chain;
        }
        Object o = null;
        if (pathInContext != null && this._filterPathMappings != null) {
            Object filters = null;
            for (int i = 0; i < this._filterPathMappings.size(); i++) {
                FilterMapping mapping = this._filterPathMappings.get(i);
                if (mapping.appliesTo(pathInContext, dispatch)) {
                    filters = LazyList.add(filters, mapping.getFilterHolder());
                }
            }
            o = filters;
        }
        if (servletHolder != null && this._filterNameMappings != null && this._filterNameMappings.size() > 0 && this._filterNameMappings.size() > 0) {
            Object o2 = this._filterNameMappings.get(servletHolder.getName());
            Object filters2 = o;
            for (int i2 = 0; i2 < LazyList.size(o2); i2++) {
                FilterMapping mapping2 = (FilterMapping) LazyList.get(o2, i2);
                if (mapping2.appliesTo(dispatch)) {
                    filters2 = LazyList.add(filters2, mapping2.getFilterHolder());
                }
            }
            Object o3 = this._filterNameMappings.get("*");
            for (int i3 = 0; i3 < LazyList.size(o3); i3++) {
                FilterMapping mapping3 = (FilterMapping) LazyList.get(o3, i3);
                if (mapping3.appliesTo(dispatch)) {
                    filters2 = LazyList.add(filters2, mapping3.getFilterHolder());
                }
            }
            o = filters2;
        }
        if (o == null) {
            return null;
        }
        FilterChain chain2 = null;
        if (this._filterChainsCached) {
            if (LazyList.size(o) > 0) {
                chain2 = newCachedChain(o, servletHolder);
            }
            Map<String, FilterChain> cache = this._chainCache[dispatch];
            Queue<String> lru = this._chainLRU[dispatch];
            while (true) {
                if (this._maxFilterChainsCacheSize <= 0 || cache.size() < this._maxFilterChainsCacheSize) {
                    break;
                }
                String k = lru.poll();
                if (k == null) {
                    cache.clear();
                    break;
                }
                cache.remove(k);
            }
            cache.put(key, chain2);
            lru.add(key);
            return chain2;
        } else if (LazyList.size(o) <= 0) {
            return null;
        } else {
            FilterChain chain3 = new Chain(baseRequest, o, servletHolder);
            return chain3;
        }
    }

    protected void invalidateChainsCache() {
        if (this._chainLRU[1] != null) {
            this._chainLRU[1].clear();
            this._chainLRU[2].clear();
            this._chainLRU[4].clear();
            this._chainLRU[8].clear();
            this._chainLRU[16].clear();
            this._chainCache[1].clear();
            this._chainCache[2].clear();
            this._chainCache[4].clear();
            this._chainCache[8].clear();
            this._chainCache[16].clear();
        }
    }

    public boolean isAvailable() {
        if (isStarted()) {
            ServletHolder[] holders = getServlets();
            for (ServletHolder holder : holders) {
                if (holder != null && !holder.isAvailable()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void setStartWithUnavailable(boolean start) {
        this._startWithUnavailable = start;
    }

    public boolean isStartWithUnavailable() {
        return this._startWithUnavailable;
    }

    public void initialize() throws Exception {
        MultiException mx = new MultiException();
        if (this._filters != null) {
            for (int i = 0; i < this._filters.length; i++) {
                this._filters[i].start();
            }
        }
        if (this._servlets != null) {
            ServletHolder[] servlets = (ServletHolder[]) this._servlets.clone();
            Arrays.sort(servlets);
            for (int i2 = 0; i2 < servlets.length; i2++) {
                try {
                } catch (Throwable e) {
                    LOG.debug(Log.EXCEPTION, e);
                    mx.add(e);
                }
                if (servlets[i2].getClassName() == null && servlets[i2].getForcedPath() != null) {
                    ServletHolder forced_holder = (ServletHolder) this._servletPathMap.match(servlets[i2].getForcedPath());
                    if (forced_holder != null && forced_holder.getClassName() != null) {
                        servlets[i2].setClassName(forced_holder.getClassName());
                    }
                    mx.add(new IllegalStateException("No forced path servlet for " + servlets[i2].getForcedPath()));
                }
                servlets[i2].start();
            }
            mx.ifExceptionThrow();
        }
    }

    public boolean isFilterChainsCached() {
        return this._filterChainsCached;
    }

    public ServletHolder newServletHolder(Holder.Source source) {
        return new ServletHolder(source);
    }

    public CachedChain newCachedChain(Object filters, ServletHolder servletHolder) {
        return new CachedChain(filters, servletHolder);
    }

    public ServletHolder addServletWithMapping(String className, String pathSpec) {
        ServletHolder holder = newServletHolder(Holder.Source.EMBEDDED);
        holder.setClassName(className);
        addServletWithMapping(holder, pathSpec);
        return holder;
    }

    public ServletHolder addServletWithMapping(Class<? extends Servlet> servlet, String pathSpec) {
        ServletHolder holder = newServletHolder(Holder.Source.EMBEDDED);
        holder.setHeldClass(servlet);
        addServletWithMapping(holder, pathSpec);
        return holder;
    }

    public void addServletWithMapping(ServletHolder servlet, String pathSpec) {
        ServletHolder[] holders = getServlets();
        if (holders != null) {
            holders = (ServletHolder[]) holders.clone();
        }
        try {
            setServlets((ServletHolder[]) LazyList.addToArray(holders, servlet, ServletHolder.class));
            ServletMapping mapping = new ServletMapping();
            mapping.setServletName(servlet.getName());
            mapping.setPathSpec(pathSpec);
            setServletMappings((ServletMapping[]) LazyList.addToArray(getServletMappings(), mapping, ServletMapping.class));
        } catch (Exception e) {
            setServlets(holders);
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            }
            throw new RuntimeException(e);
        }
    }

    public void addServlet(ServletHolder holder) {
        setServlets((ServletHolder[]) LazyList.addToArray(getServlets(), holder, ServletHolder.class));
    }

    public void addServletMapping(ServletMapping mapping) {
        setServletMappings((ServletMapping[]) LazyList.addToArray(getServletMappings(), mapping, ServletMapping.class));
    }

    public Set<String> setServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
        if (this._contextHandler != null) {
            return this._contextHandler.setServletSecurity(registration, servletSecurityElement);
        }
        return Collections.emptySet();
    }

    public FilterHolder newFilterHolder(Holder.Source source) {
        return new FilterHolder(source);
    }

    public FilterHolder getFilter(String name) {
        return this._filterNameMap.get(name);
    }

    public FilterHolder addFilterWithMapping(Class<? extends Filter> filter, String pathSpec, EnumSet<DispatcherType> dispatches) {
        FilterHolder holder = newFilterHolder(Holder.Source.EMBEDDED);
        holder.setHeldClass(filter);
        addFilterWithMapping(holder, pathSpec, dispatches);
        return holder;
    }

    public FilterHolder addFilterWithMapping(String className, String pathSpec, EnumSet<DispatcherType> dispatches) {
        FilterHolder holder = newFilterHolder(Holder.Source.EMBEDDED);
        holder.setClassName(className);
        addFilterWithMapping(holder, pathSpec, dispatches);
        return holder;
    }

    public void addFilterWithMapping(FilterHolder holder, String pathSpec, EnumSet<DispatcherType> dispatches) {
        FilterHolder[] holders = getFilters();
        if (holders != null) {
            holders = (FilterHolder[]) holders.clone();
        }
        try {
            setFilters((FilterHolder[]) LazyList.addToArray(holders, holder, FilterHolder.class));
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(holder.getName());
            mapping.setPathSpec(pathSpec);
            mapping.setDispatcherTypes(dispatches);
            addFilterMapping(mapping);
        } catch (Error e) {
            setFilters(holders);
            throw e;
        } catch (RuntimeException e2) {
            setFilters(holders);
            throw e2;
        }
    }

    public FilterHolder addFilterWithMapping(Class<? extends Filter> filter, String pathSpec, int dispatches) {
        FilterHolder holder = newFilterHolder(Holder.Source.EMBEDDED);
        holder.setHeldClass(filter);
        addFilterWithMapping(holder, pathSpec, dispatches);
        return holder;
    }

    public FilterHolder addFilterWithMapping(String className, String pathSpec, int dispatches) {
        FilterHolder holder = newFilterHolder(Holder.Source.EMBEDDED);
        holder.setClassName(className);
        addFilterWithMapping(holder, pathSpec, dispatches);
        return holder;
    }

    public void addFilterWithMapping(FilterHolder holder, String pathSpec, int dispatches) {
        FilterHolder[] holders = getFilters();
        if (holders != null) {
            holders = (FilterHolder[]) holders.clone();
        }
        try {
            setFilters((FilterHolder[]) LazyList.addToArray(holders, holder, FilterHolder.class));
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(holder.getName());
            mapping.setPathSpec(pathSpec);
            mapping.setDispatches(dispatches);
            addFilterMapping(mapping);
        } catch (Error e) {
            setFilters(holders);
            throw e;
        } catch (RuntimeException e2) {
            setFilters(holders);
            throw e2;
        }
    }

    public FilterHolder addFilter(String className, String pathSpec, EnumSet<DispatcherType> dispatches) {
        return addFilterWithMapping(className, pathSpec, dispatches);
    }

    public void addFilter(FilterHolder filter, FilterMapping filterMapping) {
        if (filter != null) {
            setFilters((FilterHolder[]) LazyList.addToArray(getFilters(), filter, FilterHolder.class));
        }
        if (filterMapping != null) {
            addFilterMapping(filterMapping);
        }
    }

    public void addFilter(FilterHolder filter) {
        if (filter != null) {
            setFilters((FilterHolder[]) LazyList.addToArray(getFilters(), filter, FilterHolder.class));
        }
    }

    public void addFilterMapping(FilterMapping mapping) {
        if (mapping != null) {
            Holder.Source source = mapping.getFilterHolder() == null ? null : mapping.getFilterHolder().getSource();
            FilterMapping[] mappings = getFilterMappings();
            if (mappings == null || mappings.length == 0) {
                setFilterMappings(insertFilterMapping(mapping, 0, false));
                if (source != null && source == Holder.Source.JAVAX_API) {
                    this._matchAfterIndex = 0;
                }
            } else if (source != null && Holder.Source.JAVAX_API == source) {
                setFilterMappings(insertFilterMapping(mapping, mappings.length - 1, false));
                if (this._matchAfterIndex < 0) {
                    this._matchAfterIndex = getFilterMappings().length - 1;
                }
            } else if (this._matchAfterIndex < 0) {
                setFilterMappings(insertFilterMapping(mapping, mappings.length - 1, false));
            } else {
                FilterMapping[] new_mappings = insertFilterMapping(mapping, this._matchAfterIndex, true);
                this._matchAfterIndex++;
                setFilterMappings(new_mappings);
            }
        }
    }

    public void prependFilterMapping(FilterMapping mapping) {
        if (mapping != null) {
            Holder.Source source = mapping.getFilterHolder().getSource();
            FilterMapping[] mappings = getFilterMappings();
            if (mappings == null || mappings.length == 0) {
                setFilterMappings(insertFilterMapping(mapping, 0, false));
                if (source != null && Holder.Source.JAVAX_API == source) {
                    this._matchBeforeIndex = 0;
                    return;
                }
                return;
            }
            if (source != null && Holder.Source.JAVAX_API == source) {
                if (this._matchBeforeIndex >= 0) {
                    FilterMapping[] new_mappings = insertFilterMapping(mapping, this._matchBeforeIndex, false);
                    this._matchBeforeIndex++;
                    setFilterMappings(new_mappings);
                } else {
                    this._matchBeforeIndex = 0;
                    FilterMapping[] new_mappings2 = insertFilterMapping(mapping, 0, true);
                    setFilterMappings(new_mappings2);
                }
            } else {
                FilterMapping[] new_mappings3 = insertFilterMapping(mapping, 0, true);
                setFilterMappings(new_mappings3);
            }
            if (this._matchAfterIndex >= 0) {
                this._matchAfterIndex++;
            }
        }
    }

    protected FilterMapping[] insertFilterMapping(FilterMapping mapping, int pos, boolean before) {
        if (pos < 0) {
            throw new IllegalArgumentException("FilterMapping insertion pos < 0");
        }
        FilterMapping[] mappings = getFilterMappings();
        if (mappings == null || mappings.length == 0) {
            return new FilterMapping[]{mapping};
        }
        FilterMapping[] new_mappings = new FilterMapping[mappings.length + 1];
        if (!before) {
            System.arraycopy(mappings, 0, new_mappings, 0, pos + 1);
            new_mappings[pos + 1] = mapping;
            if (mappings.length > pos + 1) {
                System.arraycopy(mappings, pos + 1, new_mappings, pos + 2, mappings.length - (pos + 1));
            }
        } else {
            System.arraycopy(mappings, 0, new_mappings, 0, pos);
            new_mappings[pos] = mapping;
            System.arraycopy(mappings, pos, new_mappings, pos + 1, mappings.length - pos);
        }
        return new_mappings;
    }

    protected synchronized void updateNameMappings() {
        this._filterNameMap.clear();
        int i = 0;
        if (this._filters != null) {
            for (int i2 = 0; i2 < this._filters.length; i2++) {
                this._filterNameMap.put(this._filters[i2].getName(), this._filters[i2]);
                this._filters[i2].setServletHandler(this);
            }
        }
        this._servletNameMap.clear();
        if (this._servlets != null) {
            while (true) {
                int i3 = i;
                if (i3 >= this._servlets.length) {
                    break;
                }
                this._servletNameMap.put(this._servlets[i3].getName(), this._servlets[i3]);
                this._servlets[i3].setServletHandler(this);
                i = i3 + 1;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:54:0x0110 A[Catch: all -> 0x01d3, TryCatch #0 {, blocks: (B:3:0x0001, B:5:0x0007, B:26:0x0097, B:28:0x009b, B:31:0x00a0, B:32:0x00a6, B:34:0x00ab, B:36:0x00bd, B:38:0x00c3, B:40:0x00cd, B:41:0x00d6, B:43:0x00d9, B:45:0x00dd, B:46:0x00e2, B:47:0x00e5, B:48:0x00e8, B:49:0x0106, B:50:0x0107, B:52:0x010c, B:54:0x0110, B:55:0x0113, B:57:0x0117, B:59:0x011d, B:61:0x0126, B:63:0x012e, B:64:0x01b0, B:66:0x01b4, B:72:0x01c6, B:68:0x01bc, B:70:0x01c0, B:77:0x01cd, B:78:0x01d2, B:51:0x010a, B:6:0x000d, B:7:0x001c, B:9:0x0021, B:11:0x0033, B:13:0x0044, B:14:0x004d, B:16:0x0057, B:17:0x0060, B:19:0x0063, B:21:0x0067, B:22:0x0072, B:23:0x0075, B:24:0x0078, B:25:0x0096), top: B:82:0x0001, inners: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:63:0x012e A[Catch: all -> 0x01d3, TRY_LEAVE, TryCatch #0 {, blocks: (B:3:0x0001, B:5:0x0007, B:26:0x0097, B:28:0x009b, B:31:0x00a0, B:32:0x00a6, B:34:0x00ab, B:36:0x00bd, B:38:0x00c3, B:40:0x00cd, B:41:0x00d6, B:43:0x00d9, B:45:0x00dd, B:46:0x00e2, B:47:0x00e5, B:48:0x00e8, B:49:0x0106, B:50:0x0107, B:52:0x010c, B:54:0x0110, B:55:0x0113, B:57:0x0117, B:59:0x011d, B:61:0x0126, B:63:0x012e, B:64:0x01b0, B:66:0x01b4, B:72:0x01c6, B:68:0x01bc, B:70:0x01c0, B:77:0x01cd, B:78:0x01d2, B:51:0x010a, B:6:0x000d, B:7:0x001c, B:9:0x0021, B:11:0x0033, B:13:0x0044, B:14:0x004d, B:16:0x0057, B:17:0x0060, B:19:0x0063, B:21:0x0067, B:22:0x0072, B:23:0x0075, B:24:0x0078, B:25:0x0096), top: B:82:0x0001, inners: #1 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected synchronized void updateMappings() {
        /*
            Method dump skipped, instructions count: 470
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.servlet.ServletHandler.updateMappings():void");
    }

    protected void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Not Found " + request.getRequestURI(), new Object[0]);
        }
    }

    public void setFilterChainsCached(boolean filterChainsCached) {
        this._filterChainsCached = filterChainsCached;
    }

    public void setFilterMappings(FilterMapping[] filterMappings) {
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object[]) this._filterMappings, (Object[]) filterMappings, "filterMapping", true);
        }
        this._filterMappings = filterMappings;
        updateMappings();
        invalidateChainsCache();
    }

    public synchronized void setFilters(FilterHolder[] holders) {
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object[]) this._filters, (Object[]) holders, "filter", true);
        }
        this._filters = holders;
        updateNameMappings();
        invalidateChainsCache();
    }

    public void setServletMappings(ServletMapping[] servletMappings) {
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object[]) this._servletMappings, (Object[]) servletMappings, "servletMapping", true);
        }
        this._servletMappings = servletMappings;
        updateMappings();
        invalidateChainsCache();
    }

    public synchronized void setServlets(ServletHolder[] holders) {
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object[]) this._servlets, (Object[]) holders, "servlet", true);
        }
        this._servlets = holders;
        updateNameMappings();
        invalidateChainsCache();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class CachedChain implements FilterChain {
        FilterHolder _filterHolder;
        CachedChain _next;
        ServletHolder _servletHolder;

        protected CachedChain(Object filters, ServletHolder servletHolder) {
            if (LazyList.size(filters) > 0) {
                this._filterHolder = (FilterHolder) LazyList.get(filters, 0);
                this._next = ServletHandler.this.newCachedChain(LazyList.remove(filters, 0), servletHolder);
                return;
            }
            this._servletHolder = servletHolder;
        }

        @Override // javax.servlet.FilterChain
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            Request baseRequest = request instanceof Request ? (Request) request : AbstractHttpConnection.getCurrentConnection().getRequest();
            if (this._filterHolder != null) {
                if (ServletHandler.LOG.isDebugEnabled()) {
                    Logger logger = ServletHandler.LOG;
                    logger.debug("call filter " + this._filterHolder, new Object[0]);
                }
                Filter filter = this._filterHolder.getFilter();
                if (this._filterHolder.isAsyncSupported()) {
                    filter.doFilter(request, response, this._next);
                    return;
                }
                boolean suspendable = baseRequest.isAsyncSupported();
                if (suspendable) {
                    try {
                        baseRequest.setAsyncSupported(false);
                        filter.doFilter(request, response, this._next);
                        return;
                    } finally {
                        baseRequest.setAsyncSupported(true);
                    }
                }
                filter.doFilter(request, response, this._next);
                return;
            }
            HttpServletRequest srequest = (HttpServletRequest) request;
            if (this._servletHolder != null) {
                if (ServletHandler.LOG.isDebugEnabled()) {
                    Logger logger2 = ServletHandler.LOG;
                    logger2.debug("call servlet " + this._servletHolder, new Object[0]);
                }
                this._servletHolder.handle(baseRequest, request, response);
            } else if (ServletHandler.this.getHandler() == null) {
                ServletHandler.this.notFound(srequest, (HttpServletResponse) response);
            } else {
                ServletHandler.this.nextHandle(URIUtil.addPaths(srequest.getServletPath(), srequest.getPathInfo()), baseRequest, srequest, (HttpServletResponse) response);
            }
        }

        public String toString() {
            if (this._filterHolder != null) {
                return this._filterHolder + "->" + this._next.toString();
            } else if (this._servletHolder != null) {
                return this._servletHolder.toString();
            } else {
                return "null";
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Chain implements FilterChain {
        final Request _baseRequest;
        final Object _chain;
        int _filter = 0;
        final ServletHolder _servletHolder;

        Chain(Request baseRequest, Object filters, ServletHolder servletHolder) {
            this._baseRequest = baseRequest;
            this._chain = filters;
            this._servletHolder = servletHolder;
        }

        @Override // javax.servlet.FilterChain
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (ServletHandler.LOG.isDebugEnabled()) {
                Logger logger = ServletHandler.LOG;
                logger.debug("doFilter " + this._filter, new Object[0]);
            }
            if (this._filter < LazyList.size(this._chain)) {
                Object obj = this._chain;
                int i = this._filter;
                this._filter = i + 1;
                FilterHolder holder = (FilterHolder) LazyList.get(obj, i);
                if (ServletHandler.LOG.isDebugEnabled()) {
                    Logger logger2 = ServletHandler.LOG;
                    logger2.debug("call filter " + holder, new Object[0]);
                }
                Filter filter = holder.getFilter();
                if (holder.isAsyncSupported() || !this._baseRequest.isAsyncSupported()) {
                    filter.doFilter(request, response, this);
                    return;
                }
                try {
                    this._baseRequest.setAsyncSupported(false);
                    filter.doFilter(request, response, this);
                    return;
                } finally {
                    this._baseRequest.setAsyncSupported(true);
                }
            }
            HttpServletRequest srequest = (HttpServletRequest) request;
            if (this._servletHolder != null) {
                if (ServletHandler.LOG.isDebugEnabled()) {
                    Logger logger3 = ServletHandler.LOG;
                    logger3.debug("call servlet " + this._servletHolder, new Object[0]);
                }
                this._servletHolder.handle(this._baseRequest, request, response);
            } else if (ServletHandler.this.getHandler() == null) {
                ServletHandler.this.notFound(srequest, (HttpServletResponse) response);
            } else {
                Request baseRequest = request instanceof Request ? (Request) request : AbstractHttpConnection.getCurrentConnection().getRequest();
                ServletHandler.this.nextHandle(URIUtil.addPaths(srequest.getServletPath(), srequest.getPathInfo()), baseRequest, srequest, (HttpServletResponse) response);
            }
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < LazyList.size(this._chain); i++) {
                Object o = LazyList.get(this._chain, i);
                b.append(o.toString());
                b.append("->");
            }
            b.append(this._servletHolder);
            return b.toString();
        }
    }

    public int getMaxFilterChainsCacheSize() {
        return this._maxFilterChainsCacheSize;
    }

    public void setMaxFilterChainsCacheSize(int maxFilterChainsCacheSize) {
        this._maxFilterChainsCacheSize = maxFilterChainsCacheSize;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroyServlet(Servlet servlet) {
        if (this._contextHandler != null) {
            this._contextHandler.destroyServlet(servlet);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroyFilter(Filter filter) {
        if (this._contextHandler != null) {
            this._contextHandler.destroyFilter(filter);
        }
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        super.dumpThis(out);
        dump(out, indent, TypeUtil.asList(getHandlers()), getBeans(), TypeUtil.asList(getFilterMappings()), TypeUtil.asList(getFilters()), TypeUtil.asList(getServletMappings()), TypeUtil.asList(getServlets()));
    }
}
