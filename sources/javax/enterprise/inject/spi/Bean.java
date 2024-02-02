package javax.enterprise.inject.spi;

import java.util.Set;
import javax.enterprise.context.spi.Contextual;
/* loaded from: classes.dex */
public interface Bean<T> extends Contextual<T>, BeanAttributes<T> {
    Class<?> getBeanClass();

    Set<InjectionPoint> getInjectionPoints();

    boolean isNullable();
}
