package org.dyndns.doujindb.core.db.derby;

import java.sql.Connection;
import java.util.Date;
import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;


final class ImplBook extends ImplRecord implements Record, Book
{
	private Connection connection;
	
	ImplBook(Connection conn)
	{
		this.connection = conn;
	}
	
	ImplBook(long id, Connection conn)
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
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPages() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPages(int pages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDate(Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAdult() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDecensored() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTranslated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isColored() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAdult(boolean adult) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDecensored(boolean decensored) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTranslated(boolean translated) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColored(boolean colored) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Rating getRating() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRating(Rating rating) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInfo(String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Artist> getArtists() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Circle> getCircles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Content> getContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Convention getConvention() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConvention(Convention convention) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Parody> getParodies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

}
