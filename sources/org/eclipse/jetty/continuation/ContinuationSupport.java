package org.eclipse.jetty.continuation;

import java.lang.reflect.Constructor;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
/* loaded from: classes.dex */
public class ContinuationSupport {
    static final boolean __jetty6;
    static final Constructor<? extends Continuation> __newJetty6Continuation;
    static final Constructor<? extends Continuation> __newServlet3Continuation;
    static final boolean __servlet3;
    static final Class<?> __waitingContinuation;

    static {
        boolean servlet3Support = false;
        Class<?> waiting = null;
        Constructor<? extends Continuation> s3cc = null;
        try {
            boolean servlet3 = ServletRequest.class.getMethod("startAsync", new Class[0]) != null;
            if (servlet3) {
                s3cc = ContinuationSupport.class.getClassLoader().loadClass("org.eclipse.jetty.continuation.Servlet3Continuation").asSubclass(Continuation.class).getConstructor(ServletRequest.class);
                servlet3Support = true;
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            __servlet3 = false;
            __newServlet3Continuation = null;
            throw th;
        }
        __servlet3 = servlet3Support;
        __newServlet3Continuation = s3cc;
        boolean jetty6Support = false;
        Constructor<? extends Continuation> j6cc = null;
        try {
            Class<?> jetty6ContinuationClass = ContinuationSupport.class.getClassLoader().loadClass("org.mortbay.util.ajax.Continuation");
            boolean jetty6 = jetty6ContinuationClass != null;
            if (jetty6) {
                j6cc = ContinuationSupport.class.getClassLoader().loadClass("org.eclipse.jetty.continuation.Jetty6Continuation").asSubclass(Continuation.class).getConstructor(ServletRequest.class, jetty6ContinuationClass);
                jetty6Support = true;
            }
        } catch (Exception e2) {
        } catch (Throwable th2) {
            __jetty6 = false;
            __newJetty6Continuation = null;
            throw th2;
        }
        __jetty6 = jetty6Support;
        __newJetty6Continuation = j6cc;
        try {
            waiting = ContinuationSupport.class.getClassLoader().loadClass("org.mortbay.util.ajax.WaitingContinuation");
        } catch (Exception e3) {
        } catch (Throwable th3) {
            __waitingContinuation = null;
            throw th3;
        }
        __waitingContinuation = waiting;
    }

    public static Continuation getContinuation(ServletRequest request) {
        Continuation continuation;
        Continuation continuation2 = (Continuation) request.getAttribute(Continuation.ATTRIBUTE);
        if (continuation2 != null) {
            return continuation2;
        }
        while (request instanceof ServletRequestWrapper) {
            request = request.getRequest();
        }
        if (__servlet3) {
            try {
                Continuation continuation3 = __newServlet3Continuation.newInstance(request);
                request.setAttribute(Continuation.ATTRIBUTE, continuation3);
                return continuation3;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (__jetty6) {
            Object c = request.getAttribute("org.mortbay.jetty.ajax.Continuation");
            if (c != null) {
                try {
                    if (__waitingContinuation != null && !__waitingContinuation.isInstance(c)) {
                        continuation = __newJetty6Continuation.newInstance(request, c);
                        request.setAttribute(Continuation.ATTRIBUTE, continuation);
                        return continuation;
                    }
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
            continuation = new FauxContinuation(request);
            request.setAttribute(Continuation.ATTRIBUTE, continuation);
            return continuation;
        } else {
            throw new IllegalStateException("!(Jetty || Servlet 3.0 || ContinuationFilter)");
        }
    }

    @Deprecated
    public static Continuation getContinuation(ServletRequest request, ServletResponse response) {
        return getContinuation(request);
    }
}
