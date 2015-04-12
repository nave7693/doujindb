package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Metadata")
@XmlSeeAlso({
	org.dyndns.doujindb.plug.impl.dataimport.MugiMugiProvider.Metadata.class,
	org.dyndns.doujindb.plug.impl.dataimport.EHentaiProvider.Metadata.class,
})
abstract class Metadata
{
	// Needed by JAXB
	// Define this or suffer an IllegalAnnotationsException : Task does not have a no-arg default constructor.
	Metadata() { }

	@XmlElement(name="score")
	protected Integer score = -1;
	
	@XmlElement(name="name")
	protected String name;
	@XmlElement(name="alias")
	protected Set<String> alias = new HashSet<String>();
	@XmlElement(name="translation")
	protected String translation;
	@XmlElement(name="pages")
	protected Integer pages;
	@XmlElement(name="timestamp")
	protected Long timestamp;
	@XmlElement(name="type")
	protected String type;
	@XmlElement(name="adult")
	protected Boolean adult;
	@XmlElement(name="info")
	protected String info;
	@XmlElement(name="size")
	protected Long size;
	@XmlElement(name="artist")
	protected Set<Artist> artist = new HashSet<Artist>();
	@XmlElement(name="circle")
	protected Set<Circle> circle = new HashSet<Circle>();
	@XmlElement(name="convention")
	protected Convention convention;
	@XmlElement(name="content")
	protected Set<Content> content = new HashSet<Content>();
	@XmlElement(name="parody")
	protected Set<Parody> parody = new HashSet<Parody>();
	@XmlElement(name="uri")
	protected String uri;
	@XmlElement(name="thumbnail")
	protected String thumbnail;
	
	@XmlElement(name="message")
	protected String message;
	@XmlElement(name="exception")
	protected String exception;
	
	public void exception(Throwable t) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			t.printStackTrace(ps);
			exception = os.toString("UTF8");
		} catch (Exception e) {
			exception = t.getMessage();
		}
	}
	
	public abstract String provider();
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static abstract class Item
	{
		@XmlAttribute(name="id")
		private Integer id;
		@XmlElement(name="name")
		private String name;
		
		public Item() { }
		
		public Item(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Integer getId() {
			return this.id;
		}
		
		public void setId(Integer id) {
			this.id = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			if(!(obj instanceof Item))
				if(!(obj instanceof String))
					return false;
				else
					return ((String)obj).equals(name);
			else
				return ((Item)obj).name.equals(name);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return getName();
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Artist extends Item
	{
		public Artist() { }
		
		public Artist(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Circle extends Item
	{
		public Circle() { }
		
		public Circle(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Convention extends Item
	{
		public Convention() { }
		
		public Convention(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Content extends Item
	{
		public Content() { }
		
		public Content(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Parody extends Item
	{
		public Parody() { }
		
		public Parody(String name) {
			super(name);
		}
	}
	
	@XmlRootElement
	@XmlType(namespace="default", name="Metadata")
	public static final class Default extends Metadata {
		Default() { }

		@Override
		public String provider() {
			return "default";
		}
	}
}
