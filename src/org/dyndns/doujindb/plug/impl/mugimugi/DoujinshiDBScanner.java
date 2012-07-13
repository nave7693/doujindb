package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.event.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

/**  
* DoujinshiDBScanner.java - Plugin to batch process media files thanks to the DoujinshiDB project APIs.
* @author  nozomu
* @version 1.0
*/
public final class DoujinshiDBScanner implements Plugin
{
	public String APIKEY = "";
	public int THRESHOLD = 75;
	public boolean RESIZE_COVER = false;
	public int QUERIES;
	public int IMAGE_QUERIES;
	public String USERID;
	public String USERNAME;
	
	private static String UUID = "{CB123239-06D1-4FB6-A4CC-05C4B436DF73}";
	private static DataBaseContext Context;
	
	private JComponent UI;
	private static ImageIcon PluginIcon = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/plugin-icon.png"));
	private static ImageIcon IconRefresh = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/refresh.png"));
	private static ImageIcon IconAdd = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/add.png"));
	private static ImageIcon IconSettings = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/settings.png"));
	private static ImageIcon IconTasks = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/tasks.png"));
	private static ImageIcon IconLoading = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/loading.png"));
	private static ImageIcon IconRunning = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-running.png"));
	private static ImageIcon IconQueued = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-queued.png"));
	private static ImageIcon IconError = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-error.png"));
	private static ImageIcon IconRemove = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-remove.png"));
	private static ImageIcon IconWarning = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-warning.png"));
	private static ImageIcon IconCompleted = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-completed.png"));
	private static ImageIcon IconCleanCompleted = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-clean-completed.png"));
	private static ImageIcon IconReset = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-reset.png"));
	private static ImageIcon IconStop = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-stop.png"));
	private static ImageIcon IconStar = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/task-searchquery-star.png"));
	
	public DoujinshiDBScanner()
	{
		Property prop;
		{
			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.apikey"))
				APIKEY = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").asString();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.apikey");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey");
				prop.setValue(APIKEY);
				prop.setDescription("<html><body>Apikey used to query the doujinshidb database.</body></html>");
			}	
		}
		{
			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.threshold"))
				THRESHOLD = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold").asNumber();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.threshold");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold");
				prop.setValue(THRESHOLD);
				prop.setDescription("<html><body>Threshold limit for matching cover queries.</body></html>");
			}	
		}
		{
			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.resize_cover"))
				RESIZE_COVER = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover").asBoolean();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.resize_cover");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover");
				prop.setValue(RESIZE_COVER);
				prop.setDescription("<html><body>Whether to resize covers before uploading them.</body></html>");
			}	
		}
		
		Context = Core.Database.getContext(UUID);
		
		UI = new PluginUI();
	}
	
	@Override
	public Icon getIcon() {
		return PluginIcon;
	}
	@Override
	public String getName() {
		return "DoujinshiDB Scanner";
	}
	@Override
	public String getDescription() {
		return "The DoujinshiDB plugin lets you batch process media files thanks to the DoujinshiDB APIs.";
	}
	@Override
	public String getVersion() {
		return "0.8";
	}
	@Override
	public String getAuthor() {
		return "Nozomu";
	}
	@Override
	public String getWeblink() {
		return "http://doujindb.co.cc/";
	}
	@Override
	public JComponent getUI() {
		return UI;
	}
	
	@SuppressWarnings("unused")
	private final static class XMLParser
	{
		public static XML_User getUser(InputStream in) throws Exception
		{
			XML_List list;
			JAXBContext context = JAXBContext.newInstance(XML_List.class);
			Unmarshaller um = context.createUnmarshaller();
			list = (XML_List) um.unmarshal(in);
			return list.USER;
		}
		
		@XmlRootElement(namespace = "", name="LIST")
		private static final class XML_List
		{
			@XmlElements({
			    @XmlElement(name="BOOK", type=XML_Book.class)
			  })
			private List<XML_Book> Books = new Vector<XML_Book>();
			@XmlElement(name="USER", required=false)
			private XML_User USER;
			@XmlElement(name="ERROR", required=false)
			private XML_Error ERROR;
		}
		
		@XmlRootElement(namespace = "", name="BOOK")
		private static final class XML_Book
		{
			@XmlAttribute(name="ID", required=true)
			private String ID = "";
			@XmlAttribute(name="VER", required=true)
			private int VER;
			@XmlAttribute(name="search", required=false)
			private String search;
			
			@XmlElement(name="NAME_EN", required=false)
			private String NAME_EN;
			@XmlElement(name="NAME_JP", required=false)
			private String NAME_JP;
			@XmlElement(name="NAME_R", required=false)
			private String NAME_R;
			@XmlElements({
			    @XmlElement(name="NAME_ALT", type=String.class)
			  })
			private List<String> NAME_ALT = new Vector<String>();
			@XmlElement(name="DATE_RELEASED", required=false)
			private Date DATE_RELEASED;
			@XmlElement(name="DATA_ISBN", required=false)
			private String DATA_ISBN;
			@XmlElement(name="DATA_PAGES", required=false)
			private int DATA_PAGES;
			@XmlElement(name="DATA_AGE", required=false)
			private int DATA_AGE;
			@XmlElement(name="DATA_ANTHOLOGY", required=false)
			private int DATA_ANTHOLOGY;
			@XmlElement(name="DATA_LANGUAGE", required=false)
			private int DATA_LANGUAGE;
			@XmlElement(name="DATA_COPYSHI", required=false)
			private int DATA_COPYSHI;
			@XmlElement(name="DATA_MAGAZINE", required=false)
			private int DATA_MAGAZINE;
			@XmlElement(name="DATA_INFO", required=false)
			private String DATA_INFO;
			
			@XmlElement(required=true)
			private XML_Links LINKS;		
		}
		
		@XmlRootElement(namespace = "", name="LINKS")
		private static final class XML_Links
		{
			@XmlElements({
			    @XmlElement(name="ITEM", type=XML_Item.class)
			  })
			private List<XML_Item> Items = new Vector<XML_Item>();
		}
		
		@XmlRootElement(namespace = "", name="USER")
		private static final class XML_User
		{
			@XmlAttribute(name="id", required=true)
			private String id;
			@XmlElement(name="User", required=true)
			private String User;
			@XmlElement(name="Queries", required=true)
			private int Queries;
			@XmlElement(name="Image_Queries", required=true)
			private int Image_Queries;
		}
		
		@XmlRootElement(namespace = "", name="ITEM")
		private static final class XML_Item
		{
			@XmlAttribute(name="ID", required=true)
			private String ID;
			@XmlAttribute(name="VER", required=true)
			private int VER;
			@XmlAttribute(name="TYPE", required=true)
			private XML_Type TYPE;
			@XmlAttribute(name="PARENT", required=false)
			private String PARENT;
			@XmlAttribute(name="FRQ", required=true)
			private int FRQ;
			
			@XmlElement(name="NAME_EN", required=false)
			private String NAME_EN;
			@XmlElement(name="NAME_JP", required=false)
			private String NAME_JP;
			@XmlElement(name="NAME_R", required=false)
			private String NAME_R;
			@XmlElement(name="OBJECTS", required=false)
			private int OBJECTS;
			@XmlElements({
			    @XmlElement(name="NAME_ALT", type=String.class)
			  })
			private List<String> NAME_ALT = new Vector<String>();
			@XmlElement(name="DATE_START", required=false)
			private Date DATE_START;
			@XmlElement(name="DATE_END", required=false)
			private Date DATE_END;
			@XmlElement(name="DATA_AGE", required=false)
			private int DATA_SEX;
			@XmlElement(name="DATA_AGE", required=false)
			private String DATA_AGE;
		}
		
		private enum XML_Type
		{
			type, // UNDOCUMENTED
			circle,
			author,
			parody,
			character,
			contents,
			genre,
			convention,
			collections,
			publisher,
			imprint
		}
		
		@XmlRootElement(namespace = "", name="ERROR")
		private static final class XML_Error
		{
			@XmlAttribute(name="code", required=true)
			private int code;
			
			@XmlElement(name="TYPE", required=false)
			private String TYPE;
			@XmlElement(name="EXACT", required=false)
			private String EXACT;
			@XmlElement(name="CODE", required=false)
			private int CODE;
		}
	}
	
	@SuppressWarnings("serial")
	private final class PluginUI extends JPanel implements LayoutManager, ActionListener
	{
		private JTabbedPane tabs;
		
		private JButton buttonRefresh;
		private JLabel labelApikey;
		private JTextField textApikey;
		private JLabel labelThreshold;
		//private JTextField textThreshold;
		private JSlider sliderThreshold;
		private JLabel labelUserid;
		private JTextField textUserid;
		private JLabel labelUsername;
		private JTextField textUsername;
		private JLabel labelQueries;
		private JTextField textQueries;
		private JLabel labelImageQueries;
		private JTextField textImageQueries;
		private JCheckBox boxResizeImage;
		
		private JButton buttonAddTask;
		private JButton buttonCleanCompleted;
		private DefaultListModel<Task> listModel;
		private JList<Task> listTasks;
		private JScrollPane scrollTasks;
		private Task runningTask;
		
		public PluginUI()
		{
			super();
			super.setLayout(this);
			tabs = new JTabbedPane();
			tabs.setFont(Core.Resources.Font);
			tabs.setFocusable(false);
			JPanel bogus;
			bogus = new JPanel();
			bogus.setLayout(null);
			buttonRefresh = new JButton(IconRefresh);
			buttonRefresh.addActionListener(this);
			buttonRefresh.setBorder(null);
			buttonRefresh.setFocusable(false);
			bogus.add(buttonRefresh);
			labelApikey = new JLabel("Api Key :");
			labelApikey.setFont(Core.Resources.Font);
			bogus.add(labelApikey);
			textApikey = new JTextField(APIKEY);
			textApikey.setFont(Core.Resources.Font);
			textApikey.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void changedUpdate(DocumentEvent de)
				{
					APIKEY = textApikey.getText();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
					Core.UI.validateUI(new DouzEvent(DouzEvent.Type.SETTINGS_CHANGED, "org.dyndns.doujindb.plug.mugimugi.apikey"));
				}
				@Override
				public void insertUpdate(DocumentEvent de)
				{
					APIKEY = textApikey.getText();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
					Core.UI.validateUI(new DouzEvent(DouzEvent.Type.SETTINGS_CHANGED, "org.dyndns.doujindb.plug.mugimugi.apikey"));
				}
				@Override
				public void removeUpdate(DocumentEvent de)
				{
					APIKEY = textApikey.getText();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
					Core.UI.validateUI(new DouzEvent(DouzEvent.Type.SETTINGS_CHANGED, "org.dyndns.doujindb.plug.mugimugi.apikey"));
				}				
			});
			bogus.add(textApikey);
			labelThreshold = new JLabel("Threshold : " + THRESHOLD);
			labelThreshold.setFont(Core.Resources.Font);
			bogus.add(labelThreshold);
			sliderThreshold = new JSlider(0, 100, THRESHOLD);
			sliderThreshold.setFont(Core.Resources.Font);
			sliderThreshold.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					THRESHOLD = sliderThreshold.getValue();
					labelThreshold.setText("Threshold : " + THRESHOLD);
					if(sliderThreshold.getValueIsAdjusting())
						return;
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold").setValue(THRESHOLD);
					Core.UI.validateUI(new DouzEvent(DouzEvent.Type.SETTINGS_CHANGED, "org.dyndns.doujindb.plug.mugimugi.threshold"));
				}				
			});
			bogus.add(sliderThreshold);
			labelUserid = new JLabel("User ID :");
			labelUserid.setFont(Core.Resources.Font);
			bogus.add(labelUserid);
			textUserid = new JTextField(USERID);
			textUserid.setFont(Core.Resources.Font);
			textUserid.setEditable(false);
			bogus.add(textUserid);
			labelUsername = new JLabel("User Name :");
			labelUsername.setFont(Core.Resources.Font);
			bogus.add(labelUsername);
			textUsername = new JTextField(USERNAME);
			textUsername.setFont(Core.Resources.Font);
			textUsername.setEditable(false);
			bogus.add(textUsername);
			labelQueries = new JLabel("Queries :");
			labelQueries.setFont(Core.Resources.Font);
			bogus.add(labelQueries);
			textQueries = new JTextField("" + QUERIES);
			textQueries.setFont(Core.Resources.Font);
			textQueries.setEditable(false);
			bogus.add(textQueries);
			labelImageQueries = new JLabel("Image Queries :");
			labelImageQueries.setFont(Core.Resources.Font);
			bogus.add(labelImageQueries);
			textImageQueries = new JTextField("" + IMAGE_QUERIES);
			textImageQueries.setFont(Core.Resources.Font);
			textImageQueries.setEditable(false);
			bogus.add(textImageQueries);
			boxResizeImage = new JCheckBox("<html><body>Resize covers before uploading*<br><i>(*will speed up searches and preserve bandwidth)</i></body></html>");
			boxResizeImage.setFont(Core.Resources.Font);
			boxResizeImage.setFocusable(false);
			boxResizeImage.setSelected(RESIZE_COVER);
			boxResizeImage.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					RESIZE_COVER = boxResizeImage.isSelected();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover").setValue(RESIZE_COVER);
					Core.UI.validateUI(new DouzEvent(DouzEvent.Type.SETTINGS_CHANGED, "org.dyndns.doujindb.plug.mugimugi.resize_cover"));
				}				
			});
			bogus.add(boxResizeImage);
			tabs.addTab("Settings", IconSettings, bogus);
			
			bogus = new JPanel();
			bogus.setLayout(null);
			buttonAddTask = new JButton(IconAdd);
			buttonAddTask.addActionListener(this);
			buttonAddTask.setBorder(null);
			buttonAddTask.setFocusable(false);
			bogus.add(buttonAddTask);
			buttonCleanCompleted = new JButton(IconCleanCompleted);
			buttonCleanCompleted.addActionListener(this);
			buttonCleanCompleted.setBorder(null);
			buttonCleanCompleted.setToolTipText("Clean Completed Tasks");
			buttonCleanCompleted.setFocusable(false);
			bogus.add(buttonCleanCompleted);
			listTasks = new JList<Task>();
			listModel = new DefaultListModel<Task>();
			listTasks.setModel(listModel);
			listTasks.setCellRenderer(new TaskUI());
			listTasks.setSelectionBackground(listTasks.getSelectionForeground());
			listTasks.setSelectionForeground(Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
			listTasks.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getButton() == MouseEvent.BUTTON3)
					{
						int selectedIndex = listTasks.locationToIndex(me.getPoint());
		   				if (selectedIndex < 0)
							return;
		   				final Task task = (Task)listTasks.getModel().getElementAt(selectedIndex);
						switch(task.getStatus())
						{
						case Task.TASK_COMPLETED:
						{
							Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
							tbl.put("OK", IconCompleted);
							final DouzPopupMenu pop = new DouzPopupMenu("Task Option(s)", tbl);
							pop.show((Component)me.getSource(), me.getX(), me.getY());
							new Thread(getClass().getName()+"/MouseClicked")
							{
								@Override
								public void run()
								{
									while(pop.isValid())
										try { sleep(1); } catch (InterruptedException ie) { ; }
									int selected = pop.getResult();
									switch(selected)
									{
									case 0:{
										listModel.removeElement(task);
									}
									}
								}
							}.start();
						}
							break;
						case Task.TASK_RUNNING:
							break;
						case Task.TASK_QUEUED:
						{
							Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
							tbl.put("Stop", IconStop);
							final DouzPopupMenu pop = new DouzPopupMenu("Task Option(s)", tbl);
							pop.show((Component)me.getSource(), me.getX(), me.getY());
							new Thread(getClass().getName()+"/MouseClicked")
							{
								@Override
								public void run()
								{
									while(pop.isValid())
										try { sleep(1); } catch (InterruptedException ie) { ; }
									int selected = pop.getResult();
									switch(selected)
									{
									case 0:{
										task.status = Task.TASK_ERROR;
										task.description = "Stopped by user input.";
										task.active = false;
									}
									}
								}
							}.start();
						}
							break;
						case Task.TASK_ERROR:
						{
							Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
							tbl.put("Reset", IconReset);
							tbl.put("Remove", IconRemove);
							final DouzPopupMenu pop = new DouzPopupMenu("Task Option(s)", tbl);
							pop.show((Component)me.getSource(), me.getX(), me.getY());
							new Thread(getClass().getName()+"/MouseClicked")
							{
								@Override
								public void run()
								{
									while(pop.isValid())
										try { sleep(1); } catch (InterruptedException ie) { ; }
									int selected = pop.getResult();
									switch(selected)
									{
									case 1:
										listModel.removeElement(task);
										break;
									case 0:{
										task.status = Task.TASK_QUEUED;
										task.description = task.workpath.getName();
										task.active = false;
									}
									}
								}
							}.start();
						}
							break;
						case Task.TASK_WARNING:
						{
							Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
							tbl.put("Stop", IconStop);
							final DouzPopupMenu pop = new DouzPopupMenu("Task Option(s)", tbl);
							pop.show((Component)me.getSource(), me.getX(), me.getY());
							new Thread(getClass().getName()+"/MouseClicked")
							{
								@Override
								public void run()
								{
									while(pop.isValid())
										try { sleep(1); } catch (InterruptedException ie) { ; }
									int selected = pop.getResult();
									switch(selected)
									{
									case 0:{
										task.status = Task.TASK_ERROR;
										task.description = "Stopped by user input.";
										task.active = false;
									}
									}
								}
							}.start();
						}
							break;
						}
					 }
				  }
			});
			listTasks.addMouseListener(new MouseAdapter()
	   		{
				public void mouseClicked(MouseEvent me)
	   			{
	   				int selectedIndex = listTasks.locationToIndex(me.getPoint());
	   				if (selectedIndex < 0)
						return;
	   				if(me.getClickCount() > 1)
	   				{
	   					Task task = (Task)listTasks.getModel().getElementAt(selectedIndex);
	   					task.openDialog();
	   				}	   				
	   			}
	   		});
			listTasks.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listTasks.setSelectionBackground(listTasks.getSelectionForeground());
			listTasks.setSelectionForeground(Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
			scrollTasks = new JScrollPane(listTasks);
			bogus.add(scrollTasks);
			tabs.addTab("Tasks", IconTasks, bogus);
			super.add(tabs);
			
			new Thread(getClass().getName()+"/TaskFetcher")
			{
				@Override
				public void run()
				{
					while(true)
					{
						repaint();
						try { sleep(500); } catch (InterruptedException e) { }
						if(listModel.isEmpty())
							continue;
						if(runningTask == null)
						{
							for(int k=0;k<listModel.getSize();k++)
							{
								Task t = (Task) listModel.elementAt(k);
								if(t.getStatus() != Task.TASK_QUEUED)
									continue;
								runningTask = t;
								new Thread(runningTask, getClass().getName()+"/Task").start();
								break;
							}
						}else
							if(runningTask.isActive())
								continue;
							else
							{
								for(int k=0;k<listModel.getSize();k++)
								{
									Task t = (Task) listModel.elementAt(k);
									if(t.getStatus() != Task.TASK_QUEUED)
										continue;
									runningTask = t;
									new Thread(runningTask, getClass().getName()+"/Task").start();
									break;
								}
							}
					}
				}
			}.start();
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			tabs.setBounds(0,0,width,height);
			buttonRefresh.setBounds(1,1,20,20);
			labelApikey.setBounds(5,25,120,15);
			textApikey.setBounds(125,25,width - 130,15);
			labelThreshold.setBounds(5,25+20,120,15);
			sliderThreshold.setBounds(125,25+20,width - 130,15);
			labelUserid.setBounds(5,25+40,120,15);
			textUserid.setBounds(125,25+40,width - 130,15);
			labelUsername.setBounds(5,25+60,120,15);
			textUsername.setBounds(125,25+60,width - 130,15);
			labelQueries.setBounds(5,25+80,120,15);
			textQueries.setBounds(125,25+80,width - 130,15);
			labelImageQueries.setBounds(5,25+100,120,15);
			textImageQueries.setBounds(125,25+100,width - 130,15);
			boxResizeImage.setBounds(5,25+120,width,45);
			buttonAddTask.setBounds(1,1,20,20);
			buttonCleanCompleted.setBounds(width - 25,1,20,20);
			scrollTasks.setBounds(1,21,width - 5,height - 45);
		}
		@Override
		public void addLayoutComponent(String key,Component c){}
		@Override
		public void removeLayoutComponent(Component c){}
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return new Dimension(350,350);
		}
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
			return new Dimension(350,350);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(ae.getSource() == buttonRefresh)
			{
				textApikey.setText(APIKEY);
				sliderThreshold.setValue(THRESHOLD);
				;
				buttonRefresh.setEnabled(false);
				textApikey.setEnabled(false);
				sliderThreshold.setEnabled(false);
				buttonRefresh.setIcon(IconLoading);
				new Thread(getClass().getName()+"/ActionPerformed/Refresh")
				{
					@Override
					public void run()
					{
						XMLParser.XML_User user = null;
						try
						{
							if(APIKEY == null)
								throw new Exception("Invalid API key provided.");
							URLConnection urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/").openConnection();
							urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.this.getName() + "/" + DoujinshiDBScanner.this.getVersion()+ "; +" + DoujinshiDBScanner.this.getWeblink() + ")");
							InputStream in = new ClientHttpRequest(urlc).post();
							user = XMLParser.getUser(in);
						} catch (Exception e)
						{
							e.printStackTrace();
						}
						if(user != null)
						{
							textUserid.setText(user.id);
							textUsername.setText(user.User);
							textQueries.setText("" + user.Queries);
							textImageQueries.setText("" + user.Image_Queries);
							Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
							Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold").setValue(THRESHOLD);
						}else
						{
							textUserid.setText("");
							textUsername.setText("");
							textQueries.setText("");
							textImageQueries.setText("");
						}
						buttonRefresh.setIcon(IconRefresh);
						buttonRefresh.setEnabled(true);
						textApikey.setEnabled(true);
						sliderThreshold.setEnabled(true);
					}
				}.start();
				return;
			}
			if(ae.getSource() == buttonAddTask)
			{
				try 
				{
					JFileChooser fc = Core.UI.getFileChooser();
					int prev_option = fc.getFileSelectionMode();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setMultiSelectionEnabled(true);
					if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
					{
						fc.setFileSelectionMode(prev_option);
						return;
					}
					final File files[] = fc.getSelectedFiles();
					for(File file : files)
					{
						Task task = new Task(file);
						listModel.add(0, task);
					}
					fc.setFileSelectionMode(prev_option);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			if(ae.getSource() == buttonCleanCompleted)
			{
				Vector<Task> completed = new Vector<Task>();
				for(int i=0;i<listModel.size();i++)
				{
					Task task = listModel.get(i);
					if(task.getStatus() == Task.TASK_COMPLETED)
						completed.add(task);
				}
				for(Task task : completed)
					listModel.removeElement(task);
			}
		}
		
		@SuppressWarnings("unused")
		private final class Task implements Runnable
		{
			private String description;
			private File workpath;
			private boolean active = false;
			private double threshold;
			
			private Book importedBook;
			private String warningMessage = "";
			private String errorMessage = "";
			private JComponent epanel;
			
			public static final int TASK_RUNNING = 0x01;
			public static final int TASK_QUEUED = 0x02;
			public static final int TASK_ERROR = 0x03;
			public static final int TASK_WARNING = 0x04;
			public static final int TASK_COMPLETED = 0x05;
			private int status = TASK_QUEUED;
			
			public Task(File path)
			{
				workpath = path;
				description = workpath.getName();
				threshold = THRESHOLD;
			}
			
			public String getDescription() {
				return description;
			}
			public File getWorkpath() {
				return workpath;
			}
			public int getStatus() {
				return status;
			}
			public boolean isActive() {
				return active;
			}

			@Override
			public void run()
			{
				active = true;
				status = TASK_RUNNING;
				description = "";
				try
				{
					if(APIKEY == null)
					{
						description = "Invalid API key provided.";
						status = TASK_ERROR;
						throw new Exception("Invalid API key provided.");
					}
					description = "Searching for cover image ...";
					File cover_image = findFirstFile(workpath);
					if(cover_image == null)
					{
						description = "Cover image not found (Double-click to open folder).";
						status = TASK_ERROR;
						epanel = new JPanel();
						epanel.setSize(240, 400);
						JLabel l = new JLabel("<html><body style='margin:5px'>" +
							"A suitable cover file was not found in the provided folder." +
							"<br>Press Ok to open the folder in your Desktop Environment." +
							"</body></html>");
						JPanel bottom = new JPanel();
						bottom.setLayout(new BorderLayout(5, 5));
						bottom.add(l, BorderLayout.CENTER);
						bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
						JButton ok = new JButton("Ok");
						ok.setFont(Core.Resources.Font);
						ok.setMnemonic('O');
						ok.setFocusable(false);
						ok.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent ae) 
							{
								try
								{
									Desktop desktop = Desktop.getDesktop();
									desktop.open(workpath);
								} catch (IOException ioe) { }
								DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
								window.dispose();
							}					
						});
						bottom.add(ok, BorderLayout.SOUTH);
						epanel.add(bottom);
						throw new Exception("Cover image not found.");
					}
					File cover_image2 = File.createTempFile("" + new java.util.Date().getTime(), ".jpg");
					cover_image2.deleteOnExit();
					BufferedImage resized;
					{
						BufferedImage image = javax.imageio.ImageIO.read(cover_image);
						BufferedImage dest;
						description = "Resizing cover image ...";
						if(image.getWidth() > image.getHeight())
							dest = new BufferedImage(image.getWidth() / 2, image.getHeight(), BufferedImage.TYPE_INT_RGB);
						else
							dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
						Graphics g = dest.getGraphics();
						g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
						g.dispose();
						if(RESIZE_COVER)
						try
						{
							description = "Resizing image before upload  ...";
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
							description = e.getMessage();
							status = TASK_ERROR;
							throw new Exception(e.getMessage());
						}else
						{
							resized = dest;
						}
						javax.imageio.ImageIO.write(resized, "PNG", cover_image2);
					}
					URLConnection urlc;
					urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/?S=imageSearch").openConnection();
					urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.this.getName() + "/" + DoujinshiDBScanner.this.getVersion()+ "; +" + DoujinshiDBScanner.this.getWeblink() + ")");
					description = "Sending cover image to doujinshi.mugimugi.org ...";
					InputStream in = new ClientHttpRequest(urlc).post(
				              new Object[] {
				            	  "img", cover_image2
				            	  });
					description = "Parsing XML response ...";
					{
						XMLParser.XML_List list;
						try
						{
							JAXBContext context = JAXBContext.newInstance(XMLParser.XML_List.class);
							Unmarshaller um = context.createUnmarshaller();
							list = (XMLParser.XML_List) um.unmarshal(in);
							if(list.ERROR != null)
							{
								description = "Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")";
								status = TASK_ERROR;
								epanel = new JPanel();
								epanel.setSize(240, 400);
								JLabel l = new JLabel("<html><body style='margin:5px'>" +
									"This item was not added to the Database." +
									"<br>Press Ok to open the folder in your Desktop Environment." +
									"</body></html>");
								JPanel bottom = new JPanel();
								bottom.setLayout(new BorderLayout(5, 5));
								bottom.add(l, BorderLayout.CENTER);
								bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
								JButton ok = new JButton("Ok");
								ok.setFont(Core.Resources.Font);
								ok.setMnemonic('O');
								ok.setFocusable(false);
								ok.addActionListener(new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent ae) 
									{
										try
										{
											Desktop desktop = Desktop.getDesktop();
											desktop.open(workpath);
										} catch (IOException ioe) { }
										DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
										window.dispose();
									}					
								});
								bottom.add(ok, BorderLayout.SOUTH);
								epanel.add(bottom);
								throw new Exception("Server returned Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
							}
							XMLParser.XML_User user = list.USER;
							if(user != null)
							{
								textUserid.setText(user.id);
								textUsername.setText(user.User);
								textQueries.setText("" + user.Queries);
								textImageQueries.setText("" + user.Image_Queries);
							}else
							{
								textUserid.setText("");
								textUsername.setText("");
								textQueries.setText("");
								textImageQueries.setText("");
							}
								HashMap<Double, XMLParser.XML_Book> books = new HashMap<Double, XMLParser.XML_Book>();
								double better_result = 0;
								String result_string = "";
								for(XMLParser.XML_Book book : list.Books)
								{
									double result = Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", "."));
									books.put(result, book);
									if(result > better_result)
									{
										better_result = result;
										result_string = book.search;
									}										
								}
								final String result_star = result_string;
								if(threshold > better_result)
								{
									description = "No query matched the threshold (Double-click for more info).";
									status = TASK_ERROR;
									epanel = new JPanel();
									epanel.setLayout(new BorderLayout(5, 5));
									if(!RESIZE_COVER)
										try
										{
											BufferedImage resized2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
											int wi = resized.getWidth(null),
											hi = resized.getHeight(null),
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
											resized2 = org.dyndns.doujindb.util.Image.getScaledInstance(resized, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
											resized = resized2;
										} catch (Exception e) {
											description = e.getMessage();
											status = TASK_ERROR;
											throw new Exception(e.getMessage());
										}
									epanel.add(new JLabel(new ImageIcon(resized)), BorderLayout.WEST);
									JTabbedPane tabs = new JTabbedPane();
									for(XMLParser.XML_Book book : books.values())
									{
										final int bid = Integer.parseInt(book.ID.substring(1));
										final URI uri = new URI("http://doujinshi.mugimugi.org/book/" + bid + "/");
										final JLabel cover = new JLabel(new ImageIcon());
										new Thread(getClass().getName()+"/ImageURL/Download")
										{
											@Override
											public void run()
											{
												try
												{
													URL thumbURL = new URL("http://img.mugimugi.org/tn/" + (int)Math.floor((double)bid/(double)2000) + "/" + bid + ".jpg");
													ImageIcon img = new ImageIcon(thumbURL);
													cover.setIcon(img);
												}catch(Exception e){ e.printStackTrace(); }
											}
										}.start();
										final JButton link = new JButton("http://doujinshidb/" + bid);
										link.setFont(Core.Resources.Font);
										link.setFocusable(false);
										link.addActionListener(new ActionListener()
										{
											@Override
											public void actionPerformed(ActionEvent ae) 
											{
												try
												{
													Desktop desktop = Desktop.getDesktop();
													desktop.browse(uri);
												} catch (IOException ioe) { }
												DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
												window.dispose();
											}					
										});
										JPanel panel = new JPanel();
										panel.setLayout(new LayoutManager()
										{

											@Override
											public void addLayoutComponent(String name, Component comp) { }

											@Override
											public void layoutContainer(Container comp)
											{
												int width = comp.getWidth(), height = comp.getHeight();
												link.setBounds(1,1,width-2,15);
												cover.setBounds(1,16,width-2,height-18);
											}

											@Override
											public Dimension minimumLayoutSize(Container comp) { return new Dimension(250,250); }

											@Override
											public Dimension preferredLayoutSize(Container comp) { return new Dimension(250,250); }

											@Override
											public void removeLayoutComponent(Component comp){}
											
										});
										panel.add(link);
										panel.add(cover);
										if(book.search.equals(result_star))
											tabs.addTab("" + book.search, IconStar, panel);
										else
											tabs.addTab("" + book.search, panel);
									}
									epanel.add(tabs, BorderLayout.EAST);
									JPanel bottom = new JPanel();
									bottom.setLayout(new BorderLayout(5, 5));
									final JCheckBox check = new JCheckBox("Set Threshold value = " + result_star);
									check.setFont(Core.Resources.Font);
									check.setFocusable(false);
									bottom.add(check, BorderLayout.NORTH);
									JButton ok = new JButton("Ok");
									ok.setFont(Core.Resources.Font);
									ok.setMnemonic('O');
									ok.setFocusable(false);
									ok.addActionListener(new ActionListener()
									{
										@Override
										public void actionPerformed(ActionEvent ae) 
										{
											if(check.isSelected())
												threshold = Double.parseDouble(result_star.replaceAll("%", "").replaceAll(",", "."));
											DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
											window.dispose();
										}					
									});
									bottom.add(ok, BorderLayout.SOUTH);
									epanel.add(bottom, BorderLayout.SOUTH);
									throw new Exception("No query matched the threshold.");
								}
							try
							{
								XMLParser.XML_Book xmlbook  = books.get(better_result);
								Book book = Context.doInsert(Book.class);
								book.setJapaneseName(xmlbook.NAME_JP);
								book.setTranslatedName(xmlbook.NAME_EN);
								book.setRomanjiName(xmlbook.NAME_R);
								book.setDate(xmlbook.DATE_RELEASED);
								book.setPages(xmlbook.DATA_PAGES);
								book.setAdult(xmlbook.DATA_AGE == 1);
								book.setDecensored(false);
								book.setTranslated(false);
								book.setColored(false);
								book.setRating(Rating.UNRATED);
								book.setInfo(xmlbook.DATA_INFO);
								
								RecordSet<Artist> artists = Context.getArtists(null);
								RecordSet<Circle> circles = Context.getCircles(null);
								RecordSet<Parody> parodies = Context.getParodies(null);
								RecordSet<Content> contents = Context.getContents(null);
								RecordSet<Convention> conventions = Context.getConventions(null);
								
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
														(artist.getRomanjiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
													{
														book.addArtist(artist);
														alink.put(xmlitem.ID, artist);
														break _case;
													}
												Artist a = Context.doInsert(Artist.class);
												a.setJapaneseName(xmlitem.NAME_JP);
												a.setTranslatedName(xmlitem.NAME_EN);
												a.setRomanjiName(xmlitem.NAME_R);
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
															(circle.getRomanjiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
													{
														// book.addCircle(circle);
														clink.put(xmlitem.ID, circle);
														break _case;
													}
												Circle c = Context.doInsert(Circle.class);
												c.setJapaneseName(xmlitem.NAME_JP);
												c.setTranslatedName(xmlitem.NAME_EN);
												c.setRomanjiName(xmlitem.NAME_R);
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
												Content cn = Context.doInsert(Content.class);
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
												Convention cv = Context.doInsert(Convention.class);
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
														(parody.getRomanjiName().equals(xmlitem.NAME_R) && (!xmlitem.NAME_R.equals(""))))
												{
													book.addParody(parody);
													break _case;
												}
											Parody p = Context.doInsert(Parody.class);
											p.setJapaneseName(xmlitem.NAME_JP);
											p.setTranslatedName(xmlitem.NAME_EN);
											p.setRomanjiName(xmlitem.NAME_R);
											book.addParody(p);
											}
											break;
										case publisher:
											break;
										}
									}catch(Exception e) { e.printStackTrace(); }
								}
								
								Context.doCommit();
								
								if(alink.size() > 0 && clink.size() > 0)
								{
									String[] ckeys = (String[]) clink.keySet().toArray(new String[0]);
									String[] akeys = (String[]) alink.keySet().toArray(new String[0]);
									String ids = ckeys[0];
									for(int i=1;i<ckeys.length;i++)
										ids += ckeys[i] + ",";
									urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/?S=getID&ID=" + ids + "").openConnection();
									urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.this.getName() + "/" + DoujinshiDBScanner.this.getVersion()+ "; +" + DoujinshiDBScanner.this.getWeblink() + ")");
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
								
								Context.doCommit();
								
								importedBook = book;
								if(importedBook == null)
								{
									description = "Error parsing XML data.";
									status = TASK_ERROR;
									throw new Exception("Error parsing XML data.");
								}
								
								for(Book book_ : Context.getBooks(null))
									if(importedBook.getJapaneseName().equals(book_.getJapaneseName()) && !importedBook.getID().equals(book_.getID()))
									{
										status = TASK_WARNING;
										warningMessage = "Possible duplicate item detected [ID='"+book_.getID()+"']."; //FIXME When detecting multiple dupes???
									}
							} catch (Exception e) {
								e.printStackTrace();
								description = e.getMessage();
								status = TASK_ERROR;
								throw new Exception(e.getMessage());
							}
						} catch (Exception e) {
							e.printStackTrace();
							status = TASK_ERROR;
							throw new Exception(e.getMessage());
						}
					}
					description = "Copying files into the Datastore ...";
					for(File file : workpath.listFiles())
						fileCopy(file, Core.Repository.child(importedBook.getID()));
					try
					{
						description = "Creating preview into the Datastore  ...";
						DataFile ds = Core.Repository.child(importedBook.getID());
						ds.mkdir();
						ds = Core.Repository.getPreview(importedBook.getID());
						ds.touch();
						OutputStream out = ds.getOutputStream();
						BufferedImage image = javax.imageio.ImageIO.read(cover_image2);
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
						if(status == TASK_WARNING)
						{
							description = "Doujin successfully imported (" + warningMessage + ")";
						}else{
							status = TASK_COMPLETED;
							description = "Doujin successfully imported.";
						}
					} catch (Exception e) {
						e.printStackTrace();
						description = e.getMessage();
						status = TASK_ERROR;
					}
				} catch (Exception e) {
					e.printStackTrace();
					status = TASK_ERROR;
				}
				active = false;
			}
			
			public double getThreshold() {
				return threshold;
			}

			public void setThreshold(double threshold) {
				this.threshold = threshold;
			}

			private File findFirstFile(File directory)
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
						return findFirstFile(file);
				return null;
			}
			
			private void fileCopy(File file, DataFile ds) throws IOException
			{
				DataFile dst = ds.child(file.getName());
				if(file.isDirectory())
				{
					dst.mkdirs();
					for(File f : file.listFiles())
						fileCopy(f, dst);
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
			
			private void openDialog()
			{
				switch(status)
				{
				case TASK_COMPLETED:
					try {
						QueryBook query = new QueryBook();
						query.ID = importedBook.getID();
						RecordSet<Book> books = Core.Database.getBooks(query);
						for(Book b : books)
							if(b.getID().equals(importedBook.getID()))
								Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, b);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					break;
				case TASK_RUNNING:
					break;
				case TASK_QUEUED:
					break;
				case TASK_ERROR:
					try
					{ 
						try { Core.UI.Desktop.showDialog(epanel, IconError, "Error - " + description.replaceAll(" \\(.*","")); } catch (PropertyVetoException e) { }
					} catch (NullPointerException npe) { }
					break;
				case TASK_WARNING:
					try {
						QueryBook query = new QueryBook();
						query.ID = importedBook.getID();
						RecordSet<Book> books = Core.Database.getBooks(query);
						for(Book b : books)
							if(b.getID().equals(importedBook.getID()))
									Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, b);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					break;
				}				
			}
		}
		
		private class TaskUI extends JPanel implements ListCellRenderer<Task>, LayoutManager
		{
			private JLabel Status;
			private JLabel Icon;
			
			public TaskUI()
			{
				super();
				setLayout(this);
				Status = new JLabel();
				Status.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
				Status.setOpaque(true);
				super.add(Status);
				Icon = new JLabel();
				super.add(Icon);
				setBackground(UIManager.getColor("List.textBackground"));
				setForeground(UIManager.getColor("List.textForeground"));
			}
			@Override
			public Component getListCellRendererComponent(JList<? extends Task> listBox, Task obj, int currentindex, boolean isChecked, boolean hasFocus)
			{
				Task task = (Task) obj;
				Status.setText(task.getDescription());
				switch(task.getStatus())
				{
				case Task.TASK_RUNNING:
					Icon.setIcon(IconRunning);
					break;
				case Task.TASK_COMPLETED:
					Icon.setIcon(IconCompleted);
					break;
				case Task.TASK_ERROR:
					Icon.setIcon(IconError);
					break;
				case Task.TASK_QUEUED:
					Icon.setIcon(IconQueued);
					break;
				case Task.TASK_WARNING:
					Icon.setIcon(IconWarning);
					break;
				}
				return this;
			}
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				Status.setBounds(5, 0, width - 25, 20);
				Icon.setBounds(width - 20, 0, 20, 20);
			}
			@Override
			public void addLayoutComponent(String key,Component c){}
			@Override
			public void removeLayoutComponent(Component c){}
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
				return new Dimension(150,20);
			}
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				return new Dimension(150,20);
			}
		}
	}
}
