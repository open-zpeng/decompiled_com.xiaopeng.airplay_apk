package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface InjectionPointConfigurator {
    InjectionPointConfigurator addQualifier(Annotation annotation);

    InjectionPointConfigurator addQualifiers(Set<Annotation> set);

    InjectionPointConfigurator addQualifiers(Annotation... annotationArr);

    InjectionPointConfigurator delegate(boolean z);

    InjectionPointConfigurator qualifiers(Set<Annotation> set);

    InjectionPointConfigurator qualifiers(Annotation... annotationArr);

    InjectionPointConfigurator transientField(boolean z);

    InjectionPointConfigurator type(Type type);
}
