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
	private static IDataBase instance;
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
	
	private static IDataBase getInstace()
	{
		return instance == null ? instance = new DataBaseImpl() : instance;
	}
	
	public static void doCommit() throws DataBaseException
	{
		Logger.logDebug(TAG + "call doCommit()");
		getInstace().doCommit();
	}

	public static void doRollback() throws DataBaseException
	{
		Logger.logDebug(TAG + "call doRollback()");
		getInstace().doRollback();
	}
	
	public static void doDelete(Record record) throws DataBaseException
	{
		Logger.logDebug(TAG + "call doDelete(" + record + ")");
		getInstace().doDelete(record);
	}
	
	public static RecordSet<Book> getBooks(QueryBook query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getBooks(" + query + ")");
		return getInstace().getBooks(query);
	}

	public static RecordSet<Circle> getCircles(QueryCircle query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getCircles(" + query + ")");
		return getInstace().getCircles(query);
	}

	public static RecordSet<Artist> getArtists(QueryArtist query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getArtists(" + query + ")");
		return getInstace().getArtists(query);
	}

	public static RecordSet<Parody> getParodies(QueryParody query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getParodies(" + query + ")");
		return getInstace().getParodies(query);
	}

	public static RecordSet<Content> getContents(QueryContent query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getContents(" + query + ")");
		return getInstace().getContents(query);
	}

	public static RecordSet<Convention> getConventions(QueryConvention query) throws DataBaseException
	{
		Logger.logDebug(TAG + "call getConventions(" + query + ")");
		return getInstace().getConventions(query);
	}
	
	public static RecordSet<Record> getRecycled() throws DataBaseException
	{
		Logger.logDebug(TAG + "call getRecycled()");
		return getInstace().getRecycled();
	}

	public static RecordSet<Record> getDeleted() throws DataBaseException
	{
		Logger.logDebug(TAG + "call getDeleted()");
		return getInstace().getDeleted();
	}

	public static RecordSet<Record> getModified() throws DataBaseException
	{
		Logger.logDebug(TAG + "call getModified()");
		return getInstace().getModified();
	}

	public static RecordSet<Record> getUncommitted() throws DataBaseException
	{
		Logger.logDebug(TAG + "call getUncommitted()");
		return getInstace().getUncommitted();
	}
	
	public static <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException
	{
		Logger.logDebug(TAG + "call doInsert(" + clazz + ")");
		return getInstace().doInsert(clazz);
	}
	
	public static void connect() throws DataBaseException
	{
		Logger.logDebug(TAG + "call connect()");
		getInstace().connect();
	}
	
	public static void disconnect() throws DataBaseException
	{
		Logger.logDebug(TAG + "call disconnect()");
		getInstace().disconnect();
	}
	
	public static boolean isConnected() throws DataBaseException
	{
		Logger.logDebug(TAG + "call isConnected()");
		return getInstace().isConnected();
	}
	
	public static String getConnection() throws DataBaseException
	{

		Logger.logDebug(TAG + "call getConnection()");
		return getInstace().getConnection();
	}

	public static boolean isAutocommit() throws DataBaseException
	{
		Logger.logDebug(TAG + "call isAutocommit()");
		return getInstace().isAutocommit();
	}
	
	public static void addDataBaseListener(DataBaseListener dbl)
	{
		Logger.logDebug(TAG + "call addDataBaseListener(" + dbl + ")");
		synchronized(listeners) {
			if(!listeners.contains(dbl))
				listeners.add(dbl);
		}
	}
	
	public static void removeDataBaseListener(DataBaseListener dbl)
	{
		Logger.logDebug(TAG + "call removeDataBaseListener(" + dbl + ")");
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
	
	static void fireRecordAdded(Record record)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_ADDED, record));
	}

	static void fireRecordDeleted(Record record)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_DELETED, record));
	}

	static void fireRecordUpdated(Record record, UpdateData info)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_UPDATED, record, info));
	}
	
	static void fireRecordRecycled(Record record)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_RECYCLED, record));
	}

	static void fireRecordRestored(Record record)
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.RECORD_RESTORED, record));
	}

	static void fireDatabaseConnected()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_CONNECTED));
	}

	static void fireDatabaseDisconnected()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_DISCONNECTED));
	}

	static void fireDatabaseCommit()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_COMMIT));
	}

	static void fireDatabaseRollback()
	{
		queue.offer(new DataBaseEvent(DataBaseEvent.Type.DATABASE_ROLLBACK));
	}

	static ContentAlias newContentAlias()
	{
		Logger.logDebug(TAG + "call newContentAlias()");
		return getInstace().newContentAlias();
	}
	
	static ConventionAlias newConventionAlias()
	{
		Logger.logDebug(TAG + "call newConventionAlias()");
		return getInstace().newConventionAlias();
	}
	
	static void deleteObject(Object o) throws DataBaseException
	{
		Logger.logDebug(TAG + "call deleteObject(" + o + ")");
		getInstace().deleteObject(o);
	}
}
