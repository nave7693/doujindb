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
import org.dyndns.doujindb.util.*;

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;

final class TaskManager
{
	private static java.util.List<Task> tasks = new Vector<Task>();
	private static Worker worker = new Worker();
	private static PropertyChangeSupport pcs = new PropertyChangeSupport(tasks);
	private static Set<MetadataProvider> providers = new HashSet<MetadataProvider>();
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(TaskManager.class);
	
	static {
		providers.add(new MugiMugiProvider());
	}
	
	public static void save(File file) {
		LOG.debug("call save({})", file);
		FileOutputStream out = null;
		try
		{
			TaskSet set = new TaskSet();
			set.tasks.addAll(tasks);
			out = new FileOutputStream(file);
			JAXBContext context = JAXBContext.newInstance(TaskSet.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
			m.marshal(set, out);
			LOG.debug("Saved Tasks to {}", file);
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			LOG.error("Error saving Tasks to {}", file, e);
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public static void load(File file) {
		LOG.debug("call load({})", file);
		synchronized(tasks) {
			tasks = new Vector<Task>();
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(file);
				JAXBContext context = JAXBContext.newInstance(TaskSet.class);
				Unmarshaller um = context.createUnmarshaller();
				TaskSet set = (TaskSet) um.unmarshal(in);
				tasks.addAll(set.tasks);
				LOG.debug("Loaded Tasks from {}", file);
			} catch (NullPointerException | JAXBException | FileNotFoundException e) {
				LOG.error("Error loading Tasks from {}", file, e);
			} finally {
				try { in.close(); } catch (Exception e) { }
			}
		}
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static int size() {
		return tasks.size();
	}
	
	public static void add(File file) {
		LOG.debug("call add({})", file);
		synchronized(tasks) {
			// Get unique ID
			String uuid = java.util.UUID.randomUUID().toString();
			while(tasks.contains(uuid))
				uuid = java.util.UUID.randomUUID().toString();
			tasks.add(new Task(uuid, file.getAbsolutePath()));
		}
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static void remove(Task task) {
		LOG.debug("call remove({})", task);
		synchronized(tasks) {
			tasks.remove(task);
		}
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static void reset(Task task) {
		LOG.debug("call reset({})", task);
		synchronized(tasks) {
			if(!contains(task))
				return;
			task.reset();
		}
	}
	
	public static boolean contains(Task task) {
		return tasks.contains(task);
	}
	
	public static boolean contains(String taskid) {
		return tasks.contains(taskid);
	}

	public static Iterable<Task> tasks() {
		return tasks;
	}
	
	public static Task get(int index) {
		return tasks.get(index);
	}
	
	public static Task getRunningTask() {
		if(!isRunning())
			return null;
		return worker.m_Task;
	}
	
	public static void registerListener(PropertyChangeListener listener) {
		LOG.debug("call registerListener({})", listener);
		pcs.addPropertyChangeListener(listener);
	}
	
	public static void start() {
		LOG.debug("call start()");
		Thread thread = new Thread(worker);
		thread.setName("plugin-dataimport-taskmanager");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public static void stop() {
		LOG.debug("call stop()");
		//TODO
	}
	
	public static void pause() {
		LOG.debug("call pause()");
		if(!worker.isPaused()) {
			worker.pause();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			LOG.info("Worker paused");
		}
	}
	
	public static void resume() {
		LOG.debug("call resume()");
		if(worker.isPaused()) {
			worker.resume();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			LOG.info("Worker resumed");
		}
	}
	
	public static boolean isRunning() {
		return !worker.isPaused();
	}
	
	private static final class Worker implements Runnable
	{
		private Task m_Task;
		private boolean m_Paused = true;
		private boolean m_PauseReq = true;
		
		private Thread m_PauseThread;
		
		private Worker() {
			m_PauseThread = new Thread("plugin-dataimport-taskmanager-cmdpoller")
			{
				@Override
				public void run() {
					while(true) {
						m_Paused = m_PauseReq;
						try { Thread.sleep(100); } catch (InterruptedException ie) { }
					}
				}
			};
			m_PauseThread.setDaemon(true);
			m_PauseThread.start();
		}
		
		public synchronized void pause() {
			synchronized(this) {
				if(isPaused())
					return;
				m_PauseReq = true;
				while(!isPaused())
					Thread.yield();
			}
		}
		
		public synchronized void resume() {
			synchronized(this) {
				if(!isPaused())
					return;
				m_PauseReq = false;
				while(isPaused())
					Thread.yield();
			}
		}
		
		public boolean isPaused() {
			return m_Paused;
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
					if(task.state == State.NEW) {
						m_Task = task;
						break;
					}
				if(m_Task == null || m_Task.state != State.NEW) {
					TaskManager.pause();
					continue;
				}
				
				LOG.info("{} Process started", m_Task);
				try {
					File image = findFirstFile(m_Task.file);
					LOG.debug("{} Found image file {}", m_Task, image.getAbsolutePath());
					// Crop image
					if(Configuration.options_autocrop.get()) {
						LOG.debug("{} Cropping image file", m_Task);
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
							image = File.createTempFile(m_Task.id + "-crop-", ".png");
							image.deleteOnExit();
							javax.imageio.ImageIO.write(dest, "PNG", image);
						} catch (Exception e) {
							throw new TaskException("Could not write image file " + image.getPath(), e);
						}
					}
					// Find duplicates
					if(Configuration.options_checkdupes.get()) {
						LOG.debug("{} Checking for duplicate entries", m_Task);
						//TODO use ImageSearch plugin
						/*
						task.setExec(Task.Exec.CHECK_DUPLICATE);
						
						File reqFile;
						BufferedImage reqImage;
						Integer searchResult;
						
						reqFile = new File(DataImport.PLUGIN_QUERY, task.getId() + ".png");
						try {
							reqImage = javax.imageio.ImageIO.read(reqFile);
						} catch (IllegalArgumentException | IOException e) {
							throw new TaskErrorException("Could not read image file '" + reqFile.getPath()+ "' : " + e.getMessage());
						}
						if(reqImage == null) {
							throw new TaskErrorException("Cover image not found");
						}
						//FIXME searchResult = CacheManager.search(reqImage);
						searchResult = null;
						if(searchResult != null) {
							Set<Integer> duplicateList = new HashSet<Integer>();
							duplicateList.add(searchResult);
							task.setDuplicateList(duplicateList);
							
							String japanLang = "";
							try {
								for(Integer dupe : duplicateList) {
									if(DataStore.getStore(dupe).getFile("@japanese").exists())
										continue;
									japanLang = " (missing japanese language)";
								}
							} catch (DataStoreException dse) { }
							
							String higherRes = "";
							try {
								long bytesNew = DataStore.diskUsage(new File(task.getPath()));
								long pagesNew = DataStore.listFiles(new File(task.getPath())).length;
								BufferedImage biNew = javax.imageio.ImageIO.read(new FileInputStream(findFile(new File(task.getPath()))));
								String resNew = biNew.getWidth() + "x" + biNew.getHeight();
								for(Integer dupe : duplicateList) {
									long bytesBook = DataStore.diskUsage(DataStore.getStore(dupe));
									long pagesBook = DataStore.listFiles(DataStore.getStore(dupe)).length;
									BufferedImage biBook = javax.imageio.ImageIO.read(findFile(DataStore.getStore(dupe)).openInputStream()); //FIXME throws NPE is store folder is empty but Book is still image-cached
									String resBook = biBook.getWidth() + "x" + biBook.getHeight();
									if(bytesNew > bytesBook)
										higherRes = " (may be higher resolution: [" + bytesToSize(bytesNew) + " - " + pagesNew + "p - " + resNew + "] ~ [" + bytesToSize(bytesBook) + " - " + pagesBook + "p - " + resBook + "])";
								}
							} catch (DataStoreException | IOException e) { }
							
							throw new TaskException("Duplicate book detected" + japanLang + " " + higherRes);
						}
						*/
					}
					// Resize image
					if(Configuration.options_autoresize.get()) {
						LOG.debug("{} Resizing image file", m_Task);
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
							image = File.createTempFile(m_Task.id + "-resize-", ".png");
							image.deleteOnExit();
							javax.imageio.ImageIO.write(dest, "PNG", image);
						} catch (Exception e) {
							throw new TaskException("Could not write image file " + image.getPath(), e);
						}
					}
					for(MetadataProvider provider : providers) {
						LOG.debug("{} Load metadata with provider [{}]", m_Task, provider);
						if(!isPaused()) {
							try {
								Metadata md = provider.query(image);
								m_Task.metadata.add(md);
								if(md.exception != null) {
									LOG.warn("{} Exception from provider [{}]: {}", m_Task, provider, md.message);
									m_Task.state = State.WARNING;
								}
							} catch (Exception e) {
								m_Task.message = e.getMessage();
								m_Task.exception(e);
								LOG.warn("{} Exception from provider [{}]", m_Task, provider, e);
								m_Task.state = State.WARNING;
							}
						}
					}
				} catch (TaskException te) {
					m_Task.message = te.getMessage();
					m_Task.exception(te);
					LOG.error("{} Exception while processing", m_Task, te);
					m_Task.state = State.ERROR;
				} catch (Exception e) {
					m_Task.message = e.getMessage();
					m_Task.exception(e);
					LOG.error("{} Exception while processing", m_Task, e);
					m_Task.state = State.ERROR;
					// This error was not supposed to happen, pause TaskManager
					TaskManager.pause();
				}
				if(m_Task.state == State.NEW)
					m_Task.state = State.COMPLETE;
				LOG.info("{} Process completed with State [{}]", m_Task,  m_Task.state);
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
	
	private static File findFirstFile(String base) {
		return findFirstFile(new File(base));
	}
	
	private static File findFirstFile(File base) {
		File[] files = base.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String fname) {
				return !(new File(dir, fname).isHidden());
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
				return findFirstFile(file);
		return null;
	}
	
	private static DataFile findFirstFile(DataFile base) throws DataStoreException {
		DataFile[] files = base.listFiles();
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
				return findFirstFile(file);
		return null;
	}
}
