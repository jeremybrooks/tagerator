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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.api.PhotosApi;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.jinx.dto.SearchParameters;
import net.jeremybrooks.tagerator.BlockerPanel;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.MainWindow;
import net.jeremybrooks.tagerator.ResultsWindow;
import net.jeremybrooks.tagerator.TagCount;
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import net.whirljack.common.util.IOUtil;
import org.apache.log4j.Logger;


/**
 * This class scans the user's Flickr stream, counting how many times each tag
 * is used.
 * This work is performed in a background thread.
 * 
 * @author jeremyb
 */
public class TagCollectorWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(TagCollectorWorker.class);

    /** The blocker instance. */
    private BlockerPanel blocker;

    /** The parent dialog. */
    private JFrame parent;

    private Map<String, TagCount> map;

    BufferedWriter out;

    /**
     * Create a new instance of TagCollectorWorker.
     *
     * @param parent the parent dialog.
     * @param blocker the blocker.
     */
    public TagCollectorWorker(JFrame parent, BlockerPanel blocker) {
	this.parent = parent;
	this.blocker = blocker;
	this.map = new HashMap<String, TagCount>();
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	blocker.block("Getting tags from photos (page 1/?) ...");
	try {
	    out = new BufferedWriter(new FileWriter(Main.tagCloudFile));

	    SearchParameters search = new SearchParameters();
	    search.setUserId(FlickrHelper.getInstance().getNSID());
	    search.setExtras(JinxConstants.EXTRAS_TAGS);
	    search.setPerPage(500);

	    Photos p = PhotosApi.getInstance().search(search);
	    while (p.getPhotos().size() > 0) {
	    	this.compileTags(p);
		out.newLine();
		out.flush();
		// uncomment these lines for a short run during dev cycles
//		JOptionPane.showMessageDialog(null, "DEBUG MODE");
//		if (true) break;
		
		search.setPage(p.getPage() + 1);
		blocker.updateMessage("Getting tags from photos (page " + search.getPage()
			+ "/" + (p.getPages()+1) + ") ...");
		
		p = PhotosApi.getInstance().search(search);
	    }
	    logger.info("got " + p.getPhotos().size() + " photos.");
	    logger.debug(p);

	} catch (Exception e) {
	    logger.error("Error while getting tags.", e);
	    JOptionPane.showMessageDialog(this.parent,
		    "There was an error while getting tags.\n"
		    + "Please check the log file.",
		    e.getMessage(),
		    JOptionPane.ERROR_MESSAGE);
	} finally {
	    IOUtil.flush(out);
	    IOUtil.close(out);
	}

	return null;
    }




    /**
     * Finished, so unblock and return control to the parent dialog.
     */
    @Override
    protected void done() {
	int size = map.values().size();
	ResultsWindow results = new ResultsWindow(
		map.values().toArray(new TagCount[size]),
		parent.getX() + 100, parent.getY() + 100);
	MainWindow.setTotal(size);
	blocker.unBlock();
    }


    /**
     * Keep a running total of the tag counts.
     * This could probably be more efficient.
     * @param p
     */
    private void compileTags(Photos p) {
	for (Photo photo : p.getPhotos()) {
	    for (String tag : photo.getTags().split(" ")) {
		try {
		    // write the tag to file
		    out.write(tag);
		    out.write(" ");
		} catch (Exception e) {
		    logger.error("Unable to write to file.", e);
		}
		TagCount data = null;
		if (this.map.containsKey(tag)) {
		    data = this.map.get(tag);
		    data.setCount(data.getCount() + 1);

		} else {
		    data = new TagCount();
		    data.setTag(tag);
		    data.setCount(1);
		}
		this.map.put(tag, data);

		logger.info("***** COUNT FOR " + tag + " is " + this.map.get(tag).getCount());
	    }
	}
    }

}
