package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.db.record.Book.*;
import org.dyndns.doujindb.plug.impl.dataimport.Task.State;
import org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch;
import org.dyndns.doujindb.util.*;

final class TaskManager
{
	private Set<MetadataProvider> mProviders = new HashSet<MetadataProvider>();
	private java.util.List<Task> mTaskSet = new Vector<Task>();
	private final File mTaskFile;
	private final File mTmpDir;
	private Worker mWorker = new Worker();
	private PropertyChangeSupport mPCS = new PropertyChangeSupport(mTaskSet);
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(TaskManager.class);
	
	{
		if(Configuration.provider_mugimugi_enable.get())
			mProviders.add(new MugiMugiProvider());
		if(Configuration.provider_ehentai_enable.get())
			mProviders.add(new EHentaiProvider());
	}
	
	TaskManager(final File homeDir) {
		mTaskFile = new File(homeDir, "tasks.xml");
		mTmpDir = new File(homeDir, "tmp");
		mTmpDir.mkdirs();
	}
	
	public void save() {
		LOG.debug("call save()");
		FileOutputStream out = null;
		try
		{
			TaskSet set = new TaskSet();
			set.tasks.addAll(mTaskSet);
			out = new FileOutputStream(mTaskFile);
			JAXBContext context = JAXBContext.newInstance(TaskSet.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
			m.marshal(set, out);
			LOG.debug("Tasks saved to {}", mTaskFile);
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			LOG.error("Error saving tasks to {}", mTaskFile, e);
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public void save(Task task, File file) {
		LOG.debug("call save({}, {})", task, file);
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			JAXBContext context = JAXBContext.newInstance(Task.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
			m.marshal(task, out);
			LOG.debug("Task {} saved to {}", task, file);
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			LOG.error("Error saving task {} to {}", new Object[]{task, file, e});
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public void load() {
		LOG.debug("call load()");
		synchronized(mTaskSet) {
			mTaskSet = new Vector<Task>();
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(mTaskFile);
				JAXBContext context = JAXBContext.newInstance(TaskSet.class);
				Unmarshaller um = context.createUnmarshaller();
				TaskSet set = (TaskSet) um.unmarshal(in);
				mTaskSet.addAll(set.tasks);
				LOG.debug("Tasks loaded from {}", mTaskFile);
			} catch (NullPointerException | JAXBException | FileNotFoundException e) {
				LOG.error("Error loading tasks from {}", mTaskFile, e);
			} finally {
				try { in.close(); } catch (Exception e) { }
			}
		}
		mPCS.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public int size() {
		return mTaskSet.size();
	}
	
	public void add(File file) {
		LOG.debug("call add({})", file);
		synchronized(mTaskSet) {
			// Get unique ID
			String uuid = java.util.UUID.randomUUID().toString();
			while(mTaskSet.contains(uuid))
				uuid = java.util.UUID.randomUUID().toString();
			mTaskSet.add(new Task(uuid, file.getAbsolutePath()));
		}
		mPCS.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public void remove(Task task) {
		LOG.debug("call remove({})", task);
		synchronized(mTaskSet) {
			mTaskSet.remove(task);
		}
		mPCS.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public void reset(Task task) {
		LOG.debug("call reset({})", task);
		synchronized(mTaskSet) {
			if(!contains(task))
				return;
			task.reset();
		}
	}
	
	public boolean contains(Task task) {
		return mTaskSet.contains(task);
	}
	
	public boolean contains(String taskid) {
		return mTaskSet.contains(taskid);
	}

	public Iterable<Task> tasks() {
		return mTaskSet;
	}
	
	public Task get(int index) {
		return mTaskSet.get(index);
	}
	
	public Task getRunningTask() {
		if(!isRunning())
			return null;
		return mWorker.mCurrentTask;
	}
	
	public void registerListener(PropertyChangeListener listener) {
		LOG.debug("call registerListener({})", listener);
		mPCS.addPropertyChangeListener(listener);
	}
	
	public void start() {
		LOG.debug("call start()");
		Thread thread = new Thread(mWorker);
		thread.setName("plugin-dataimport-taskmanager");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public void stop() {
		LOG.debug("call stop()");
		//TODO
	}
	
	public void pause() {
		LOG.debug("call pause()");
		if(!mWorker.isPaused()) {
			mWorker.pause();
			mPCS.firePropertyChange("taskmanager-info", 0, 1);
			LOG.info("Worker paused");
		}
	}
	
	public void resume() {
		LOG.debug("call resume()");
		if(mWorker.isPaused()) {
			mWorker.resume();
			mPCS.firePropertyChange("taskmanager-info", 0, 1);
			LOG.info("Worker resumed");
		}
	}
	
	public boolean isRunning() {
		return !mWorker.isPaused();
	}
	
	private final class Worker implements Runnable
	{
		private Task mCurrentTask;
		private boolean mIsPaused = true;
		private boolean mPauseRequested = true;
		
		private Thread mCmdpollerThread;
		
		private Worker() {
			mCmdpollerThread = new Thread("plugin-dataimport-taskmanager-cmdpoller")
			{
				@Override
				public void run() {
					while(true) {
						mIsPaused = mPauseRequested;
						try { Thread.sleep(100); } catch (InterruptedException ie) { }
					}
				}
			};
			mCmdpollerThread.setDaemon(true);
			mCmdpollerThread.start();
		}
		
		public synchronized void pause() {
			synchronized(this) {
				if(isPaused())
					return;
				mPauseRequested = true;
				while(!isPaused())
					Thread.yield();
			}
		}
		
		public synchronized void resume() {
			synchronized(this) {
				if(!isPaused())
					return;
				mPauseRequested = false;
				while(isPaused())
					Thread.yield();
			}
		}
		
		public boolean isPaused() {
			return mIsPaused;
		}
		
		@Override
		public void run() {
			while(true)
			{
				// Don't hog the CPU
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					LOG.warn("Worker interrupted", ie);
				}
				
				// Do nothing if paused
				if(isPaused())
					continue;
				
				// Get next queued Task
				for(Task task : tasks())
					if(task.getState() == State.NEW) {
						mCurrentTask = task;
						break;
					}
				if(mCurrentTask == null || mCurrentTask.getState() != State.NEW) {
					TaskManager.this.pause();
					continue;
				}
				
				LOG.info("{} Process started", mCurrentTask);
				try {
					// Find cover image
					mCurrentTask.setState(State.FIND_COVER);
					mPCS.firePropertyChange("task-info", 0, 1);
					File image = findImage(mCurrentTask.getFile());
					if(image == null) {
						throw new TaskException("Could not locate any image file in " + mCurrentTask.getFile());
					}
					LOG.debug("{} Found image file {}", mCurrentTask, image.getAbsolutePath());
					// Crop image
					mCurrentTask.setState(State.CROP_COVER);
					mPCS.firePropertyChange("task-info", 0, 1);
					if(Configuration.options_autocrop.get()) {
						LOG.debug("{} Cropping image file", mCurrentTask);
						BufferedImage src = javax.imageio.ImageIO.read(image);
						if(src == null)
							throw new TaskException("Error loading image from file" + image.getPath());
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
							image = File.createTempFile(mCurrentTask.getId() + "-crop-", ".png", mTmpDir);
							image.deleteOnExit();
							javax.imageio.ImageIO.write(dest, "PNG", image);
						} catch (Exception e) {
							throw new TaskException("Could not write image file " + image.getPath(), e);
						}
					}
					// Resize image
					mCurrentTask.setState(State.RESIZE_COVER);
					mPCS.firePropertyChange("task-info", 0, 1);
					if(Configuration.options_autoresize.get()) {
						LOG.debug("{} Resizing image file", mCurrentTask);
						BufferedImage src = javax.imageio.ImageIO.read(image);
						if(src == null)
							throw new TaskException("Error loading image from file" + image.getPath());
						BufferedImage dest;
						try
						{
							dest = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
							dest = ImageTool.getScaledInstance(src, 256, 256, true);
						} catch (Exception e) {
							throw new TaskException("Could not resize image file " + image.getPath(), e);
						}
						try {
							image = File.createTempFile(mCurrentTask.getId() + "-resize-", ".png", mTmpDir);
							image.deleteOnExit();
							javax.imageio.ImageIO.write(dest, "PNG", image);
						} catch (Exception e) {
							throw new TaskException("Could not write image file " + image.getPath(), e);
						}
					}
					// Save "final" image before uploading
					// Used for retrieval in TaskManager.getImage(Task)
					{
						File saved = new File(mTmpDir, mCurrentTask.getId() + ".png");
						mCurrentTask.setThumbnail(saved.getAbsolutePath());
						javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(image), "PNG", saved);
					}
					// Find duplicates
					mCurrentTask.setState(State.FIND_DUPLICATE);
					mPCS.firePropertyChange("task-info", 0, 1);
					if(Configuration.options_checkdupes.get()) {
						LOG.debug("{} Checking for duplicate entries", mCurrentTask);
						Integer found = ImageSearch.search(image);
						if(found != null) {
							mCurrentTask.addDuplicate(found);
							String langCheck = "";
							if(DataStore.getStore(found).getFile("@japanese").exists())
								langCheck = " (missing japanese language)";
							String sizeCheck = "";
							try {
								long bytesNew = DataStore.diskUsage(new File(mCurrentTask.getFile()));
								long bytesFound = DataStore.diskUsage(DataStore.getStore(found));
								if(bytesNew > bytesFound)
									sizeCheck = " (bigger filesize " + format(bytesNew) + " > " + format(bytesFound) + ")";
							} catch (Exception e) {
								LOG.warn("{} Exception in sizeCheck", new Object[]{mCurrentTask, e});
							}
							String countCheck = "";
							try {
								long filesNew = DataStore.listFiles(new File(mCurrentTask.getFile())).length;
								long filesFound = DataStore.listFiles(DataStore.getStore(found)).length;
								if(filesNew > filesFound)
									countCheck = " (more files " + filesNew + " > " + filesFound + ")";
							} catch (Exception e) {
								LOG.warn("{} Exception in countCheck", new Object[]{mCurrentTask, e});
							}
							String resolutionCheck = "";
							try {
								BufferedImage imageNew = javax.imageio.ImageIO.read(new FileInputStream(findImage(new File(mCurrentTask.getFile()))));
								String resolutionNew = imageNew.getWidth() + "x" + imageNew.getHeight();
								BufferedImage imageFound = javax.imageio.ImageIO.read(findImage(DataStore.getStore(found)).openInputStream());
								String resolutionFound = imageFound.getWidth() + "x" + imageFound.getHeight();
								if(imageNew.getHeight() > imageFound.getHeight())
									resolutionCheck = " (higher resolution " + resolutionNew + " > " + resolutionFound + ")";
							} catch (Exception e) {
								LOG.warn("{} Exception in resolutionCheck", new Object[]{mCurrentTask, e});
							}
							throw new TaskException(String.format("Duplicate book detected with Id %d %s %s %s %s", found, langCheck, sizeCheck, countCheck, resolutionCheck));
						}
					}
					// Run Metadata providers
					mCurrentTask.setState(State.FETCH_METADATA);
					mPCS.firePropertyChange("task-info", 0, 1);
					for(MetadataProvider provider : mProviders) {
						LOG.debug("{} Load metadata with provider [{}]", new Object[]{mCurrentTask, provider});
						if(!isPaused()) {
							try {
								Metadata md = provider.query(image);
								mCurrentTask.addMetadata(md);
								if(md.exception != null) {
									LOG.warn("{} Exception from provider [{}]: {}", new Object[]{mCurrentTask, provider, md.message});
								}
							} catch (Exception e) {
								mCurrentTask.warning(e);
								LOG.warn("{} Exception from provider [{}]", new Object[]{mCurrentTask, provider, e});
							}
						}
					}
					// Find possible duplicates, this time based on Metadata info, not cover image
					mCurrentTask.setState(State.FIND_SIMILAR);
					mPCS.firePropertyChange("task-info", 0, 1);
					if(Configuration.options_checksimilar.get()) {
						LOG.debug("{} Checking for duplicate entries", mCurrentTask);
						Set<Integer> duplicates = new HashSet<Integer>();
						QueryBook query;
						//TODO Find possible duplicate based on Metadata info
						if(!duplicates.isEmpty()) {
							for(Integer book : duplicates)
								if(mCurrentTask.duplicates().get(book) == null ||
									!mCurrentTask.duplicates().containsKey(book))
									mCurrentTask.addDuplicate(book);
							throw new TaskException("Possible duplicate book" + (duplicates.size() > 1 ? "s" : "") + " detected");
						}
					}
					// Task is complete
					mCurrentTask.setState(State.DONE);
					mPCS.firePropertyChange("task-info", 0, 1);
					LOG.info("{} Process completed with State [{}]", mCurrentTask,  mCurrentTask.getState());
				} catch (TaskException te) {
					mCurrentTask.error(te);
					LOG.error("{} Exception while processing", mCurrentTask, te);
				} catch (Exception e) {
					mCurrentTask.error(e);
					LOG.error("{} Exception while processing", mCurrentTask, e);
					// This error was not supposed to happen, pause TaskManager
					TaskManager.this.pause();
				}
			}
		}
	}
	
	private static String format(long bytes)
	{
		int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = ("KMGTPE").charAt(exp-1) + ("i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	private static boolean execSimilarityCheck(Task task) throws TaskException, TaskException
	{
		/*
		task.setExec(Task.Exec.CHECK_SIMILARITY);
		
		Set<Book> books = new HashSet<Book>();
		QueryBook query;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book book = null;
		
		rspFile = new File(DataImport.PLUGIN_QUERY, task.getId() + ".xml");
		try {
			list = XMLParser.readList(new FileInputStream(rspFile));
			for(XMLParser.XML_Book _book : list.Books) {
				if(_book.ID.equals(task.getMugimugiBid())) {
					book = _book;
					break;
				}
			}
			if(book == null) {
				URLConnection urlc;
				try {
					urlc = new java.net.URL(DataImport.DOUJINSHIDB_APIURL + DataImport.APIKEY + "/?S=getID&ID=B" + task.getMugimugiBid() + "").openConnection();
					urlc.setRequestProperty("User-Agent", DataImport.USER_AGENT);
					book = XMLParser.readList(urlc.getInputStream()).Books.get(0);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if(book == null)
				throw new TaskErrorException("Error parsing XML : Book '" + task.getMugimugiBid() + "' was not found in Response file '" + task.getId() + ".xml'");
			
			if(!book.NAME_JP.equals("")) {
				query = new QueryBook();
				query.JapaneseName = book.NAME_JP;
				for(Book b : DataBase.getBooks(query))
					books.add(b);
			}
			if(!book.NAME_EN.equals("")) {
				query = new QueryBook();
				query.TranslatedName = book.NAME_EN;
				for(Book b : DataBase.getBooks(query))
					books.add(b);
			}
			if(!book.NAME_R.equals("")) {
				query = new QueryBook();
				query.RomajiName = book.NAME_R;
				for(Book b : DataBase.getBooks(query))
					books.add(b);
			}
			if(!books.isEmpty()) {
				Set<Integer> duplicateList = new HashSet<Integer>();
				for(Book _book : books)
					duplicateList.add(_book.getId());
				task.setDuplicateList(duplicateList);
				throw new TaskException("Possible duplicate book" + (duplicateList.size() > 1 ? "s" : "") + " detected");
			}
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
		}
		*/
		return true;
	}
	
	private static boolean execDatabaseInsert(Task task) throws TaskException, TaskException
	{
		/*
		task.setExec(Task.Exec.SAVE_DATABASE);
		
		Book book;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book xmlbook = null;
		
		rspFile = new File(DataImport.PLUGIN_QUERY, task.getId() + ".xml");
		try {
			list = XMLParser.readList(new FileInputStream(rspFile));
			for(XMLParser.XML_Book _book : list.Books) {
				if(_book.ID.equals(task.getMugimugiBid())) {
					xmlbook = _book;
					break;
				}
			}
			if(xmlbook == null) {
				URLConnection urlc;
				try {
					urlc = new java.net.URL(DataImport.DOUJINSHIDB_APIURL + DataImport.APIKEY + "/?S=getID&ID=B" + task.getMugimugiBid() + "").openConnection();
					urlc.setRequestProperty("User-Agent", DataImport.USER_AGENT);
					xmlbook = XMLParser.readList(urlc.getInputStream()).Books.get(0);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if(xmlbook == null)
				throw new TaskErrorException("Error parsing XML : Book '" + task.getMugimugiBid() + "' was not found in Response file '" + task.getId() + ".xml'");
			
			book = DataBase.doInsert(Book.class);
			book.setJapaneseName(xmlbook.NAME_JP);
			book.setTranslatedName(xmlbook.NAME_EN);
			book.setRomajiName(xmlbook.NAME_R);
			book.setDate(xmlbook.DATE_RELEASED);
			book.setPages(xmlbook.DATA_PAGES);
			book.setAdult(xmlbook.DATA_AGE == 1);
			book.setRating(Rating.UNRATED);
			book.setInfo(xmlbook.DATA_INFO.length() > 255 ? xmlbook.DATA_INFO.substring(0, 255) : xmlbook.DATA_INFO);
			
			RecordSet<Artist> artists = DataBase.getArtists(new QueryArtist());
			RecordSet<Circle> circles = DataBase.getCircles(new QueryCircle());
			RecordSet<Parody> parodies = DataBase.getParodies(new QueryParody());
			RecordSet<Content> contents = DataBase.getContents(new QueryContent());
			RecordSet<Convention> conventions = DataBase.getConventions(new QueryConvention());
			
			Map<String, Artist> artists_added = new HashMap<String, Artist>();
			Map<String, Circle> circles_added = new HashMap<String, Circle>();
			
			for(XMLParser.XML_Item xmlitem : xmlbook.LINKS.Items) {
				try {
					switch(xmlitem.TYPE) {
					case type:
						for(Book.Type type : Book.Type.values())
							if(type.toString().equals(xmlitem.NAME_JP))
								book.setType(type);
						break;
					case author:
						_case:{
							for(Artist artist : artists)
								if((artist.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
									(artist.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
									(artist.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
								{
									book.addArtist(artist);
									artists_added.put(xmlitem.ID, artist);
									break _case;
								}
							Artist a = DataBase.doInsert(Artist.class);
							a.setJapaneseName(xmlitem.NAME_JP);
							a.setTranslatedName(xmlitem.NAME_EN);
							a.setRomajiName(xmlitem.NAME_R);
							book.addArtist(a);
							// save Artist reference so we can link it with the appropriate Circle
							artists_added.put(xmlitem.ID, a);
						}
						break;
					case character:
						break;
					case circle:
						_case:{
							for(Circle circle : circles)
								if((circle.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
										(circle.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
										(circle.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
								{
									book.addCircle(circle);
									circles_added.put(xmlitem.ID, circle);
									break _case;
								}
							Circle c = DataBase.doInsert(Circle.class);
							c.setJapaneseName(xmlitem.NAME_JP);
							c.setTranslatedName(xmlitem.NAME_EN);
							c.setRomajiName(xmlitem.NAME_R);
							book.addCircle(c);
							// save Artist reference so we can link it with the appropriate Circle
							circles_added.put(xmlitem.ID, c);
						}
						break;
					case collections:
						break;
					case contents:
						_case:{
							for(Content content : contents)
								if((content.getTagName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
										content.getTagName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals("")) ||
										content.getTagName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals("")) ||
										content.getAliases().contains(xmlitem.NAME_JP) ||
										content.getAliases().contains(xmlitem.NAME_EN) ||
										content.getAliases().contains(xmlitem.NAME_R))
								{
									book.addContent(content);
									break _case;
								}
							Content cn = DataBase.doInsert(Content.class);
							// Tag Name priority NAME_JP > NAME_EN > NAME_R
							cn.setTagName(xmlitem.NAME_JP.equals("")?xmlitem.NAME_EN.equals("")?xmlitem.NAME_R:xmlitem.NAME_EN:xmlitem.NAME_JP);
							book.addContent(cn);
						}
						break;
					case convention:
						if(book.getConvention() != null)
							break;
						_case:{
							for(Convention convention : conventions)
								if((convention.getTagName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
										convention.getTagName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals("")) ||
										convention.getTagName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals("")) ||
										convention.getAliases().contains(xmlitem.NAME_JP) ||
										convention.getAliases().contains(xmlitem.NAME_EN) ||
										convention.getAliases().contains(xmlitem.NAME_R))
								{
									book.setConvention(convention);
									break _case;
								}
							Convention cv = DataBase.doInsert(Convention.class);
							// Tag Name priority NAME_EN > NAME_JP > NAME_R
							cv.setTagName(xmlitem.NAME_EN.equals("")?xmlitem.NAME_JP.equals("")?xmlitem.NAME_R:xmlitem.NAME_JP:xmlitem.NAME_EN);
							book.setConvention(cv);
						}
						break;
					case genre:
						break;
					case imprint:
						break;
					case parody:
						_case:{
							for(Parody parody : parodies)
								if((parody.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
										(parody.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
										(parody.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
								{
									book.addParody(parody);
									break _case;
								}
							Parody p = DataBase.doInsert(Parody.class);
							p.setJapaneseName(xmlitem.NAME_JP);
							p.setTranslatedName(xmlitem.NAME_EN);
							p.setRomajiName(xmlitem.NAME_R);
							book.addParody(p);
						}
						break;
					case publisher:
						break;
					}
				} catch (NullPointerException e) {
					throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
				}
			}
			
			DataBase.doCommit();
			
			if(artists_added.size() > 0 && circles_added.size() > 0) {
				String[] ckeys = (String[]) circles_added.keySet().toArray(new String[0]);
				String[] akeys = (String[]) artists_added.keySet().toArray(new String[0]);
				String ids = ckeys[0];
				for(int i=1;i<ckeys.length;i++)
					ids += ckeys[i] + ",";
				URLConnection urlc = new java.net.URL(DataImport.DOUJINSHIDB_APIURL + DataImport.APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
				urlc.setRequestProperty("User-Agent", DataImport.USER_AGENT);
				InputStream in0 = urlc.getInputStream();
				DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
				docfactory.setNamespaceAware(true);
				DocumentBuilder builder = docfactory.newDocumentBuilder();
				Document doc = builder.parse(in0);
				XPathFactory xmlfactory = XPathFactory.newInstance();
				XPath xpath = xmlfactory.newXPath();
				for(String cid : ckeys) {
					for(String aid : akeys) {
						XPathExpression expr = xpath.compile("//ITEM[@ID='" + cid + "']/LINKS/ITEM[@ID='" + aid + "']");
						Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
						if(node != null)
							circles_added.get(cid).addArtist(artists_added.get(aid));
					}
				}
			}
			
			DataBase.doCommit();
			
			task.setBook(book.getId());
			
		} catch (NullPointerException |
				JAXBException |
				IOException |
				ParserConfigurationException |
				XPathExpressionException |
				SAXException e) {
			throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
		}
		*/
		return true;
	}
	
	private static boolean execDatastoreSave(Task task) throws TaskException, TaskException
	{
		/*
		task.setExec(Task.Exec.SAVE_DATASTORE);
		
		File basepath;
		File reqFile;
		
		basepath = new File(task.getPath());
		reqFile = new File(DataImport.PLUGIN_QUERY, task.getId() + ".png");
		
		try {
			DataStore.fromFile(basepath, DataStore.getStore(task.getBook()), true);
		} catch (DataBaseException | IOException | DataStoreException e) {
			throw new TaskErrorException("Error copying '" + basepath + "' in  DataStore : " + e.getMessage());
		}
		try {
			DataFile df = DataStore.getThumbnail(task.getBook());
			OutputStream out = df.openOutputStream();
			BufferedImage image = javax.imageio.ImageIO.read(reqFile);
			javax.imageio.ImageIO.write(ImageTool.getScaledInstance(image, 256, 256, true), "PNG", out);
			out.close();
		} catch (IOException | DataStoreException e) {
			throw new TaskErrorException("Error creating preview in the DataStore : " + e.getMessage());
		}
		*/
		return true;
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
}
