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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import net.whirljack.common.util.IOUtil;
import org.apache.log4j.Logger;

/**
 * Load the tag cache on a background thread, displaying the results window
 * with the tags, count, and the date the tag cache file was saved.
 * 
 * @author jeremyb
 */
public class TagCacheLoader implements Runnable {

    Logger logger = Logger.getLogger(TagCacheLoader.class);

    public void run() {
	File f = new File(Main.configDir, TConstants.TAG_CACHE_FILENAME);
	if (f.exists()) {
	    List<TagCount> list = new ArrayList<TagCount>();
	    BufferedReader in = null;

	    try {
		in = new BufferedReader(new FileReader(f));
		String line = null;
		while ((line = in.readLine()) != null) {
		    int delim = line.lastIndexOf(",");
		    if (delim != -1) {
			String tag = line.substring(0, delim);
			int count = Integer.parseInt(line.substring(delim + 1).trim());

			TagCount tc = new TagCount();
			tc.setCount(count);
			tc.setTag(tag);
			list.add(tc);
		    }
		}

		// show the window
		ResultsWindow rsWin = new ResultsWindow(list.toArray(new TagCount[list.size()]), 100, 100);
		rsWin.setTitle(Main.getPropertyStore().getProperty(TConstants.LAST_DATE));
		MainWindow.setTotal(list.size());

		MainWindow.getMainWindow().enableTagCloud();

	    } catch (Exception e) {
		logger.error("Something went wrong while reading the tag cache file.", e);
	    } finally {
		IOUtil.close(in);
	    }
	} else {
	    logger.debug("No tag cache file found.");
	}
    }

}
