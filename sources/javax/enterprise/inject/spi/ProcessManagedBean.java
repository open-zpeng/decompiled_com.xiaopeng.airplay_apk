package javax.enterprise.inject.spi;
/* loaded from: classes.dex */
public interface ProcessManagedBean<X> extends ProcessBean<X> {
    AnnotatedType<X> getAnnotatedBeanClass();
}
