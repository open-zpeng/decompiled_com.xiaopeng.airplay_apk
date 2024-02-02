package org.eclipse.jetty.servlet.listener;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ELContextCleaner implements ServletContextListener {
    private static final Logger LOG = Log.getLogger(ELContextCleaner.class);

    @Override // javax.servlet.ServletContextListener
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override // javax.servlet.ServletContextListener
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            Class beanELResolver = Loader.loadClass(getClass(), "javax.el.BeanELResolver");
            Field field = getField(beanELResolver);
            purgeEntries(field);
            LOG.info("javax.el.BeanELResolver purged", new Object[0]);
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e2) {
            LOG.warn("Cannot purge classes from javax.el.BeanELResolver", e2);
        } catch (IllegalArgumentException e3) {
            LOG.warn("Cannot purge classes from javax.el.BeanELResolver", e3);
        } catch (NoSuchFieldException e4) {
            LOG.info("Not cleaning cached beans: no such field javax.el.BeanELResolver.properties", new Object[0]);
        } catch (SecurityException e5) {
            LOG.warn("Cannot purge classes from javax.el.BeanELResolver", e5);
        }
    }

    protected Field getField(Class beanELResolver) throws SecurityException, NoSuchFieldException {
        if (beanELResolver == null) {
            return null;
        }
        return beanELResolver.getDeclaredField("properties");
    }

    protected void purgeEntries(Field properties) throws IllegalArgumentException, IllegalAccessException {
        if (properties == null) {
            return;
        }
        if (!properties.isAccessible()) {
            properties.setAccessible(true);
        }
        Map map = (Map) properties.get(null);
        if (map == null) {
            return;
        }
        Iterator<Class> itor = map.keySet().iterator();
        while (itor.hasNext()) {
            Class clazz = itor.next();
            Logger logger = LOG;
            logger.info("Clazz: " + clazz + " loaded by " + clazz.getClassLoader(), new Object[0]);
            if (Thread.currentThread().getContextClassLoader().equals(clazz.getClassLoader())) {
                itor.remove();
                LOG.info("removed", new Object[0]);
            } else {
                Logger logger2 = LOG;
                logger2.info("not removed: contextclassloader=" + Thread.currentThread().getContextClassLoader() + "clazz's classloader=" + clazz.getClassLoader(), new Object[0]);
            }
        }
    }
}
