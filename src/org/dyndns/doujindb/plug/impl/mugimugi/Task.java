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
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;

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
		INSERT("Commit Book record"),
		//FIXME? CHECK("Scan for duplicates")
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
	
	private transient Map<Double, XMLParser.XML_Book> results = new TreeMap<Double, XMLParser.XML_Book>(Collections.reverseOrder());
	
	@XmlElement(name="Book")
	private String bookid;
	
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
	
	public String id()
	{
		return id;
	}
	
	public String bookid()
	{
		return bookid;
	}
	
	public String message()
	{
		return message;
	}
	
	private void message(String message)
	{
		this.message = message;
	}
	
	public double threshold()
	{
		return threshold;
	}
	
	public File workpath()
	{
		return workpath;
	}
	
	public Map<Step, State> steps()
	{
		return steps;
	}
	
	public Step step()
	{
		return step;
	}
	
	public Map<Double, XMLParser.XML_Book> results()
	{
		if(id != null && results.isEmpty())
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLParser.XML_List list = (XMLParser.XML_List) um.unmarshal(
				new FileInputStream(
					new File(DoujinshiDBScanner.PLUGIN_HOME, id + ".xml")));
			results = new TreeMap<Double, XMLParser.XML_Book>(Collections.reverseOrder());
			for(XMLParser.XML_Book xml_book : list.Books)
			{
				double result = Double.parseDouble(xml_book.search.replaceAll("%", "").replaceAll(",", "."));
				results.put(result, xml_book);
			}
		} catch (Exception e) { e.printStackTrace(); }
		return results;
	}
	
	private void status(State status)
	{
		this.steps.put(step, status);
		for(TaskListener tl : listeners)
			tl.statusChanged(step, status);
	}
	
	private void step(Step step)
	{
		this.step = step;
		for(TaskListener tl : listeners)
			tl.stepChanged(step);
	}

	@Override
	public void run()
	{
		step(Step.INIT);
		message("Checking API key ...");
		status(State.RUNNING);
		
		if(DoujinshiDBScanner.APIKEY == null ||
				!(DoujinshiDBScanner.APIKEY + "").matches("[0-9a-f]{20}"))
			{
				message("Invalid API key provided.");
				status(State.ERROR);
				return;
			} else {
				message("DoujinshiDB API is valid.");
				status(State.COMPLETED);
			}

			step(Step.SCAN);
			message("Searching for cover image ...");
			status(State.RUNNING);

			File cover_file = findFile(workpath);
			if(cover_file == null)
			{
				message("Cover image not found.");
				status(State.ERROR);
				return;
			}
			BufferedImage image;
			try {
				image = javax.imageio.ImageIO.read(cover_file);
			} catch (IOException ioe) {
				message(ioe.getMessage());
				status(State.ERROR);
				return;
			}
			if(image == null)
			{
				message("Cover image not found.");
				status(State.ERROR);
				return;
			}
			message("Cover image found.");
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
					message("Resizing image before upload  ...");
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
					message(e.getMessage());
					status(State.ERROR);
					return;
				}else
				{
					resized = dest;
				}
				try {
					javax.imageio.ImageIO.write(resized, "PNG", req_file);
				} catch (IOException ioe) {
					message(ioe.getMessage());
					status(State.ERROR);
					return;
				}
				status(State.COMPLETED);
			}

			step(Step.UPLOAD);
			message("Sending cover image to doujinshi.mugimugi.org ...");
			status(State.RUNNING);

			URLConnection urlc;
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
				message(murle.getMessage());
				status(State.ERROR);
				return;
			} catch (IOException ioe) {
				message(ioe.getMessage());
				status(State.ERROR);
				return;
			}
			status(State.COMPLETED);

			step(Step.PARSE);
			message("Parsing XML response ...");
			status(State.RUNNING);

			Book book;
			{
				XMLParser.XML_List list;
				try
				{
					JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
					Unmarshaller um = context.createUnmarshaller();
					list = (XMLParser.XML_List) um.unmarshal(new FileInputStream(rsp_file));
					if(list.ERROR != null)
					{
						message("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
						status(State.ERROR);
						throw new Exception("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
					}
					DoujinshiDBScanner.User = list.USER;
					
					double better_result = 0;
					for(XMLParser.XML_Book xml_book : list.Books)
					{
						double result = Double.parseDouble(xml_book.search.replaceAll("%", "").replaceAll(",", "."));
						results.put(result, xml_book);
						if(result > better_result)
							better_result = result;
					}
					if(threshold > better_result)
					{
						for(XMLParser.XML_Book result : results.values())
						{
							final int bid = Integer.parseInt(result.ID.substring(1));
							// final URI uri = new URI("http://doujinshi.mugimugi.org/book/" + bid + "/");
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
						message("No query matched the threshold.");
						status(State.WARNING);
						return;
					}
					try
					{
						XMLParser.XML_Book xmlbook  = results.get(better_result);
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
						status(State.COMPLETED);
						
						step(Step.INSERT);
						message("Committing ...");
						status(State.RUNNING);
						
						DoujinshiDBScanner.Context.doCommit();
						
						if(alink.size() > 0 && clink.size() > 0)
						{
							String[] ckeys = (String[]) clink.keySet().toArray(new String[0]);
							String[] akeys = (String[]) alink.keySet().toArray(new String[0]);
							String ids = ckeys[0];
							for(int i=1;i<ckeys.length;i++)
								ids += ckeys[i] + ",";
							urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + DoujinshiDBScanner.APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
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
						
						this.bookid = book.getID();
						
						/**
						 * //FIXME When detecting multiple dupes???
						for(Book book_ : Context.getBooks(null))
							if(book.getJapaneseName().equals(book_.getJapaneseName()) && !book.getID().equals(book_.getID()))
							{
								status(State.WARNING);
								message("Possible duplicate item detected [ID='"+book_.getID()+"'].");
							}
						*/
					} catch (Exception e) {
						message(e.getMessage());
						status(State.ERROR);
						e.printStackTrace();
						throw new Exception(e.getMessage());
					}
				} catch (Exception e) {
					message(e.getMessage());
					status(State.ERROR);
					e.printStackTrace();
					return;
				}
			}
			message("Copying files into the Datastore ...");
			for(File file : workpath.listFiles())
				try {
					copyFile(file, Core.Repository.child(book.getID()));
				} catch (DataBaseException | IOException e) {
					message(e.getMessage());
					status(State.ERROR);
					e.printStackTrace();
				}
			try
			{
				message("Creating preview into the Datastore  ...");
				DataFile ds = Core.Repository.child(book.getID());
				ds.mkdir();
				ds = Core.Repository.getPreview(book.getID());
				ds.touch();
				OutputStream out = ds.getOutputStream();
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
				message("Doujin successfully imported.");
				status(State.COMPLETED);
			} catch (Exception e) {
				message(e.getMessage());
				status(State.ERROR);
				e.printStackTrace();
			}
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
}