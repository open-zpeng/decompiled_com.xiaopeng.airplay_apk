package org.eclipse.jetty.security;

import org.eclipse.jetty.util.security.Constraint;
/* loaded from: classes.dex */
public class ConstraintMapping {
    Constraint _constraint;
    String _method;
    String[] _methodOmissions;
    String _pathSpec;

    public Constraint getConstraint() {
        return this._constraint;
    }

    public void setConstraint(Constraint constraint) {
        this._constraint = constraint;
    }

    public String getMethod() {
        return this._method;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public String getPathSpec() {
        return this._pathSpec;
    }

    public void setPathSpec(String pathSpec) {
        this._pathSpec = pathSpec;
    }

    public void setMethodOmissions(String[] omissions) {
        this._methodOmissions = omissions;
    }

    public String[] getMethodOmissions() {
        return this._methodOmissions;
    }
}
