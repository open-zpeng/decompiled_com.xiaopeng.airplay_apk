package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
/* loaded from: classes.dex */
public interface AnnotatedConstructorConfigurator<T> {
    AnnotatedConstructorConfigurator<T> add(Annotation annotation);

    AnnotatedConstructor<T> getAnnotated();

    List<AnnotatedParameterConfigurator<T>> params();

    AnnotatedConstructorConfigurator<T> remove(Predicate<Annotation> predicate);

    static /* synthetic */ boolean lambda$removeAll$0(Annotation a) {
        return true;
    }

    default AnnotatedConstructorConfigurator<T> removeAll() {
        return remove(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedConstructorConfigurator$l5DGkwG1wJDlpKyUrl0Mjd6xIxk
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AnnotatedConstructorConfigurator.lambda$removeAll$0((Annotation) obj);
            }
        });
    }

    default Stream<AnnotatedParameterConfigurator<T>> filterParams(final Predicate<AnnotatedParameter<T>> predicate) {
        return params().stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedConstructorConfigurator$75-ROUu6HlK4rym2HzAadCMwj3c
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean test;
                test = predicate.test(((AnnotatedParameterConfigurator) obj).getAnnotated());
                return test;
            }
        });
    }
}
