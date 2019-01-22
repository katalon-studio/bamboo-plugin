package com.katalon.task;


import com.atlassian.bamboo.build.logger.BuildLogger;

class LogUtils {

    static void log(BuildLogger buildLogger, String message) {
        buildLogger.addBuildLogEntry(message);
    }

}
