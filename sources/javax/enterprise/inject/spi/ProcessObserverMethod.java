package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.ObserverMethodConfigurator;
/* loaded from: classes.dex */
public interface ProcessObserverMethod<T, X> {
    void addDefinitionError(Throwable th);

    ObserverMethodConfigurator<T> configureObserverMethod();

    AnnotatedMethod<X> getAnnotatedMethod();

    ObserverMethod<T> getObserverMethod();

    void setObserverMethod(ObserverMethod<T> observerMethod);

    void veto();
}
