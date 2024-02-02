package org.eclipse.jetty.util.log;

import java.io.PrintStream;
import java.security.AccessControlException;
import java.util.Properties;
import org.eclipse.jetty.util.DateCache;
/* loaded from: classes.dex */
public class StdErrLog extends AbstractLogger {
    public static final int LEVEL_ALL = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARN = 3;
    private static DateCache _dateCache;
    private final String _abbrevname;
    private int _configuredLevel;
    private boolean _hideStacks;
    private int _level;
    private final String _name;
    private boolean _printLongNames;
    private boolean _source;
    private PrintStream _stderr;
    private static final String EOL = System.getProperty("line.separator");
    private static final Properties __props = new Properties();
    private static final boolean __source = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.SOURCE", Log.__props.getProperty("org.eclipse.jetty.util.log.stderr.SOURCE", "false")));
    private static final boolean __long = Boolean.parseBoolean(Log.__props.getProperty("org.eclipse.jetty.util.log.stderr.LONG", "false"));

    static {
        __props.putAll(Log.__props);
        String[] deprecatedProperties = {"DEBUG", "org.eclipse.jetty.util.log.DEBUG", "org.eclipse.jetty.util.log.stderr.DEBUG"};
        for (String deprecatedProp : deprecatedProperties) {
            if (System.getProperty(deprecatedProp) != null) {
                System.err.printf("System Property [%s] has been deprecated! (Use org.eclipse.jetty.LEVEL=DEBUG instead)%n", deprecatedProp);
            }
        }
        try {
            _dateCache = new DateCache("yyyy-MM-dd HH:mm:ss");
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    public StdErrLog() {
        this(null);
    }

    public StdErrLog(String name) {
        this(name, __props);
    }

    public StdErrLog(String name, Properties props) {
        this._level = 2;
        this._stderr = null;
        this._source = __source;
        this._printLongNames = __long;
        this._hideStacks = false;
        if (props != null && props != __props) {
            __props.putAll(props);
        }
        this._name = name == null ? "" : name;
        this._abbrevname = condensePackageString(this._name);
        this._level = getLoggingLevel(props, this._name);
        this._configuredLevel = this._level;
        try {
            this._source = Boolean.parseBoolean(props.getProperty(this._name + ".SOURCE", Boolean.toString(this._source)));
        } catch (AccessControlException e) {
            this._source = __source;
        }
    }

    public static int getLoggingLevel(Properties props, String name) {
        String nameSegment = name;
        while (nameSegment != null && nameSegment.length() > 0) {
            String levelStr = props.getProperty(nameSegment + ".LEVEL");
            int level = getLevelId(nameSegment + ".LEVEL", levelStr);
            if (level != -1) {
                return level;
            }
            int idx = nameSegment.lastIndexOf(46);
            if (idx >= 0) {
                nameSegment = nameSegment.substring(0, idx);
            } else {
                nameSegment = null;
            }
        }
        return getLevelId("log.LEVEL", props.getProperty("log.LEVEL", "INFO"));
    }

    protected static int getLevelId(String levelSegment, String levelName) {
        if (levelName == null) {
            return -1;
        }
        String levelStr = levelName.trim();
        if ("ALL".equalsIgnoreCase(levelStr)) {
            return 0;
        }
        if ("DEBUG".equalsIgnoreCase(levelStr)) {
            return 1;
        }
        if ("INFO".equalsIgnoreCase(levelStr)) {
            return 2;
        }
        if ("WARN".equalsIgnoreCase(levelStr)) {
            return 3;
        }
        PrintStream printStream = System.err;
        printStream.println("Unknown StdErrLog level [" + levelSegment + "]=[" + levelStr + "], expecting only [ALL, DEBUG, INFO, WARN] as values.");
        return -1;
    }

    protected static String condensePackageString(String classname) {
        String[] parts = classname.split("\\.");
        StringBuilder dense = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            dense.append(parts[i].charAt(0));
        }
        if (dense.length() > 0) {
            dense.append('.');
        }
        dense.append(parts[parts.length - 1]);
        return dense.toString();
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public String getName() {
        return this._name;
    }

    public void setPrintLongNames(boolean printLongNames) {
        this._printLongNames = printLongNames;
    }

    public boolean isPrintLongNames() {
        return this._printLongNames;
    }

    public boolean isHideStacks() {
        return this._hideStacks;
    }

    public void setHideStacks(boolean hideStacks) {
        this._hideStacks = hideStacks;
    }

    public boolean isSource() {
        return this._source;
    }

    public void setSource(boolean source) {
        this._source = source;
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Object... args) {
        if (this._level <= 3) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":WARN:", msg, args);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(Throwable thrown) {
        warn("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void warn(String msg, Throwable thrown) {
        if (this._level <= 3) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":WARN:", msg, thrown);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Object... args) {
        if (this._level <= 2) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":INFO:", msg, args);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(Throwable thrown) {
        info("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void info(String msg, Throwable thrown) {
        if (this._level <= 2) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":INFO:", msg, thrown);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public boolean isDebugEnabled() {
        return this._level <= 1;
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void setDebugEnabled(boolean enabled) {
        if (enabled) {
            this._level = 1;
            for (Logger log : Log.getLoggers().values()) {
                if (log.getName().startsWith(getName()) && (log instanceof StdErrLog)) {
                    ((StdErrLog) log).setLevel(1);
                }
            }
            return;
        }
        this._level = this._configuredLevel;
        for (Logger log2 : Log.getLoggers().values()) {
            if (log2.getName().startsWith(getName()) && (log2 instanceof StdErrLog)) {
                ((StdErrLog) log2).setLevel(((StdErrLog) log2)._configuredLevel);
            }
        }
    }

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public void setStdErrStream(PrintStream stream) {
        this._stderr = stream == System.err ? null : stream;
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Object... args) {
        if (this._level <= 1) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":DBUG:", msg, args);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(Throwable thrown) {
        debug("", thrown);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void debug(String msg, Throwable thrown) {
        if (this._level <= 1) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":DBUG:", msg, thrown);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }

    private void format(StringBuilder buffer, String level, String msg, Object... args) {
        String d = _dateCache.now();
        int ms = _dateCache.lastMs();
        tag(buffer, d, ms, level);
        format(buffer, msg, args);
    }

    private void format(StringBuilder buffer, String level, String msg, Throwable thrown) {
        format(buffer, level, msg, new Object[0]);
        if (isHideStacks()) {
            format(buffer, String.valueOf(thrown), new Object[0]);
        } else {
            format(buffer, thrown);
        }
    }

    private void tag(StringBuilder buffer, String d, int ms, String tag) {
        buffer.setLength(0);
        buffer.append(d);
        if (ms > 99) {
            buffer.append('.');
        } else if (ms > 9) {
            buffer.append(".0");
        } else {
            buffer.append(".00");
        }
        buffer.append(ms);
        buffer.append(tag);
        if (this._printLongNames) {
            buffer.append(this._name);
        } else {
            buffer.append(this._abbrevname);
        }
        buffer.append(':');
        if (this._source) {
            Throwable source = new Throwable();
            StackTraceElement[] frames = source.getStackTrace();
            for (StackTraceElement frame : frames) {
                String clazz = frame.getClassName();
                if (!clazz.equals(StdErrLog.class.getName()) && !clazz.equals(Log.class.getName())) {
                    if (!this._printLongNames && clazz.startsWith("org.eclipse.jetty.")) {
                        buffer.append(condensePackageString(clazz));
                    } else {
                        buffer.append(clazz);
                    }
                    buffer.append('#');
                    buffer.append(frame.getMethodName());
                    if (frame.getFileName() != null) {
                        buffer.append('(');
                        buffer.append(frame.getFileName());
                        buffer.append(':');
                        buffer.append(frame.getLineNumber());
                        buffer.append(')');
                    }
                    buffer.append(':');
                    return;
                }
            }
        }
    }

    private void format(StringBuilder builder, String msg, Object... args) {
        if (msg == null) {
            String msg2 = "";
            for (int i = 0; i < args.length; i++) {
                msg2 = msg2 + "{} ";
            }
            msg = msg2;
        }
        int start = 0;
        for (Object arg : args) {
            int bracesIndex = msg.indexOf("{}", start);
            if (bracesIndex < 0) {
                escape(builder, msg.substring(start));
                builder.append(" ");
                builder.append(arg);
                start = msg.length();
            } else {
                escape(builder, msg.substring(start, bracesIndex));
                builder.append(String.valueOf(arg));
                int start2 = bracesIndex + "{}".length();
                start = start2;
            }
        }
        escape(builder, msg.substring(start));
    }

    private void escape(StringBuilder builder, String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isISOControl(c)) {
                if (c == '\n') {
                    builder.append('|');
                } else if (c == '\r') {
                    builder.append('<');
                } else {
                    builder.append('?');
                }
            } else {
                builder.append(c);
            }
        }
    }

    private void format(StringBuilder buffer, Throwable thrown) {
        if (thrown == null) {
            buffer.append("null");
            return;
        }
        buffer.append(EOL);
        format(buffer, thrown.toString(), new Object[0]);
        StackTraceElement[] elements = thrown.getStackTrace();
        for (int i = 0; elements != null && i < elements.length; i++) {
            buffer.append(EOL);
            buffer.append("\tat ");
            format(buffer, elements[i].toString(), new Object[0]);
        }
        Throwable cause = thrown.getCause();
        if (cause != null && cause != thrown) {
            buffer.append(EOL);
            buffer.append("Caused by: ");
            format(buffer, cause);
        }
    }

    @Override // org.eclipse.jetty.util.log.AbstractLogger
    protected Logger newLogger(String fullname) {
        StdErrLog logger = new StdErrLog(fullname);
        logger.setPrintLongNames(this._printLongNames);
        logger.setSource(this._source);
        logger._stderr = this._stderr;
        if (this._level != this._configuredLevel) {
            logger._level = this._level;
        }
        return logger;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("StdErrLog:");
        s.append(this._name);
        s.append(":LEVEL=");
        switch (this._level) {
            case 0:
                s.append("ALL");
                break;
            case 1:
                s.append("DEBUG");
                break;
            case 2:
                s.append("INFO");
                break;
            case 3:
                s.append("WARN");
                break;
            default:
                s.append("?");
                break;
        }
        return s.toString();
    }

    public static void setProperties(Properties props) {
        __props.clear();
        __props.putAll(props);
    }

    @Override // org.eclipse.jetty.util.log.Logger
    public void ignore(Throwable ignored) {
        if (this._level <= 0) {
            StringBuilder buffer = new StringBuilder(64);
            format(buffer, ":IGNORED:", "", ignored);
            (this._stderr == null ? System.err : this._stderr).println(buffer);
        }
    }
}
