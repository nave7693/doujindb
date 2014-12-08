package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.*;
import java.util.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Task")
class Task
{
	// Needed by JAXB
	// Define this or suffer an IllegalAnnotationsException : Task does not have a no-arg default constructor.
	Task() { }
	
	Task(String id, String file) {
		this.id = id;
		this.file = file;
	}
	
	@XmlAttribute(name="id")
	private String id = "";
	@XmlElement(name="file")
	private String file = "";
	
	@XmlElement(name="metadata")
	public Set<Metadata> metadata = new HashSet<Metadata>();
	
	@XmlJavaTypeAdapter(CDATAAdapter.class)
	public String message;
	@XmlJavaTypeAdapter(CDATAAdapter.class)
	public String exception;
	
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof Task))
			if(!(obj instanceof String))
				return false;
			else
				return ((String)obj).equals(id);
		else
			return ((Task)obj).id.equals(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
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
