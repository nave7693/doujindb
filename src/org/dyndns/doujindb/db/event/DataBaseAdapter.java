package org.dyndns.doujindb.db.event;

import org.dyndns.doujindb.db.Record;

public class DataBaseAdapter implements DataBaseListener
{
	@Override
	public void recordAdded(Record rcd) { }

	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd, UpdateData info) { }
	
	@Override
	public void recordRecycled(Record rcd) { }

	@Override
	public void recordRestored(Record rcd) { }

	@Override
	public void databaseConnected() { }

	@Override
	public void databaseDisconnected() { }

	@Override
	public void databaseCommit() { }

	@Override
	public void databaseRollback() { }
}