package javax.enterprise.inject.spi;

import java.util.List;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
/* loaded from: classes.dex */
public interface AfterTypeDiscovery {
    <T> AnnotatedTypeConfigurator<T> addAnnotatedType(Class<T> cls, String str);

    void addAnnotatedType(AnnotatedType<?> annotatedType, String str);

    List<Class<?>> getAlternatives();

    List<Class<?>> getDecorators();

    List<Class<?>> getInterceptors();
}
