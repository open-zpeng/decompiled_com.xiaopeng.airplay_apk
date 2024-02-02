package javax.servlet;

import java.io.IOException;
import java.util.EventListener;
/* loaded from: classes.dex */
public interface ReadListener extends EventListener {
    void onAllDataRead() throws IOException;

    void onDataAvailable() throws IOException;

    void onError(Throwable th);
}
