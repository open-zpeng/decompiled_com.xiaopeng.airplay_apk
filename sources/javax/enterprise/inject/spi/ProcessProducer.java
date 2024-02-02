package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.ProducerConfigurator;
/* loaded from: classes.dex */
public interface ProcessProducer<T, X> {
    void addDefinitionError(Throwable th);

    ProducerConfigurator<X> configureProducer();

    AnnotatedMember<T> getAnnotatedMember();

    Producer<X> getProducer();

    void setProducer(Producer<X> producer);
}
