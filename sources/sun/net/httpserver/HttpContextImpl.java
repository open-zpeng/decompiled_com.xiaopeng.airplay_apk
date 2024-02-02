package sun.net.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class HttpContextImpl extends HttpContext {
    private Authenticator authenticator;
    private AuthFilter authfilter;
    private HttpHandler handler;
    private String path;
    private String protocol;
    private ServerImpl server;
    private Map<String, Object> attributes = new HashMap();
    private LinkedList<Filter> sfilters = new LinkedList<>();
    private LinkedList<Filter> ufilters = new LinkedList<>();

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpContextImpl(String str, String str2, HttpHandler httpHandler, ServerImpl serverImpl) {
        if (str2 == null || str == null || str2.length() < 1 || str2.charAt(0) != '/') {
            throw new IllegalArgumentException("Illegal value for path or protocol");
        }
        this.protocol = str.toLowerCase();
        this.path = str2;
        if (!this.protocol.equals("http") && !this.protocol.equals("https")) {
            throw new IllegalArgumentException("Illegal value for protocol");
        }
        this.handler = httpHandler;
        this.server = serverImpl;
        this.authfilter = new AuthFilter(null);
        this.sfilters.add(this.authfilter);
    }

    @Override // com.sun.net.httpserver.HttpContext
    public HttpHandler getHandler() {
        return this.handler;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public void setHandler(HttpHandler httpHandler) {
        if (httpHandler == null) {
            throw new NullPointerException("Null handler parameter");
        }
        if (this.handler != null) {
            throw new IllegalArgumentException("handler already set");
        }
        this.handler = httpHandler;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public String getPath() {
        return this.path;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public HttpServer getServer() {
        return this.server.getWrapper();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ServerImpl getServerImpl() {
        return this.server;
    }

    public String getProtocol() {
        return this.protocol;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public List<Filter> getFilters() {
        return this.ufilters;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<Filter> getSystemFilters() {
        return this.sfilters;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public Authenticator setAuthenticator(Authenticator authenticator) {
        Authenticator authenticator2 = this.authenticator;
        this.authenticator = authenticator;
        this.authfilter.setAuthenticator(authenticator);
        return authenticator2;
    }

    @Override // com.sun.net.httpserver.HttpContext
    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Logger getLogger() {
        return this.server.getLogger();
    }
}
