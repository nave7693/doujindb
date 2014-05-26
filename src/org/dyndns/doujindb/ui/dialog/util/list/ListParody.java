package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ParodyContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Parody;

@SuppressWarnings("serial")
public class ListParody extends RecordList<Parody>
{
	private ParodyContainer tokenIParody;
	
	public ListParody(ParodyContainer token) throws DataBaseException
	{
		super(token.getParodies(), Parody.class);
		this.tokenIParody = token;
	}
	
	public boolean contains(Parody item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Parody> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record r, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Parody)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Parody)data.getTarget());
			break;
		}
	}
}
