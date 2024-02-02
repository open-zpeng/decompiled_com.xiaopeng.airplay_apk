package org.eclipse.jetty.util.log;

import java.util.logging.Level;
/* loaded from: classes.dex */
public class JavaUtilLog extends AbstractLogger {
    private java.util.logging.Logger _logger;
    private Level configuredLevel;

    public JavaUtilLog() {
        this("org.eclipse.jetty.util.log");
    }

    public JavaUtilLog(String name) {
        this._logger = java.util.logging.Logger.getLogger(name);
        if (Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.DEBUG", "false"))) {
            this._logger.setLevel(Level.FINE);
        }
        this.configuredLevel = this._logger.getLevel();
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public String getName() {
        return this._logger.getName();
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Object... args) {
        this._logger.log(Level.WARNING, format(msg, args));
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(Throwable thrown) {
        warn("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Throwable thrown) {
        this._logger.log(Level.WARNING, msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Object... args) {
        this._logger.log(Level.INFO, format(msg, args));
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(Throwable thrown) {
        info("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Throwable thrown) {
        this._logger.log(Level.INFO, msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public boolean isDebugEnabled() {
        return this._logger.isLoggable(Level.FINE);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void setDebugEnabled(boolean enabled) {
        if (enabled) {
            this.configuredLevel = this._logger.getLevel();
            this._logger.setLevel(Level.FINE);
            return;
        }
        this._logger.setLevel(this.configuredLevel);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Object... args) {
        this._logger.log(Level.FINE, format(msg, args));
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(Throwable thrown) {
        debug("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Throwable thrown) {
        this._logger.log(Level.FINE, msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.AbstractLogger
    protected Logger newLogger(String fullname) {
        return new JavaUtilLog(fullname);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void ignore(Throwable ignored) {
        if (Log.isIgnored()) {
            warn(Log.IGNORED, ignored);
        }
    }

    private String format(String msg, Object... args) {
        String msg2 = String.valueOf(msg);
        StringBuilder builder = new StringBuilder();
        int start = 0;
        for (Object arg : args) {
            int bracesIndex = msg2.indexOf("{}", start);
            if (bracesIndex < 0) {
                builder.append(msg2.substring(start));
                builder.append(" ");
                builder.append(arg);
                start = msg2.length();
            } else {
                builder.append(msg2.substring(start, bracesIndex));
                builder.append(String.valueOf(arg));
                int start2 = bracesIndex + "{}".length();
                start = start2;
            }
        }
        builder.append(msg2.substring(start));
        return builder.toString();
    }
}
