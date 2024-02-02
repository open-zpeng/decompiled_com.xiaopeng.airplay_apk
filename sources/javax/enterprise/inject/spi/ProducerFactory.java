package javax.enterprise.inject.spi;
/* loaded from: classes.dex */
public interface ProducerFactory<X> {
    <T> Producer<T> createProducer(Bean<T> bean);
}
