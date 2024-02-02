package javax.enterprise.inject;
/* loaded from: classes.dex */
public class UnproxyableResolutionException extends ResolutionException {
    private static final long serialVersionUID = 1667539354548135465L;

    public UnproxyableResolutionException() {
    }

    public UnproxyableResolutionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UnproxyableResolutionException(String message) {
        super(message);
    }

    public UnproxyableResolutionException(Throwable throwable) {
        super(throwable);
    }
}
