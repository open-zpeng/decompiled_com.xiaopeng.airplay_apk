package javax.enterprise.inject.se;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.enterprise.inject.spi.Extension;
/* loaded from: classes.dex */
public abstract class SeContainerInitializer {
    public abstract SeContainerInitializer addBeanClasses(Class<?>... clsArr);

    public abstract SeContainerInitializer addExtensions(Class<? extends Extension>... clsArr);

    public abstract SeContainerInitializer addExtensions(Extension... extensionArr);

    public abstract SeContainerInitializer addPackages(boolean z, Class<?>... clsArr);

    public abstract SeContainerInitializer addPackages(boolean z, Package... packageArr);

    public abstract SeContainerInitializer addPackages(Class<?>... clsArr);

    public abstract SeContainerInitializer addPackages(Package... packageArr);

    public abstract SeContainerInitializer addProperty(String str, Object obj);

    public abstract SeContainerInitializer disableDiscovery();

    public abstract SeContainerInitializer enableDecorators(Class<?>... clsArr);

    public abstract SeContainerInitializer enableInterceptors(Class<?>... clsArr);

    public abstract SeContainer initialize();

    public abstract SeContainerInitializer selectAlternativeStereotypes(Class<? extends Annotation>... clsArr);

    public abstract SeContainerInitializer selectAlternatives(Class<?>... clsArr);

    public abstract SeContainerInitializer setClassLoader(ClassLoader classLoader);

    public abstract SeContainerInitializer setProperties(Map<String, Object> map);

    public static SeContainerInitializer newInstance() {
        return findSeContainerInitializer();
    }

    private static SeContainerInitializer findSeContainerInitializer() {
        Iterator<SeContainerInitializer> iterator = ServiceLoader.load(SeContainerInitializer.class, SeContainerInitializer.class.getClassLoader()).iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("No valid CDI implementation found");
        }
        try {
            SeContainerInitializer result = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("Two or more CDI implementations found, only one is supported");
            }
            return result;
        } catch (ServiceConfigurationError e) {
            throw new IllegalStateException("Error while instantiating SeContainerInitializer", e);
        }
    }
}
