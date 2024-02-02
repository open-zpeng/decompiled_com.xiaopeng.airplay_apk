package javax.enterprise.context;
/* loaded from: classes.dex */
public class ContextException extends RuntimeException {
    private static final long serialVersionUID = -3599813072560026919L;

    public ContextException() {
    }

    public ContextException(String message) {
        super(message);
    }

    public ContextException(Throwable cause) {
        super(cause);
    }

    public ContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
