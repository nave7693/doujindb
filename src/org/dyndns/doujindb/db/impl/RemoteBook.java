package org.dyndns.doujindb.db.impl;

import java.util.Date;
import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.db.rmi.RMIBook;

/**  
* RemoteBook.java - Remote Book.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteBook extends RecordImpl implements Artist, Serializable//, Comparable<Book>
{
	private RMIBook remoteBook;

	public RemoteBook(RMIBook remoteBook) throws DataBaseException {
		this.remoteBook = remoteBook;
	}

	public String getJapaneseName();
	public String getTranslatedName();
	public String getRomanjiName();
	public void setJapaneseName(String japaneseName);
	public void setTranslatedName(String translatedName);
	public void setRomanjiName(String romanjiName);
	public Date getDate();
	public Type getType();
	public int getPages();
	public void setPages(int pages);
	public void setDate(Date date);
	public void setType(Type type);
	public boolean isAdult();
	public boolean isDecensored();
	public boolean isTranslated();
	public boolean isColored();
	public void setAdult(boolean adult);
	public void setDecensored(boolean decensored);
	public void setTranslated(boolean translated);
	public void setColored(boolean colored);
	public Rating getRating();
	public String getInfo();
	public void setRating(Rating rating);
	public void setInfo(String info);
	public RecordSet<Artist> getArtists();
	public RecordSet<Circle> getCircles();
	public RecordSet<Content> getContents();
	public Convention getConvention();
	public void setConvention(Convention convention);
	public RecordSet<Parody> getParodies();
	public void addArtist(Artist artist);
	public void addContent(Content content);
	public void addParody(Parody parody) {
	}
	public void removeArtist(Artist artist) {
	}
	public void removeContent(Content content) {
	}
	public void removeParody(Parody parody) {
	}
}
