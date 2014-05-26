package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CircleContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Circle;

@SuppressWarnings("serial")
public class ListCircle extends RecordList<Circle>
{
	private CircleContainer tokenICircle;
	
	public ListCircle(CircleContainer token) throws DataBaseException
	{
		super(token.getCircles(), Circle.class);
		this.tokenICircle = token;
	}
	
	public boolean contains(Circle item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Circle> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Circle)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Circle)data.getTarget());
			break;
		}
	}
}
