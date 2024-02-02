package org.eclipse.jetty.continuation;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
/* loaded from: classes.dex */
public class ContinuationFilter implements Filter {
    static boolean __debug;
    static boolean _initialized;
    ServletContext _context;
    private boolean _debug;
    private boolean _faux;
    private boolean _filtered;
    private boolean _jetty6;

    /* loaded from: classes.dex */
    public interface FilteredContinuation extends Continuation {
        boolean enter(ServletResponse servletResponse);

        boolean exit();
    }

    @Override // javax.servlet.Filter
    public void init(FilterConfig filterConfig) throws ServletException {
        boolean jetty_7_or_greater = "org.eclipse.jetty.servlet".equals(filterConfig.getClass().getPackage().getName());
        this._context = filterConfig.getServletContext();
        String param = filterConfig.getInitParameter("debug");
        boolean z = false;
        this._debug = param != null && Boolean.parseBoolean(param);
        if (this._debug) {
            __debug = true;
        }
        String param2 = filterConfig.getInitParameter("jetty6");
        if (param2 == null) {
            param2 = filterConfig.getInitParameter("partial");
        }
        if (param2 != null) {
            this._jetty6 = Boolean.parseBoolean(param2);
        } else {
            this._jetty6 = ContinuationSupport.__jetty6 && !jetty_7_or_greater;
        }
        String param3 = filterConfig.getInitParameter("faux");
        if (param3 != null) {
            this._faux = Boolean.parseBoolean(param3);
        } else {
            this._faux = (jetty_7_or_greater || this._jetty6 || this._context.getMajorVersion() >= 3) ? false : true;
        }
        if (this._faux || this._jetty6) {
            z = true;
        }
        this._filtered = z;
        if (this._debug) {
            this._context.log("ContinuationFilter  jetty=" + jetty_7_or_greater + " jetty6=" + this._jetty6 + " faux=" + this._faux + " filtered=" + this._filtered + " servlet3=" + ContinuationSupport.__servlet3);
        }
        _initialized = true;
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x003c  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:61:0x0050 A[SYNTHETIC] */
    @Override // javax.servlet.Filter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void doFilter(javax.servlet.ServletRequest r8, javax.servlet.ServletResponse r9, javax.servlet.FilterChain r10) throws java.io.IOException, javax.servlet.ServletException {
        /*
            r7 = this;
            boolean r0 = r7._filtered
            if (r0 == 0) goto L85
            java.lang.String r0 = "org.eclipse.jetty.continuation"
            java.lang.Object r0 = r8.getAttribute(r0)
            org.eclipse.jetty.continuation.Continuation r0 = (org.eclipse.jetty.continuation.Continuation) r0
            boolean r1 = r7._faux
            if (r1 == 0) goto L21
            if (r0 == 0) goto L16
            boolean r1 = r0 instanceof org.eclipse.jetty.continuation.FauxContinuation
            if (r1 != 0) goto L21
        L16:
            org.eclipse.jetty.continuation.FauxContinuation r1 = new org.eclipse.jetty.continuation.FauxContinuation
            r1.<init>(r8)
            java.lang.String r2 = "org.eclipse.jetty.continuation"
            r8.setAttribute(r2, r1)
            goto L24
        L21:
            r1 = r0
            org.eclipse.jetty.continuation.ContinuationFilter$FilteredContinuation r1 = (org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation) r1
        L24:
            r2 = 0
            r3 = r1
            r1 = r2
        L27:
            if (r1 != 0) goto L84
            r4 = 1
            if (r3 == 0) goto L37
            boolean r5 = r3.enter(r9)     // Catch: java.lang.Throwable -> L33 org.eclipse.jetty.continuation.ContinuationThrowable -> L35
            if (r5 == 0) goto L3a
            goto L37
        L33:
            r5 = move-exception
            goto L6c
        L35:
            r5 = move-exception
            goto L52
        L37:
            r10.doFilter(r8, r9)     // Catch: java.lang.Throwable -> L33 org.eclipse.jetty.continuation.ContinuationThrowable -> L35
        L3a:
            if (r3 != 0) goto L45
            java.lang.String r5 = "org.eclipse.jetty.continuation"
            java.lang.Object r5 = r8.getAttribute(r5)
            r3 = r5
            org.eclipse.jetty.continuation.ContinuationFilter$FilteredContinuation r3 = (org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation) r3
        L45:
            if (r3 == 0) goto L50
            boolean r5 = r3.exit()
            if (r5 == 0) goto L4e
            goto L50
        L4e:
            r4 = r2
        L50:
            r1 = r4
            goto L27
        L52:
            java.lang.String r6 = "faux"
            r7.debug(r6, r5)     // Catch: java.lang.Throwable -> L33
            if (r3 != 0) goto L63
            java.lang.String r5 = "org.eclipse.jetty.continuation"
            java.lang.Object r5 = r8.getAttribute(r5)
            r3 = r5
            org.eclipse.jetty.continuation.ContinuationFilter$FilteredContinuation r3 = (org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation) r3
        L63:
            if (r3 == 0) goto L50
            boolean r5 = r3.exit()
            if (r5 == 0) goto L4e
            goto L50
        L6c:
            if (r3 != 0) goto L77
            java.lang.String r6 = "org.eclipse.jetty.continuation"
            java.lang.Object r6 = r8.getAttribute(r6)
            r3 = r6
            org.eclipse.jetty.continuation.ContinuationFilter$FilteredContinuation r3 = (org.eclipse.jetty.continuation.ContinuationFilter.FilteredContinuation) r3
        L77:
            if (r3 == 0) goto L81
            boolean r6 = r3.exit()
            if (r6 == 0) goto L80
            goto L81
        L80:
            goto L82
        L81:
            r2 = r4
        L82:
            r1 = r2
            throw r5
        L84:
            goto L8f
        L85:
            r10.doFilter(r8, r9)     // Catch: org.eclipse.jetty.continuation.ContinuationThrowable -> L89
            goto L8f
        L89:
            r0 = move-exception
            java.lang.String r1 = "caught"
            r7.debug(r1, r0)
        L8f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.continuation.ContinuationFilter.doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain):void");
    }

    private void debug(String string) {
        if (this._debug) {
            this._context.log(string);
        }
    }

    private void debug(String string, Throwable th) {
        if (this._debug) {
            if (th instanceof ContinuationThrowable) {
                ServletContext servletContext = this._context;
                servletContext.log(string + ":" + th);
                return;
            }
            this._context.log(string, th);
        }
    }

    @Override // javax.servlet.Filter
    public void destroy() {
    }
}
