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
	private String id = "";
	@XmlElement(name="file")
	private String file = "";
	@XmlElement(name="thumbnail")
	private String thumbnail;
	@XmlElement(name="state")
	private State state;
	@XmlElement(name="fetchedMetadata")
	private Set<Metadata> fetchedMetadata = new HashSet<Metadata>();
	@XmlElement(name="selectedMetadata")
	private Metadata selectedMetadata;
	@XmlElement(name="infoMessage")
	private String message;
	@XmlElement(name="warningMessage")
	private Map<String,String> warnings = new HashMap<String,String>();
	@XmlElement(name="errorMessage")
	private Map<String,String> errors = new HashMap<String,String>();
//	@XmlElement(name="duplicateBook")
	private Map<Integer, DuplicateOption> duplicates = new HashMap<Integer, DuplicateOption>();
	
	private transient boolean selected = false;
	
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
	
	public enum DuplicateOption {
		IGNORE,
		MERGE,
		REPLACE
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
	
	public void addMetadata(Metadata md) {
		fetchedMetadata.add(md);
	}
	
	public Set<Metadata> metadata() {
		return fetchedMetadata;
	}
	
	public void addDuplicate(Integer id) {
		duplicates.put(id, null);
	}
	
	public void addDuplicate(Integer id, DuplicateOption op) {
		duplicates.put(id, op);
	}
	
	public Map<Integer, DuplicateOption> duplicates() {
		return duplicates;
	}
	
	public boolean hasDuplicates() {
		return !duplicates.isEmpty();
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public String getFile() {
		return file;
	}

	public void reset() {
		this.state = State.NEW;
		this.fetchedMetadata = new HashSet<Metadata>();
		this.message = null;
		this.errors = new HashMap<String,String>();
		this.warnings = new HashMap<String,String>();
		this.selected = false;
		this.duplicates = new HashMap<Integer, DuplicateOption>();
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
