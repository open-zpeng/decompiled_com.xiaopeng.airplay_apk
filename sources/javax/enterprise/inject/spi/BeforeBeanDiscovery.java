package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
/* loaded from: classes.dex */
public interface BeforeBeanDiscovery {
    <T> AnnotatedTypeConfigurator<T> addAnnotatedType(Class<T> cls, String str);

    void addAnnotatedType(AnnotatedType<?> annotatedType);

    void addAnnotatedType(AnnotatedType<?> annotatedType, String str);

    void addInterceptorBinding(Class<? extends Annotation> cls, Annotation... annotationArr);

    void addInterceptorBinding(AnnotatedType<? extends Annotation> annotatedType);

    void addQualifier(Class<? extends Annotation> cls);

    void addQualifier(AnnotatedType<? extends Annotation> annotatedType);

    void addScope(Class<? extends Annotation> cls, boolean z, boolean z2);

    void addStereotype(Class<? extends Annotation> cls, Annotation... annotationArr);

    <T extends Annotation> AnnotatedTypeConfigurator<T> configureInterceptorBinding(Class<T> cls);

    <T extends Annotation> AnnotatedTypeConfigurator<T> configureQualifier(Class<T> cls);
}
