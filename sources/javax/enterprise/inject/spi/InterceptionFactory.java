package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
/* loaded from: classes.dex */
public interface InterceptionFactory<T> {
    AnnotatedTypeConfigurator<T> configure();

    T createInterceptedInstance(T t);

    InterceptionFactory<T> ignoreFinalMethods();
}
