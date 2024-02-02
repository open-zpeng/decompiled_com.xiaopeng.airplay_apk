package javax.enterprise.inject;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Provider;
/* loaded from: classes.dex */
public interface Instance<T> extends Iterable<T>, Provider<T> {
    void destroy(T t);

    boolean isAmbiguous();

    boolean isUnsatisfied();

    <U extends T> Instance<U> select(Class<U> cls, Annotation... annotationArr);

    <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotationArr);

    Instance<T> select(Annotation... annotationArr);

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default boolean isResolvable() {
        return (isUnsatisfied() || isAmbiguous()) ? false : true;
    }
}
