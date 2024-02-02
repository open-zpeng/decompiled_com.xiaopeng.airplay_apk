package org.seamless.swing.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
/* loaded from: classes.dex */
public class LogCategory {
    private List<Group> groups;
    private String name;

    public LogCategory(String name) {
        this.groups = new ArrayList();
        this.name = name;
    }

    public LogCategory(String name, Group[] groups) {
        this.groups = new ArrayList();
        this.name = name;
        this.groups = Arrays.asList(groups);
    }

    public String getName() {
        return this.name;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    public void addGroup(String name, LoggerLevel[] loggerLevels) {
        this.groups.add(new Group(name, loggerLevels));
    }

    /* loaded from: classes.dex */
    public static class Group {
        private boolean enabled;
        private List<LoggerLevel> loggerLevels;
        private String name;
        private List<LoggerLevel> previousLevels;

        public Group(String name) {
            this.loggerLevels = new ArrayList();
            this.previousLevels = new ArrayList();
            this.name = name;
        }

        public Group(String name, LoggerLevel[] loggerLevels) {
            this.loggerLevels = new ArrayList();
            this.previousLevels = new ArrayList();
            this.name = name;
            this.loggerLevels = Arrays.asList(loggerLevels);
        }

        public String getName() {
            return this.name;
        }

        public List<LoggerLevel> getLoggerLevels() {
            return this.loggerLevels;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<LoggerLevel> getPreviousLevels() {
            return this.previousLevels;
        }

        public void setPreviousLevels(List<LoggerLevel> previousLevels) {
            this.previousLevels = previousLevels;
        }
    }

    /* loaded from: classes.dex */
    public static class LoggerLevel {
        private Level level;
        private String logger;

        public LoggerLevel(String logger, Level level) {
            this.logger = logger;
            this.level = level;
        }

        public String getLogger() {
            return this.logger;
        }

        public Level getLevel() {
            return this.level;
        }
    }
}
