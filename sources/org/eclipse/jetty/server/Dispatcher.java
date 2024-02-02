package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
/* loaded from: classes.dex */
public class Dispatcher implements RequestDispatcher {
    public static final String __FORWARD_PREFIX = "javax.servlet.forward.";
    public static final String __INCLUDE_PREFIX = "javax.servlet.include.";
    public static final String __JSP_FILE = "org.apache.catalina.jsp_file";
    private final ContextHandler _contextHandler;
    private final String _dQuery;
    private final String _named;
    private final String _path;
    private final String _uri;

    public Dispatcher(ContextHandler contextHandler, String uri, String pathInContext, String query) {
        this._contextHandler = contextHandler;
        this._uri = uri;
        this._path = pathInContext;
        this._dQuery = query;
        this._named = null;
    }

    public Dispatcher(ContextHandler contextHandler, String name) throws IllegalStateException {
        this._contextHandler = contextHandler;
        this._named = name;
        this._uri = null;
        this._path = null;
        this._dQuery = null;
    }

    @Override // javax.servlet.RequestDispatcher
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        forward(request, response, DispatcherType.FORWARD);
    }

    public void error(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        forward(request, response, DispatcherType.ERROR);
    }

    @Override // javax.servlet.RequestDispatcher
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        Request baseRequest = request instanceof Request ? (Request) request : AbstractHttpConnection.getCurrentConnection().getRequest();
        if (!(request instanceof HttpServletRequest)) {
            request = new ServletRequestHttpWrapper(request);
        }
        if (!(response instanceof HttpServletResponse)) {
            response = new ServletResponseHttpWrapper(response);
        }
        DispatcherType old_type = baseRequest.getDispatcherType();
        Attributes old_attr = baseRequest.getAttributes();
        MultiMap old_params = baseRequest.getParameters();
        try {
            baseRequest.setDispatcherType(DispatcherType.INCLUDE);
            baseRequest.getConnection().include();
            if (this._named != null) {
                this._contextHandler.handle(this._named, baseRequest, (HttpServletRequest) request, (HttpServletResponse) response);
            } else {
                String query = this._dQuery;
                if (query != null) {
                    if (old_params == null) {
                        baseRequest.extractParameters();
                        old_params = baseRequest.getParameters();
                    }
                    MultiMap parameters = new MultiMap();
                    UrlEncoded.decodeTo(query, parameters, baseRequest.getCharacterEncoding());
                    if (old_params != null && old_params.size() > 0) {
                        for (Map.Entry<String, Object> entry : old_params.entrySet()) {
                            String name = entry.getKey();
                            Object values = entry.getValue();
                            for (int i = 0; i < LazyList.size(values); i++) {
                                parameters.add(name, LazyList.get(values, i));
                            }
                        }
                    }
                    baseRequest.setParameters(parameters);
                }
                IncludeAttributes attr = new IncludeAttributes(old_attr);
                attr._requestURI = this._uri;
                attr._contextPath = this._contextHandler.getContextPath();
                attr._servletPath = null;
                attr._pathInfo = this._path;
                attr._query = query;
                baseRequest.setAttributes(attr);
                this._contextHandler.handle(this._path, baseRequest, (HttpServletRequest) request, (HttpServletResponse) response);
            }
        } finally {
            baseRequest.setAttributes(old_attr);
            baseRequest.getConnection().included();
            baseRequest.setParameters(old_params);
            baseRequest.setDispatcherType(old_type);
        }
    }

    /* JADX WARN: Not initialized variable reg: 17, insn: 0x007c: MOVE  (r4 I:??[OBJECT, ARRAY]) = (r17 I:??[OBJECT, ARRAY] A[D('old_type' javax.servlet.DispatcherType)]), block:B:23:0x007c */
    protected void forward(ServletRequest request, ServletResponse response, DispatcherType dispatch) throws ServletException, IOException {
        ServletRequest request2;
        DispatcherType old_type;
        DispatcherType old_type2;
        DispatcherType old_type3;
        ServletResponse servletResponse = response;
        Request baseRequest = request instanceof Request ? (Request) request : AbstractHttpConnection.getCurrentConnection().getRequest();
        Response base_response = baseRequest.getResponse();
        response.resetBuffer();
        base_response.fwdReset();
        if (!(request instanceof HttpServletRequest)) {
            request2 = new ServletRequestHttpWrapper(request);
        } else {
            request2 = request;
        }
        boolean z = servletResponse instanceof HttpServletResponse;
        ServletResponse response2 = servletResponse;
        if (!z) {
            response2 = new ServletResponseHttpWrapper(servletResponse);
        }
        boolean old_handled = baseRequest.isHandled();
        String old_uri = baseRequest.getRequestURI();
        String old_context_path = baseRequest.getContextPath();
        String old_servlet_path = baseRequest.getServletPath();
        String old_path_info = baseRequest.getPathInfo();
        String old_query = baseRequest.getQueryString();
        Attributes old_attr = baseRequest.getAttributes();
        DispatcherType old_type4 = baseRequest.getDispatcherType();
        MultiMap<String> old_params = baseRequest.getParameters();
        try {
            baseRequest.setHandled(false);
            baseRequest.setDispatcherType(dispatch);
            try {
                if (this._named != null) {
                    try {
                        try {
                            old_type3 = old_type4;
                            this._contextHandler.handle(this._named, baseRequest, (HttpServletRequest) request2, (HttpServletResponse) response2);
                        } catch (Throwable th) {
                            th = th;
                            old_type = old_type4;
                            baseRequest.setHandled(old_handled);
                            baseRequest.setRequestURI(old_uri);
                            baseRequest.setContextPath(old_context_path);
                            baseRequest.setServletPath(old_servlet_path);
                            baseRequest.setPathInfo(old_path_info);
                            baseRequest.setAttributes(old_attr);
                            baseRequest.setParameters(old_params);
                            baseRequest.setQueryString(old_query);
                            baseRequest.setDispatcherType(old_type);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        old_type = old_type4;
                    }
                } else {
                    old_type3 = old_type4;
                    try {
                        String query = this._dQuery;
                        if (query != null) {
                            if (old_params == null) {
                                baseRequest.extractParameters();
                                old_params = baseRequest.getParameters();
                            }
                            baseRequest.mergeQueryString(query);
                        }
                        ForwardAttributes attr = new ForwardAttributes(old_attr);
                        if (old_attr.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) != null) {
                            attr._pathInfo = (String) old_attr.getAttribute(RequestDispatcher.FORWARD_PATH_INFO);
                            attr._query = (String) old_attr.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
                            attr._requestURI = (String) old_attr.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
                            attr._contextPath = (String) old_attr.getAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH);
                            attr._servletPath = (String) old_attr.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
                        } else {
                            attr._pathInfo = old_path_info;
                            attr._query = old_query;
                            attr._requestURI = old_uri;
                            attr._contextPath = old_context_path;
                            attr._servletPath = old_servlet_path;
                        }
                        baseRequest.setRequestURI(this._uri);
                        baseRequest.setContextPath(this._contextHandler.getContextPath());
                        baseRequest.setServletPath(null);
                        baseRequest.setPathInfo(this._uri);
                        baseRequest.setAttributes(attr);
                        this._contextHandler.handle(this._path, baseRequest, (HttpServletRequest) request2, (HttpServletResponse) response2);
                        if (!baseRequest.getAsyncContinuation().isAsyncStarted()) {
                            commitResponse(response2, baseRequest);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        old_type = old_type3;
                        baseRequest.setHandled(old_handled);
                        baseRequest.setRequestURI(old_uri);
                        baseRequest.setContextPath(old_context_path);
                        baseRequest.setServletPath(old_servlet_path);
                        baseRequest.setPathInfo(old_path_info);
                        baseRequest.setAttributes(old_attr);
                        baseRequest.setParameters(old_params);
                        baseRequest.setQueryString(old_query);
                        baseRequest.setDispatcherType(old_type);
                        throw th;
                    }
                }
                baseRequest.setHandled(old_handled);
                baseRequest.setRequestURI(old_uri);
                baseRequest.setContextPath(old_context_path);
                baseRequest.setServletPath(old_servlet_path);
                baseRequest.setPathInfo(old_path_info);
                baseRequest.setAttributes(old_attr);
                baseRequest.setParameters(old_params);
                baseRequest.setQueryString(old_query);
                baseRequest.setDispatcherType(old_type3);
            } catch (Throwable th4) {
                th = th4;
                old_type = old_type2;
            }
        } catch (Throwable th5) {
            th = th5;
            old_type = old_type4;
        }
    }

    private void commitResponse(ServletResponse response, Request baseRequest) throws IOException {
        if (baseRequest.getResponse().isWriting()) {
            try {
                response.getWriter().close();
                return;
            } catch (IllegalStateException e) {
                response.getOutputStream().close();
                return;
            }
        }
        try {
            response.getOutputStream().close();
        } catch (IllegalStateException e2) {
            response.getWriter().close();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ForwardAttributes implements Attributes {
        final Attributes _attr;
        String _contextPath;
        String _pathInfo;
        String _query;
        String _requestURI;
        String _servletPath;

        ForwardAttributes(Attributes attributes) {
            this._attr = attributes;
        }

        @Override // org.eclipse.jetty.util.Attributes
        public Object getAttribute(String key) {
            if (Dispatcher.this._named == null) {
                if (key.equals(RequestDispatcher.FORWARD_PATH_INFO)) {
                    return this._pathInfo;
                }
                if (key.equals(RequestDispatcher.FORWARD_REQUEST_URI)) {
                    return this._requestURI;
                }
                if (key.equals(RequestDispatcher.FORWARD_SERVLET_PATH)) {
                    return this._servletPath;
                }
                if (key.equals(RequestDispatcher.FORWARD_CONTEXT_PATH)) {
                    return this._contextPath;
                }
                if (key.equals(RequestDispatcher.FORWARD_QUERY_STRING)) {
                    return this._query;
                }
            }
            if (key.startsWith(Dispatcher.__INCLUDE_PREFIX)) {
                return null;
            }
            return this._attr.getAttribute(key);
        }

        @Override // org.eclipse.jetty.util.Attributes
        public Enumeration getAttributeNames() {
            HashSet set = new HashSet();
            Enumeration e = this._attr.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                if (!name.startsWith(Dispatcher.__INCLUDE_PREFIX) && !name.startsWith(Dispatcher.__FORWARD_PREFIX)) {
                    set.add(name);
                }
            }
            if (Dispatcher.this._named == null) {
                if (this._pathInfo != null) {
                    set.add(RequestDispatcher.FORWARD_PATH_INFO);
                } else {
                    set.remove(RequestDispatcher.FORWARD_PATH_INFO);
                }
                set.add(RequestDispatcher.FORWARD_REQUEST_URI);
                set.add(RequestDispatcher.FORWARD_SERVLET_PATH);
                set.add(RequestDispatcher.FORWARD_CONTEXT_PATH);
                if (this._query != null) {
                    set.add(RequestDispatcher.FORWARD_QUERY_STRING);
                } else {
                    set.remove(RequestDispatcher.FORWARD_QUERY_STRING);
                }
            }
            return Collections.enumeration(set);
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void setAttribute(String key, Object value) {
            if (Dispatcher.this._named == null && key.startsWith("javax.servlet.")) {
                if (key.equals(RequestDispatcher.FORWARD_PATH_INFO)) {
                    this._pathInfo = (String) value;
                } else if (key.equals(RequestDispatcher.FORWARD_REQUEST_URI)) {
                    this._requestURI = (String) value;
                } else if (key.equals(RequestDispatcher.FORWARD_SERVLET_PATH)) {
                    this._servletPath = (String) value;
                } else if (key.equals(RequestDispatcher.FORWARD_CONTEXT_PATH)) {
                    this._contextPath = (String) value;
                } else if (key.equals(RequestDispatcher.FORWARD_QUERY_STRING)) {
                    this._query = (String) value;
                } else if (value == null) {
                    this._attr.removeAttribute(key);
                } else {
                    this._attr.setAttribute(key, value);
                }
            } else if (value == null) {
                this._attr.removeAttribute(key);
            } else {
                this._attr.setAttribute(key, value);
            }
        }

        public String toString() {
            return "FORWARD+" + this._attr.toString();
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void clearAttributes() {
            throw new IllegalStateException();
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void removeAttribute(String name) {
            setAttribute(name, null);
        }
    }

    /* loaded from: classes.dex */
    private class IncludeAttributes implements Attributes {
        final Attributes _attr;
        String _contextPath;
        String _pathInfo;
        String _query;
        String _requestURI;
        String _servletPath;

        IncludeAttributes(Attributes attributes) {
            this._attr = attributes;
        }

        @Override // org.eclipse.jetty.util.Attributes
        public Object getAttribute(String key) {
            if (Dispatcher.this._named == null) {
                if (key.equals(RequestDispatcher.INCLUDE_PATH_INFO)) {
                    return this._pathInfo;
                }
                if (key.equals(RequestDispatcher.INCLUDE_SERVLET_PATH)) {
                    return this._servletPath;
                }
                if (key.equals(RequestDispatcher.INCLUDE_CONTEXT_PATH)) {
                    return this._contextPath;
                }
                if (key.equals(RequestDispatcher.INCLUDE_QUERY_STRING)) {
                    return this._query;
                }
                if (key.equals(RequestDispatcher.INCLUDE_REQUEST_URI)) {
                    return this._requestURI;
                }
            } else if (key.startsWith(Dispatcher.__INCLUDE_PREFIX)) {
                return null;
            }
            return this._attr.getAttribute(key);
        }

        @Override // org.eclipse.jetty.util.Attributes
        public Enumeration getAttributeNames() {
            HashSet set = new HashSet();
            Enumeration e = this._attr.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                if (!name.startsWith(Dispatcher.__INCLUDE_PREFIX)) {
                    set.add(name);
                }
            }
            if (Dispatcher.this._named == null) {
                if (this._pathInfo != null) {
                    set.add(RequestDispatcher.INCLUDE_PATH_INFO);
                } else {
                    set.remove(RequestDispatcher.INCLUDE_PATH_INFO);
                }
                set.add(RequestDispatcher.INCLUDE_REQUEST_URI);
                set.add(RequestDispatcher.INCLUDE_SERVLET_PATH);
                set.add(RequestDispatcher.INCLUDE_CONTEXT_PATH);
                if (this._query != null) {
                    set.add(RequestDispatcher.INCLUDE_QUERY_STRING);
                } else {
                    set.remove(RequestDispatcher.INCLUDE_QUERY_STRING);
                }
            }
            return Collections.enumeration(set);
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void setAttribute(String key, Object value) {
            if (Dispatcher.this._named == null && key.startsWith("javax.servlet.")) {
                if (!key.equals(RequestDispatcher.INCLUDE_PATH_INFO)) {
                    if (!key.equals(RequestDispatcher.INCLUDE_REQUEST_URI)) {
                        if (!key.equals(RequestDispatcher.INCLUDE_SERVLET_PATH)) {
                            if (!key.equals(RequestDispatcher.INCLUDE_CONTEXT_PATH)) {
                                if (!key.equals(RequestDispatcher.INCLUDE_QUERY_STRING)) {
                                    if (value == null) {
                                        this._attr.removeAttribute(key);
                                        return;
                                    } else {
                                        this._attr.setAttribute(key, value);
                                        return;
                                    }
                                }
                                this._query = (String) value;
                                return;
                            }
                            this._contextPath = (String) value;
                            return;
                        }
                        this._servletPath = (String) value;
                        return;
                    }
                    this._requestURI = (String) value;
                    return;
                }
                this._pathInfo = (String) value;
            } else if (value == null) {
                this._attr.removeAttribute(key);
            } else {
                this._attr.setAttribute(key, value);
            }
        }

        public String toString() {
            return "INCLUDE+" + this._attr.toString();
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void clearAttributes() {
            throw new IllegalStateException();
        }

        @Override // org.eclipse.jetty.util.Attributes
        public void removeAttribute(String name) {
            setAttribute(name, null);
        }
    }
}
