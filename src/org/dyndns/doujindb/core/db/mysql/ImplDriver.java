package org.dyndns.doujindb.core.db.mysql;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

public class ImplDriver implements Driver
{
	private final String TABLE_PREFIX = "DOUZ_";
	
	private final String TABLE_INSTALL = "INSTALL";
	
	private final String TABLE_DOUJIN = "DOUJIN";
	private final String TABLE_TYPE = "TYPE";
	private final String TABLE_CIRCLE = "CIRCLE";
	private final String TABLE_ARTIST = "ARTIST";
	private final String TABLE_CONVENTION = "CONVENTION";
	private final String TABLE_CONTENT = "CONTENT";
	private final String TABLE_PARODY = "PARODY";
	
	private final String TABLE_PARODYOF = "PARODYOF";
	private final String TABLE_CONTAINS = "CONTAINS";
	private final String TABLE_DRAWNBY = "DRAWNBY";
	
	public ImplDriver() {
		
	}
	
	@Override
	public void commit() throws DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollback() throws DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getAutoCommit() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Table<Book> getBooks() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Circle> getCircles() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Artist> getArtists() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Parody> getParodies() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Content> getContents() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Convention> getConventions() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Record> getDeleted() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Record> getShared() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Record> getUnchecked() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Artist newArtist() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Book newBook() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Circle newCircle() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content newContent() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Convention newConvention() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parody newParody() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConnection() throws DatabaseException {
		return "mysql://admin:@localhost:3306/";
	}

}
