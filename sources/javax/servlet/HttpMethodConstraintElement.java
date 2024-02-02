package javax.servlet;
/* loaded from: classes.dex */
public class HttpMethodConstraintElement extends HttpConstraintElement {
    private String methodName;

    public HttpMethodConstraintElement(String methodName) {
        if (methodName == null || methodName.length() == 0) {
            throw new IllegalArgumentException("invalid HTTP method name");
        }
        this.methodName = methodName;
    }

    public HttpMethodConstraintElement(String methodName, HttpConstraintElement constraint) {
        super(constraint.getEmptyRoleSemantic(), constraint.getTransportGuarantee(), constraint.getRolesAllowed());
        if (methodName == null || methodName.length() == 0) {
            throw new IllegalArgumentException("invalid HTTP method name");
        }
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }
}
