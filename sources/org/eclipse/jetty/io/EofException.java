package org.eclipse.jetty.io;

import java.io.EOFException;
/* loaded from: classes.dex */
public class EofException extends EOFException {
    public EofException() {
    }

    public EofException(String reason) {
        super(reason);
    }

    public EofException(Throwable th) {
        if (th != null) {
            initCause(th);
        }
    }
}
