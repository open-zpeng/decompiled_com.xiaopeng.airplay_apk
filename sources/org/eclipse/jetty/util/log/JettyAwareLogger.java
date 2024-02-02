package org.eclipse.jetty.util.log;

import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;
/* loaded from: classes.dex */
class JettyAwareLogger implements org.slf4j.Logger {
    private static final int DEBUG = 10;
    private static final int ERROR = 40;
    private static final String FQCN = Slf4jLog.class.getName();
    private static final int INFO = 20;
    private static final int TRACE = 0;
    private static final int WARN = 30;
    private final LocationAwareLogger _logger;

    public JettyAwareLogger(LocationAwareLogger logger) {
        this._logger = logger;
    }

    public String getName() {
        return this._logger.getName();
    }

    public boolean isTraceEnabled() {
        return this._logger.isTraceEnabled();
    }

    public void trace(String msg) {
        log(null, 0, msg, null, null);
    }

    public void trace(String format, Object arg) {
        log(null, 0, format, new Object[]{arg}, null);
    }

    public void trace(String format, Object arg1, Object arg2) {
        log(null, 0, format, new Object[]{arg1, arg2}, null);
    }

    public void trace(String format, Object[] argArray) {
        log(null, 0, format, argArray, null);
    }

    public void trace(String msg, Throwable t) {
        log(null, 0, msg, null, t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return this._logger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        log(marker, 0, msg, null, null);
    }

    public void trace(Marker marker, String format, Object arg) {
        log(marker, 0, format, new Object[]{arg}, null);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, 0, format, new Object[]{arg1, arg2}, null);
    }

    public void trace(Marker marker, String format, Object[] argArray) {
        log(marker, 0, format, argArray, null);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        log(marker, 0, msg, null, t);
    }

    public boolean isDebugEnabled() {
        return this._logger.isDebugEnabled();
    }

    public void debug(String msg) {
        log(null, 10, msg, null, null);
    }

    public void debug(String format, Object arg) {
        log(null, 10, format, new Object[]{arg}, null);
    }

    public void debug(String format, Object arg1, Object arg2) {
        log(null, 10, format, new Object[]{arg1, arg2}, null);
    }

    public void debug(String format, Object[] argArray) {
        log(null, 10, format, argArray, null);
    }

    public void debug(String msg, Throwable t) {
        log(null, 10, msg, null, t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return this._logger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        log(marker, 10, msg, null, null);
    }

    public void debug(Marker marker, String format, Object arg) {
        log(marker, 10, format, new Object[]{arg}, null);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, 10, format, new Object[]{arg1, arg2}, null);
    }

    public void debug(Marker marker, String format, Object[] argArray) {
        log(marker, 10, format, argArray, null);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        log(marker, 10, msg, null, t);
    }

    public boolean isInfoEnabled() {
        return this._logger.isInfoEnabled();
    }

    public void info(String msg) {
        log(null, 20, msg, null, null);
    }

    public void info(String format, Object arg) {
        log(null, 20, format, new Object[]{arg}, null);
    }

    public void info(String format, Object arg1, Object arg2) {
        log(null, 20, format, new Object[]{arg1, arg2}, null);
    }

    public void info(String format, Object[] argArray) {
        log(null, 20, format, argArray, null);
    }

    public void info(String msg, Throwable t) {
        log(null, 20, msg, null, t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return this._logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        log(marker, 20, msg, null, null);
    }

    public void info(Marker marker, String format, Object arg) {
        log(marker, 20, format, new Object[]{arg}, null);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, 20, format, new Object[]{arg1, arg2}, null);
    }

    public void info(Marker marker, String format, Object[] argArray) {
        log(marker, 20, format, argArray, null);
    }

    public void info(Marker marker, String msg, Throwable t) {
        log(marker, 20, msg, null, t);
    }

    public boolean isWarnEnabled() {
        return this._logger.isWarnEnabled();
    }

    public void warn(String msg) {
        log(null, 30, msg, null, null);
    }

    public void warn(String format, Object arg) {
        log(null, 30, format, new Object[]{arg}, null);
    }

    public void warn(String format, Object[] argArray) {
        log(null, 30, format, argArray, null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        log(null, 30, format, new Object[]{arg1, arg2}, null);
    }

    public void warn(String msg, Throwable t) {
        log(null, 30, msg, null, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return this._logger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        log(marker, 30, msg, null, null);
    }

    public void warn(Marker marker, String format, Object arg) {
        log(marker, 30, format, new Object[]{arg}, null);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, 30, format, new Object[]{arg1, arg2}, null);
    }

    public void warn(Marker marker, String format, Object[] argArray) {
        log(marker, 30, format, argArray, null);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        log(marker, 30, msg, null, t);
    }

    public boolean isErrorEnabled() {
        return this._logger.isErrorEnabled();
    }

    public void error(String msg) {
        log(null, 40, msg, null, null);
    }

    public void error(String format, Object arg) {
        log(null, 40, format, new Object[]{arg}, null);
    }

    public void error(String format, Object arg1, Object arg2) {
        log(null, 40, format, new Object[]{arg1, arg2}, null);
    }

    public void error(String format, Object[] argArray) {
        log(null, 40, format, argArray, null);
    }

    public void error(String msg, Throwable t) {
        log(null, 40, msg, null, t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return this._logger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        log(marker, 40, msg, null, null);
    }

    public void error(Marker marker, String format, Object arg) {
        log(marker, 40, format, new Object[]{arg}, null);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, 40, format, new Object[]{arg1, arg2}, null);
    }

    public void error(Marker marker, String format, Object[] argArray) {
        log(marker, 40, format, argArray, null);
    }

    public void error(Marker marker, String msg, Throwable t) {
        log(marker, 40, msg, null, t);
    }

    public String toString() {
        return this._logger.toString();
    }

    private void log(Marker marker, int level, String msg, Object[] argArray, Throwable t) {
        if (argArray == null) {
            this._logger.log(marker, FQCN, level, msg, (Object[]) null, t);
            return;
        }
        int loggerLevel = this._logger.isTraceEnabled() ? 0 : this._logger.isDebugEnabled() ? 10 : this._logger.isInfoEnabled() ? 20 : this._logger.isWarnEnabled() ? 30 : 40;
        if (loggerLevel <= level) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, argArray);
            this._logger.log(marker, FQCN, level, ft.getMessage(), (Object[]) null, t);
        }
    }
}
