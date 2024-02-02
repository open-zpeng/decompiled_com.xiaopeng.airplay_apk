package javax.enterprise.inject.spi;
/* loaded from: classes.dex */
public interface CDIProvider extends Prioritized {
    public static final int DEFAULT_CDI_PROVIDER_PRIORITY = 0;

    CDI<Object> getCDI();

    @Override // javax.enterprise.inject.spi.Prioritized
    default int getPriority() {
        return 0;
    }
}
