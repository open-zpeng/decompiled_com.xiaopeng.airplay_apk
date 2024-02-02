package javax.enterprise.inject.se;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
/* loaded from: classes.dex */
public interface SeContainer extends Instance<Object>, AutoCloseable {
    @Override // java.lang.AutoCloseable
    void close();

    BeanManager getBeanManager();

    boolean isRunning();
}
