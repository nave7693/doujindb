package org.dyndns.doujindb.db;

import java.util.concurrent.*;

import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.cayenne.ContentAlias;
import org.dyndns.doujindb.db.cayenne.ConventionAlias;
import org.dyndns.doujindb.log.*;

public final class DataBase
{
	private static IDataBase instance = new DataBaseImpl();
	private static CopyOnWriteArraySet<DataBaseListener> listeners = new CopyOnWriteArraySet<DataBaseListener>();
	private static ConcurrentLinkedQueue<DataBaseEvent> queue = new ConcurrentLinkedQueue<DataBaseEvent>();
	
	private static final String TAG = "DataBase : ";
	
	static
	{
		new Thread() {
			@Override
			public void run() {
				super.setName("database-eventpoller");
				while(true) {
					try {
						Thread.sleep(1);
						if(queue.isEmpty())
							continue;
						process(queue.poll());
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {
						e.printStackTrace();
					}
				}
			}
			private void process(DataBaseEvent event) {
				try {
					synchronized(listeners) {
						switch(event.type) {
							case RECORD_ADDED:
								for(DataBaseListener dbl : listeners)
									dbl.recordAdded(event.record);
								break;
							case RECORD_DELETED:
								for(DataBaseListener dbl : listeners)
									dbl.recordDeleted(event.record);
								break;
							case RECORD_UPDATED:
								for(DataBaseListener dbl : listeners)
									dbl.recordUpdated(event.record, event.data);
								break;
							case RECORD_RECYCLED:
								for(DataBaseListener dbl : listeners)
									dbl.recordRecycled(event.record);
								break;
							case RECORD_RESTORED:
								for(DataBaseListener dbl : listeners)
									dbl.recordRestored(event.record);
								break;
							case DATABASE_CONNECTED:
								for(DataBaseListener dbl : listeners)
									dbl.databaseConnected();
								break;
							case DATABASE_DISCONNECTED:
								for(DataBaseListener dbl : listeners)
									dbl.databaseDisconnected();
								break;
							case DATABASE_COMMIT:
								for(DataBaseListener dbl : listeners)
									dbl.databaseCommit();
								break;
							case DATABASE_ROLLBACK:
								for(DataBaseListener dbl : listeners)
									dbl.databaseRollback();
								break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public static DataBaseContext getContext(String ID) throws DataBaseException
	{
		return instance.getContext(ID);
	}
	
	public static void doCommit() throws DataBaseException
	{
		instance.doCommit();
	}

	public static void doRollback() throws DataBaseException
	{
		instance.doRollback();
	}
	
	public static void doDelete(Record record) throws DataBaseException
	{
		instance.doDelete(record);
	}
	
	public static RecordSet<Book> getBooks(QueryBook query) throws DataBaseException
	{
		return instance.getBooks(query);
	}

	public static RecordSet<Circle> getCircles(QueryCircle query) throws DataBaseException
	{
		return instance.getCircles(query);
	}

	public static RecordSet<Artist> getArtists(QueryArtist query) throws DataBaseException
	{
		return instance.getArtists(query);
	}

	public static RecordSet<Parody> getParodies(QueryParody query) throws DataBaseException
	{
		return instance.getParodies(query);
	}

	public static RecordSet<Content> getContents(QueryContent query) throws DataBaseException
	{
		return instance.getContents(query);
	}

	public static RecordSet<Convention> getConventions(QueryConvention query) throws DataBaseException
	{
		return instance.getConventions(query);
	}
	
	public static RecordSet<Record> getRecycled() throws DataBaseException
	{
		return instance.getRecycled();
	}

	public static RecordSet<Record> getDeleted() throws DataBaseException
	{
		return instance.getDeleted();
	}

	public static RecordSet<Record> getModified() throws DataBaseException
	{
		return instance.getModified();
	}

	public static RecordSet<Record> getUncommitted() throws DataBaseException
	{
		return instance.getUncommitted();
	}
	
	public static <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException
	{
		return instance.doInsert(clazz);
	}
	
	public static void connect() throws DataBaseException
	{
		instance.connect();
	}
	
	public static void disconnect() throws DataBaseException
	{
		instance.disconnect();
	}
	
	public static boolean isConnected() throws DataBaseException
	{
		return instance.isConnected();
	}
	
	public static String getConnection() throws DataBaseException
	{
		return instance.getConnection();
	}

	public static boolean isAutocommit() throws DataBaseException
	{
		return instance.isAutocommit();
	}
	
	public static void addDataBaseListener(DataBaseListener dbl)
	{
		synchronized(listeners) {
			if(!listeners.contains(dbl))
				listeners.add(dbl);
		}
	}
	
	public static void removeDataBaseListener(DataBaseListener dbl)
	{
		synchronized(listeners) {
			listeners.remove(dbl);
		}
	}
	
	private static final class DataBaseEvent
	{
		private enum Type
		{
			RECORD_ADDED,
			RECORD_DELETED,
			RECORD_UPDATED,
			RECORD_RECYCLED,
			RECORD_RESTORED,
			DATABASE_CONNECTED,
			DATABASE_DISCONNECTED,
			DATABASE_COMMIT,
			DATABASE_ROLLBACK
		}
		
		private Type type;
		private Record record;
		private UpdateData data;
		
		private DataBaseEvent(Type type)
		{
			this(type, null);
		}
		
		private DataBaseEvent(Type type, Record record)
		{
			this.type = type;
			this.record = record;
		}
		
		private DataBaseEvent(Type type, Record record, UpdateData data)
		{
			this.type = type;
			this.record = record;
			this.data = data;
		}
	}
	
	static void _recordAdded(Record rcd)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_ADDED, rcd));
	}

	static void _recordDeleted(Record rcd)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_DELETED, rcd));
	}

	static void _recordUpdated(Record rcd, UpdateData info)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_UPDATED, rcd, info));
	}
	
	static void _recordRecycled(Record rcd)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_RECYCLED, rcd));
	}

	static void _recordRestored(Record rcd)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_RESTORED, rcd));
	}

	static void _databaseConnected()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_CONNECTED));
	}

	static void _databaseDisconnected()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_DISCONNECTED));
	}

	static void _databaseCommit()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_COMMIT));
	}

	static void _databaseRollback()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_ROLLBACK));
	}

	static ContentAlias newContentAlias() {
		return instance.newContentAlias();
	}
	
	static ConventionAlias newConventionAlias() {
		return instance.newConventionAlias();
	}
	
	static void deleteObject(Object o) throws DataBaseException
	{
		instance.deleteObject(o);
	}
}
