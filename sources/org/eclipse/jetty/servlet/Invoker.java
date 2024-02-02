package org.eclipse.jetty.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Invoker extends HttpServlet {
    private static final Logger LOG = Log.getLogger(Invoker.class);
    private ContextHandler _contextHandler;
    private Map.Entry _invokerEntry;
    private boolean _nonContextServlets;
    private Map _parameters;
    private ServletHandler _servletHandler;
    private boolean _verbose;

    @Override // javax.servlet.GenericServlet
    public void init() {
        ServletContext config = getServletContext();
        this._contextHandler = ((ContextHandler.Context) config).getContextHandler();
        Handler handler = this._contextHandler.getHandler();
        while (handler != null && !(handler instanceof ServletHandler) && (handler instanceof HandlerWrapper)) {
            handler = ((HandlerWrapper) handler).getHandler();
        }
        this._servletHandler = (ServletHandler) handler;
        Enumeration e = getInitParameterNames();
        while (e.hasMoreElements()) {
            String param = e.nextElement();
            String value = getInitParameter(param);
            String lvalue = value.toLowerCase(Locale.ENGLISH);
            boolean z = false;
            if ("nonContextServlets".equals(param)) {
                this._nonContextServlets = value.length() > 0 && lvalue.startsWith("t");
            }
            if ("verbose".equals(param)) {
                if (value.length() > 0 && lvalue.startsWith("t")) {
                    z = true;
                }
                this._verbose = z;
            } else {
                if (this._parameters == null) {
                    this._parameters = new HashMap();
                }
                this._parameters.put(param, value);
            }
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(6:(8:(3:139|140|(5:142|143|77|78|79))|70|71|(1:75)|76|77|78|79)|64|65|66|67|(3:93|94|(1:96)(6:97|98|99|100|101|102))(1:69)) */
    /* JADX WARN: Code restructure failed: missing block: B:89:0x021b, code lost:
        r0 = th;
     */
    @Override // javax.servlet.http.HttpServlet
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void service(javax.servlet.http.HttpServletRequest r22, javax.servlet.http.HttpServletResponse r23) throws javax.servlet.ServletException, java.io.IOException {
        /*
            Method dump skipped, instructions count: 718
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.servlet.Invoker.service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):void");
    }

    /* loaded from: classes.dex */
    class InvokedRequest extends HttpServletRequestWrapper {
        boolean _included;
        String _pathInfo;
        String _servletPath;

        InvokedRequest(HttpServletRequest request, boolean included, String name, String servletPath, String pathInfo) {
            super(request);
            this._included = included;
            this._servletPath = URIUtil.addPaths(servletPath, name);
            this._pathInfo = pathInfo.substring(name.length() + 1);
            if (this._pathInfo.length() == 0) {
                this._pathInfo = null;
            }
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public String getServletPath() {
            if (this._included) {
                return super.getServletPath();
            }
            return this._servletPath;
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public String getPathInfo() {
            if (this._included) {
                return super.getPathInfo();
            }
            return this._pathInfo;
        }

        @Override // javax.servlet.ServletRequestWrapper, javax.servlet.ServletRequest
        public Object getAttribute(String name) {
            if (this._included) {
                if (name.equals(RequestDispatcher.INCLUDE_REQUEST_URI)) {
                    return URIUtil.addPaths(URIUtil.addPaths(getContextPath(), this._servletPath), this._pathInfo);
                }
                if (name.equals(RequestDispatcher.INCLUDE_PATH_INFO)) {
                    return this._pathInfo;
                }
                if (name.equals(RequestDispatcher.INCLUDE_SERVLET_PATH)) {
                    return this._servletPath;
                }
            }
            return super.getAttribute(name);
        }
    }

    private ServletHolder getHolder(ServletHolder[] holders, String servlet) {
        if (holders == null) {
            return null;
        }
        ServletHolder holder = null;
        for (int i = 0; holder == null && i < holders.length; i++) {
            if (holders[i].getName().equals(servlet)) {
                holder = holders[i];
            }
        }
        return holder;
    }
}
