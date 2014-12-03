package org.dyndns.doujindb.plug.impl.dataimport;

import java.util.Set;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Task")
@XmlSeeAlso({org.dyndns.doujindb.plug.impl.dataimport.TaskManager.TaskImpl.class})
abstract class Task
{
	// Needed by JAXB
	// Define this or suffer an IllegalAnnotationsException : Task does not have a no-arg default constructor.
	Task() { }
	
	Task(String id, String path)
	{
		this.id = id;
		this.path = path;
	}
	
	enum Info {
		IDLE,		// Yet to be started
		RUNNING,	// Still running
		WARNING,	// Completed with warning(s)
		ERROR,		// Automatically stopped by error(s)
		COMPLETED,	// Completed successfully
		PAUSED		// Waiting for user input
	}
	
	enum Exec {
		NO_OPERATION,		// Slacking off
		CHECK_API,			// Checks if API key is set/valid
		CHECK_DUPLICATE,	// Checks if item is a duplicate entry (cover image)
		CHECK_SIMILARITY,	// Checks if item has a similar entry (name)
		SAVE_DATABASE,		// Insert data into the database
		SAVE_DATASTORE,		// Save data files in the datastore
		SCAN_IMAGE,			// Find/resize cover image to be uploaded
		UPLOAD_IMAGE,		// Upload image to mugimugi API system
		PARSE_XML,			// Parse returned XML data
		PARSE_BID,			// Add data directly from a BookID (mugimugi)
		CLEANUP_DATA		// Housekeeping
	}
	
	@XmlAttribute(name="taskId")
	private String id;
	@XmlElement(name="taskPath")
	private String path;
	@XmlElement(name="searchThreshold")
	private int threshold = DataImport.THRESHOLD;
	@XmlElement(name="taskInfo")
	private Task.Info info = Task.Info.IDLE;
	@XmlElement(name="taskExec")
	private Task.Exec exec = Task.Exec.NO_OPERATION;
	
	@XmlElement(name="messageError")
	private String error;
	@XmlElement(name="messageWarning")
	private String warning;
	
	@XmlElement(name="mugimugiBId")
	private Integer mugimugi_bid;
	@XmlElement(name="taskBook")
	private Integer book;
	
	@XmlElement(name="duplicateList")
	private Set<Integer> duplicate_list;
	@XmlElement(name="mugimugiList")
	private Set<Integer> mugimugi_list;

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

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public Task.Info getInfo() {
		return info;
	}

	public void setInfo(Task.Info info) {
		this.info = info;
	}

	public Task.Exec getExec() {
		return exec;
	}

	public void setExec(Task.Exec exec) {
		this.exec = exec;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}
	
	public Integer getBook() {
		return book;
	}
	
	protected void setBook(Integer book) {
		this.book = book;
	}
	
	public Integer getMugimugiBid() {
		return mugimugi_bid;
	}
	
	protected void setMugimugiBid(Integer mugimugi_bid) {
		this.mugimugi_bid = mugimugi_bid;
	}
	
	public Set<Integer> getDuplicatelist() {
		return duplicate_list;
	}

	protected void setDuplicateList(Set<Integer> duplicate_list) {
		this.duplicate_list = duplicate_list;
	}
	
	public Set<Integer> getMugimugiList() {
		return mugimugi_list;
	}

	protected void setMugimugiList(Set<Integer> mugimugi_list) {
		this.mugimugi_list = mugimugi_list;
	}
	
	public String getError() {
		return error;
	}
	
	protected void setError(String error) {
		this.error = error;
	}
	
	public String getWarning() {
		return warning;
	}
	
	protected void setWarning(String warning) {
		this.warning = warning;
	}

	public abstract boolean isRunning();
	
	public abstract String getMessage();
	
	public abstract int getProgress();
	
	private transient boolean selected = false;

}