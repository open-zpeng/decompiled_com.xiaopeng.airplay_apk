package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import javax.enterprise.inject.spi.AnnotatedParameter;
/* loaded from: classes.dex */
public interface AnnotatedParameterConfigurator<T> {
    AnnotatedParameterConfigurator<T> add(Annotation annotation);

    AnnotatedParameter<T> getAnnotated();

    AnnotatedParameterConfigurator<T> remove(Predicate<Annotation> predicate);

    static /* synthetic */ boolean lambda$removeAll$0(Annotation a) {
        return true;
    }

    default AnnotatedParameterConfigurator<T> removeAll() {
        return remove(new Predicate() { // from class: javax.enterprise.inject.spi.configurator.-$$Lambda$AnnotatedParameterConfigurator$EPyK4pCOu90G4BwGtODehNvn9xM
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AnnotatedParameterConfigurator.lambda$removeAll$0((Annotation) obj);
            }
        });
    }
}
