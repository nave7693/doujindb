package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.rmi.*;
import java.util.Date;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.*;

/**  
* Book.java - Interface Book.
* @author nozomu
* @version 1.0
*/
public interface Book extends Record, Remote, Serializable, ArtistContainer, CircleContainer, ContentContainer, ConventionContainer, ParodyContainer
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
	
	public String getJapaneseName() throws RemoteException;
	public String getTranslatedName() throws RemoteException;
	public String getRomanjiName() throws RemoteException;
	public void setJapaneseName(String japaneseName) throws RemoteException;
	public void setTranslatedName(String translatedName) throws RemoteException;
	public void setRomanjiName(String romanjiName) throws RemoteException;
	public Date getDate() throws RemoteException;
	public Type getType() throws RemoteException;
	public int getPages() throws RemoteException;
	public void setPages(int pages) throws RemoteException;
	public void setDate(Date date) throws RemoteException;
	public void setType(Type type) throws RemoteException;
	public boolean isAdult() throws RemoteException;
	public boolean isDecensored() throws RemoteException;
	public boolean isTranslated() throws RemoteException;
	public boolean isColored() throws RemoteException;
	public void setAdult(boolean adult) throws RemoteException;
	public void setDecensored(boolean decensored) throws RemoteException;
	public void setTranslated(boolean translated) throws RemoteException;
	public void setColored(boolean colored) throws RemoteException;
	public Rating getRating() throws RemoteException;
	public String getInfo() throws RemoteException;
	public void setRating(Rating rating) throws RemoteException;
	public void setInfo(String info) throws RemoteException;
	public Set<Artist> getArtists() throws RemoteException;
	public Set<Circle> getCircles() throws RemoteException;
	public Set<Content> getContents() throws RemoteException;
	public Convention getConvention() throws RemoteException;
	public void setConvention(Convention convention) throws RemoteException;
	public Set<Parody> getParodies() throws RemoteException;
}
