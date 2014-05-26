package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ParodyContainer;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.ui.UI;

@SuppressWarnings("serial")
public class RecordParodyEditor extends JSplitPane implements DataBaseListener
{
	private ParodyContainer tokenIParody;
	private RecordList<Parody> recordList;
	private JTextField searchField = new JTextField("");
	protected static final Font font = UI.Font;
	
	public RecordParodyEditor(ParodyContainer token) throws DataBaseException
	{
		super();
		this.tokenIParody = token;
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
		recordList = new RecordList<Parody>(tokenIParody.getParodies(), Parody.class);
		setTopComponent(searchField);
		setBottomComponent(recordList);
		setDividerSize(0);
		super.setEnabled(false);
		validate();
	}
	
	public boolean contains(Parody item)
	{
		boolean contains = false;
		for(Object o : recordList.getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Parody> iterator()
	{
		return recordList.getRecords().iterator();
	}
	
	public RecordList<Parody> getRecordList()
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
	public void recordAdded(Record rcd) { }
	
	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			recordList.addRecord((Parody)data.getTarget());
			break;
		case UNLINK:
			recordList.removeRecord((Parody)data.getTarget());
			break;
		}
	}
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }

	@Override
	public void recordRecycled(Record rcd)
	{
		recordList.recordsChanged();
	}

	@Override
	public void recordRestored(Record rcd)
	{
		recordList.recordsChanged();
	}
}