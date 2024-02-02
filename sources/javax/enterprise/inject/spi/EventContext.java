package javax.enterprise.inject.spi;
/* loaded from: classes.dex */
public interface EventContext<T> {
    T getEvent();

    EventMetadata getMetadata();
}
