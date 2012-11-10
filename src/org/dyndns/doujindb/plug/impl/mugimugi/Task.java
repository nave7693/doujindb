package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.ImageIcon;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.mugimugi", name="Task")
final class Task implements Runnable
{
	enum State {
		IDLE,
		RUNNING,
		ERROR,
		WARNING,
		COMPLETED
	}
	
	enum Step {
		INIT("Check API key"),
		SCAN("Scan cover image"),
		UPLOAD("Upload cover image"),
		PARSE("Parse XML response"),
		CHECK("Scan for duplicates"),
		INSERT("Commit Book record")
		;
		
		private String value;
		
		Step(String value)
		{
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}
	
	@XmlAttribute(name="ID")
	private String id;
	@XmlElement(name="Message")
	private String message;
	@XmlElement(name="Workpath")
	private File workpath;
	@XmlElement(name="Threshold")
	private double threshold;
	
	private transient Set<TaskListener> listeners = new HashSet<TaskListener>();
	
	@XmlElement(name="Step")
	private Step step = Step.INIT;
	@XmlElement(name="Steps")
	private final Map<Step, State> steps = new HashMap<Step, State>();
	
	/**
	 * Imported book
	 */
	@XmlElement(name="Book")
	private String book;
	
	/**
	 * Matching result
	 */
	@XmlElement(name="Result")
	private String result;
	
	/**
	 * All results from the image query
	 * @see loadResults()
	 */
	private transient Map<String, XMLParser.XML_Book> results = new TreeMap<String, XMLParser.XML_Book>(Collections.reverseOrder());
	
	/**
	 * All dupes found
	 */
	private Set<String> dupes = new HashSet<String>();
	
	@XmlElement(name="Done")
	private boolean done;
	
	{
		for(Step step : Step.values())
			steps.put(step, State.IDLE);
	}
	
	/**
	 * Used by JAXB, do not remove or suffer
	 * IllegalAnnotationsException : Task does not have a no-arg default constructor.
	 */
	@SuppressWarnings("unused")
	private Task() {}

	Task(String id, File path)
	{
		this.id = id;
		workpath = path;
		message = workpath.getName();
		threshold = DoujinshiDBScanner.THRESHOLD;
	}
	
	public void addTaskListener(TaskListener tl)
	{
		listeners.add(tl);
	}
	
	public void removeTaskListener(TaskListener tl)
	{
		listeners.remove(tl);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Task))
			if(!(obj instanceof String))
				return false;
			else
				return ((String)obj).equals(id);
		else
			return ((Task)obj).id.equals(id);
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getBook()
	{
		return book;
	}
	
	public void setBook(String book)
	{
		this.result = book;
	}
	
	public Set<String> getDuplicates()
	{
		return dupes;
	}
	
	public void skipDuplicates()
	{
		this.dupes = new HashSet<String>();
		this.steps.put(Step.CHECK, State.COMPLETED);
	}
	
	public String getMessage()
	{
		return message;
	}
	
	private void setMessage(String message)
	{
		this.message = message;
	}
	
	public double getThreshold()
	{
		return threshold;
	}
	
	public File getWorkpath()
	{
		return workpath;
	}
	
	public Iterable<Step> getSteps()
	{
		return steps.keySet();
	}
	
	public State getStatus(Step step)
	{
		return steps.get(step);
	}
	
	public Step getStep()
	{
		return step;
	}
	
	public Map<String, XMLParser.XML_Book> getResults()
	{
		return results;
	}
	
	void loadResults()
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLParser.XML_List list = (XMLParser.XML_List) um.unmarshal(
				new FileInputStream(
					new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".xml")));
			results = new HashMap<String, XMLParser.XML_Book>();
			for(XMLParser.XML_Book xml_book : list.Books)
			{
				results.put(xml_book.ID, xml_book);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private void setStatus(State status)
	{
		this.steps.put(step, status);
		for(TaskListener tl : listeners)
			tl.statusChanged(step, status);
	}
	
	private void setStep(Step step)
	{
		this.step = step;
		for(TaskListener tl : listeners)
			tl.stepChanged(step);
	}

	@Override
	public void run()
	{
		if(!getStatus(Step.INIT).equals(State.COMPLETED))
		{
			setStatus(doInit());
			if(!getStatus(Step.INIT).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}

		if(!getStatus(Step.SCAN).equals(State.COMPLETED))
		{
			setStatus(doScan());
			if(!getStatus(Step.SCAN).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}
		
		if(!getStatus(Step.UPLOAD).equals(State.COMPLETED))
		{
			setStatus(doUpload());
			if(!getStatus(Step.UPLOAD).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}
		
		if(!getStatus(Step.PARSE).equals(State.COMPLETED))
		{
			setStatus(doParse());
			if(!getStatus(Step.PARSE).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}
		
		if(!getStatus(Step.CHECK).equals(State.COMPLETED))
		{
			setStatus(doCheck());
			if(!getStatus(Step.CHECK).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}
		
		if(!getStatus(Step.INSERT).equals(State.COMPLETED))
		{
			setStatus(doInsert());
			if(!getStatus(Step.INSERT).equals(State.COMPLETED))
			{
				setDone(true);
				return;
			}
		}

		setDone(true);
	}

	private File findFile(File directory)
	{
		File[] files = directory.listFiles(
				new FilenameFilter()
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
	
	private void copyFile(File file, DataFile ds) throws IOException
	{
		DataFile dst = ds.child(file.getName());
		if(file.isDirectory())
		{
			dst.mkdirs();
			for(File f : file.listFiles())
				copyFile(f, dst);
		}else
		{
			dst.getParent().mkdirs();
			dst.touch();
			OutputStream out = dst.getOutputStream();
			InputStream in = new FileInputStream(file);
			byte[] buff = new byte[0x800];
			int read;
			while((read = in.read(buff)) != -1)
			{
				out.write(buff, 0, read);
			}
			in.close();
			out.close();
		}
	}
	
	private void copyStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buff = new byte[0x800];
		int read;
		while((read = in.read(buff)) != -1)
		{
			out.write(buff, 0, read);
		}
	}
	
	private State doInit()
	{
		setStep(Step.INIT);
		setStatus(State.RUNNING);
		setMessage("Checking API key ...");
		
		if(DoujinshiDBScanner.APIKEY == null ||
				!(DoujinshiDBScanner.APIKEY + "").matches("[0-9a-f]{20}"))
		{
			setMessage("Invalid API key provided.");
			return State.ERROR;
		} else {
			setMessage("DoujinshiDB API is valid.");
			return State.COMPLETED;
		}
	}
	
	private State doScan()
	{
		setStep(Step.SCAN);
		setStatus(State.RUNNING);
		setMessage("Searching for cover image ...");

		File cover_file = findFile(workpath);
		if(cover_file == null)
		{
			setMessage("Cover image not found.");
			return State.ERROR;
		}
		BufferedImage image;
		try {
			image = javax.imageio.ImageIO.read(cover_file);
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
			return State.ERROR;
		}
		if(image == null)
		{
			setMessage("Cover image not found.");
			return State.ERROR;
		}
		setMessage("Cover image found.");
		File req_file = new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".png");
		BufferedImage resized;
		{
			BufferedImage dest;
			if(image.getWidth() > image.getHeight())
				dest = new BufferedImage(image.getWidth() / 2, image.getHeight(), BufferedImage.TYPE_INT_RGB);
			else
				dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = dest.getGraphics();
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();
			if(DoujinshiDBScanner.RESIZE_COVER)
			try
			{
				setMessage("Resizing image before upload  ...");
				resized = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
				int wi = dest.getWidth(null),
				hi = dest.getHeight(null),
				wl = 256, 
				hl = 256; 
				if ((double)wl/wi > (double)hl/hi)
				{
					wi = (int) (wi * (double)hl/hi);
					hi = (int) (hi * (double)hl/hi);
				}else{
					hi = (int) (hi * (double)wl/wi);
					wi = (int) (wi * (double)wl/wi);
				}
				resized = org.dyndns.doujindb.util.Image.getScaledInstance(dest, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			} catch (Exception e) {
				setMessage(e.getMessage());
				return State.ERROR;
			}else
			{
				resized = dest;
			}
			try {
				javax.imageio.ImageIO.write(resized, "PNG", req_file);
			} catch (IOException ioe) {
				setMessage(ioe.getMessage());
				return State.ERROR;
			}
			return State.COMPLETED;
		}
	}
	
	private State doUpload()
	{
		setStep(Step.UPLOAD);
		setStatus(State.RUNNING);
		setMessage("Sending cover image to doujinshi.mugimugi.org ...");

		URLConnection urlc;
		File req_file = new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".png");
		File rsp_file;
		
		try {
			urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + DoujinshiDBScanner.APIKEY + "/?S=imageSearch").openConnection();
			urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.Name + "/" + DoujinshiDBScanner.Version + "; +" + DoujinshiDBScanner.Weblink + ")");
			InputStream rsp_in = new ClientHttpRequest(urlc).post(
			          new Object[] {
			        	  "img", req_file
			        	  });
			rsp_file = new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".xml");
			FileOutputStream rsp_out = new FileOutputStream(rsp_file);
			copyStream(rsp_in, rsp_out);
			rsp_in.close();
			rsp_out.close();
		} catch (MalformedURLException murle) {
			setMessage(murle.getMessage());
			return State.ERROR;
		} catch (IOException ioe) {
			setMessage(ioe.getMessage());
			return State.ERROR;
		}
		return State.COMPLETED;
	}
	
	private State doParse()
	{
		setStep(Step.PARSE);
		setStatus(State.RUNNING);
		setMessage("Parsing XML response ...");
		
		if(this.result != null)
		{
			return State.COMPLETED;
		}
		
		File rsp_file = new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".xml");
		XMLParser.XML_List list;
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			list = (XMLParser.XML_List) um.unmarshal(new FileInputStream(rsp_file));
			if(list.ERROR != null)
			{
				setMessage("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
				throw new Exception("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
			}
			DoujinshiDBScanner.User = list.USER;
			
			double better_result = 0;
			for(XMLParser.XML_Book xml_book : list.Books)
			{
				double result = Double.parseDouble(xml_book.search.replaceAll("%", "").replaceAll(",", "."));
				results.put(xml_book.ID, xml_book);
				if(result > better_result)
				{
					better_result = result;
					this.result = xml_book.ID;
				}
			}
			if(threshold > better_result)
			{
				/**
				 * Reset found result
				 */
				this.result = null;
				/**
				 * Get more data from the DoujinshiDB (images)
				 */
				for(XMLParser.XML_Book result : results.values())
				{
					final int bid = Integer.parseInt(result.ID.substring(1));
					try
					{
						URL thumbURL = new URL("http://img.mugimugi.org/tn/" + (int)Math.floor((double)bid/(double)2000) + "/" + bid + ".jpg");
						Image img = new ImageIcon(thumbURL).getImage();
						BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
						bi.getGraphics().drawImage(img, 0, 0, null);
						javax.imageio.ImageIO.write(bi,
								"PNG",
								new File(
									new File(DoujinshiDBScanner.PLUGIN_HOME,
											".cache"), result.ID + ".png"));
					}catch(Exception e){ e.printStackTrace(); }
				}
				setMessage("No query matched the threshold.");
				return State.WARNING;
			} else {
				return State.COMPLETED;
			}
		} catch (Exception e) {
			setMessage(e.getMessage());
			return State.ERROR;
		}
	}
	
	private State doCheck()
	{
		setStep(Step.CHECK);
		setStatus(State.RUNNING);
		setMessage("Scanning for duplicate Books ...");
		
		Set<Book> books = new TreeSet<Book>();
		QueryBook query;
		XMLParser.XML_Book xmlbook;
		
		xmlbook = results.get(this.result);		
		if(!xmlbook.NAME_JP.equals(""))
		{
			query = new QueryBook();
			query.JapaneseName = xmlbook.NAME_JP;
			for(Book b : Core.Database.getBooks(query))
				books.add(b);
		}
		if(!xmlbook.NAME_EN.equals(""))
		{
			query = new QueryBook();
			query.TranslatedName = xmlbook.NAME_EN;
			for(Book b : Core.Database.getBooks(query))
				books.add(b);
		}
		if(!xmlbook.NAME_R.equals(""))
		{
			query = new QueryBook();
			query.RomajiName = xmlbook.NAME_R;
			for(Book b : Core.Database.getBooks(query))
				books.add(b);
		}
		
		if(!books.isEmpty())
		{
			dupes.clear();
			for(Book b : books)
				dupes.add(b.getID());
			setMessage("Duplicate Book" + (books.size() > 1 ? "s" : "") + " detected!");
			return State.WARNING;
		}
		
		return State.COMPLETED;
	}
	
	private State doInsert()
	{
		setStep(Step.INSERT);
		setStatus(State.RUNNING);
		setMessage("Importing data ...");
		
		Book book;
		try
		{
			XMLParser.XML_Book xmlbook  = results.get(this.result);
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
			book.setInfo(xmlbook.DATA_INFO);
			
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
				} catch(Exception e) { e.printStackTrace(); }
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
				urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.Name + "/" + DoujinshiDBScanner.Version + "; +" + DoujinshiDBScanner.Weblink + ")");
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
			
			this.book = book.getID();
			
		} catch (Exception e) {
			setMessage(e.getMessage());
			return State.ERROR;
		}
		
		setMessage("Copying files into the Datastore ...");
		for(File file : workpath.listFiles())
			try {
				copyFile(file, Core.Repository.child(book.getID()));
			} catch (DataBaseException | IOException e) {
				setMessage(e.getMessage());
				return State.ERROR;
			}
		File req_file = new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".png");
		try
		{
			setMessage("Creating preview into the Datastore  ...");
			DataFile ds = Core.Repository.child(book.getID());
			ds.mkdir();
			ds = Core.Repository.getPreview(book.getID());
			ds.touch();
			OutputStream out = ds.getOutputStream();
			BufferedImage image = javax.imageio.ImageIO.read(req_file);
			int wi = image.getWidth(null),
			hi = image.getHeight(null),
			wl = 256, 
			hl = 256; 
			if(!(wi < wl) && !(hi < hl)) // Cannot scale an image smaller than 256x256, or getScaledInstance is going to loop
				if ((double)wl/wi > (double)hl/hi)
				{
					wi = (int) (wi * (double)hl/hi);
					hi = (int) (hi * (double)hl/hi);
				}else{
					hi = (int) (hi * (double)wl/wi);
					wi = (int) (wi * (double)wl/wi);
				}
			javax.imageio.ImageIO.write(org.dyndns.doujindb.util.Image.getScaledInstance(image, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true), "PNG", out);
			out.close();
			setMessage("Doujin successfully imported.");
			return State.COMPLETED;
		} catch (Exception e) {
			setMessage(e.getMessage());
			return State.ERROR;
		}
	}

	public boolean isDone()
	{
		return done;
	}
	
	public void setDone(boolean done)
	{
		this.done = done;
	}
}