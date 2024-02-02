package org.eclipse.jetty.server.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
/* loaded from: classes.dex */
public class HandlerList extends HandlerCollection {
    @Override // org.eclipse.jetty.server.handler.HandlerCollection, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Handler[] handlers = getHandlers();
        if (handlers != null && isStarted()) {
            for (Handler handler : handlers) {
                handler.handle(target, baseRequest, request, response);
                if (baseRequest.isHandled()) {
                    return;
                }
            }
        }
    }
}
