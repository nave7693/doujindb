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
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.panels.util.*;

@SuppressWarnings("serial")
public class RecordCircleEditor extends JSplitPane implements DataBaseListener
{
	private CntCircle tokenICircle;
	private RecordList<Circle> recordList;
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
		    	recordList.filterChanged(searchField.getText());
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	recordList.filterChanged(searchField.getText());
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	recordList.filterChanged(searchField.getText());
		    }
		});
		recordList = new RecordList<Circle>(tokenICircle.getCircles(), Circle.class);
		setTopComponent(searchField);
		setBottomComponent(recordList);
		setDividerSize(0);
		super.setEnabled(false);
		validate();
	}
	
	public boolean contains(Circle item)
	{
		boolean contains = false;
		for(Object o : recordList.getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Circle> iterator()
	{
		return recordList.getRecords().iterator();
	}
	
	public RecordList<Circle> getRecordList()
	{
		return recordList;
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		recordList.setEnabled(enabled);
		searchField.setEnabled(enabled);
	}

	@Override
	public void recordAdded(Record rcd)
	{
		recordList.recordAdded(rcd);
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		recordList.recordDeleted(rcd);
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		if(tokenICircle.equals(rcd))
			try {
				//TODO ? recordList.setSelectedItems(tokenICircle.getCircles());
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		recordList.recordUpdated(rcd, data);
	}
	
	@Override
	public void databaseConnected()
	{
		recordList.databaseConnected();
	}
	
	@Override
	public void databaseDisconnected()
	{
		recordList.databaseDisconnected();
	}
	
	@Override
	public void databaseCommit()
	{
		recordList.databaseCommit();
	}
	
	@Override
	public void databaseRollback()
	{
		recordList.databaseRollback();
	}

	@Override
	public void recordRecycled(Record rcd)
	{
		recordList.recordRecycled(rcd);
	}

	@Override
	public void recordRestored(Record rcd)
	{
		recordList.recordRestored(rcd);
	}
}