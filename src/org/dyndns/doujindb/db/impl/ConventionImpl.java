package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Convention")
final class ConventionImpl extends RecordImpl implements Record, Convention, Serializable//, Comparable<Convention>
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
	
	public ConventionImpl() throws RemoteException { super(); }

	@Override
	public synchronized String getTagName() {
		return tagName;
	}

	public synchronized void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public synchronized String getInfo() {
		return info;
	}

	public synchronized void setInfo(String info) {
		this.info = info;
	}	
	
	@Override
	public synchronized String getWeblink() {
		return weblink;
	}

	public synchronized void setWeblink(String weblink) {
		this.weblink = weblink;
	}

	@Override
	public synchronized Set<Book> getBooks() {
		return books;
	}

	@Override
	public synchronized String toString() {
		return tagName;
	}
	
	/*@Override
	public synchronized int compareTo(Convention c) {
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
			return o.equals(this.tagName);
		else
			if(o instanceof Convention)
				return compareTo((Convention)o) == 0;
			else
				return false;
	}
	
	@Override
	public synchronized String getID() { return (ID == -1L ? null : String.format("CN%016x", ID)); }
}
