package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
/* loaded from: classes.dex */
public interface AnnotatedConstructor<X> extends AnnotatedCallable<X> {
    @Override // javax.enterprise.inject.spi.AnnotatedMember
    Constructor<X> getJavaMember();

    @Override // javax.enterprise.inject.spi.Annotated
    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        return new LinkedHashSet(Arrays.asList(getJavaMember().getAnnotationsByType(annotationType)));
    }
}
