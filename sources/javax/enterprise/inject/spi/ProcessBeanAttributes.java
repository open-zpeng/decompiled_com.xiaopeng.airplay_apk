package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.BeanAttributesConfigurator;
/* loaded from: classes.dex */
public interface ProcessBeanAttributes<T> {
    void addDefinitionError(Throwable th);

    BeanAttributesConfigurator<T> configureBeanAttributes();

    Annotated getAnnotated();

    BeanAttributes<T> getBeanAttributes();

    void ignoreFinalMethods();

    void setBeanAttributes(BeanAttributes<T> beanAttributes);

    void veto();
}
