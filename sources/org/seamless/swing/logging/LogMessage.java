package org.seamless.swing.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
/* loaded from: classes.dex */
public class LogMessage {
    private Long createdOn;
    private Level level;
    private String message;
    private String source;
    private String thread;

    public LogMessage(String message) {
        this(Level.INFO, message);
    }

    public LogMessage(String source, String message) {
        this(Level.INFO, source, message);
    }

    public LogMessage(Level level, String message) {
        this(level, null, message);
    }

    public LogMessage(Level level, String source, String message) {
        this.createdOn = Long.valueOf(new Date().getTime());
        this.thread = Thread.currentThread().getName();
        this.level = level;
        this.source = source;
        this.message = message;
    }

    public Level getLevel() {
        return this.level;
    }

    public Long getCreatedOn() {
        return this.createdOn;
    }

    public String getThread() {
        return this.thread;
    }

    public String getSource() {
        return this.source;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        return getLevel() + " - " + dateFormat.format(new Date(getCreatedOn().longValue())) + " - " + getThread() + " : " + getSource() + " : " + getMessage();
    }
}
