package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.InjectionPointConfigurator;
/* loaded from: classes.dex */
public interface ProcessInjectionPoint<T, X> {
    void addDefinitionError(Throwable th);

    InjectionPointConfigurator configureInjectionPoint();

    InjectionPoint getInjectionPoint();

    void setInjectionPoint(InjectionPoint injectionPoint);
}
