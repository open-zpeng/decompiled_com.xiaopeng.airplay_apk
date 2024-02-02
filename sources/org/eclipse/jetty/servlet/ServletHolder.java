package org.eclipse.jetty.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ServletHolder extends Holder<Servlet> implements UserIdentity.Scope, Comparable {
    private static final Logger LOG = Log.getLogger(ServletHolder.class);
    public static final Map<String, String> NO_MAPPED_ROLES = Collections.emptyMap();
    private transient Config _config;
    private transient boolean _enabled;
    private String _forcedPath;
    private IdentityService _identityService;
    private boolean _initOnStartup;
    private int _initOrder;
    private ServletRegistration.Dynamic _registration;
    private Map<String, String> _roleMap;
    private String _runAsRole;
    private RunAsToken _runAsToken;
    private transient Servlet _servlet;
    private transient long _unavailable;
    private transient UnavailableException _unavailableEx;

    public ServletHolder() {
        this(Holder.Source.EMBEDDED);
    }

    public ServletHolder(Holder.Source creator) {
        super(creator);
        this._initOnStartup = false;
        this._enabled = true;
    }

    public ServletHolder(Servlet servlet) {
        this(Holder.Source.EMBEDDED);
        setServlet(servlet);
    }

    public ServletHolder(String name, Class<? extends Servlet> servlet) {
        this(Holder.Source.EMBEDDED);
        setName(name);
        setHeldClass(servlet);
    }

    public ServletHolder(String name, Servlet servlet) {
        this(Holder.Source.EMBEDDED);
        setName(name);
        setServlet(servlet);
    }

    public ServletHolder(Class<? extends Servlet> servlet) {
        this(Holder.Source.EMBEDDED);
        setHeldClass(servlet);
    }

    public UnavailableException getUnavailableException() {
        return this._unavailableEx;
    }

    public synchronized void setServlet(Servlet servlet) {
        if (servlet != null) {
            if (!(servlet instanceof SingleThreadModel)) {
                this._extInstance = true;
                this._servlet = servlet;
                setHeldClass(servlet.getClass());
                if (getName() == null) {
                    setName(servlet.getClass().getName() + "-" + super.hashCode());
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public int getInitOrder() {
        return this._initOrder;
    }

    public void setInitOrder(int order) {
        this._initOnStartup = true;
        this._initOrder = order;
    }

    public boolean isSetInitOrder() {
        return this._initOnStartup;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object o) {
        if (o instanceof ServletHolder) {
            ServletHolder sh = (ServletHolder) o;
            int i = 0;
            if (sh == this) {
                return 0;
            }
            if (sh._initOrder < this._initOrder) {
                return 1;
            }
            if (sh._initOrder > this._initOrder) {
                return -1;
            }
            if (this._className != null && sh._className != null) {
                i = this._className.compareTo(sh._className);
            }
            int c = i;
            if (c == 0) {
                return this._name.compareTo(sh._name);
            }
            return c;
        }
        return 1;
    }

    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    public int hashCode() {
        return this._name == null ? System.identityHashCode(this) : this._name.hashCode();
    }

    public synchronized void setUserRoleLink(String name, String link) {
        if (this._roleMap == null) {
            this._roleMap = new HashMap();
        }
        this._roleMap.put(name, link);
    }

    public String getUserRoleLink(String name) {
        if (this._roleMap == null) {
            return name;
        }
        String link = this._roleMap.get(name);
        return link == null ? name : link;
    }

    public Map<String, String> getRoleMap() {
        return this._roleMap == null ? NO_MAPPED_ROLES : this._roleMap;
    }

    public String getForcedPath() {
        return this._forcedPath;
    }

    public void setForcedPath(String forcedPath) {
        this._forcedPath = forcedPath;
    }

    public boolean isEnabled() {
        return this._enabled;
    }

    public void setEnabled(boolean enabled) {
        this._enabled = enabled;
    }

    @Override // org.eclipse.jetty.servlet.Holder, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._unavailable = 0L;
        if (!this._enabled) {
            return;
        }
        try {
            super.doStart();
            try {
                checkServletType();
                this._identityService = this._servletHandler.getIdentityService();
                if (this._identityService != null && this._runAsRole != null) {
                    this._runAsToken = this._identityService.newRunAsToken(this._runAsRole);
                }
                this._config = new Config();
                if (this._class != null && SingleThreadModel.class.isAssignableFrom(this._class)) {
                    this._servlet = new SingleThreadedWrapper();
                }
                if (this._extInstance || this._initOnStartup) {
                    try {
                        initServlet();
                    } catch (Exception e) {
                        if (this._servletHandler.isStartWithUnavailable()) {
                            LOG.ignore(e);
                            return;
                        }
                        throw e;
                    }
                }
            } catch (UnavailableException ue) {
                makeUnavailable(ue);
                if (this._servletHandler.isStartWithUnavailable()) {
                    LOG.ignore(ue);
                    return;
                }
                throw ue;
            }
        } catch (UnavailableException ue2) {
            makeUnavailable(ue2);
            if (this._servletHandler.isStartWithUnavailable()) {
                LOG.ignore(ue2);
                return;
            }
            throw ue2;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x0031, code lost:
        if (r4._identityService == null) goto L3;
     */
    @Override // org.eclipse.jetty.servlet.Holder, org.eclipse.jetty.util.component.AbstractLifeCycle
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void doStop() throws java.lang.Exception {
        /*
            r4 = this;
            r0 = 0
            javax.servlet.Servlet r1 = r4._servlet
            if (r1 == 0) goto L3e
            org.eclipse.jetty.security.IdentityService r1 = r4._identityService     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            if (r1 == 0) goto L18
            org.eclipse.jetty.security.IdentityService r1 = r4._identityService     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            org.eclipse.jetty.security.IdentityService r2 = r4._identityService     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            org.eclipse.jetty.server.UserIdentity r2 = r2.getSystemUserIdentity()     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            org.eclipse.jetty.security.RunAsToken r3 = r4._runAsToken     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            java.lang.Object r1 = r1.setRunAs(r2, r3)     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            r0 = r1
        L18:
            javax.servlet.Servlet r1 = r4._servlet     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            r4.destroyInstance(r1)     // Catch: java.lang.Throwable -> L27 java.lang.Exception -> L29
            org.eclipse.jetty.security.IdentityService r1 = r4._identityService
            if (r1 == 0) goto L3e
        L21:
            org.eclipse.jetty.security.IdentityService r1 = r4._identityService
            r1.unsetRunAs(r0)
            goto L3e
        L27:
            r1 = move-exception
            goto L34
        L29:
            r1 = move-exception
            org.eclipse.jetty.util.log.Logger r2 = org.eclipse.jetty.servlet.ServletHolder.LOG     // Catch: java.lang.Throwable -> L27
            r2.warn(r1)     // Catch: java.lang.Throwable -> L27
            org.eclipse.jetty.security.IdentityService r1 = r4._identityService
            if (r1 == 0) goto L3e
            goto L21
        L34:
            org.eclipse.jetty.security.IdentityService r2 = r4._identityService
            if (r2 == 0) goto L3d
            org.eclipse.jetty.security.IdentityService r2 = r4._identityService
            r2.unsetRunAs(r0)
        L3d:
            throw r1
        L3e:
            boolean r1 = r4._extInstance
            r2 = 0
            if (r1 != 0) goto L45
            r4._servlet = r2
        L45:
            r4._config = r2
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.servlet.ServletHolder.doStop():void");
    }

    @Override // org.eclipse.jetty.servlet.Holder
    public void destroyInstance(Object o) throws Exception {
        if (o == null) {
            return;
        }
        Servlet servlet = (Servlet) o;
        getServletHandler().destroyServlet(servlet);
        servlet.destroy();
    }

    public synchronized Servlet getServlet() throws ServletException {
        if (this._unavailable != 0) {
            if (this._unavailable < 0 || (this._unavailable > 0 && System.currentTimeMillis() < this._unavailable)) {
                throw this._unavailableEx;
            }
            this._unavailable = 0L;
            this._unavailableEx = null;
        }
        if (this._servlet == null) {
            initServlet();
        }
        return this._servlet;
    }

    public Servlet getServletInstance() {
        return this._servlet;
    }

    public void checkServletType() throws UnavailableException {
        if (this._class == null || !Servlet.class.isAssignableFrom(this._class)) {
            throw new UnavailableException("Servlet " + this._class + " is not a javax.servlet.Servlet");
        }
    }

    public boolean isAvailable() {
        if (isStarted() && this._unavailable == 0) {
            return true;
        }
        try {
            getServlet();
        } catch (Exception e) {
            LOG.ignore(e);
        }
        return isStarted() && this._unavailable == 0;
    }

    private void makeUnavailable(UnavailableException e) {
        if (this._unavailableEx == e && this._unavailable != 0) {
            return;
        }
        this._servletHandler.getServletContext().log("unavailable", e);
        this._unavailableEx = e;
        this._unavailable = -1L;
        if (e.isPermanent()) {
            this._unavailable = -1L;
        } else if (this._unavailableEx.getUnavailableSeconds() > 0) {
            this._unavailable = System.currentTimeMillis() + (1000 * this._unavailableEx.getUnavailableSeconds());
        } else {
            this._unavailable = System.currentTimeMillis() + 5000;
        }
    }

    private void makeUnavailable(final Throwable e) {
        if (e instanceof UnavailableException) {
            makeUnavailable((UnavailableException) e);
            return;
        }
        ServletContext ctx = this._servletHandler.getServletContext();
        if (ctx == null) {
            LOG.info("unavailable", e);
        } else {
            ctx.log("unavailable", e);
        }
        this._unavailableEx = new UnavailableException(String.valueOf(e), -1) { // from class: org.eclipse.jetty.servlet.ServletHolder.1
            {
                initCause(e);
            }
        };
        this._unavailable = -1L;
    }

    private void initServlet() throws ServletException {
        Object old_run_as = null;
        try {
            try {
                if (this._servlet == null) {
                    this._servlet = newInstance();
                }
                if (this._config == null) {
                    this._config = new Config();
                }
                if (this._identityService != null) {
                    old_run_as = this._identityService.setRunAs(this._identityService.getSystemUserIdentity(), this._runAsToken);
                }
                if (isJspServlet()) {
                    initJspServlet();
                }
                initMultiPart();
                this._servlet.init(this._config);
            } catch (UnavailableException e) {
                makeUnavailable(e);
                this._servlet = null;
                this._config = null;
                throw e;
            } catch (ServletException e2) {
                makeUnavailable(e2.getCause() == null ? e2 : e2.getCause());
                this._servlet = null;
                this._config = null;
                throw e2;
            } catch (Exception e3) {
                makeUnavailable(e3);
                this._servlet = null;
                this._config = null;
                throw new ServletException(toString(), e3);
            }
        } finally {
            if (this._identityService != null) {
                this._identityService.unsetRunAs(old_run_as);
            }
        }
    }

    protected void initJspServlet() throws Exception {
        ContextHandler ch = ((ContextHandler.Context) getServletHandler().getServletContext()).getContextHandler();
        ch.setAttribute("org.apache.catalina.jsp_classpath", ch.getClassPath());
        setInitParameter("com.sun.appserv.jsp.classpath", Loader.getClassPath(ch.getClassLoader().getParent()));
        if ("?".equals(getInitParameter("classpath"))) {
            String classpath = ch.getClassPath();
            Logger logger = LOG;
            logger.debug("classpath=" + classpath, new Object[0]);
            if (classpath != null) {
                setInitParameter("classpath", classpath);
            }
        }
    }

    protected void initMultiPart() throws Exception {
        if (((Registration) getRegistration()).getMultipartConfig() != null) {
            ContextHandler ch = ((ContextHandler.Context) getServletHandler().getServletContext()).getContextHandler();
            ch.addEventListener(new Request.MultiPartCleanerListener());
        }
    }

    @Override // org.eclipse.jetty.server.UserIdentity.Scope
    public String getContextPath() {
        return this._config.getServletContext().getContextPath();
    }

    @Override // org.eclipse.jetty.server.UserIdentity.Scope
    public Map<String, String> getRoleRefMap() {
        return this._roleMap;
    }

    public String getRunAsRole() {
        return this._runAsRole;
    }

    public void setRunAsRole(String role) {
        this._runAsRole = role;
    }

    public void handle(Request baseRequest, ServletRequest request, ServletResponse response) throws ServletException, UnavailableException, IOException {
        if (this._class == null) {
            throw new UnavailableException("Servlet Not Initialized");
        }
        Servlet servlet = this._servlet;
        synchronized (this) {
            if (!isStarted()) {
                throw new UnavailableException("Servlet not initialized", -1);
            }
            if (this._unavailable != 0 || !this._initOnStartup) {
                servlet = getServlet();
            }
            if (servlet == null) {
                throw new UnavailableException("Could not instantiate " + this._class);
            }
        }
        Object old_run_as = null;
        boolean suspendable = baseRequest.isAsyncSupported();
        try {
            try {
                if (this._forcedPath != null) {
                    request.setAttribute(Dispatcher.__JSP_FILE, this._forcedPath);
                }
                if (this._identityService != null) {
                    old_run_as = this._identityService.setRunAs(baseRequest.getResolvedUserIdentity(), this._runAsToken);
                }
                if (!isAsyncSupported()) {
                    baseRequest.setAsyncSupported(false);
                }
                MultipartConfigElement mpce = ((Registration) getRegistration()).getMultipartConfig();
                if (mpce != null) {
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, mpce);
                }
                servlet.service(request, response);
                baseRequest.setAsyncSupported(suspendable);
                if (this._identityService != null) {
                    this._identityService.unsetRunAs(old_run_as);
                }
                if (0 != 0) {
                    request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME, getName());
                }
            } catch (UnavailableException e) {
                makeUnavailable(e);
                throw this._unavailableEx;
            }
        } catch (Throwable th) {
            baseRequest.setAsyncSupported(suspendable);
            if (this._identityService != null) {
                this._identityService.unsetRunAs(old_run_as);
            }
            if (1 != 0) {
                request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME, getName());
            }
            throw th;
        }
    }

    private boolean isJspServlet() {
        boolean result = false;
        if (this._servlet == null) {
            return false;
        }
        for (Class c = this._servlet.getClass(); c != null && !result; c = c.getSuperclass()) {
            result = isJspServlet(c.getName());
        }
        return result;
    }

    private boolean isJspServlet(String classname) {
        if (classname == null) {
            return false;
        }
        return "org.apache.jasper.servlet.JspServlet".equals(classname);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class Config extends Holder<Servlet>.HolderConfig implements ServletConfig {
        protected Config() {
            super();
        }

        @Override // javax.servlet.ServletConfig
        public String getServletName() {
            return ServletHolder.this.getName();
        }
    }

    /* loaded from: classes.dex */
    public class Registration extends Holder<Servlet>.HolderRegistration implements ServletRegistration.Dynamic {
        protected MultipartConfigElement _multipartConfig;

        public Registration() {
            super();
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ String getClassName() {
            return super.getClassName();
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ String getInitParameter(String x0) {
            return super.getInitParameter(x0);
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ Map getInitParameters() {
            return super.getInitParameters();
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ String getName() {
            return super.getName();
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration.Dynamic
        public /* bridge */ /* synthetic */ void setAsyncSupported(boolean x0) {
            super.setAsyncSupported(x0);
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration
        public /* bridge */ /* synthetic */ void setDescription(String x0) {
            super.setDescription(x0);
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ boolean setInitParameter(String x0, String x1) {
            return super.setInitParameter(x0, x1);
        }

        @Override // org.eclipse.jetty.servlet.Holder.HolderRegistration, javax.servlet.Registration
        public /* bridge */ /* synthetic */ Set setInitParameters(Map x0) {
            return super.setInitParameters(x0);
        }

        @Override // javax.servlet.ServletRegistration
        public Set<String> addMapping(String... urlPatterns) {
            ServletHolder.this.illegalStateIfContextStarted();
            Set<String> clash = null;
            for (String pattern : urlPatterns) {
                ServletMapping mapping = ServletHolder.this._servletHandler.getServletMapping(pattern);
                if (mapping != null && !mapping.isDefault()) {
                    if (clash == null) {
                        clash = new HashSet<>();
                    }
                    clash.add(pattern);
                }
            }
            if (clash != null) {
                return clash;
            }
            ServletMapping mapping2 = new ServletMapping();
            mapping2.setServletName(ServletHolder.this.getName());
            mapping2.setPathSpecs(urlPatterns);
            ServletHolder.this._servletHandler.addServletMapping(mapping2);
            return Collections.emptySet();
        }

        @Override // javax.servlet.ServletRegistration
        public Collection<String> getMappings() {
            String[] specs;
            ServletMapping[] mappings = ServletHolder.this._servletHandler.getServletMappings();
            List<String> patterns = new ArrayList<>();
            if (mappings != null) {
                for (ServletMapping mapping : mappings) {
                    if (mapping.getServletName().equals(getName()) && (specs = mapping.getPathSpecs()) != null && specs.length > 0) {
                        patterns.addAll(Arrays.asList(specs));
                    }
                }
            }
            return patterns;
        }

        @Override // javax.servlet.ServletRegistration
        public String getRunAsRole() {
            return ServletHolder.this._runAsRole;
        }

        @Override // javax.servlet.ServletRegistration.Dynamic
        public void setLoadOnStartup(int loadOnStartup) {
            ServletHolder.this.illegalStateIfContextStarted();
            ServletHolder.this.setInitOrder(loadOnStartup);
        }

        public int getInitOrder() {
            return ServletHolder.this.getInitOrder();
        }

        @Override // javax.servlet.ServletRegistration.Dynamic
        public void setMultipartConfig(MultipartConfigElement element) {
            this._multipartConfig = element;
        }

        public MultipartConfigElement getMultipartConfig() {
            return this._multipartConfig;
        }

        @Override // javax.servlet.ServletRegistration.Dynamic
        public void setRunAsRole(String role) {
            ServletHolder.this._runAsRole = role;
        }

        @Override // javax.servlet.ServletRegistration.Dynamic
        public Set<String> setServletSecurity(ServletSecurityElement securityElement) {
            return ServletHolder.this._servletHandler.setServletSecurity(this, securityElement);
        }
    }

    public ServletRegistration.Dynamic getRegistration() {
        if (this._registration == null) {
            this._registration = new Registration();
        }
        return this._registration;
    }

    /* loaded from: classes.dex */
    private class SingleThreadedWrapper implements Servlet {
        Stack<Servlet> _stack;

        private SingleThreadedWrapper() {
            this._stack = new Stack<>();
        }

        @Override // javax.servlet.Servlet
        public void destroy() {
            synchronized (this) {
                while (this._stack.size() > 0) {
                    try {
                        this._stack.pop().destroy();
                    } catch (Exception e) {
                        ServletHolder.LOG.warn(e);
                    }
                }
            }
        }

        @Override // javax.servlet.Servlet
        public ServletConfig getServletConfig() {
            return ServletHolder.this._config;
        }

        @Override // javax.servlet.Servlet
        public String getServletInfo() {
            return null;
        }

        @Override // javax.servlet.Servlet
        public void init(ServletConfig config) throws ServletException {
            synchronized (this) {
                if (this._stack.size() == 0) {
                    try {
                        Servlet s = ServletHolder.this.newInstance();
                        s.init(config);
                        this._stack.push(s);
                    } catch (ServletException e) {
                        throw e;
                    } catch (Exception e2) {
                        throw new ServletException(e2);
                    }
                }
            }
        }

        @Override // javax.servlet.Servlet
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            Servlet s;
            synchronized (this) {
                if (this._stack.size() > 0) {
                    s = this._stack.pop();
                } else {
                    try {
                        try {
                            s = ServletHolder.this.newInstance();
                            s.init(ServletHolder.this._config);
                        } catch (ServletException e) {
                            throw e;
                        }
                    } catch (Exception e2) {
                        throw new ServletException(e2);
                    }
                }
            }
            try {
                s.service(req, res);
                synchronized (this) {
                    this._stack.push(s);
                }
            } catch (Throwable th) {
                synchronized (this) {
                    this._stack.push(s);
                    throw th;
                }
            }
        }
    }

    protected Servlet newInstance() throws ServletException, IllegalAccessException, InstantiationException {
        try {
            ServletContext ctx = getServletHandler().getServletContext();
            if (ctx == null) {
                return getHeldClass().newInstance();
            }
            return ((ServletContextHandler.Context) ctx).createServlet(getHeldClass());
        } catch (ServletException se) {
            Throwable cause = se.getRootCause();
            if (cause instanceof InstantiationException) {
                throw ((InstantiationException) cause);
            }
            if (cause instanceof IllegalAccessException) {
                throw ((IllegalAccessException) cause);
            }
            throw se;
        }
    }
}
