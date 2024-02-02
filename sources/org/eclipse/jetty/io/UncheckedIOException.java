package org.eclipse.jetty.io;
/* loaded from: classes.dex */
public class UncheckedIOException extends RuntimeException {
    public UncheckedIOException() {
    }

    public UncheckedIOException(String message) {
        super(message);
    }

    public UncheckedIOException(Throwable cause) {
        super(cause);
    }

    public UncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
