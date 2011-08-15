package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Book")
final class BookImpl extends RecordImpl implements Record, Book, Serializable//, Comparable<Book>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	@XmlElement(required=true)
	private String japaneseName;
	@XmlElement(required=false)
	private String translatedName = "";
	@XmlElement(required=false)
	private String romanjiName = "";
	@XmlElement(name="artist", required=false)
	private Set<Artist> artists = new HashSet<Artist>();
	@XmlElement(name="circle", required=false)
	private Set<Circle> circles = new HashSet<Circle>();
	@XmlElement(name="parody", required=false)
	private Set<Parody> parodies = new HashSet<Parody>();
	@XmlElement(required=false)
	private Convention convention;
	@XmlElement(required=false)
	private Date released;
	@XmlElement(required=false)
	private Type type;
	@XmlElement(required=false)
	private int pages = 0;
	@XmlElement(required=false)
	private boolean adult = true;
	@XmlElement(required=false)
	private boolean decensored = false;
	@XmlElement(required=false)
	private boolean translated = false;
	@XmlElement(required=false)
	private boolean colored = false;
	@XmlElement(required=false)
	private Rating rating = Rating.UNRATED;
	@XmlElement(name="content", required=false)
	private Set<Content> tags = new HashSet<Content>();
	@XmlElement(required=false)
	private String info = "";	
	
	public BookImpl() throws RemoteException { super(); }

	@Override
	public synchronized String getJapaneseName() {
		return japaneseName;
	}

	public synchronized void setJapaneseName(String japaneseName) {
		this.japaneseName = japaneseName;
	}

	@Override
	public synchronized String getTranslatedName() {
		return translatedName;
	}

	public synchronized void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}

	@Override
	public synchronized String getRomanjiName() {
		return romanjiName;
	}

	public synchronized void setRomanjiName(String romanjiName) {
		this.romanjiName = romanjiName;
	}

	@Override
	public synchronized Set<Artist> getArtists() {
		return artists;
	}

	@Override
	public synchronized Set<Circle> getCircles() {
		return circles;
	}

	@Override
	public synchronized Set<Parody> getParodies() {
		return parodies;
	}

	@Override
	public synchronized Date getDate() {
		return released;
	}

	public synchronized void setDate(Date released) {
		this.released = released;
	}

	@Override
	public synchronized Type getType() {
		return type;
	}

	public synchronized void setType(Type type) {
		this.type = type;
	}

	@Override
	public synchronized boolean isAdult() {
		return adult;
	}

	public synchronized void setAdult(boolean adult) {
		this.adult = adult;
	}

	@Override
	public synchronized boolean isDecensored() {
		return decensored;
	}

	public synchronized void setDecensored(boolean decensored) {
		this.decensored = decensored;
	}

	@Override
	public synchronized boolean isTranslated() {
		return translated;
	}

	public synchronized void setTranslated(boolean translated) {
		this.translated = translated;
	}
	
	@Override
	public synchronized boolean isColored() {
		return colored;
	}

	public synchronized void setColored(boolean colored) {
		this.colored = colored;
	}

	@Override
	public synchronized Rating getRating() {
		return rating;
	}

	public synchronized void setRating(Rating rating) {
		this.rating = rating;
	}
	
	@Override
	public synchronized Set<Content> getContents() {
		return tags;
	}

	@Override
	public synchronized int getPages() {
		return pages;
	}

	public synchronized void setPages(int pages) {
		this.pages = pages;
	}

	@Override
	public synchronized Convention getConvention() {
		return convention;
	}

	public synchronized void setConvention(Convention convention) {
		this.convention = convention;
	}

	@Override
	public synchronized String getInfo() {
		return info;
	}

	public synchronized void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public synchronized String toString() {
		//return this.japaneseName;
		return "("+(getConvention()==null?"不詳":getConvention())+") " +
			"("+getType()+") " +
			"["+getCircles().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／") +
			"("+getArtists().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")] " +
			""+getJapaneseName() + " " +
			"("+getParodies().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")";
	}

	/*@Override
	public synchronized int compareTo(Book b) {
		if(this.getID() == null)
			if(b.getID() == null)
				return 0;
			else
				return -1;
		if(b.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(b.getID());
	}*/
	
	@Override
	public synchronized boolean equals(Object o) {
		if( o instanceof String)
			return o.equals(this.japaneseName);
		else
			if(o instanceof Book)
				return compareTo((Book)o) == 0;
			else
				return false;
	}
	
	@Override
	public synchronized String getID() { return (ID == -1L ? null : String.format("B%016x", ID)); }
}
