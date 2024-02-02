package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import javax.enterprise.inject.spi.AnnotatedField;
/* loaded from: classes.dex */
public interface AnnotatedFieldConfigurator<T> {
    AnnotatedFieldConfigurator<T> add(Annotation annotation);

    AnnotatedField<T> getAnnotated();

    AnnotatedFieldConfigurator<T> remove(Predicate<Annotation> predicate);

    static /* synthetic */ boolean lambda$removeAll$0(Annotation a) {
        return true;
    }

    default AnnotatedFieldConfigurator<T> removeAll() {
        return remove(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedFieldConfigurator$bEUhBCdO5JbYkKYjvlO8F3VRP3g
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AnnotatedFieldConfigurator.lambda$removeAll$0((Annotation) obj);
            }
        });
    }
}
