package org.seamless.util;
/* loaded from: classes.dex */
public class Exceptions {
    public static Throwable unwrap(Throwable current) throws IllegalArgumentException {
        if (current == null) {
            throw new IllegalArgumentException("Cannot unwrap null throwable");
        }
        Throwable throwable = current;
        while (current != null) {
            throwable = current;
            current = current.getCause();
        }
        return throwable;
    }
}
