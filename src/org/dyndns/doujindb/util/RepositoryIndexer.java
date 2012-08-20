package org.dyndns.doujindb.util;

import java.io.*;
import java.util.*;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;

public final class RepositoryIndexer
{
	public static void rebuildIndexes()
	{
		RepositoryIndexer.rebuildIndexes(Core.Database.getBooks(null));
	}
	
	public static void rebuildIndexes(Iterable<Book> books)
	{
		for(Book book : books)
			RepositoryIndexer.xmlBook(book, Core.Repository.getMetadata(book.getID()).getOutputStream());
	}
	
	private static void xmlBook(Book book, OutputStream dest) throws DataBaseException
	{
		XMLBook doujin = new XMLBook();
		doujin.japaneseName = book.getJapaneseName();
		doujin.translatedName = book.getTranslatedName();
		doujin.romanjiName = book.getRomanjiName();
		doujin.Convention = book.getConvention() == null ? "" : book.getConvention().getTagName();
		doujin.Released = book.getDate();
		doujin.Type = book.getType();
		doujin.Pages = book.getPages();
		doujin.Adult = book.isAdult();
		doujin.Decensored = book.isDecensored();
		doujin.Colored = book.isColored();
		doujin.Translated = book.isTranslated();
		doujin.Rating = book.getRating();
		doujin.Info = book.getInfo();
		for(Artist a : book.getArtists())
			doujin.artists.add(a.getJapaneseName());
		for(Circle c : book.getCircles())
			doujin.circles.add(c.getJapaneseName());
		for(Parody p : book.getParodies())
			doujin.parodies.add(p.getJapaneseName());
		for(Content ct : book.getContents())
			doujin.contents.add(ct.getTagName());
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLBook.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(doujin, dest);
		} catch (Exception e) {
			Core.Logger.log("Error parsing XML file (" + e.getMessage() + ").", Level.WARNING);
		}
	}
	
	@SuppressWarnings("unused")
	@XmlRootElement(name="Doujin")
	private static final class XMLBook
	{
		@XmlElement(required=true)
		private String japaneseName;
		@XmlElement(required=false)
		private String translatedName = "";
		@XmlElement(required=false)
		private String romanjiName = "";
		@XmlElement(required=false)
		private String Convention = "";
		@XmlElement(required=false)
		private Date Released;
		@XmlElement(required=false)
		private Type Type;
		@XmlElement(required=false)
		private int Pages;
		@XmlElement(required=false)
		private boolean Adult;
		@XmlElement(required=false)
		private boolean Decensored;
		@XmlElement(required=false)
		private boolean Translated;
		@XmlElement(required=false)
		private boolean Colored;
		@XmlElement(required=false)
		private Rating Rating;
		@XmlElement(required=false)
		private String Info;
		@XmlElement(name="Artist", required=false)
		private List<String> artists = new Vector<String>();
		@XmlElement(name="Circle", required=false)
		private List<String> circles = new Vector<String>();
		@XmlElement(name="Parody", required=false)
		private List<String> parodies = new Vector<String>();
		@XmlElement(name="Content", required=false)
		private List<String> contents = new Vector<String>();
	}
}