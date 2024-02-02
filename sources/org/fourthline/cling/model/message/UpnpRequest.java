package org.fourthline.cling.model.message;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.eclipse.jetty.http.HttpMethods;
/* loaded from: classes.dex */
public class UpnpRequest extends UpnpOperation {
    private Method method;
    private URI uri;

    /* loaded from: classes.dex */
    public enum Method {
        GET(HttpMethods.GET),
        POST(HttpMethods.POST),
        NOTIFY("NOTIFY"),
        MSEARCH("M-SEARCH"),
        SUBSCRIBE("SUBSCRIBE"),
        UNSUBSCRIBE("UNSUBSCRIBE"),
        UNKNOWN("UNKNOWN");
        
        private static Map<String, Method> byName = new HashMap<String, Method>() { // from class: org.fourthline.cling.model.message.UpnpRequest.Method.1
            {
                Method[] values;
                for (Method m : Method.values()) {
                    put(m.getHttpName(), m);
                }
            }
        };
        private String httpName;

        Method(String httpName) {
            this.httpName = httpName;
        }

        public String getHttpName() {
            return this.httpName;
        }

        public static Method getByHttpName(String httpName) {
            Method m;
            if (httpName != null && (m = byName.get(httpName.toUpperCase(Locale.ROOT))) != null) {
                return m;
            }
            return UNKNOWN;
        }
    }

    public UpnpRequest(Method method) {
        this.method = method;
    }

    public UpnpRequest(Method method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    public UpnpRequest(Method method, URL url) {
        this.method = method;
        if (url != null) {
            try {
                this.uri = url.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHttpMethodName() {
        return this.method.getHttpName();
    }

    public URI getURI() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(getHttpMethodName());
        if (getURI() != null) {
            str = " " + getURI();
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }
}
