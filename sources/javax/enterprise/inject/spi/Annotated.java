package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface Annotated {
    <T extends Annotation> T getAnnotation(Class<T> cls);

    Set<Annotation> getAnnotations();

    <T extends Annotation> Set<T> getAnnotations(Class<T> cls);

    Type getBaseType();

    Set<Type> getTypeClosure();

    boolean isAnnotationPresent(Class<? extends Annotation> cls);
}
