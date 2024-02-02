package javax.servlet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
/* loaded from: classes.dex */
public class ServletSecurityElement extends HttpConstraintElement {
    private Collection<HttpMethodConstraintElement> methodConstraints;
    private Collection<String> methodNames;

    public ServletSecurityElement() {
        this.methodConstraints = new HashSet();
        this.methodNames = Collections.emptySet();
    }

    public ServletSecurityElement(HttpConstraintElement constraint) {
        super(constraint.getEmptyRoleSemantic(), constraint.getTransportGuarantee(), constraint.getRolesAllowed());
        this.methodConstraints = new HashSet();
        this.methodNames = Collections.emptySet();
    }

    public ServletSecurityElement(Collection<HttpMethodConstraintElement> methodConstraints) {
        this.methodConstraints = methodConstraints == null ? new HashSet<>() : methodConstraints;
        this.methodNames = checkMethodNames(this.methodConstraints);
    }

    public ServletSecurityElement(HttpConstraintElement constraint, Collection<HttpMethodConstraintElement> methodConstraints) {
        super(constraint.getEmptyRoleSemantic(), constraint.getTransportGuarantee(), constraint.getRolesAllowed());
        this.methodConstraints = methodConstraints == null ? new HashSet<>() : methodConstraints;
        this.methodNames = checkMethodNames(this.methodConstraints);
    }

    public ServletSecurityElement(ServletSecurity annotation) {
        super(annotation.value().value(), annotation.value().transportGuarantee(), annotation.value().rolesAllowed());
        this.methodConstraints = new HashSet();
        HttpMethodConstraint[] arr$ = annotation.httpMethodConstraints();
        for (HttpMethodConstraint constraint : arr$) {
            this.methodConstraints.add(new HttpMethodConstraintElement(constraint.value(), new HttpConstraintElement(constraint.emptyRoleSemantic(), constraint.transportGuarantee(), constraint.rolesAllowed())));
        }
        this.methodNames = checkMethodNames(this.methodConstraints);
    }

    public Collection<HttpMethodConstraintElement> getHttpMethodConstraints() {
        return Collections.unmodifiableCollection(this.methodConstraints);
    }

    public Collection<String> getMethodNames() {
        return Collections.unmodifiableCollection(this.methodNames);
    }

    private Collection<String> checkMethodNames(Collection<HttpMethodConstraintElement> methodConstraints) {
        Collection<String> methodNames = new HashSet<>();
        for (HttpMethodConstraintElement methodConstraint : methodConstraints) {
            String methodName = methodConstraint.getMethodName();
            if (!methodNames.add(methodName)) {
                throw new IllegalArgumentException("Duplicate HTTP method name: " + methodName);
            }
        }
        return methodNames;
    }
}
