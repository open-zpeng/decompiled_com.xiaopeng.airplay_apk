package javax.enterprise.context.spi;
/* loaded from: classes.dex */
public interface CreationalContext<T> {
    void push(T t);

    void release();
}
