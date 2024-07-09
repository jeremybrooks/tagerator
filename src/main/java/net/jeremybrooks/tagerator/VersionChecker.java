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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Check for a new version of the program.
 *
 * @author jeremyb
 */
public class VersionChecker implements Runnable {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Run loop for the Runnable.
     *
     * <p>This method will check to see if there is a new version available.
     * It runs as a separate Thread so that it will not block the GUI if the
     * network connection is slow or missing.</p>
     */
    @Override
    public void run() {
        HttpURLConnection conn;
        String latestVersion;

        try {
            // WAIT A LITTLE BIT TO MAKE SURE THE MAIN WINDOW IS READY
            Thread.sleep(2000);
            conn = (HttpURLConnection) new URL(TConstants.VERSION_URL).openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                latestVersion = in.readLine();

                if (latestVersion.compareTo(Main.VERSION) > 0) {
                    logger.info("New version is available.");
                    MainWindow.getMainWindow().setUpdateAvailable(true);
                } else {
                    logger.info("No new version is available.");
                }

            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            logger.warn("ERROR WHILE CHECKING FOR A NEW VERSION.", e);
        }
    }
}
