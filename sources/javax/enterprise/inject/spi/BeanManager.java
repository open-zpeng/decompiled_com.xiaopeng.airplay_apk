package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
/* loaded from: classes.dex */
public interface BeanManager {
    boolean areInterceptorBindingsEquivalent(Annotation annotation, Annotation annotation2);

    boolean areQualifiersEquivalent(Annotation annotation, Annotation annotation2);

    <T> AnnotatedType<T> createAnnotatedType(Class<T> cls);

    <T> Bean<T> createBean(BeanAttributes<T> beanAttributes, Class<T> cls, InjectionTargetFactory<T> injectionTargetFactory);

    <T, X> Bean<T> createBean(BeanAttributes<T> beanAttributes, Class<X> cls, ProducerFactory<X> producerFactory);

    BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> annotatedMember);

    <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> annotatedType);

    <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual);

    InjectionPoint createInjectionPoint(AnnotatedField<?> annotatedField);

    InjectionPoint createInjectionPoint(AnnotatedParameter<?> annotatedParameter);

    <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> annotatedType);

    Instance<Object> createInstance();

    <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> creationalContext, Class<T> cls);

    void fireEvent(Object obj, Annotation... annotationArr);

    Set<Bean<?>> getBeans(String str);

    Set<Bean<?>> getBeans(Type type, Annotation... annotationArr);

    Context getContext(Class<? extends Annotation> cls);

    ELResolver getELResolver();

    Event<Object> getEvent();

    <T extends Extension> T getExtension(Class<T> cls);

    Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> creationalContext);

    <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType);

    Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> cls);

    int getInterceptorBindingHashCode(Annotation annotation);

    Bean<?> getPassivationCapableBean(String str);

    <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> annotatedField, Bean<X> bean);

    <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> annotatedMethod, Bean<X> bean);

    int getQualifierHashCode(Annotation annotation);

    Object getReference(Bean<?> bean, Type type, CreationalContext<?> creationalContext);

    Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> cls);

    boolean isInterceptorBinding(Class<? extends Annotation> cls);

    boolean isNormalScope(Class<? extends Annotation> cls);

    boolean isPassivatingScope(Class<? extends Annotation> cls);

    boolean isQualifier(Class<? extends Annotation> cls);

    boolean isScope(Class<? extends Annotation> cls);

    boolean isStereotype(Class<? extends Annotation> cls);

    <X> Bean<? extends X> resolve(Set<Bean<? extends X>> set);

    List<Decorator<?>> resolveDecorators(Set<Type> set, Annotation... annotationArr);

    List<Interceptor<?>> resolveInterceptors(InterceptionType interceptionType, Annotation... annotationArr);

    <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T t, Annotation... annotationArr);

    void validate(InjectionPoint injectionPoint);

    ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory);
}
