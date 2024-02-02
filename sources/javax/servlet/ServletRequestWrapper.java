package javax.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
/* loaded from: classes.dex */
public class ServletRequestWrapper implements ServletRequest {
    private ServletRequest request;

    public ServletRequestWrapper(ServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        this.request = request;
    }

    public ServletRequest getRequest() {
        return this.request;
    }

    public void setRequest(ServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        this.request = request;
    }

    @Override // javax.servlet.ServletRequest
    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration<String> getAttributeNames() {
        return this.request.getAttributeNames();
    }

    @Override // javax.servlet.ServletRequest
    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }

    @Override // javax.servlet.ServletRequest
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding(enc);
    }

    @Override // javax.servlet.ServletRequest
    public int getContentLength() {
        return this.request.getContentLength();
    }

    @Override // javax.servlet.ServletRequest
    public String getContentType() {
        return this.request.getContentType();
    }

    @Override // javax.servlet.ServletRequest
    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }

    @Override // javax.servlet.ServletRequest
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    @Override // javax.servlet.ServletRequest
    public Map<String, String[]> getParameterMap() {
        return this.request.getParameterMap();
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration<String> getParameterNames() {
        return this.request.getParameterNames();
    }

    @Override // javax.servlet.ServletRequest
    public String[] getParameterValues(String name) {
        return this.request.getParameterValues(name);
    }

    @Override // javax.servlet.ServletRequest
    public String getProtocol() {
        return this.request.getProtocol();
    }

    @Override // javax.servlet.ServletRequest
    public String getScheme() {
        return this.request.getScheme();
    }

    @Override // javax.servlet.ServletRequest
    public String getServerName() {
        return this.request.getServerName();
    }

    @Override // javax.servlet.ServletRequest
    public int getServerPort() {
        return this.request.getServerPort();
    }

    @Override // javax.servlet.ServletRequest
    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    @Override // javax.servlet.ServletRequest
    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    @Override // javax.servlet.ServletRequest
    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    @Override // javax.servlet.ServletRequest
    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }

    @Override // javax.servlet.ServletRequest
    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    @Override // javax.servlet.ServletRequest
    public Locale getLocale() {
        return this.request.getLocale();
    }

    @Override // javax.servlet.ServletRequest
    public Enumeration<Locale> getLocales() {
        return this.request.getLocales();
    }

    @Override // javax.servlet.ServletRequest
    public boolean isSecure() {
        return this.request.isSecure();
    }

    @Override // javax.servlet.ServletRequest
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.request.getRequestDispatcher(path);
    }

    @Override // javax.servlet.ServletRequest
    public String getRealPath(String path) {
        return this.request.getRealPath(path);
    }

    @Override // javax.servlet.ServletRequest
    public int getRemotePort() {
        return this.request.getRemotePort();
    }

    @Override // javax.servlet.ServletRequest
    public String getLocalName() {
        return this.request.getLocalName();
    }

    @Override // javax.servlet.ServletRequest
    public String getLocalAddr() {
        return this.request.getLocalAddr();
    }

    @Override // javax.servlet.ServletRequest
    public int getLocalPort() {
        return this.request.getLocalPort();
    }

    @Override // javax.servlet.ServletRequest
    public ServletContext getServletContext() {
        return this.request.getServletContext();
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext startAsync() throws IllegalStateException {
        return this.request.startAsync();
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return this.request.startAsync(servletRequest, servletResponse);
    }

    @Override // javax.servlet.ServletRequest
    public boolean isAsyncStarted() {
        return this.request.isAsyncStarted();
    }

    @Override // javax.servlet.ServletRequest
    public boolean isAsyncSupported() {
        return this.request.isAsyncSupported();
    }

    @Override // javax.servlet.ServletRequest
    public AsyncContext getAsyncContext() {
        return this.request.getAsyncContext();
    }

    public boolean isWrapperFor(ServletRequest wrapped) {
        if (this.request == wrapped) {
            return true;
        }
        if (this.request instanceof ServletRequestWrapper) {
            return ((ServletRequestWrapper) this.request).isWrapperFor(wrapped);
        }
        return false;
    }

    public boolean isWrapperFor(Class wrappedType) {
        if (!ServletRequest.class.isAssignableFrom(wrappedType)) {
            throw new IllegalArgumentException("Given class " + wrappedType.getName() + " not a subinterface of " + ServletRequest.class.getName());
        } else if (wrappedType.isAssignableFrom(this.request.getClass())) {
            return true;
        } else {
            if (this.request instanceof ServletRequestWrapper) {
                return ((ServletRequestWrapper) this.request).isWrapperFor(wrappedType);
            }
            return false;
        }
    }

    @Override // javax.servlet.ServletRequest
    public DispatcherType getDispatcherType() {
        return this.request.getDispatcherType();
    }
}
