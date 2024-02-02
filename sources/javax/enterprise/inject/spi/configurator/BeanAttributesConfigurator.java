package javax.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import javax.enterprise.util.TypeLiteral;
/* loaded from: classes.dex */
public interface BeanAttributesConfigurator<T> {
    BeanAttributesConfigurator<T> addQualifier(Annotation annotation);

    BeanAttributesConfigurator<T> addQualifiers(Set<Annotation> set);

    BeanAttributesConfigurator<T> addQualifiers(Annotation... annotationArr);

    BeanAttributesConfigurator<T> addStereotype(Class<? extends Annotation> cls);

    BeanAttributesConfigurator<T> addStereotypes(Set<Class<? extends Annotation>> set);

    BeanAttributesConfigurator<T> addTransitiveTypeClosure(Type type);

    BeanAttributesConfigurator<T> addType(Type type);

    BeanAttributesConfigurator<T> addType(TypeLiteral<?> typeLiteral);

    BeanAttributesConfigurator<T> addTypes(Set<Type> set);

    BeanAttributesConfigurator<T> addTypes(Type... typeArr);

    BeanAttributesConfigurator<T> alternative(boolean z);

    BeanAttributesConfigurator<T> name(String str);

    BeanAttributesConfigurator<T> qualifiers(Set<Annotation> set);

    BeanAttributesConfigurator<T> qualifiers(Annotation... annotationArr);

    BeanAttributesConfigurator<T> scope(Class<? extends Annotation> cls);

    BeanAttributesConfigurator<T> stereotypes(Set<Class<? extends Annotation>> set);

    BeanAttributesConfigurator<T> types(Set<Type> set);

    BeanAttributesConfigurator<T> types(Type... typeArr);
}
