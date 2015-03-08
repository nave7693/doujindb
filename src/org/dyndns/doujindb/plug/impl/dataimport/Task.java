package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.*;
import java.util.*;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Task")
final class Task
{
	// Needed by JAXB
	// Define this or suffer an IllegalAnnotationsException : Task does not have a no-arg default constructor.
	Task() { }
	
	Task(String id, String file) {
		reset();
		this.id = id;
		this.file = file;
	}
	
	@XmlAttribute(name="id")
	protected String id = "";
	@XmlElement(name="file")
	protected String file = "";
	@XmlElement(name="state")
	protected State state;
	@XmlElement(name="metadata")
	protected Set<Metadata> metadata;
	@XmlElement(name="message")
	protected String message;
	@XmlElement(name="exception")
	protected String exception;
	
	protected transient boolean selected = false;
	
	static enum State {
		NEW,
		COMPLETE,
		ERROR,
		WARNING,
		ABORT,
		UNKNOW
	}
	
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
	
	public void reset() {
		this.state = State.NEW;
		this.metadata = new HashSet<Metadata>();
		this.message = null;
		this.exception = null;
		this.selected = false;
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + id;
	}
}
