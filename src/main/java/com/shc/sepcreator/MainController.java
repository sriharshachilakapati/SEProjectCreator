package com.shc.sepcreator;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

/**
 * @author Sri Harsha Chilakapati
 */
public class MainController
{
    @FXML
    public TextField   pkgName;
    @FXML
    public TextField   clsName;
    @FXML
    public TextField   pDir;
    @FXML
    public Label       messageStr;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button      generateBtn;
    @FXML
    public CheckBox    desktopGen;
    @FXML
    public CheckBox    html5Gen;

    @FXML
    public void generateProjectClicked()
    {
        Project project = new Project();

        project.packageName = pkgName.getText();
        project.className = clsName.getText();
        project.projectDirectory = new File(pDir.getText());

        project.generateHtml5Project = html5Gen.isSelected();
        project.generateDesktopProject = desktopGen.isSelected();

        Task<Void> task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                project.progressCallback = progress -> updateProgress(progress, 100);
                project.generate();
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        project.errorCallback = reason ->
        {
            task.cancel();
            generateBtn.setDisable(false);

            messageStr.setText("Error generating project: " + reason);
            messageStr.getStyleClass().clear();
            messageStr.getStyleClass().add("error");
        };

        task.workDoneProperty().addListener((observable, oldValue, newValue) ->
        {
            messageStr.setText("Generating project " + newValue.intValue() + "%");

            if (newValue.intValue() == 100)
            {
                generateBtn.setDisable(false);

                messageStr.setText("Done generating the project!");
                messageStr.getStyleClass().clear();
                messageStr.getStyleClass().add("success");
            }
        });

        new Thread(task).start();

        generateBtn.setDisable(true);
        messageStr.setText("Generating project");
        messageStr.getStyleClass().clear();
        messageStr.getStyleClass().add("progress");
    }

    @FXML
    public void browseDirSelected()
    {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(null);

        pDir.setText(dir.getAbsolutePath());
    }
}
