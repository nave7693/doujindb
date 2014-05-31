package org.dyndns.doujindb.db.event;

import org.dyndns.doujindb.db.Record;

public class DataBaseAdapter implements DataBaseListener
{
	@Override
	public void recordAdded(Record record) { }

	@Override
	public void recordDeleted(Record record) { }
	
	@Override
	public void recordUpdated(Record record, UpdateData info) { }
	
	@Override
	public void recordRecycled(Record record) { }

	@Override
	public void recordRestored(Record record) { }

	@Override
	public void databaseConnected() { }

	@Override
	public void databaseDisconnected() { }

	@Override
	public void databaseCommit() { }

	@Override
	public void databaseRollback() { }
}
