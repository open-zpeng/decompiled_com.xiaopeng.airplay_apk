package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.EventContext;
import javax.enterprise.inject.spi.ObserverMethod;
/* loaded from: classes.dex */
public interface ObserverMethodConfigurator<T> {

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface EventConsumer<T> {
        void accept(EventContext<T> eventContext) throws Exception;
    }

    ObserverMethodConfigurator<T> addQualifier(Annotation annotation);

    ObserverMethodConfigurator<T> addQualifiers(Set<Annotation> set);

    ObserverMethodConfigurator<T> addQualifiers(Annotation... annotationArr);

    ObserverMethodConfigurator<T> async(boolean z);

    ObserverMethodConfigurator<T> beanClass(Class<?> cls);

    ObserverMethodConfigurator<T> notifyWith(EventConsumer<T> eventConsumer);

    ObserverMethodConfigurator<T> observedType(Type type);

    ObserverMethodConfigurator<T> priority(int i);

    ObserverMethodConfigurator<T> qualifiers(Set<Annotation> set);

    ObserverMethodConfigurator<T> qualifiers(Annotation... annotationArr);

    ObserverMethodConfigurator<T> read(Method method);

    ObserverMethodConfigurator<T> read(AnnotatedMethod<?> annotatedMethod);

    ObserverMethodConfigurator<T> read(ObserverMethod<T> observerMethod);

    ObserverMethodConfigurator<T> reception(Reception reception);

    ObserverMethodConfigurator<T> transactionPhase(TransactionPhase transactionPhase);
}
