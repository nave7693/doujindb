package org.dyndns.doujindb.db;

import java.lang.reflect.*;
import java.sql.*;

import javax.sql.*;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.DbGenerator;
// import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conn.PoolManager;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.exp.*;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.db.cayenne.ContentAlias;
import org.dyndns.doujindb.db.cayenne.ConventionAlias;
import org.dyndns.doujindb.db.cayenne.EmbeddedConfiguration;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;

/**  
* DataBaseImpl.java - DoujinDB database instance implementation.
* @author  nozomu
* @version 1.0
*/
final class DataBaseImpl extends IDataBase
{
	private DataDomain domain;
	private DataNode node;
	protected ObjectContext context;
	private String connection;
	private boolean autocommit = true;
	
	private static SelectQuery queryArtistAnd;
	private static SelectQuery queryBookAnd;
	private static SelectQuery queryCircleAnd;
	private static SelectQuery queryConventionAnd;
	private static SelectQuery queryContentAnd;
	private static SelectQuery queryParodyAnd;
	
	private static SelectQuery queryArtistOr;
	private static SelectQuery queryBookOr;
	private static SelectQuery queryCircleOr;
	private static SelectQuery queryConventionOr;
	private static SelectQuery queryContentOr;
	private static SelectQuery queryParodyOr;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataBaseImpl.class);
	
	{
		List<Expression> list;
		
		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		list.add(ExpressionFactory.likeExp("japaneseName", 
		         new ExpressionParameter("JapaneseName")));
		list.add(ExpressionFactory.likeExp("romajiName", 
		         new ExpressionParameter("RomajiName")));
		list.add(ExpressionFactory.likeExp("translatedName", 
		         new ExpressionParameter("TranslatedName")));
		list.add(ExpressionFactory.likeExp("weblink", 
		         new ExpressionParameter("Weblink")));
		queryArtistAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryArtistOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));

		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		list.add(ExpressionFactory.likeExp("japaneseName", 
		         new ExpressionParameter("JapaneseName")));
		list.add(ExpressionFactory.likeExp("romajiName", 
		         new ExpressionParameter("RomajiName")));
		list.add(ExpressionFactory.likeExp("translatedName", 
		         new ExpressionParameter("TranslatedName")));
		list.add(ExpressionFactory.matchExp("type", 
		         new ExpressionParameter("Type")));
		list.add(ExpressionFactory.matchExp("adult", 
		         new ExpressionParameter("Adult")));
		list.add(ExpressionFactory.matchExp("color", 
		         new ExpressionParameter("Colored")));
		list.add(ExpressionFactory.matchExp("translated", 
		         new ExpressionParameter("Translated")));
		list.add(ExpressionFactory.matchExp("decensored", 
		         new ExpressionParameter("Decensored")));
		queryBookAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Book.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryBookOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Book.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		
		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		list.add(ExpressionFactory.likeExp("japaneseName", 
		         new ExpressionParameter("JapaneseName")));
		list.add(ExpressionFactory.likeExp("romajiName", 
		         new ExpressionParameter("RomajiName")));
		list.add(ExpressionFactory.likeExp("translatedName", 
		         new ExpressionParameter("TranslatedName")));
		list.add(ExpressionFactory.likeExp("weblink", 
		         new ExpressionParameter("Weblink")));
		queryCircleAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryCircleOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		
		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		List<Expression> conventionT = new ArrayList<Expression>();
		conventionT.add(ExpressionFactory.likeExp("tagName", new ExpressionParameter("TagName")));
		conventionT.add(ExpressionFactory.likeExp("aliases+.tagName", new ExpressionParameter("TagName")));
		list.add(ExpressionFactory.joinExp(Expression.OR, conventionT));
		list.add(ExpressionFactory.likeExp("weblink", 
		         new ExpressionParameter("Weblink")));
		queryConventionAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Convention.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryConventionOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Convention.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		
		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		List<Expression> contentT = new ArrayList<Expression>();
		contentT.add(ExpressionFactory.likeExp("tagName", new ExpressionParameter("TagName")));
		contentT.add(ExpressionFactory.likeExp("aliases+.tagName", new ExpressionParameter("TagName")));
		list.add(ExpressionFactory.joinExp(Expression.OR, contentT));
		queryContentAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Content.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryContentOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Content.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		
		list = new ArrayList<Expression>();
		list.add(ExpressionFactory.matchDbExp("ID", 
		         new ExpressionParameter("Id")));
		list.add(ExpressionFactory.likeExp("japaneseName", 
		         new ExpressionParameter("JapaneseName")));
		list.add(ExpressionFactory.likeExp("romajiName", 
		         new ExpressionParameter("RomajiName")));
		list.add(ExpressionFactory.likeExp("translatedName", 
		         new ExpressionParameter("TranslatedName")));
		list.add(ExpressionFactory.likeExp("weblink", 
		         new ExpressionParameter("Weblink")));
		queryParodyAnd = new SelectQuery(org.dyndns.doujindb.db.cayenne.Parody.class, ExpressionFactory.joinExp(Expression.AND, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
		queryParodyOr = new SelectQuery(org.dyndns.doujindb.db.cayenne.Parody.class, ExpressionFactory.joinExp(Expression.OR, list).joinExp(Expression.AND, Expression.fromString("recycled = FALSE")));
	}
	
	public DataBaseImpl()
	{
		org.apache.cayenne.conf.Configuration.initializeSharedConfiguration(EmbeddedConfiguration.class);
		org.apache.cayenne.conf.Configuration conf = org.apache.cayenne.conf.Configuration.getSharedConfiguration();
		
		domain = conf.getDomain("doujindb");
		node = new DataNode("default");
		node.setDataSourceFactory("org.apache.cayenne.conf.DriverDataSourceFactory");
		node.setSchemaUpdateStrategy(new org.apache.cayenne.access.dbsync.ThrowOnPartialOrCreateSchemaStrategy());
		
		node.addDataMap(domain.getMap("map"));

		domain.addNode(node);

		context = domain.createDataContext();
	}
	
	private synchronized void checkContext(DataSource ds, int timeout) throws DataBaseException
	{
		final DataSource _ds = ds;
		final int _timeout = timeout;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Connection> task = new Callable<Connection>()
		{
			public Connection call()
			{
				try {
					return _ds.getConnection();
				} catch (SQLException sqle) {
					LOG.error("Cannot initialize connection", sqle);
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
			throw new DataBaseException("ExecutionException : Cannot initialize connection.");
		} finally {
		   future.cancel(true);
		}
	}
	
	@Override
	protected synchronized Artist newArtist() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Artist o = context.newObject(org.dyndns.doujindb.db.cayenne.Artist.class);
		return new ArtistImpl(o);
	}

	@Override
	protected synchronized Book newBook() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Book o = context.newObject(org.dyndns.doujindb.db.cayenne.Book.class);
		return new BookImpl(o);
	}

	@Override
	protected synchronized Circle newCircle() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Circle o = context.newObject(org.dyndns.doujindb.db.cayenne.Circle.class);
		return new CircleImpl(o);
	}

	@Override
	protected synchronized Content newContent() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Content o = context.newObject(org.dyndns.doujindb.db.cayenne.Content.class);
		return new ContentImpl(o);
	}
	
	@Override
	protected synchronized ContentAlias newContentAlias() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.ContentAlias o = context.newObject(org.dyndns.doujindb.db.cayenne.ContentAlias.class);
		return o;
	}

	@Override
	protected synchronized Convention newConvention() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Convention o = context.newObject(org.dyndns.doujindb.db.cayenne.Convention.class);
		return new ConventionImpl(o);
	}
	
	@Override
	protected synchronized ConventionAlias newConventionAlias() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.ConventionAlias o = context.newObject(org.dyndns.doujindb.db.cayenne.ConventionAlias.class);
		return o;
	}

	@Override
	protected synchronized Parody newParody() throws DataBaseException
	{
		org.dyndns.doujindb.db.cayenne.Parody o = context.newObject(org.dyndns.doujindb.db.cayenne.Parody.class);
		return new ParodyImpl(o);
	}
	
	@Override
	protected synchronized void deleteObject(Object o) throws DataBaseException
	{
		context.deleteObject(o);
	}
	
	@Override
	public synchronized void doCommit() throws DataBaseException
	{
		context.commitChanges();
		
		DataBase.fireDatabaseCommit();
	}

	@Override
	public synchronized void doRollback() throws DataBaseException
	{
		context.rollbackChanges();
		
		DataBase.fireDatabaseRollback();
	}
	
	@Override
	public synchronized void doDelete(Record record) throws DataBaseException
	{
		context.deleteObject(((RecordImpl)record).ref);
		
		DataBase.fireRecordDeleted(record);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Book> getBooks(QueryBook query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryBook)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryBookAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryBookOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Book.class, Expression.fromString("recycled = FALSE"));
		}

		if(!query.Contents.isEmpty())
		{
	        EJBQLQuery ejbqlq = new EJBQLQuery("SELECT b.id FROM Book b"
	        		+ " JOIN b.contents t"
	        		+ " WHERE (t.tagName IN (:TAGS))"
	        		+ " GROUP BY b.id"
	        		+ " HAVING COUNT(b.id) = :TAG_COUNT");
	        Set<String> tags = new HashSet<String>();
	        for(Content t : query.Contents)
	        	tags.add(t.getTagName());
	        ejbqlq.setParameter("TAGS", tags);
	        ejbqlq.setParameter("TAG_COUNT", tags.size());
	        select.setQualifier(select.getQualifier().andExp(ExpressionFactory.inDbExp("ID", context.performQuery(ejbqlq))));
		}

		List<org.dyndns.doujindb.db.cayenne.Book> list = context.performQuery(select);
		Set<Book> buff = new TreeSet<Book>();
		for(org.dyndns.doujindb.db.cayenne.Book o : list)
			buff.add(new BookImpl(o));
		return new RecordSetImpl<Book>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Circle> getCircles(QueryCircle query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryCircle)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryCircleAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryCircleOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class, Expression.fromString("recycled = FALSE"));
		}
		List<org.dyndns.doujindb.db.cayenne.Circle> list = context.performQuery(select);
		Set<Circle> buff = new TreeSet<Circle>();
		for(org.dyndns.doujindb.db.cayenne.Circle o : list)
			buff.add(new CircleImpl(o));
		return new RecordSetImpl<Circle>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Artist> getArtists(QueryArtist query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryArtist)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryArtistAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryArtistOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class, Expression.fromString("recycled = FALSE"));
		}
		List<org.dyndns.doujindb.db.cayenne.Artist> list = context.performQuery(select);
		Set<Artist> buff = new TreeSet<Artist>();
		for(org.dyndns.doujindb.db.cayenne.Artist o : list)
			buff.add(new ArtistImpl(o));
		return new RecordSetImpl<Artist>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Parody> getParodies(QueryParody query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryParody)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryParodyAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryParodyOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Parody.class, Expression.fromString("recycled = FALSE"));
		}
		List<org.dyndns.doujindb.db.cayenne.Parody> list = context.performQuery(select);
		Set<Parody> buff = new TreeSet<Parody>();
		for(org.dyndns.doujindb.db.cayenne.Parody o : list)
			buff.add(new ParodyImpl(o));
		return new RecordSetImpl<Parody>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Content> getContents(QueryContent query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryContent)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryContentAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryContentOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Content.class, Expression.fromString("recycled = FALSE"));
		}
		List<org.dyndns.doujindb.db.cayenne.Content> list = context.performQuery(select);
		Set<Content> buff = new TreeSet<Content>();
		for(org.dyndns.doujindb.db.cayenne.Content o : list)
			buff.add(new ContentImpl(o));
		return new RecordSetImpl<Content>(buff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RecordSet<Convention> getConventions(QueryConvention query) throws DataBaseException
	{
		SelectQuery select;
		if(query instanceof QueryConvention)
		{
			switch(query.QueryType)
			{
				case AND:
					select = queryConventionAnd.queryWithParameters(parseObject(query));
					break;
				case OR:
					select = queryConventionOr.queryWithParameters(parseObject(query));
					break;
				default:
					throw new DataBaseException("Unknown QueryType '" + query.QueryType + "'");
			}
		} else {
			select = new SelectQuery(org.dyndns.doujindb.db.cayenne.Convention.class, Expression.fromString("recycled = FALSE"));
		}
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
		{
			T record = (T) newArtist();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		if(clazz == Book.class)
		{
			T record = (T) newBook();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		if(clazz == Circle.class)
		{
			T record = (T) newCircle();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		if(clazz == Content.class)
		{
			T record = (T) newContent();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		if(clazz == Convention.class)
		{
			T record = (T) newConvention();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		if(clazz == Parody.class)
		{
			T record = (T) newParody();
			DataBase.fireRecordAdded((Record)record);
			return record;
		}
		throw new DataBaseException("Invalid record class '" + clazz + "' specified.");
	}

	@Override
	public String getConnection() throws DataBaseException
	{
		return connection;
	}
	
	@Override
	public void connect() throws DataBaseException
	{
		if(isConnected())
			throw new DataBaseException("DataBase already connected.");
		try
		{
			String driver =   (String) Configuration.configRead("org.dyndns.doujindb.db.driver");
			String url =      (String) Configuration.configRead("org.dyndns.doujindb.db.url");
			String username = (String) Configuration.configRead("org.dyndns.doujindb.db.username");
			String password = (String) Configuration.configRead("org.dyndns.doujindb.db.password");
			PoolManager pool = new PoolManager(driver,
					url,
			        1,
			        1,
			        username,
			        password);
			// Doesn't work, handle timeout manually
			// pool.setLoginTimeout(3);
			checkContext(pool, (Integer) Configuration.configRead("org.dyndns.doujindb.db.connection_timeout"));
			
			// Pool is valid
			node.setDataSource(pool);
			
			DbAdapter adpt = new org.apache.cayenne.dba.AutoAdapter(pool);
			
			/**
			 * Check whether to create the 'auto_pk_support' table.
			 * @see https://issues.apache.org/jira/browse/CAY-1040
			 */
			boolean auto_pk_support = true;
			Connection conn = DriverManager.getConnection(url, username, password);
			DatabaseMetaData dmd = conn.getMetaData();
			/**
			 * Can't use conn.getSchema() here, MySQL jdbc driver throws AbstractMethodError
			 */
			ResultSet schemas = dmd.getCatalogs();
			while(schemas.next())
			{
				ResultSet tables = dmd.getTables(null, schemas.getString(1), null, null);
				while(tables.next())
				{
					String table = tables.getString("TABLE_NAME");
					if(table.equalsIgnoreCase("auto_pk_support"))
					{
						auto_pk_support = false;
						break;
					}
				}
				if(!auto_pk_support)
					break;
			}
			if(auto_pk_support)
			{
				ResultSet tables = dmd.getTables(null, null, "AUTO_PK_SUPPORT", null);
				while(tables.next())
				{
					auto_pk_support = false;
					break;
				}
			}
			conn.close();
			if(auto_pk_support)
			{
				DbGenerator generator = new DbGenerator(adpt, domain.getMap("map"));
				generator.setShouldCreatePKSupport(true);
				generator.setShouldDropPKSupport(false);
				generator.setShouldCreateTables(false);
				generator.setShouldDropTables(false);
				generator.setShouldCreateFKConstraints(false);
				generator.runGenerator(pool);
			}
				
			node.setAdapter(adpt);
			
			connection = url;
		} catch (SQLException sqle) {
			throw new DataBaseException(sqle);
		} catch (Exception e) {
			throw new DataBaseException(e);
		}
		
		DataBase.fireDatabaseConnected();
	}
	
	@Override
	public void disconnect() throws DataBaseException
	{
		node.setDataSource(null);
		
		DataBase.fireDatabaseDisconnected();
	}
	
	@Override
	public boolean isConnected() throws DataBaseException
	{
		return node.getDataSource() != null;
	}
	
	@Override
	public boolean isAutocommit() throws DataBaseException
	{
		return autocommit;
	}

	private Map<String, Object> parseObject(Object o)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		for(Field field : o.getClass().getFields())
			try {
				Object value = field.get(o);
				if(value != null)
					map.put(field.getName(), value);
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
		return map;
	}
}
