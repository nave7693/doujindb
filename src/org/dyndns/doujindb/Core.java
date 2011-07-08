package org.dyndns.doujindb;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.Properties;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.rc.*;
import org.dyndns.doujindb.util.*;


/**  
* Core.java - DoujinDB core.
* @author  nozomu
* @version 1.0
*/
public final class Core implements Runnable
{
	public static Logger Logger;
	public static Properties Settings;
	public static Resources Resources;
	public static UI UI;
	public static DataStore Datastore;
	public static Utils Utils = new Utils();

	@Override
	public void run()
	{
		/**  
		* Init the console logger
		* @see ImplLoggerConsole
		*/
		Logger = new ImplLoggerConsole();
		Logger.log(new LogEvent("System logger loaded.", LogLevel.MESSAGE));
		/**  
		* Load system folders
		*/
		new File(System.getProperty("user.home"), ".doujindb").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/lib").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/rc").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/log").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/plug").mkdir();
		//new File(System.getProperty("user.home"), ".doujindb/dat").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/log").mkdir();
		/**  
		* Init the file logger
		* @see ImplLoggerFile
		*/
		try
		{
			Logger FileLogger = new ImplLoggerFile(new File(System.getProperty("user.home"), ".doujindb/log/doujindb.log"));
			Logger.loggerAttach(FileLogger);
		} catch (FileNotFoundException fnfe)
		{
			Logger.log(new LogEvent("Cannot load file logger.", LogLevel.ERROR));
		}
		
		/**  
		* Load system settings
		* @see Properties
		*/
		try
		{
			File src = new File(new File(System.getProperty("user.home"), ".doujindb"), "doujindb.properties");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(src));
			Settings = (Properties)in.readObject();
			Logger.log(new LogEvent("System settings loaded.", LogLevel.MESSAGE));
		} catch (Exception e)
		{
			Logger.log(new LogEvent("Failed to load system settings : "+e.getMessage() + ".", LogLevel.ERROR));
			Settings = new ImplProperties();
			Logger.log(new LogEvent("System settings restored to default.", LogLevel.MESSAGE));
		}
		//TODO
		/**  
		* Load Apache Derby database engine
		
		System.setProperty("derby.system.home", System.getProperty("user.home") + "/.doujindb");
		System.setProperty("derby.stream.error.file", System.getProperty("user.home") + "/.doujindb/log/derby.log");
		try
		{
			ImplDriver db = new ImplDriver();
			db.install();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		/**  
		* Check if the system is headless
		* DoujinDB can't run on headless systems
		*/
		if(java.awt.GraphicsEnvironment.isHeadless()) 
		{
			Logger.log(new LogEvent("DoujinDB cannot run on headless systems.", LogLevel.ERROR));
			return;
		}
		Logger.log(new LogEvent("Loading user interface ...", LogLevel.MESSAGE));
		/**  
		* Load system resources
		* @see Resources
		*/
		try
		{
			Resources = new Resources();
			Resources.Font = (Font)Settings.getValue("org.dyndns.doujindb.ui.font");
		} catch (Exception e)
		{
			Core.Logger.log(new LogEvent(e.getMessage(), LogLevel.ERROR));
			return;
		}
		Core.Logger.log(new LogEvent("System resources loaded.", LogLevel.MESSAGE));
		/**  
		* Load datastore
		* @see DataStore
		*/
		Datastore = new ImplDataStore();
		if(Core.Settings.getValue("org.dyndns.doujindb.dat.datastore").equals(Core.Settings.getValue("org.dyndns.doujindb.dat.temp")))
			Core.Logger.log(new LogEvent("DataStore folder is the temporary system folder.", LogLevel.WARNING));
		Core.Logger.log(new LogEvent("DataStore loaded.", LogLevel.MESSAGE));
		/**  
		* Load UI
		* @see UI
		*/
		String title = "DoujinDB v" + Core.class.getPackage().getSpecificationVersion();
		UI = new UI(title);
		Core.Logger.log(new LogEvent("User interface loaded.", LogLevel.MESSAGE));
		try
		{
			Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_RELOAD, null));
		} catch (Exception e) {
			Core.Logger.log(new LogEvent(e.getMessage(), LogLevel.ERROR));
		}
	}
	
	/**  
	* ImplLoggerConsole - Print every log event on the standard output.
	* @author yoshika
	* @version 1.0
	*/
	private static final class ImplLoggerConsole implements Logger
	{
		private Vector<Logger> loggers = new Vector<Logger>();
		private OutputStream stream = System.out;
		
		@Override
		public void log(LogEvent event)
		{
			try
			{
				String level_string = "";
				switch(event.getLevel())
				{
					case MESSAGE:
						level_string = "Message";
						break;
					case WARNING:
						level_string = "Warning";
						break;
					case ERROR:
						level_string = "Error";
						break;
				}
				stream.write(
						String.format("[0x%08x:%s] %s # %s\r\n",
								event.getTime(),
								level_string,
								event.getSource(),
								event.getMessage()
							).getBytes());
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			for(Logger logger : loggers)
				logger.log(event);
		}

		@Override
		public synchronized void loggerAttach(Logger logger)
		{
			if(!loggers.contains(logger))
				loggers.add(logger);
		}

		@Override
		public synchronized void loggerDetach(Logger logger)
		{
			loggers.remove(logger);			
		}
	}
	
	/**  
	* ImplLoggerFile - Write every log event on a file.
	* @author yoshika
	* @version 1.0
	*/
	private static final class ImplLoggerFile implements Logger
	{
		private Vector<Logger> loggers = new Vector<Logger>();
		private RandomAccessFile writer;
		private LinkedList<LogEvent> buffer = new LinkedList<LogEvent>();
		private final int MAX_LOG_BUFFER = 0x80;
		
		public ImplLoggerFile(File out) throws FileNotFoundException
		{
			writer = new RandomAccessFile(out, "rw");
			try
			{
				writer.seek(writer.length());
				writer.writeBytes("----------------------------\r\n");
				writer.writeBytes(" DoujinDB session started.\r\n");
				writer.writeBytes(" " + new Date() + "\r\n");
				writer.writeBytes("----------------------------\r\n");
			} catch (IOException ioe) { }
			new Thread()
			{
				@Override
				public void run()
				{
					super.setPriority(Thread.MIN_PRIORITY);
					while(true)
					{
						if(!buffer.isEmpty())
						{
							LogEvent event = buffer.peek();
							String level_string = "";
							switch(event.getLevel())
							{
								case MESSAGE:
									level_string = "    ";
									break;
								case WARNING:
									level_string = "<W> ";
									break;
								case ERROR:
									level_string = "<E> ";
									break;
							}
							try
							{
								writer.writeBytes(
										String.format("%s%s\r\n",
												level_string,
												event.getMessage()
											));
								buffer.poll();
								writer.setLength(writer.length());
							} catch (IOException ioe) {
								Logger.log(new LogEvent(ioe.getMessage(), LogLevel.ERROR));
							}
						} else
							try { sleep(100); } catch (InterruptedException e) { }
					}
				}
			}.start();
		}
		
		@Override
		public void log(LogEvent event)
		{
			if(buffer.size() > MAX_LOG_BUFFER)
				Logger.log(new LogEvent("File logger exceeded max number of cached entries.", LogLevel.ERROR));
			else
				buffer.offer(event);
			for(Logger logger : loggers)
				logger.log(event);
		}

		@Override
		public synchronized void loggerAttach(Logger logger)
		{
			if(!loggers.contains(logger))
				loggers.add(logger);
		}

		@Override
		public synchronized void loggerDetach(Logger logger)
		{
			loggers.remove(logger);			
		}
	}
	
	/**  
	* ImplConfiguration - Store all the configurations of DoujinDB
	* @author yoshika
	* @version 1.0
	*/
	private static final class ImplProperties implements Properties, Serializable
	{
		private static final long serialVersionUID = 0xDEADBEEFFEEDL;
		private HashMap<String, Serializable> values;
		private HashMap<String, String> descriptions;
		
		public ImplProperties()
		{
			values = new HashMap<String, Serializable>();
			descriptions = new HashMap<String, String>();
			values.put("org.dyndns.doujindb.ui.font", new Font("Lucida Sans", Font.PLAIN, 12));
			descriptions.put("org.dyndns.doujindb.ui.font", "<html><body>Default JCK font.<br/>Used to render Japanese/Chinese/Korean strings.</body></html>");
			values.put("org.dyndns.doujindb.ui.font_size", 11);
			descriptions.put("org.dyndns.doujindb.ui.font_size", "<html><body>Default font size.</body></html>");
			values.put("org.dyndns.doujindb.ui.delay_threads", 20);
			descriptions.put("org.dyndns.doujindb.ui.delay_threads", "<html><body>Delay used in multi-threader operation.<br/>Value is in milliseconds.</body></html>");
			values.put("org.dyndns.doujindb.ui.always_on_top", false);
			descriptions.put("org.dyndns.doujindb.ui.always_on_top", "<html><body>Whether the user interface should be always painted on top of other windows.</body></html>");
			values.put("org.dyndns.doujindb.ui.tray_on_exit", false);
			descriptions.put("org.dyndns.doujindb.ui.tray_on_exit", "<html><body>Whether the user interface should be minimized on tray when is closed.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.color", new Color(0xAA, 0xAA, 0xAA));
			descriptions.put("org.dyndns.doujindb.ui.theme.color", "<html><body>Foreground windows color.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.background", new Color(0x22, 0x22, 0x22));
			descriptions.put("org.dyndns.doujindb.ui.theme.background", "<html><body>Background windows color.</body></html>");
			values.put("org.dyndns.doujindb.dat.datastore", new File(System.getProperty("java.io.tmpdir")));
			descriptions.put("org.dyndns.doujindb.dat.datastore", "<html><body>The folder in which are stored all the media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.file_extension", ".douz");
			descriptions.put("org.dyndns.doujindb.dat.file_extension", "<html><body>Default file extension given to files when exporting media archives.</body></html>");
			values.put("org.dyndns.doujindb.dat.temp", new File(System.getProperty("java.io.tmpdir")));
			descriptions.put("org.dyndns.doujindb.dat.temp", "<html><body>Temporary folder used to store session media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.save_on_exit", false);
			descriptions.put("org.dyndns.doujindb.dat.save_on_exit", "<html><body>Whether the database should be saved on exit.</body></html>");
			//values.put("org.dyndns.doujindb.dat.export_filename", false);
			//descriptions.put("org.dyndns.doujindb.dat.export_filename", "<html><body>The name given to exported files.</body></html>");
			values.put("org.dyndns.doujindb.net.autocheck_updates", false);
			descriptions.put("org.dyndns.doujindb.net.autocheck_updates", "<html><body>Whether to check if program updates are available.</body></html>");
			values.put("org.dyndns.doujindb.net.listen_port", 8899);
			descriptions.put("org.dyndns.doujindb.net.listen_port", "<html><body>Network port used to accept incoming connections.</body></html>");
			values.put("org.dyndns.doujindb.net.connect_on_start", false);
			descriptions.put("org.dyndns.doujindb.net.connect_on_start", "<html><body>Whether to connect on program startup.</body></html>");
		}
		
		public Serializable getValue(String key) throws PropertyException
		{
			if(!values.containsKey(key))
				throw new PropertyException("Invalid key '" + key + "'");
			return values.get(key);
		}
		
		public void setValue(String key, Serializable value) throws PropertyException
		{
			if(!values.containsKey(key))
				throw new PropertyException("Invalid key '" + key + "'");
			values.put(key, value);
		}
		
		public void newValue(String key, Serializable value) throws PropertyException
		{
			if(values.containsKey(key))
				throw new PropertyException("Key '" + key + "' is already present");
			values.put(key, value);
			descriptions.put(key, "");
		}
		
		public Iterable<String> values()
		{
			return values.keySet();
		}
		
		public boolean containsValue(String key)
		{
			return values.containsKey(key);
		}
		
		public Serializable getDescription(String key) throws PropertyException
		{
			if(!descriptions.containsKey(key))
				throw new PropertyException("Invalid key '" + key + "'");
			return descriptions.get(key);
		}
		
		public void setDescription(String key, String value) throws PropertyException
		{
			if(!descriptions.containsKey(key))
				throw new PropertyException("Invalid key '" + key + "'");
			descriptions.put(key, value);
		}
	}
	
	/**  
	* ImplDatastore - DoujinDB datastore
	* @author yoshika
	* @version 1.0
	*/
	@SuppressWarnings("serial")
	private static final class ImplDataStore implements DataStore, Serializable
	{
		private File Root = (File)Core.Settings.getValue("org.dyndns.doujindb.dat.datastore");
		//TODO
		/*private Hashtable<String, Set<DouzRecord>> readDoujin(InputStream src)
		{
			Doujin doujin;
			Hashtable<String, Set<DouzRecord>> parsed = new Hashtable<String, Set<DouzRecord>>();
			parsed.put("Artist://", new HashSet<DouzRecord>());
			parsed.put("Book://", new HashSet<DouzRecord>());
			parsed.put("Circle://", new HashSet<DouzRecord>());
			parsed.put("Convention://", new HashSet<DouzRecord>());
			parsed.put("Content://", new HashSet<DouzRecord>());
			parsed.put("Parody://", new HashSet<DouzRecord>());
			Serializer serializer = new Persister();
			try
			{
				doujin = serializer.read(Doujin.class, src);
			} catch (Exception e) {
				Core.Logger.log(new LogEvent("Error parsing XML file (" + e.getMessage() + ").", LogLevel.WARNING));
				return null;
			}
			Book book = Database.newBook();
			book.setJapaneseName(doujin.JapaneseName);
			book.setType(doujin.Type);
			book.setTranslatedName(doujin.TranslatedName);
			book.setRomanjiName(doujin.RomanjiName);
			book.setDate(doujin.Released);
			book.setType(doujin.Type);
			book.setPages(doujin.Pages);
			book.setAdult(doujin.Adult);
			book.setDecensored(doujin.Decensored);
			book.setTranslated(doujin.Translated);
			book.setColored(doujin.Colored);
			book.setRating(doujin.Rating);
			book.setInfo(doujin.Info);
			parsed.get("Book://").add(book);
			{
				Vector<DouzRecord> temp = new Vector<DouzRecord>();
				for(Convention convention : Database.getConventions())
					if(doujin.Convention.matches(convention.getTagName()))
						temp.add(convention);
				if(temp.size() == 0 && !doujin.Convention.equals(""))
				{
					Convention convention = Database.newConvention();
					convention.setTagName(doujin.Convention);
					parsed.get("Convention://").add(convention);
					Database.getUnchecked().insert(convention);
				}			
				else
					parsed.get("Convention://").addAll(temp);
			}
			{
				for(String japaneseName : doujin.artists)
				{
					Vector<DouzRecord> temp = new Vector<DouzRecord>();
					for(Artist artist : Database.getArtists())
						if(japaneseName.matches(artist.getJapaneseName()))
							temp.add(artist);
					if(temp.size() == 0)
					{
						Artist artist = Database.newArtist();
						artist.setJapaneseName(japaneseName);
						parsed.get("Artist://").add(artist);
						Database.getUnchecked().insert(artist);
					}			
					else
						parsed.get("Artist://").addAll(temp);
				}
			}
			{
				for(String japaneseName : doujin.circles)
				{
					Vector<DouzRecord> temp = new Vector<DouzRecord>();
					for(Circle circle : Database.getCircles())
						if(japaneseName.matches(circle.getJapaneseName()))
							temp.add(circle);
					if(temp.size() == 0)
					{
						Circle circle = Database.newCircle();
						circle.setJapaneseName(japaneseName);
						parsed.get("Circle://").add(circle);
						Database.getUnchecked().insert(circle);
					}			
					else
						parsed.get("Circle://").addAll(temp);
				}
			}
			{
				for(String tagName : doujin.contents)
				{
					Vector<DouzRecord> temp = new Vector<DouzRecord>();
					for(Content content : Database.getContents())
						if(tagName.matches(content.getTagName()))
							temp.add(content);
					if(temp.size() == 0)
					{
						Content content = Database.newContent();
						content.setTagName(tagName);
						parsed.get("Content://").add(content);
						Database.getUnchecked().insert(content);
					}			
					else
						parsed.get("Content://").addAll(temp);
				}
			}
			{
				for(String japaneseName : doujin.parodies)
				{
					Vector<DouzRecord> temp = new Vector<DouzRecord>();
					for(Parody parody : Database.getParodies())
						if(japaneseName.matches(parody.getJapaneseName()))
							temp.add(parody);
					if(temp.size() == 0)
					{
						Parody parody = Database.newParody();
						parody.setJapaneseName(japaneseName);
						parsed.get("Parody://").add(parody);
						Database.getUnchecked().insert(parody);
					}			
					else
						parsed.get("Parody://").addAll(temp);
				}
			}
			return parsed;
		}
		
		private void writeDoujin(Book book, OutputStream dest)
		{
			Doujin doujin = new Doujin();
			doujin.JapaneseName = book.getJapaneseName();
			doujin.TranslatedName = book.getTranslatedName();
			doujin.RomanjiName = book.getRomanjiName();
			doujin.Convention = book.getConvention() == null ? "" : book.getConvention().getTagName();
			doujin.Released = book.getDate();
			doujin.Type = book.getType();
			doujin.Pages = book.getPages();
			doujin.Adult = book.isAdult();
			doujin.Decensored = book.isDecensored();
			doujin.Colored = book.isColored();
			doujin.Translated = book.isTranslated();
			doujin.Rating = book.getRating();
			doujin.Info = book.getInfo();
			for(Artist a : book.getArtists())
				doujin.artists.add(a.getJapaneseName());
			for(Circle c : book.getCircles())
				doujin.circles.add(c.getJapaneseName());
			for(Parody p : book.getParodies())
				doujin.parodies.add(p.getJapaneseName());
			for(Content ct : book.getContents())
				doujin.contents.add(ct.getTagName());
			Serializer serializer = new Persister();
			try
			{
				serializer.write(doujin, dest);
			} catch (Exception e) {
				Core.Logger.log(new LogEvent("Error parsing XML file (" + e.getMessage() + ").", LogLevel.WARNING));
			}
		}
		
		@Root(name="Doujin")
		private static final class Doujin
		{
			@Element(required=true)
			private String JapaneseName;
			@Element(required=false)
			private String TranslatedName = "";
			@Element(required=false)
			private String RomanjiName = "";
			@Element(required=false)
			private String Convention = "";
			@Element(required=false)
			private Date Released;
			@Element(required=false)
			private Type Type;
			@Element(required=false)
			private int Pages;
			@Element(required=false)
			private boolean Adult;
			@Element(required=false)
			private boolean Decensored;
			@Element(required=false)
			private boolean Translated;
			@Element(required=false)
			private boolean Colored;
			@Element(required=false)
			private Rating Rating;
			@Element(required=false)
			private String Info;
			@ElementList(entry="Artist", inline=true, required=false)
			private List<String> artists = new Vector<String>();
			@ElementList(entry="Circle", inline=true, required=false)
			private List<String> circles = new Vector<String>();
			@ElementList(entry="Parody", inline=true, required=false)
			private List<String> parodies = new Vector<String>();
			@ElementList(entry="Content", inline=true, required=false)
			private List<String> contents = new Vector<String>();
		}*/
		
		@Override
		public Set<DataSource> children() throws DataStoreException
		{
			Set<DataSource> ds = new TreeSet<DataSource>();
			if(Root.listFiles() == null)
				return ds;
			for(File child : Root.listFiles())
				ds.add(new ImplDataSource(child));
			return ds;
		}

		@Override
		public DataSource child(String name) throws DataStoreException
		{
			File file = new File(Root, name);
			return new ImplDataSource(file);
		}
		
		@Override
		public long size() throws DataStoreException
		{
			long size = 0;
			for(DataSource ds : children())
			{
				if(ds.isDirectory())
					size += _size(ds);
				else
					size += ds.size();
			}
			return size;
		}
		
		private long _size(DataSource source)
		{
			long size = 0;
			for(DataSource ds : source.children())
			{
				if(ds.isDirectory())
					size += _size(ds);
				else
					size += ds.size();
			}
			return size;
		}
		
		private final class ImplDataSource implements DataSource, Comparable<DataSource>
		{
			private File DsFile;
			
			public ImplDataSource(File file)
			{
				DsFile = file;
			}
			
			@Override
			public String getName() throws DataStoreException
			{
				if(!DsFile.equals(Root))
					return DsFile.getName();
				else
					return "/";
			}
			
			@Override
			public String getPath() throws DataStoreException
			{
				if(!DsFile.equals(Root))
					return getParent().getPath() + DsFile.getName() + (isDirectory()?"/":"");
				else
					return getName();
			}

			@Override
			public boolean isDirectory() throws DataStoreException
			{
				return DsFile.isDirectory();
			}

			@Override
			public boolean isFile() throws DataStoreException
			{
				return DsFile.isFile();
			}

			@Override
			public long size() throws DataStoreException
			{
				if(isDirectory())
					return -1L;
				else
					return DsFile.length();
			}

			@Override
			public InputStream getInputStream() throws DataStoreException
			{
				if(isDirectory())
					return null;
				try
				{
					return new FileInputStream(DsFile);
				} catch (FileNotFoundException e)
				{
					throw new DataStoreException(e);
				}
			}

			@Override
			public OutputStream getOutputStream() throws DataStoreException
			{
				if(isDirectory())
					return null;
				try
				{
					return new FileOutputStream(DsFile);
				} catch (FileNotFoundException e)
				{
					throw new DataStoreException(e);
				}
			}

			@Override
			public Set<DataSource> children() throws DataStoreException
			{
				Set<DataSource> ds = new TreeSet<DataSource>();
				if(DsFile.listFiles() == null)
					return ds;
				for(File child : DsFile.listFiles())
					ds.add(new ImplDataSource(child));
				return ds;
			}
			
			@Override
			public DataSource child(String name) throws DataStoreException
			{
				File file = new File(DsFile, name);
				return new ImplDataSource(file);
			}

			@Override
			public void touch() throws DataStoreException
			{
				try {
					DsFile.createNewFile();
				} catch (IOException ioe) {
					throw new DataStoreException(ioe);
				}
			}
			
			@Override
			public void mkdir() throws DataStoreException
			{
				if(!DsFile.mkdir())
					if(!DsFile.exists())
						throw new DataStoreException("Could not create directory '" + getName()+ "'.");
			}
			
			@Override
			public void mkdirs() throws DataStoreException
			{
				if(!DsFile.equals(Root))
					getParent().mkdirs();
				mkdir();
			}

			@Override
			public void delete() throws DataStoreException
			{
				if(!DsFile.equals(Root))
				if(isDirectory())
				{
					_delete(children());
					if(!DsFile.delete())
						DsFile.deleteOnExit();
				}
				else	
				if(!DsFile.delete())
					DsFile.deleteOnExit();
			}
			
			private void _delete(Set<DataSource> dss) throws DataStoreException
			{
				for(DataSource ds : dss)
					if(ds.isDirectory())
					{
						_delete(ds.children());
						ds.delete();
					}
					else
						ds.delete();
			}

			@Override
			public int compareTo(DataSource ds)
			{
				return getName().compareTo(ds.getName());
			}

			@Override
			public boolean exists() throws DataStoreException
			{
				return DsFile.exists();
			}

			@Override
			public DataSource getParent() throws DataStoreException
			{
				if(!DsFile.equals(Root))
					return new ImplDataSource(DsFile.getParentFile());
				else
					return new ImplDataSource(Root);
			}
		}
	}
}
