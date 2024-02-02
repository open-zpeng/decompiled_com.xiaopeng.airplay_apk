package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ErrorHandler extends AbstractHandler {
    public static final String ERROR_PAGE = "org.eclipse.jetty.server.error_page";
    private static final Logger LOG = Log.getLogger(ErrorHandler.class);
    boolean _showStacks = true;
    boolean _showMessageInTitle = true;
    String _cacheControl = "must-revalidate,no-cache,no-store";

    /* loaded from: classes.dex */
    public interface ErrorPageMapper {
        String getErrorPage(HttpServletRequest httpServletRequest);
    }

    @Override // org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String error_page;
        String old_error_page;
        AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
        String method = request.getMethod();
        if (!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD)) {
            connection.getRequest().setHandled(true);
            return;
        }
        if ((this instanceof ErrorPageMapper) && (error_page = ((ErrorPageMapper) this).getErrorPage(request)) != null && request.getServletContext() != null && ((old_error_page = (String) request.getAttribute(ERROR_PAGE)) == null || !old_error_page.equals(error_page))) {
            request.setAttribute(ERROR_PAGE, error_page);
            Dispatcher dispatcher = (Dispatcher) request.getServletContext().getRequestDispatcher(error_page);
            try {
                if (dispatcher != null) {
                    dispatcher.error(request, response);
                    return;
                }
                Logger logger = LOG;
                logger.warn("No error page " + error_page, new Object[0]);
            } catch (ServletException e) {
                LOG.warn(Log.EXCEPTION, e);
                return;
            }
        }
        connection.getRequest().setHandled(true);
        response.setContentType(MimeTypes.TEXT_HTML_8859_1);
        if (this._cacheControl != null) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, this._cacheControl);
        }
        ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(4096);
        handleErrorPage(request, writer, connection.getResponse().getStatus(), connection.getResponse().getReason());
        writer.flush();
        response.setContentLength(writer.size());
        writer.writeTo(response.getOutputStream());
        writer.destroy();
    }

    protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        writeErrorPage(request, writer, code, message, this._showStacks);
    }

    protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        if (message == null) {
            message = HttpStatus.getMessage(code);
        }
        writer.write("<html>\n<head>\n");
        writeErrorPageHead(request, writer, code, message);
        writer.write("</head>\n<body>");
        writeErrorPageBody(request, writer, code, message, showStacks);
        writer.write("\n</body>\n</html>\n");
    }

    protected void writeErrorPageHead(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n");
        writer.write("<title>Error ");
        writer.write(Integer.toString(code));
        if (this._showMessageInTitle) {
            writer.write(32);
            write(writer, message);
        }
        writer.write("</title>\n");
    }

    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        String uri = request.getRequestURI();
        writeErrorPageMessage(request, writer, code, message, uri);
        if (showStacks) {
            writeErrorPageStacks(request, writer);
        }
        writer.write("<hr /><i><small>Powered by Jetty://</small></i>");
        for (int i = 0; i < 20; i++) {
            writer.write("<br/>                                                \n");
        }
    }

    protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message, String uri) throws IOException {
        writer.write("<h2>HTTP ERROR ");
        writer.write(Integer.toString(code));
        writer.write("</h2>\n<p>Problem accessing ");
        write(writer, uri);
        writer.write(". Reason:\n<pre>    ");
        write(writer, message);
        writer.write("</pre></p>");
    }

    protected void writeErrorPageStacks(HttpServletRequest request, Writer writer) throws IOException {
        for (Throwable th = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION); th != null; th = th.getCause()) {
            writer.write("<h3>Caused by:</h3><pre>");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            pw.flush();
            write(writer, sw.getBuffer().toString());
            writer.write("</pre>\n");
        }
    }

    public String getCacheControl() {
        return this._cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this._cacheControl = cacheControl;
    }

    public boolean isShowStacks() {
        return this._showStacks;
    }

    public void setShowStacks(boolean showStacks) {
        this._showStacks = showStacks;
    }

    public void setShowMessageInTitle(boolean showMessageInTitle) {
        this._showMessageInTitle = showMessageInTitle;
    }

    public boolean getShowMessageInTitle() {
        return this._showMessageInTitle;
    }

    protected void write(Writer writer, String string) throws IOException {
        if (string == null) {
            return;
        }
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '&') {
                writer.write("&amp;");
            } else if (c == '<') {
                writer.write("&lt;");
            } else if (c == '>') {
                writer.write("&gt;");
            } else if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
                writer.write(63);
            } else {
                writer.write(c);
            }
        }
    }
}
