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
	protected Integer score;
	
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
	protected Set<String> artist = new HashSet<String>();
	@XmlElement(name="circle")
	protected Set<String> circle = new HashSet<String>();
	@XmlElement(name="convention")
	protected String convention;
	@XmlElement(name="content")
	protected Set<String> content = new HashSet<String>();
	@XmlElement(name="parody")
	protected Set<String> parody = new HashSet<String>();
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
	static abstract class MetadataItem
	{
		@XmlAttribute(name="id")
		private Integer id;
		@XmlElement(name="name")
		private String name;
		
		public MetadataItem() { }
		
		public MetadataItem(String name) {
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
			if(!(obj instanceof MetadataItem))
				if(!(obj instanceof Integer))
					return false;
				else
					return ((Integer)obj).equals(id);
			else
				return ((MetadataItem)obj).id.equals(id);
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return getName();
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class MetadataArtist extends MetadataItem
	{
		public MetadataArtist() { }
		
		public MetadataArtist(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class MetadataCircle extends MetadataItem
	{
		public MetadataCircle() { }
		
		public MetadataCircle(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class MetadataConvention extends MetadataItem
	{
		public MetadataConvention() { }
		
		public MetadataConvention(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class MetadataContent extends MetadataItem
	{
		public MetadataContent() { }
		
		public MetadataContent(String name) {
			super(name);
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class MetadataParody extends MetadataItem
	{
		public MetadataParody() { }
		
		public MetadataParody(String name) {
			super(name);
		}
	}
}
