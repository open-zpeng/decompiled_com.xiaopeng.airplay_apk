package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface BeanAttributes<T> {
    String getName();

    Set<Annotation> getQualifiers();

    Class<? extends Annotation> getScope();

    Set<Class<? extends Annotation>> getStereotypes();

    Set<Type> getTypes();

    boolean isAlternative();
}
