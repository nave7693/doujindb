package org.dyndns.doujindb.core;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

public final class Database
{
	private static Driver instance = null;
	
	public final static String TYPE_DBO = "org.dyndns.doujindb.core.db.dbo.ImplDriver";
	public final static String TYPE_DERBY = "org.dyndns.doujindb.core.db.derby.ImplDriver";
	public final static String TYPE_MYSQL = "org.dyndns.doujindb.core.db.mysql.ImplDriver";

	private Database() { ; }

	public static boolean isConnected()
	{
		return instance != null;
	}
	
	public static void connect(String DBType) throws DatabaseException
	{
		if(instance != null)
			throw new DatabaseException("Database already connected.");
		try
		{
			Class<?> clazz = Class.forName(DBType);
			instance = (Driver) clazz.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new DatabaseException("Cannot connect to Database '" + DBType + "' : Class not found.");
		} catch (InstantiationException ie) {
			throw new DatabaseException("Cannot connect to Database '" + DBType + "' : Instantiation exception.");
		} catch (IllegalAccessException iae) {
			throw new DatabaseException("Cannot connect to Database '" + DBType + "' : Illegal access exception.");
		} catch (ClassCastException cce) {
			throw new DatabaseException("Cannot connect to Database '" + DBType + "' : Class cast exception.");
		}
	}
	
	public static void disconnect() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		instance = null;
	}
	
	public static void commit() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		instance.commit();
	}
	
	public static void rollback() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		instance.rollback();
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	public static Table<Artist> getArtists() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getArtists();
	}
	
	public static Table<Book> getBooks() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getBooks();
	}

	public static Table<Circle> getCircles() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getCircles();
	}

	public static Table<Parody> getParodies() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getParodies();
	}

	public static Table<Content> getContents() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getContents();
	}

	public static Table<Convention> getConventions() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getConventions();
	}

	public static Table<Record> getDeleted() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getDeleted();
	}

	public static Table<Record> getShared() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getShared();
	}

	public static Table<Record> getUnchecked() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.getUnchecked();
	}

	public static Artist newArtist() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newArtist();
	}

	public static Book newBook() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newBook();
	}

	public static Circle newCircle() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newCircle();
	}

	public static Content newContent() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newContent();
	}

	public static Convention newConvention() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newConvention();
	}

	public static Parody newParody() throws DatabaseException
	{
		if(instance == null)
			throw new DatabaseException("Database not connected.");
		return instance.newParody();
	}

	public static String getConnection()
	{
		return instance.getConnection();
	}
}
