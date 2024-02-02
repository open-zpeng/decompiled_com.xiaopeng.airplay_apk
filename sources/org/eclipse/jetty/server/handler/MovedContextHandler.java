package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.URIUtil;
/* loaded from: classes.dex */
public class MovedContextHandler extends ContextHandler {
    boolean _discardPathInfo;
    boolean _discardQuery;
    String _expires;
    String _newContextURL;
    boolean _permanent;
    final Redirector _redirector;

    public MovedContextHandler() {
        this._redirector = new Redirector();
        setHandler(this._redirector);
        setAllowNullPathInfo(true);
    }

    public MovedContextHandler(HandlerContainer parent, String contextPath, String newContextURL) {
        super(parent, contextPath);
        this._newContextURL = newContextURL;
        this._redirector = new Redirector();
        setHandler(this._redirector);
    }

    public boolean isDiscardPathInfo() {
        return this._discardPathInfo;
    }

    public void setDiscardPathInfo(boolean discardPathInfo) {
        this._discardPathInfo = discardPathInfo;
    }

    public String getNewContextURL() {
        return this._newContextURL;
    }

    public void setNewContextURL(String newContextURL) {
        this._newContextURL = newContextURL;
    }

    public boolean isPermanent() {
        return this._permanent;
    }

    public void setPermanent(boolean permanent) {
        this._permanent = permanent;
    }

    public boolean isDiscardQuery() {
        return this._discardQuery;
    }

    public void setDiscardQuery(boolean discardQuery) {
        this._discardQuery = discardQuery;
    }

    /* loaded from: classes.dex */
    private class Redirector extends AbstractHandler {
        private Redirector() {
        }

        @Override // org.eclipse.jetty.server.Handler
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if (MovedContextHandler.this._newContextURL == null) {
                return;
            }
            String path = MovedContextHandler.this._newContextURL;
            if (!MovedContextHandler.this._discardPathInfo && request.getPathInfo() != null) {
                path = URIUtil.addPaths(path, request.getPathInfo());
            }
            StringBuilder location = URIUtil.hasScheme(path) ? new StringBuilder() : baseRequest.getRootURL();
            location.append(path);
            if (!MovedContextHandler.this._discardQuery && request.getQueryString() != null) {
                location.append('?');
                String q = request.getQueryString();
                location.append(q.replaceAll("\r\n?&=", "!"));
            }
            response.setHeader(HttpHeaders.LOCATION, location.toString());
            if (MovedContextHandler.this._expires != null) {
                response.setHeader(HttpHeaders.EXPIRES, MovedContextHandler.this._expires);
            }
            response.setStatus(MovedContextHandler.this._permanent ? 301 : 302);
            response.setContentLength(0);
            baseRequest.setHandled(true);
        }
    }

    public String getExpires() {
        return this._expires;
    }

    public void setExpires(String expires) {
        this._expires = expires;
    }
}
