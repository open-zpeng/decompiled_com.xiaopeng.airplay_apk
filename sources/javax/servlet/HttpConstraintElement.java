package javax.servlet;

import javax.servlet.annotation.ServletSecurity;
/* loaded from: classes.dex */
public class HttpConstraintElement {
    private ServletSecurity.EmptyRoleSemantic emptyRoleSemantic;
    private String[] rolesAllowed;
    private ServletSecurity.TransportGuarantee transportGuarantee;

    public HttpConstraintElement() {
        this(ServletSecurity.EmptyRoleSemantic.PERMIT);
    }

    public HttpConstraintElement(ServletSecurity.EmptyRoleSemantic semantic) {
        this(semantic, ServletSecurity.TransportGuarantee.NONE, new String[0]);
    }

    public HttpConstraintElement(ServletSecurity.TransportGuarantee guarantee, String... roleNames) {
        this(ServletSecurity.EmptyRoleSemantic.PERMIT, guarantee, roleNames);
    }

    public HttpConstraintElement(ServletSecurity.EmptyRoleSemantic semantic, ServletSecurity.TransportGuarantee guarantee, String... roleNames) {
        if (semantic == ServletSecurity.EmptyRoleSemantic.DENY && roleNames.length > 0) {
            throw new IllegalArgumentException("Deny semantic with rolesAllowed");
        }
        this.emptyRoleSemantic = semantic;
        this.transportGuarantee = guarantee;
        this.rolesAllowed = roleNames;
    }

    public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }

    public ServletSecurity.TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }

    public String[] getRolesAllowed() {
        return this.rolesAllowed;
    }
}
