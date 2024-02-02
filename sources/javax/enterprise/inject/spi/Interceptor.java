package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.interceptor.InvocationContext;
/* loaded from: classes.dex */
public interface Interceptor<T> extends Bean<T> {
    Set<Annotation> getInterceptorBindings();

    Object intercept(InterceptionType interceptionType, T t, InvocationContext invocationContext) throws Exception;

    boolean intercepts(InterceptionType interceptionType);
}
