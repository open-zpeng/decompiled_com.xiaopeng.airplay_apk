package org.eclipse.jetty.servlet;

import java.io.IOException;
import java.util.Arrays;
/* loaded from: classes.dex */
public class ServletMapping {
    private boolean _default;
    private String[] _pathSpecs;
    private String _servletName;

    public String[] getPathSpecs() {
        return this._pathSpecs;
    }

    public String getServletName() {
        return this._servletName;
    }

    public void setPathSpecs(String[] pathSpecs) {
        this._pathSpecs = pathSpecs;
    }

    public void setPathSpec(String pathSpec) {
        this._pathSpecs = new String[]{pathSpec};
    }

    public void setServletName(String servletName) {
        this._servletName = servletName;
    }

    public boolean isDefault() {
        return this._default;
    }

    public void setDefault(boolean fromDefault) {
        this._default = fromDefault;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._pathSpecs == null ? "[]" : Arrays.asList(this._pathSpecs).toString());
        sb.append("=>");
        sb.append(this._servletName);
        return sb.toString();
    }

    public void dump(Appendable out, String indent) throws IOException {
        out.append(String.valueOf(this)).append("\n");
    }
}
