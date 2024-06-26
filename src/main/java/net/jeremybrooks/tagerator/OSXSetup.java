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


import java.awt.Desktop;

/**
 * Handle Mac-specific events.
 *
 *
 * @author jeremyb
 */
public class OSXSetup {
	public OSXSetup() {
		Desktop.getDesktop().setAboutHandler(ae -> new AboutDialog(MainWindow.getMainWindow()).setVisible(true));

		Desktop.getDesktop().setQuitHandler((qe, qr) -> {
			MainWindow.getMainWindow().confirmAndExit(qr);
		});

		Desktop.getDesktop().setPreferencesHandler(pe -> new SettingsDialog(MainWindow.getMainWindow()).setVisible(true));
	}
}
