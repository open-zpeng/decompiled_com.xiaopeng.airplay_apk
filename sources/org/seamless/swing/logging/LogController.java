package org.seamless.swing.logging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.seamless.swing.AbstractController;
import org.seamless.swing.Application;
import org.seamless.swing.Controller;
/* loaded from: classes.dex */
public abstract class LogController extends AbstractController<JPanel> {
    private final JButton clearButton;
    private final JButton configureButton;
    private final JButton copyButton;
    private final JButton expandButton;
    private final JComboBox expirationComboBox;
    private final LogCategorySelector logCategorySelector;
    private final JTable logTable;
    private final LogTableModel logTableModel;
    private final JButton pauseButton;
    private final JLabel pauseLabel;
    private final JToolBar toolBar;

    protected abstract void expand(LogMessage logMessage);

    protected abstract Frame getParentWindow();

    /* loaded from: classes.dex */
    public enum Expiration {
        TEN_SECONDS(10, "10 Seconds"),
        SIXTY_SECONDS(60, "60 Seconds"),
        FIVE_MINUTES(300, "5 Minutes"),
        NEVER(Integer.MAX_VALUE, "Never");
        
        private String label;
        private int seconds;

        Expiration(int seconds, String label) {
            this.seconds = seconds;
            this.label = label;
        }

        public int getSeconds() {
            return this.seconds;
        }

        public String getLabel() {
            return this.label;
        }

        @Override // java.lang.Enum
        public String toString() {
            return getLabel();
        }
    }

    public LogController(Controller parentController, List<LogCategory> logCategories) {
        this(parentController, Expiration.SIXTY_SECONDS, logCategories);
    }

    public LogController(Controller parentController, Expiration expiration, List<LogCategory> logCategories) {
        super(new JPanel(new BorderLayout()), parentController);
        this.toolBar = new JToolBar();
        this.configureButton = createConfigureButton();
        this.clearButton = createClearButton();
        this.copyButton = createCopyButton();
        this.expandButton = createExpandButton();
        this.pauseButton = createPauseButton();
        this.pauseLabel = new JLabel(" (Active)");
        this.expirationComboBox = new JComboBox(Expiration.values());
        this.logCategorySelector = new LogCategorySelector(logCategories);
        this.logTableModel = new LogTableModel(expiration.getSeconds());
        this.logTable = new JTable(this.logTableModel);
        this.logTable.setDefaultRenderer(LogMessage.class, new LogTableCellRenderer() { // from class: org.seamless.swing.logging.LogController.1
            @Override // org.seamless.swing.logging.LogTableCellRenderer
            protected ImageIcon getWarnErrorIcon() {
                return LogController.this.getWarnErrorIcon();
            }

            @Override // org.seamless.swing.logging.LogTableCellRenderer
            protected ImageIcon getDebugIcon() {
                return LogController.this.getDebugIcon();
            }

            @Override // org.seamless.swing.logging.LogTableCellRenderer
            protected ImageIcon getTraceIcon() {
                return LogController.this.getTraceIcon();
            }

            @Override // org.seamless.swing.logging.LogTableCellRenderer
            protected ImageIcon getInfoIcon() {
                return LogController.this.getInfoIcon();
            }
        });
        this.logTable.setCellSelectionEnabled(false);
        this.logTable.setRowSelectionAllowed(true);
        this.logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() { // from class: org.seamless.swing.logging.LogController.2
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && e.getSource() == LogController.this.logTable.getSelectionModel()) {
                    int[] rows = LogController.this.logTable.getSelectedRows();
                    if (rows == null || rows.length == 0) {
                        LogController.this.copyButton.setEnabled(false);
                        LogController.this.expandButton.setEnabled(false);
                    } else if (rows.length == 1) {
                        LogController.this.copyButton.setEnabled(true);
                        LogMessage msg = (LogMessage) LogController.this.logTableModel.getValueAt(rows[0], 0);
                        if (msg.getMessage().length() > LogController.this.getExpandMessageCharacterLimit()) {
                            LogController.this.expandButton.setEnabled(true);
                        } else {
                            LogController.this.expandButton.setEnabled(false);
                        }
                    } else {
                        LogController.this.copyButton.setEnabled(true);
                        LogController.this.expandButton.setEnabled(false);
                    }
                }
            }
        });
        adjustTableUI();
        initializeToolBar(expiration);
        getView().setPreferredSize(new Dimension(250, 100));
        getView().setMinimumSize(new Dimension(250, 50));
        getView().add(new JScrollPane(this.logTable), "Center");
        getView().add(this.toolBar, "South");
    }

    public void pushMessage(final LogMessage message) {
        SwingUtilities.invokeLater(new Runnable() { // from class: org.seamless.swing.logging.LogController.3
            @Override // java.lang.Runnable
            public void run() {
                LogController.this.logTableModel.pushMessage(message);
                if (!LogController.this.logTableModel.isPaused()) {
                    LogController.this.logTable.scrollRectToVisible(LogController.this.logTable.getCellRect(LogController.this.logTableModel.getRowCount() - 1, 0, true));
                }
            }
        });
    }

    protected void adjustTableUI() {
        this.logTable.setFocusable(false);
        this.logTable.setRowHeight(18);
        this.logTable.getTableHeader().setReorderingAllowed(false);
        this.logTable.setBorder(BorderFactory.createEmptyBorder());
        this.logTable.getColumnModel().getColumn(0).setMinWidth(30);
        this.logTable.getColumnModel().getColumn(0).setMaxWidth(30);
        this.logTable.getColumnModel().getColumn(0).setResizable(false);
        this.logTable.getColumnModel().getColumn(1).setMinWidth(90);
        this.logTable.getColumnModel().getColumn(1).setMaxWidth(90);
        this.logTable.getColumnModel().getColumn(1).setResizable(false);
        this.logTable.getColumnModel().getColumn(2).setMinWidth(100);
        this.logTable.getColumnModel().getColumn(2).setMaxWidth(250);
        this.logTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        this.logTable.getColumnModel().getColumn(3).setMaxWidth(400);
        this.logTable.getColumnModel().getColumn(4).setPreferredWidth(600);
    }

    protected void initializeToolBar(Expiration expiration) {
        this.configureButton.setFocusable(false);
        this.configureButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.4
            public void actionPerformed(ActionEvent e) {
                Application.center(LogController.this.logCategorySelector, LogController.this.getParentWindow());
                LogController.this.logCategorySelector.setVisible(!LogController.this.logCategorySelector.isVisible());
            }
        });
        this.clearButton.setFocusable(false);
        this.clearButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.5
            public void actionPerformed(ActionEvent e) {
                LogController.this.logTableModel.clearMessages();
            }
        });
        this.copyButton.setFocusable(false);
        this.copyButton.setEnabled(false);
        this.copyButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.6
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                List<LogMessage> messages = LogController.this.getSelectedMessages();
                for (LogMessage message : messages) {
                    sb.append(message.toString());
                    sb.append("\n");
                }
                Application.copyToClipboard(sb.toString());
            }
        });
        this.expandButton.setFocusable(false);
        this.expandButton.setEnabled(false);
        this.expandButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.7
            public void actionPerformed(ActionEvent e) {
                List<LogMessage> messages = LogController.this.getSelectedMessages();
                if (messages.size() != 1) {
                    return;
                }
                LogController.this.expand(messages.get(0));
            }
        });
        this.pauseButton.setFocusable(false);
        this.pauseButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.8
            public void actionPerformed(ActionEvent e) {
                LogController.this.logTableModel.setPaused(!LogController.this.logTableModel.isPaused());
                if (LogController.this.logTableModel.isPaused()) {
                    LogController.this.pauseLabel.setText(" (Paused)");
                } else {
                    LogController.this.pauseLabel.setText(" (Active)");
                }
            }
        });
        this.expirationComboBox.setSelectedItem(expiration);
        this.expirationComboBox.setMaximumSize(new Dimension(100, 32));
        this.expirationComboBox.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogController.9
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                Expiration expiration2 = (Expiration) cb.getSelectedItem();
                LogController.this.logTableModel.setMaxAgeSeconds(expiration2.getSeconds());
            }
        });
        this.toolBar.setFloatable(false);
        this.toolBar.add(this.copyButton);
        this.toolBar.add(this.expandButton);
        this.toolBar.add(Box.createHorizontalGlue());
        this.toolBar.add(this.configureButton);
        this.toolBar.add(this.clearButton);
        this.toolBar.add(this.pauseButton);
        this.toolBar.add(this.pauseLabel);
        this.toolBar.add(Box.createHorizontalGlue());
        this.toolBar.add(new JLabel("Clear after:"));
        this.toolBar.add(this.expirationComboBox);
    }

    protected List<LogMessage> getSelectedMessages() {
        int[] selectedRows;
        List<LogMessage> messages = new ArrayList<>();
        for (int row : this.logTable.getSelectedRows()) {
            messages.add((LogMessage) this.logTableModel.getValueAt(row, 0));
        }
        return messages;
    }

    protected int getExpandMessageCharacterLimit() {
        return 100;
    }

    public LogTableModel getLogTableModel() {
        return this.logTableModel;
    }

    protected JButton createConfigureButton() {
        return new JButton("Options...", Application.createImageIcon(LogController.class, "img/configure.png"));
    }

    protected JButton createClearButton() {
        return new JButton("Clear Log", Application.createImageIcon(LogController.class, "img/removetext.png"));
    }

    protected JButton createCopyButton() {
        return new JButton("Copy", Application.createImageIcon(LogController.class, "img/copyclipboard.png"));
    }

    protected JButton createExpandButton() {
        return new JButton("Expand", Application.createImageIcon(LogController.class, "img/viewtext.png"));
    }

    protected JButton createPauseButton() {
        return new JButton("Pause/Continue Log", Application.createImageIcon(LogController.class, "img/pause.png"));
    }

    protected ImageIcon getWarnErrorIcon() {
        return Application.createImageIcon(LogController.class, "img/warn.png");
    }

    protected ImageIcon getDebugIcon() {
        return Application.createImageIcon(LogController.class, "img/debug.png");
    }

    protected ImageIcon getTraceIcon() {
        return Application.createImageIcon(LogController.class, "img/trace.png");
    }

    protected ImageIcon getInfoIcon() {
        return Application.createImageIcon(LogController.class, "img/info.png");
    }
}
