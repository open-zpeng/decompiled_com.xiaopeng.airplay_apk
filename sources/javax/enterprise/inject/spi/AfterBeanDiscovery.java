package javax.enterprise.inject.spi;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.configurator.BeanConfigurator;
import javax.enterprise.inject.spi.configurator.ObserverMethodConfigurator;
/* loaded from: classes.dex */
public interface AfterBeanDiscovery {
    <T> BeanConfigurator<T> addBean();

    void addBean(Bean<?> bean);

    void addContext(Context context);

    void addDefinitionError(Throwable th);

    <T> ObserverMethodConfigurator<T> addObserverMethod();

    void addObserverMethod(ObserverMethod<?> observerMethod);

    <T> AnnotatedType<T> getAnnotatedType(Class<T> cls, String str);

    <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> cls);
}
