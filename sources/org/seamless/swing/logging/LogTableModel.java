package org.seamless.swing.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/* loaded from: classes.dex */
public class LogTableModel extends AbstractTableModel {
    protected int maxAgeSeconds;
    protected boolean paused = false;
    protected List<LogMessage> messages = new ArrayList();

    public LogTableModel(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public int getMaxAgeSeconds() {
        return this.maxAgeSeconds;
    }

    public void setMaxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public synchronized void pushMessage(LogMessage message) {
        if (this.paused) {
            return;
        }
        if (this.maxAgeSeconds != Integer.MAX_VALUE) {
            Iterator<LogMessage> it = this.messages.iterator();
            long currentTime = new Date().getTime();
            while (it.hasNext()) {
                LogMessage logMessage = it.next();
                long delta = this.maxAgeSeconds * 1000;
                if (logMessage.getCreatedOn().longValue() + delta < currentTime) {
                    it.remove();
                }
            }
        }
        this.messages.add(message);
        fireTableDataChanged();
    }

    public Object getValueAt(int row, int column) {
        return this.messages.get(row);
    }

    public void clearMessages() {
        this.messages.clear();
        fireTableDataChanged();
    }

    public int getRowCount() {
        return this.messages.size();
    }

    public int getColumnCount() {
        return 5;
    }

    public Class<?> getColumnClass(int i) {
        return LogMessage.class;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "";
            case 1:
                return "Time";
            case 2:
                return "Thread";
            case 3:
                return "Source";
            default:
                return "Message";
        }
    }
}
