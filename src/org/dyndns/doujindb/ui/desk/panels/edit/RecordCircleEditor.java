package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CntCircle;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

@SuppressWarnings("serial")
public class RecordCircleEditor extends JSplitPane implements DataBaseListener
{
	private CntCircle tokenICircle;
	private CheckBoxListEx<Circle> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	
	public RecordCircleEditor(CntCircle token) throws DataBaseException
	{
		super();
		this.tokenICircle = token;
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
		checkboxList = new CheckBoxListEx<Circle>(Core.Database.getCircles(null), searchField);
		checkboxList.setSelectedItems(tokenICircle.getCircles());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		super.setEnabled(false);
		validate();
	}
	
	public boolean contains(Circle item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Circle> iterator()
	{
		return checkboxList.getSelectedItems().iterator();
	}
	
	public CheckBoxListEx<Circle> getCheckBoxList()
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
		if(tokenICircle.equals(rcd))
			try {
				checkboxList.setSelectedItems(tokenICircle.getCircles());
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