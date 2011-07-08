package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.dyndns.doujindb.db.DouzRecord;
import org.dyndns.doujindb.db.containers.HasArtist;
import org.dyndns.doujindb.db.containers.HasCircle;
import org.dyndns.doujindb.db.containers.HasContent;
import org.dyndns.doujindb.db.containers.HasConvention;
import org.dyndns.doujindb.db.containers.HasParody;


public interface Book extends DouzRecord, Serializable, HasArtist, HasCircle, HasContent, HasConvention, HasParody
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
