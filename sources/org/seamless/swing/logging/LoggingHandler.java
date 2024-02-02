package org.seamless.swing.logging;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
/* loaded from: classes.dex */
public abstract class LoggingHandler extends Handler {
    public int sourcePathElements;

    protected abstract void log(LogMessage logMessage);

    public LoggingHandler() {
        this.sourcePathElements = 3;
    }

    public LoggingHandler(int sourcePathElements) {
        this.sourcePathElements = 3;
        this.sourcePathElements = sourcePathElements;
    }

    @Override // java.util.logging.Handler
    public void publish(LogRecord logRecord) {
        LogMessage logMessage = new LogMessage(logRecord.getLevel(), getSource(logRecord), logRecord.getMessage());
        log(logMessage);
    }

    @Override // java.util.logging.Handler
    public void flush() {
    }

    @Override // java.util.logging.Handler
    public void close() throws SecurityException {
    }

    protected String getSource(LogRecord record) {
        StringBuilder sb = new StringBuilder(180);
        String[] split = record.getSourceClassName().split("\\.");
        if (split.length > this.sourcePathElements) {
            split = (String[]) Arrays.copyOfRange(split, split.length - this.sourcePathElements, split.length);
        }
        for (String s : split) {
            sb.append(s);
            sb.append(".");
        }
        sb.append(record.getSourceMethodName());
        return sb.toString();
    }
}
