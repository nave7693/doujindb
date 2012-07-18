package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CntContent;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

@SuppressWarnings("serial")
public class RecordContentEditor extends JSplitPane implements DataBaseListener
{
	private CntContent tokenIContent;
	private DouzCheckBoxList<Content> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	
	public RecordContentEditor(CntContent token) throws DataBaseException
	{
		super();
		this.tokenIContent = token;
		searchField.setFont(font);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) {
		    	checkboxList.filterChanged();
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	checkboxList.filterChanged();
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	checkboxList.filterChanged();
		    }
		});
		checkboxList = new DouzCheckBoxList<Content>(Core.Database.getContents(null), searchField);
		checkboxList.setSelectedItems(tokenIContent.getContents());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		super.setEnabled(false);
		validate();
	}
	
	public boolean contains(Content item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Content> iterator()
	{
		return checkboxList.getSelectedItems().iterator();
	}
	
	public DouzCheckBoxList<Content> getCheckBoxList()
	{
		return checkboxList;
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		checkboxList.setEnabled(enabled);
		searchField.setEnabled(enabled);
	}

	@Override
	public void recordAdded(Record rcd)
	{
		checkboxList.recordAdded(rcd);
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		checkboxList.recordDeleted(rcd);
	}
	
	@Override
	public void recordUpdated(Record rcd)
	{
		if(tokenIContent.equals(rcd))
			try {
				checkboxList.setSelectedItems(tokenIContent.getContents());
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		checkboxList.recordUpdated(rcd);
	}
	
	@Override
	public void databaseConnected()
	{
		checkboxList.databaseConnected();
	}
	
	@Override
	public void databaseDisconnected()
	{
		checkboxList.databaseDisconnected();
	}
	
	@Override
	public void databaseCommit()
	{
		checkboxList.databaseCommit();
	}
	
	@Override
	public void databaseRollback()
	{
		checkboxList.databaseRollback();
	}
}