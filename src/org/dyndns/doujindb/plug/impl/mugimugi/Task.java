package org.dyndns.doujindb.plug.impl.mugimugi;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.*;

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
		SCAN("Scanning for cover"),
		UPLOAD("Uploading cover"),
		PARSE("Parsing response"),
		INSERT("Committing Book record"),
		CHECK("Checking for duplicates");
		
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
	public String id;
	@XmlElement(name="Description")
	public String description;
	@XmlElement(name="Workpath")
	public File workpath;
	@XmlElement(name="Active")
	public boolean active = false;
	@XmlElement(name="Threshold")
	public double threshold;
	
	private transient Set<TaskListener> listeners = new HashSet<TaskListener>();
	
//	private Book importedBook;
//	private String warningMessage = "";
//	private String errorMessage = "";
//	private JComponent epanel;
	
	@XmlElement(name="State")
	public State status = State.IDLE;
	@XmlElement(name="Step")
	public Step step = Step.SCAN;
	
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
		description = workpath.getName();
		threshold = DoujinshiDBScanner.THRESHOLD;
	}
	
	@Override
	public void run()
	{
		status = State.COMPLETED;
		for(TaskListener tl : listeners)
			tl.statusChanged(status);
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

//	@Override
//	public void run()
//	{
//		active = true;
//		status = TaskState.TASK_RUNNING;
//		description = "";
//		try
//		{
//			if(APIKEY == null)
//			{
//				description = "Invalid API key provided.";
//				status = TaskState.TASK_ERROR;
//				throw new Exception("Invalid API key provided.");
//			}
//			description = "Searching for cover image ...";
//			File cover_image = findFirstFile(workpath);
//			if(cover_image == null)
//			{
//				description = "Cover image not found (Double-click to open folder).";
//				status = TaskState.TASK_ERROR;
//				epanel = new JPanel();
//				epanel.setSize(240, 400);
//				JLabel l = new JLabel("<html><body style='margin:5px'>" +
//					"A suitable cover file was not found in the provided folder." +
//					"<br>Press Ok to open the folder in your Desktop Environment." +
//					"</body></html>");
//				JPanel bottom = new JPanel();
//				bottom.setLayout(new BorderLayout(5, 5));
//				bottom.add(l, BorderLayout.CENTER);
//				bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//				JButton ok = new JButton("Ok");
//				ok.setFont(Core.Resources.Font);
//				ok.setMnemonic('O');
//				ok.setFocusable(false);
//				ok.addActionListener(new ActionListener()
//				{
//					@Override
//					public void actionPerformed(ActionEvent ae) 
//					{
//						try
//						{
//							Desktop desktop = Desktop.getDesktop();
//							desktop.open(workpath);
//						} catch (IOException ioe) { }
//						DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
//						window.dispose();
//					}					
//				});
//				bottom.add(ok, BorderLayout.SOUTH);
//				epanel.add(bottom);
//				throw new Exception("Cover image not found.");
//			}
//			File cover_image2 = File.createTempFile("" + new java.util.Date().getTime(), ".jpg");
//			cover_image2.deleteOnExit();
//			BufferedImage resized;
//			{
//				BufferedImage image = javax.imageio.ImageIO.read(cover_image);
//				BufferedImage dest;
//				description = "Resizing cover image ...";
//				if(image.getWidth() > image.getHeight())
//					dest = new BufferedImage(image.getWidth() / 2, image.getHeight(), BufferedImage.TYPE_INT_RGB);
//				else
//					dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
//				Graphics g = dest.getGraphics();
//				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
//				g.dispose();
//				if(RESIZE_COVER)
//				try
//				{
//					description = "Resizing image before upload  ...";
//					resized = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
//					int wi = dest.getWidth(null),
//					hi = dest.getHeight(null),
//					wl = 256, 
//					hl = 256; 
//					if ((double)wl/wi > (double)hl/hi)
//					{
//						wi = (int) (wi * (double)hl/hi);
//						hi = (int) (hi * (double)hl/hi);
//					}else{
//						hi = (int) (hi * (double)wl/wi);
//						wi = (int) (wi * (double)wl/wi);
//					}
//					resized = org.dyndns.doujindb.util.Image.getScaledInstance(dest, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
//				} catch (Exception e) {
//					description = e.getMessage();
//					status = TaskState.TASK_ERROR;
//					throw new Exception(e.getMessage());
//				}else
//				{
//					resized = dest;
//				}
//				javax.imageio.ImageIO.write(resized, "PNG", cover_image2);
//			}
//			URLConnection urlc;
//			urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/?S=imageSearch").openConnection();
//			urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.Name + "/" + DoujinshiDBScanner.Version + "; +" + DoujinshiDBScanner.Weblink + ")");
//			description = "Sending cover image to doujinshi.mugimugi.org ...";
//			InputStream in = new ClientHttpRequest(urlc).post(
//		              new Object[] {
//		            	  "img", cover_image2
//		            	  });
//			description = "Parsing XML response ...";
//			{
//				XMLParser.XML_List list;
//				try
//				{
//					JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
//					Unmarshaller um = context.createUnmarshaller();
//					list = (XMLParser.XML_List) um.unmarshal(in);
//					if(list.ERROR != null)
//					{
//						description = "Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")";
//						status = TaskState.TASK_ERROR;
//						epanel = new JPanel();
//						epanel.setSize(240, 400);
//						JLabel l = new JLabel("<html><body style='margin:5px'>" +
//							"This item was not added to the Database." +
//							"<br>Press Ok to open the folder in your Desktop Environment." +
//							"</body></html>");
//						JPanel bottom = new JPanel();
//						bottom.setLayout(new BorderLayout(5, 5));
//						bottom.add(l, BorderLayout.CENTER);
//						bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//						JButton ok = new JButton("Ok");
//						ok.setFont(Core.Resources.Font);
//						ok.setMnemonic('O');
//						ok.setFocusable(false);
//						ok.addActionListener(new ActionListener()
//						{
//							@Override
//							public void actionPerformed(ActionEvent ae) 
//							{
//								try
//								{
//									Desktop desktop = Desktop.getDesktop();
//									desktop.open(workpath);
//								} catch (IOException ioe) { }
//								DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
//								window.dispose();
//							}					
//						});
//						bottom.add(ok, BorderLayout.SOUTH);
//						epanel.add(bottom);
//						throw new Exception("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
//					}
//					User = list.USER;
//					HashMap<Double, XMLParser.XML_Book> books = new HashMap<Double, XMLParser.XML_Book>();
//					double better_result = 0;
//					String result_string = "";
//					for(XMLParser.XML_Book book : list.Books)
//					{
//						double result = Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", "."));
//						books.put(result, book);
//						if(result > better_result)
//						{
//							better_result = result;
//							result_string = book.search;
//						}										
//					}
//					final String result_star = result_string;
//					if(threshold > better_result)
//					{
//						description = "No query matched the threshold (Double-click for more info).";
//						status = TaskState.TASK_ERROR;
//						epanel = new JPanel();
//						epanel.setLayout(new BorderLayout(5, 5));
//						if(!RESIZE_COVER)
//							try
//							{
//								BufferedImage resized2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
//								int wi = resized.getWidth(null),
//								hi = resized.getHeight(null),
//								wl = 256, 
//								hl = 256; 
//								if ((double)wl/wi > (double)hl/hi)
//								{
//									wi = (int) (wi * (double)hl/hi);
//									hi = (int) (hi * (double)hl/hi);
//								}else{
//									hi = (int) (hi * (double)wl/wi);
//									wi = (int) (wi * (double)wl/wi);
//								}
//								resized2 = org.dyndns.doujindb.util.Image.getScaledInstance(resized, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
//								resized = resized2;
//							} catch (Exception e) {
//								description = e.getMessage();
//								status = TaskState.TASK_ERROR;
//								throw new Exception(e.getMessage());
//							}
//						epanel.add(new JLabel(new ImageIcon(resized)), BorderLayout.WEST);
//						JTabbedPane tabs = new JTabbedPane();
//						for(XMLParser.XML_Book book : books.values())
//						{
//							final int bid = Integer.parseInt(book.ID.substring(1));
//							final URI uri = new URI("http://doujinshi.mugimugi.org/book/" + bid + "/");
//							final JLabel cover = new JLabel(new ImageIcon());
//							new Thread(getClass().getName()+"/ImageURL/Download")
//							{
//								@Override
//								public void run()
//								{
//									try
//									{
//										URL thumbURL = new URL("http://img.mugimugi.org/tn/" + (int)Math.floor((double)bid/(double)2000) + "/" + bid + ".jpg");
//										ImageIcon img = new ImageIcon(thumbURL);
//										cover.setIcon(img);
//									}catch(Exception e){ e.printStackTrace(); }
//								}
//							}.start();
//							final JButton link = new JButton("http://doujinshidb/" + bid);
//							link.setFont(Core.Resources.Font);
//							link.setFocusable(false);
//							link.addActionListener(new ActionListener()
//							{
//								@Override
//								public void actionPerformed(ActionEvent ae) 
//								{
//									try
//									{
//										Desktop desktop = Desktop.getDesktop();
//										desktop.browse(uri);
//									} catch (IOException ioe) { }
//									DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
//									window.dispose();
//								}					
//							});
//							JPanel panel = new JPanel();
//							panel.setLayout(new LayoutManager()
//							{
//
//								@Override
//								public void addLayoutComponent(String name, Component comp) { }
//
//								@Override
//								public void layoutContainer(Container comp)
//								{
//									int width = comp.getWidth(), height = comp.getHeight();
//									link.setBounds(1,1,width-2,15);
//									cover.setBounds(1,16,width-2,height-18);
//								}
//
//								@Override
//								public Dimension minimumLayoutSize(Container comp) { return new Dimension(250,250); }
//
//								@Override
//								public Dimension preferredLayoutSize(Container comp) { return new Dimension(250,250); }
//
//								@Override
//								public void removeLayoutComponent(Component comp){}
//								
//							});
//							panel.add(link);
//							panel.add(cover);
//							if(book.search.equals(result_star))
//								tabs.addTab("" + book.search, Resources.Icons.get("Plugin/Task/SearchQuery/Star"), panel);
//							else
//								tabs.addTab("" + book.search, panel);
//						}
//						epanel.add(tabs, BorderLayout.EAST);
//						JPanel bottom = new JPanel();
//						bottom.setLayout(new BorderLayout(5, 5));
//						final JCheckBox check = new JCheckBox("Set Threshold value = " + result_star);
//						check.setFont(Core.Resources.Font);
//						check.setFocusable(false);
//						bottom.add(check, BorderLayout.NORTH);
//						JButton ok = new JButton("Ok");
//						ok.setFont(Core.Resources.Font);
//						ok.setMnemonic('O');
//						ok.setFocusable(false);
//						ok.addActionListener(new ActionListener()
//						{
//							@Override
//							public void actionPerformed(ActionEvent ae) 
//							{
//								if(check.isSelected())
//									threshold = Double.parseDouble(result_star.replaceAll("%", "").replaceAll(",", "."));
//								DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
//								window.dispose();
//							}					
//						});
//						bottom.add(ok, BorderLayout.SOUTH);
//						epanel.add(bottom, BorderLayout.SOUTH);
//						throw new Exception("No query matched the threshold.");
//					}
//					try
//					{
//						XMLParser.XML_Book xmlbook  = books.get(better_result);
//						Book book = Context.doInsert(Book.class);
//						book.setJapaneseName(xmlbook.NAME_JP);
//						book.setTranslatedName(xmlbook.NAME_EN);
//						book.setRomajiName(xmlbook.NAME_R);
//						book.setDate(xmlbook.DATE_RELEASED);
//						book.setPages(xmlbook.DATA_PAGES);
//						book.setAdult(xmlbook.DATA_AGE == 1);
//						book.setDecensored(false);
//						book.setTranslated(false);
//						book.setColored(false);
//						book.setRating(Rating.UNRATED);
//						book.setInfo(xmlbook.DATA_INFO);
//						
//						RecordSet<Artist> artists = Context.getArtists(null);
//						RecordSet<Circle> circles = Context.getCircles(null);
//						RecordSet<Parody> parodies = Context.getParodies(null);
//						RecordSet<Content> contents = Context.getContents(null);
//						RecordSet<Convention> conventions = Context.getConventions(null);
//						
//						Map<String, Artist> alink = new HashMap<String, Artist>();
//						Map<String, Circle> clink = new HashMap<String, Circle>();
//						
//						for(XMLParser.XML_Item xmlitem : xmlbook.LINKS.Items)
//						{
//							try
//							{
//								switch(xmlitem.TYPE)
//								{
//								case type:
//									for(Book.Type type : Book.Type.values())
//										if(type.toString().equals(xmlitem.NAME_JP))
//											book.setType(type);
//									break;
//								case author:
//									_case:{
//										for(Artist artist : artists)
//											if((artist.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
//												(artist.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
//												(artist.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
//											{
//												book.addArtist(artist);
//												alink.put(xmlitem.ID, artist);
//												break _case;
//											}
//										Artist a = Context.doInsert(Artist.class);
//										a.setJapaneseName(xmlitem.NAME_JP);
//										a.setTranslatedName(xmlitem.NAME_EN);
//										a.setRomajiName(xmlitem.NAME_R);
//										book.addArtist(a);
//										alink.put(xmlitem.ID, a);
//									}
//									break;
//								case character:
//									break;
//								case circle:
//									/**
//									 * Ok, we cannot link book <--> circle directly.
//									 * We have to link book <--> artist <--> circle instead.
//									 */
//									_case:{
//										for(Circle circle : circles)
//											if((circle.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
//													(circle.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
//													(circle.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
//											{
//												// book.addCircle(circle);
//												clink.put(xmlitem.ID, circle);
//												break _case;
//											}
//										Circle c = Context.doInsert(Circle.class);
//										c.setJapaneseName(xmlitem.NAME_JP);
//										c.setTranslatedName(xmlitem.NAME_EN);
//										c.setRomajiName(xmlitem.NAME_R);
//										// book.addCircle(c);
//										clink.put(xmlitem.ID, c);
//									}
//									break;
//								case collections:
//									break;
//								case contents:
//									_case:{
//										for(Content content : contents)
//											if((content.getTagName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
//													content.getTagName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals("")) ||
//													content.getTagName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals("")) ||
//													content.getAliases().contains(xmlitem.NAME_JP) ||
//													content.getAliases().contains(xmlitem.NAME_EN) ||
//													content.getAliases().contains(xmlitem.NAME_R))
//											{
//												book.addContent(content);
//												break _case;
//											}
//										Content cn = Context.doInsert(Content.class);
//										// Tag Name priority NAME_JP > NAME_EN > NAME_R
//										cn.setTagName(xmlitem.NAME_JP.equals("")?xmlitem.NAME_EN.equals("")?xmlitem.NAME_R:xmlitem.NAME_EN:xmlitem.NAME_JP);
//										book.addContent(cn);
//									}
//									break;
//								case convention:
//									if(book.getConvention() != null)
//										break;
//									_case:{
//										for(Convention convention : conventions)
//											if((convention.getTagName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
//													convention.getTagName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals("")) ||
//													convention.getTagName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals("")) ||
//													convention.getAliases().contains(xmlitem.NAME_JP) ||
//													convention.getAliases().contains(xmlitem.NAME_EN) ||
//													convention.getAliases().contains(xmlitem.NAME_R))
//											{
//												book.setConvention(convention);
//												break _case;
//											}
//										Convention cv = Context.doInsert(Convention.class);
//										// Tag Name priority NAME_EN > NAME_JP > NAME_R
//										cv.setTagName(xmlitem.NAME_EN.equals("")?xmlitem.NAME_JP.equals("")?xmlitem.NAME_R:xmlitem.NAME_JP:xmlitem.NAME_EN);
//										book.setConvention(cv);
//									}
//									break;
//								case genre:
//									break;
//								case imprint:
//									break;
//								case parody:
//									_case:{
//									for(Parody parody : parodies)
//										if((parody.getJapaneseName().equals(xmlitem.NAME_JP) && (!xmlitem.NAME_JP.equals(""))) ||
//												(parody.getTranslatedName().equals(xmlitem.NAME_EN) && (!xmlitem.NAME_EN.equals(""))) ||
//												(parody.getRomajiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
//										{
//											book.addParody(parody);
//											break _case;
//										}
//									Parody p = Context.doInsert(Parody.class);
//									p.setJapaneseName(xmlitem.NAME_JP);
//									p.setTranslatedName(xmlitem.NAME_EN);
//									p.setRomajiName(xmlitem.NAME_R);
//									book.addParody(p);
//									}
//									break;
//								case publisher:
//									break;
//								}
//							}catch(Exception e) { e.printStackTrace(); }
//						}
//						
//						Context.doCommit();
//						
//						if(alink.size() > 0 && clink.size() > 0)
//						{
//							String[] ckeys = (String[]) clink.keySet().toArray(new String[0]);
//							String[] akeys = (String[]) alink.keySet().toArray(new String[0]);
//							String ids = ckeys[0];
//							for(int i=1;i<ckeys.length;i++)
//								ids += ckeys[i] + ",";
//							urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
//							urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.Name + "/" + DoujinshiDBScanner.Version + "; +" + DoujinshiDBScanner.Weblink + ")");
//							InputStream in0 = urlc.getInputStream();
//							DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
//							docfactory.setNamespaceAware(true);
//							DocumentBuilder builder = docfactory.newDocumentBuilder();
//							Document doc = builder.parse(in0);
//							XPathFactory xmlfactory = XPathFactory.newInstance();
//							XPath xpath = xmlfactory.newXPath();
//							for(String cid : ckeys)
//							{
//								for(String aid : akeys)
//								{
//									XPathExpression expr = xpath.compile("//ITEM[@ID='" + cid + "']/LINKS/ITEM[@ID='" + aid + "']");
//									Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
//									if(node == null)
//										continue;
//									else
//										clink.get(cid).addArtist(alink.get(aid));
//								}
//							}
//						}
//						
//						Context.doCommit();
//						
//						importedBook = book;
//						if(importedBook == null)
//						{
//							description = "Error parsing XML data.";
//							status = TaskState.TASK_ERROR;
//							throw new Exception("Error parsing XML data.");
//						}
//						
//						for(Book book_ : Context.getBooks(null))
//							if(importedBook.getJapaneseName().equals(book_.getJapaneseName()) && !importedBook.getID().equals(book_.getID()))
//							{
//								status = TaskState.TASK_WARNING;
//								warningMessage = "Possible duplicate item detected [ID='"+book_.getID()+"']."; //FIXME When detecting multiple dupes???
//							}
//					} catch (Exception e) {
//						e.printStackTrace();
//						description = e.getMessage();
//						status = TaskState.TASK_ERROR;
//						throw new Exception(e.getMessage());
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					status = TaskState.TASK_ERROR;
//					throw new Exception(e.getMessage());
//				}
//			}
//			description = "Copying files into the Datastore ...";
//			for(File file : workpath.listFiles())
//				fileCopy(file, Core.Repository.child(importedBook.getID()));
//			try
//			{
//				description = "Creating preview into the Datastore  ...";
//				DataFile ds = Core.Repository.child(importedBook.getID());
//				ds.mkdir();
//				ds = Core.Repository.getPreview(importedBook.getID());
//				ds.touch();
//				OutputStream out = ds.getOutputStream();
//				BufferedImage image = javax.imageio.ImageIO.read(cover_image2);
//				int wi = image.getWidth(null),
//				hi = image.getHeight(null),
//				wl = 256, 
//				hl = 256; 
//				if(!(wi < wl) && !(hi < hl)) // Cannot scale an image smaller than 256x256, or getScaledInstance is going to loop
//					if ((double)wl/wi > (double)hl/hi)
//					{
//						wi = (int) (wi * (double)hl/hi);
//						hi = (int) (hi * (double)hl/hi);
//					}else{
//						hi = (int) (hi * (double)wl/wi);
//						wi = (int) (wi * (double)wl/wi);
//					}
//				javax.imageio.ImageIO.write(org.dyndns.doujindb.util.Image.getScaledInstance(image, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true), "PNG", out);
//				out.close();
//				if(status == TaskState.TASK_WARNING)
//				{
//					description = "Doujin successfully imported (" + warningMessage + ")";
//				}else{
//					status = TaskState.TASK_COMPLETED;
//					description = "Doujin successfully imported.";
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				description = e.getMessage();
//				status = TaskState.TASK_ERROR;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			status = TaskState.TASK_ERROR;
//		}
//		active = false;
//	}
//	
//	public double getThreshold() {
//		return threshold;
//	}
//
//	public void setThreshold(double threshold) {
//		this.threshold = threshold;
//	}
//
//	private File findFirstFile(File directory)
//	{
//		File[] files = directory.listFiles(
//				new FilenameFilter()
//				{
//					@Override
//					public boolean accept(File dir, String fname)
//					{
//						return !(new File(dir, fname).isHidden());
//					}
//				});
//		Arrays.sort(files, new Comparator<File>()
//		{
//			@Override
//			public int compare(File f1, File f2)
//			{
//				return f1.getName().compareTo(f2.getName());
//			}
//		});				
//		for(File file : files)
//			if(file.isFile())
//				return file;
//			else
//				return findFirstFile(file);
//		return null;
//	}
//	
//	private void fileCopy(File file, DataFile ds) throws IOException
//	{
//		DataFile dst = ds.child(file.getName());
//		if(file.isDirectory())
//		{
//			dst.mkdirs();
//			for(File f : file.listFiles())
//				fileCopy(f, dst);
//		}else
//		{
//			dst.getParent().mkdirs();
//			dst.touch();
//			OutputStream out = dst.getOutputStream();
//			InputStream in = new FileInputStream(file);
//			byte[] buff = new byte[0x800];
//			int read;
//			while((read = in.read(buff)) != -1)
//			{
//				out.write(buff, 0, read);
//			}
//			in.close();
//			out.close();
//		}
//	}
	
	//FIXME
//	private void openDialog()
//	{	
//		switch(status)
//		{
//		case TASK_COMPLETED:
//			try {
//				QueryBook query = new QueryBook();
//				query.ID = importedBook.getID();
//				RecordSet<Book> books = Core.Database.getBooks(query);
//				for(Book b : books)
//					if(b.getID().equals(importedBook.getID()))
//						Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, b);
//			} catch (DataBaseException dbe) {
//				Core.Logger.log(dbe.getMessage(), Level.ERROR);
//				dbe.printStackTrace();
//			}
//			break;
//		case TASK_RUNNING:
//			break;
//		case TASK_QUEUED:
//			break;
//		case TASK_ERROR:
//			try
//			{ 
//				try {
//					Core.UI.Desktop.showDialog(
//							(RootPaneContainer) getRootPane().getParent(),
//							epanel, 
//							IconError, 
//							"Error - " + description.replaceAll(" \\(.*",""));
//					} catch (PropertyVetoException pve) { }
//			} catch (NullPointerException npe) { }
//			break;
//		case TASK_WARNING:
//			try {
//				QueryBook query = new QueryBook();
//				query.ID = importedBook.getID();
//				RecordSet<Book> books = Core.Database.getBooks(query);
//				for(Book b : books)
//					if(b.getID().equals(importedBook.getID()))
//							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, b);
//			} catch (DataBaseException dbe) {
//				Core.Logger.log(dbe.getMessage(), Level.ERROR);
//				dbe.printStackTrace();
//			}
//			break;
//		}				
//	}
}