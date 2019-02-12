package com.katalon.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.google.common.base.Throwables;
import com.katalon.utils.KatalonUtils;
import com.katalon.utils.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class KatalonTask implements TaskType {

    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        String version = taskContext.getConfigurationMap().get("version");
        String location = taskContext.getConfigurationMap().get("location");
        String executeArgs = taskContext.getConfigurationMap().get("executeArgs");
        String x11Display = taskContext.getConfigurationMap().get("x11Display");
        String xvfbConfiguration = taskContext.getConfigurationMap().get("xvfbConfiguration");



        try {

            File workspace = taskContext.getWorkingDirectory();

            if (workspace != null) {

                String workspaceLocation = workspace.getPath();

                if (workspaceLocation != null) {
                    Logger logger = new PluginLogger(buildLogger);
                    KatalonUtils.executeKatalon(logger,
                        version,
                        location,
                        workspaceLocation,
                        executeArgs,
                        x11Display,
                        xvfbConfiguration);

                }
            }

        } catch (Exception e) {
            String stackTrace = Throwables.getStackTraceAsString(e);
            LogUtils.log(buildLogger, stackTrace);
            TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        }

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }
}
