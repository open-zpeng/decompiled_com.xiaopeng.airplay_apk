package org.seamless.swing.logging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.seamless.swing.logging.LogCategory;
/* loaded from: classes.dex */
public class LogCategorySelector extends JDialog {
    protected final JPanel mainPanel = new JPanel();

    public LogCategorySelector(List<LogCategory> logCategories) {
        setTitle("Select logging categories...");
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, 1));
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addLogCategories(logCategories);
        JScrollPane scrollPane = new JScrollPane(this.mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);
        setMaximumSize(new Dimension(750, 550));
        setResizable(false);
        pack();
    }

    protected void addLogCategories(List<LogCategory> logCategories) {
        for (LogCategory logCategory : logCategories) {
            addLogCategory(logCategory);
        }
    }

    protected void addLogCategory(LogCategory logCategory) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(BorderFactory.createTitledBorder(logCategory.getName()));
        addLoggerGroups(logCategory, categoryPanel);
        this.mainPanel.add(categoryPanel);
    }

    protected void addLoggerGroups(final LogCategory logCategory, final JPanel categoryPanel) {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, 1));
        for (final LogCategory.Group group : logCategory.getGroups()) {
            JCheckBox checkBox = new JCheckBox(group.getName());
            checkBox.setSelected(group.isEnabled());
            checkBox.setFocusable(false);
            checkBox.addItemListener(new ItemListener() { // from class: org.seamless.swing.logging.LogCategorySelector.1
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == 2) {
                        LogCategorySelector.this.disableLoggerGroup(group);
                    } else if (e.getStateChange() == 1) {
                        LogCategorySelector.this.enableLoggerGroup(group);
                    }
                }
            });
            checkboxPanel.add(checkBox);
        }
        JToolBar buttonBar = new JToolBar();
        buttonBar.setFloatable(false);
        JButton enableAllButton = new JButton("All");
        enableAllButton.setFocusable(false);
        enableAllButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogCategorySelector.2
            public void actionPerformed(ActionEvent e) {
                for (LogCategory.Group group2 : logCategory.getGroups()) {
                    LogCategorySelector.this.enableLoggerGroup(group2);
                }
                categoryPanel.removeAll();
                LogCategorySelector.this.addLoggerGroups(logCategory, categoryPanel);
                categoryPanel.revalidate();
            }
        });
        buttonBar.add(enableAllButton);
        JButton disableAllButton = new JButton("None");
        disableAllButton.setFocusable(false);
        disableAllButton.addActionListener(new ActionListener() { // from class: org.seamless.swing.logging.LogCategorySelector.3
            public void actionPerformed(ActionEvent e) {
                for (LogCategory.Group group2 : logCategory.getGroups()) {
                    LogCategorySelector.this.disableLoggerGroup(group2);
                }
                categoryPanel.removeAll();
                LogCategorySelector.this.addLoggerGroups(logCategory, categoryPanel);
                categoryPanel.revalidate();
            }
        });
        buttonBar.add(disableAllButton);
        categoryPanel.add(checkboxPanel, "Center");
        categoryPanel.add(buttonBar, "North");
    }

    protected void enableLoggerGroup(LogCategory.Group group) {
        group.setEnabled(true);
        group.getPreviousLevels().clear();
        for (LogCategory.LoggerLevel loggerLevel : group.getLoggerLevels()) {
            Logger logger = Logger.getLogger(loggerLevel.getLogger());
            group.getPreviousLevels().add(new LogCategory.LoggerLevel(logger.getName(), getLevel(logger)));
            logger.setLevel(loggerLevel.getLevel());
        }
    }

    protected void disableLoggerGroup(LogCategory.Group group) {
        group.setEnabled(false);
        for (LogCategory.LoggerLevel loggerLevel : group.getPreviousLevels()) {
            Logger logger = Logger.getLogger(loggerLevel.getLogger());
            logger.setLevel(loggerLevel.getLevel());
        }
        if (group.getPreviousLevels().size() == 0) {
            for (LogCategory.LoggerLevel loggerLevel2 : group.getLoggerLevels()) {
                Logger logger2 = Logger.getLogger(loggerLevel2.getLogger());
                logger2.setLevel(Level.INFO);
            }
        }
        group.getPreviousLevels().clear();
    }

    public Level getLevel(Logger logger) {
        Level level = logger.getLevel();
        if (level == null && logger.getParent() != null) {
            return logger.getParent().getLevel();
        }
        return level;
    }
}
