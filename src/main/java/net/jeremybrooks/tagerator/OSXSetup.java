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

// APPLE STUFF
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.Application;



/**
 * Handle Mac-specific events.
 *
 *
 * @author jeremyb
 */
public class OSXSetup {

    public OSXSetup() {
	Application app = Application.getApplication();

	app.setAboutHandler(new AboutHandler() {
	    public void handleAbout(AboutEvent ae) {
		new AboutDialog(null, true).setVisible(true);
	    }
	});

	/*
	app.setPreferencesHandler(new PreferencesHandler() {
	    public void handlePreferences(PreferencesEvent pe) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	});
	 */
    }


}
