package org.dyndns.doujindb.core.db.dbo;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Book")
final class ImplBook extends ImplRecord implements DouzRecord, Book, Serializable//, Comparable<Book>
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
	
	public ImplBook() { super(); }

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
	public Set<Artist> getArtists() {
		return artists;
	}

	@Override
	public Set<Circle> getCircles() {
		return circles;
	}

	@Override
	public Set<Parody> getParodies() {
		return parodies;
	}

	@Override
	public Date getDate() {
		return released;
	}

	public void setDate(Date released) {
		this.released = released;
	}

	@Override
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	@Override
	public boolean isDecensored() {
		return decensored;
	}

	public void setDecensored(boolean decensored) {
		this.decensored = decensored;
	}

	@Override
	public boolean isTranslated() {
		return translated;
	}

	public void setTranslated(boolean translated) {
		this.translated = translated;
	}
	
	@Override
	public boolean isColored() {
		return colored;
	}

	public void setColored(boolean colored) {
		this.colored = colored;
	}

	@Override
	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}
	
	@Override
	public Set<Content> getContents() {
		return tags;
	}

	@Override
	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	@Override
	public Convention getConvention() {
		return convention;
	}

	public void setConvention(Convention convention) {
		this.convention = convention;
	}

	@Override
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		//return this.japaneseName;
		return "("+(getConvention()==null?"不詳":getConvention())+") " +
			"("+getType()+") " +
			"["+getCircles().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／") +
			"("+getArtists().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")] " +
			""+getJapaneseName() + " " +
			"("+getParodies().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")";
	}

	/*@Override
	public int compareTo(Book b) {
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
	public boolean equals(Object o) {
		if( o instanceof String)
			return o.equals(this.japaneseName);
		else
			if(o instanceof Book)
				return compareTo((Book)o) == 0;
			else
				return false;
	}
	
	@Override
	public String getID() { return (ID == -1L ? null : String.format("B%016x", ID)); }
}
