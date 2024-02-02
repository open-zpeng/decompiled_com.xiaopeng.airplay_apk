package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
/* loaded from: classes.dex */
public interface AnnotatedTypeConfigurator<T> {
    AnnotatedTypeConfigurator<T> add(Annotation annotation);

    Set<AnnotatedConstructorConfigurator<T>> constructors();

    Set<AnnotatedFieldConfigurator<? super T>> fields();

    AnnotatedType<T> getAnnotated();

    Set<AnnotatedMethodConfigurator<? super T>> methods();

    AnnotatedTypeConfigurator<T> remove(Predicate<Annotation> predicate);

    static /* synthetic */ boolean lambda$removeAll$0(Annotation a) {
        return true;
    }

    default AnnotatedTypeConfigurator<T> removeAll() {
        return remove(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedTypeConfigurator$lpe2CSi01RkiPrCugI0dPzTLiPM
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AnnotatedTypeConfigurator.lambda$removeAll$0((Annotation) obj);
            }
        });
    }

    default Stream<AnnotatedMethodConfigurator<? super T>> filterMethods(final Predicate<AnnotatedMethod<? super T>> predicate) {
        return methods().stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedTypeConfigurator$1PpgnCbbxHeqAUPXRRiyEVEEZTc
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean test;
                test = predicate.test(((AnnotatedMethodConfigurator) obj).getAnnotated());
                return test;
            }
        });
    }

    default Stream<AnnotatedFieldConfigurator<? super T>> filterFields(final Predicate<AnnotatedField<? super T>> predicate) {
        return fields().stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedTypeConfigurator$n4TJHaiJmTilHKh0U4G-rHGmZd4
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean test;
                test = predicate.test(((AnnotatedFieldConfigurator) obj).getAnnotated());
                return test;
            }
        });
    }

    default Stream<AnnotatedConstructorConfigurator<T>> filterConstructors(final Predicate<AnnotatedConstructor<T>> predicate) {
        return constructors().stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedTypeConfigurator$4AW92lD9XSIh31t7OzWaNW-yl3M
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean test;
                test = predicate.test(((AnnotatedConstructorConfigurator) obj).getAnnotated());
                return test;
            }
        });
    }
}
