package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="Task")
final class Task implements Runnable
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
	@XmlAttribute(name="state")
	private State state = State.FACTORY_RESET;
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
	@XmlElement(name="duplicateBook")
	private Set<Duplicate> duplicates = new HashSet<Duplicate>();
	@XmlAttribute(name="needInput")
	private boolean needInput = false;
	@XmlElement(name="result")
	private Integer result;
	
	private transient boolean selected = false;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(Task.class);
	
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
	
	public Map<String,String> errors() {
		return errors;
	}
	
	public void addMetadata(Metadata md) {
		fetchedMetadata.add(md);
	}
	
	public Set<Metadata> fetchedMetadata() {
		return fetchedMetadata;
	}
	
	public void selectMetadata(Metadata md) {
		selectedMetadata = md;
	}
	
	public Metadata selectedMetadata() {
		return selectedMetadata;
	}
	
	public void addDuplicate(Duplicate duplicate) {
		duplicates.add(duplicate);
	}
	
	public Set<Duplicate> duplicates() {
		return duplicates;
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
	
	public boolean needInput() {
		return needInput;
	}

	public void needInput(boolean needAnswer) {
		this.needInput = needAnswer;
	}
	
	public void setResult(Integer result) {
		this.result = result;
	}
	
	public Integer getResult() {
		return result;
	}

	public void reset() {
		this.result = null;
		this.state = State.FACTORY_RESET;
		this.fetchedMetadata = new HashSet<Metadata>();
		this.selectedMetadata = null;
		this.message = null;
		this.errors = new HashMap<String,String>();
		this.warnings = new HashMap<String,String>();
		this.selected = false;
		this.duplicates = new HashSet<Duplicate>();
		this.needInput = false;
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
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static final class Duplicate
	{
		public static enum Option {
			IGNORE,
			MERGE,
			REPLACE
		}
		@XmlAttribute(name="id")
		public Integer id;
		@XmlElement(name="metadataOption")
		public Option metadataOption = Option.IGNORE;
		@XmlElement(name="dataOption")
		public Option dataOption = Option.IGNORE;
		@XmlElement(name="annotation")
		public Set<String> annotations = new HashSet<String>();
		
		public Duplicate() { }
		
		public Duplicate(Integer id) {
			this.id = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			if(!(obj instanceof Duplicate))
				if(!(obj instanceof Integer))
					return false;
				else
					return ((Integer)obj).equals(id);
			else
				return ((Duplicate)obj).id.equals(id);
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

	public static enum State {

		FACTORY_RESET {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return IMAGE_SEARCH;
			}
		},
		IMAGE_SEARCH {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return IMAGE_CROP;
			}
		},
		IMAGE_CROP {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return IMAGE_RESIZE;
			}
		},
		IMAGE_RESIZE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return SIMILAR_CHECK;
			}
		},
		SIMILAR_CHECK {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return METADATA_FETCH;
			}
		},
		METADATA_FETCH {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return DUPLICATE_CHECK;
			}
		},
		DUPLICATE_CHECK {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return DATABASE_INSERT;
			}
		},
		DATABASE_INSERT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return DATASTORE_INSERT;
			}
		},
		DATASTORE_INSERT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return TASK_CLEANUP;
			}
		},
		TASK_CLEANUP {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return TASK_COMPLETE;
			}
		},
		TASK_COMPLETE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		},
		TASK_PAUSE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		},
		SIMILAR_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		},
		DUPLICATE_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		},
		METADATA_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		},
		ERROR_RAISE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return this;
			}
		};

		protected void preRun(Task context) { LOG.debug("call preRun(Task={}, State={})", new Object[]{context.getId(), this}); }
		abstract State run(Task context);
		protected void postRun(Task context) { LOG.debug("call postRun(Task={}, State={})", new Object[]{context.getId(), this}); }
		
		State processWrapper(Task context) {
			try {
				preRun(context);
				State state = run(context);
				postRun(context);
				TaskManager.fireTaskChanged(context);
				return state;
			} catch (TaskException te) {
				LOG.error("Exception while processing Task Id={}", context.getId(), te);
				return ERROR_RAISE;
			} catch (Exception e) {
				LOG.error("Exception while processing Task Id={}", context.getId(), e);
				// This error was not supposed to happen, re-throw it
				throw new TaskException(e);
			}
		}
	}

	@Override
	public void run() {
		state = state.processWrapper(this);
	}
}
