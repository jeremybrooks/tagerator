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

/**
 * Data object holding a tag and its count.
 * @author jeremyb
 */
public class TagCount {
    private String tag;
    private int count;


    /**
     * @return the tag
     */
    public String getTag() {
	return tag;
    }


    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
	this.tag = tag;
    }


    /**
     * @return the count
     */
    public int getCount() {
	return count;
    }


    /**
     * @param count the count to set
     */
    public void setCount(int count) {
	this.count = count;
    }

}
