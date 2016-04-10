package com.shc.sepcreator;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    public void generateProjectClicked()
    {
        Task<Void> task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                final int max = 100;

                for (int i = 1; i <= max; i++)
                {
                    if (isCancelled())
                        break;

                    updateProgress(i, max);
                    Thread.sleep(20);
                }

                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        task.workDoneProperty().addListener((observable, oldValue, newValue) -> {
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
