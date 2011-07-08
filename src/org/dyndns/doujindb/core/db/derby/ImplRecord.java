package org.dyndns.doujindb.core.db.derby;

import org.dyndns.doujindb.db.DouzRecord;

public abstract class ImplRecord implements DouzRecord, Comparable<DouzRecord>
{
	long ID;
	
	public ImplRecord()
	{
		ID = -1L;
	}
	
	@Override
	public void setID(long id)
	{
		ID = id;
	}
	
	@Override
	public int compareTo(DouzRecord o)
	{
		if(this.getID() == null)
			if(o.getID() == null)
				return 0;
			else
				return -1;
		if(o.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(o.getID());
	}
}
