package org.dyndns.doujindb.core.db.derby;

import java.sql.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;


public class ImplTable <T extends Record> implements Table<T>
{
	private String tableName;
	private Connection connection;
	
	ImplTable(String name, Connection conn)
	{
		this.tableName = name;
		this.connection = conn;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator()
	{
		Vector<T> v = new Vector<T>();
		Statement st;
		try
		{
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT ID AS ID FROM " + tableName + "");
			while(rs.next());
			{
				if(tableName.equals("DOUZ_ARTIST"))
					v.add((T) new ImplArtist(rs.getLong("ID"), connection));
				if(tableName.equals("DOUZ_BOOK"))
					v.add((T) new ImplBook(rs.getLong(1), connection));
				if(tableName.equals("DOUZ_CIRCLE"))
					v.add((T) new ImplCircle(rs.getLong("ID"), connection));
				if(tableName.equals("DOUZ_CONTENT"))
					v.add((T) new ImplContent(rs.getLong("ID"), connection));
				if(tableName.equals("DOUZ_CONVENTION"))
					v.add((T) new ImplConvention(rs.getLong("ID"), connection));
				if(tableName.equals("DOUZ_PARODY"))
					v.add((T) new ImplParody(rs.getLong("ID"), connection));
			}
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
		return v.iterator();
	}

	@Override
	public void insert(T row)
	{
		Statement st;
		try
		{
			st = connection.createStatement();
			st.executeUpdate("INSERT INTO " + tableName + " VALUES ( " + valuesOf(row) + " )");
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public void delete(T row)
	{
		Statement st;
		try
		{
			st = connection.createStatement();
			st.executeUpdate("DELETE FROM " + tableName + " WHERE ID='" + row.getID() + "'");
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public boolean contains(T row)
	{
		Statement st;
		try
		{
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) AS COUNT FROM " + tableName + " WHERE ID='" + row.getID() + "'");
			rs.next();
			return (rs.getLong("COUNT") == 1);
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public long count() throws DatabaseException
	{
		Statement st;
		try
		{
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) AS COUNT FROM " + tableName);
			rs.next();
			return rs.getLong("COUNT");
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}
	
	private String valuesOf(T record)
	{
		String values = "";
		if(record instanceof Artist)
		{
			Artist a = (Artist) record;
			values = a.getID() + " , " +
				a.getJapaneseName() + " , " +
				a.getRomanjiName() + " , " +
				a.getTranslatedName() + " , " +
				a.getWeblink();
			return values;
		}
		if(record instanceof Book)
		{
			Book b = (Book) record;
			values = b.getID() + " , " +
				b.getJapaneseName() + " , " +
				b.getRomanjiName() + " , " +
				b.getTranslatedName() + " , " +
				b.getInfo() + " , " +
				b.getConvention().getID() + " , " +
				b.isAdult() + " , " +
				b.isDecensored() + " , " +
				b.isTranslated() + " , " +
				b.isColored() + " , " +
				b.getPages() + " , " +
				b.getDate() + " , " +
				b.getType(); //TODO
			return values;
		}
		if(record instanceof Circle)
		{
			Circle c = (Circle) record;
			values = c.getID() + " , " +
				c.getJapaneseName() + " , " +
				c.getRomanjiName() + " , " +
				c.getTranslatedName() + " , " +
				c.getWeblink();
			return values;
		}
		if(record instanceof Content)
		{
			Content c = (Content) record;
			values = c.getID() + " , " +
				c.getTagName() + " , " +
				c.getInfo();
			return values;
		}
		if(record instanceof Convention)
		{
			Convention c = (Convention) record;
			values = c.getID() + " , " +
				c.getTagName() + " , " +
				c.getInfo() + " , " +
				c.getWeblink();
			return values;
		}
		if(record instanceof Parody)
		{
			Parody p = (Parody) record;
			values = p.getID() + " , " +
				p.getJapaneseName() + " , " +
				p.getRomanjiName() + " , " +
				p.getTranslatedName() + " , " +
				p.getWeblink();
			return values;
		}
		return values;
	}
}
