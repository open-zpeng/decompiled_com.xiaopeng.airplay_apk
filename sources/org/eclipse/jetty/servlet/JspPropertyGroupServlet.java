package org.eclipse.jetty.servlet;

import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class JspPropertyGroupServlet extends GenericServlet {
    public static final String NAME = "__org.eclipse.jetty.servlet.JspPropertyGroupServlet__";
    private static final long serialVersionUID = 3681783214726776945L;
    private final ContextHandler _contextHandler;
    private ServletHolder _dftServlet;
    private ServletHolder _jspServlet;
    private final ServletHandler _servletHandler;
    private boolean _starJspMapped;

    public JspPropertyGroupServlet(ContextHandler context, ServletHandler servletHandler) {
        this._contextHandler = context;
        this._servletHandler = servletHandler;
    }

    @Override // javax.servlet.GenericServlet
    public void init() throws ServletException {
        String jsp_name = "jsp";
        ServletMapping servlet_mapping = this._servletHandler.getServletMapping("*.jsp");
        if (servlet_mapping != null) {
            this._starJspMapped = true;
            ServletMapping[] mappings = this._servletHandler.getServletMappings();
            ServletMapping servlet_mapping2 = servlet_mapping;
            for (ServletMapping m : mappings) {
                String[] paths = m.getPathSpecs();
                if (paths != null) {
                    ServletMapping servlet_mapping3 = servlet_mapping2;
                    for (String path : paths) {
                        if ("*.jsp".equals(path) && !NAME.equals(m.getServletName())) {
                            servlet_mapping3 = m;
                        }
                    }
                    servlet_mapping2 = servlet_mapping3;
                }
            }
            jsp_name = servlet_mapping2.getServletName();
        }
        this._jspServlet = this._servletHandler.getServlet(jsp_name);
        String dft_name = ServletHandler.__DEFAULT_SERVLET;
        ServletMapping default_mapping = this._servletHandler.getServletMapping("/");
        if (default_mapping != null) {
            dft_name = default_mapping.getServletName();
        }
        this._dftServlet = this._servletHandler.getServlet(dft_name);
    }

    @Override // javax.servlet.GenericServlet, javax.servlet.Servlet
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String servletPath;
        String pathInfo;
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
                servletPath = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
                pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
                if (servletPath == null) {
                    servletPath = request.getServletPath();
                    pathInfo = request.getPathInfo();
                }
            } else {
                servletPath = request.getServletPath();
                pathInfo = request.getPathInfo();
            }
            String pathInContext = URIUtil.addPaths(servletPath, pathInfo);
            if (pathInContext.endsWith("/")) {
                this._dftServlet.getServlet().service(req, res);
                return;
            } else if (this._starJspMapped && pathInContext.toLowerCase().endsWith(".jsp")) {
                this._jspServlet.getServlet().service(req, res);
                return;
            } else {
                Resource resource = this._contextHandler.getResource(pathInContext);
                if (resource != null && resource.isDirectory()) {
                    this._dftServlet.getServlet().service(req, res);
                    return;
                } else {
                    this._jspServlet.getServlet().service(req, res);
                    return;
                }
            }
        }
        throw new ServletException("Request not HttpServletRequest");
    }
}
