package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;
/* loaded from: classes.dex */
public interface BeanConfigurator<T> {
    BeanConfigurator<T> addInjectionPoint(InjectionPoint injectionPoint);

    BeanConfigurator<T> addInjectionPoints(Set<InjectionPoint> set);

    BeanConfigurator<T> addInjectionPoints(InjectionPoint... injectionPointArr);

    BeanConfigurator<T> addQualifier(Annotation annotation);

    BeanConfigurator<T> addQualifiers(Set<Annotation> set);

    BeanConfigurator<T> addQualifiers(Annotation... annotationArr);

    BeanConfigurator<T> addStereotype(Class<? extends Annotation> cls);

    BeanConfigurator<T> addStereotypes(Set<Class<? extends Annotation>> set);

    BeanConfigurator<T> addTransitiveTypeClosure(Type type);

    BeanConfigurator<T> addType(Type type);

    BeanConfigurator<T> addType(TypeLiteral<?> typeLiteral);

    BeanConfigurator<T> addTypes(Set<Type> set);

    BeanConfigurator<T> addTypes(Type... typeArr);

    BeanConfigurator<T> alternative(boolean z);

    BeanConfigurator<T> beanClass(Class<?> cls);

    <U extends T> BeanConfigurator<U> createWith(Function<CreationalContext<U>, U> function);

    BeanConfigurator<T> destroyWith(BiConsumer<T, CreationalContext<T>> biConsumer);

    BeanConfigurator<T> disposeWith(BiConsumer<T, Instance<Object>> biConsumer);

    BeanConfigurator<T> id(String str);

    BeanConfigurator<T> injectionPoints(Set<InjectionPoint> set);

    BeanConfigurator<T> injectionPoints(InjectionPoint... injectionPointArr);

    BeanConfigurator<T> name(String str);

    <U extends T> BeanConfigurator<U> produceWith(Function<Instance<Object>, U> function);

    BeanConfigurator<T> qualifiers(Set<Annotation> set);

    BeanConfigurator<T> qualifiers(Annotation... annotationArr);

    <U extends T> BeanConfigurator<U> read(AnnotatedType<U> annotatedType);

    BeanConfigurator<T> read(BeanAttributes<?> beanAttributes);

    BeanConfigurator<T> scope(Class<? extends Annotation> cls);

    BeanConfigurator<T> stereotypes(Set<Class<? extends Annotation>> set);

    BeanConfigurator<T> types(Set<Type> set);

    BeanConfigurator<T> types(Type... typeArr);
}
