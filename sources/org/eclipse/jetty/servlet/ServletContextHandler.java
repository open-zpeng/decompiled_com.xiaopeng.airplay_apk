package org.eclipse.jetty.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;
import org.eclipse.jetty.security.ConstraintAware;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
/* loaded from: classes.dex */
public class ServletContextHandler extends ContextHandler {
    public static final int NO_SECURITY = 0;
    public static final int NO_SESSIONS = 0;
    public static final int SECURITY = 2;
    public static final int SESSIONS = 1;
    protected final List<Decorator> _decorators;
    protected Class<? extends SecurityHandler> _defaultSecurityHandlerClass;
    protected JspConfigDescriptor _jspConfig;
    protected int _options;
    private boolean _restrictListeners;
    protected Object _restrictedContextListeners;
    protected SecurityHandler _securityHandler;
    protected ServletHandler _servletHandler;
    protected SessionHandler _sessionHandler;
    protected HandlerWrapper _wrapper;

    /* loaded from: classes.dex */
    public interface Decorator {
        void decorateFilterHolder(FilterHolder filterHolder) throws ServletException;

        <T extends Filter> T decorateFilterInstance(T t) throws ServletException;

        <T extends EventListener> T decorateListenerInstance(T t) throws ServletException;

        void decorateServletHolder(ServletHolder servletHolder) throws ServletException;

        <T extends Servlet> T decorateServletInstance(T t) throws ServletException;

        void destroyFilterInstance(Filter filter);

        void destroyListenerInstance(EventListener eventListener);

        void destroyServletInstance(Servlet servlet);
    }

    public ServletContextHandler() {
        this(null, null, null, null, null);
    }

    public ServletContextHandler(int options) {
        this(null, null, options);
    }

    public ServletContextHandler(HandlerContainer parent, String contextPath) {
        this(parent, contextPath, null, null, null, null);
    }

    public ServletContextHandler(HandlerContainer parent, String contextPath, int options) {
        this(parent, contextPath, null, null, null, null);
        this._options = options;
    }

    public ServletContextHandler(HandlerContainer parent, String contextPath, boolean sessions, boolean security) {
        this(parent, contextPath, (security ? 2 : 0) | (sessions ? 1 : 0));
    }

    public ServletContextHandler(HandlerContainer parent, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
        this(parent, null, sessionHandler, securityHandler, servletHandler, errorHandler);
    }

    public ServletContextHandler(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
        super((ContextHandler.Context) null);
        this._decorators = new ArrayList();
        this._defaultSecurityHandlerClass = ConstraintSecurityHandler.class;
        this._restrictListeners = true;
        this._scontext = new Context();
        this._sessionHandler = sessionHandler;
        this._securityHandler = securityHandler;
        this._servletHandler = servletHandler;
        if (errorHandler != null) {
            setErrorHandler(errorHandler);
        }
        if (contextPath != null) {
            setContextPath(contextPath);
        }
        if (parent instanceof HandlerWrapper) {
            ((HandlerWrapper) parent).setHandler(this);
        } else if (parent instanceof HandlerCollection) {
            ((HandlerCollection) parent).addHandler(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.ContextHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        if (this._decorators != null) {
            this._decorators.clear();
        }
        if (this._wrapper != null) {
            this._wrapper.setHandler(null);
        }
    }

    public Class<? extends SecurityHandler> getDefaultSecurityHandlerClass() {
        return this._defaultSecurityHandlerClass;
    }

    public void setDefaultSecurityHandlerClass(Class<? extends SecurityHandler> defaultSecurityHandlerClass) {
        this._defaultSecurityHandlerClass = defaultSecurityHandlerClass;
    }

    protected SessionHandler newSessionHandler() {
        return new SessionHandler();
    }

    protected SecurityHandler newSecurityHandler() {
        try {
            return this._defaultSecurityHandlerClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected ServletHandler newServletHandler() {
        return new ServletHandler();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.ContextHandler
    public void startContext() throws Exception {
        getSessionHandler();
        getSecurityHandler();
        getServletHandler();
        Handler handler = this._servletHandler;
        if (this._securityHandler != null) {
            this._securityHandler.setHandler(handler);
            handler = this._securityHandler;
        }
        if (this._sessionHandler != null) {
            this._sessionHandler.setHandler(handler);
            handler = this._sessionHandler;
        }
        this._wrapper = this;
        while (this._wrapper != handler && (this._wrapper.getHandler() instanceof HandlerWrapper)) {
            this._wrapper = (HandlerWrapper) this._wrapper.getHandler();
        }
        if (this._wrapper != handler) {
            if (this._wrapper.getHandler() != null) {
                throw new IllegalStateException("!ScopedHandler");
            }
            this._wrapper.setHandler(handler);
        }
        super.startContext();
        if (this._servletHandler != null && this._servletHandler.isStarted()) {
            for (int i = this._decorators.size() - 1; i >= 0; i--) {
                Decorator decorator = this._decorators.get(i);
                if (this._servletHandler.getFilters() != null) {
                    FilterHolder[] arr$ = this._servletHandler.getFilters();
                    for (FilterHolder holder : arr$) {
                        decorator.decorateFilterHolder(holder);
                    }
                }
                if (this._servletHandler.getServlets() != null) {
                    ServletHolder[] arr$2 = this._servletHandler.getServlets();
                    for (ServletHolder holder2 : arr$2) {
                        decorator.decorateServletHolder(holder2);
                    }
                }
            }
            this._servletHandler.initialize();
        }
    }

    public SecurityHandler getSecurityHandler() {
        if (this._securityHandler == null && (this._options & 2) != 0 && !isStarted()) {
            this._securityHandler = newSecurityHandler();
        }
        return this._securityHandler;
    }

    public ServletHandler getServletHandler() {
        if (this._servletHandler == null && !isStarted()) {
            this._servletHandler = newServletHandler();
        }
        return this._servletHandler;
    }

    public SessionHandler getSessionHandler() {
        if (this._sessionHandler == null && (this._options & 1) != 0 && !isStarted()) {
            this._sessionHandler = newSessionHandler();
        }
        return this._sessionHandler;
    }

    public ServletHolder addServlet(String className, String pathSpec) {
        return getServletHandler().addServletWithMapping(className, pathSpec);
    }

    public ServletHolder addServlet(Class<? extends Servlet> servlet, String pathSpec) {
        return getServletHandler().addServletWithMapping(servlet.getName(), pathSpec);
    }

    public void addServlet(ServletHolder servlet, String pathSpec) {
        getServletHandler().addServletWithMapping(servlet, pathSpec);
    }

    public void addFilter(FilterHolder holder, String pathSpec, EnumSet<DispatcherType> dispatches) {
        getServletHandler().addFilterWithMapping(holder, pathSpec, dispatches);
    }

    public FilterHolder addFilter(Class<? extends Filter> filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
        return getServletHandler().addFilterWithMapping(filterClass, pathSpec, dispatches);
    }

    public FilterHolder addFilter(String filterClass, String pathSpec, EnumSet<DispatcherType> dispatches) {
        return getServletHandler().addFilterWithMapping(filterClass, pathSpec, dispatches);
    }

    protected ServletRegistration.Dynamic dynamicHolderAdded(ServletHolder holder) {
        return holder.getRegistration();
    }

    protected void addRoles(String... roleNames) {
        if (this._securityHandler != null && (this._securityHandler instanceof ConstraintAware)) {
            HashSet<String> union = new HashSet<>();
            Set<String> existing = ((ConstraintAware) this._securityHandler).getRoles();
            if (existing != null) {
                union.addAll(existing);
            }
            union.addAll(Arrays.asList(roleNames));
            ((ConstraintSecurityHandler) this._securityHandler).setRoles(union);
        }
    }

    public Set<String> setServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
        Collection<String> pathSpecs = registration.getMappings();
        if (pathSpecs != null) {
            for (String pathSpec : pathSpecs) {
                List<ConstraintMapping> mappings = ConstraintSecurityHandler.createConstraintsWithMappingsForPath(registration.getName(), pathSpec, servletSecurityElement);
                for (ConstraintMapping m : mappings) {
                    ((ConstraintAware) getSecurityHandler()).addConstraintMapping(m);
                }
            }
        }
        return Collections.emptySet();
    }

    @Override // org.eclipse.jetty.server.handler.ContextHandler
    public void restrictEventListener(EventListener e) {
        if (this._restrictListeners && (e instanceof ServletContextListener)) {
            this._restrictedContextListeners = LazyList.add(this._restrictedContextListeners, e);
        }
    }

    public boolean isRestrictListeners() {
        return this._restrictListeners;
    }

    public void setRestrictListeners(boolean restrictListeners) {
        this._restrictListeners = restrictListeners;
    }

    @Override // org.eclipse.jetty.server.handler.ContextHandler
    public void callContextInitialized(ServletContextListener l, ServletContextEvent e) {
        try {
            if (LazyList.contains(this._restrictedContextListeners, l)) {
                getServletContext().setEnabled(false);
            }
            super.callContextInitialized(l, e);
        } finally {
            getServletContext().setEnabled(true);
        }
    }

    @Override // org.eclipse.jetty.server.handler.ContextHandler
    public void callContextDestroyed(ServletContextListener l, ServletContextEvent e) {
        super.callContextDestroyed(l, e);
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        this._sessionHandler = sessionHandler;
    }

    public void setSecurityHandler(SecurityHandler securityHandler) {
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        this._securityHandler = securityHandler;
    }

    public void setServletHandler(ServletHandler servletHandler) {
        if (isStarted()) {
            throw new IllegalStateException(AbstractLifeCycle.STARTED);
        }
        this._servletHandler = servletHandler;
    }

    public List<Decorator> getDecorators() {
        return Collections.unmodifiableList(this._decorators);
    }

    public void setDecorators(List<Decorator> decorators) {
        this._decorators.clear();
        this._decorators.addAll(decorators);
    }

    public void addDecorator(Decorator decorator) {
        this._decorators.add(decorator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroyServlet(Servlet servlet) {
        for (Decorator decorator : this._decorators) {
            decorator.destroyServletInstance(servlet);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void destroyFilter(Filter filter) {
        for (Decorator decorator : this._decorators) {
            decorator.destroyFilterInstance(filter);
        }
    }

    /* loaded from: classes.dex */
    public static class JspPropertyGroup implements JspPropertyGroupDescriptor {
        private String _buffer;
        private String _defaultContentType;
        private String _deferredSyntaxAllowedAsLiteral;
        private String _elIgnored;
        private String _errorOnUndeclaredNamespace;
        private String _isXml;
        private String _pageEncoding;
        private String _scriptingInvalid;
        private String _trimDirectiveWhitespaces;
        private List<String> _urlPatterns = new ArrayList();
        private List<String> _includePreludes = new ArrayList();
        private List<String> _includeCodas = new ArrayList();

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public Collection<String> getUrlPatterns() {
            return new ArrayList(this._urlPatterns);
        }

        public void addUrlPattern(String s) {
            if (!this._urlPatterns.contains(s)) {
                this._urlPatterns.add(s);
            }
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getElIgnored() {
            return this._elIgnored;
        }

        public void setElIgnored(String s) {
            this._elIgnored = s;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getPageEncoding() {
            return this._pageEncoding;
        }

        public void setPageEncoding(String pageEncoding) {
            this._pageEncoding = pageEncoding;
        }

        public void setScriptingInvalid(String scriptingInvalid) {
            this._scriptingInvalid = scriptingInvalid;
        }

        public void setIsXml(String isXml) {
            this._isXml = isXml;
        }

        public void setDeferredSyntaxAllowedAsLiteral(String deferredSyntaxAllowedAsLiteral) {
            this._deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
        }

        public void setTrimDirectiveWhitespaces(String trimDirectiveWhitespaces) {
            this._trimDirectiveWhitespaces = trimDirectiveWhitespaces;
        }

        public void setDefaultContentType(String defaultContentType) {
            this._defaultContentType = defaultContentType;
        }

        public void setBuffer(String buffer) {
            this._buffer = buffer;
        }

        public void setErrorOnUndeclaredNamespace(String errorOnUndeclaredNamespace) {
            this._errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getScriptingInvalid() {
            return this._scriptingInvalid;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getIsXml() {
            return this._isXml;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public Collection<String> getIncludePreludes() {
            return new ArrayList(this._includePreludes);
        }

        public void addIncludePrelude(String prelude) {
            if (!this._includePreludes.contains(prelude)) {
                this._includePreludes.add(prelude);
            }
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public Collection<String> getIncludeCodas() {
            return new ArrayList(this._includeCodas);
        }

        public void addIncludeCoda(String coda) {
            if (!this._includeCodas.contains(coda)) {
                this._includeCodas.add(coda);
            }
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getDeferredSyntaxAllowedAsLiteral() {
            return this._deferredSyntaxAllowedAsLiteral;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getTrimDirectiveWhitespaces() {
            return this._trimDirectiveWhitespaces;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getDefaultContentType() {
            return this._defaultContentType;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getBuffer() {
            return this._buffer;
        }

        @Override // javax.servlet.descriptor.JspPropertyGroupDescriptor
        public String getErrorOnUndeclaredNamespace() {
            return this._errorOnUndeclaredNamespace;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("JspPropertyGroupDescriptor:");
            sb.append(" el-ignored=" + this._elIgnored);
            sb.append(" is-xml=" + this._isXml);
            sb.append(" page-encoding=" + this._pageEncoding);
            sb.append(" scripting-invalid=" + this._scriptingInvalid);
            sb.append(" deferred-syntax-allowed-as-literal=" + this._deferredSyntaxAllowedAsLiteral);
            sb.append(" trim-directive-whitespaces" + this._trimDirectiveWhitespaces);
            sb.append(" default-content-type=" + this._defaultContentType);
            sb.append(" buffer=" + this._buffer);
            sb.append(" error-on-undeclared-namespace=" + this._errorOnUndeclaredNamespace);
            for (String prelude : this._includePreludes) {
                sb.append(" include-prelude=" + prelude);
            }
            for (String coda : this._includeCodas) {
                sb.append(" include-coda=" + coda);
            }
            return sb.toString();
        }
    }

    /* loaded from: classes.dex */
    public static class TagLib implements TaglibDescriptor {
        private String _location;
        private String _uri;

        @Override // javax.servlet.descriptor.TaglibDescriptor
        public String getTaglibURI() {
            return this._uri;
        }

        public void setTaglibURI(String uri) {
            this._uri = uri;
        }

        @Override // javax.servlet.descriptor.TaglibDescriptor
        public String getTaglibLocation() {
            return this._location;
        }

        public void setTaglibLocation(String location) {
            this._location = location;
        }

        public String toString() {
            return "TagLibDescriptor: taglib-uri=" + this._uri + " location=" + this._location;
        }
    }

    /* loaded from: classes.dex */
    public static class JspConfig implements JspConfigDescriptor {
        private List<TaglibDescriptor> _taglibs = new ArrayList();
        private List<JspPropertyGroupDescriptor> _jspPropertyGroups = new ArrayList();

        @Override // javax.servlet.descriptor.JspConfigDescriptor
        public Collection<TaglibDescriptor> getTaglibs() {
            return new ArrayList(this._taglibs);
        }

        public void addTaglibDescriptor(TaglibDescriptor d) {
            this._taglibs.add(d);
        }

        @Override // javax.servlet.descriptor.JspConfigDescriptor
        public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
            return new ArrayList(this._jspPropertyGroups);
        }

        public void addJspPropertyGroup(JspPropertyGroupDescriptor g) {
            this._jspPropertyGroups.add(g);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("JspConfigDescriptor: \n");
            for (TaglibDescriptor taglib : this._taglibs) {
                sb.append(taglib + "\n");
            }
            for (JspPropertyGroupDescriptor jpg : this._jspPropertyGroups) {
                sb.append(jpg + "\n");
            }
            return sb.toString();
        }
    }

    /* loaded from: classes.dex */
    public class Context extends ContextHandler.Context {
        public Context() {
            super();
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public RequestDispatcher getNamedDispatcher(String name) {
            ServletHolder holder;
            ContextHandler context = ServletContextHandler.this;
            if (ServletContextHandler.this._servletHandler == null || (holder = ServletContextHandler.this._servletHandler.getServlet(name)) == null || !holder.isEnabled()) {
                return null;
            }
            return new Dispatcher(context, name);
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            if (ServletContextHandler.this.isStarted()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            FilterHolder holder = handler.getFilter(filterName);
            if (holder == null) {
                FilterHolder holder2 = handler.newFilterHolder(Holder.Source.JAVAX_API);
                holder2.setName(filterName);
                holder2.setHeldClass(filterClass);
                handler.addFilter(holder2);
                return holder2.getRegistration();
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setHeldClass(filterClass);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, String className) {
            if (ServletContextHandler.this.isStarted()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            FilterHolder holder = handler.getFilter(filterName);
            if (holder == null) {
                FilterHolder holder2 = handler.newFilterHolder(Holder.Source.JAVAX_API);
                holder2.setName(filterName);
                holder2.setClassName(className);
                handler.addFilter(holder2);
                return holder2.getRegistration();
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setClassName(className);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            if (ServletContextHandler.this.isStarted()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            FilterHolder holder = handler.getFilter(filterName);
            if (holder == null) {
                FilterHolder holder2 = handler.newFilterHolder(Holder.Source.JAVAX_API);
                holder2.setName(filterName);
                holder2.setFilter(filter);
                handler.addFilter(holder2);
                return holder2.getRegistration();
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setFilter(filter);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            ServletHolder holder = handler.getServlet(servletName);
            if (holder == null) {
                ServletHolder holder2 = handler.newServletHolder(Holder.Source.JAVAX_API);
                holder2.setName(servletName);
                holder2.setHeldClass(servletClass);
                handler.addServlet(holder2);
                return ServletContextHandler.this.dynamicHolderAdded(holder2);
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setHeldClass(servletClass);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            ServletHolder holder = handler.getServlet(servletName);
            if (holder == null) {
                ServletHolder holder2 = handler.newServletHolder(Holder.Source.JAVAX_API);
                holder2.setName(servletName);
                holder2.setClassName(className);
                handler.addServlet(holder2);
                return ServletContextHandler.this.dynamicHolderAdded(holder2);
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setClassName(className);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            ServletHolder holder = handler.getServlet(servletName);
            if (holder == null) {
                ServletHolder holder2 = handler.newServletHolder(Holder.Source.JAVAX_API);
                holder2.setName(servletName);
                holder2.setServlet(servlet);
                handler.addServlet(holder2);
                return ServletContextHandler.this.dynamicHolderAdded(holder2);
            } else if (holder.getClassName() == null && holder.getHeldClass() == null) {
                holder.setServlet(servlet);
                return holder.getRegistration();
            } else {
                return null;
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public boolean setInitParameter(String name, String value) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            return super.setInitParameter(name, value);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r3v0, types: [javax.servlet.Filter] */
        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
            try {
                T f = c.newInstance();
                for (int i = ServletContextHandler.this._decorators.size() - 1; i >= 0; i--) {
                    Decorator decorator = ServletContextHandler.this._decorators.get(i);
                    f = decorator.decorateFilterInstance(f);
                }
                return f;
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            } catch (InstantiationException e2) {
                throw new ServletException(e2);
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r3v0, types: [javax.servlet.Servlet] */
        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
            try {
                T s = c.newInstance();
                for (int i = ServletContextHandler.this._decorators.size() - 1; i >= 0; i--) {
                    Decorator decorator = ServletContextHandler.this._decorators.get(i);
                    s = decorator.decorateServletInstance(s);
                }
                return s;
            } catch (IllegalAccessException e) {
                throw new ServletException(e);
            } catch (InstantiationException e2) {
                throw new ServletException(e2);
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            if (ServletContextHandler.this._sessionHandler != null) {
                return ServletContextHandler.this._sessionHandler.getSessionManager().getDefaultSessionTrackingModes();
            }
            return null;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            if (ServletContextHandler.this._sessionHandler != null) {
                return ServletContextHandler.this._sessionHandler.getSessionManager().getEffectiveSessionTrackingModes();
            }
            return null;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public FilterRegistration getFilterRegistration(String filterName) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            FilterHolder holder = ServletContextHandler.this.getServletHandler().getFilter(filterName);
            if (holder == null) {
                return null;
            }
            return holder.getRegistration();
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            HashMap<String, FilterRegistration> registrations = new HashMap<>();
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            FilterHolder[] holders = handler.getFilters();
            if (holders != null) {
                for (FilterHolder holder : holders) {
                    registrations.put(holder.getName(), holder.getRegistration());
                }
            }
            return registrations;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public ServletRegistration getServletRegistration(String servletName) {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletHolder holder = ServletContextHandler.this.getServletHandler().getServlet(servletName);
            if (holder == null) {
                return null;
            }
            return holder.getRegistration();
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            HashMap<String, ServletRegistration> registrations = new HashMap<>();
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            ServletHolder[] holders = handler.getServlets();
            if (holders != null) {
                for (ServletHolder holder : holders) {
                    registrations.put(holder.getName(), holder.getRegistration());
                }
            }
            return registrations;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public SessionCookieConfig getSessionCookieConfig() {
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            if (ServletContextHandler.this._sessionHandler != null) {
                return ServletContextHandler.this._sessionHandler.getSessionManager().getSessionCookieConfig();
            }
            return null;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            if (ServletContextHandler.this._sessionHandler != null) {
                ServletContextHandler.this._sessionHandler.getSessionManager().setSessionTrackingModes(sessionTrackingModes);
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public void addListener(String className) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            super.addListener(className);
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public <T extends EventListener> void addListener(T t) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            super.addListener((Context) t);
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public void addListener(Class<? extends EventListener> listenerClass) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            super.addListener(listenerClass);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r3v0, types: [java.util.EventListener] */
        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
            try {
                T l = (T) super.createListener(clazz);
                for (int i = ServletContextHandler.this._decorators.size() - 1; i >= 0; i--) {
                    Decorator decorator = ServletContextHandler.this._decorators.get(i);
                    l = decorator.decorateListenerInstance(l);
                }
                return l;
            } catch (ServletException e) {
                throw e;
            } catch (Exception e2) {
                throw new ServletException(e2);
            }
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public JspConfigDescriptor getJspConfigDescriptor() {
            return ServletContextHandler.this._jspConfig;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context
        public void setJspConfigDescriptor(JspConfigDescriptor d) {
            ServletContextHandler.this._jspConfig = d;
        }

        @Override // org.eclipse.jetty.server.handler.ContextHandler.Context, javax.servlet.ServletContext
        public void declareRoles(String... roleNames) {
            if (!ServletContextHandler.this.isStarting()) {
                throw new IllegalStateException();
            }
            if (!this._enabled) {
                throw new UnsupportedOperationException();
            }
            ServletContextHandler.this.addRoles(roleNames);
        }
    }
}
