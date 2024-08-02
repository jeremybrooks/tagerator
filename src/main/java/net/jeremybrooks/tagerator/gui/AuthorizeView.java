package net.jeremybrooks.tagerator.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.net.URISyntaxException;
import java.net.URL;

public class AuthorizeView {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    Button btnAuthorize;

    @FXML
    Button btnVerify;

    @FXML
    TextField txtCode;

    public void doAuthorize(ActionEvent actionEvent) {
        URL url = null;
        try {
            url = FlickrHelper.getInstance().getAuthenticationURL();
            Desktop.getDesktop().browse(url.toURI());
            txtCode.setDisable(false);
            txtCode.requestFocus();
        } catch (URISyntaxException use) {
           logger.error("URI syntax error for URL {}", url, use);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("URI Syntax Error");
            alert.setHeaderText(use.getMessage());
            alert.setContentText(
                    "There was an error while trying to open a browser to\n" +
                    "the Flickr authorization URL.");
            alert.showAndWait();
        } catch (Exception e) {
            logger.error("Could not get authorization URL", e);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authorization Error");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Could not get the authorization URL");
            alert.showAndWait();
        }
    }

    public void codeTyped(KeyEvent keyEvent) {
        btnVerify.setDisable(txtCode.getText().isEmpty());
    }

    public void doVerify(ActionEvent actionEvent) {
        String code = txtCode.getText();
        try {
            if (code.isEmpty()) {
               throw new Exception("No verification code entered.");
            }
            FlickrHelper.getInstance().completeAuthentication(code);
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Authorization Successful");
            alert.setContentText("Tagerator is now authorized to access your Flickr photos.");
            alert.showAndWait();
            Main.showScene(Main.TageratorScene.MAIN);
        } catch (Exception e) {
            logger.error("Authorization completion failed using code {}", code, e);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Code Verification Error");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Could not verify the authorization code.");
            alert.showAndWait();
        }
    }
}
