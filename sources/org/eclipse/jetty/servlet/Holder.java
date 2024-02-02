package org.eclipse.jetty.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Registration;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Holder<T> extends AbstractLifeCycle implements Dumpable {
    private static final Logger LOG = Log.getLogger(Holder.class);
    protected boolean _asyncSupported;
    protected transient Class<? extends T> _class;
    protected String _className;
    protected String _displayName;
    protected boolean _extInstance;
    protected final Map<String, String> _initParams = new HashMap(3);
    protected String _name;
    protected ServletHandler _servletHandler;
    private final Source _source;

    /* loaded from: classes.dex */
    public enum Source {
        EMBEDDED,
        JAVAX_API,
        DESCRIPTOR,
        ANNOTATION
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Holder(Source source) {
        this._source = source;
        switch (this._source) {
            case JAVAX_API:
            case DESCRIPTOR:
            case ANNOTATION:
                this._asyncSupported = false;
                return;
            default:
                this._asyncSupported = true;
                return;
        }
    }

    public Source getSource() {
        return this._source;
    }

    public boolean isInstance() {
        return this._extInstance;
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        if (this._class == null && (this._className == null || this._className.equals(""))) {
            throw new UnavailableException("No class for Servlet or Filter for " + this._name);
        } else if (this._class == null) {
            try {
                this._class = Loader.loadClass(Holder.class, this._className);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Holding {}", this._class);
                }
            } catch (Exception e) {
                LOG.warn(e);
                throw new UnavailableException(e.getMessage());
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        if (!this._extInstance) {
            this._class = null;
        }
    }

    public String getClassName() {
        return this._className;
    }

    public Class<? extends T> getHeldClass() {
        return this._class;
    }

    public String getDisplayName() {
        return this._displayName;
    }

    public String getInitParameter(String param) {
        if (this._initParams == null) {
            return null;
        }
        return this._initParams.get(param);
    }

    public Enumeration getInitParameterNames() {
        if (this._initParams == null) {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
        return Collections.enumeration(this._initParams.keySet());
    }

    public Map<String, String> getInitParameters() {
        return this._initParams;
    }

    public String getName() {
        return this._name;
    }

    public ServletHandler getServletHandler() {
        return this._servletHandler;
    }

    public void destroyInstance(Object instance) throws Exception {
    }

    public void setClassName(String className) {
        this._className = className;
        this._class = null;
        if (this._name == null) {
            this._name = className + "-" + Integer.toHexString(hashCode());
        }
    }

    public void setHeldClass(Class<? extends T> held) {
        this._class = held;
        if (held != null) {
            this._className = held.getName();
            if (this._name == null) {
                this._name = held.getName() + "-" + Integer.toHexString(hashCode());
            }
        }
    }

    public void setDisplayName(String name) {
        this._displayName = name;
    }

    public void setInitParameter(String param, String value) {
        this._initParams.put(param, value);
    }

    public void setInitParameters(Map<String, String> map) {
        this._initParams.clear();
        this._initParams.putAll(map);
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setServletHandler(ServletHandler servletHandler) {
        this._servletHandler = servletHandler;
    }

    public void setAsyncSupported(boolean suspendable) {
        this._asyncSupported = suspendable;
    }

    public boolean isAsyncSupported() {
        return this._asyncSupported;
    }

    public String toString() {
        return this._name;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void illegalStateIfContextStarted() {
        ContextHandler.Context context;
        if (this._servletHandler != null && (context = (ContextHandler.Context) this._servletHandler.getServletContext()) != null && context.getContextHandler().isStarted()) {
            throw new IllegalStateException("Started");
        }
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        out.append(this._name).append("==").append(this._className).append(" - ").append(AbstractLifeCycle.getState(this)).append("\n");
        AggregateLifeCycle.dump(out, indent, this._initParams.entrySet());
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        return AggregateLifeCycle.dump(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class HolderConfig {
        /* JADX INFO: Access modifiers changed from: protected */
        public HolderConfig() {
        }

        public ServletContext getServletContext() {
            return Holder.this._servletHandler.getServletContext();
        }

        public String getInitParameter(String param) {
            return Holder.this.getInitParameter(param);
        }

        public Enumeration getInitParameterNames() {
            return Holder.this.getInitParameterNames();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class HolderRegistration implements Registration.Dynamic {
        /* JADX INFO: Access modifiers changed from: protected */
        public HolderRegistration() {
        }

        @Override // javax.servlet.Registration.Dynamic
        public void setAsyncSupported(boolean isAsyncSupported) {
            Holder.this.illegalStateIfContextStarted();
            Holder.this.setAsyncSupported(isAsyncSupported);
        }

        public void setDescription(String description) {
            if (Holder.LOG.isDebugEnabled()) {
                Logger logger = Holder.LOG;
                logger.debug(this + " is " + description, new Object[0]);
            }
        }

        @Override // javax.servlet.Registration
        public String getClassName() {
            return Holder.this.getClassName();
        }

        @Override // javax.servlet.Registration
        public String getInitParameter(String name) {
            return Holder.this.getInitParameter(name);
        }

        @Override // javax.servlet.Registration
        public Map<String, String> getInitParameters() {
            return Holder.this.getInitParameters();
        }

        @Override // javax.servlet.Registration
        public String getName() {
            return Holder.this.getName();
        }

        @Override // javax.servlet.Registration
        public boolean setInitParameter(String name, String value) {
            Holder.this.illegalStateIfContextStarted();
            if (name == null) {
                throw new IllegalArgumentException("init parameter name required");
            }
            if (value == null) {
                throw new IllegalArgumentException("non-null value required for init parameter " + name);
            } else if (Holder.this.getInitParameter(name) != null) {
                return false;
            } else {
                Holder.this.setInitParameter(name, value);
                return true;
            }
        }

        @Override // javax.servlet.Registration
        public Set<String> setInitParameters(Map<String, String> initParameters) {
            Holder.this.illegalStateIfContextStarted();
            Set<String> clash = null;
            for (Map.Entry<String, String> entry : initParameters.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("init parameter name required");
                }
                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("non-null value required for init parameter " + entry.getKey());
                } else if (Holder.this.getInitParameter(entry.getKey()) != null) {
                    if (clash == null) {
                        clash = new HashSet<>();
                    }
                    clash.add(entry.getKey());
                }
            }
            if (clash != null) {
                return clash;
            }
            Holder.this.getInitParameters().putAll(initParameters);
            return Collections.emptySet();
        }
    }
}
