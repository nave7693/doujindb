package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ContentContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Content;

@SuppressWarnings("serial")
public class ListContent extends RecordList<Content>
{
	private ContentContainer tokenIContent;
	
	public ListContent(ContentContainer token) throws DataBaseException
	{
		super(token.getContents(), Content.class);
		this.tokenIContent = token;
	}
	
	public boolean contains(Content item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Content> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Content)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Content)data.getTarget());
			break;
		}
	}
}
