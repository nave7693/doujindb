package org.dyndns.doujindb.util;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;

public final class Metadata
{
	public static void toXML(Book book, OutputStream out)
	{
		XMLBook xmlbook = new XMLBook();
		xmlbook.JapaneseName = book.getJapaneseName();
		xmlbook.TranslatedName = book.getTranslatedName();
		xmlbook.RomajiName = book.getRomajiName();
		xmlbook.Convention = book.getConvention() == null ? "" : book.getConvention().getTagName();
		xmlbook.Released = book.getDate();
		xmlbook.Type = book.getType();
		xmlbook.Pages = book.getPages();
		xmlbook.Adult = book.isAdult();
		xmlbook.Decensored = book.isDecensored();
		xmlbook.Colored = book.isColored();
		xmlbook.Translated = book.isTranslated();
		xmlbook.Rating = book.getRating();
		xmlbook.Info = book.getInfo();
		for(Artist a : book.getArtists())
			xmlbook.artists.add(a.getJapaneseName());
		for(Circle c : book.getCircles())
			xmlbook.circles.add(c.getJapaneseName());
		for(Parody p : book.getParodies())
			xmlbook.parodies.add(p.getJapaneseName());
		for(Content t : book.getContents())
			xmlbook.contents.add(t.getTagName());
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLBook.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(xmlbook, out);
			out.close();
		} catch (JAXBException jaxbe) {
			Logger.logError("Error writing XML metadata : " + jaxbe.getMessage(), jaxbe);
		} catch (IOException ioe) {
			Logger.logError("Error writing XML metadata : " + ioe.getMessage(), ioe);
		}
	}
	
	public static void toJSON(Book book, OutputStream out)
	{
		throw new RuntimeException("Not Yet Implemented");
	}
	
	@XmlRootElement(name="Book")
	private static final class XMLBook
	{
		@XmlElement(required=true)
		private String JapaneseName;
		@XmlElement(required=false)
		private String TranslatedName = "";
		@XmlElement(required=false)
		private String RomajiName = "";
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