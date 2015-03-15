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
	protected Set<Metadata> metadata = new HashSet<Metadata>();
	@XmlElement(name="message")
	protected String message;
	@XmlElement(name="warning")
	protected Map<String,String> warnings = new HashMap<String,String>();
	@XmlElement(name="error")
	protected Map<String,String> errors = new HashMap<String,String>();
	@XmlElement(name="duplicate")
	protected Set<Integer> duplicates = new HashSet<Integer>();
	
	protected transient boolean selected = false;
	
	static enum State {
		NEW(0),
		FIND_COVER(1),
		CROP_COVER(2),
		RESIZE_COVER(3),
		FIND_DUPLICATE(4),
		FETCH_METADATA(5),
		FIND_SIMILAR(6),
		INSERT_DATABASE(7),
		INSERT_DATASTORE(8),
		DONE(9);
		
		private Integer value;

		private State(Integer value) { this.value = value; }
		
		public Integer getValue() { return this.value; }
	}
	
	public void error(Throwable t) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			t.printStackTrace(ps);
			errors.put(t.getMessage(), os.toString("UTF8"));
		} catch (Exception e) {
			errors.put(t.getMessage(), "");
		}
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public Map<String,String> errors() {
		return errors;
	}
	
	public void warning(Throwable t) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			t.printStackTrace(ps);
			warnings.put(t.getMessage(), os.toString("UTF8"));
		} catch (Exception e) {
			warnings.put(t.getMessage(), "");
		}
	}
	
	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}
	
	public Map<String,String> warnings() {
		return warnings;
	}
	
	public void reset() {
		this.state = State.NEW;
		this.metadata = new HashSet<Metadata>();
		this.message = null;
		this.errors = new HashMap<String,String>();
		this.warnings = new HashMap<String,String>();
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
