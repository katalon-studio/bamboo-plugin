package com.katalon.task;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class KatalonTaskConfiguration extends AbstractTaskConfigurator {

    //save config action
    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull ActionParametersMap params, @Nullable TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("version", params.getString("version"));
        config.put("location", params.getString("location"));
        config.put("executeArgs", params.getString("executeArgs"));
        config.put("x11Display", params.getString("x11Display"));
        config.put("xvfbConfiguration", params.getString("xvfbConfiguration"));

        return config;
    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put("katalonVersion", taskDefinition.getConfiguration().get("katalonVersion"));
        context.put("version", taskDefinition.getConfiguration().get("version"));
        context.put("location", taskDefinition.getConfiguration().get("location"));
        context.put("executeArgs", taskDefinition.getConfiguration().get("executeArgs"));
        context.put("x11Display", taskDefinition.getConfiguration().get("x11Display"));
        context.put("xvfbConfiguration", taskDefinition.getConfiguration().get("xvfbConfiguration"));
    }
}
