package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
/* loaded from: classes.dex */
public class ServletResponseHttpWrapper extends ServletResponseWrapper implements HttpServletResponse {
    public ServletResponseHttpWrapper(ServletResponse response) {
        super(response);
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addCookie(Cookie cookie) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public boolean containsHeader(String name) {
        return false;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeURL(String url) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeUrl(String url) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendError(int sc, String msg) throws IOException {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendError(int sc) throws IOException {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void sendRedirect(String location) throws IOException {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setDateHeader(String name, long date) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addDateHeader(String name, long date) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setHeader(String name, String value) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addHeader(String name, String value) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setIntHeader(String name, int value) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void addIntHeader(String name, int value) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setStatus(int sc) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public void setStatus(int sc, String sm) {
    }

    @Override // javax.servlet.http.HttpServletResponse
    public String getHeader(String name) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override // javax.servlet.http.HttpServletResponse
    public int getStatus() {
        return 0;
    }
}
