package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.db.record.Book.Rating;
import org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch;
import org.dyndns.doujindb.util.ImageTool;
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
	@XmlElement(name="errorMessage")
	private Map<String,String> errors = new HashMap<String,String>();
	@XmlElement(name="duplicateBook")
	private Set<Duplicate> duplicates = new HashSet<Duplicate>();
	@XmlElement(name="result")
	private Integer result;
	@XmlElement(name="locked")
	private boolean locked = false;
	
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
	
	public void lock() {
		this.locked = true;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void unlock() throws TaskException {
		this.locked = false;
		switch(state) {
			case SIMILAR_SELECT:
				state = State.SIMILAR_CHECK.next();
				break;
			case DUPLICATE_SELECT:
				state = State.DUPLICATE_CHECK.next();
				break;
			case METADATA_SELECT:
				state = State.METADATA_FETCH.next();
				break;
		}
	}
	
	public boolean isLocked() {
		return locked;
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

	public String getId() {
		return id;
	}

	public String getFile() {
		return file;
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
		this.locked = false;
		this.selected = false;
		this.duplicates = new HashSet<Duplicate>();
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
				return next();
			}

			@Override
			State next() { return IMAGE_SEARCH; }
		},
		IMAGE_SEARCH {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				File thumbnail = new File(context.getThumbnail());
				File image = findImage(context.getFile());
				if(image == null) {
					throw new TaskException("Could not locate any image file in " + context.getFile());
				}
				LOG.debug("Found image file {}", image);
				BufferedImage bImage;
				try {
					bImage = javax.imageio.ImageIO.read(image);
				} catch (IOException ioe) {
					throw new TaskException("Error loading image from file " + image, ioe);
				}
				if(bImage == null) {
					throw new TaskException("Could not load BufferedImage from " + image);
				}
				try {
					javax.imageio.ImageIO.write(bImage, "PNG", new File(context.getThumbnail()));
				} catch (IOException ioe) {
					throw new TaskException("Could not write image file " + thumbnail, ioe);
				}
				LOG.debug("Saved BufferedImage to file {}", context.getThumbnail());
				return next();
			}

			@Override
			State next() { return IMAGE_CROP; }
			
		},
		IMAGE_CROP {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				if(!Configuration.options_autocrop.get()) {
					LOG.debug("Image auto-cropping disabled, skipped");
					return next();
				}
				File thumbnail = new File(context.getThumbnail());
				BufferedImage src;
				try {
					src = javax.imageio.ImageIO.read(thumbnail);
				} catch (IOException ioe) {
					throw new TaskException("Error loading image from file " + thumbnail, ioe);
				}
				if(src == null)
					throw new TaskException("Error loading image from file " + thumbnail);
				BufferedImage dest;
				int img_width = src.getWidth(),
						img_height = src.getHeight();
				if(img_width > img_height)
					dest = new BufferedImage(img_width / 2, img_height, BufferedImage.TYPE_INT_ARGB);
				else
					dest = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_ARGB);
				Graphics g = dest.getGraphics();
				g.drawImage(src, 0, 0, img_width, img_height, null);
				g.dispose();
				try {
					javax.imageio.ImageIO.write(dest, "PNG", thumbnail);
				} catch (Exception e) {
					throw new TaskException("Could not write image file " + thumbnail, e);
				}
				return next();
			}

			@Override
			State next() { return IMAGE_RESIZE; }
		},
		IMAGE_RESIZE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				if(!Configuration.options_autoresize.get()) {
					LOG.debug("Image auto-resizing disabled, skipped");
					return next();
				}
				LOG.debug("Resizing image file");
				File thumbnail = new File(context.getThumbnail());
				BufferedImage src;
				try {
					src = javax.imageio.ImageIO.read(thumbnail);
				} catch (IOException ioe) {
					throw new TaskException("Error loading image from file " + thumbnail, ioe);
				}
				if(src == null)
					throw new TaskException("Error loading image from file " + thumbnail);
				BufferedImage dest;
				try
				{
					dest = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
					dest = ImageTool.getScaledInstance(src, 256, 256, true);
				} catch (Exception e) {
					throw new TaskException("Could not resize image file " + thumbnail, e);
				}
				try {
					javax.imageio.ImageIO.write(dest, "PNG", thumbnail);
				} catch (IOException ioe) {
					throw new TaskException("Could not write image file " + thumbnail, ioe);
				}
				return next();
			}

			@Override
			State next() { return SIMILAR_CHECK; }
		},
		SIMILAR_CHECK {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				if(!Configuration.options_check_similar.get()) {
					LOG.debug("Image similarity scan disabled, skipped");
					return next();
				}
				LOG.debug("Checking for similar Image entries");
				File thumbnail = new File(context.getThumbnail());
				Integer found = ImageSearch.search(thumbnail);
				if(found != null) {
					Task.Duplicate duplicate = new Task.Duplicate(found);
					try {
						if(DataStore.getStore(found).getFile("@japanese").exists())
							duplicate.annotations.add("missing japanese language");
					} catch (DataStoreException dse) {
						LOG.warn("{} Exception in langCheck", new Object[]{context, dse});
					}
					try {
						long bytesNew = DataStore.diskUsage(new File(context.getFile()));
						long bytesFound = DataStore.diskUsage(DataStore.getStore(found));
						if(bytesNew > bytesFound)
							duplicate.annotations.add("bigger filesize : " + format(bytesNew) + " > " + format(bytesFound) + "");
					} catch (Exception e) {
						LOG.warn("{} Exception in sizeCheck", new Object[]{context, e});
					}
					try {
						long filesNew = DataStore.listFiles(new File(context.getFile())).length;
						long filesFound = DataStore.listFiles(DataStore.getStore(found)).length;
						if(filesNew > filesFound)
							duplicate.annotations.add("more files : " + filesNew + " > " + filesFound + "");
					} catch (Exception e) {
						LOG.warn("{} Exception in countCheck", new Object[]{context, e});
					}
					try {
						BufferedImage imageNew = javax.imageio.ImageIO.read(new FileInputStream(findImage(new File(context.getFile()))));
						String resolutionNew = imageNew.getWidth() + "x" + imageNew.getHeight();
						BufferedImage imageFound = javax.imageio.ImageIO.read(findImage(DataStore.getStore(found)).openInputStream());
						String resolutionFound = imageFound.getWidth() + "x" + imageFound.getHeight();
						if(imageNew.getHeight() > imageFound.getHeight())
							duplicate.annotations.add("higher resolution : " + resolutionNew + " > " + resolutionFound + "");
					} catch (Exception e) {
						LOG.warn("{} Exception in resolutionCheck", new Object[]{context, e});
					}
					context.addDuplicate(duplicate);
				}
				if(!context.duplicates().isEmpty()) {
					return SIMILAR_SELECT;
				}
				return next();
			}

			@Override
			State next() { return METADATA_FETCH; }
		},
		METADATA_FETCH {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				boolean needMetadata = context.duplicates().isEmpty(); // No duplicate => need to fetch Metadata
				for(Task.Duplicate d : context.duplicates())
					if(d.metadataOption != Task.Duplicate.Option.IGNORE) { // At least 1 duplicate without IGNORE option => need to fetch Metadata
						needMetadata = true;
						break;
					}
				if(!needMetadata) {
					LOG.debug("{} does not need Metadata to be fetched", context);
					return next();
				}
				File thumbnail = new File(context.getThumbnail());
				for(MetadataProvider provider : Metadata.providers()) {
					if(!provider.isEnabled()) {
						LOG.debug("{} metadata provider [{}] is disabled and will not be used", new Object[]{context, provider});
						continue;
					}
					LOG.debug("{} Load metadata with provider [{}]", new Object[]{context, provider});
					try {
						Metadata md = provider.query(thumbnail);
						context.addMetadata(md);
						if(md.exception != null) {
							LOG.warn("{} Exception from provider [{}]: {}", new Object[]{context, provider, md.message});
						}
					} catch (Exception e) {
						LOG.warn("{} Exception from provider [{}]", new Object[]{context, provider, e});
					}
				}
				for(Metadata md : context.fetchedMetadata()) {
					// Map Artist items
					for(Metadata.Artist mobj : md.artist) {
						QueryArtist query = new QueryArtist();
						query.JapaneseName = mobj.getName();
						query.RomajiName = mobj.getName();
						query.TranslatedName = mobj.getName();
						query.QueryType = Query.Type.OR;
						for(Artist obj : DataBase.getArtists(query)) {
							if(obj.getJapaneseName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Artist obj : DataBase.getArtists(query)) {
							if(obj.getRomajiName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Artist obj : DataBase.getArtists(query)) {
							if(obj.getTranslatedName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
					}
					// Map Circle items
					for(Metadata.Circle mobj : md.circle) {
						QueryCircle query = new QueryCircle();
						query.JapaneseName = mobj.getName();
						query.RomajiName = mobj.getName();
						query.TranslatedName = mobj.getName();
						query.QueryType = Query.Type.OR;
						for(Circle obj : DataBase.getCircles(query)) {
							if(obj.getJapaneseName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Circle obj : DataBase.getCircles(query)) {
							if(obj.getRomajiName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Circle obj : DataBase.getCircles(query)) {
							if(obj.getTranslatedName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
					}
					// Map Content items
					for(Metadata.Content mobj : md.content) {
						QueryContent query = new QueryContent();
						query.TagName = mobj.getName();
						for(Content obj : DataBase.getContents(query)) {
							if(obj.getTagName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
					}
					// Map Parody items
					for(Metadata.Parody mobj : md.parody) {
						QueryParody query = new QueryParody();
						query.JapaneseName = mobj.getName();
						query.RomajiName = mobj.getName();
						query.TranslatedName = mobj.getName();
						query.QueryType = Query.Type.OR;
						for(Parody obj : DataBase.getParodies(query)) {
							if(obj.getJapaneseName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Parody obj : DataBase.getParodies(query)) {
							if(obj.getRomajiName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
						if(mobj.getId() != null)
							break; // Found our match
						for(Parody obj : DataBase.getParodies(query)) {
							if(obj.getTranslatedName().equalsIgnoreCase(mobj.getName())) {
								mobj.setId(obj.getId());
								break;
							}
						}
					}
					// Map Convention item
					if(md.convention != null) {
						QueryConvention query = new QueryConvention();
						query.TagName = md.convention.getName();
						for(Convention obj : DataBase.getConventions(query)) {
							if(obj.getTagName().equalsIgnoreCase(md.convention.getName())) {
								md.convention.setId(obj.getId());
								break;
							}
						}
					}
				}
				Integer score = Integer.MIN_VALUE;
				Metadata selectedMetadata = null;
				for(Metadata md : context.fetchedMetadata()) {
					if(md.score > score && md.exception == null) {
						selectedMetadata = md;
						score = md.score;
					}
				}
				context.selectMetadata(selectedMetadata);
				if(context.selectedMetadata() == null) {
					return METADATA_SELECT;
				}
				return next();
			}

			@Override
			State next() { return DUPLICATE_CHECK; }
		},
		DUPLICATE_CHECK {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				if(!Configuration.options_check_duplicate.get()) {
					LOG.debug("Metadata duplicate scan disabled, skipped");
					return next();
				}
				LOG.debug("Checking for duplicate entries");
				Set<Integer> duplicates = new HashSet<Integer>();
				QueryBook query;
				Metadata md = context.selectedMetadata();
				if(!md.name.equals("")) {
					query = new QueryBook();
					query.JapaneseName = md.name;
					for(Book b : DataBase.getBooks(query))
						duplicates.add(b.getId());
				}
				for(String alias : md.alias) {
					if(!alias.equals("")) {
						query = new QueryBook();
						query.JapaneseName = alias;
						for(Book b : DataBase.getBooks(query))
							duplicates.add(b.getId());
					}
				}
				if(!duplicates.isEmpty()) {
					for(Integer book : duplicates)
						if(!context.duplicates().contains(book))
							context.addDuplicate(new Task.Duplicate(book));
				}
				if(!context.duplicates().isEmpty()) {
					return DUPLICATE_SELECT;
				}		
				return next();
			}

			@Override
			State next() { return DATABASE_INSERT; }
		},
		DATABASE_INSERT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				Map<Integer, Task.Duplicate.Option> duplicates = new HashMap<Integer, Task.Duplicate.Option>();
				for(Task.Duplicate duplicate : context.duplicates()) {
					if(duplicate.dataOption == Task.Duplicate.Option.IGNORE && duplicate.metadataOption == Task.Duplicate.Option.IGNORE)
						continue;
					duplicates.put(duplicate.id, duplicate.metadataOption);
				}
				Metadata md = context.selectedMetadata();
				if(duplicates.isEmpty()) { // No duplicate to process, create a new Book
					try {
						Book book;
						book = DataBase.doInsert(Book.class);
						context.setResult(book.getId());
						//TODO Refactor
						book.setJapaneseName(md.name);
						book.setTranslatedName(md.translation);
						book.setDate(new Date(md.timestamp));
						book.setPages(md.pages);
						book.setAdult(md.adult);
						book.setRating(Rating.UNRATED);
						book.setInfo(md.info);
						try { book.setType(Book.Type.valueOf(md.type)); } catch (Exception e) { book.setType(Book.Type.不詳); }
						//TODO Refactor
						if(md.convention != null) {
							if(md.convention.getId() != null) {
								QueryConvention q = new QueryConvention();
								q.Id = md.convention.getId();
								book.setConvention(DataBase.getConventions(q).iterator().next());
							} else {
								Convention newo = DataBase.doInsert(Convention.class);
								newo.setTagName(md.convention.getName());
								DataBase.doCommit();
							}
						}
						for(Metadata.Artist o : md.artist) {
							if(o.getId() != null) {
								QueryArtist q = new QueryArtist();
								q.Id = o.getId();
								book.addArtist(DataBase.getArtists(q).iterator().next());
							} else {
								Artist newo = DataBase.doInsert(Artist.class);
								newo.setJapaneseName(o.getName());
								newo.setTranslatedName(o.getName());
								DataBase.doCommit();
							}
						}
						for(Metadata.Circle o : md.circle) {
							if(o.getId() != null) {
								QueryCircle q = new QueryCircle();
								q.Id = o.getId();
								book.addCircle(DataBase.getCircles(q).iterator().next());
							} else {
								Circle newo = DataBase.doInsert(Circle.class);
								newo.setJapaneseName(o.getName());
								newo.setTranslatedName(o.getName());
								DataBase.doCommit();
							}
						}
						for(Metadata.Content o : md.content) {
							if(o.getId() != null) {
								QueryContent q = new QueryContent();
								q.Id = o.getId();
								book.addContent(DataBase.getContents(q).iterator().next());
							} else {
								Content newo = DataBase.doInsert(Content.class);
								newo.setTagName(o.getName());
								DataBase.doCommit();
							}
						}
						for(Metadata.Parody o : md.parody) {
							if(o.getId() != null) {
								QueryParody q = new QueryParody();
								q.Id = o.getId();
								book.addParody(DataBase.getParodies(q).iterator().next());
							} else {
								Parody newo = DataBase.doInsert(Parody.class);
								newo.setJapaneseName(o.getName());
								newo.setTranslatedName(o.getName());
								DataBase.doCommit();
							}
						}
						DataBase.doCommit();
					} catch (NullPointerException e) {
						throw new TaskException("Error inserting DataBase info", e);
					}
				} else { // Process duplicates
					for(Integer id : duplicates.keySet()) {
						Book book = null;
						Task.Duplicate.Option option = duplicates.get(id);
						{
							QueryBook qid = new QueryBook();
							qid.Id = id;
							RecordSet<Book> set = DataBase.getBooks(qid);
							if(set.size() == 1)
								book = set.iterator().next();
							else {
								LOG.error("Error retrieving Book Id={} from DataBase", id);
								continue;
							}
						}
						switch(option) {
						case MERGE:
							LOG.debug("Merging Metadata in Book Id={}", id);
							//TODO Refactor
							if(md.convention != null) {
								if(md.convention.getId() != null) {
									QueryConvention q = new QueryConvention();
									q.Id = md.convention.getId();
									book.setConvention(DataBase.getConventions(q).iterator().next());
								} else {
									Convention newo = DataBase.doInsert(Convention.class);
									newo.setTagName(md.convention.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Artist o : md.artist) {
								if(o.getId() != null) {
									QueryArtist q = new QueryArtist();
									q.Id = o.getId();
									book.addArtist(DataBase.getArtists(q).iterator().next());
								} else {
									Artist newo = DataBase.doInsert(Artist.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Circle o : md.circle) {
								if(o.getId() != null) {
									QueryCircle q = new QueryCircle();
									q.Id = o.getId();
									book.addCircle(DataBase.getCircles(q).iterator().next());
								} else {
									Circle newo = DataBase.doInsert(Circle.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Content o : md.content) {
								if(o.getId() != null) {
									QueryContent q = new QueryContent();
									q.Id = o.getId();
									book.addContent(DataBase.getContents(q).iterator().next());
								} else {
									Content newo = DataBase.doInsert(Content.class);
									newo.setTagName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Parody o : md.parody) {
								if(o.getId() != null) {
									QueryParody q = new QueryParody();
									q.Id = o.getId();
									book.addParody(DataBase.getParodies(q).iterator().next());
								} else {
									Parody newo = DataBase.doInsert(Parody.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							break;
						case REPLACE:
							LOG.debug("Replacing Metadata in Book Id={}", id);
							//TODO Refactor
							book.setJapaneseName(md.name);
							book.setTranslatedName(md.translation);
							book.setDate(new Date(md.timestamp));
							book.setPages(md.pages);
							book.setAdult(md.adult);
							book.setRating(Rating.UNRATED);
							book.setInfo(md.info);
							try { book.setType(Book.Type.valueOf(md.type)); } catch (Exception e) { book.setType(Book.Type.不詳); }
							// Clear Book
							book.removeAll();
							//TODO Refactor
							if(md.convention != null) {
								if(md.convention.getId() != null) {
									QueryConvention q = new QueryConvention();
									q.Id = md.convention.getId();
									book.setConvention(DataBase.getConventions(q).iterator().next());
								} else {
									Convention newo = DataBase.doInsert(Convention.class);
									newo.setTagName(md.convention.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Artist o : md.artist) {
								if(o.getId() != null) {
									QueryArtist q = new QueryArtist();
									q.Id = o.getId();
									book.addArtist(DataBase.getArtists(q).iterator().next());
								} else {
									Artist newo = DataBase.doInsert(Artist.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Circle o : md.circle) {
								if(o.getId() != null) {
									QueryCircle q = new QueryCircle();
									q.Id = o.getId();
									book.addCircle(DataBase.getCircles(q).iterator().next());
								} else {
									Circle newo = DataBase.doInsert(Circle.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Content o : md.content) {
								if(o.getId() != null) {
									QueryContent q = new QueryContent();
									q.Id = o.getId();
									book.addContent(DataBase.getContents(q).iterator().next());
								} else {
									Content newo = DataBase.doInsert(Content.class);
									newo.setTagName(o.getName());
									DataBase.doCommit();
								}
							}
							for(Metadata.Parody o : md.parody) {
								if(o.getId() != null) {
									QueryParody q = new QueryParody();
									q.Id = o.getId();
									book.addParody(DataBase.getParodies(q).iterator().next());
								} else {
									Parody newo = DataBase.doInsert(Parody.class);
									newo.setJapaneseName(o.getName());
									newo.setTranslatedName(o.getName());
									DataBase.doCommit();
								}
							}
							break;
						case IGNORE:
							break;
						}
					}
				}
				return next();
			}

			@Override
			State next() { return DATASTORE_INSERT; }
		},
		DATASTORE_INSERT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				Map<Integer, Task.Duplicate.Option> duplicates = new HashMap<Integer, Task.Duplicate.Option>();
				for(Task.Duplicate duplicate : context.duplicates()) {
					if(duplicate.dataOption == Task.Duplicate.Option.IGNORE && duplicate.metadataOption == Task.Duplicate.Option.IGNORE)
						continue;
					duplicates.put(duplicate.id, duplicate.dataOption);
				}
				if(duplicates.isEmpty()) { // No duplicate to process, process Result
					File basepath = new File(context.getFile());
					try {
						DataFile store = DataStore.getStore(context.getResult());
						store.mkdirs();
						DataStore.fromFile(basepath, store, true);
					} catch (DataBaseException | IOException | DataStoreException e) {
						throw new TaskException("Error copying '" + basepath + "' in DataStore", e);
					}
					try {
						DataFile df = DataStore.getThumbnail(context.getResult());
						OutputStream out = df.openOutputStream();
						BufferedImage image = javax.imageio.ImageIO.read(new File(context.getThumbnail()));
						javax.imageio.ImageIO.write(ImageTool.getScaledInstance(image, 256, 256, true), "PNG", out);
						out.close();
					} catch (IOException | DataStoreException e) {
						throw new TaskException("Error creating preview in the DataStore", e);
					}
				} else { // Process duplicates
					for(Integer id : duplicates.keySet()) {
						//TODO
					}
				}
				return next();
			}

			@Override
			State next() { return TASK_CLEANUP; }
		},
		TASK_CLEANUP {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}

			@Override
			State next() { return TASK_COMPLETE; }
		},
		TASK_COMPLETE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}

			@Override
			protected void postRun(Task context) {
				super.postRun(context);
				context.lock();
			}

			@Override
			State next() { return this; }
		},
		SIMILAR_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}

			@Override
			protected void postRun(Task context) {
				super.postRun(context);
				context.lock();
			}

			@Override
			State next() { return this; }
		},
		DUPLICATE_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}

			@Override
			protected void postRun(Task context) {
				super.postRun(context);
				context.lock();
			}

			@Override
			State next() { return this; }
		},
		METADATA_SELECT {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}

			@Override
			protected void postRun(Task context) {
				super.postRun(context);
				context.lock();
			}

			@Override
			State next() { return this; }
		},
		ERROR_RAISE {
			@Override
			State run(Task context) {
				LOG.debug("call run(Task={}, State={})", new Object[]{context.getId(), this});
				return next();
			}
			
			@Override
			protected void postRun(Task context) {
				super.postRun(context);
				context.lock();
			}

			@Override
			State next() { return this; }
		};

		protected void preRun(Task context) {
			LOG.debug("call preRun(Task={}, State={})", new Object[]{context.getId(), this});
		}
		
		abstract State run(Task context);
		
		abstract State next();
		
		protected void postRun(Task context) {
			LOG.debug("call postRun(Task={}, State={})", new Object[]{context.getId(), this});
		}
		
		State process(Task context) {
			State state;
			try {
				preRun(context);
				state = run(context);
				postRun(context);
				TaskManager.fireTaskChanged(context);
				return state;
			} catch (TaskException te) {
				LOG.error("Exception while processing Task Id={}", context.getId(), te);
				context.error(te);
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
		state = this.state.process(this);
	}
	
	private static File findImage(String base) {
		return findImage(new File(base));
	}
	
	private static File findImage(File base) {
		File[] files = base.listFiles(new FilenameFilter()
		{
			private String getExtension(String file) {
				if(file.lastIndexOf(".") == -1)
					return "";
				return file.toLowerCase().substring(file.lastIndexOf("."));
			}
			@Override
			public boolean accept(File dir, String fname) {
				File file = new File(dir, fname);
				return !(file.isHidden()) && (file.isDirectory() || getExtension(fname).matches("^.(png|jp(e)?g|gif|bmp|tiff)$"));
			}
		});
		Arrays.sort(files, new Comparator<File>()
		{
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});				
		for(File file : files)
			if(file.isFile())
				return file;
			else
				return findImage(file);
		return null;
	}
	
	private static DataFile findImage(DataFile base) throws DataStoreException {
		DataFile[] files = base.listFiles("^(png|jp(e)?g|gif|bmp|tiff)$");
		Arrays.sort(files, new Comparator<DataFile>()
		{
			@Override
			public int compare(DataFile f1, DataFile f2) {
				try {
					return f1.getName().compareTo(f2.getName());
				} catch (DataStoreException dse) {
					return 0;
				}
			}
		});				
		for(DataFile file : files)
			if(file.isFile())
				return file;
			else
				return findImage(file);
		return null;
	}
	
	private static String format(long bytes)
	{
		int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = ("KMGTPE").charAt(exp-1) + ("i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
