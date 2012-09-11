package org.dyndns.doujindb.ui.desk.panels;

import javax.swing.*;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.ui.desk.*;

@SuppressWarnings("serial")
public final class PanelEditor extends JPanel implements DataBaseListener
{
	private DataBaseListener child;
	
	public PanelEditor(WindowEx parent, WindowEx.Type type) throws DataBaseException
	{
		this(parent, type, null);
	}
	public PanelEditor(WindowEx parent, WindowEx.Type type, Object token) throws DataBaseException
	{
		super();
		switch(type)
		{
		case WINDOW_ARTIST:
		{
			child = new PanelArtist(this, (Artist)token);
			break;
		}
		case WINDOW_BOOK:
		{
			child = new PanelBook(this, (Book)token);
			break;
		}
		case WINDOW_CIRCLE:
		{
			child = new PanelCircle(this, (Circle)token);
			break;
		}
		case WINDOW_CONTENT:
		{
			child = new PanelContent(this, (Content)token);
			break;
		}
		case WINDOW_CONVENTION:
		{
			child = new PanelConvention(this, (Convention)token);
			break;
		}
		case WINDOW_PARODY:
		{
			child = new PanelParody(this, (Parody)token);
			break;
		}
		case WINDOW_RECYCLEBIN:
		{
			child = new PanelRecycleBin(parent, this);
			break;
		}
		}
	}
	
	@Override
	public void recordAdded(Record rcd)
	{
		child.recordAdded(rcd);
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		child.recordDeleted(rcd);
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		child.recordUpdated(rcd, data);
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		child.recordRecycled(rcd);
	}
	
	@Override
	public void recordRestored(Record rcd)
	{
		child.recordRestored(rcd);
	}
	
	@Override
	public void databaseConnected()
	{
		child.databaseConnected();
	}
	
	@Override
	public void databaseDisconnected()
	{
		child.databaseDisconnected();
	}
	
	@Override
	public void databaseCommit()
	{
		child.databaseCommit();
	}
	
	@Override
	public void databaseRollback()
	{
		child.databaseRollback();
	}
}