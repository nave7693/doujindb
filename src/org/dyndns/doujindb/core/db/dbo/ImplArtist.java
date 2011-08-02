package org.dyndns.doujindb.core.db.dbo;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Artist")
final class ImplArtist extends ImplRecord implements Record, Artist, Serializable//, Comparable<Artist>
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
	@XmlElement(name="book", required=false)
	private Set<Book> books = new HashSet<Book>();
	@XmlElement(name="circle", required=false)
	private Set<Circle> circles = new HashSet<Circle>();
	
	public ImplArtist() { super(); }
	
	@Override
	public String getJapaneseName() {
		return japaneseName;
	}

	public void setJapaneseName(String japaneseName) {
		this.japaneseName = japaneseName;
	}

	@Override
	public String getTranslatedName() {
		return translatedName;
	}

	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}

	@Override
	public String getRomanjiName() {
		return romanjiName;
	}

	public void setRomanjiName(String romanjiName) {
		this.romanjiName = romanjiName;
	}

	@Override
	public String getWeblink() {
		return weblink;
	}

	public void setWeblink(String weblink) {
		this.weblink = weblink;
	}

	@Override
	public Set<Book> getBooks() {
		return books;
	}
	
	@Override
	public Set<Circle> getCircles() {
		return circles;
	}

	@Override
	public String toString() {
		return this.japaneseName;
		/*return japaneseName + 
			(romanjiName.equals("") ? "" : " ("+romanjiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
	}

	/*@Override
	public int compareTo(Artist a) {
		if(this.getID() == null)
			if(a.getID() == null)
				return 0;
			else
				return -1;
		if(a.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(a.getID());
	}*/
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof String)
			return o.equals(this.japaneseName);
		else
			if(o instanceof Artist)
				return compareTo((Artist)o) == 0;
			else
				return false;
	}
	
	@Override
	public String getID() { return (ID == -1L ? null : String.format("A%016x", ID)); }
	
}
