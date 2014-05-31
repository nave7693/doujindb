package org.dyndns.doujindb.db.event;

import org.dyndns.doujindb.db.*;

public interface DataBaseListener
{
	public void recordAdded(Record record);
	public void recordDeleted(Record record);
	public void recordUpdated(Record record, UpdateData data);
	public void recordRecycled(Record record);
	public void recordRestored(Record record);

	public void databaseConnected();
	public void databaseDisconnected();
	public void databaseCommit();
	public void databaseRollback();
}
