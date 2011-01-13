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

import java.io.BufferedReader;
import java.io.File;
import java.util.Properties;
import javax.swing.JOptionPane;
import net.jeremybrooks.jinx.Jinx;
import net.whirljack.common.PropertyStore;
import net.whirljack.common.util.IOUtil;
import net.whirljack.common.util.NetUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 *
 * @author jeremyb
 */
public class Main {

    public static String VERSION;

    public static File configDir;

    public static File tagCloudFile;

    private static Logger logger = Logger.getLogger(Main.class);

    private static final String FLICKR_KEY = "8f47e79509c0ad500433d3dba1d0e53f";

    private static final String FLICKR_SECRET = "ef0bbb2b6b37d19b";

    private static PropertyStore props = null;

    /**
     * Tagerator entry point. No command line arguments are supported.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	// set up logging
	// If running on a Mac, set up the event handler
	if (System.getProperty("mrj.version") != null) {
	    new OSXSetup();
	}

	// ADD SHUTDOWN HOOK
	//Runtime.getRuntime().addShutdownHook(new Thread(new Reaper(), "ReaperThread"));

	// SET VERSION
	BufferedReader in = null;
	try {
	    Properties appProps = new Properties();
	    appProps.load(Main.class.getClassLoader().getResourceAsStream("net/jeremybrooks/tagerator/VERSION"));
	    Main.VERSION = appProps.getProperty("app.version");

	} catch (Exception e) {
	    Main.VERSION = "0.0.0";
	} finally {
	    IOUtil.close(in);
	}

	// SET CONFIG DIR BASED ON USER HOME
	Main.configDir = new File(System.getProperty("user.home"), ".tagerator");
	if (!Main.configDir.exists()) {
	    Main.configDir.mkdirs();
	}
	tagCloudFile = new File(configDir, "tagcloud.txt");

	try {
	    props = new PropertyStore(Main.configDir, "tagerator.properties");

	    // SET UP LOGGING
	    Properties p = new Properties();
	    p.setProperty("log4j.rootLogger", "DEBUG,FILE");
	    p.setProperty("log4j.appender.FILE", "org.apache.log4j.RollingFileAppender");
	    p.setProperty("log4j.appender.FILE.Threshold", "DEBUG");
	    p.setProperty("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
	    p.setProperty("log4j.appender.FILE.layout.ConversionPattern", "%p %c [%t] %d{ISO8601} - %m%n");
	    p.setProperty("log4j.appender.FILE.File", (new File(Main.configDir, "tagerator.log")).getAbsolutePath());
	    p.setProperty("log4j.appender.FILE.MaxFileSize", "1MB");
	    p.setProperty("log4j.appender.FILE.MaxBackupIndex", "2");

	    PropertyConfigurator.configure(p);

	    logger.info("Logging configuration: " + p);
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

	    // Set up proxy
	    if (props.getPropertyAsBoolean(TConstants.USE_PROXY)) {
		String host = props.getProperty(TConstants.PROXY_HOST);
		String port = props.getProperty(TConstants.PROXY_PORT);
		String user = props.getProperty(TConstants.PROXY_USER);
		String pass = props.getProperty(TConstants.PROXY_PASS);

		logger.info("Using proxy " + host + ":" + port);

		NetUtil.enableProxy(host, port, user, pass.toCharArray());
	    }

	    Jinx.getInstance().init(FLICKR_KEY, FLICKR_SECRET);

	    // Finally, show the main window
	    java.awt.EventQueue.invokeLater(new Runnable() {

		@Override
		public void run() {
		    MainWindow main = new MainWindow();
		    main.setVisible(true);
		    main.doAuth();
		}

	    });

	    // Check for updates
	    if (props.getPropertyAsBoolean(TConstants.CHECK_FOR_UPDATES)) {
		(new Thread(new VersionChecker(), "VersionCheckerThread")).start();
	    }
	    
	} catch (Throwable t) {
	    System.out.println("A fatal error has occurred.");
	    t.printStackTrace();
	    logger.fatal("A fatal error has occurred.");
	    JOptionPane.showMessageDialog(null,
		    "An unrecoverable error has occurred.\n"
		    + t.getMessage() + "\n"
		    + "Please send the logs to tagerator@jeremybrooks.net\n\n"
		    + "This program will now exit.",
		    "Unrecoverable Error",
		    JOptionPane.ERROR_MESSAGE);
	    System.exit(2);
	}
    }

    public static PropertyStore getPropertyStore() {
	return Main.props;
    }
    
}
