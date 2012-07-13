package org.dyndns.doujindb.ui.desk.panels;

import javax.swing.*;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.event.*;

@SuppressWarnings("serial")
public final class EditPanel extends JPanel implements Validable
{
	private Validable child;
	
	public EditPanel(DouzWindow parent, DouzWindow.Type type) throws DataBaseException
	{
		this(parent, type, null);
	}
	public EditPanel(DouzWindow parent, DouzWindow.Type type, Object token) throws DataBaseException
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
		case WINDOW_MEDIAMANAGER:
		{
			child = new PanelMediaManager(parent, this);
			break;
		}
		}
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		child.validateUI(ve);
		super.validate();
	}
}