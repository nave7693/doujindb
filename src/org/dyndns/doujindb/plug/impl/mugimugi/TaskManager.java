package org.dyndns.doujindb.plug.impl.mugimugi;

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

import org.w3c.dom.*;
import org.xml.sax.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.impl.imagesearch.CacheManager;
import org.dyndns.doujindb.util.*;

final class TaskManager
{
	private static Vector<Task> tasks = new Vector<Task>();
	private static Worker worker = new Worker();
	
	private static ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();
	
	private static PropertyChangeSupport pcs = new PropertyChangeSupport(tasks);
	
	private static final String TAG = "DoujinshiDBScanner.TaskManager : ";
	
	static {
		loadTasks();
		
		Thread thread;
		
		thread = new Thread(worker);
		thread.setName("plugin/doujinshidb-scanner/taskmanager-worker");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		
		thread = new Thread() {
			@Override
			public void run() {
				long bookid = 0;
				while(true) {
					try {
						Thread.sleep(1);
						if(queue.isEmpty())
							continue;
						bookid = queue.peek();
						fetch(bookid);
						queue.poll();
					} catch (IOException ioe) {
						Logger.logWarning(TAG + "failed to download image of bookid '" + bookid + "'");
					} catch (InterruptedException ie) { }
				}
			}
			
			private void fetch(long bookId) throws IOException {
				File file = new File(DoujinshiDBScanner.PLUGIN_IMAGECACHE, "B" + bookId + ".jpg");
				if(file.exists())
					return;
				URL thumbURL = new URL(DoujinshiDBScanner.DOUJINSHIDB_IMGURL + "tn/" + (int)Math.floor((double)bookId/(double)2000) + "/" + bookId + ".jpg");
				Image i = new ImageIcon(thumbURL).getImage();
				BufferedImage bi = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2 = bi.createGraphics();
				g2.drawImage(i, 0, 0, null);
				g2.dispose();
				ImageIO.write(bi, "JPG", file);
			}
		};
		thread.setName("plugin/doujinshidb-scanner/taskmanager-downloader");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public static synchronized void fetchImage(long bookId)
	{
		queue.offer(bookId);
	}
	
	public static void saveTasks() {
		File file = new File(DoujinshiDBScanner.PLUGIN_HOME, "tasks.xml");
		FileOutputStream out = null;
		try
		{
			TaskSet set = new TaskSet();
			set.tasks = tasks;
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
	
	public static void loadTasks()
	{
		synchronized(tasks)
		{
			tasks = new Vector<Task>();
			
			File file = new File(DoujinshiDBScanner.PLUGIN_HOME, "tasks.xml");
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(file);
				JAXBContext context = JAXBContext.newInstance(TaskSet.class);
				Unmarshaller um = context.createUnmarshaller();
				TaskSet set = (TaskSet) um.unmarshal(in);
				tasks = set.tasks;
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
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static int size() {
		return tasks.size();
	}
	
	public static void add(File workpath) {
		synchronized(tasks)
		{
			// Get unique ID
			String uuid = java.util.UUID.randomUUID().toString();
			while(tasks.contains(uuid))
				uuid = java.util.UUID.randomUUID().toString();
			//
			Task task = new TaskImpl(uuid, workpath);
			tasks.add(task);
		}
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static void remove(Task task) {
		synchronized(tasks)
		{
			tasks.remove(task);
		}
		pcs.firePropertyChange("taskmanager-info", 0, 1);
	}
	
	public static void reset(Task task) {
		synchronized(tasks)
		{
			if(!contains(task))
				return;
			task.setBook(null);
			task.setDuplicateList(null);
			task.setMugimugiList(null);
			task.setExec(Task.Exec.NO_OPERATION);
			task.setInfo(Task.Info.IDLE);
			task.setError(null);
			task.setWarning(null);
			task.setSelected(false);
			task.setMugimugiBid(null);
		}
	}
	
	public static boolean contains(Task task) {
		return tasks.contains(task);
	}
	
	public static boolean contains(String taskid) {
		return tasks.contains(taskid);
	}

	@SuppressWarnings("unchecked")
	public static Iterable<Task> tasks() {
		return (Iterable<Task>) tasks.clone();
	}
	
	public static Task get(int index) {
		return tasks.get(index);
	}
	
	public static Task getRunning() {
		if(!isRunning())
			return null;
		return worker.m_Task;
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
		public void setExec(Task.Exec exec) {
			super.setExec(exec);
			pcs.firePropertyChange("task-exec", 0, 1);
		}
		
		@Override
		public void setInfo(Task.Info info) {
			super.setInfo(info);
			pcs.firePropertyChange("task-info", 0, 1);
		}
		
		@Override
		public int getProgress() {
			switch(this.getExec())
			{
				case NO_OPERATION:
					if(this.getInfo() == Task.Info.IDLE)
						return 0;
					else if (this.getInfo() == Task.Info.COMPLETED)
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
			if(super.getInfo().equals(Task.Info.ERROR))
				return super.getError();
			if(super.getInfo().equals(Task.Info.WARNING))
				return super.getWarning();
			switch(this.getExec())
			{
				case NO_OPERATION:
					if(this.getInfo() == Task.Info.IDLE)
						return "queue.wait()";
					else if (this.getInfo() == Task.Info.COMPLETED)
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
			if(!worker.isPaused())
				return this.equals(worker.m_Task);
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
		if(worker.isPaused())
		{
			worker.resume();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			Logger.logInfo("Worker started");
		}
	}
	
	public static void stop()
	{
		if(!worker.isPaused())
		{
			worker.pause();
			pcs.firePropertyChange("taskmanager-info", 0, 1);
			Logger.logInfo("Worker stopped");
		}
	}
	
	public static boolean isRunning()
	{
		return !worker.isPaused();
	}
	
	private static final class Worker implements Runnable
	{
		private Task m_Task;
		private boolean m_Paused = true;
		private boolean m_PauseReq = true;
		
		private Thread m_PauseThread;
		
		private Worker()
		{
			m_PauseThread = new Thread("plugin/doujinshidb-scanner/taskmanager-cmdpoller")
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
			m_PauseThread.setDaemon(true);
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
					Logger.logWarning("Not enough (" + queryCount + ") API queries to process more Tasks");
					TaskManager.stop();
					pcs.firePropertyChange("api-info", 0, 1);
					continue;
				}
				
				// Get next queued Task
				for(Task task : tasks())
					if(task.getInfo() == Task.Info.IDLE)
					{
						m_Task = task;
						break;
					}
				if(m_Task == null || m_Task.getInfo() != Task.Info.IDLE)
				{
					TaskManager.stop();
					continue;
				}
				
				m_Task.setInfo(Task.Info.RUNNING);
				try {
					if(m_Task.getExec() == Task.Exec.NO_OPERATION)
						execApiCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.CHECK_API)
						execImageScan(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.SCAN_IMAGE)
						execDuplicateCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.CHECK_DUPLICATE)
						execImageUpload(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.UPLOAD_IMAGE)
						execXMLParse(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
//					if(m_Task.getExec() == Task.Exec.PARSE_XML)
//						execBIDParse(m_Task);
//					if(isPaused())
//					{
//						m_Task.setInfo(Task.Info.IDLE);
//						continue;
//					}
					
					if(m_Task.getExec() == Task.Exec.PARSE_XML)
						execSimilarityCheck(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.CHECK_SIMILARITY)
						execDatabaseInsert(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.SAVE_DATABASE)
						execDatastoreSave(m_Task);
					if(isPaused())
					{
						m_Task.setInfo(Task.Info.IDLE);
						continue;
					}
					
					if(m_Task.getExec() == Task.Exec.SAVE_DATASTORE)
						execCleanup(m_Task);
				} catch (TaskWarningException twe) {
					m_Task.setWarning(twe.getMessage());
					m_Task.setInfo(Task.Info.WARNING);
					twe.printStackTrace();
					continue;
				} catch (TaskErrorException tee) {
					m_Task.setError(tee.getMessage());
					m_Task.setInfo(Task.Info.ERROR);
					tee.printStackTrace();
					continue;
				} catch (Exception e) {
					m_Task.setError("[FATAL] " + e.getMessage()); // Overkill, not supposed to happen, but still ...
					m_Task.setInfo(Task.Info.ERROR);
					e.printStackTrace();
					continue;
				}
				m_Task.setExec(Task.Exec.NO_OPERATION);
				m_Task.setInfo(Task.Info.COMPLETED);
			}
		}
	}
	
	private static String bytesToSize(long bytes)
	{
		int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = ("KMGTPE").charAt(exp-1) + ("i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	private static boolean execApiCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.CHECK_API);
		
		if(!(DoujinshiDBScanner.APIKEY + "").matches("[0-9a-f]{20}"))
		{
			TaskManager.stop();
			throw new TaskErrorException("Invalid API Key (" + DoujinshiDBScanner.APIKEY + ") provided");
		}
		
		return true;
	}
	
	private static boolean execImageScan(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.SCAN_IMAGE);
		
		File coverFile;
		File reqFile;
		BufferedImage coverImage;
		BufferedImage resizedImage;
		
		coverFile = findFile(task.getPath());
		if(coverFile == null) {
			throw new TaskErrorException("Cover image not found");
		}
		try {
			coverImage = ImageTool.read(coverFile);
		} catch (IllegalArgumentException | IOException e) {
			throw new TaskErrorException("Could not read image file '" + coverFile.getPath()+ "' : " + e.getMessage());
		}
		if(coverImage == null) {
			throw new TaskErrorException("Cover image not found");
		}
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		{
			BufferedImage dest;
			int img_width = coverImage.getWidth(),
				img_height = coverImage.getHeight();
			if(img_width > img_height)
				dest = new BufferedImage(img_width / 2, img_height, BufferedImage.TYPE_INT_RGB);
			else
				dest = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);
			Graphics g = dest.getGraphics();
			g.drawImage(coverImage, 0, 0, img_width, img_height, null);
			g.dispose();
			if(DoujinshiDBScanner.RESIZE_COVER)
			try
			{
				resizedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
				resizedImage = ImageTool.getScaledInstance(dest, 256, 256, true);
			} catch (Exception e) {
				throw new TaskErrorException("Could not resize image file '" + coverFile.getPath()+ "' : " + e.getMessage());
			} else {
				resizedImage = dest;
			}
			try {
				ImageTool.write(resizedImage, reqFile);
				pcs.firePropertyChange("task-image", 0, 1);
			} catch (Exception e) {
				throw new TaskErrorException("Could not write image file '" + coverFile.getPath()+ "' : " + e.getMessage());
			}
		}
		
		return true;
	}
	
	private static boolean execDuplicateCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.CHECK_DUPLICATE);
		
		File reqFile;
		BufferedImage reqImage;
		String searchResult;
		
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		try {
			reqImage = ImageTool.read(reqFile);
		} catch (IllegalArgumentException | IOException e) {
			throw new TaskErrorException("Could not read image file '" + reqFile.getPath()+ "' : " + e.getMessage());
		}
		if(reqImage == null) {
			throw new TaskErrorException("Cover image not found");
		}
		searchResult = CacheManager.search(reqImage);
		if(searchResult != null) {
			Set<String> duplicateList = new HashSet<String>();
			duplicateList.add(searchResult);
			task.setDuplicateList(duplicateList);
			
			String japanLang = "";
			try {
				for(String dupe : duplicateList) {
					if(DataStore.getFile(dupe).getFile("@japanese").exists())
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
				for(String dupe : duplicateList) {
					long bytesBook = DataStore.diskUsage(DataStore.getFile(dupe));
					long pagesBook = DataStore.listFiles(DataStore.getFile(dupe)).length;
					BufferedImage biBook = javax.imageio.ImageIO.read(findFile(DataStore.getFile(dupe)).getInputStream());
					String resBook = biBook.getWidth() + "x" + biBook.getHeight();
					if(bytesNew > bytesBook)
						higherRes = " (may be higher resolution: [" + bytesToSize(bytesNew) + " - " + pagesNew + "p - " + resNew + "] ~ [" + bytesToSize(bytesBook) + " - " + pagesBook + "p - " + resBook + "])";
				}
			} catch (DataStoreException | IOException e) { }
			
			throw new TaskWarningException("Duplicate book detected" + japanLang + " " + higherRes);
		}
		
		return true;
	}
	
	private static boolean execImageUpload(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.UPLOAD_IMAGE);
		
		URLConnection urlConnection;
		File reqFile;
		File rspFile;
		
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		try {
			urlConnection = new java.net.URL(DoujinshiDBScanner.DOUJINSHIDB_APIURL + DoujinshiDBScanner.APIKEY + "/?S=imageSearch").openConnection();
			urlConnection.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
			InputStream rspIn = new ClientHttpRequest(urlConnection).post(
				new Object[] {
					"img", reqFile
				});
			rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
			Files.copy(rspIn, rspFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			rspIn.close();
		} catch (MalformedURLException murle) {
			throw new TaskErrorException("Error uploading image : " + murle.getMessage());
		} catch (IOException ioe) {
			throw new TaskErrorException("Error uploading image : " + ioe.getMessage());
		}
		
		return true;
	}
	
	private static boolean execXMLParse(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.PARSE_XML);
		
		File rspFile;
		XMLParser.XML_List list;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
		try {
			list = XMLParser.readList(new FileInputStream(rspFile));
			if(list.ERROR != null) {
				throw new TaskErrorException("Server returned : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
			}
			DoujinshiDBScanner.UserInfo = (list.USER == null ? DoujinshiDBScanner.UserInfo : list.USER);
			pcs.firePropertyChange("api-info", 0, 1);
			
			double bestResult = 0;
			Set<String> mugimugi_list = new HashSet<String>();
			for(XMLParser.XML_Book book : list.Books) {
				mugimugi_list.add(book.ID);
				fetchImage(Long.valueOf(book.ID.substring(1)));
				double result = Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", "."));
				if(result > bestResult) {
					bestResult = result;
					task.setMugimugiBid(book.ID);
				}
			}
			task.setMugimugiList(mugimugi_list);
			
			if(task.getThreshold() > bestResult)
				throw new TaskWarningException("No query matched the threshold (" + DoujinshiDBScanner.THRESHOLD + ")");
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			throw new TaskErrorException("Error parsing XML : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execSimilarityCheck(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.CHECK_SIMILARITY);
		
		Set<Book> books = new HashSet<Book>();
		QueryBook query;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book book = null;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
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
					urlc = new java.net.URL(DoujinshiDBScanner.DOUJINSHIDB_APIURL + DoujinshiDBScanner.APIKEY + "/?S=getID&ID=" + task.getMugimugiBid() + "").openConnection();
					urlc.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
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
				Set<String> duplicateList = new HashSet<String>();
				for(Book _book : books)
					duplicateList.add(_book.getID());
				task.setDuplicateList(duplicateList);
				throw new TaskWarningException("Possible duplicate book" + (duplicateList.size() > 1 ? "s" : "") + " detected");
			}
		} catch (NullPointerException | JAXBException | FileNotFoundException e) {
			throw new TaskErrorException(task.getExec() + " : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execDatabaseInsert(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.SAVE_DATABASE);
		
		Book book;
		File rspFile;
		XMLParser.XML_List list;
		XMLParser.XML_Book xmlbook = null;
		
		rspFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".xml");
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
					urlc = new java.net.URL(DoujinshiDBScanner.DOUJINSHIDB_APIURL + DoujinshiDBScanner.APIKEY + "/?S=getID&ID=" + task.getMugimugiBid() + "").openConnection();
					urlc.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
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
			book.setDecensored(false);
			book.setTranslated(false);
			book.setColored(false);
			book.setRating(Rating.UNRATED);
			book.setInfo(xmlbook.DATA_INFO.length() > 255 ? xmlbook.DATA_INFO.substring(0, 255) : xmlbook.DATA_INFO);
			
			RecordSet<Artist> artists = DataBase.getArtists(null);
			RecordSet<Circle> circles = DataBase.getCircles(null);
			RecordSet<Parody> parodies = DataBase.getParodies(null);
			RecordSet<Content> contents = DataBase.getContents(null);
			RecordSet<Convention> conventions = DataBase.getConventions(null);
			
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
							/**
							 * save Artist reference
							 * so we can link it with the appropriate Circle
							 */
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
							/**
							 * save Artist reference
							 * so we can link it with the appropriate Circle
							 */
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
				URLConnection urlc = new java.net.URL(DoujinshiDBScanner.DOUJINSHIDB_APIURL + DoujinshiDBScanner.APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
				urlc.setRequestProperty("User-Agent", DoujinshiDBScanner.USER_AGENT);
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
		task.setExec(Task.Exec.SAVE_DATASTORE);
		
		File basepath;
		File reqFile;
		
		basepath = new File(task.getPath());
		reqFile = new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png");
		
		try {
			DataStore.fromFile(basepath, DataStore.getFile(task.getBook()), true);
		} catch (DataBaseException | IOException | DataStoreException e) {
			throw new TaskErrorException("Error copying '" + basepath + "' in  DataStore : " + e.getMessage());
		}
		try {
			DataFile df = DataStore.getThumbnail(task.getBook());
			OutputStream out = df.getOutputStream();
			BufferedImage image = ImageTool.read(reqFile);
			ImageTool.write(ImageTool.getScaledInstance(image, 256, 256, true), out);
			out.close();
		} catch (IOException | DataStoreException e) {
			throw new TaskErrorException("Error creating preview in the DataStore : " + e.getMessage());
		}
		
		return true;
	}
	
	private static boolean execCleanup(Task task) throws TaskWarningException, TaskErrorException
	{
		task.setExec(Task.Exec.CLEANUP_DATA);
		
		String id = task.getBook();
		try {
			CacheManager.put(id, (BufferedImage) new ImageIcon(
				ImageTool.read(
						DataStore.getThumbnail(id).getInputStream())).getImage());
		} catch (IOException | ClassCastException | DataStoreException e) {
			throw new TaskErrorException("Error adding book to cache : " + e.getMessage());
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
	
	private static DataFile findFile(DataFile base) throws DataStoreException
	{
		DataFile[] files = base.listFiles();
		Arrays.sort(files, new Comparator<DataFile>()
		{
			@Override
			public int compare(DataFile f1, DataFile f2)
			{
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
				return findFile(file);
		return null;
	}
}
