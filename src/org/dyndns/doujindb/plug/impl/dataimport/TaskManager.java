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
import org.dyndns.doujindb.plug.impl.dataimport.Task.Duplicate.Option;
import org.dyndns.doujindb.plug.impl.dataimport.Task.State;
import org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch;
import org.dyndns.doujindb.util.*;

final class TaskManager
{
	private static final Set<MetadataProvider> mProviders = new HashSet<MetadataProvider>();
	private java.util.List<Task> mTaskSet = new Vector<Task>();
	private final File mTaskFile;
	private final File mTmpDir;
	private Worker mWorker = new Worker();
	private PropertyChangeSupport mPCS = new PropertyChangeSupport(mTaskSet);
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(TaskManager.class);
	
	static {
		mProviders.add(new MugiMugiProvider());
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
			Task newTask = new Task(uuid, file.getAbsolutePath());
			newTask.setThumbnail(new File(mTmpDir, newTask.getId() + ".png").getAbsolutePath());
			mTaskSet.add(newTask);
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
				Task nextTask = null;
				for(Task task : tasks()) {
					if(task.getState() == State.DONE)
						continue;
					if(task.hasErrors())
						continue;
					if(task.needInput())
						continue;
					nextTask = task;
					break;
				}
				if(nextTask == null) {
					LOG.info("{} No more task to process", mCurrentTask);
					TaskManager.this.pause();
					continue;
				} else {
					mCurrentTask = nextTask;
				}
				
				LOG.info("{} Processing {}", new Object[]{mCurrentTask, mCurrentTask.getState()});
				try {
					switch(mCurrentTask.getState()) {
					case FIND_COVER:
						doFindCover(mCurrentTask);
						break;
					case CROP_COVER:
						doCropCover(mCurrentTask);
						break;
					case RESIZE_COVER:
						doResizeCover(mCurrentTask);
						break;
					case FIND_DUPLICATE:
						doFindDuplicate(mCurrentTask);
						break;
					case FETCH_METADATA:
						doFetchMetadata(mCurrentTask);
						break;
					case FIND_SIMILAR:
						doFindSimilar(mCurrentTask);
						break;
					case INSERT_DATABASE:
						doInsertDatabase(mCurrentTask);
						break;
					case INSERT_DATASTORE:
						doInsertDatastore(mCurrentTask);
						break;
					case DONE:
						LOG.error("{} Processing requested for DONE task", mCurrentTask);
						// This error was not supposed to happen, pause TaskManager
						TaskManager.this.pause();
						break;
					}
					mPCS.firePropertyChange("task-info", 0, 1);
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
	
	private static void doFindCover(Task task) throws TaskException
	{
		LOG.debug("{} Search for cover image file in {}", task, task.getFile());
		File thumbnail = new File(task.getThumbnail());
		File image = findImage(task.getFile());
		if(image == null) {
			throw new TaskException("Could not locate any image file in " + task.getFile());
		}
		LOG.debug("{} Found image file {}", task, image);
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
			javax.imageio.ImageIO.write(bImage, "PNG", new File(task.getThumbnail()));
		} catch (IOException ioe) {
			throw new TaskException("Could not write image file " + thumbnail, ioe);
		}
		LOG.debug("{} Saved BufferedImage to file {}", task, task.getThumbnail());
		// Decide next step
		if(Configuration.options_autocrop.get()) {
			task.setState(State.CROP_COVER);
			return;
		}
		if(Configuration.options_autoresize.get()) {
			task.setState(State.RESIZE_COVER);
			return;
		}
		if(Configuration.options_checkdupes.get()) {
			task.setState(State.FIND_DUPLICATE);
			return;
		}
		task.setState(State.FETCH_METADATA);
	}
	
	private static void doCropCover(Task task) throws TaskException
	{
		LOG.debug("{} Cropping image file", task);
		File thumbnail = new File(task.getThumbnail());
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
		// Decide next step
		if(Configuration.options_autoresize.get()) {
			task.setState(State.RESIZE_COVER);
			return;
		}
		if(Configuration.options_checkdupes.get()) {
			task.setState(State.FIND_DUPLICATE);
			return;
		}
		task.setState(State.FETCH_METADATA);
	}
	
	private static void doResizeCover(Task task) throws TaskException
	{
		LOG.debug("{} Resizing image file", task);
		File thumbnail = new File(task.getThumbnail());
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
		// Decide next step
		if(Configuration.options_checkdupes.get()) {
			task.setState(State.FIND_DUPLICATE);
			return;
		}
		task.setState(State.FETCH_METADATA);
	}
	
	private static void doFindDuplicate(Task task) throws TaskException
	{
		LOG.debug("{} Checking for duplicate entries", task);
		File thumbnail = new File(task.getThumbnail());
		Integer found = ImageSearch.search(thumbnail);
		if(found != null) {
			Task.Duplicate duplicate = new Task.Duplicate(found);
			try {
				if(DataStore.getStore(found).getFile("@japanese").exists())
					duplicate.annotations.add("missing japanese language");
			} catch (DataStoreException dse) {
				LOG.warn("{} Exception in langCheck", new Object[]{task, dse});
			}
			try {
				long bytesNew = DataStore.diskUsage(new File(task.getFile()));
				long bytesFound = DataStore.diskUsage(DataStore.getStore(found));
				if(bytesNew > bytesFound)
					duplicate.annotations.add("bigger filesize : " + format(bytesNew) + " > " + format(bytesFound) + "");
			} catch (Exception e) {
				LOG.warn("{} Exception in sizeCheck", new Object[]{task, e});
			}
			try {
				long filesNew = DataStore.listFiles(new File(task.getFile())).length;
				long filesFound = DataStore.listFiles(DataStore.getStore(found)).length;
				if(filesNew > filesFound)
					duplicate.annotations.add("more files : " + filesNew + " > " + filesFound + "");
			} catch (Exception e) {
				LOG.warn("{} Exception in countCheck", new Object[]{task, e});
			}
			try {
				BufferedImage imageNew = javax.imageio.ImageIO.read(new FileInputStream(findImage(new File(task.getFile()))));
				String resolutionNew = imageNew.getWidth() + "x" + imageNew.getHeight();
				BufferedImage imageFound = javax.imageio.ImageIO.read(findImage(DataStore.getStore(found)).openInputStream());
				String resolutionFound = imageFound.getWidth() + "x" + imageFound.getHeight();
				if(imageNew.getHeight() > imageFound.getHeight())
					duplicate.annotations.add("higher resolution : " + resolutionNew + " > " + resolutionFound + "");
			} catch (Exception e) {
				LOG.warn("{} Exception in resolutionCheck", new Object[]{task, e});
			}
			task.addDuplicate(duplicate);
		}
		// Decide next step
		if(task.hasDuplicates()) {
			for(Task.Duplicate dup : task.duplicates()) {
				if(dup.dataOption == Option.UNSET || dup.metadataOption == Option.UNSET) {
					// Task needs user input
					task.needInput(true);
					return;
				}
			}
		}
		task.setState(State.FETCH_METADATA);
	}
	
	private static void doFetchMetadata(Task task) throws TaskException
	{
		File thumbnail = new File(task.getThumbnail());
		for(MetadataProvider provider : mProviders) {
			if(!provider.isEnabled()) {
				LOG.debug("{} metadata provider [{}] is disabled and will not be used", new Object[]{task, provider});
				continue;
			}
			LOG.debug("{} Load metadata with provider [{}]", new Object[]{task, provider});
			try {
				Metadata md = provider.query(thumbnail);
				task.addMetadata(md);
				if(md.exception != null) {
					LOG.warn("{} Exception from provider [{}]: {}", new Object[]{task, provider, md.message});
				}
			} catch (Exception e) {
				task.warning(e);
				LOG.warn("{} Exception from provider [{}]", new Object[]{task, provider, e});
			}
		}
		for(Metadata md : task.fetchedMetadata()) {
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
		for(Metadata md : task.fetchedMetadata()) {
			if(md.score > score && md.exception == null) {
				selectedMetadata = md;
				score = md.score;
			}
		}
		task.selectMetadata(selectedMetadata);
		// Decide next step
		if(task.selectedMetadata() == null) {
			// Task needs user input
			task.needInput(true);
			return;
		}
		if(Configuration.options_checksimilar.get()) {
			task.setState(State.FIND_SIMILAR);
			return;
		}
		task.setState(State.INSERT_DATABASE);
	}
	
	private static void doFindSimilar(Task task) throws TaskException
	{
		LOG.debug("{} Checking for duplicate entries", task);
		Set<Integer> duplicates = new HashSet<Integer>();
		QueryBook query;
		Metadata md = task.selectedMetadata();
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
				if(!task.duplicates().contains(book))
					task.addDuplicate(new Task.Duplicate(book));
		}
		// Decide next step
		if(task.hasDuplicates()) {
			for(Task.Duplicate dup : task.duplicates()) {
				if(dup.dataOption == Option.UNSET || dup.metadataOption == Option.UNSET) {
					// Task needs user input
					task.needInput(true);
					return;
				}
			}
		}		
		task.setState(State.INSERT_DATABASE);
	}
	
	private static void doInsertDatabase(Task task) throws TaskException
	{
		Book book;
		Metadata md = task.selectedMetadata();
		try {
			book = DataBase.doInsert(Book.class);
			task.setResult(book.getId());
			book.setJapaneseName(md.name);
			book.setTranslatedName(md.translation);
			book.setDate(new Date(md.timestamp));
			book.setPages(md.pages);
			book.setAdult(md.adult);
			book.setRating(Rating.UNRATED);
			book.setInfo(md.info);
			try { book.setType(Book.Type.valueOf(md.type)); } catch (Exception e) { book.setType(Book.Type.不詳); }
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
		task.setState(State.INSERT_DATASTORE);
	}
	
	private static void doInsertDatastore(Task task) throws TaskException
	{
		File basepath = new File(task.getFile());
		try {
			DataFile store = DataStore.getStore(task.getResult());
			store.mkdirs();
			DataStore.fromFile(basepath, store, true);
		} catch (DataBaseException | IOException | DataStoreException e) {
			throw new TaskException("Error copying '" + basepath + "' in DataStore", e);
		}
		try {
			DataFile df = DataStore.getThumbnail(task.getResult());
			OutputStream out = df.openOutputStream();
			BufferedImage image = javax.imageio.ImageIO.read(new File(task.getThumbnail()));
			javax.imageio.ImageIO.write(ImageTool.getScaledInstance(image, 256, 256, true), "PNG", out);
			out.close();
		} catch (IOException | DataStoreException e) {
			throw new TaskException("Error creating preview in the DataStore", e);
		}
		task.setState(State.DONE);
	}
	
	private static String format(long bytes)
	{
		int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = ("KMGTPE").charAt(exp-1) + ("i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
