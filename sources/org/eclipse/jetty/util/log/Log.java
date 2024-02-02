package org.eclipse.jetty.util.log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.Loader;
/* loaded from: classes.dex */
public class Log {
    public static final String EXCEPTION = "EXCEPTION ";
    public static final String IGNORED = "IGNORED ";
    private static Logger LOG;
    public static boolean __ignored;
    private static boolean __initialized;
    public static String __logClass;
    private static final ConcurrentMap<String, Logger> __loggers = new ConcurrentHashMap();
    protected static Properties __props = new Properties();

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() { // from class: org.eclipse.jetty.util.log.Log.1
            @Override // java.security.PrivilegedAction
            public Object run() {
                URL testProps = Loader.getResource(Log.class, "jetty-logging.properties", true);
                if (testProps != null) {
                    InputStream in = null;
                    try {
                        try {
                            in = testProps.openStream();
                            Log.__props.load(in);
                        } catch (IOException e) {
                            System.err.println("Unable to load " + testProps);
                            e.printStackTrace(System.err);
                        }
                    } finally {
                        IO.close(in);
                    }
                }
                Enumeration<?> propertyNames = System.getProperties().propertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = (String) propertyNames.nextElement();
                    String val = System.getProperty(key);
                    if (val != null) {
                        Log.__props.setProperty(key, val);
                    }
                }
                Log.__logClass = Log.__props.getProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
                Log.__ignored = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.IGNORED", "false"));
                return null;
            }
        });
    }

    public static boolean initialized() {
        boolean z = true;
        if (LOG != null) {
            return true;
        }
        synchronized (Log.class) {
            if (__initialized) {
                if (LOG == null) {
                    z = false;
                }
                return z;
            }
            __initialized = true;
            try {
                Class<?> log_class = Loader.loadClass(Log.class, __logClass);
                if (LOG == null || !LOG.getClass().equals(log_class)) {
                    LOG = (Logger) log_class.newInstance();
                    LOG.debug("Logging to {} via {}", LOG, log_class.getName());
                }
            } catch (Throwable e) {
                initStandardLogging(e);
            }
            return LOG != null;
        }
    }

    private static void initStandardLogging(Throwable e) {
        if (e != null && __ignored) {
            e.printStackTrace();
        }
        if (LOG == null) {
            LOG = new StdErrLog();
            LOG.debug("Logging to {} via {}", LOG, StdErrLog.class.getName());
        }
    }

    public static void setLog(Logger log) {
        LOG = log;
    }

    @Deprecated
    public static Logger getLog() {
        initialized();
        return LOG;
    }

    public static Logger getRootLogger() {
        initialized();
        return LOG;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isIgnored() {
        return __ignored;
    }

    public static void setLogToParent(String name) {
        ClassLoader loader = Log.class.getClassLoader();
        if (loader != null && loader.getParent() != null) {
            try {
                Class<?> uberlog = loader.getParent().loadClass("org.eclipse.jetty.util.log.Log");
                Method getLogger = uberlog.getMethod("getLogger", String.class);
                Object logger = getLogger.invoke(null, name);
                setLog(new LoggerLog(logger));
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        setLog(getLogger(name));
    }

    @Deprecated
    public static void debug(Throwable th) {
        if (!isDebugEnabled()) {
            return;
        }
        LOG.debug(EXCEPTION, th);
    }

    @Deprecated
    public static void debug(String msg) {
        if (!initialized()) {
            return;
        }
        LOG.debug(msg, new Object[0]);
    }

    @Deprecated
    public static void debug(String msg, Object arg) {
        if (!initialized()) {
            return;
        }
        LOG.debug(msg, arg);
    }

    @Deprecated
    public static void debug(String msg, Object arg0, Object arg1) {
        if (!initialized()) {
            return;
        }
        LOG.debug(msg, arg0, arg1);
    }

    @Deprecated
    public static void ignore(Throwable thrown) {
        if (!initialized()) {
            return;
        }
        LOG.ignore(thrown);
    }

    @Deprecated
    public static void info(String msg) {
        if (!initialized()) {
            return;
        }
        LOG.info(msg, new Object[0]);
    }

    @Deprecated
    public static void info(String msg, Object arg) {
        if (!initialized()) {
            return;
        }
        LOG.info(msg, arg);
    }

    @Deprecated
    public static void info(String msg, Object arg0, Object arg1) {
        if (!initialized()) {
            return;
        }
        LOG.info(msg, arg0, arg1);
    }

    @Deprecated
    public static boolean isDebugEnabled() {
        if (!initialized()) {
            return false;
        }
        return LOG.isDebugEnabled();
    }

    @Deprecated
    public static void warn(String msg) {
        if (!initialized()) {
            return;
        }
        LOG.warn(msg, new Object[0]);
    }

    @Deprecated
    public static void warn(String msg, Object arg) {
        if (!initialized()) {
            return;
        }
        LOG.warn(msg, arg);
    }

    @Deprecated
    public static void warn(String msg, Object arg0, Object arg1) {
        if (!initialized()) {
            return;
        }
        LOG.warn(msg, arg0, arg1);
    }

    @Deprecated
    public static void warn(String msg, Throwable th) {
        if (!initialized()) {
            return;
        }
        LOG.warn(msg, th);
    }

    @Deprecated
    public static void warn(Throwable th) {
        if (!initialized()) {
            return;
        }
        LOG.warn(EXCEPTION, th);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        if (!initialized()) {
            return null;
        }
        if (name == null) {
            return LOG;
        }
        Logger logger = __loggers.get(name);
        if (logger == null) {
            return LOG.getLogger(name);
        }
        return logger;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ConcurrentMap<String, Logger> getMutableLoggers() {
        return __loggers;
    }

    public static Map<String, Logger> getLoggers() {
        return Collections.unmodifiableMap(__loggers);
    }
}
