package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
/* loaded from: classes.dex */
public interface InjectionTargetFactory<T> {
    InjectionTarget<T> createInjectionTarget(Bean<T> bean);

    default AnnotatedTypeConfigurator<T> configure() {
        throw new UnsupportedOperationException("Configuration not supported here");
    }
}
