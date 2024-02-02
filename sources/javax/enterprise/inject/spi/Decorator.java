package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface Decorator<T> extends Bean<T> {
    Set<Type> getDecoratedTypes();

    Set<Annotation> getDelegateQualifiers();

    Type getDelegateType();
}
