package org.eclipse.jetty.util.log;
/* loaded from: classes.dex */
public abstract class AbstractLogger implements Logger {
    protected abstract Logger newLogger(String str);

    @Override // org.eclipse.jetty.util.log.Logger
    public final Logger getLogger(String name) {
        String fullname;
        if (isBlank(name)) {
            return this;
        }
        String basename = getName();
        if (isBlank(basename) || Log.getRootLogger() == this) {
            fullname = name;
        } else {
            fullname = basename + "." + name;
        }
        Logger logger = Log.getLoggers().get(fullname);
        if (logger == null) {
            Logger newlog = newLogger(fullname);
            Logger logger2 = Log.getMutableLoggers().putIfAbsent(fullname, newlog);
            if (logger2 == null) {
                return newlog;
            }
            return logger2;
        }
        return logger;
    }

    private static boolean isBlank(String name) {
        if (name == null) {
            return true;
        }
        int size = name.length();
        for (int i = 0; i < size; i++) {
            char c = name.charAt(i);
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
}
