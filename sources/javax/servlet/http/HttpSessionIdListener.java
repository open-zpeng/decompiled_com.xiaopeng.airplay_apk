package javax.servlet.http;

import java.util.EventListener;
/* loaded from: classes.dex */
public interface HttpSessionIdListener extends EventListener {
    void sessionIdChanged(HttpSessionEvent httpSessionEvent, String str);
}
