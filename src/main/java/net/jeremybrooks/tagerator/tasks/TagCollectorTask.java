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
import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger();

    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Map<String, Integer> call() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        updateMessage("Searching....");
        int photoCount = 0;
        boolean debug = false;
        String debugFlag = System.getenv("tagerator.debug");
        if (debugFlag != null && debugFlag.equalsIgnoreCase("true")) {
            debug = true;
        }
        logger.info("Debug mode is {}", debug);

        SearchParameters search = new SearchParameters();
        search.setUserId(FlickrHelper.getInstance().getNSID());
        search.setExtras(Set.of(JinxConstants.PhotoExtras.tags));
        search.setPerPage(500);

        PhotosApi photosApi = FlickrHelper.getInstance().getPhotosApi();
        Photos p = photosApi.search(search);
        int page = 1;
        while (page <= p.getPages()) {
            photoCount += p.getPhotoList().size();
            updateMessage(String.format("Processing page %d/%d - %d/%d photos", page, p.getPages(),
                    photoCount, p.getTotal()));
            // update the tag count for each photo in the photo list
            for (Photo photo : p.getPhotoList()) {
                for (String tag : photo.getTags().split(" ")) {
                    if (map.containsKey(tag)) {
                        int tagCount = map.get(tag);
                        tagCount++;
                        map.put(tag, tagCount);
                    } else {
                        map.put(tag, 1);
                    }
                }
            }
            if (debug) {
                break;
            }
            page++;
            if (page <= p.getPages()) {
                search.setPage(page);
                p = photosApi.search(search);
            }
        }

        updateMessage("Complete");

        return map;
    }
}
