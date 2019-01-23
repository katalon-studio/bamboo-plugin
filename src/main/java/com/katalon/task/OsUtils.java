package com.katalon.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class OsUtils {

    static String getOSVersion(BuildLogger buildLogger) {

        if (SystemUtils.IS_OS_WINDOWS) {

            try {
                Process p = Runtime.getRuntime().exec("wmic os get osarchitecture");
                try (InputStream inputStream = p.getInputStream()) {
                    String output = IOUtils.toString(inputStream);
                    p.destroy();

                    if (output.contains("64")) {
                        return "windows 64";
                    } else {
                        return "windows 32";
                    }
                }
            } catch (Exception e) {
                LogUtils.log(buildLogger, "Cannot detect the OS architecture. Assume it is x64.");
                LogUtils.log(buildLogger, "Reason: " + e.getMessage() + ".");
                return "windows 64";
            }

        } else if (SystemUtils.IS_OS_MAC) {
            return "macos (app)";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux";
        }
        return "";
    }

    static boolean runCommand(
            BuildLogger buildLogger,
            String command,
            String x11Display,
            String xvfbConfiguration)
            throws IOException, InterruptedException {

        String[] cmdarray;
        if (SystemUtils.IS_OS_WINDOWS) {
            cmdarray = Arrays.asList("cmd", "/c", command).toArray(new String[]{});
        } else {
            if (!StringUtils.isBlank(x11Display)) {
                command = "DISPLAY=" + x11Display + " " + command;
            }
            if (!StringUtils.isBlank(xvfbConfiguration)) {
                command = "xvfb-run " + xvfbConfiguration + " " + command;
            }
            List<String> cmdlist = Arrays.asList("sh", "-c", command);
            cmdarray = cmdlist.toArray(new String[]{});
        }
        Path workingDirectory = Files.createTempDirectory("katalon-");
        LogUtils.log(buildLogger, "Execute " + Arrays.toString(cmdarray) + " in " + workingDirectory);
        Process cmdProc = Runtime.getRuntime().exec(cmdarray, null, workingDirectory.toFile());
        try (
                BufferedReader stdoutReader = new BufferedReader(
                        new InputStreamReader(
                                cmdProc.getInputStream(), StandardCharsets.UTF_8));
                BufferedReader stderrReader = new BufferedReader(
                        new InputStreamReader(
                                cmdProc.getErrorStream(), StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = stdoutReader.readLine()) != null ||
                    (line = stderrReader.readLine()) != null) {
                LogUtils.log(buildLogger, line);
            }
        }
        cmdProc.waitFor();
        return cmdProc.exitValue() == 0;
    }
}