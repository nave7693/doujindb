package org.dyndns.doujindb.plug.impl.dataimport;

import java.util.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Metadata")
@XmlSeeAlso({
	org.dyndns.doujindb.plug.impl.dataimport.MugiMugiProvider.Metadata.class,
})
abstract class Metadata
{
	// Needed by JAXB
	// Define this or suffer an IllegalAnnotationsException : Task does not have a no-arg default constructor.
	Metadata() { }

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
	@XmlJavaTypeAdapter(CDATAAdapter.class)
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
	
	private static class CDATAAdapter extends XmlAdapter<String, String>
	{
	    @Override
	    public String marshal(String str) throws Exception {
	        return "<![CDATA[" + str + "]]>";
	    }
	    @Override
	    public String unmarshal(String str) throws Exception {
	        return str;
	    }
	}
}
