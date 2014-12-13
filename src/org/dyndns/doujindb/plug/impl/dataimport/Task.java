package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.*;
import java.util.*;

import javax.xml.bind.annotation.*;

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
		this.state = State.NEW;
	}
	
	@XmlAttribute(name="id")
	protected String id = "";
	@XmlElement(name="file")
	protected String file = "";
	@XmlElement(name="state")
	protected State state = State.UNKNOW;
	
	protected transient boolean selected = false;
	
	@XmlElement(name="metadata")
	protected Set<Metadata> metadata = new HashSet<Metadata>();
	
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
	
	static enum State {
		NEW,
		COMPLETE,
		ERROR,
		WARNING,
		ABORT,
		UNKNOW
	}

	public void reset() {
		this.state = State.NEW;
		this.metadata = new HashSet<Metadata>();
		this.message = null;
		this.exception = null;
	}
}
