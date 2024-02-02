package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
/* loaded from: classes.dex */
public interface ObserverMethod<T> extends Prioritized {
    public static final int DEFAULT_PRIORITY = 2500;

    Class<?> getBeanClass();

    Set<Annotation> getObservedQualifiers();

    Type getObservedType();

    Reception getReception();

    TransactionPhase getTransactionPhase();

    @Override // javax.enterprise.inject.spi.Prioritized
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

    default void notify(T event) {
    }

    default void notify(EventContext<T> eventContext) {
        notify((ObserverMethod<T>) eventContext.getEvent());
    }

    default boolean isAsync() {
        return false;
    }
}
