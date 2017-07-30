package com.shc.sepcreator;

import javafx.application.Platform;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Sri Harsha Chilakapati
 */
public class Project
{
    File projectDirectory;

    String packageName;
    String className;

    IProgressCallback progressCallback;
    IErrorCallback    errorCallback;

    boolean generateDesktopProject;
    boolean generateHtml5Project;
    boolean generateAndroidProject;

    private Map<String, String> templateParameters;
    private Map<String, String> files;

    public Project()
    {
        templateParameters = new HashMap<>();
        files = new HashMap<>();
    }

    private void setTemplateVariable(String variable, String value)
    {
        templateParameters.put(String.format("${%s}", variable), value);
    }

    private void addFile(String targetDestination, String templateName)
    {
        files.put(targetDestination, "/templates/" + templateName);
    }

    private String getTemplatedFileName(String templateFile)
    {
        templateFile = renderTemplate(templateFile.replaceAll("\\\\", "/"));

        String[] paths = templateFile.split("/");

        StringBuilder templateFileBuilder = new StringBuilder();
        for (String path : paths)
        {
            if (path.isEmpty())
                continue;

            if (path.equals(".."))
                templateFileBuilder = new StringBuilder(templateFileBuilder.substring(0, templateFileBuilder.lastIndexOf("/")));
            else
                templateFileBuilder.append("/").append(path);
        }
        templateFile = templateFileBuilder.toString();

        return templateFile;
    }

    private String renderTemplate(String templateString)
    {
        for (String key : templateParameters.keySet())
            templateString = templateString.replaceAll(Pattern.quote(key), templateParameters.get(key));

        return templateString;
    }

    void generate()
    {
        if (className.isEmpty())
        {
            reportError("Class name cannot be empty");
            return;
        }

        if (packageName.isEmpty())
        {
            reportError("Package name cannot be empty");
            return;
        }

        if (className.trim().equalsIgnoreCase("Game") || className.trim().equalsIgnoreCase("SilenceEngine"))
        {
            reportError("Class name conflicts with SilenceEngine's Game class");
            return;
        }

        VelocityEngine ve = new VelocityEngine();
        VelocityContext ctx = new VelocityContext();

        ctx.put("generateAndroid", generateAndroidProject);
        ctx.put("generateDesktop", generateDesktopProject);
        ctx.put("generateHtml5", generateHtml5Project);

        setTemplateVariable("className", className);
        setTemplateVariable("packageName", packageName);
        setTemplateVariable("coreDirName", packageName.contains(".") ?
                                           packageName.replace(packageName.substring(0, packageName.lastIndexOf('.') + 1), "")
                                                                     : packageName);

        setTemplateVariable("packageDir", packageName.replaceAll("\\.", "/"));
        setTemplateVariable("corePackageName", packageName.contains(".")
                                               ? packageName.substring(0, packageName.lastIndexOf('.'))
                                               : "");
        setTemplateVariable("coreModuleName", packageName.contains(".")
                                              ? renderTemplate("${corePackageName}.${className}")
                                              : renderTemplate("${className}"));

        // Add libraries to be extracted into the project directory
        if (generateHtml5Project)
        {
            addFile("/libs/backend-gwt.jar", "libs/backend-gwt.jar");
            addFile("/libs/backend-gwt-javadoc.jar", "libs/backend-gwt-javadoc.jar");
            addFile("/libs/silenceengine-sources.jar", "libs/silenceengine-sources.jar");
        }

        if (generateDesktopProject)
        {
            addFile("/libs/backend-lwjgl.jar", "libs/backend-lwjgl.jar");
            addFile("/libs/backend-lwjgl-javadoc.jar", "libs/backend-lwjgl-javadoc.jar");
        }

        if (generateAndroidProject)
        {
            addFile("/libs/backend-android-release.aar", "libs/backend-android-release.aar");
            addFile("/libs/backend-android-debug.aar", "libs/backend-android-debug.aar");
        }

        addFile("/libs/silenceengine.jar", "libs/silenceengine.jar");
        addFile("/libs/silenceengine-javadoc.jar", "libs/silenceengine-javadoc.jar");
        addFile("/libs/silenceengine-resources.jar", "libs/silenceengine-resources.jar");

        // Add template files for root project
        addFile("/build.gradle", "build.gradle.vm");

        // Add template files for core project
        addFile("/${className}Core/build.gradle", "core/build.gradle.vm");
        addFile("/${className}Core/src/main/java/${packageDir}/${className}.java", "core/game.java.vm");
        addFile("/${className}Core/src/main/resources/README.txt", "core/resources.txt.vm");

        if (generateHtml5Project)
            addFile("/${className}Core/src/main/java/${packageDir}/../${className}.gwt.xml", "core/project.gwt.xml.vm");

        if (generateDesktopProject)
        {
            // Add template files for desktop project
            addFile("/${className}Desktop/build.gradle", "desktop/build.gradle.vm");
            addFile("/${className}Desktop/src/main/java/${packageDir}/desktop/${className}Launcher.java", "desktop/launcher.java.vm");
        }

        if (generateHtml5Project)
        {
            // Add template files for html5 project
            addFile("/${className}Html5/build.gradle", "html5/build.gradle.vm");
            addFile("/${className}Html5/src/main/java/${packageDir}/${className}Gwt.gwt.xml", "html5/gamegwt.gwt.xml.vm");
            addFile("/${className}Html5/src/main/java/${packageDir}/html/${className}Launcher.java", "html5/launcher.java.vm");
            addFile("/${className}Html5/src/main/webapp/index.html", "html5/index.html.vm");
            addFile("/${className}Html5/src/main/java/${packageDir}/public/background.css", "html5/background.css.vm");
        }

        if (generateAndroidProject)
        {
            // Add template files for android project
            addFile("/${className}Android/build.gradle", "android/build.gradle.vm");
            addFile("/${className}Android/src/main/AndroidManifest.xml", "android/manifest.xml.vm");
            addFile("/${className}Android/src/main/java/${packageDir}/android/${className}Launcher.java", "android/launcher.java.vm");
            addFile("/${className}Android/src/main/res/drawable/ic_launcher.png", "android/ic_launcher.png");
        }

        // Add the .gitignore file for repositories
        addFile("/.gitignore", ".gitignore.vm");

        // Add the gradle wrapper
        addFile("/gradlew", "gradlew");
        addFile("/gradlew.bat", "gradlew.bat");
        addFile("/gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar");
        addFile("/gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.properties");

        float maxFiles = files.size() + 1;
        float done = 0;

        try
        {
            for (String fileName : files.keySet())
            {
                File dest = new File(projectDirectory, getTemplatedFileName(fileName));

                if (!dest.exists() && !dest.getParentFile().mkdirs() && !dest.createNewFile())
                    throw new Exception("Cannot create directories for " + dest.getAbsolutePath());

                String sourceFileName = files.get(fileName);

                if (sourceFileName.endsWith(".vm"))
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(sourceFileName)));

                    StringBuilder templateData = new StringBuilder(reader.readLine());
                    String line;

                    while ((line = reader.readLine()) != null)
                        templateData.append("\n").append(line);

                    reader.close();

                    templateData = new StringBuilder(renderTemplate(templateData.toString()));

                    FileWriter writer = new FileWriter(dest);
                    ve.evaluate(ctx, writer, sourceFileName, templateData.toString());

                    writer.flush();
                    writer.close();
                }
                else
                    saveResource(dest, sourceFileName);

                done++;
                updateProgress((int) (done / maxFiles * 100.0));
            }

            // Write the settings.gradle file linking all these modules
            String settingsFile = "include '${className}Core'";

            if (generateAndroidProject)
                settingsFile += ", '${className}Android'";

            if (generateDesktopProject)
                settingsFile += ", '${className}Desktop'";

            if (generateHtml5Project)
                settingsFile += ", '${className}Html5'";

            settingsFile = renderTemplate(settingsFile);

            FileWriter writer = new FileWriter(new File(projectDirectory, "settings.gradle"));
            writer.write(settingsFile);
            writer.write("\n");
            writer.flush();
            writer.close();

            done++;
            updateProgress((int) (done / maxFiles * 100.0));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            reportError(e.getMessage());
        }
    }

    private void saveResource(File out, String name)
            throws IOException
    {
        InputStream resource = this.getClass().getResourceAsStream(name);

        if (resource == null)
            throw new FileNotFoundException(name + " (resource not found)");

        try (InputStream in = resource;
             OutputStream writer = new BufferedOutputStream(new FileOutputStream(out)))
        {
            byte[] buffer = new byte[1024 * 4];
            int length;

            while ((length = in.read(buffer)) >= 0)
                writer.write(buffer, 0, length);
        }
    }

    private void updateProgress(int progress)
    {
        if (Platform.isFxApplicationThread())
            progressCallback.update(progress);
        else
            Platform.runLater(() -> progressCallback.update(progress));
    }

    private void reportError(String error)
    {
        if (Platform.isFxApplicationThread())
            errorCallback.handle(error);
        else
            Platform.runLater(() -> errorCallback.handle(error));
    }

    @FunctionalInterface
    interface IProgressCallback
    {
        void update(int progress);
    }

    @FunctionalInterface
    interface IErrorCallback
    {
        void handle(String reason);
    }
}
