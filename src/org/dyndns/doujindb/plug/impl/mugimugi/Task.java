package org.dyndns.doujindb.plug.impl.mugimugi;

import java.util.Set;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.mugimugi", name="Task")
@XmlSeeAlso({org.dyndns.doujindb.plug.impl.mugimugi.TaskManager.TaskImpl.class})
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
	
	@XmlAttribute(name="id")
	private String id;
	@XmlElement(name="path")
	private String path;
	@XmlElement(name="threshold")
	private int threshold = DoujinshiDBScanner.THRESHOLD;
	@XmlElement(name="info")
	private TaskInfo info = TaskInfo.IDLE;
	@XmlElement(name="exec")
	private TaskExec exec = TaskExec.NO_OPERATION;
	
	@XmlElement(name="error")
	private String error;
	@XmlElement(name="warning")
	private String warning;
	
	@XmlElement(name="mugimugi_bid")
	private String mugimugi_bid;
	@XmlElement(name="book")
	private String book;
	
	@XmlElement(name="duplicate_list")
	private Set<String> duplicate_list;
	@XmlElement(name="mugimugi_list")
	private Set<String> mugimugi_list;

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

	public TaskInfo getInfo() {
		return info;
	}

	public void setInfo(TaskInfo info) {
		this.info = info;
	}

	public TaskExec getExec() {
		return exec;
	}

	public void setExec(TaskExec exec) {
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
	
	public String getBook() {
		return book;
	}
	
	protected void setBook(String book) {
		this.book = book;
	}
	
	public String getMugimugiBid() {
		return mugimugi_bid;
	}
	
	protected void setMugimugiBid(String mugimugi_bid) {
		this.mugimugi_bid = mugimugi_bid;
	}
	
	public Set<String> getDuplicatelist() {
		return duplicate_list;
	}

	protected void setDuplicateList(Set<String> duplicate_list) {
		this.duplicate_list = duplicate_list;
	}
	
	public Set<String> getMugimugiList() {
		return mugimugi_list;
	}

	protected void setMugimugiList(Set<String> mugimugi_list) {
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