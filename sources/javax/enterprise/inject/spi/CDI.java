package javax.enterprise.inject.spi;

import java.util.Collections;
import java.util.Comparator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.enterprise.inject.Instance;
/* loaded from: classes.dex */
public abstract class CDI<T> implements Instance<T> {
    private static final Object lock = new Object();
    protected static volatile Set<CDIProvider> discoveredProviders = null;
    protected static volatile CDIProvider configuredProvider = null;

    public abstract BeanManager getBeanManager();

    public static CDI<Object> current() {
        return getCDIProvider().getCDI();
    }

    private static CDIProvider getCDIProvider() {
        if (configuredProvider != null) {
            return configuredProvider;
        }
        if (discoveredProviders == null) {
            synchronized (lock) {
                if (discoveredProviders == null) {
                    findAllProviders();
                }
            }
        }
        configuredProvider = discoveredProviders.stream().filter(new Predicate() { // from class: javax.enterprise.inject.spi.-$$Lambda$CDI$VtMQE4gy6Be8hLi63ycJkPbhEyY
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return CDI.lambda$getCDIProvider$0((CDIProvider) obj);
            }
        }).findFirst().orElseThrow(new Supplier() { // from class: javax.enterprise.inject.spi.-$$Lambda$CDI$vlharCOP3lgtHJi9f7BzID95uUk
            @Override // java.util.function.Supplier
            public final Object get() {
                return CDI.lambda$getCDIProvider$1();
            }
        });
        return configuredProvider;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$getCDIProvider$0(CDIProvider c) {
        return c.getCDI() != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ IllegalStateException lambda$getCDIProvider$1() {
        return new IllegalStateException("Unable to access CDI");
    }

    public static void setCDIProvider(CDIProvider provider) {
        if (provider != null) {
            configuredProvider = provider;
            return;
        }
        throw new IllegalStateException("CDIProvider must not be null");
    }

    private static void findAllProviders() {
        final Set<CDIProvider> providers = new TreeSet<>((Comparator<? super CDIProvider>) Comparator.comparingInt(new ToIntFunction() { // from class: javax.enterprise.inject.spi.-$$Lambda$hrUPzmEpZRtVa-24-oiOCMDXb84
            @Override // java.util.function.ToIntFunction
            public final int applyAsInt(Object obj) {
                return ((CDIProvider) obj).getPriority();
            }
        }).reversed());
        ServiceLoader<CDIProvider> providerLoader = ServiceLoader.load(CDIProvider.class, CDI.class.getClassLoader());
        if (!providerLoader.iterator().hasNext()) {
            throw new IllegalStateException("Unable to locate CDIProvider");
        }
        try {
            providers.getClass();
            providerLoader.forEach(new Consumer() { // from class: javax.enterprise.inject.spi.-$$Lambda$PRucj68iY_HdOkW5gVPa4MNlkSs
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    providers.add((CDIProvider) obj);
                }
            });
            discoveredProviders = Collections.unmodifiableSet(providers);
        } catch (ServiceConfigurationError e) {
            throw new IllegalStateException(e);
        }
    }
}
