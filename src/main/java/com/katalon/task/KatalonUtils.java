package com.katalon.task;

import com.atlassian.bamboo.archive.Archiver;
import com.atlassian.bamboo.archive.ArchiverResolver;
import com.atlassian.bamboo.archive.ArchiverType;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Untar;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

class KatalonUtils {

    private static final String RELEASES_LIST =
            "https://raw.githubusercontent.com/katalon-studio/katalon-studio/master/releases.json";

    private static KatalonVersion getVersionInfo(BuildLogger buildLogger, String versionNumber) throws IOException {

        URL url = new URL(RELEASES_LIST);

        String os = OsUtils.getOSVersion(buildLogger);

        LogUtils.log(buildLogger, "Retrieve Katalon Studio version: " + versionNumber + ", OS: " + os);

        ObjectMapper objectMapper = new ObjectMapper();
        List<KatalonVersion> versions = objectMapper.readValue(url, new TypeReference<List<KatalonVersion>>() {
        });

        LogUtils.log(buildLogger, "Number of releases: " + versions.size());

        for (KatalonVersion version : versions) {
            if ((version.getVersion().equals(versionNumber)) && (version.getOs().equalsIgnoreCase(os))) {
                String containingFolder = version.getContainingFolder();
                if (containingFolder == null) {
                    String fileName = version.getFilename();
                    String fileExtension = "";
                    if (fileName.endsWith(".zip")) {
                        fileExtension = ".zip";
                    } else if (fileName.endsWith(".tar.gz")) {
                        fileExtension = ".tar.gz";
                    }
                    containingFolder = fileName.replace(fileExtension, "");
                    version.setContainingFolder(containingFolder);
                }
                LogUtils.log(buildLogger, "Katalon Studio is hosted at " + version.getUrl() + ".");
                return version;
            }
        }
        return null;
    }

    private static void downloadAndExtract(
            BuildLogger buildLogger, String versionNumber, File targetDir)
        throws IOException, InterruptedException {

        KatalonVersion version = KatalonUtils.getVersionInfo(buildLogger, versionNumber);

        String versionUrl = version.getUrl();

        LogUtils.log(
                buildLogger,
                "Downloading Katalon Studio from " + versionUrl + ". It may take a few minutes.");

        URL url = new URL(versionUrl);

        try (InputStream inputStream = url.openStream()) {
            Path temporaryFile = Files.createTempFile("Katalon-" + versionNumber, "");
            Files.copy(
                inputStream,
                temporaryFile,
                StandardCopyOption.REPLACE_EXISTING);

            Logger logger = new PluginLogger(buildLogger);
            if (versionUrl.contains(".zip")) {
                ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
                zipUnArchiver.setSourceFile(temporaryFile.toFile());
                zipUnArchiver.setDestDirectory(targetDir);
                zipUnArchiver.enableLogging(logger);
                zipUnArchiver.extract();
            } else if (versionUrl.contains(".tar.gz")) {
                TarGZipUnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();
                tarGZipUnArchiver.setSourceFile(temporaryFile.toFile());
                tarGZipUnArchiver.setDestDirectory(targetDir);
                tarGZipUnArchiver.enableLogging(logger);
                tarGZipUnArchiver.extract();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private static File getKatalonFolder(String version) {
        String path = System.getProperty("user.home");
        Path p = Paths.get(path, ".katalon", version);
        return p.toFile();
    }

    static File getKatalonPackage(
            BuildLogger buildLogger, String versionNumber)
            throws IOException, InterruptedException {

        File katalonDir = getKatalonFolder(versionNumber);
        LogUtils.log(buildLogger, "katalon Dir " + katalonDir);

        Path fileLog = Paths.get(katalonDir.toString(), ".katalon.done");

        if (fileLog.toFile().exists()) {
            LogUtils.log(buildLogger, "Katalon Studio package has been downloaded already.");
        } else {
            FileUtils.deleteDirectory(katalonDir);

            boolean katalonDirCreated = katalonDir.mkdirs();
            if (!katalonDirCreated) {
                throw new IllegalStateException("Cannot create directory to store Katalon Studio package.");
            }

            KatalonUtils.downloadAndExtract(buildLogger, versionNumber, katalonDir);

            boolean fileLogCreated = fileLog.toFile().createNewFile();
            if (fileLogCreated) {
                LogUtils.log(buildLogger, "Katalon Studio has been installed successfully.");
            }
        }

        String[] childrenNames = katalonDir.list((dir, name) -> {
            File file = new File(dir, name);
            return file.isDirectory() && name.contains("Katalon");
        });

        String katalonContainingDirName = Arrays.stream(childrenNames).findFirst().get();


        File katalonContainingDir = new File(katalonDir, katalonContainingDirName);

        return katalonContainingDir;
    }

    private static boolean executeKatalon(
            BuildLogger buildLogger,
            String katalonExecutableFile,
            String projectPath,
            String executeArgs,
            String x11Display,
            String xvfbConfiguration)
            throws IOException, InterruptedException {
        File file = new File(katalonExecutableFile);
        if (!file.exists()) {
            file = new File(katalonExecutableFile + ".exe");
        }
        if (file.exists()) {
            file.setExecutable(true);
        }
        if (katalonExecutableFile.contains(" ")) {
            katalonExecutableFile = "\"" + katalonExecutableFile + "\"";
        }
        String command = katalonExecutableFile +
                " -noSplash " +
                " -runMode=console ";
        if (!executeArgs.contains("-projectPath")) {
            command += " -projectPath=\"" + projectPath + "\" ";
        }
        command += " " + executeArgs + " ";

        return OsUtils.runCommand(buildLogger, command, x11Display, xvfbConfiguration);
    }

    public static boolean executeKatalon(
            BuildLogger buildLogger,
            String version,
            String location,
            String projectPath,
            String executeArgs,
            String x11Display,
            String xvfbConfiguration)
            throws IOException, InterruptedException {

        String katalonDirPath;

        if (StringUtils.isBlank(location)) {
            File katalonDir = KatalonUtils.getKatalonPackage(buildLogger, version);
            katalonDirPath = katalonDir.getAbsolutePath();
        } else {
            katalonDirPath = location;
        }

        LogUtils.log(buildLogger, "Using Katalon Studio at " + katalonDirPath);
        String katalonExecutableFile;
        String os = OsUtils.getOSVersion(buildLogger);
        if (os.contains("macos")) {
            katalonExecutableFile = Paths.get(katalonDirPath, "Contents", "MacOS", "katalon")
                    .toAbsolutePath()
                    .toString();
        } else {
            katalonExecutableFile = Paths.get(katalonDirPath, "katalon")
                    .toAbsolutePath()
                    .toString();
        }
        return executeKatalon(
            buildLogger,
            katalonExecutableFile,
            projectPath,
            executeArgs,
            x11Display,
            xvfbConfiguration);
    }
}
