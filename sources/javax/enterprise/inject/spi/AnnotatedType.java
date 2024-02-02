package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
/* loaded from: classes.dex */
public interface AnnotatedType<X> extends Annotated {
    Set<AnnotatedConstructor<X>> getConstructors();

    Set<AnnotatedField<? super X>> getFields();

    Class<X> getJavaClass();

    Set<AnnotatedMethod<? super X>> getMethods();

    /* JADX WARN: Multi-variable type inference failed */
    @Override // javax.enterprise.inject.spi.Annotated
    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        return new LinkedHashSet(Arrays.asList(getJavaClass().getAnnotationsByType(annotationType)));
    }
}
