package org.eclipse.jetty.util.log;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
/* loaded from: classes.dex */
public class Slf4jLog extends AbstractLogger {
    private final org.slf4j.Logger _logger;

    public Slf4jLog() throws Exception {
        this("org.eclipse.jetty.util.log");
    }

    public Slf4jLog(String name) {
        LocationAwareLogger logger = LoggerFactory.getLogger(name);
        if (logger instanceof LocationAwareLogger) {
            this._logger = new JettyAwareLogger(logger);
        } else {
            this._logger = logger;
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public String getName() {
        return this._logger.getName();
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Object... args) {
        this._logger.warn(msg, args);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(Throwable thrown) {
        warn("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Throwable thrown) {
        this._logger.warn(msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Object... args) {
        this._logger.info(msg, args);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(Throwable thrown) {
        info("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Throwable thrown) {
        this._logger.info(msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Object... args) {
        this._logger.debug(msg, args);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(Throwable thrown) {
        debug("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Throwable thrown) {
        this._logger.debug(msg, thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public boolean isDebugEnabled() {
        return this._logger.isDebugEnabled();
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void setDebugEnabled(boolean enabled) {
        warn("setDebugEnabled not implemented", null, null);
    }

    @Override // org.eclipse.jetty.util.log.AbstractLogger
    protected Logger newLogger(String fullname) {
        return new Slf4jLog(fullname);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void ignore(Throwable ignored) {
        if (Log.isIgnored()) {
            warn(Log.IGNORED, ignored);
        }
    }

    public String toString() {
        return this._logger.toString();
    }
}
