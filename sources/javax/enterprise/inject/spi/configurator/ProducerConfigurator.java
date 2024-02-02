package javax.enterprise.inject.spi.configurator;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.enterprise.context.spi.CreationalContext;
/* loaded from: classes.dex */
public interface ProducerConfigurator<T> {
    ProducerConfigurator<T> disposeWith(Consumer<T> consumer);

    <U extends T> ProducerConfigurator<T> produceWith(Function<CreationalContext<U>, U> function);
}
