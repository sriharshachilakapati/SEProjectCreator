package com.shc.sepcreator;

import javafx.application.Platform;

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
    public File projectDirectory;

    public String packageName;
    public String className;

    public IProgressCallback progressCallback;
    public IErrorCallback    errorCallback;

    public boolean generateDesktopProject;
    public boolean generateHtml5Project;

    private Map<String, String> templateParameters;
    private Map<String, String> files;

    public Project()
    {
        templateParameters = new HashMap<>();
        files = new HashMap<>();
    }

    public void setTemplateVariable(String variable, String value)
    {
        templateParameters.put(String.format("${%s}", variable), value);
    }

    public void addFile(String targetDestination, String templateName)
    {
        files.put(targetDestination, "/templates/" + templateName);
    }

    private String getTemplatedFileName(String templateFile)
    {
        templateFile = renderTemplate(templateFile.replaceAll("\\\\", "/"));

        String[] paths = templateFile.split("/");
        templateFile = "";

        for (String path : paths)
        {
            if (path.isEmpty())
                continue;

            if (path.equals(".."))
                templateFile = templateFile.substring(0, templateFile.lastIndexOf('/'));
            else
                templateFile += "/" + path;
        }

        return templateFile;
    }

    private String renderTemplate(String templateString)
    {
        for (String key : templateParameters.keySet())
            templateString = templateString.replaceAll(Pattern.quote(key), templateParameters.get(key));

        return templateString;
    }

    public void generate()
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

        // Add libraries
        addFile("/libs/backend-gwt.jar", "libs/backend-gwt.jar");
        addFile("/libs/backend-gwt-javadoc.jar", "libs/backend-gwt-javadoc.jar");
        addFile("/libs/backend-lwjgl.jar", "libs/backend-lwjgl.jar");
        addFile("/libs/backend-lwjgl-javadoc.jar", "libs/backend-lwjgl-javadoc.jar");
        addFile("/libs/silenceengine.jar", "libs/silenceengine.jar");
        addFile("/libs/silenceengine-javadoc.jar", "libs/silenceengine-javadoc.jar");
        addFile("/libs/silenceengine-resources.jar", "libs/silenceengine-resources.jar");
        addFile("/libs/silenceengine-sources.jar", "libs/silenceengine-sources.jar");

        // Add template files for root project
        addFile("/settings.gradle", "settings.gradle.tpl");
        addFile("/build.gradle", "build.gradle.tpl");

        // Add template files for core project
        addFile("/${className}Core/build.gradle", "core/build.gradle.tpl");
        addFile("/${className}Core/src/main/java/${packageDir}/../${className}.gwt.xml", "core/project.gwt.xml.tpl");
        addFile("/${className}Core/src/main/java/${packageDir}/${className}.java", "core/game.java.tpl");
        addFile("/${className}Core/src/main/resources/README.txt", "core/resources.txt.tpl");

        if (generateDesktopProject)
        {
            // Add template files for desktop project
            addFile("/${className}Desktop/build.gradle", "desktop/build.gradle.tpl");
            addFile("/${className}Desktop/src/main/java/${packageDir}/desktop/${className}Launcher.java", "desktop/launcher.java.tpl");
        }

        if (generateHtml5Project)
        {
            // Add template files for html5 project
            addFile("/${className}Html5/build.gradle", "html5/build.gradle.tpl");
            addFile("/${className}Html5/src/main/java/${packageDir}/${className}Gwt.gwt.xml", "html5/gamegwt.gwt.xml.tpl");
            addFile("/${className}Html5/src/main/java/${packageDir}/html/${className}Launcher.java", "html5/launcher.java.tpl");
            addFile("/${className}Html5/src/main/webapp/index.html", "html5/index.html.tpl");
        }

        float maxFiles = files.size();
        float done = 0;

        try
        {
            for (String fileName : files.keySet())
            {
                File dest = new File(projectDirectory, getTemplatedFileName(fileName));

                if (!dest.exists() && !dest.getParentFile().mkdirs() && !dest.createNewFile())
                    throw new Exception("Cannot create directories for " + dest.getAbsolutePath());

                String sourceFileName = files.get(fileName);

                if (sourceFileName.endsWith(".tpl"))
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(sourceFileName)));

                    String templateData = reader.readLine();
                    String line;

                    while ((line = reader.readLine()) != null)
                        templateData += "\n" + line;

                    reader.close();

                    templateData = renderTemplate(templateData);

                    FileWriter writer = new FileWriter(dest);
                    writer.write(templateData);
                    writer.flush();
                    writer.close();
                }
                else
                    saveResource(dest, sourceFileName);

                done++;
                updateProgress((int) (done / maxFiles * 100.0));
            }
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
    public interface IProgressCallback
    {
        void update(int progress);
    }

    @FunctionalInterface
    public interface IErrorCallback
    {
        void handle(String reason);
    }
}
