package org.eclipse.jetty.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.HttpConstraintElement;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.StringMap;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.security.Constraint;
/* loaded from: classes.dex */
public class ConstraintSecurityHandler extends SecurityHandler implements ConstraintAware {
    private static final String OMISSION_SUFFIX = ".omission";
    private final List<ConstraintMapping> _constraintMappings = new CopyOnWriteArrayList();
    private final Set<String> _roles = new CopyOnWriteArraySet();
    private final PathMap _constraintMap = new PathMap();
    private boolean _strict = true;

    public static Constraint createConstraint() {
        return new Constraint();
    }

    public static Constraint createConstraint(Constraint constraint) {
        try {
            return (Constraint) constraint.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Constraint createConstraint(String name, boolean authenticate, String[] roles, int dataConstraint) {
        Constraint constraint = createConstraint();
        if (name != null) {
            constraint.setName(name);
        }
        constraint.setAuthenticate(authenticate);
        constraint.setRoles(roles);
        constraint.setDataConstraint(dataConstraint);
        return constraint;
    }

    public static Constraint createConstraint(String name, HttpConstraintElement element) {
        return createConstraint(name, element.getRolesAllowed(), element.getEmptyRoleSemantic(), element.getTransportGuarantee());
    }

    public static Constraint createConstraint(String name, String[] rolesAllowed, ServletSecurity.EmptyRoleSemantic permitOrDeny, ServletSecurity.TransportGuarantee transport) {
        Constraint constraint = createConstraint();
        if (rolesAllowed == null || rolesAllowed.length == 0) {
            if (permitOrDeny.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                constraint.setName(name + "-Deny");
                constraint.setAuthenticate(true);
            } else {
                constraint.setName(name + "-Permit");
                constraint.setAuthenticate(false);
            }
        } else {
            constraint.setAuthenticate(true);
            constraint.setRoles(rolesAllowed);
            constraint.setName(name + "-RolesAllowed");
        }
        constraint.setDataConstraint(transport.equals(ServletSecurity.TransportGuarantee.CONFIDENTIAL) ? 2 : 0);
        return constraint;
    }

    public static List<ConstraintMapping> getConstraintMappingsForPath(String pathSpec, List<ConstraintMapping> constraintMappings) {
        if (pathSpec == null || "".equals(pathSpec.trim()) || constraintMappings == null || constraintMappings.size() == 0) {
            return Collections.emptyList();
        }
        List<ConstraintMapping> mappings = new ArrayList<>();
        for (ConstraintMapping mapping : constraintMappings) {
            if (pathSpec.equals(mapping.getPathSpec())) {
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    public static List<ConstraintMapping> removeConstraintMappingsForPath(String pathSpec, List<ConstraintMapping> constraintMappings) {
        if (pathSpec == null || "".equals(pathSpec.trim()) || constraintMappings == null || constraintMappings.size() == 0) {
            return Collections.emptyList();
        }
        List<ConstraintMapping> mappings = new ArrayList<>();
        for (ConstraintMapping mapping : constraintMappings) {
            if (!pathSpec.equals(mapping.getPathSpec())) {
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    public static List<ConstraintMapping> createConstraintsWithMappingsForPath(String name, String pathSpec, ServletSecurityElement securityElement) {
        List<ConstraintMapping> mappings = new ArrayList<>();
        Constraint constraint = createConstraint(name, securityElement);
        ConstraintMapping defaultMapping = new ConstraintMapping();
        defaultMapping.setPathSpec(pathSpec);
        defaultMapping.setConstraint(constraint);
        mappings.add(defaultMapping);
        List<String> methodOmissions = new ArrayList<>();
        Collection<HttpMethodConstraintElement> methodConstraints = securityElement.getHttpMethodConstraints();
        if (methodConstraints != null) {
            for (HttpMethodConstraintElement methodConstraint : methodConstraints) {
                Constraint mconstraint = createConstraint(name, methodConstraint);
                ConstraintMapping mapping = new ConstraintMapping();
                mapping.setConstraint(mconstraint);
                mapping.setPathSpec(pathSpec);
                if (methodConstraint.getMethodName() != null) {
                    mapping.setMethod(methodConstraint.getMethodName());
                    methodOmissions.add(methodConstraint.getMethodName());
                }
                mappings.add(mapping);
            }
        }
        if (methodOmissions.size() > 0) {
            defaultMapping.setMethodOmissions((String[]) methodOmissions.toArray(new String[methodOmissions.size()]));
        }
        return mappings;
    }

    public boolean isStrict() {
        return this._strict;
    }

    public void setStrict(boolean strict) {
        this._strict = strict;
    }

    @Override // org.eclipse.jetty.security.ConstraintAware
    public List<ConstraintMapping> getConstraintMappings() {
        return this._constraintMappings;
    }

    @Override // org.eclipse.jetty.security.ConstraintAware
    public Set<String> getRoles() {
        return this._roles;
    }

    public void setConstraintMappings(List<ConstraintMapping> constraintMappings) {
        setConstraintMappings(constraintMappings, null);
    }

    public void setConstraintMappings(ConstraintMapping[] constraintMappings) {
        setConstraintMappings(Arrays.asList(constraintMappings), null);
    }

    @Override // org.eclipse.jetty.security.ConstraintAware
    public void setConstraintMappings(List<ConstraintMapping> constraintMappings, Set<String> roles) {
        this._constraintMappings.clear();
        this._constraintMappings.addAll(constraintMappings);
        if (roles == null) {
            roles = new HashSet();
            for (ConstraintMapping cm : constraintMappings) {
                String[] cmr = cm.getConstraint().getRoles();
                if (cmr != null) {
                    for (String r : cmr) {
                        if (!"*".equals(r)) {
                            roles.add(r);
                        }
                    }
                }
            }
        }
        setRoles(roles);
        if (isStarted()) {
            for (ConstraintMapping mapping : this._constraintMappings) {
                processConstraintMapping(mapping);
            }
        }
    }

    public void setRoles(Set<String> roles) {
        this._roles.clear();
        this._roles.addAll(roles);
    }

    @Override // org.eclipse.jetty.security.ConstraintAware
    public void addConstraintMapping(ConstraintMapping mapping) {
        this._constraintMappings.add(mapping);
        if (mapping.getConstraint() != null && mapping.getConstraint().getRoles() != null) {
            String[] arr$ = mapping.getConstraint().getRoles();
            for (String role : arr$) {
                addRole(role);
            }
        }
        if (isStarted()) {
            processConstraintMapping(mapping);
        }
    }

    @Override // org.eclipse.jetty.security.ConstraintAware
    public void addRole(String role) {
        boolean modified = this._roles.add(role);
        if (isStarted() && modified && this._strict) {
            for (Map<String, RoleInfo> map : this._constraintMap.values()) {
                for (RoleInfo info : map.values()) {
                    if (info.isAnyRole()) {
                        info.addRole(role);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.security.SecurityHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._constraintMap.clear();
        if (this._constraintMappings != null) {
            for (ConstraintMapping mapping : this._constraintMappings) {
                processConstraintMapping(mapping);
            }
        }
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.security.SecurityHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._constraintMap.clear();
        this._constraintMappings.clear();
        this._roles.clear();
        super.doStop();
    }

    protected void processConstraintMapping(ConstraintMapping mapping) {
        Map<String, RoleInfo> mappings = (Map) this._constraintMap.get(mapping.getPathSpec());
        if (mappings == null) {
            mappings = new StringMap();
            this._constraintMap.put(mapping.getPathSpec(), mappings);
        }
        RoleInfo allMethodsRoleInfo = mappings.get(null);
        if (allMethodsRoleInfo != null && allMethodsRoleInfo.isForbidden()) {
            return;
        }
        if (mapping.getMethodOmissions() != null && mapping.getMethodOmissions().length > 0) {
            processConstraintMappingWithMethodOmissions(mapping, mappings);
            return;
        }
        String httpMethod = mapping.getMethod();
        RoleInfo roleInfo = mappings.get(httpMethod);
        if (roleInfo == null) {
            roleInfo = new RoleInfo();
            mappings.put(httpMethod, roleInfo);
            if (allMethodsRoleInfo != null) {
                roleInfo.combine(allMethodsRoleInfo);
            }
        }
        if (roleInfo.isForbidden()) {
            return;
        }
        configureRoleInfo(roleInfo, mapping);
        if (roleInfo.isForbidden()) {
            if (httpMethod == null) {
                mappings.clear();
                mappings.put(null, roleInfo);
            }
        } else if (httpMethod == null) {
            for (Map.Entry<String, RoleInfo> entry : mappings.entrySet()) {
                if (entry.getKey() != null) {
                    RoleInfo specific = entry.getValue();
                    specific.combine(roleInfo);
                }
            }
        }
    }

    protected void processConstraintMappingWithMethodOmissions(ConstraintMapping mapping, Map<String, RoleInfo> mappings) {
        String[] omissions = mapping.getMethodOmissions();
        for (String omission : omissions) {
            RoleInfo ri = mappings.get(omission + OMISSION_SUFFIX);
            if (ri == null) {
                ri = new RoleInfo();
                mappings.put(omission + OMISSION_SUFFIX, ri);
            }
            configureRoleInfo(ri, mapping);
        }
    }

    protected void configureRoleInfo(RoleInfo ri, ConstraintMapping mapping) {
        Constraint constraint = mapping.getConstraint();
        boolean forbidden = constraint.isForbidden();
        ri.setForbidden(forbidden);
        UserDataConstraint userDataConstraint = UserDataConstraint.get(mapping.getConstraint().getDataConstraint());
        ri.setUserDataConstraint(userDataConstraint);
        if (!ri.isForbidden()) {
            boolean checked = mapping.getConstraint().getAuthenticate();
            ri.setChecked(checked);
            if (ri.isChecked()) {
                if (mapping.getConstraint().isAnyRole()) {
                    if (this._strict) {
                        for (String role : this._roles) {
                            ri.addRole(role);
                        }
                        return;
                    }
                    ri.setAnyRole(true);
                    return;
                }
                String[] newRoles = mapping.getConstraint().getRoles();
                for (String role2 : newRoles) {
                    if (this._strict && !this._roles.contains(role2)) {
                        throw new IllegalArgumentException("Attempt to use undeclared role: " + role2 + ", known roles: " + this._roles);
                    }
                    ri.addRole(role2);
                }
            }
        }
    }

    @Override // org.eclipse.jetty.security.SecurityHandler
    protected Object prepareConstraintInfo(String pathInContext, Request request) {
        Map<String, RoleInfo> mappings = (Map) this._constraintMap.match(pathInContext);
        if (mappings == null) {
            return null;
        }
        String httpMethod = request.getMethod();
        RoleInfo roleInfo = mappings.get(httpMethod);
        if (roleInfo == null) {
            List<RoleInfo> applicableConstraints = new ArrayList<>();
            RoleInfo all = mappings.get(null);
            if (all != null) {
                applicableConstraints.add(all);
            }
            for (Map.Entry<String, RoleInfo> entry : mappings.entrySet()) {
                if (entry.getKey() != null && entry.getKey().contains(OMISSION_SUFFIX)) {
                    if (!(httpMethod + OMISSION_SUFFIX).equals(entry.getKey())) {
                        applicableConstraints.add(entry.getValue());
                    }
                }
            }
            if (applicableConstraints.size() == 1) {
                return applicableConstraints.get(0);
            }
            RoleInfo roleInfo2 = new RoleInfo();
            roleInfo2.setUserDataConstraint(UserDataConstraint.None);
            for (RoleInfo r : applicableConstraints) {
                roleInfo2.combine(r);
            }
            return roleInfo2;
        }
        return roleInfo;
    }

    @Override // org.eclipse.jetty.security.SecurityHandler
    protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException {
        StringBuilder sb;
        StringBuilder sb2;
        if (constraintInfo == null) {
            return true;
        }
        RoleInfo roleInfo = (RoleInfo) constraintInfo;
        if (roleInfo.isForbidden()) {
            return false;
        }
        UserDataConstraint dataConstraint = roleInfo.getUserDataConstraint();
        if (dataConstraint == null || dataConstraint == UserDataConstraint.None) {
            return true;
        }
        AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
        Connector connector = connection.getConnector();
        if (dataConstraint == UserDataConstraint.Integral) {
            if (connector.isIntegral(request)) {
                return true;
            }
            if (connector.getIntegralPort() > 0) {
                String scheme = connector.getIntegralScheme();
                int port = connector.getIntegralPort();
                if ("https".equalsIgnoreCase(scheme) && port == 443) {
                    sb2 = new StringBuilder();
                    sb2.append("https://");
                    sb2.append(request.getServerName());
                } else {
                    sb2 = new StringBuilder();
                    sb2.append(scheme);
                    sb2.append("://");
                    sb2.append(request.getServerName());
                    sb2.append(":");
                    sb2.append(port);
                }
                sb2.append(request.getRequestURI());
                String url = sb2.toString();
                if (request.getQueryString() != null) {
                    url = url + "?" + request.getQueryString();
                }
                response.setContentLength(0);
                response.sendRedirect(url);
            } else {
                response.sendError(403, "!Integral");
            }
            request.setHandled(true);
            return false;
        } else if (dataConstraint == UserDataConstraint.Confidential) {
            if (connector.isConfidential(request)) {
                return true;
            }
            if (connector.getConfidentialPort() > 0) {
                String scheme2 = connector.getConfidentialScheme();
                int port2 = connector.getConfidentialPort();
                if ("https".equalsIgnoreCase(scheme2) && port2 == 443) {
                    sb = new StringBuilder();
                    sb.append("https://");
                    sb.append(request.getServerName());
                } else {
                    sb = new StringBuilder();
                    sb.append(scheme2);
                    sb.append("://");
                    sb.append(request.getServerName());
                    sb.append(":");
                    sb.append(port2);
                }
                sb.append(request.getRequestURI());
                String url2 = sb.toString();
                if (request.getQueryString() != null) {
                    url2 = url2 + "?" + request.getQueryString();
                }
                response.setContentLength(0);
                response.sendRedirect(url2);
            } else {
                response.sendError(403, "!Confidential");
            }
            request.setHandled(true);
            return false;
        } else {
            throw new IllegalArgumentException("Invalid dataConstraint value: " + dataConstraint);
        }
    }

    @Override // org.eclipse.jetty.security.SecurityHandler
    protected boolean isAuthMandatory(Request baseRequest, Response base_response, Object constraintInfo) {
        if (constraintInfo == null) {
            return false;
        }
        return ((RoleInfo) constraintInfo).isChecked();
    }

    @Override // org.eclipse.jetty.security.SecurityHandler
    protected boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException {
        if (constraintInfo == null) {
            return true;
        }
        RoleInfo roleInfo = (RoleInfo) constraintInfo;
        if (!roleInfo.isChecked()) {
            return true;
        }
        if (roleInfo.isAnyRole() && request.getAuthType() != null) {
            return true;
        }
        for (String role : roleInfo.getRoles()) {
            if (userIdentity.isUserInRole(role, null)) {
                return true;
            }
        }
        return false;
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        dump(out, indent, Collections.singleton(getLoginService()), Collections.singleton(getIdentityService()), Collections.singleton(getAuthenticator()), Collections.singleton(this._roles), this._constraintMap.entrySet(), getBeans(), TypeUtil.asList(getHandlers()));
    }
}
