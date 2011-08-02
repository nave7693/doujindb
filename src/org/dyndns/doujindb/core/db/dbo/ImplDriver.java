package org.dyndns.doujindb.core.db.dbo;

import java.io.*;
import java.util.Hashtable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Driver")
public final class ImplDriver implements Driver
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	private boolean autoCommit = false;
	
	@XmlElement(name="book", required=false)
	private Table<Book> books;
	@XmlElement(name="circle", required=false)
	private Table<Circle> circles;
	@XmlElement(name="artist", required=false)
	private Table<Artist> artists;
	@XmlElement(name="parody", required=false)
	private Table<Parody> parodies;
	@XmlElement(name="content", required=false)
	private Table<Content> contents;
	@XmlElement(name="convention", required=false)
	private Table<Convention> conventions;
	private Table<Record> deleted;
	private Table<Record> shared;
	private Table<Record> unchecked;
	
	public Table<Record> getDeleted() {
		return deleted;
	}

	public Table<Record> getShared() {
		return shared;
	}

	public Table<Record> getUnchecked() {
		return unchecked;
	}

	public ImplDriver()
	{
		books = new ImplTable<Book>();
		circles = new ImplTable<Circle>();
		artists = new ImplTable<Artist>();
		parodies = new ImplTable<Parody>();
		contents = new ImplTable<Content>();
		conventions = new ImplTable<Convention>();
		deleted = new ImplTable<Record>();
		shared = new ImplTable<Record>();
		unchecked = new ImplTable<Record>();
	}

	public Table<Book> getBooks() {
		return books;
	}

	public Table<Circle> getCircles() {
		return circles;
	}

	public Table<Artist> getArtists() {
		return artists;
	}

	public Table<Parody> getParodies() {
		return parodies;
	}

	public Table<Content> getContents() {
		return contents;
	}

	public Table<Convention> getConventions() {
		return conventions;
	}

	@Override
	public Artist newArtist() {
		return new ImplArtist();
	}

	@Override
	public Book newBook() {
		return new ImplBook();
	}

	@Override
	public Circle newCircle() {
		return new ImplCircle();
	}

	@Override
	public Content newContent() {
		return new ImplContent();
	}

	@Override
	public Convention newConvention() {
		return new ImplConvention();
	}

	@Override
	public Parody newParody() {
		return new ImplParody();
	}

	@Override
	public void commit() throws DatabaseException
	{
		File file = new File(System.getProperty("user.home"), ".doujindb/doujindb.dbo");
		ObjectOutputStream out;
		try
		{
			out = new ObjectOutputStream(new FileOutputStream(file));
			Hashtable<String, Table<? extends Record>> serialized = new Hashtable<String, Table<? extends Record>>();
			serialized.put("Artist", artists);
			serialized.put("Book", books);
			serialized.put("Circle", circles);
			serialized.put("Content", contents);
			serialized.put("Convention", conventions);
			serialized.put("Parody", parodies);
			serialized.put("Deleted", deleted);
			serialized.put("Shared", shared);
			serialized.put("Unchecked", unchecked);
			out.writeObject(serialized);
			out.close();
		} catch (FileNotFoundException fnfe) {
			throw new DatabaseException("Database file not found.");
		} catch (IOException ioe) {
			throw new DatabaseException("Database I/O error (" + ioe.getMessage() + ").");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void rollback() throws DatabaseException
	{
		File file = new File(System.getProperty("user.home"), ".doujindb/doujindb.dbo");
		ObjectInputStream in;
		try
		{
			in = new ObjectInputStream(new FileInputStream(file));
			Hashtable<String, Serializable> serialized = (Hashtable<String, Serializable>) in.readObject();
			in.close();
			if(!serialized.containsKey("Artist"))
				throw new DatabaseException("Database load error : missing Artist table.");
			if(!serialized.containsKey("Book"))
				throw new DatabaseException("Database load error : missing Book table.");
			if(!serialized.containsKey("Circle"))
				throw new DatabaseException("Database load error : missing Circle table.");
			if(!serialized.containsKey("Content"))
				throw new DatabaseException("Database load error : missing Content table.");
			if(!serialized.containsKey("Convention"))
				throw new DatabaseException("Database load error : missing Convention table.");
			if(!serialized.containsKey("Parody"))
				throw new DatabaseException("Database load error : missing Parody table.");
			if(!serialized.containsKey("Deleted"))
				throw new DatabaseException("Database load error : missing Deleted table.");
			if(!serialized.containsKey("Shared"))
				throw new DatabaseException("Database load error : missing Shared table.");
			if(!serialized.containsKey("Unchecked"))
				throw new DatabaseException("Database load error : missing Unchecked table.");
			try {
				artists = (Table<Artist>) serialized.get("Artist");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Artist table.");
			}
			try {
				books = (Table<Book>) serialized.get("Book");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Book table.");
			}
			try {
				circles = (Table<Circle>) serialized.get("Circle");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Circle table.");
			}
			try {
				contents = (Table<Content>) serialized.get("Content");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Content table.");
			}
			try {
				conventions = (Table<Convention>) serialized.get("Convention");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Convention table.");
			}
			try {
				parodies = (Table<Parody>) serialized.get("Parody");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Parody table.");
			}
			try {
				deleted = (Table<Record>) serialized.get("Deleted");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Deleted table.");
			}
			try {
				shared = (Table<Record>) serialized.get("Shared");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Shared table.");
			}
			try {
				unchecked = (Table<Record>) serialized.get("Unchecked");
			} catch (ClassCastException cce) {
				throw new DatabaseException("Database load error : invalid Unchecked table.");
			}
		} catch (FileNotFoundException fnfe) {
			throw new DatabaseException("Database file not found.");
		} catch (IOException ioe) {
			throw new DatabaseException("Database I/O error (" + ioe.getMessage() + ").");
		} catch (ClassNotFoundException cnfe) {
			throw new DatabaseException("Database cast error (" + cnfe.getMessage() + ").");
		} catch (ClassCastException cce) {
			throw new DatabaseException("Database cast error (" + cce.getMessage() + ").");
		}
	}

	@Override
	public boolean getAutoCommit() throws DatabaseException
	{
		return autoCommit;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws DatabaseException
	{
		this.autoCommit = autoCommit;
	}

	@Override
	public String getConnection() throws DatabaseException
	{
		return "dbo://admin:@localhost/ ";
	}
}