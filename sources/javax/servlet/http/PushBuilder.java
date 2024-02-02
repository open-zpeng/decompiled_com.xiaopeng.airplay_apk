package javax.servlet.http;

import java.util.Set;
/* loaded from: classes.dex */
public interface PushBuilder {
    PushBuilder addHeader(String str, String str2);

    String getHeader(String str);

    Set<String> getHeaderNames();

    String getMethod();

    String getPath();

    String getQueryString();

    String getSessionId();

    PushBuilder method(String str);

    PushBuilder path(String str);

    void push();

    PushBuilder queryString(String str);

    PushBuilder removeHeader(String str);

    PushBuilder sessionId(String str);

    PushBuilder setHeader(String str, String str2);
}
