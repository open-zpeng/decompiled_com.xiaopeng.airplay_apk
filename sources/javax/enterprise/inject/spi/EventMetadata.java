package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface EventMetadata {
    InjectionPoint getInjectionPoint();

    Set<Annotation> getQualifiers();

    Type getType();
}
