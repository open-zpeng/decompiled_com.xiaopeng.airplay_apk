package org.eclipse.jetty.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
/* loaded from: classes.dex */
public class ErrorPageErrorHandler extends ErrorHandler implements ErrorHandler.ErrorPageMapper {
    public static final String GLOBAL_ERROR_PAGE = "org.eclipse.jetty.server.error_page.global";
    protected ServletContext _servletContext;
    private final Map<String, String> _errorPages = new HashMap();
    private final List<ErrorCodeRange> _errorPageList = new ArrayList();

    @Override // org.eclipse.jetty.server.handler.ErrorHandler.ErrorPageMapper
    public String getErrorPage(HttpServletRequest request) {
        Integer code;
        String error_page = null;
        Class<?> exClass = (Class) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);
        if (ServletException.class.equals(exClass)) {
            String error_page2 = this._errorPages.get(exClass.getName());
            error_page = error_page2;
            if (error_page == null) {
                Throwable th = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                while (th instanceof ServletException) {
                    th = ((ServletException) th).getRootCause();
                }
                if (th != null) {
                    exClass = th.getClass();
                }
            }
        }
        while (error_page == null && exClass != null) {
            String error_page3 = this._errorPages.get(exClass.getName());
            error_page = error_page3;
            exClass = exClass.getSuperclass();
        }
        if (error_page == null && (code = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)) != null) {
            String error_page4 = this._errorPages.get(Integer.toString(code.intValue()));
            error_page = error_page4;
            if (error_page == null && this._errorPageList != null) {
                int i = 0;
                while (true) {
                    if (i >= this._errorPageList.size()) {
                        break;
                    }
                    ErrorCodeRange errCode = this._errorPageList.get(i);
                    if (!errCode.isInRange(code.intValue())) {
                        i++;
                    } else {
                        error_page = errCode.getUri();
                        break;
                    }
                }
            }
        }
        if (error_page == null) {
            String error_page5 = this._errorPages.get(GLOBAL_ERROR_PAGE);
            return error_page5;
        }
        return error_page;
    }

    public Map<String, String> getErrorPages() {
        return this._errorPages;
    }

    public void setErrorPages(Map<String, String> errorPages) {
        this._errorPages.clear();
        if (errorPages != null) {
            this._errorPages.putAll(errorPages);
        }
    }

    public void addErrorPage(Class<? extends Throwable> exception, String uri) {
        this._errorPages.put(exception.getName(), uri);
    }

    public void addErrorPage(String exceptionClassName, String uri) {
        this._errorPages.put(exceptionClassName, uri);
    }

    public void addErrorPage(int code, String uri) {
        this._errorPages.put(Integer.toString(code), uri);
    }

    public void addErrorPage(int from, int to, String uri) {
        this._errorPageList.add(new ErrorCodeRange(from, to, uri));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        this._servletContext = ContextHandler.getCurrentContext();
    }

    /* loaded from: classes.dex */
    private class ErrorCodeRange {
        private int _from;
        private int _to;
        private String _uri;

        ErrorCodeRange(int from, int to, String uri) throws IllegalArgumentException {
            if (from > to) {
                throw new IllegalArgumentException("from>to");
            }
            this._from = from;
            this._to = to;
            this._uri = uri;
        }

        boolean isInRange(int value) {
            if (value >= this._from && value <= this._to) {
                return true;
            }
            return false;
        }

        String getUri() {
            return this._uri;
        }

        public String toString() {
            return "from: " + this._from + ",to: " + this._to + ",uri: " + this._uri;
        }
    }
}
