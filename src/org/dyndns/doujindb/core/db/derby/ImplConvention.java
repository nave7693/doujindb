package org.dyndns.doujindb.core.db.derby;

import java.sql.Connection;
import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;


final class ImplConvention extends ImplRecord implements Record, Convention
{
	private Connection connection;
	
	ImplConvention(Connection conn)
	{
		this.connection = conn;
	}
	
	ImplConvention(long id, Connection conn)
	{
		this.ID = id;
		this.connection = conn;
	}
	
	@Override
	public String getTagName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWeblink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTagName(String tagName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInfo(String info) {
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
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

}
