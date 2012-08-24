package org.dyndns.doujindb.db.records;

import java.util.Date;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Book.java - Interface Book.
* @author nozomu
* @version 1.0
*/
public interface Book extends Record, CntArtist, CntCircle, CntContent, CntConvention, CntParody
{
	public enum Type implements org.apache.cayenne.ExtendedEnumeration
	{
		不詳(0), 同人誌(1), 同人ソフト(2), 同人CG(3), 漫画(4), アートブック(5);

		private Integer value;

		private Type(Integer value) { this.value = value; }
		
		public Integer getDatabaseValue() { return value; }
	}
	
	public enum Rating implements org.apache.cayenne.ExtendedEnumeration
	{
		UNRATED(0), R1(1), R2(2), R3(3), R4(4), R5(5);
		
		private Integer value;

		private Rating(Integer value) { this.value = value; }
		
		public Integer getDatabaseValue() { return value; }
	}
	
	public String getJapaneseName() throws DataBaseException;
	public String getTranslatedName() throws DataBaseException;
	public String getRomajiName() throws DataBaseException;
	public void setJapaneseName(String japaneseName) throws DataBaseException;
	public void setTranslatedName(String translatedName) throws DataBaseException;
	public void setRomajiName(String romajiName) throws DataBaseException;
	public Date getDate() throws DataBaseException;
	public Type getType() throws DataBaseException;
	public int getPages() throws DataBaseException;
	public void setPages(int pages) throws DataBaseException;
	public void setDate(Date date) throws DataBaseException;
	public void setType(Type type) throws DataBaseException;
	public boolean isAdult() throws DataBaseException;
	public boolean isDecensored() throws DataBaseException;
	public boolean isTranslated() throws DataBaseException;
	public boolean isColored() throws DataBaseException;
	public void setAdult(boolean adult) throws DataBaseException;
	public void setDecensored(boolean decensored) throws DataBaseException;
	public void setTranslated(boolean translated) throws DataBaseException;
	public void setColored(boolean colored) throws DataBaseException;
	public Rating getRating() throws DataBaseException;
	public String getInfo() throws DataBaseException;
	public void setRating(Rating rating) throws DataBaseException;
	public void setInfo(String info) throws DataBaseException;
	public RecordSet<Artist> getArtists() throws DataBaseException;
	public RecordSet<Circle> getCircles() throws DataBaseException;
	public RecordSet<Content> getContents() throws DataBaseException;
	public Convention getConvention() throws DataBaseException;
	public void setConvention(Convention convention) throws DataBaseException;
	public RecordSet<Parody> getParodies() throws DataBaseException;
	public void addArtist(Artist artist) throws DataBaseException;
	public void addContent(Content content) throws DataBaseException;
	public void addParody(Parody parody) throws DataBaseException;
	public void removeArtist(Artist artist) throws DataBaseException;
	public void removeContent(Content content) throws DataBaseException;
	public void removeParody(Parody parody) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
