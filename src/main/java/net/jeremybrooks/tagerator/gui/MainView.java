package net.jeremybrooks.tagerator.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.TConstants;
import net.jeremybrooks.tagerator.tasks.TagCollectorTask;
import net.jeremybrooks.tagerator.tasks.WordFrequencyTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class MainView {


    private static final Logger logger = LogManager.getLogger();

    @FXML
    public MenuItem mnuDeauthorize;
    @FXML
    public Button btnStart;
    @FXML
    public Label lblStatus;
    @FXML
    public ChoiceBox<String> sourceBox;
    @FXML
    public ChoiceBox<String> cbxShape;
    @FXML
    public TextField txtFile;
    @FXML
    public Button btnBrowse;
    @FXML
    public Label lblFileHelp;

    @FXML
    public void initialize() {
        populateComboBox();
        cbxShape.getItems().addAll("Circle", "Square", "Image");
        cbxShape.getSelectionModel().select(0);
        btnBrowse.visibleProperty().bind(txtFile.visibleProperty());
        lblFileHelp.visibleProperty().bind(txtFile.visibleProperty());
        txtFile.setVisible(false);
        cbxShape.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFile.setVisible(newValue.equals("Image"));
        });
    }


    public void populateComboBox() {
        sourceBox.getItems().clear();
        if (Files.exists(Main.tagCacheFile)) {
            sourceBox.getItems().addAll("Cache", "Flickr");
        } else {
            sourceBox.getItems().add("Flickr");
        }
        sourceBox.getSelectionModel().select(0);
    }

    // TODO add an "open" button that is activated when the word cloud has been saved

    public void doStart() {
        switch (sourceBox.getValue()) {
            case "Cache" -> doWordFrequency();
            case "Flickr" -> doTagCollect();
        }
    }

    public void doTagCollect() {
        try {
            TagCollectorTask task = new TagCollectorTask();

            task.setOnSucceeded(t -> {
                lblStatus.textProperty().unbind();
                btnStart.disableProperty().unbind();
                sourceBox.disableProperty().unbind();
                try {
                    saveTagCache(task.getValue());
                } catch (Exception e) {
                    logger.error("Could not save tag cache to file.", e);
                }
                doWordFrequency();
            });

            task.setOnFailed(t -> {
                lblStatus.textProperty().unbind();
                btnStart.disableProperty().unbind();
                sourceBox.disableProperty().unbind();

                Throwable error = task.getException();
                lblStatus.setText("Error getting tags from Flickr.");
                logger.error("Error getting tags from Flickr.", error);

                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error getting tags from Flickr.");
                alert.setContentText("Error message was:\n%s\nSee logs for more detail."
                        .formatted(error.getMessage()));
                alert.showAndWait();
            });

            lblStatus.textProperty().bind(task.messageProperty());
            btnStart.disableProperty().bind(task.runningProperty());
            sourceBox.disableProperty().bind(task.runningProperty());
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    private void saveTagCache(Map<String, Integer> map) throws Exception {
        Path p = Paths.get(Main.configDir.toString(), TConstants.TAG_CACHE_FILENAME);
        logger.info("Saving tag cache to file {}", p);
        try (BufferedWriter out = Files.newBufferedWriter(p)) {
            for (String tag : map.keySet()) {
                if (!tag.isBlank()) {
                    out.write("%s: %s\n".formatted(map.get(tag), tag.replaceAll(":", "-")));
                }
            }
            out.flush();
            populateComboBox();
        }
    }

    public void doWordFrequency() {
        try {
            WordFrequencyTask task = new WordFrequencyTask();

            task.setOnSucceeded(t -> {
                lblStatus.textProperty().unbind();
                btnStart.disableProperty().unbind();
                sourceBox.disableProperty().unbind();
            });

            task.setOnFailed(t -> {
                lblStatus.textProperty().unbind();
                btnStart.disableProperty().unbind();
                sourceBox.disableProperty().unbind();

                Throwable error = task.getException();
                lblStatus.setText("Error while calculating word frequency.");
                logger.error("Error while calculating word frequency.", error);

                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error while calculating word frequency.");
                alert.setContentText("Error message was:\n%s\nSee logs for more detail."
                        .formatted(error.getMessage()));
                alert.showAndWait();
            });

            lblStatus.textProperty().bind(task.messageProperty());
            btnStart.disableProperty().bind(task.runningProperty());
            sourceBox.disableProperty().bind(task.runningProperty());
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public void doDeauthorize() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deauthorize?");
        alert.setHeaderText("Deauthorize Flickr?");
        alert.setContentText(
                "This will remove the saved OAuth token.\n" +
                        "After this, Tagerator will not work until you authorize it again.\n\n" +
                        "Do you want to continue?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Path oauthToken = Paths.get(Main.configDir.toString(), TConstants.OAUTH_TOKEN_NAME);
            if (Files.exists(oauthToken)) {
                try {
                    Files.delete(oauthToken);
                    Main.showScene(Main.TageratorScene.AUTHORIZE);
                } catch (IOException ioe) {
                    logger.error("There was an error while deleting the oauth token. path={}", oauthToken, ioe);
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error deleting OAuth token");
                    alert.setContentText("There was an error while trying to delete the saved token.\n" +
                            "Error message was\n" + ioe.getMessage() +
                            "\nSee logs for more detail.");
                    alert.showAndWait();
                }
            }
        }
    }

    public void btnBrowseAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files", "*.png", "*.jpg", "*.gif"));

        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null) {
            txtFile.setText(file.getAbsolutePath());
        }
    }
}
