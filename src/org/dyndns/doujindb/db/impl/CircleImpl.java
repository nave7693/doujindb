package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Circle")
final class CircleImpl extends RecordImpl implements Record, Circle, Serializable//, Comparable<Circle>
{
	private static final long serialVersionUID = 0xFEED0001L;

	@XmlElement(required=true)
	private String japaneseName;
	@XmlElement(required=false)
	private String translatedName = "";
	@XmlElement(required=false)
	private String romanjiName = "";
	@XmlElement(required=false)
	private String weblink = "";
	@XmlElement(name="artist", required=false)
	private Set<Artist> artists = new HashSet<Artist>();
	@XmlElement(name="book", required=false)
	private Set<Book> books = new HashSet<Book>();
	
	public CircleImpl() throws RemoteException { super(); }

	@Override
	public synchronized String getJapaneseName()
	{
		return japaneseName;
	}

	public synchronized void setJapaneseName(String japaneseName)
	{
		this.japaneseName = japaneseName;
	}

	@Override
	public synchronized String getTranslatedName()
	{
		return translatedName;
	}

	public synchronized void setTranslatedName(String translatedName)
	{
		this.translatedName = translatedName;
	}

	@Override
	public synchronized String getRomanjiName()
	{
		return romanjiName;
	}

	public synchronized void setRomanjiName(String romanjiName)
	{
		this.romanjiName = romanjiName;
	}

	@Override
	public synchronized String getWeblink()
	{
		return weblink;
	}

	public synchronized void setWeblink(String weblink)
	{
		this.weblink = weblink;
	}

	@Override
	public synchronized Set<Artist> getArtists()
	{
		return artists;
	}

	@Override
	public synchronized Set<Book> getBooks()
	{
		return books;
	}
	
	@Override
	public synchronized String toString() {
		return this.japaneseName;
		/*return japaneseName + 
			(romanjiName.equals("") ? "" : " ("+romanjiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
	}

	/*@Override
	public synchronized int compareTo(Circle c) {
		if(this.getID() == null)
			if(c.getID() == null)
				return 0;
			else
				return -1;
		if(c.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(c.getID());
	}*/
	
	@Override
	public synchronized boolean equals(Object o) {
		if( o instanceof String)
			return o.equals(this.japaneseName);
		else
			if(o instanceof Circle)
				return compareTo((Circle)o) == 0;
			else
				return false;
	}
	
	@Override
	public synchronized String getID() { return (ID == -1L ? null : String.format("C%016x", ID)); }
}
