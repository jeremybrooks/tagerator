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
package net.jeremybrooks.tagerator.tasks;

import javafx.concurrent.Task;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.api.PhotosApi;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.Photos;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.TConstants;
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This class scans the user's Flickr stream, counting how many times each tag
 * is used.
 * This work is performed in a background thread.
 *
 * @author jeremyb
 */
public class TagCollectorTask extends Task<Map<String, Integer>> {

    /**
     * Logging.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The blocker instance.
     */
//    private final BlockerPanel blocker;


    private final Map<String, Integer> map;

    /**
     * Create a new instance of TagCollectorWorker.
     *
     */
    public TagCollectorTask() {
        this.map = new HashMap<>();
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Map<String, Integer> call() throws Exception {
        int count = 0;
        updateMessage("Searching....");
        boolean debug = false;
        String debugFlag = System.getenv("tagerator.debug");
        if (debugFlag != null && debugFlag.equalsIgnoreCase("true")) {
            debug = true;
        }
        logger.info("Debug mode is {}", debug);
//        blocker.block("Getting tags from photos (page 1/?) ...");

        try (BufferedWriter out = Files.newBufferedWriter(Main.tagCloudFile)) {
            SearchParameters search = new SearchParameters();
            search.setUserId(FlickrHelper.getInstance().getNSID());
            search.setExtras(Set.of(JinxConstants.PhotoExtras.tags));
            search.setPerPage(500);

            PhotosApi photosApi = FlickrHelper.getInstance().getPhotosApi();
            Photos p = photosApi.search(search);
            int page = 1;
            while (page <= p.getPages()) {
                count += p.getPhotoList().size();
                updateMessage(String.format("Processing page %d/%d - %d/%d photos", page, p.getPages(),
                        count, p.getTotal()));
                compileTags(p, out);
                out.newLine();
                out.flush();
                if (debug) {
                    break;
                }
                page++;
                if (page <= p.getPages()) {
                    search.setPage(page);
                    p = photosApi.search(search);
                }
            }
            logger.info("got {} photos.", p.getPhotoList().size());
            logger.debug(p);

        } catch (Exception e) {
            logger.error("Error while getting tags.", e);
//            JOptionPane.showMessageDialog(this.parent,
//                    "There was an error while getting tags.\n"
//                            + "Please check the log file.",
//                    e.getMessage(),
//                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }


    /**
     * Finished, so unblock and return control to the parent dialog.
     */
    @Override
    protected void done() {
        int size = map.values().size();
//        ResultsWindow results = new ResultsWindow(
//                map.values().toArray(new TagCount[size]),
//                parent.getX() + 100, parent.getY() + 100);
//        MainWindow.setTotal(size);

//        results.setTitle(Main.getPropertyStore().getProperty(TConstants.LAST_DATE));

        new Thread(new WriteTagCache()).start();

//        blocker.unBlock();
    }


    /**
     * Keep a running total of the tag counts.
     * This could probably be more efficient.
     *
     * @param p   photo list to process.
     * @param out the buffered writer instance used to write tags to file.
     */
    private void compileTags(Photos p, BufferedWriter out) {
        for (Photo photo : p.getPhotoList()) {
            for (String tag : photo.getTags().split(" ")) {
                try {
                    // write the tag to file
                    out.write(tag);
                    out.write(" ");
                } catch (Exception e) {
                    logger.error("Unable to write to file.", e);
                }
                if (this.map.containsKey(tag)) {
                    int count = map.get(tag);
                    count++;
                    map.put(tag, count);
                } else {
                    map.put(tag, 1);
                }

                logger.info("***** COUNT FOR {} is {}", tag, map.get(tag));
            }
        }
    }



    class WriteTagCache implements Runnable {
        /**
         * Save the tags in a file.
         */
        public void run() {
            Path p = Paths.get(Main.configDir.toString(), TConstants.TAG_CACHE_FILENAME);
            try (BufferedWriter out = Files.newBufferedWriter(p)) {
                for (String tag : map.keySet()) {
                    out.write(tag);
                    out.write(",");
                    out.write(Integer.toString(map.get(tag)));
                    out.newLine();
                    out.flush();
                }
            } catch (Exception e) {
                logger.warn("Could not save tag cache.", e);
            }
        }
    }
}
