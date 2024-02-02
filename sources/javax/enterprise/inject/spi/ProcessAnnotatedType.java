package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
/* loaded from: classes.dex */
public interface ProcessAnnotatedType<X> {
    AnnotatedTypeConfigurator<X> configureAnnotatedType();

    AnnotatedType<X> getAnnotatedType();

    void setAnnotatedType(AnnotatedType<X> annotatedType);

    void veto();
}
