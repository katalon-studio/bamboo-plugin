package com.katalon.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.katalon.utils.Logger;

public class PluginLogger implements Logger {

    private BuildLogger buildLogger;

    public PluginLogger(BuildLogger buildLogger) {
        this.buildLogger = buildLogger;
    }

    @Override
    public void info(String message) {
        LogUtils.log(buildLogger, message);
    }
}
