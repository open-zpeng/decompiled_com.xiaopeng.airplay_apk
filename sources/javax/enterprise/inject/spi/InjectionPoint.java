package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;
/* loaded from: classes.dex */
public interface InjectionPoint {
    Annotated getAnnotated();

    Bean<?> getBean();

    Member getMember();

    Set<Annotation> getQualifiers();

    Type getType();

    boolean isDelegate();

    boolean isTransient();
}
