package com.katalon.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.google.common.base.Throwables;
import com.katalon.license.LicenseUtils;
import com.katalon.license.LicenseValidation;
import com.katalon.utils.KatalonUtils;
import com.katalon.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KatalonTask implements TaskType {

    @Autowired
    private LicenseUtils licenseUtils;


    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) {

        LicenseValidation license = licenseUtils.validateLicense();

        final BuildLogger buildLogger = taskContext.getBuildLogger();

        if (license.isValid()) {
            String version = taskContext.getConfigurationMap().get("version");
            String location = taskContext.getConfigurationMap().get("location");
            String executeArgs = taskContext.getConfigurationMap().get("executeArgs");
            String x11Display = taskContext.getConfigurationMap().get("x11Display");
            String xvfbConfiguration = taskContext.getConfigurationMap().get("xvfbConfiguration");

            VariableContext variables = taskContext.getBuildContext().getVariableContext();
            Map<String, VariableDefinitionContext> variable = variables.getEffectiveVariables();
            Map<String, String> environmentVariables = variable.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> Optional.ofNullable(entry.getValue().getValue()).orElse("")
                    ));

            try {

                File workspace = taskContext.getWorkingDirectory();
                boolean runCommand = false;

                if (workspace != null) {

                    String workspaceLocation = workspace.getPath();
                    if (workspaceLocation != null) {
                        Logger logger = new PluginLogger(buildLogger);
                        runCommand = KatalonUtils.executeKatalon(logger,
                                version,
                                location,
                                workspaceLocation,
                                executeArgs,
                                x11Display,
                                xvfbConfiguration,
                                environmentVariables);

                    }
                }

                if (runCommand) {
                    return TaskResultBuilder.newBuilder(taskContext).success().build();
                } else {
                    return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
                }

            } catch (Exception e) {
                String stackTrace = Throwables.getStackTraceAsString(e);
                LogUtils.log(buildLogger, stackTrace);
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }
        }

        buildLogger.addErrorLogEntry("Katalon add-on for Bamboo requires a valid license. Please contact your Bamboo admin.");
        return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
    }
}
