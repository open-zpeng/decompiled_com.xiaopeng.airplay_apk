package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ShutdownHandler extends AbstractHandler {
    private static final Logger LOG = Log.getLogger(ShutdownHandler.class);
    private boolean _exitJvm = false;
    private final Server _server;
    private final String _shutdownToken;

    public ShutdownHandler(Server server, String shutdownToken) {
        this._server = server;
        this._shutdownToken = shutdownToken;
    }

    /* JADX WARN: Type inference failed for: r0v7, types: [org.eclipse.jetty.server.handler.ShutdownHandler$1] */
    @Override // org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!target.equals("/shutdown")) {
            return;
        }
        if (!request.getMethod().equals(HttpMethods.POST)) {
            response.sendError(400);
        } else if (!hasCorrectSecurityToken(request)) {
            Logger logger = LOG;
            logger.warn("Unauthorized shutdown attempt from " + getRemoteAddr(request), new Object[0]);
            response.sendError(401);
        } else if (!requestFromLocalhost(request)) {
            Logger logger2 = LOG;
            logger2.warn("Unauthorized shutdown attempt from " + getRemoteAddr(request), new Object[0]);
            response.sendError(401);
        } else {
            Logger logger3 = LOG;
            logger3.info("Shutting down by request from " + getRemoteAddr(request), new Object[0]);
            new Thread() { // from class: org.eclipse.jetty.server.handler.ShutdownHandler.1
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        ShutdownHandler.this.shutdownServer();
                    } catch (InterruptedException e) {
                        ShutdownHandler.LOG.ignore(e);
                    } catch (Exception e2) {
                        throw new RuntimeException("Shutting down server", e2);
                    }
                }
            }.start();
        }
    }

    private boolean requestFromLocalhost(HttpServletRequest request) {
        return "127.0.0.1".equals(getRemoteAddr(request));
    }

    protected String getRemoteAddr(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private boolean hasCorrectSecurityToken(HttpServletRequest request) {
        return this._shutdownToken.equals(request.getParameter("token"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void shutdownServer() throws Exception {
        this._server.stop();
        if (this._exitJvm) {
            System.exit(0);
        }
    }

    public void setExitJvm(boolean exitJvm) {
        this._exitJvm = exitJvm;
    }
}
