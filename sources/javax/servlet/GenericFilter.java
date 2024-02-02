package javax.servlet;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.ResourceBundle;
/* loaded from: classes.dex */
public abstract class GenericFilter implements Filter, FilterConfig, Serializable {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);
    private transient FilterConfig config;

    @Override // javax.servlet.FilterConfig
    public String getInitParameter(String name) {
        FilterConfig fc = getFilterConfig();
        if (fc == null) {
            throw new IllegalStateException(lStrings.getString("err.filter_config_not_initialized"));
        }
        return fc.getInitParameter(name);
    }

    @Override // javax.servlet.FilterConfig
    public Enumeration<String> getInitParameterNames() {
        FilterConfig fc = getFilterConfig();
        if (fc == null) {
            throw new IllegalStateException(lStrings.getString("err.filter_config_not_initialized"));
        }
        return fc.getInitParameterNames();
    }

    public FilterConfig getFilterConfig() {
        return this.config;
    }

    @Override // javax.servlet.FilterConfig
    public ServletContext getServletContext() {
        FilterConfig sc = getFilterConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.filter_config_not_initialized"));
        }
        return sc.getServletContext();
    }

    @Override // javax.servlet.Filter
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        init();
    }

    public void init() throws ServletException {
    }

    @Override // javax.servlet.FilterConfig
    public String getFilterName() {
        FilterConfig sc = getFilterConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
        }
        return sc.getFilterName();
    }
}
