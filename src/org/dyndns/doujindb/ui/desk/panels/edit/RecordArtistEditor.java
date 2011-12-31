package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.containers.CntArtist;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

@SuppressWarnings("serial")
public class RecordArtistEditor extends JSplitPane implements Validable
{
	private CntArtist tokenIArtist;
	private DouzCheckBoxList<Artist> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	
	public RecordArtistEditor(CntArtist token) throws DataBaseException
	{
		super();
		this.tokenIArtist = token;
		searchField.setFont(font);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		});
		checkboxList = new DouzCheckBoxList<Artist>(Core.Database.getArtists(null), searchField);
		checkboxList.setSelectedItems(tokenIArtist.getArtists());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		super.setEnabled(false);
		validate();
	}
	
	public boolean contains(Artist item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Artist> iterator()
	{
		return checkboxList.getSelectedItems().iterator();
	}
	
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(ve.getType() != DouzEvent.DATABASE_ITEMCHANGED)
			checkboxList.validateUI(ve);
		else
		{
			try {
				checkboxList.setSelectedItems(tokenIArtist.getArtists());
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		}
		validate();
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		checkboxList.setEnabled(enabled);
		searchField.setEnabled(enabled);
	}
}