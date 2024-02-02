package org.eclipse.jetty.util.log;

import java.lang.reflect.Method;
/* loaded from: classes.dex */
public class LoggerLog extends AbstractLogger {
    private volatile boolean _debug;
    private final Method _debugMAA;
    private final Method _debugMT;
    private final Method _getLoggerN;
    private final Method _getName;
    private final Method _infoMAA;
    private final Method _infoMT;
    private final Object _logger;
    private final Method _setDebugEnabledE;
    private final Method _warnMAA;
    private final Method _warnMT;

    public LoggerLog(Object logger) {
        try {
            this._logger = logger;
            Class<?> lc = logger.getClass();
            this._debugMT = lc.getMethod("debug", String.class, Throwable.class);
            this._debugMAA = lc.getMethod("debug", String.class, Object[].class);
            this._infoMT = lc.getMethod("info", String.class, Throwable.class);
            this._infoMAA = lc.getMethod("info", String.class, Object[].class);
            this._warnMT = lc.getMethod("warn", String.class, Throwable.class);
            this._warnMAA = lc.getMethod("warn", String.class, Object[].class);
            Method _isDebugEnabled = lc.getMethod("isDebugEnabled", new Class[0]);
            this._setDebugEnabledE = lc.getMethod("setDebugEnabled", Boolean.TYPE);
            this._getLoggerN = lc.getMethod("getLogger", String.class);
            this._getName = lc.getMethod("getName", new Class[0]);
            this._debug = ((Boolean) _isDebugEnabled.invoke(this._logger, new Object[0])).booleanValue();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public String getName() {
        try {
            return (String) this._getName.invoke(this._logger, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Object... args) {
        try {
            this._warnMAA.invoke(this._logger, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(Throwable thrown) {
        warn("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Throwable thrown) {
        try {
            this._warnMT.invoke(this._logger, msg, thrown);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Object... args) {
        try {
            this._infoMAA.invoke(this._logger, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(Throwable thrown) {
        info("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Throwable thrown) {
        try {
            this._infoMT.invoke(this._logger, msg, thrown);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public boolean isDebugEnabled() {
        return this._debug;
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void setDebugEnabled(boolean enabled) {
        try {
            this._setDebugEnabledE.invoke(this._logger, Boolean.valueOf(enabled));
            this._debug = enabled;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Object... args) {
        if (!this._debug) {
            return;
        }
        try {
            this._debugMAA.invoke(this._logger, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(Throwable thrown) {
        debug("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Throwable th) {
        if (!this._debug) {
            return;
        }
        try {
            this._debugMT.invoke(this._logger, msg, th);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void ignore(Throwable ignored) {
        if (Log.isIgnored()) {
            warn(Log.IGNORED, ignored);
        }
    }

    @Override // org.eclipse.jetty.util.log.AbstractLogger
    protected Logger newLogger(String fullname) {
        try {
            Object logger = this._getLoggerN.invoke(this._logger, fullname);
            return new LoggerLog(logger);
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }
}
