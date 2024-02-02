package org.seamless.swing.logging;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
/* loaded from: classes.dex */
public abstract class LogTableCellRenderer extends DefaultTableCellRenderer {
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    protected abstract ImageIcon getDebugIcon();

    protected abstract ImageIcon getInfoIcon();

    protected abstract ImageIcon getTraceIcon();

    protected abstract ImageIcon getWarnErrorIcon();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        LogMessage message = (LogMessage) value;
        switch (column) {
            case 0:
                if (message.getLevel().equals(Level.SEVERE) || message.getLevel().equals(Level.WARNING)) {
                    return new JLabel(getWarnErrorIcon());
                }
                if (message.getLevel().equals(Level.FINE)) {
                    return new JLabel(getDebugIcon());
                }
                if (message.getLevel().equals(Level.FINER) || message.getLevel().equals(Level.FINEST)) {
                    return new JLabel(getTraceIcon());
                }
                return new JLabel(getInfoIcon());
            case 1:
                Date date = new Date(message.getCreatedOn().longValue());
                return super.getTableCellRendererComponent(table, this.dateFormat.format(date), isSelected, hasFocus, row, column);
            case 2:
                return super.getTableCellRendererComponent(table, message.getThread(), isSelected, hasFocus, row, column);
            case 3:
                return super.getTableCellRendererComponent(table, message.getSource(), isSelected, hasFocus, row, column);
            default:
                return super.getTableCellRendererComponent(table, message.getMessage().replaceAll("\n", "<NL>").replaceAll("\r", "<CR>"), isSelected, hasFocus, row, column);
        }
    }
}
