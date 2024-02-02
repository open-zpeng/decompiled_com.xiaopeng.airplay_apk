package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
/* loaded from: classes.dex */
public interface AnnotatedMethodConfigurator<T> {
    AnnotatedMethodConfigurator<T> add(Annotation annotation);

    AnnotatedMethod<T> getAnnotated();

    List<AnnotatedParameterConfigurator<T>> params();

    AnnotatedMethodConfigurator<T> remove(Predicate<Annotation> predicate);

    static /* synthetic */ boolean lambda$removeAll$0(Annotation a) {
        return true;
    }

    default AnnotatedMethodConfigurator<T> removeAll() {
        return remove(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedMethodConfigurator$_E8HKOoKaE_eFkP-_Jf6vGPown0
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AnnotatedMethodConfigurator.lambda$removeAll$0((Annotation) obj);
            }
        });
    }

    default Stream<AnnotatedParameterConfigurator<T>> filterParams(final Predicate<AnnotatedParameter<T>> predicate) {
        return params().stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedMethodConfigurator$ZCKLrokN_eEpBe9FGTLPjeElUtc
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean test;
                test = predicate.test(((AnnotatedParameterConfigurator) obj).getAnnotated());
                return test;
            }
        });
    }
}
