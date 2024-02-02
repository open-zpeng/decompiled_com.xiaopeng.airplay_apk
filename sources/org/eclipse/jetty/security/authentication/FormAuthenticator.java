package org.eclipse.jetty.security.authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class FormAuthenticator extends LoginAuthenticator {
    private static final Logger LOG = Log.getLogger(FormAuthenticator.class);
    public static final String __FORM_DISPATCH = "org.eclipse.jetty.security.dispatch";
    public static final String __FORM_ERROR_PAGE = "org.eclipse.jetty.security.form_error_page";
    public static final String __FORM_LOGIN_PAGE = "org.eclipse.jetty.security.form_login_page";
    public static final String __J_PASSWORD = "j_password";
    public static final String __J_POST = "org.eclipse.jetty.security.form_POST";
    public static final String __J_SECURITY_CHECK = "/j_security_check";
    public static final String __J_URI = "org.eclipse.jetty.security.form_URI";
    public static final String __J_USERNAME = "j_username";
    private boolean _alwaysSaveUri;
    private boolean _dispatch;
    private String _formErrorPage;
    private String _formErrorPath;
    private String _formLoginPage;
    private String _formLoginPath;

    public FormAuthenticator() {
    }

    public FormAuthenticator(String login, String error, boolean dispatch) {
        this();
        if (login != null) {
            setLoginPage(login);
        }
        if (error != null) {
            setErrorPage(error);
        }
        this._dispatch = dispatch;
    }

    public void setAlwaysSaveUri(boolean alwaysSave) {
        this._alwaysSaveUri = alwaysSave;
    }

    public boolean getAlwaysSaveUri() {
        return this._alwaysSaveUri;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginAuthenticator, org.eclipse.jetty.security.Authenticator
    public void setConfiguration(Authenticator.AuthConfiguration configuration) {
        super.setConfiguration(configuration);
        String login = configuration.getInitParameter(__FORM_LOGIN_PAGE);
        if (login != null) {
            setLoginPage(login);
        }
        String error = configuration.getInitParameter(__FORM_ERROR_PAGE);
        if (error != null) {
            setErrorPage(error);
        }
        String dispatch = configuration.getInitParameter(__FORM_DISPATCH);
        this._dispatch = dispatch == null ? this._dispatch : Boolean.valueOf(dispatch).booleanValue();
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public String getAuthMethod() {
        return "FORM";
    }

    private void setLoginPage(String path) {
        if (!path.startsWith("/")) {
            LOG.warn("form-login-page must start with /", new Object[0]);
            path = "/" + path;
        }
        this._formLoginPage = path;
        this._formLoginPath = path;
        if (this._formLoginPath.indexOf(63) > 0) {
            this._formLoginPath = this._formLoginPath.substring(0, this._formLoginPath.indexOf(63));
        }
    }

    private void setErrorPage(String path) {
        if (path == null || path.trim().length() == 0) {
            this._formErrorPath = null;
            this._formErrorPage = null;
            return;
        }
        if (!path.startsWith("/")) {
            LOG.warn("form-error-page must start with /", new Object[0]);
            path = "/" + path;
        }
        this._formErrorPage = path;
        this._formErrorPath = path;
        if (this._formErrorPath.indexOf(63) > 0) {
            this._formErrorPath = this._formErrorPath.substring(0, this._formErrorPath.indexOf(63));
        }
    }

    @Override // org.eclipse.jetty.security.authentication.LoginAuthenticator
    public UserIdentity login(String username, Object password, ServletRequest request) {
        UserIdentity user = super.login(username, password, request);
        if (user != null) {
            HttpSession session = ((HttpServletRequest) request).getSession(true);
            Authentication cached = new SessionAuthentication(getAuthMethod(), user, password);
            session.setAttribute(SessionAuthentication.__J_AUTHENTICATED, cached);
        }
        return user;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
        String nuri;
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();
        if (uri == null) {
            uri = "/";
        }
        String uri2 = uri;
        if (!(mandatory | isJSecurityCheck(uri2))) {
            return new DeferredAuthentication(this);
        }
        if (isLoginOrErrorPage(URIUtil.addPaths(request.getServletPath(), request.getPathInfo())) && !DeferredAuthentication.isDeferred(response)) {
            return new DeferredAuthentication(this);
        }
        HttpSession session = request.getSession(true);
        try {
            if (isJSecurityCheck(uri2)) {
                String username = request.getParameter(__J_USERNAME);
                String password = request.getParameter(__J_PASSWORD);
                UserIdentity user = login(username, password, request);
                HttpSession session2 = request.getSession(true);
                if (user != null) {
                    synchronized (session2) {
                        nuri = (String) session2.getAttribute(__J_URI);
                        if (nuri == null || nuri.length() == 0) {
                            nuri = request.getContextPath();
                            if (nuri.length() == 0) {
                                nuri = "/";
                            }
                        }
                    }
                    response.setContentLength(0);
                    response.sendRedirect(response.encodeRedirectURL(nuri));
                    return new FormAuthentication(getAuthMethod(), user);
                }
                if (LOG.isDebugEnabled()) {
                    Logger logger = LOG;
                    logger.debug("Form authentication FAILED for " + StringUtil.printable(username), new Object[0]);
                }
                if (this._formErrorPage == null) {
                    if (response != null) {
                        response.sendError(403);
                    }
                } else if (this._dispatch) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(this._formErrorPage);
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
                    response.setDateHeader(HttpHeaders.EXPIRES, 1L);
                    dispatcher.forward(new FormRequest(request), new FormResponse(response));
                } else {
                    response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getContextPath(), this._formErrorPage)));
                }
                return Authentication.SEND_FAILURE;
            }
            Authentication authentication = (Authentication) session.getAttribute(SessionAuthentication.__J_AUTHENTICATED);
            if (authentication != null) {
                if ((authentication instanceof Authentication.User) && this._loginService != null && !this._loginService.validate(((Authentication.User) authentication).getUserIdentity())) {
                    session.removeAttribute(SessionAuthentication.__J_AUTHENTICATED);
                } else {
                    String j_uri = (String) session.getAttribute(__J_URI);
                    if (j_uri != null) {
                        MultiMap<String> j_post = (MultiMap) session.getAttribute(__J_POST);
                        if (j_post != null) {
                            StringBuffer buf = request.getRequestURL();
                            if (request.getQueryString() != null) {
                                buf.append("?");
                                buf.append(request.getQueryString());
                            }
                            if (j_uri.equals(buf.toString())) {
                                session.removeAttribute(__J_POST);
                                Request base_request = req instanceof Request ? (Request) req : AbstractHttpConnection.getCurrentConnection().getRequest();
                                base_request.setMethod(HttpMethods.POST);
                                base_request.setParameters(j_post);
                            }
                        } else {
                            session.removeAttribute(__J_URI);
                        }
                    }
                    return authentication;
                }
            }
            if (DeferredAuthentication.isDeferred(response)) {
                LOG.debug("auth deferred {}", session.getId());
                return Authentication.UNAUTHENTICATED;
            }
            synchronized (session) {
                if (session.getAttribute(__J_URI) == null || this._alwaysSaveUri) {
                    StringBuffer buf2 = request.getRequestURL();
                    if (request.getQueryString() != null) {
                        buf2.append("?");
                        buf2.append(request.getQueryString());
                    }
                    session.setAttribute(__J_URI, buf2.toString());
                    if (MimeTypes.FORM_ENCODED.equalsIgnoreCase(req.getContentType()) && HttpMethods.POST.equals(request.getMethod())) {
                        Request base_request2 = req instanceof Request ? (Request) req : AbstractHttpConnection.getCurrentConnection().getRequest();
                        base_request2.extractParameters();
                        session.setAttribute(__J_POST, new MultiMap((MultiMap) base_request2.getParameters()));
                    }
                }
            }
            if (this._dispatch) {
                RequestDispatcher dispatcher2 = request.getRequestDispatcher(this._formLoginPage);
                response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
                response.setDateHeader(HttpHeaders.EXPIRES, 1L);
                dispatcher2.forward(new FormRequest(request), new FormResponse(response));
            } else {
                response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getContextPath(), this._formLoginPage)));
            }
            return Authentication.SEND_CONTINUE;
        } catch (IOException e) {
            throw new ServerAuthException(e);
        } catch (ServletException e2) {
            throw new ServerAuthException(e2);
        }
    }

    public boolean isJSecurityCheck(String uri) {
        int jsc = uri.indexOf(__J_SECURITY_CHECK);
        if (jsc < 0) {
            return false;
        }
        int e = __J_SECURITY_CHECK.length() + jsc;
        if (e == uri.length()) {
            return true;
        }
        char c = uri.charAt(e);
        return c == ';' || c == '#' || c == '/' || c == '?';
    }

    public boolean isLoginOrErrorPage(String pathInContext) {
        return pathInContext != null && (pathInContext.equals(this._formErrorPath) || pathInContext.equals(this._formLoginPath));
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
        return true;
    }

    /* loaded from: classes.dex */
    protected static class FormRequest extends HttpServletRequestWrapper {
        public FormRequest(HttpServletRequest request) {
            super(request);
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public long getDateHeader(String name) {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-")) {
                return -1L;
            }
            return super.getDateHeader(name);
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public String getHeader(String name) {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-")) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public Enumeration getHeaderNames() {
            return Collections.enumeration(Collections.list(super.getHeaderNames()));
        }

        @Override // javax.servlet.http.HttpServletRequestWrapper, javax.servlet.http.HttpServletRequest
        public Enumeration getHeaders(String name) {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-")) {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }
            return super.getHeaders(name);
        }
    }

    /* loaded from: classes.dex */
    protected static class FormResponse extends HttpServletResponseWrapper {
        public FormResponse(HttpServletResponse response) {
            super(response);
        }

        @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
        public void addDateHeader(String name, long date) {
            if (notIgnored(name)) {
                super.addDateHeader(name, date);
            }
        }

        @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
        public void addHeader(String name, String value) {
            if (notIgnored(name)) {
                super.addHeader(name, value);
            }
        }

        @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
        public void setDateHeader(String name, long date) {
            if (notIgnored(name)) {
                super.setDateHeader(name, date);
            }
        }

        @Override // javax.servlet.http.HttpServletResponseWrapper, javax.servlet.http.HttpServletResponse
        public void setHeader(String name, String value) {
            if (notIgnored(name)) {
                super.setHeader(name, value);
            }
        }

        private boolean notIgnored(String name) {
            if (HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(name) || HttpHeaders.PRAGMA.equalsIgnoreCase(name) || HttpHeaders.ETAG.equalsIgnoreCase(name) || HttpHeaders.EXPIRES.equalsIgnoreCase(name) || HttpHeaders.LAST_MODIFIED.equalsIgnoreCase(name) || HttpHeaders.AGE.equalsIgnoreCase(name)) {
                return false;
            }
            return true;
        }
    }

    /* loaded from: classes.dex */
    public static class FormAuthentication extends UserAuthentication implements Authentication.ResponseSent {
        public FormAuthentication(String method, UserIdentity userIdentity) {
            super(method, userIdentity);
        }

        @Override // org.eclipse.jetty.security.UserAuthentication
        public String toString() {
            return "Form" + super.toString();
        }
    }
}
