package javax.enterprise.event;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import javax.enterprise.util.TypeLiteral;
/* loaded from: classes.dex */
public interface Event<T> {
    void fire(T t);

    <U extends T> CompletionStage<U> fireAsync(U u);

    <U extends T> CompletionStage<U> fireAsync(U u, NotificationOptions notificationOptions);

    <U extends T> Event<U> select(Class<U> cls, Annotation... annotationArr);

    <U extends T> Event<U> select(TypeLiteral<U> typeLiteral, Annotation... annotationArr);

    Event<T> select(Annotation... annotationArr);
}
