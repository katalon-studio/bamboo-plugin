package com.katalon.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class PluginLogger extends AbstractLogger {

    private BuildLogger buildLogger;

    public PluginLogger(BuildLogger buildLogger) {
        super(LEVEL_INFO, "katalon-plugin");
        this.buildLogger = buildLogger;
    }

    @Override
    public void debug(String message, Throwable throwable) {
        buildLogger.addBuildLogEntry(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        buildLogger.addBuildLogEntry(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        buildLogger.addBuildLogEntry(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        buildLogger.addErrorLogEntry(message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        buildLogger.addErrorLogEntry(message, throwable);
    }

    @Override
    public Logger getChildLogger(String name) {
        return this;
    }
}
