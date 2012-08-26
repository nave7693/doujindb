package org.dyndns.doujindb.db.event;

import org.dyndns.doujindb.db.*;

public interface DataBaseListener
{
	public void recordAdded(Record rcd);
	public void recordDeleted(Record rcd);
	public void recordUpdated(Record rcd);
	public void recordRecycled(Record rcd);
	public void recordRestored(Record rcd);

	public void databaseConnected();
	public void databaseDisconnected();
	public void databaseCommit();
	public void databaseRollback();
}