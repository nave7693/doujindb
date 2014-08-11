package org.dyndns.doujindb.db;

import java.util.concurrent.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.db.cayenne.ContentAlias;
import org.dyndns.doujindb.db.cayenne.ConventionAlias;

public final class DataBase
{
	private static IDataBase instance;
	private static CopyOnWriteArraySet<DataBaseListener> listeners = new CopyOnWriteArraySet<DataBaseListener>();
	private static ConcurrentLinkedQueue<DataBaseEvent> queue = new ConcurrentLinkedQueue<DataBaseEvent>();
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataBase.class);
	
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
		LOG.debug("call doCommit()");
		getInstace().doCommit();
	}

	public static void doRollback() throws DataBaseException
	{
		LOG.debug("call doRollback()");
		getInstace().doRollback();
	}
	
	public static void doDelete(Record record) throws DataBaseException
	{
		LOG.debug("call doDelete({})", record);
		getInstace().doDelete(record);
	}
	
	public static RecordSet<Book> getBooks(QueryBook query) throws DataBaseException
	{
		LOG.debug("call getBooks({})", query);
		return getInstace().getBooks(query);
	}

	public static RecordSet<Circle> getCircles(QueryCircle query) throws DataBaseException
	{
		LOG.debug("call getCircles({})", query);
		return getInstace().getCircles(query);
	}

	public static RecordSet<Artist> getArtists(QueryArtist query) throws DataBaseException
	{
		LOG.debug("call getArtists({})", query);
		return getInstace().getArtists(query);
	}

	public static RecordSet<Parody> getParodies(QueryParody query) throws DataBaseException
	{
		LOG.debug("call getParodies({})", query);
		return getInstace().getParodies(query);
	}

	public static RecordSet<Content> getContents(QueryContent query) throws DataBaseException
	{
		LOG.debug("call getContents({})", query);
		return getInstace().getContents(query);
	}

	public static RecordSet<Convention> getConventions(QueryConvention query) throws DataBaseException
	{
		LOG.debug("call getConventions({})", query);
		return getInstace().getConventions(query);
	}
	
	public static RecordSet<Record> getRecycled() throws DataBaseException
	{
		LOG.debug("call getRecycled()");
		return getInstace().getRecycled();
	}

	public static RecordSet<Record> getDeleted() throws DataBaseException
	{
		LOG.debug("call getDeleted()");
		return getInstace().getDeleted();
	}

	public static RecordSet<Record> getModified() throws DataBaseException
	{
		LOG.debug("call getModified()");
		return getInstace().getModified();
	}

	public static RecordSet<Record> getUncommitted() throws DataBaseException
	{
		LOG.debug("call getUncommitted()");
		return getInstace().getUncommitted();
	}
	
	public static <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException
	{
		LOG.debug("call doInsert({})", clazz);
		return getInstace().doInsert(clazz);
	}
	
	public static void connect() throws DataBaseException
	{
		LOG.debug("call connect()");
		getInstace().connect();
	}
	
	public static void disconnect() throws DataBaseException
	{
		LOG.debug("call disconnect()");
		getInstace().disconnect();
	}
	
	public static boolean isConnected() throws DataBaseException
	{
		LOG.debug("call isConnected()");
		return getInstace().isConnected();
	}
	
	public static String getConnection() throws DataBaseException
	{

		LOG.debug("call getConnection()");
		return getInstace().getConnection();
	}

	public static boolean isAutocommit() throws DataBaseException
	{
		LOG.debug("call isAutocommit()");
		return getInstace().isAutocommit();
	}
	
	public static void addDataBaseListener(DataBaseListener dbl)
	{
		LOG.debug("call addDataBaseListener({})", dbl);
		synchronized(listeners) {
			if(!listeners.contains(dbl))
				listeners.add(dbl);
		}
	}
	
	public static void removeDataBaseListener(DataBaseListener dbl)
	{
		LOG.debug("call removeDataBaseListener({})", dbl);
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
		LOG.debug("call newContentAlias()");
		return getInstace().newContentAlias();
	}
	
	static ConventionAlias newConventionAlias()
	{
		LOG.debug("call newConventionAlias()");
		return getInstace().newConventionAlias();
	}
	
	static void deleteObject(Object o) throws DataBaseException
	{
		LOG.debug("call deleteObject({})", o);
		getInstace().deleteObject(o);
	}
}
