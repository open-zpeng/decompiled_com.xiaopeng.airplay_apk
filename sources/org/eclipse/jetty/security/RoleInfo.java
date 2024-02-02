package org.eclipse.jetty.security;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
/* loaded from: classes.dex */
public class RoleInfo {
    private boolean _checked;
    private boolean _forbidden;
    private boolean _isAnyRole;
    private final Set<String> _roles = new CopyOnWriteArraySet();
    private UserDataConstraint _userDataConstraint;

    public boolean isChecked() {
        return this._checked;
    }

    public void setChecked(boolean checked) {
        this._checked = checked;
        if (!checked) {
            this._forbidden = false;
            this._roles.clear();
            this._isAnyRole = false;
        }
    }

    public boolean isForbidden() {
        return this._forbidden;
    }

    public void setForbidden(boolean forbidden) {
        this._forbidden = forbidden;
        if (forbidden) {
            this._checked = true;
            this._userDataConstraint = null;
            this._isAnyRole = false;
            this._roles.clear();
        }
    }

    public boolean isAnyRole() {
        return this._isAnyRole;
    }

    public void setAnyRole(boolean anyRole) {
        this._isAnyRole = anyRole;
        if (anyRole) {
            this._checked = true;
            this._roles.clear();
        }
    }

    public UserDataConstraint getUserDataConstraint() {
        return this._userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraint userDataConstraint) {
        if (userDataConstraint == null) {
            throw new NullPointerException("Null UserDataConstraint");
        }
        if (this._userDataConstraint == null) {
            this._userDataConstraint = userDataConstraint;
        } else {
            this._userDataConstraint = this._userDataConstraint.combine(userDataConstraint);
        }
    }

    public Set<String> getRoles() {
        return this._roles;
    }

    public void addRole(String role) {
        this._roles.add(role);
    }

    public void combine(RoleInfo other) {
        if (other._forbidden) {
            setForbidden(true);
        } else if (!other._checked) {
            setChecked(true);
        } else if (other._isAnyRole) {
            setAnyRole(true);
        } else if (!this._isAnyRole) {
            for (String r : other._roles) {
                this._roles.add(r);
            }
        }
        setUserDataConstraint(other._userDataConstraint);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{RoleInfo");
        sb.append(this._forbidden ? ",F" : "");
        sb.append(this._checked ? ",C" : "");
        sb.append(this._isAnyRole ? ",*" : this._roles);
        sb.append("}");
        return sb.toString();
    }
}
