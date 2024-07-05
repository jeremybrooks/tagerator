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
package net.jeremybrooks.tagerator.workers;

import net.jeremybrooks.tagerator.BlockerPanel;
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.net.URL;


/**
 * This class performs Flickr authentication on a background thread.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class FlickrAuthenticatorWorker extends SwingWorker<Void, Void> {

    /**
     * Logging.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The blocker instance.
     */
    private final BlockerPanel blocker;

    /**
     * The parent dialog.
     */
    private final JDialog parent;


    /**
     * Create a new instance of FlickrAuthenticator.
     *
     * @param parent  the parent dialog.
     * @param blocker the blocker.
     */
    public FlickrAuthenticatorWorker(JDialog parent, BlockerPanel blocker) {
        this.parent = parent;
        this.blocker = blocker;
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * <p>The user's browser will be opened to the Flickr auth page. Once the
     * user has authorized Tagerator, control will return to the parent dialog.</p>
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
        blocker.block("Getting authentication URL...");
        try {
            URL url = FlickrHelper.getInstance().getAuthenticationURL();
            Desktop.getDesktop().browse(url.toURI());
            blocker.updateMessage("Waiting for authentication...");

            String verificationCode = JOptionPane.showInputDialog(this.parent,
                    "Your browser will open the Flickr site.\n" +
                            "After granting permission to Tagerator, enter the verification code and click OK.",
                            "Waiting For Authorization",
                    JOptionPane.INFORMATION_MESSAGE);

            blocker.updateMessage("Completing authentication...");

            FlickrHelper.getInstance().completeAuthentication(verificationCode);

            logger.info("Authentication success.");

        } catch (Exception e) {
            logger.error("Error while attempting to authenticate.", e);
            JOptionPane.showMessageDialog(this.parent,
                    "There was an error while attempting to authenticate.\n" +
                            "Please check the log file.",
                    e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }


    /**
     * Finished, so unblock and return control to the parent dialog.
     */
    @Override
    protected void done() {

        blocker.unBlock();
        this.parent.dispose();
        this.parent.setVisible(false);
    }

}
