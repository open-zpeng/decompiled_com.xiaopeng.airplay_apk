package javax.enterprise.inject.spi;

import javax.enterprise.context.spi.CreationalContext;
/* loaded from: classes.dex */
public interface InjectionTarget<T> extends Producer<T> {
    void inject(T t, CreationalContext<T> creationalContext);

    void postConstruct(T t);

    void preDestroy(T t);
}
