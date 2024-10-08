/*
 * Tagerator is Copyright 2011 by Jeremy Brooks
 *
 * This file is part of Tagerator.
 *
 *  Tagerator is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tagerator is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tagerator.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jeremybrooks.tagerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import net.jeremybrooks.common.PropertyStore;
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * @author jeremyb
 */
public class Main extends Application {
    public enum TageratorScene {
        MAIN,
        AUTHORIZE;
    }

    public static String VERSION;

    public static Path configDir;
    public static Path tagCacheFile;

//    public static Path tagCloudFile;

    private static Stage primaryStage;
    private static final Logger logger = LogManager.getLogger();


    private static PropertyStore props = null;


    /**
     * Tagerator entry point. No command line arguments are supported.
     *
     * @param args the command line arguments
     */
    public static void main(String... args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
       Main.primaryStage = primaryStage;

        // If running on a Mac, set up the event handler
        if (System.getProperty("mrj.version") != null) {
            new OSXSetup();
        }

        // ADD SHUTDOWN HOOK
        //Runtime.getRuntime().addShutdownHook(new Thread(new Reaper(), "ReaperThread"));

        // SET VERSION
        try {
            Properties appProps = new Properties();
            appProps.load(Main.class.getClassLoader().getResourceAsStream("net/jeremybrooks/tagerator/VERSION"));
            Main.VERSION = appProps.getProperty("app.version");
        } catch (Exception e) {
            Main.VERSION = "0.0.0";
        }

        try {
            // SET CONFIG DIR BASED ON USER HOME
            configDir = Paths.get(System.getProperty("user.home"), ".tagerator");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            tagCacheFile = Paths.get(configDir.toString(), TConstants.TAG_CACHE_FILENAME);

            props = new PropertyStore(configDir.toString(), "tagerator.properties");
            logger.info("Tagerator version " + Main.VERSION + " starting.");
            // Ask about automatic updates
            if (props.getProperty(TConstants.CHECK_FOR_UPDATES) == null) {
                int yesno = JOptionPane.showOptionDialog(null,
                        "Would you like Tagerator to check for updates when it starts?",
                        "Check for Updates?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, null);
                props.setProperty(TConstants.CHECK_FOR_UPDATES, yesno == JOptionPane.YES_OPTION);
            }

            // set up Jinx
            FlickrHelper.getInstance();

            // TODO is this needed?
            // create a default color config file if needed
            createColorSchemeFile();

            // Check for updates
            if (props.getPropertyAsBoolean(TConstants.CHECK_FOR_UPDATES)) {
                (new Thread(new VersionChecker(), "VersionCheckerThread")).start();
            }

            // If there is a saved token, show the main view
            // Otherwise, show the authorization view
            FXMLLoader fxmlLoader;
            if (FlickrHelper.getInstance().loadOauthToken()) {
                showScene(TageratorScene.MAIN);
            } else {
                showScene(TageratorScene.AUTHORIZE);
            }
        } catch (Throwable t) {
            logger.fatal("A fatal error has occurred.", t);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unrecoverable Error");
            alert.setHeaderText(t.getMessage());
            alert.setContentText(
                    "An unrecoverable error has occurred.\n"
                            + "Please send the logs to tagerator@jeremybrooks.net\n\n"
                            + "This program will now exit.");
            alert.showAndWait();
            System.exit(2);
        }

    }

    public static void showScene(TageratorScene sceneName) throws IOException {
        FXMLLoader fxmlLoader;
        String name = null;
        String title = null;

        switch (sceneName) {
            case MAIN:
                name = "/net/jeremybrooks/tagerator/gui/main-view.fxml";
                title = String.format("Tagerator - %s", Main.VERSION);
                break;
            case AUTHORIZE:
                name = "/net/jeremybrooks/tagerator/gui/authorize-view.fxml";
                title = "Authorize";
                break;
        }
        fxmlLoader = new FXMLLoader(Main.class.getResource(name));
        primaryStage.setTitle(title);
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static PropertyStore getPropertyStore() {
        return Main.props;
    }


    private static void createColorSchemeFile() {
        try (BufferedWriter out = Files.newBufferedWriter(Paths.get(configDir.toString(), TConstants.COLOR_SCHEME_FILENAME))) {
            out.write("# Colors for tag cloud generation");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Each line of this file represents a color scheme.");
            out.newLine();
            out.write("# A color scheme has a name and a list of colors that will be used.");
            out.newLine();
            out.write("# The name is on the left side of the = sign, and the colors on the right.");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# A color definition consists of one to four digits, separated by");
            out.newLine();
            out.write("# commas. Each color definition is separated with a semicolon.");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# A color defined by one digit is a shade of gray from");
            out.newLine();
            out.write("# black (0) to white (255). A color defined by two");
            out.newLine();
            out.write("# digits is a shade of gray from black (0) to white (255)");
            out.newLine();
            out.write("# and an alpha channel. So this line would define a color scheme");
            out.newLine();
            out.write("# called \"Grayscale\" consisting of four different shades of grey:");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Grayscale=25; 50; 128; 200");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# This line would define a scheme called \"Grayscale Alpha\" consisting");
            out.newLine();
            out.write("# of three shades from solid black to a more transparent black:");
            out.newLine();
            out.write("# Grayscale Alpha=0, 0; 0, 128; 0, 200");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# A color defined by three digits is an RGB representation of the color.");
            out.newLine();
            out.write("# So this scheme would contain red, green, and blue colors:");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# RGB=255,0,0; 0,255,0; 0,0,255");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# A color scheme defined by four digits is an RGB representation plus");
            out.newLine();
            out.write("# an alpha channel. So this would define a color scheme with varying");
            out.newLine();
            out.write("# shades of red:");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Some Reds=255,0,0,10; 255,0,0,100; 255,0,0,180");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Color schemes can contain any combintaion of color definitions:");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Red And Black=255,0,0; 0");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Lines starting with a # are comments, and are ignored.");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Numbers can be represented in decimal or hex:");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Mixed Up=0,0,0; #ff, #a3, #bb");
            out.newLine();
            out.write("#");
            out.newLine();
            out.write("# Have fun!");
            out.newLine();
            out.write("#");
            out.newLine();
            out.newLine();
            out.write("# Red, Yellow, Blue");
            out.newLine();
            out.write("Primary=255,0,0; 255,255,0; 0,0,255");
            out.newLine();
            out.newLine();
            out.write("# Purple, Orange, Green");
            out.newLine();
            out.write("Secondary=138,43,226; 255,165,0; 0,255,0");
            out.newLine();
            out.newLine();
            out.write("# Some shades of green");
            out.newLine();
            out.write("Garden=#08,#6E,#00; #36,#68,#0E; #3A,#BA,#00; #BA,#ED,#BB; #92,#C8,#52");
            out.newLine();
            out.newLine();
            out.write("# An autumn inspired color palette");
            out.newLine();
            out.write("Autumn=#6A,#65,#00; #CA,#49,#00; #FF,#6A,#00; #90,#FF,#FF; #7A,#AC,#3D");
            out.newLine();
            out.newLine();
            out.write("# Shades of blue");
            out.newLine();
            out.write("Ocean=#00,#62,#C8; #1C,#93,#C6; #1B,#6D,#EA; #A2,#D8,#F7; #00,#00,#5F");
            out.newLine();
            out.newLine();
            out.write("# Rainbow");
            out.newLine();
            out.write("Rainbow=255,0,0; 255,165,0; 255,255,0; 0,0,255; 0,255,0; 138,43,226");
            out.newLine();
            out.newLine();
            out.write("# Grayscale");
            out.newLine();
            out.write("Black And White=0; 25; 50; 100; 128; 150; 200; 222");
            out.newLine();
        } catch (Exception e) {
            logger.warn("Could not create color scheme file.", e);
        }
    }
}
