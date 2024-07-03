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

import net.jeremybrooks.tagerator.helpers.FlickrHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;


/**
 * Display a JTable with the tag count results.
 * 
 * @author jeremyb
 */
public class ResultsWindow {

    private Logger logger = LogManager.getLogger();

    private static TagCount[] data;

    private static String[] columns = {"Tag", "Count"};

    private JFrame frame = null;

    private final JXTable table;


    public ResultsWindow(TagCount[] data, int x, int y) {
	ResultsWindow.data = data;
	table = new JXTable(new SampleTableModel());
	HighlighterPipeline highlighters = new HighlighterPipeline();
	highlighters.addHighlighter(new AlternateRowHighlighter());
	table.setHighlighters(highlighters);
	frame = new JFrame();
	frame.getContentPane().add(new JScrollPane(table));
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	// Open a browser to a Flickr search page on double click
	table.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseClicked(MouseEvent evnt) {
		if (evnt.getClickCount() > 1) {
		    doSearch((String) table.getValueAt(table.getSelectedRow(), 0));
		}
	    }

	});

	// Override copy to get only the first column in the table
	table.addKeyListener(new KeyAdapter() {

	    @Override
	    public void keyReleased(KeyEvent event) {
		boolean ctrl = false;
		if (System.getProperty("mrj.version") != null) {
		    if (event.isMetaDown()) {
			ctrl = true;
		    }
		} else {
		    if (event.isControlDown()) {
			ctrl = true;
		    }
		}
		if (ctrl && event.getKeyCode() == KeyEvent.VK_C) {
		    if (table.getCellEditor() != null) {
			table.getCellEditor().cancelCellEditing();
		    }
		    String tag = (String) table.getValueAt(table.getSelectedRow(), 0);
		    StringSelection sel = new StringSelection(tag);
		    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		}
	    }

	});

	frame.setBounds(x, y, 400, 600);
	frame.setVisible(true);
    }


    /**
     * Table Model for tag/count.
     */
    private static class SampleTableModel extends AbstractTableModel {

	public int getColumnCount() {
	    return columns.length;
	}


	@Override
	public String getColumnName(int column) {
	    return columns[column];
	}


	public int getRowCount() {
	    return data.length;
	}


	public Object getValueAt(int rowIndex, int columnIndex) {


	    Object result = null;
	    switch (columnIndex) {

		case 0:
		    result = data[rowIndex].getTag();
		    break;
		case 1:
		    result = data[rowIndex].getCount();
		    break;
		default:
		    break;
	    }

	    return result;
	}

    }


    /*
     * Open a browser window to a Flickr search for the tag.
     *
     */
    private void doSearch(String tag) {
	StringBuilder sb = new StringBuilder();
	sb.append("https://www.flickr.com/search/?q=").append(tag);
	sb.append("&w=").append(FlickrHelper.getInstance().getNSID());
	sb.append("&m=tags");

	try {
		Desktop.getDesktop().browse(new URI(sb.toString()));
	} catch (Exception e) {
	    logger.error("Could not open browser.", e);
	    JOptionPane.showMessageDialog(frame,
		    "There was an error while open the web browser.\n"
		    + "Check the logs to see the error message.",
		    "Could not open browser",
		    JOptionPane.ERROR_MESSAGE);

	}
    }


    public void setTitle(String title) {
	this.frame.setTitle(title);
    }

}
