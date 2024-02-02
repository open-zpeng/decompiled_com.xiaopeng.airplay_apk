package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class DefaultHandler extends AbstractHandler {
    private static final Logger LOG = Log.getLogger(DefaultHandler.class);
    byte[] _favicon;
    final long _faviconModified = (System.currentTimeMillis() / 1000) * 1000;
    boolean _serveIcon = true;
    boolean _showContexts = true;

    public DefaultHandler() {
        try {
            URL fav = getClass().getClassLoader().getResource("org/eclipse/jetty/favicon.ico");
            if (fav != null) {
                Resource r = Resource.newResource(fav);
                this._favicon = IO.readBytes(r.getInputStream());
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    @Override // org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (response.isCommitted() || baseRequest.isHandled()) {
            return;
        }
        baseRequest.setHandled(true);
        String method = request.getMethod();
        if (this._serveIcon && this._favicon != null && method.equals(HttpMethods.GET) && request.getRequestURI().equals("/favicon.ico")) {
            if (request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) == this._faviconModified) {
                response.setStatus(304);
                return;
            }
            response.setStatus(200);
            response.setContentType("image/x-icon");
            response.setContentLength(this._favicon.length);
            response.setDateHeader(HttpHeaders.LAST_MODIFIED, this._faviconModified);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=360000,public");
            response.getOutputStream().write(this._favicon);
        } else if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/")) {
            response.sendError(404);
        } else {
            response.setStatus(404);
            response.setContentType(MimeTypes.TEXT_HTML);
            ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
            writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
            writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
            writer.write("No context on this server matched or handled this request.<BR>");
            int i = 0;
            if (this._showContexts) {
                writer.write("Contexts known to this server are: <ul>");
                Server server = getServer();
                Handler[] handlers = server == null ? null : server.getChildHandlersByClass(ContextHandler.class);
                for (int i2 = 0; handlers != null && i2 < handlers.length; i2++) {
                    ContextHandler context = (ContextHandler) handlers[i2];
                    if (context.isRunning()) {
                        writer.write("<li><a href=\"");
                        if (context.getVirtualHosts() != null && context.getVirtualHosts().length > 0) {
                            writer.write("http://" + context.getVirtualHosts()[0] + ":" + request.getLocalPort());
                        }
                        writer.write(context.getContextPath());
                        if (context.getContextPath().length() > 1 && context.getContextPath().endsWith("/")) {
                            writer.write("/");
                        }
                        writer.write("\">");
                        writer.write(context.getContextPath());
                        if (context.getVirtualHosts() != null && context.getVirtualHosts().length > 0) {
                            writer.write("&nbsp;@&nbsp;" + context.getVirtualHosts()[0] + ":" + request.getLocalPort());
                        }
                        writer.write("&nbsp;--->&nbsp;");
                        writer.write(context.toString());
                        writer.write("</a></li>\n");
                    } else {
                        writer.write("<li>");
                        writer.write(context.getContextPath());
                        if (context.getVirtualHosts() != null && context.getVirtualHosts().length > 0) {
                            writer.write("&nbsp;@&nbsp;" + context.getVirtualHosts()[0] + ":" + request.getLocalPort());
                        }
                        writer.write("&nbsp;--->&nbsp;");
                        writer.write(context.toString());
                        if (context.isFailed()) {
                            writer.write(" [failed]");
                        }
                        if (context.isStopped()) {
                            writer.write(" [stopped]");
                        }
                        writer.write("</li>\n");
                    }
                }
            }
            while (true) {
                int i3 = i;
                if (i3 < 10) {
                    writer.write("\n<!-- Padding for IE                  -->");
                    i = i3 + 1;
                } else {
                    writer.write("\n</BODY>\n</HTML>\n");
                    writer.flush();
                    response.setContentLength(writer.size());
                    OutputStream out = response.getOutputStream();
                    writer.writeTo(out);
                    out.close();
                    return;
                }
            }
        }
    }

    public boolean getServeIcon() {
        return this._serveIcon;
    }

    public void setServeIcon(boolean serveIcon) {
        this._serveIcon = serveIcon;
    }

    public boolean getShowContexts() {
        return this._showContexts;
    }

    public void setShowContexts(boolean show) {
        this._showContexts = show;
    }
}
