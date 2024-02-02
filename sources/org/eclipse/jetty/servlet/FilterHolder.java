package org.eclipse.jetty.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class FilterHolder extends Holder<Filter> {
    private static final Logger LOG = Log.getLogger(FilterHolder.class);
    private transient Config _config;
    private transient Filter _filter;
    private transient FilterRegistration.Dynamic _registration;

    public FilterHolder() {
        this(Holder.Source.EMBEDDED);
    }

    public FilterHolder(Holder.Source source) {
        super(source);
    }

    public FilterHolder(Class<? extends Filter> filter) {
        this(Holder.Source.EMBEDDED);
        setHeldClass(filter);
    }

    public FilterHolder(Filter filter) {
        this(Holder.Source.EMBEDDED);
        setFilter(filter);
    }

    @Override // org.eclipse.jetty.servlet.Holder, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        if (!Filter.class.isAssignableFrom(this._class)) {
            String msg = this._class + " is not a javax.servlet.Filter";
            super.stop();
            throw new IllegalStateException(msg);
        }
        if (this._filter == null) {
            try {
                this._filter = ((ServletContextHandler.Context) this._servletHandler.getServletContext()).createFilter(getHeldClass());
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
        this._config = new Config();
        this._filter.init(this._config);
    }

    @Override // org.eclipse.jetty.servlet.Holder, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        if (this._filter != null) {
            try {
                destroyInstance(this._filter);
            } catch (Exception e) {
                LOG.warn(e);
            }
        }
        if (!this._extInstance) {
            this._filter = null;
        }
        this._config = null;
        super.doStop();
    }

    @Override // org.eclipse.jetty.servlet.Holder
    public void destroyInstance(Object o) throws Exception {
        if (o == null) {
            return;
        }
        Filter f = (Filter) o;
        f.destroy();
        getServletHandler().destroyFilter(f);
    }

    public synchronized void setFilter(Filter filter) {
        this._filter = filter;
        this._extInstance = true;
        setHeldClass(filter.getClass());
        if (getName() == null) {
            setName(filter.getClass().getName());
        }
    }

    public Filter getFilter() {
        return this._filter;
    }

    @Override // org.eclipse.jetty.servlet.Holder
    public String toString() {
        return getName();
    }

    public FilterRegistration.Dynamic getRegistration() {
        if (this._registration == null) {
            this._registration = new Registration();
        }
        return this._registration;
    }

    /* loaded from: classes.dex */
    protected class Registration extends Holder<Filter>.HolderRegistration implements FilterRegistration.Dynamic {
        protected Registration() {
            super();
        }

        @Override // javax.servlet.FilterRegistration
        public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
            FilterHolder.this.illegalStateIfContextStarted();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterHolder(FilterHolder.this);
            mapping.setServletNames(servletNames);
            mapping.setDispatcherTypes(dispatcherTypes);
            if (isMatchAfter) {
                FilterHolder.this._servletHandler.addFilterMapping(mapping);
            } else {
                FilterHolder.this._servletHandler.prependFilterMapping(mapping);
            }
        }

        @Override // javax.servlet.FilterRegistration
        public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
            FilterHolder.this.illegalStateIfContextStarted();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterHolder(FilterHolder.this);
            mapping.setPathSpecs(urlPatterns);
            mapping.setDispatcherTypes(dispatcherTypes);
            if (isMatchAfter) {
                FilterHolder.this._servletHandler.addFilterMapping(mapping);
            } else {
                FilterHolder.this._servletHandler.prependFilterMapping(mapping);
            }
        }

        @Override // javax.servlet.FilterRegistration
        public Collection<String> getServletNameMappings() {
            String[] servlets;
            FilterMapping[] mappings = FilterHolder.this._servletHandler.getFilterMappings();
            List<String> names = new ArrayList<>();
            for (FilterMapping mapping : mappings) {
                if (mapping.getFilterHolder() == FilterHolder.this && (servlets = mapping.getServletNames()) != null && servlets.length > 0) {
                    names.addAll(Arrays.asList(servlets));
                }
            }
            return names;
        }

        @Override // javax.servlet.FilterRegistration
        public Collection<String> getUrlPatternMappings() {
            FilterMapping[] mappings = FilterHolder.this._servletHandler.getFilterMappings();
            List<String> patterns = new ArrayList<>();
            for (FilterMapping mapping : mappings) {
                if (mapping.getFilterHolder() == FilterHolder.this) {
                    String[] specs = mapping.getPathSpecs();
                    patterns.addAll(TypeUtil.asList(specs));
                }
            }
            return patterns;
        }
    }

    /* loaded from: classes.dex */
    class Config extends Holder<Filter>.HolderConfig implements FilterConfig {
        Config() {
            super();
        }

        @Override // javax.servlet.FilterConfig
        public String getFilterName() {
            return FilterHolder.this._name;
        }
    }
}
