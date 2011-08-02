package org.dyndns.doujindb.core.db.derby;

import java.sql.Connection;
import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;


@SuppressWarnings("serial")
final class ImplArtist extends ImplRecord implements Record, Artist
{
	private Connection connection;
	
	ImplArtist(Connection conn)
	{
		this.connection = conn;
	}
	
	ImplArtist(long id, Connection conn)
	{
		this.ID = id;
		this.connection = conn;
	}
	
	@Override
	public String getJapaneseName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTranslatedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRomanjiName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWeblink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJapaneseName(String japaneseName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTranslatedName(String translatedName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRomanjiName(String romanjiName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWeblink(String weblink) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Book> getBooks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Circle> getCircles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

}
