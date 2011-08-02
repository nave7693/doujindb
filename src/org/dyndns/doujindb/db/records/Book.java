package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ArtistContainer;
import org.dyndns.doujindb.db.containers.CircleContainer;
import org.dyndns.doujindb.db.containers.ContentContainer;
import org.dyndns.doujindb.db.containers.ConventionContainer;
import org.dyndns.doujindb.db.containers.ParodyContainer;

/**  
* Book.java - Interface Book.
* @author nozomu
* @version 1.0
*/
public interface Book extends Record, Serializable, ArtistContainer, CircleContainer, ContentContainer, ConventionContainer, ParodyContainer
{
	public enum Type implements Serializable
	{
		同人誌,
		同人ソフト,
		同人CG,
		漫画,
		アートブック,
		不詳
	}
	
	public enum Rating
	{
		UNRATED, R1, R2, R3, R4, R5
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
	public Set<Artist> getArtists();
	public Set<Circle> getCircles();
	public Set<Content> getContents();
	public Convention getConvention();
	public void setConvention(Convention convention);
	public Set<Parody> getParodies();
}
