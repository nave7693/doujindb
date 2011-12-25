package org.dyndns.doujindb.db.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import javax.sql.DataSource;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.*;
import org.apache.cayenne.conf.*;
import org.apache.cayenne.conn.PoolManager;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.cayenne.EmbeddedConfiguration;
import org.dyndns.doujindb.db.masks.*;
import org.dyndns.doujindb.db.records.*;

public final class DataBaseImpl implements DataBase
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	private ObjectContext context;
	private DataDomain domain;
	
	private String connID;
	
	private Hashtable<String, DataBase> children;
	
	public DataBaseImpl() throws DataBaseException
	{
		super();
		
//		DefaultConfiguration conf = new DefaultConfiguration();
//		conf.addClassPath("org/dyndns/doujindb/db/cayenne/");
//		Configuration.initializeSharedConfiguration(conf);
		
		Configuration.initializeSharedConfiguration(EmbeddedConfiguration.class);
		Configuration conf = Configuration.getSharedConfiguration();
		
		domain = conf.getDomain("doujindb");
		DataNode node = new DataNode("default");
		node.setDataSourceFactory("org.apache.cayenne.conf.DriverDataSourceFactory");
		node.setSchemaUpdateStrategy(new org.apache.cayenne.access.dbsync.ThrowOnPartialOrCreateSchemaStrategy());
		for(DataMap map : domain.getDataMaps())
		    node.addDataMap(map);
		
		try
		{
			String driver = Core.Properties.get("org.dyndns.doujindb.db.driver").asString();
			String url = Core.Properties.get("org.dyndns.doujindb.db.url").asString();
			String username =Core.Properties.get("org.dyndns.doujindb.db.username").asString();
			String password = Core.Properties.get("org.dyndns.doujindb.db.password").asString();
			PoolManager pool = new PoolManager(driver,
					url,
			        1,
			        1,
			        username,
			        password);
			node.setDataSource(pool);
			//Doesn't work, handle timeout manually
			//pool.setLoginTimeout(3);
			checkContext(pool, 3);
			connID = url;
		} catch (SQLException sqle) {
			throw new DataBaseException(sqle);
		}
		
		
		//JdbcAdapter adapter = new org.apache.cayenne.dba.mysql.MySQLAdapter();
		//adapter.setSupportsGeneratedKeys(true);
		//node.setAdapter(adapter);
		node.setAdapter(new org.apache.cayenne.dba.AutoAdapter(node.getDataSource()));
		
		domain.addNode(node);
		
		context = domain.createDataContext();
		
		children = new Hashtable<String, DataBase>();
	}
	
	private synchronized void checkContext(DataSource ds, int timeout) throws DataBaseException
	{
		final DataSource _ds = ds;
		final int _timeout = timeout;
		ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Connection> task = new Callable<Connection>()
		{
		   public Connection call()
		   {
		      try {
				return _ds.getConnection();
			} catch (SQLException sqle) {
				return null;
			}
		   }
		};
		Future<Connection> future = executor.submit(task);
		try
		{
			Connection conn = future.get(_timeout, TimeUnit.SECONDS);
			if(conn == null)
				throw new DataBaseException("Cannot initialize connection.");
			conn.close();
		} catch (TimeoutException te) {
			throw new DataBaseException("TimeoutException : Cannot initialize connection.");
		} catch (InterruptedException ie) {
			throw new DataBaseException("InterruptedException : Cannot initialize connection.");
		} catch (ExecutionException ee) {
			throw new DataBaseException("ExecutionException : Cannot initialize connection.");
		} catch (SQLException sqle) {
			throw new DataBaseException("SQLException : Cannot initialize connection.");
		} finally {
		   future.cancel(true);
		}
	}
	
	public synchronized DataBase childContext(String ID) throws DataBaseException
	{
		if(!children.containsKey(ID))
		{
			DataBase db = new DataBaseImpl();
			children.put(ID, db);
			return db;
		}else
		{
			return children.get(ID);
		}
	}

	private synchronized Artist newArtist() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Artist o = context.newObject(org.dyndns.doujindb.db.cayenne.Artist.class);
		return new ArtistImpl(o);
	}

	private synchronized Book newBook() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Book o = context.newObject(org.dyndns.doujindb.db.cayenne.Book.class);
		return new BookImpl(o);
	}

	private synchronized Circle newCircle() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Circle o = context.newObject(org.dyndns.doujindb.db.cayenne.Circle.class);
		return new CircleImpl(o);
	}

	private synchronized Content newContent() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Content o = context.newObject(org.dyndns.doujindb.db.cayenne.Content.class);
		return new ContentImpl(o);
	}

	private synchronized Convention newConvention() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Convention o = context.newObject(org.dyndns.doujindb.db.cayenne.Convention.class);
		return new ConventionImpl(o);
	}

	private synchronized Parody newParody() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Parody o = context.newObject(org.dyndns.doujindb.db.cayenne.Parody.class);
		return new ParodyImpl(o);
	}
	
	

	@Override
	public synchronized void doCommit() throws DataBaseException
	{
		context.commitChanges();
	}

	@Override
	public synchronized void doRollback() throws DataBaseException
	{
		context.rollbackChanges();
	}

	@Override
	public synchronized String getConnection() throws DataBaseException
	{
		return connID;
	}
	
	@Override
	public synchronized void doDelete(Record record) throws DataBaseException
	{
		context.deleteObject(((RecordImpl)record).ref);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Book> getBooks(MskBook mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Book.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Book> list = context.performQuery(select);
		Set<Book> buff = new TreeSet<Book>();
		for(org.dyndns.doujindb.db.cayenne.Book o : list)
			buff.add(new BookImpl(o));
		return new RecordSetImpl<Book>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Circle> getCircles(MskCircle mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Circle> list = context.performQuery(select);
		Set<Circle> buff = new TreeSet<Circle>();
		for(org.dyndns.doujindb.db.cayenne.Circle o : list)
			buff.add(new CircleImpl(o));
		return new RecordSetImpl<Circle>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Artist> getArtists(MskArtist mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Artist> list = context.performQuery(select);
		Set<Artist> buff = new TreeSet<Artist>();
		for(org.dyndns.doujindb.db.cayenne.Artist o : list)
			buff.add(new ArtistImpl(o));
		return new RecordSetImpl<Artist>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Parody> getParodies(MskParody mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Parody.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Parody> list = context.performQuery(select);
		Set<Parody> buff = new TreeSet<Parody>();
		for(org.dyndns.doujindb.db.cayenne.Parody o : list)
			buff.add(new ParodyImpl(o));
		return new RecordSetImpl<Parody>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Content> getContents(MskContent mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Content.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Content> list = context.performQuery(select);
		Set<Content> buff = new TreeSet<Content>();
		for(org.dyndns.doujindb.db.cayenne.Content o : list)
			buff.add(new ContentImpl(o));
		return new RecordSetImpl<Content>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Convention> getConventions(MskConvention mask) throws DataBaseException
	{
		SelectQuery select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Convention.class, Expression.fromString("recycled = FALSE"));
		List<org.dyndns.doujindb.db.cayenne.Convention> list = context.performQuery(select);
		Set<Convention> buff = new TreeSet<Convention>();
		for(org.dyndns.doujindb.db.cayenne.Convention o : list)
			buff.add(new ConventionImpl(o));
		return new RecordSetImpl<Convention>(buff);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Record> getRecycled() throws DataBaseException
	{
		Set<Record> buff = new TreeSet<Record>();
		SelectQuery select;
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Artist> artist_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Artist o : artist_list)
			buff.add(new ArtistImpl(o));
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Book.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Book> book_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Book o : book_list)
			buff.add(new BookImpl(o));
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Circle> circle_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Circle o : circle_list)
			buff.add(new CircleImpl(o));
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Content.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Content> content_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Content o : content_list)
			buff.add(new ContentImpl(o));
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Convention.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Convention> conventiony_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Convention o : conventiony_list)
			buff.add(new ConventionImpl(o));
		select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Parody.class, Expression.fromString("recycled = TRUE"));
		List<org.dyndns.doujindb.db.cayenne.Parody> parody_list = context.performQuery(select);
		for(org.dyndns.doujindb.db.cayenne.Parody o : parody_list)
			buff.add(new ParodyImpl(o));
		return new RecordSetImpl<Record>(buff);
	}

	@Override
	public RecordSet<Record> getDeleted() throws DataBaseException
	{
		Collection<?> deleted0 = context.deletedObjects();
		Set<Record> deleted1 = new TreeSet<Record>();
		for(Object o : deleted0)
		{
			if(o instanceof org.dyndns.doujindb.db.cayenne.Artist)
				deleted1.add(new ArtistImpl((org.dyndns.doujindb.db.cayenne.Artist)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Circle)
				deleted1.add(new CircleImpl((org.dyndns.doujindb.db.cayenne.Circle)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Book)
				deleted1.add(new BookImpl((org.dyndns.doujindb.db.cayenne.Book)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Content)
				deleted1.add(new ContentImpl((org.dyndns.doujindb.db.cayenne.Content)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Convention)
				deleted1.add(new ConventionImpl((org.dyndns.doujindb.db.cayenne.Convention)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Parody)
				deleted1.add(new ParodyImpl((org.dyndns.doujindb.db.cayenne.Parody)o));
		}
		return new RecordSetImpl<Record>(deleted1);
	}

	@Override
	public RecordSet<Record> getModified() throws DataBaseException
	{
		Collection<?> modified0 = context.modifiedObjects();
		Set<Record> modified1 = new TreeSet<Record>();
		for(Object o : modified0)
		{
			if(o instanceof org.dyndns.doujindb.db.cayenne.Artist)
				modified1.add(new ArtistImpl((org.dyndns.doujindb.db.cayenne.Artist)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Circle)
				modified1.add(new CircleImpl((org.dyndns.doujindb.db.cayenne.Circle)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Book)
				modified1.add(new BookImpl((org.dyndns.doujindb.db.cayenne.Book)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Content)
				modified1.add(new ContentImpl((org.dyndns.doujindb.db.cayenne.Content)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Convention)
				modified1.add(new ConventionImpl((org.dyndns.doujindb.db.cayenne.Convention)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Parody)
				modified1.add(new ParodyImpl((org.dyndns.doujindb.db.cayenne.Parody)o));
		}
		return new RecordSetImpl<Record>(modified1);
	}

	@Override
	public RecordSet<Record> getUncommitted() throws DataBaseException
	{
		Collection<?> uncommitted0 = context.uncommittedObjects();
		Set<Record> uncommitted1 = new TreeSet<Record>();
		for(Object o : uncommitted0)
		{
			if(o instanceof org.dyndns.doujindb.db.cayenne.Artist)
				uncommitted1.add(new ArtistImpl((org.dyndns.doujindb.db.cayenne.Artist)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Circle)
				uncommitted1.add(new CircleImpl((org.dyndns.doujindb.db.cayenne.Circle)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Book)
				uncommitted1.add(new BookImpl((org.dyndns.doujindb.db.cayenne.Book)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Content)
				uncommitted1.add(new ContentImpl((org.dyndns.doujindb.db.cayenne.Content)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Convention)
				uncommitted1.add(new ConventionImpl((org.dyndns.doujindb.db.cayenne.Convention)o));
			if(o instanceof org.dyndns.doujindb.db.cayenne.Parody)
				uncommitted1.add(new ParodyImpl((org.dyndns.doujindb.db.cayenne.Parody)o));
		}
		return new RecordSetImpl<Record>(uncommitted1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException
	{
		if(clazz == Artist.class)
			return (T) newArtist();
		if(clazz == Book.class)
			return (T) newBook();
		if(clazz == Circle.class)
			return (T) newCircle();
		if(clazz == Content.class)
			return (T) newContent();
		if(clazz == Convention.class)
			return (T) newConvention();
		if(clazz == Parody.class)
			return (T) newParody();
		throw new DataBaseException("Invalid record class '" + clazz + "' specified.");
	}
}