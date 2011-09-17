package org.dyndns.doujindb.db.impl;

import java.util.Date;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;

/**  
* RMIBook.java - RMI Interface Book.
* @author nozomu
* @version 1.0
*/
public interface RMIBook extends Remote
{
	public String getID() throws RemoteException;
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
	public RecordSet<Artist> getArtists() throws RemoteException;
	public RecordSet<Circle> getCircles() throws RemoteException;
	public RecordSet<Content> getContents() throws RemoteException;
	public Convention getConvention() throws RemoteException;
	public void setConvention(Convention convention) throws RemoteException;
	public RecordSet<Parody> getParodies() throws RemoteException;
	public void addArtist(Artist artist) throws RemoteException;
	public void addContent(Content content) throws RemoteException;
	public void addParody(Parody parody) throws RemoteException;
	public void removeArtist(Artist artist) throws RemoteException;
	public void removeContent(Content content) throws RemoteException;
	public void removeParody(Parody parody) throws RemoteException;
	public boolean isRecycled() throws RemoteException;
	public void doRestore() throws RemoteException;
	public void doRecycle() throws RemoteException;
	public int compareTo(Book o) throws RemoteException;
	public String remoteToString() throws RemoteException;
}
