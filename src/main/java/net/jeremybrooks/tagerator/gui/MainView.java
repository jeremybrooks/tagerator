package net.jeremybrooks.tagerator.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.TConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class MainView {


    private static final Logger logger = LogManager.getLogger();

    @FXML
    public MenuItem mnuDeauthorize;

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
                }
            }
        }
    }
}
