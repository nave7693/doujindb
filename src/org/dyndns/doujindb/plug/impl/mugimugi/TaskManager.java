package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.ImageIcon;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.util.ImageTool;

final class TaskManager
{
	private static Vector<Task> m_Tasks;
	private static Worker m_Worker = new Worker();
	
	private static PropertyChangeSupport pcs = new PropertyChangeSupport(m_Worker);
		
	static {
		m_Tasks = new Vector<Task>();
		read();
		
		Thread t = new Thread(m_Worker);
		t.setName(TaskManager.class.getName()+"$Worker");
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	public static void write() {
		File file = new File(DoujinshiDBScanner.PLUGIN_HOME, "tasks.xml");
		FileOutputStream out = null;
		try
		{
			TaskSet set = new TaskSet();
			set.tasks = m_Tasks;
			out = new FileOutputStream(file);
			JAXBContext context = JAXBContext.newInstance(TaskSet.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(set, out);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (JAXBException jaxbe) {
			jaxbe.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public static void read()
	{
		synchronized(m_Tasks)
		{
			m_Tasks = new Vector<Task>();
			
			File file = new File(DoujinshiDBScanner.PLUGIN_HOME, "tasks.xml");
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(file);
				JAXBContext context = JAXBContext.newInstance(TaskSet.class);
				Unmarshaller um = context.createUnmarshaller();
				TaskSet set = (TaskSet) um.unmarshal(in);
				m_Tasks = set.tasks;
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (JAXBException jaxbe) {
				jaxbe.printStackTrace();
			} catch (FileNotFoundException fnfe) {
				;
			} finally {
				try { in.close(); } catch (Exception e) { }
			}
		}
	}
	
	public static int size() {
		return m_Tasks.size();
	}
	
	public static void add(File workpath) {
		synchronized(m_Tasks)
		{
			// Get unique ID
			String id = java.util.UUID.randomUUID().toString();
			while(m_Tasks.contains(id))
				id = java.util.UUID.randomUUID().toString();
			//
			Task task = new TaskImpl(id, workpath);
			m_Tasks.add(task);
		}
	}
	
	public static void remove(Task task) {
		synchronized(m_Tasks)
		{
			m_Tasks.remove(task);
		}
	}
	
	public static void reset(Task task) {
		synchronized(m_Tasks)
		{
			if(!contains(task))
				return;
			task.setBook(null);
			task.setDuplicateList(null);
			task.setMugimugiList(null);
			task.setExec(TaskExec.NO_OPERATION);
			task.setInfo(TaskInfo.IDLE);
			task.setError(null);
			task.setWarning(null);
			task.setSelected(false);
			task.setMugimugiBid(null);
		}
	}
	
	public static boolean contains(Task task) {
		return m_Tasks.contains(task);
	}
	
	public static boolean contains(String taskid) {
		return m_Tasks.contains(taskid);
	}

	@SuppressWarnings("unchecked")
	public static Iterable<Task> tasks() {
		return (Iterable<Task>) m_Tasks.clone();
	}
	
	public static Task get(int index) {
		return m_Tasks.get(index);
	}
	
	public static Task getRunning() {
		if(!isRunning())
			return null;
		return m_Worker.m_Task;
	}
	
	public static void registerListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.mugimugi", name="Task")
	static final class TaskImpl extends Task
	{
		/**
		 * Used by JAXB, do not remove or suffer
		 * IllegalAnnotationsException : TaskImpl does not have a no-arg default constructor.
		 */
		private TaskImpl()
		{
			super(null, null);
		}
		
		private TaskImpl(String id, File path)
		{
			super(id, path.getPath());
		}
		
		@Override
		public void setExec(TaskExec exec) {
			super.setExec(exec);
			pcs.firePropertyChange("task-exec", 0, 1);
		}
		
		@Override
		public void setInfo(TaskInfo info) {
			super.setInfo(info);
			pcs.firePropertyChange("task-info", 0, 1);
		}
		
		@Override
		public int getProgress() {
			switch(this.getExec())
			{
				case NO_OPERATION:
					if(this.getInfo() == TaskInfo.IDLE)
						return 0;
					else if (this.getInfo() == TaskInfo.COMPLETED)
						return 100;
					break;
				case CHECK_API:
					return 10;
				case SCAN_IMAGE:
					return 20;
				case CHECK_DUPLICATE:
					return 30;
				case UPLOAD_IMAGE:
					return 40;
				case PARSE_BID:
					return 50;
				case PARSE_XML:
					return 50;
				case CHECK_SIMILARITY:
					return 60;
				case SAVE_DATABASE:
					return 70;
				case SAVE_DATASTORE:
					return 80;
				case CLEANUP_DATA:
					return 90;
				default:
					break;
			}
			return -1;
		}

		@Override
		public String getMessage() {
			if(super.getInfo().equals(TaskInfo.ERROR))
				return super.getError();
			if(super.getInfo().equals(TaskInfo.WARNING))
				return super.getWarning();
			switch(this.getExec())
			{
				case NO_OPERATION:
					if(this.getInfo() == TaskInfo.IDLE)
						return "queue.wait()";
					else if (this.getInfo() == TaskInfo.COMPLETED)
						return "status:done";
					break;
				case CHECK_API:
					return "api.check(key)";
				case SCAN_IMAGE:
					return "folder.scan_image()";
				case CHECK_DUPLICATE:
					return "cache.find_duplicate(image)";
				case UPLOAD_IMAGE:
					return "http.upload(image)";
				case PARSE_BID:
					return "xml.parse(book_id)";
				case PARSE_XML:
					return "xml.parse(response)";
				case CHECK_SIMILARITY:
					return "database.find_similar(book)";
				case SAVE_DATABASE:
					return "database.insert(book)";
				case SAVE_DATASTORE:
					return "datastore.upload(files)";
				case CLEANUP_DATA:
					return "plugin.cleanup()";
				default:
					break;
			}
			return "status:unknown";
		}

		@Override
		public boolean isRunning() {
			if(!m_Worker.isPaused())
				return this.equals(m_Worker.m_Task);
			return false;
		}
	}
	
	@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.mugimugi", name="TaskSet")
	private static final class TaskSet
	{
		@XmlElements({
		    @XmlElement(name="Task", type=Task.class)
		  })
		private Vector<Task> tasks = new Vector<Task>();
	}
	
	public static void start()
	{
		if(m_Worker.isPaused())
		{
			m_Worker.resume();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			Core.Logger.log("Worker started.", Level.INFO);
		}
	}
	
	public static void stop()
	{
		if(!m_Worker.isPaused())
		{
			m_Worker.pause();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			Core.Logger.log("Worker stopped.", Level.INFO);
		}
	}
	
	public static boolean isRunning()
	{
		return !m_Worker.isPaused();
	}
	
	private static final class Worker implements Runnable
	{
		private Task m_Task;
		private boolean m_Paused = true;
		private boolean m_PauseReq = true;
		
		private Thread m_PauseThread;
		
		private Worker()
		{
			m_PauseThread = new Thread(getClass().getName()+"$PauseCmdPoller")
			{
				@Override
				public void run()
				{
					while(true)
					{
						m_Paused = m_PauseReq;
						try { Thread.sleep(100); } catch (InterruptedException ie) { }
					}
				}
			};
			m_PauseThread.start();
		}
		
		public synchronized void pause()
		{
			synchronized(this)
			{
				if(isPaused())
					return;
				m_PauseReq = true;
				while(!isPaused())
					Thread.yield();
			}
		}
		
		public synchronized void resume()
		{
			synchronized(this)
			{
				if(!isPaused())
					return;
				m_PauseReq = false;
				while(isPaused())
					Thread.yield();
			}
		}
		
		public boolean isPaused()
		{
			return m_Paused;
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				// Don't hog the CPU
				try { Thread.sleep(100); } catch (InterruptedException ie) { }
				
				// Do nothing if paused
				if(isPaused())
					continue;
				
				/*
				 *  Check if we have enough queries
				 *  If not pause the Worker and notify the UI
				 */
				int queryCount = DoujinshiDBScanner.UserInfo.Queries;
				if( queryCount < 2)
				{
					Core.Logger.log("Not enough (" + queryCount + ") API queries to process more Tasks.", Level.WARNING);
					TaskManager.stop();
					pcs.firePropertyChange("api-info", 0, 1);
					continue;
				}
				
				// Get next queued Task
				for(Task task : tasks())
					if(task.getInfo() == TaskInfo.IDLE)
					{
						m_Task = task;
						break;
					}
				if(m_Task == null || m_Task.getInfo() != TaskInfo.IDLE)
				{
					TaskManager.stop();
					continue;
				}
				
				m_Task.setInfo(TaskInfo.RUNNING);
				try {
					if(m_Task.getExec() == TaskExec.NO_OPERATION)
						execApiCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.CHECK_API)
						execImageScan(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.SCAN_IMAGE)
						execDuplicateCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.CHECK_DUPLICATE)
						execImageUpload(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.UPLOAD_IMAGE)
						execXMLParse(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
//					if(m_Task.getExec() == TaskExec.PARSE_XML)
//						execBIDParse(m_Task);
//					if(isPaused())
//					{
//						m_Task.setInfo(TaskInfo.IDLE);
//						continue;
//					}
					
					if(m_Task.getExec() == TaskExec.PARSE_XML)
						execSimilarityCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.CHECK_SIMILARITY)
						execDatabaseInsert(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.SAVE_DATABASE)
						execDatastoreSave(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(TaskInfo.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == TaskExec.SAVE_DATASTORE)
						execCleanup(m_Task);
				} catch (TaskWarningException twe) {
					m_Task.setWarning(twe.getMessage());
					m_Task.setInfo(TaskInfo.WARNING);
					twe.printStackTrace();
					continue;
				} catch (TaskErrorException tee) {
					m_Task.setError(tee.getMessage());
					m_Task.setInfo(TaskInfo.ERROR);
					tee.printStackTrace();
					continue;
				} catch (Exception e) {
					m_Task.setError("[FATAL] " + e.getMessage()); // Overkill, not supposed to happen, but still ...
					m_Task.setInfo(TaskInfo.ERROR);
					e.printStackTrace();
					continue;
				}
				m_Task.setExec(TaskExec.NO_OPERATION);
				m_Task.setInfo(TaskInfo.COMPLETED);
			}
		}
	}
	
	private static boolean execApiCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.CHECK_API);
		
		if(!(DoujinshiDBScanner.APIKEY + "").matches("[0-9a-f]{20}"))
		{
			TaskManager.stop();
			throw new TaskErrorException("Invalid API Key (" + DoujinshiDBScanner.APIKEY + ") provided");
		}
		
		return true;
	}
	
	private static boolean execImageScan(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.SCAN_IMAGE);
		
		File coverFile;
		File reqFile;
		BufferedImage coverImage;
		BufferedImage resizedImage;
		
		coverFile= findFile(task.getPath());
		if(coverFile == null)
		{
			throw new TaskErrorException("Cover Image not found");
		}
		try {
			coverImage = ImageTool.read(coverFile);
		} catch (IllegalArgumentException | IOException e) {
			throw new TaskErrorException("Could not read Image file '" + coverFile.getPath()+ "' : " + e.getMessage());
		}
		if(coverImage == null)
		{
			throw new TaskErrorException("Cover Image not found");
		}
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		{
			BufferedImage dest;
			if(coverImage.getWidth() > coverImage.getHeight())
				dest = new BufferedImage(coverImage.getWidth() / 2, coverImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			else
				dest = new BufferedImage(coverImage.getWidth(), coverImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = dest.getGraphics();
			g.drawImage(coverImage, 0, 0, coverImage.getWidth(), coverImage.getHeight(), null);
			g.dispose();
			if(DoujinshiDBScanner.RESIZE_COVER)
			try
			{
				resizedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
				resizedImage = ImageTool.getScaledInstance(dest, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			} catch (Exception e) {
				throw new TaskErrorException("Could not resize Image file '" + coverFile.getPath()+ "' : " + e.getMessage());
			}else
			{
				resizedImage = dest;
			}
			try {
				ImageTool.write(resizedImage, reqFile);
				pcs.firePropertyChange("task-image", 0, 1);
			} catch (Exception e) {
				throw new TaskErrorException("Could not write Image file '" + coverFile.getPath()+ "' : " + e.getMessage());
			}
		}
		
		return true;
	}
	
	private static boolean execDuplicateCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.CHECK_DUPLICATE);
		
		File reqFile;
		BufferedImage reqImage;
		String searchResult;
		
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		try {
			reqImage = ImageTool.read(reqFile);
		} catch (IllegalArgumentException | IOException e) {
			throw new TaskErrorException("Could not read Image file '" + reqFile.getPath()+ "' : " + e.getMessage());
		}
		if(reqImage == null)
		{
			throw new TaskErrorException("Cover Image not found");
		}
		searchResult = CacheManager.search(reqImage);
		if(searchResult != null)
		{
			Set<String> duplicateList = new HashSet<String>();
			duplicateList.add(searchResult);
			task.setDuplicateList(duplicateList);
			throw new TaskWarningException("Duplicate Book detected");
		}
		
		return true;
	}
	
	private static boolean execImageUpload(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.UPLOAD_IMAGE);
		
		URLConnection urlConnection;
		File reqFile;
		File rspFile;
		
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		try {
			urlConnection = new java.net.URL("http://doujinshi.mugimugi.org/api/" + DoujinshiDBScanner.APIKEY + "/?S=imageSearch").openConnection();
			urlConnection.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
			InputStream rspIn = new ClientHttpRequest(urlConnection).post(
				new Object[] {
					"img", reqFile
				});
			rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
			FileOutputStream rspOut = new FileOutputStream(rspFile);
			copyStream(rspIn, rspOut);
			rspIn.close();
			rspOut.close();
		} catch (MalformedURLException murle) {
			throw new TaskErrorException("Error uploading Image : " + murle.getMessage());
		} catch (IOException ioe) {
			throw new TaskErrorException("Error uploading Image : " + ioe.getMessage());
		}
		
		return true;
	}
	
	private static boolean execXMLParse(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.PARSE_XML);
		
		File rspFile;
		XMLParser.XML_List list;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			list = (XMLParser.XML_List) um.unmarshal(new FileInputStream(rspFile));
			if(list.ERROR != null)
			{
				throw new TaskErrorException("Server returned : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
			}
			DoujinshiDBScanner.UserInfo = (list.USER == null ? DoujinshiDBScanner.UserInfo : list.USER);
			pcs.firePropertyChange("api-info", 0, 1);
			
			double bestResult = 0;
			Set<String> mugimugi_list = new HashSet<String>();
			for(XMLParser.XML_Book book : list.Books)
			{
				mugimugi_list.add(book.ID);
				double result = Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", "."));
				if(result > bestResult)
				{
					bestResult = result;
					task.setMugimugiBid(book.ID);
				}
			}
			task.setMugimugiList(mugimugi_list);
			
			if(task.getThreshold() > bestResult)
				throw new TaskWarningException("No query matched the Threshold (" + DoujinshiDBScanner.THRESHOLD + ")");
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			throw new TaskErrorException("Error parsing XML : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execBIDParse(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.PARSE_BID);
		
		return true;
	}
	
	private static boolean execSimilarityCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.CHECK_SIMILARITY);
		
		Set<Book> books = new HashSet<Book>();
		QueryBook query;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book book = null;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			list = (XMLParser.XML_List) um.unmarshal(new FileInputStream(rspFile));
			
			for(XMLParser.XML_Book _book : list.Books)
			{
				if(_book.ID.equals(task.getMugimugiBid()))
				{
					book = _book;
					break;
				}
			}
			if(book == null)
				throw new TaskErrorException("Error parsing XML : Book '" + task.getMugimugiBid() + "' was not found in Response file '" + task.getId() + ".xml'");
			
			if(!book.NAME_JP.equals(""))
			{
				query = new QueryBook();
				query.JapaneseName = book.NAME_JP;
				for(Book b : Core.Database.getBooks(query))
					books.add(b);
			}
			if(!book.NAME_EN.equals(""))
			{
				query = new QueryBook();
				query.TranslatedName = book.NAME_EN;
				for(Book b : Core.Database.getBooks(query))
					books.add(b);
			}
			if(!book.NAME_R.equals(""))
			{
				query = new QueryBook();
				query.RomajiName = book.NAME_R;
				for(Book b : Core.Database.getBooks(query))
					books.add(b);
			}
			
			if(!books.isEmpty())
			{
				Set<String> duplicateList = new HashSet<String>();
				for(Book _book : books)
					duplicateList.add(_book.getID());
				task.setDuplicateList(duplicateList);
				throw new TaskWarningException("Possible duplicate Book" + (duplicateList.size() > 1 ? "s" : "") + " detected");
			}

		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execDatabaseInsert(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.SAVE_DATABASE);
		
		Book book;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book xmlbook = null;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			list = (XMLParser.XML_List) um.unmarshal(new FileInputStream(rspFile));
			
			for(XMLParser.XML_Book _book : list.Books)
			{
				if(_book.ID.equals(task.getMugimugiBid()))
				{
					xmlbook = _book;
					break;
				}
			}
			if(xmlbook == null)
				throw new TaskErrorException("Error parsing XML : Book '" + task.getMugimugiBid() + "' was not found in Response file '" + task.getId() + ".xml'");
			
			book = DoujinshiDBScanner.Context.doInsert(Book.class);
			book.setJapaneseName(xmlbook.NAME_JP);
			book.setTranslatedName(xmlbook.NAME_EN);
			book.setRomajiName(xmlbook.NAME_R);
			book.setDate(xmlbook.DATE_RELEASED);
			book.setPages(xmlbook.DATA_PAGES);
			book.setAdult(xmlbook.DATA_AGE == 1);
			book.setDecensored(false);
			book.setTranslated(false);
			book.setColored(false);
			book.setRating(Rating.UNRATED);
			book.setInfo(xmlbook.DATA_INFO.length() > 255 ? xmlbook.DATA_INFO.substring(0, 255) : xmlbook.DATA_INFO);
			
			RecordSet<Artist> artists = DoujinshiDBScanner.Context.getArtists(null);
			RecordSet<Circle> circles = DoujinshiDBScanner.Context.getCircles(null);
			RecordSet<Parody> parodies = DoujinshiDBScanner.Context.getParodies(null);
			RecordSet<Content> contents = DoujinshiDBScanner.Context.getContents(null);
			RecordSet<Convention> conventions = DoujinshiDBScanner.Context.getConventions(null);
			
			Map<String, Artist> alink = new HashMap<String, Artist>();
			Map<String, Circle> clink = new HashMap<String, Circle>();
			
			for(XMLParser.XML_Item xmlitem : xmlbook.LINKS.Items)
			{
				try
				{
					switch(xmlitem.TYPE)
					{
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
									alink.put(xmlitem.ID, artist);
									break _case;
								}
							Artist a = DoujinshiDBScanner.Context.doInsert(Artist.class);
							a.setJapaneseName(xmlitem.NAME_JP);
							a.setTranslatedName(xmlitem.NAME_EN);
							a.setRomajiName(xmlitem.NAME_R);
							book.addArtist(a);
							alink.put(xmlitem.ID, a);
						}
						break;
					case character:
						break;
					case circle:
						/**
						 * Ok, we cannot link book <--> circle directly.
						 * We have to link book <--> artist <--> circle instead.
						 */
						_case:{
							for(Circle circle : circles)
								if((circle.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
										(circle.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
										(circle.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
								{
									// book.addCircle(circle);
									clink.put(xmlitem.ID, circle);
									break _case;
								}
							Circle c = DoujinshiDBScanner.Context.doInsert(Circle.class);
							c.setJapaneseName(xmlitem.NAME_JP);
							c.setTranslatedName(xmlitem.NAME_EN);
							c.setRomajiName(xmlitem.NAME_R);
							// book.addCircle(c);
							clink.put(xmlitem.ID, c);
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
							Content cn = DoujinshiDBScanner.Context.doInsert(Content.class);
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
							Convention cv = DoujinshiDBScanner.Context.doInsert(Convention.class);
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
						Parody p = DoujinshiDBScanner.Context.doInsert(Parody.class);
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
			
			DoujinshiDBScanner.Context.doCommit();
			
			if(alink.size() > 0 && clink.size() > 0)
			{
				String[] ckeys = (String[]) clink.keySet().toArray(new String[0]);
				String[] akeys = (String[]) alink.keySet().toArray(new String[0]);
				String ids = ckeys[0];
				for(int i=1;i<ckeys.length;i++)
					ids += ckeys[i] + ",";
				URLConnection urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + DoujinshiDBScanner.APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
				urlc.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
				InputStream in0 = urlc.getInputStream();
				DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
				docfactory.setNamespaceAware(true);
				DocumentBuilder builder = docfactory.newDocumentBuilder();
				Document doc = builder.parse(in0);
				XPathFactory xmlfactory = XPathFactory.newInstance();
				XPath xpath = xmlfactory.newXPath();
				for(String cid : ckeys)
				{
					for(String aid : akeys)
					{
						XPathExpression expr = xpath.compile("//ITEM[@ID='" + cid + "']/LINKS/ITEM[@ID='" + aid + "']");
						Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
						if(node == null)
							continue;
						else
							clink.get(cid).addArtist(alink.get(aid));
					}
				}
			}
			
			DoujinshiDBScanner.Context.doCommit();
			
			task.setBook(book.getID());
			
		} catch (NullPointerException |
				JAXBException |
				IOException |
				ParserConfigurationException |
				XPathExpressionException |
				SAXException e) {
			throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execDatastoreSave(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.SAVE_DATASTORE);
		
		File basepath;
		File reqFile;
		
		basepath = new File(task.getPath());
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		
		for(File file : basepath.listFiles())
			try {
				copyFile(file, Core.Repository.child(task.getBook()));
			} catch (DataBaseException | IOException e) {
				throw new TaskErrorException("Error copying file '" + file.getPath() + "' in the DataStore : " + e.getMessage());
			}
		try
		{
			DataFile df = Core.Repository.child(task.getBook());
			df.mkdir();
			df = Core.Repository.getPreview(task.getBook());
			df.touch();
			OutputStream out = df.getOutputStream();
			BufferedImage image = ImageTool.read(reqFile);
			ImageTool.write(ImageTool.getScaledInstance(image, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true), out);
			out.close();
		} catch (IOException e) {
			throw new TaskErrorException("Error creating preview in the DataStore : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execCleanup(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(TaskExec.CLEANUP_DATA);
		
		String id = task.getBook();
		try
		{
			CacheManager.put(id, (BufferedImage) new ImageIcon(
				ImageTool.read(
					Core.Repository.getPreview(id).getInputStream())).getImage());
		} catch (IOException | ClassCastException e) {
			throw new TaskErrorException("Error adding Book to Cache : " + e.getMessage());
		}
		
		return true;
	}
	
	private static File findFile(String base)
	{
		return findFile(new File(base));
	}
	
	private static File findFile(File base)
	{
		File[] files = base.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String fname)
			{
				return !(new File(dir, fname).isHidden());
			}
		});
		Arrays.sort(files, new Comparator<File>()
		{
			@Override
			public int compare(File f1, File f2)
			{
				return f1.getName().compareTo(f2.getName());
			}
		});				
		for(File file : files)
			if(file.isFile())
				return file;
			else
				return findFile(file);
		return null;
	}
	
	private static void copyFile(File file, DataFile df) throws IOException
	{
		DataFile dfChild = df.child(file.getName());
		if(file.isDirectory())
		{
			dfChild.mkdirs();
			for(File f : file.listFiles())
				copyFile(f, dfChild);
		}else
		{
			dfChild.getParent().mkdirs();
			dfChild.touch();
			OutputStream out = dfChild.getOutputStream();
			InputStream in = new FileInputStream(file);
			copyStream(in, out);
			in.close();
			out.close();
		}
	}
	
	private static void copyStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buff = new byte[0x800];
		int read;
		while((read = in.read(buff)) != -1)
		{
			out.write(buff, 0, read);
		}
	}
}