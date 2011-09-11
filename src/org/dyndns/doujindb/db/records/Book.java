package org.dyndns.doujindb.db.records;

import java.util.Date;
import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Book.java - Interface Book.
* @author nozomu
* @version 1.0
*/
public interface Book extends Record, Serializable, CntArtist, CntCircle, CntContent, CntConvention, CntParody
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
	public void addParody(Parody parody);
	public void removeArtist(Artist artist);
	public void removeContent(Content content);
	public void removeParody(Parody parody);
}
