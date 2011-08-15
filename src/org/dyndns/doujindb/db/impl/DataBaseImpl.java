package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="DataBase")
public final class DataBaseImpl extends UnicastRemoteObject implements DataBase
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
	
	public synchronized Table<Record> getDeleted() {
		return deleted;
	}

	public synchronized Table<Record> getShared() {
		return shared;
	}

	public synchronized Table<Record> getUnchecked() {
		return unchecked;
	}

	public DataBaseImpl() throws RemoteException
	{
		books = new TableImpl<Book>();
		circles = new TableImpl<Circle>();
		artists = new TableImpl<Artist>();
		parodies = new TableImpl<Parody>();
		contents = new TableImpl<Content>();
		conventions = new TableImpl<Convention>();
		deleted = new TableImpl<Record>();
		shared = new TableImpl<Record>();
		unchecked = new TableImpl<Record>();
	}

	public synchronized Table<Book> getBooks() {
		return books;
	}

	public synchronized Table<Circle> getCircles() {
		return circles;
	}

	public synchronized Table<Artist> getArtists() {
		return artists;
	}

	public synchronized Table<Parody> getParodies() {
		return parodies;
	}

	public synchronized Table<Content> getContents() {
		return contents;
	}

	public synchronized Table<Convention> getConventions() {
		return conventions;
	}

	@Override
	public synchronized Artist newArtist() throws DataBaseException {
		try {
			return new ArtistImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized Book newBook() throws DataBaseException {
		try {
			return new BookImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized Circle newCircle() throws DataBaseException {
		try {
			return new CircleImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized Content newContent() throws DataBaseException {
		try {
			return new ContentImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized Convention newConvention() throws DataBaseException {
		try {
			return new ConventionImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized Parody newParody() throws DataBaseException {
		try {
			return new ParodyImpl();
		} catch (RemoteException re) {
			throw new DataBaseException(re);
		}
	}

	@Override
	public synchronized void commit() throws DataBaseException
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
			throw new DataBaseException("DataBase file not found.");
		} catch (IOException ioe) {
			throw new DataBaseException("DataBase I/O error (" + ioe.getMessage() + ").");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void rollback() throws DataBaseException
	{
		File file = new File(System.getProperty("user.home"), ".doujindb/doujindb.dbo");
		ObjectInputStream in;
		try
		{
			in = new ObjectInputStream(new FileInputStream(file));
			Hashtable<String, Serializable> serialized = (Hashtable<String, Serializable>) in.readObject();
			in.close();
			if(!serialized.containsKey("Artist"))
				throw new DataBaseException("DataBase load error : missing Artist table.");
			if(!serialized.containsKey("Book"))
				throw new DataBaseException("DataBase load error : missing Book table.");
			if(!serialized.containsKey("Circle"))
				throw new DataBaseException("DataBase load error : missing Circle table.");
			if(!serialized.containsKey("Content"))
				throw new DataBaseException("DataBase load error : missing Content table.");
			if(!serialized.containsKey("Convention"))
				throw new DataBaseException("DataBase load error : missing Convention table.");
			if(!serialized.containsKey("Parody"))
				throw new DataBaseException("DataBase load error : missing Parody table.");
			if(!serialized.containsKey("Deleted"))
				throw new DataBaseException("DataBase load error : missing Deleted table.");
			if(!serialized.containsKey("Shared"))
				throw new DataBaseException("DataBase load error : missing Shared table.");
			if(!serialized.containsKey("Unchecked"))
				throw new DataBaseException("DataBase load error : missing Unchecked table.");
			try {
				artists = (Table<Artist>) serialized.get("Artist");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Artist table.");
			}
			try {
				books = (Table<Book>) serialized.get("Book");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Book table.");
			}
			try {
				circles = (Table<Circle>) serialized.get("Circle");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Circle table.");
			}
			try {
				contents = (Table<Content>) serialized.get("Content");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Content table.");
			}
			try {
				conventions = (Table<Convention>) serialized.get("Convention");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Convention table.");
			}
			try {
				parodies = (Table<Parody>) serialized.get("Parody");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Parody table.");
			}
			try {
				deleted = (Table<Record>) serialized.get("Deleted");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Deleted table.");
			}
			try {
				shared = (Table<Record>) serialized.get("Shared");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Shared table.");
			}
			try {
				unchecked = (Table<Record>) serialized.get("Unchecked");
			} catch (ClassCastException cce) {
				throw new DataBaseException("DataBase load error : invalid Unchecked table.");
			}
		} catch (FileNotFoundException fnfe) {
			throw new DataBaseException("DataBase file not found.");
		} catch (IOException ioe) {
			throw new DataBaseException("DataBase I/O error (" + ioe.getMessage() + ").");
		} catch (ClassNotFoundException cnfe) {
			throw new DataBaseException("DataBase cast error (" + cnfe.getMessage() + ").");
		} catch (ClassCastException cce) {
			throw new DataBaseException("DataBase cast error (" + cce.getMessage() + ").");
		}
	}

	@Override
	public synchronized boolean getAutoCommit() throws DataBaseException
	{
		return autoCommit;
	}

	@Override
	public synchronized void setAutoCommit(boolean autoCommit) throws DataBaseException
	{
		this.autoCommit = autoCommit;
	}

	@Override
	public synchronized String getConnection() throws DataBaseException
	{
		return "dbo://admin:@localhost/ ";
	}
}