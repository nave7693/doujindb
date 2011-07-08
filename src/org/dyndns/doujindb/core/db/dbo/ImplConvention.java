package org.dyndns.doujindb.core.db.dbo;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Convention")
final class ImplConvention extends ImplRecord implements DouzRecord, Convention, Serializable//, Comparable<Convention>
{
	private static final long serialVersionUID = 0xFEED0001L;

	@XmlElement(required=true)
	private String tagName = "";
	@XmlElement(required=false)
	private String info = "";
	@XmlElement(required=false)
	private String weblink = "";
	@XmlElement(name="book", required=false)
	private Set<Book> books = new HashSet<Book>();
	
	public ImplConvention() { super(); }

	@Override
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
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
	public String toString() {
		return tagName;
	}
	
	/*@Override
	public int compareTo(Convention c) {
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
	public boolean equals(Object o) {
		if( o instanceof String)
			return o.equals(this.tagName);
		else
			if(o instanceof Convention)
				return compareTo((Convention)o) == 0;
			else
				return false;
	}
	
	@Override
	public String getID() { return (ID == -1L ? null : String.format("CN%016x", ID)); }
}
