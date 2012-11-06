package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.plug.impl.mugimugi.Task.State;
import org.dyndns.doujindb.plug.impl.mugimugi.Task.Step;
import org.dyndns.doujindb.plug.impl.mugimugi.rc.Resources;
import org.dyndns.doujindb.ui.desk.WindowEx;

import javax.xml.bind.*;

/**  
* DoujinshiDBScanner.java - Plugin to batch process media files thanks to the DoujinshiDB project APIs.
* @author  nozomu
* @version 1.0
*/
public final class DoujinshiDBScanner extends Plugin
{
	static String APIKEY = "";
	static int THRESHOLD = 75;
	static boolean RESIZE_COVER = false;
	static int QUERIES;
	static int IMAGE_QUERIES;
	static String USERID;
	static String USERNAME;
	
	private static Resources Resources = new Resources();
	
	static final String Author = "Nozomu";
	static final String Version = "1.1";
	static final String Weblink = "http://code.google.com/p/doujindb/";
	static final String Name = "DoujinshiDB Scanner";
	static final String Description = "The DoujinshiDB plugin lets you batch process media files thanks to DoujinshiDB API.";
	static final ImageIcon Icon = new ImageIcon(DoujinshiDBScanner.class.getResource("rc/icons/plugin.png"));
	
	static final String UUID = "{CB123239-06D1-4FB6-A4CC-05C4B436DF73}";
	static final File PLUGIN_HOME = new File(System.getProperty("doujindb.home"),  "plugins/" + UUID);
	static final DataBaseContext Context = Core.Database.getContext(UUID);
	
	private static final JComponent UI = new PluginUI();
	static XMLParser.XML_User User = new XMLParser.XML_User();
	
	static
	{
		Property prop;
		if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.apikey"))
			APIKEY = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").asString();
		else
			Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.apikey");
		{
			prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey");
			prop.setValue(APIKEY);
			prop.setDescription("<html><body>Apikey used to query the doujinshidb database.</body></html>");
		}	


		if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.threshold"))
			THRESHOLD = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold").asNumber();
		else
			Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.threshold");
		{
			prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold");
			prop.setValue(THRESHOLD);
			prop.setDescription("<html><body>Threshold limit for matching cover queries.</body></html>");
		}	


		if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.resize_cover"))
			RESIZE_COVER = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover").asBoolean();
		else
			Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.resize_cover");
		{
			prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover");
			prop.setValue(RESIZE_COVER);
			prop.setDescription("<html><body>Whether to resize covers before uploading them.</body></html>");
		}
		
		File file = new File(System.getProperty("doujindb.home"), "plugins/" + UUID);
		file = new File(file, ".cache");
		file.mkdirs();
	}
	
	@Override
	public String getUUID() {
		return UUID;
	}
	
	@Override
	public Icon getIcon() {
		return Icon;
	}
	
	@Override
	public String getName() {
		return Name;
	}
	
	@Override
	public String getDescription() {
		return Description;
	}
	
	@Override
	public String getVersion() {
		return Version;
	}
	
	@Override
	public String getAuthor() {
		return Author;
	}
	
	@Override
	public String getWeblink() {
		return Weblink;
	}
	
	@Override
	public JComponent getUI() {
		return UI;
	}
	
	@SuppressWarnings("serial")
	private static final class PluginUI extends JPanel implements LayoutManager, ActionListener
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
		private JButton buttonDeleteSelected;
		private JButton buttonCleanCompleted;
		private JButton buttonWorkerResume;
		private JButton buttonWorkerPause;
		private JCheckBox bottonSelection;
		private PanelTaskUI panelTasks;
		private JScrollPane scrollTasks;
		
		private Thread workerThread = new Thread(getClass().getName()+"$Task[null]");
		private boolean fetcherRunning = false;
		private Thread fetcherThread;
		
		static
		{
			Property prop;
			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.apikey"))
				APIKEY = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").asString();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.apikey");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey");
				prop.setValue(APIKEY);
				prop.setDescription("<html><body>Apikey used to query the doujinshidb database.</body></html>");
			}	


			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.threshold"))
				THRESHOLD = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold").asNumber();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.threshold");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.threshold");
				prop.setValue(THRESHOLD);
				prop.setDescription("<html><body>Threshold limit for matching cover queries.</body></html>");
			}	


			if(Core.Properties.contains("org.dyndns.doujindb.plug.mugimugi.resize_cover"))
				RESIZE_COVER = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover").asBoolean();
			else
				Core.Properties.add("org.dyndns.doujindb.plug.mugimugi.resize_cover");
			{
				prop = Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.resize_cover");
				prop.setValue(RESIZE_COVER);
				prop.setDescription("<html><body>Whether to resize covers before uploading them.</body></html>");
			}
			
			File file = new File(System.getProperty("doujindb.home"), "plugins/" + UUID);
			file = new File(file, ".cache");
			file.mkdirs();
		}
		
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
			buttonRefresh = new JButton(Resources.Icons.get("Plugin/Refresh"));
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
					Core.UI.propertyUpdated("org.dyndns.doujindb.plug.mugimugi.apikey");
				}
				@Override
				public void insertUpdate(DocumentEvent de)
				{
					APIKEY = textApikey.getText();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
					Core.UI.propertyUpdated("org.dyndns.doujindb.plug.mugimugi.apikey");
				}
				@Override
				public void removeUpdate(DocumentEvent de)
				{
					APIKEY = textApikey.getText();
					Core.Properties.get("org.dyndns.doujindb.plug.mugimugi.apikey").setValue(APIKEY);
					Core.UI.propertyUpdated("org.dyndns.doujindb.plug.mugimugi.apikey");
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
					Core.UI.propertyUpdated("org.dyndns.doujindb.plug.mugimugi.threshold");
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
					Core.UI.propertyUpdated("org.dyndns.doujindb.plug.mugimugi.resize_cover");
				}				
			});
			bogus.add(boxResizeImage);
			tabs.addTab("Settings", Resources.Icons.get("Plugin/Settings"), bogus);
			
			bogus = new JPanel();
			bogus.setLayout(null);
			buttonAddTask = new JButton(Resources.Icons.get("Plugin/Add"));
			buttonAddTask.addActionListener(this);
			buttonAddTask.setBorder(null);
			buttonAddTask.setFocusable(false);
			bogus.add(buttonAddTask);
			buttonCleanCompleted = new JButton(Resources.Icons.get("Plugin/CleanCompleted"));
			buttonCleanCompleted.addActionListener(this);
			buttonCleanCompleted.setBorder(null);
			buttonCleanCompleted.setToolTipText("Clean Completed");
			buttonCleanCompleted.setFocusable(false);
			bogus.add(buttonCleanCompleted);
			buttonDeleteSelected = new JButton(Resources.Icons.get("Plugin/DeleteSelected"));
			buttonDeleteSelected.addActionListener(this);
			buttonDeleteSelected.setBorder(null);
			buttonDeleteSelected.setToolTipText("Delete Selected");
			buttonDeleteSelected.setFocusable(false);
			bogus.add(buttonDeleteSelected);
			buttonWorkerResume = new JButton(Resources.Icons.get("Plugin/Task/Resume"));
			buttonWorkerResume.addActionListener(this);
			buttonWorkerResume.setBorder(null);
			buttonWorkerResume.setToolTipText("Resume Worker");
			buttonWorkerResume.setFocusable(false);
			bogus.add(buttonWorkerResume);
			buttonWorkerPause = new JButton(Resources.Icons.get("Plugin/Task/Pause"));
			buttonWorkerPause.addActionListener(this);
			buttonWorkerPause.setBorder(null);
			buttonWorkerPause.setToolTipText("Pause Worker");
			buttonWorkerPause.setFocusable(false);
			bogus.add(buttonWorkerPause);
			bottonSelection = new JCheckBox();
			bottonSelection.setSelected(false);
			bottonSelection.addActionListener(this);
			bogus.add(bottonSelection);
			panelTasks = new PanelTaskUI();
			scrollTasks = new JScrollPane(panelTasks);
			bogus.add(scrollTasks);
			tabs.addTab("Tasks", Resources.Icons.get("Plugin/Tasks"), bogus);
			super.add(tabs);
			
			Set<Task> tasks = unserialize();
			for(Task task : tasks)
				panelTasks.addTask(task);
			
			Runtime.getRuntime().addShutdownHook(new Thread(getClass().getName()+"$TaskSerializer")
			{
				@Override
				public void run()
				{
					Set<Task> tasks = new HashSet<Task>();
					for(Task task : panelTasks)
						tasks.add(task);
					serialize(tasks);
				}
			});
			
			fetcherThread = new Thread(getClass().getName()+"$TaskFetcher")
			{
				@Override
				public void run()
				{
					while(true)
					{
						/**
						 * Put the sleep() here and not at the end so
						 * it doesn't get skipped by calling those 'continue'
						 */
						try { sleep(1000); } catch (InterruptedException e) { }
						
						if(!fetcherRunning)
							continue;
						if(panelTasks.countTasks() < 1)
							continue;
						if(workerThread.isAlive())
							continue;
						else
						{
							for(Task task : panelTasks)
							{
								if(task.isDone())
									continue;
								workerThread = new Thread(task, getClass().getName()+"$Task[id:" + task.getId() + "]");
								workerThread.start();
								break;
							}
						}
					}
				}
			};
			fetcherThread.start();
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
			buttonCleanCompleted.setBounds(width - 65,1,20,20);
			buttonDeleteSelected.setBounds(width - 85,1,20,20);
			if(fetcherRunning)
			{
				buttonWorkerResume.setBounds(21,1,0,0);
				buttonWorkerPause.setBounds(21,1,20,20);
			} else {
				buttonWorkerResume.setBounds(21,1,20,20);
				buttonWorkerPause.setBounds(21,1,0,0);
			}
			bottonSelection.setBounds(width - 45,1,20,20);
			scrollTasks.setBounds(1,21,width - 5,height - 45);
			if(User != null)
			{
				textUserid.setText(User.id);
				textUsername.setText(User.User);
				textQueries.setText("" + User.Queries);
				textImageQueries.setText("" + User.Image_Queries);
			}else
			{
				textUserid.setText("");
				textUsername.setText("");
				textQueries.setText("");
				textImageQueries.setText("");
			}
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
				buttonRefresh.setIcon(Resources.Icons.get("Plugin/Loading"));
				new Thread(getClass().getName()+"$ActionPerformed/Refresh")
				{
					@Override
					public void run()
					{
						try
						{
							if(APIKEY == null)
								throw new Exception("Invalid API key provided.");
							URLConnection urlc = new java.net.URL("http://doujinshi.mugimugi.org/api/" + APIKEY + "/").openConnection();
							urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; " + DoujinshiDBScanner.Name + "/" + DoujinshiDBScanner.Version+ "; +" + DoujinshiDBScanner.Weblink + ")");
							InputStream in = new ClientHttpRequest(urlc).post();
							User = XMLParser.parseUser(in);
						} catch (Exception e)
						{
							e.printStackTrace();
						}
						buttonRefresh.setIcon(Resources.Icons.get("Plugin/Refresh"));
						buttonRefresh.setEnabled(true);
						textApikey.setEnabled(true);
						sliderThreshold.setEnabled(true);
						doLayout();
						validate();
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
						String ID = java.util.UUID.randomUUID().toString();
						while(panelTasks.containsTask(ID))
							ID = java.util.UUID.randomUUID().toString();
						Task task = new Task(ID, file);
						panelTasks.addTask(task);
					}
					panelTasks.doLayout();
					fc.setFileSelectionMode(prev_option);
				} catch (Exception e) {
					panelTasks.doLayout();
					e.printStackTrace();
				}
				return;
			}
			if(ae.getSource() == buttonCleanCompleted)
			{
				for(Task task : panelTasks.getCompletedTasks())
				{
					panelTasks.removeTask(task);
				}
				if(panelTasks.countTasks() < 1)
					bottonSelection.setSelected(false);
				return;
			}
			if(ae.getSource() == buttonDeleteSelected)
			{
				for(Task task : panelTasks.getSelectedTasks())
				{
					panelTasks.removeTask(task);
				}
				if(panelTasks.countTasks() < 1)
					bottonSelection.setSelected(false);
				return;
			}
			if(ae.getSource() == buttonWorkerResume)
			{
				fetcherRunning = true;
				doLayout();
				return;
			}
			if(ae.getSource() == buttonWorkerPause)
			{
				fetcherRunning = false;
				doLayout();
				return;
			}
			if(ae.getSource() == bottonSelection)
			{
				for(TaskUI ui : panelTasks.map.values())
					ui.bottonSelection.setSelected(bottonSelection.isSelected());
				return;
			}
		}
		
		@SuppressWarnings("unused")
		private final class PanelTaskUI extends JPanel implements LayoutManager, Iterable<Task> 
		{
			private final Map<Task, TaskUI> map = new HashMap<Task, TaskUI>();
			
			public PanelTaskUI()
			{
				super();
				super.setLayout(this);
			}

			public void addTask(Task task) {
				TaskUI taskui = new TaskUI(task);
				map.put(task, taskui);
				add(taskui);
				revalidate();
				doLayout();
			}
			
			public void removeTask(Task task) {
				remove(map.remove(task));
				doLayout();
			}
			
			public boolean containsTask(Task task) {
				return map.keySet().contains(task);
			}
			
			public boolean containsTask(String taskid) {
				return map.keySet().contains(taskid);
			}
			
			public int countTasks() {
				return map.size();
			}
			
			public List<Task> getSelectedTasks() {
				Vector<Task> selected = new Vector<Task>();
				for(Task task : map.keySet())
				{
					TaskUI ui = map.get(task);
					if(ui.bottonSelection.isSelected())
						selected.add(task);
				}
				return selected;
			}
			
			public List<Task> getCompletedTasks() {
				Vector<Task> completed = new Vector<Task>();
				for(Task task : map.keySet())
				{
					boolean iscompleted = true;
					for(Step step : Step.values())
					{
						State status = task.getStatus(step);
						if(!status.equals(Task.State.COMPLETED))
						{
							iscompleted = false;
							break;
						}
					}
					if(iscompleted)
						completed.add(task);
				}
				return completed;
			}
			
			@Override
			public Iterator<Task> iterator() {
				return map.keySet().iterator();
			}

			@Override
			public void layoutContainer(Container parent)
			{
				int height = 0;
				int posy = 0;
				int width = parent.getWidth();
				for(TaskUI ui : map.values())
				{
					ui.setBounds(0, posy, width, ui.getHeight());
					posy += ui.getHeight();
					height += ui.getHeight();
				}
				panelTasks.setPreferredSize(new Dimension(250, height));
				panelTasks.repaint();
				scrollTasks.doLayout();
			}
			@Override
			public void addLayoutComponent(String key,Component c){}
			@Override
			public void removeLayoutComponent(Component c){}
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
			     return parent.getMinimumSize();
			}
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				int height = 0;
				for(TaskUI ui : map.values())
					height += ui.getHeight();
				return new Dimension(250, height);
			}
		}
		
		private final class TaskUI extends JPanel implements LayoutManager, ActionListener, MouseListener, TaskListener
		{
			private Task task;
			private JLabel titleBar;
			private JLabel imagePreview;
			private JButton buttonToggle;
			private JButton buttonLink;
			private JButton buttonFolder;
			private JButton buttonRerun;
			private JCheckBox bottonSelection;
			private Map<Double, JButton> buttonResults;
			private String resultId;
			private Map<Task.Step, JLabel> steps;
			private final Map<Task.State, ImageIcon> icons = new HashMap<Task.State, ImageIcon>();
			
			{
				icons.put(State.IDLE, Resources.Icons.get("Plugin/Task/Step/Idle"));
				icons.put(State.RUNNING, Resources.Icons.get("Plugin/Task/Step/Running"));
				icons.put(State.ERROR, Resources.Icons.get("Plugin/Task/Step/Error"));
				icons.put(State.WARNING, Resources.Icons.get("Plugin/Task/Step/Warning"));
				icons.put(State.COMPLETED, Resources.Icons.get("Plugin/Task/Step/Completed"));
			}

			private final int STATUS_MINIMIZED = 0x1;
			private final int STATUS_MAXIMIZED = 0x2;
			private int STATUS = STATUS_MINIMIZED;
			private ImageIcon ICON_CHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Checked");
			private ImageIcon ICON_UNCHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Unchecked");
			
			public TaskUI(Task task)
			{
				super();
				setLayout(this);
				setSize(100, 20);
				setMinimumSize(new Dimension(100, 20));
				setPreferredSize(new Dimension(280, 280));
				setMaximumSize(new Dimension(1280, 280));
				setBackground(UIManager.getColor("List.textBackground"));
				setForeground(UIManager.getColor("List.textForeground"));
				setPreferredSize(new Dimension(280, 20));
				titleBar = new JLabel();
				titleBar.setText(task.getMessage());
				titleBar.setIcon(icons.get(task.getStatus(task.getStep())));
				add(titleBar);
				imagePreview = new JLabel();
				imagePreview.setIcon(Resources.Icons.get("Plugin/Task/Preview/Missing"));
				add(imagePreview);
				buttonToggle = new JButton(ICON_CHECKED);
				buttonToggle.setSelected(true);
				buttonToggle.addActionListener(this);
				add(buttonToggle);
				buttonLink = new JButton(Resources.Icons.get("Plugin/Task/Book"));
				buttonLink.setFocusable(false);
				buttonLink.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) {
						String bookid = TaskUI.this.task.getBook();
						if(bookid != null)
						{
							QueryBook qid = new QueryBook();
							qid.ID = bookid;
							RecordSet<Book> set = Core.Database.getBooks(qid);
							if(set.size() == 1)
								Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
						}
					}
				});
				add(buttonLink);
				buttonRerun = new JButton(Resources.Icons.get("Plugin/Task/Rerun"));
				buttonRerun.setFocusable(false);
				buttonRerun.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) {
							TaskUI.this.task.setBook(resultId);
							TaskUI.this.task.setDone(false);
							doLayout();
							validate();
					}
				});
				add(buttonRerun);
				buttonFolder = new JButton(Resources.Icons.get("Plugin/Task/Folder"));
				buttonFolder.setFocusable(false);
				buttonFolder.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) {
						File workpath = TaskUI.this.task.getWorkpath();
						try {
							URI uri = workpath.toURI();
							Desktop.getDesktop().browse(uri);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				});
				add(buttonFolder);
				bottonSelection = new JCheckBox();
				bottonSelection.setSelected(false);
				add(bottonSelection);
				steps = new TreeMap<Task.Step, JLabel>();
				for(Task.Step step : task.getSteps())
				{
					JLabel labelStep = new JLabel();
					labelStep.setFont(Core.Resources.Font);
					labelStep.setText("" + step);
					labelStep.setIcon(icons.get(task.getStatus(step)));
					steps.put(step, labelStep);
					add(labelStep);
				}
				/**
				 * Check if cover image is ready to be shown
				 */
				if(task.getStatus(Step.SCAN).equals(State.COMPLETED))
				{
					try {
						imagePreview.setIcon(
							new ImageIcon(
								javax.imageio.ImageIO.read(
									new File(DoujinshiDBScanner.PLUGIN_HOME, task.getId() + ".png"))));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				if(task.getStatus(Step.PARSE).equals(State.WARNING))
				{
					Map<String, XMLParser.XML_Book> results = task.getResults();
					buttonResults = new TreeMap<Double, JButton>(Collections.reverseOrder());
					for(String id : results.keySet())
					{
						XMLParser.XML_Book book = results.get(id);
						JButton buttonResult = new JButton();
						try {
							buttonResult.setIcon(new ImageIcon(new File(new File(DoujinshiDBScanner.PLUGIN_HOME, ".cache"), book.ID + ".png").toURI().toURL()));
						} catch (MalformedURLException murle) {
							buttonResult.setIcon(new ImageIcon());
						}
						buttonResult.setText("");
						buttonResult.addActionListener(this);
						buttonResult.addMouseListener(this);
						buttonResult.setActionCommand("setResult:" + book.ID);
						buttonResult.setFocusable(false);
						buttonResult.setPreferredSize(new Dimension(25, 155));
						buttonResults.put(Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", ".")),
								buttonResult);
						add(buttonResult);
					}
					// First result is already expanded
					JButton firstResult = buttonResults.values().iterator().next();
					firstResult.setPreferredSize(new Dimension(110, 155));
					resultId = firstResult.getActionCommand().substring(firstResult.getActionCommand().indexOf(':') + 1);
				}
				this.task = task;
				this.task.addTaskListener(this);
			}

			@SuppressWarnings("unused")
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth(),
					height = parent.getHeight();
				bottonSelection.setBounds(width - 40, 0, 20, 20);
				buttonToggle.setBounds(width - 20, 0, 20, 20);
				buttonFolder.setBounds(width - 60, 0, 20, 20);
				if(task.getBook() != null)
					buttonLink.setBounds(width - 80, 0, 20, 20);
				else
					buttonLink.setBounds(width - 80, 0, 0, 0);
				if(task.getStatus(Step.PARSE).equals(State.WARNING)
						&& task.isDone())
					buttonRerun.setBounds(width - 80, 0, 20, 20);
				else
					buttonRerun.setBounds(width - 80, 0, 0, 0);
				titleBar.setBounds(0, 0, width - 80, 20);
				imagePreview.setBounds(0, 20, 200, 256);
				int index = 0;
				for(JLabel labelStep : steps.values())
					labelStep.setBounds(200, index = index + 18, width - 210, 20);
				/**
				 * Race conditions : State = WARNING, but we don't still have buttonResults initialized and layoutContainer kicks in
				 * if(task.getSteps().get(Step.PARSE).equals(State.WARNING))
				 */
				if(buttonResults != null)
				{
					index += 30;
					int prevsize = 0;
					for(JButton button : buttonResults.values())
					{
						Dimension prefsize = button.getPreferredSize();
						button.setBounds(200 + prevsize, index, (int)prefsize.getWidth(), (int)prefsize.getHeight());
						prevsize += (int)prefsize.getWidth();
					}
				}
			}
			
			@Override
			public void addLayoutComponent(String key,Component c) {}
			
			@Override
			public void removeLayoutComponent(Component c) {}
			
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
			    return new Dimension(0, 20);
			}
			
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
			    return new Dimension(250, 250);
			}
			
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(ae.getSource() == buttonToggle)
				{
					if(STATUS == STATUS_MAXIMIZED)
					{
						STATUS = STATUS_MINIMIZED;
						buttonToggle.setIcon(ICON_CHECKED);
						setSize(new Dimension(getWidth(), (int)getMinimumSize().getHeight()));
						panelTasks.doLayout();
						panelTasks.validate();
					} else {
						STATUS = STATUS_MAXIMIZED;
						buttonToggle.setIcon(ICON_UNCHECKED);
						setSize(new Dimension(getWidth(), (int)getMaximumSize().getHeight()));
						panelTasks.doLayout();
						panelTasks.validate();
					}
					return;
				}
				if(ae.getActionCommand().startsWith("setResult:"))
				{
					resultId = ae.getActionCommand().substring(ae.getActionCommand().indexOf(':') + 1);
					JButton source = (JButton) ae.getSource();
					for(JButton button : buttonResults.values())
					{
						button.setPreferredSize(new Dimension(25, 155));
					}
					source.setPreferredSize(new Dimension(110, 155));
					doLayout();
					validate();
					return;
				}
			}

			@Override
			public void statusChanged(Step step, State status) {
				titleBar.setIcon(icons.get(status));
				titleBar.setText(task.getMessage());
				steps.get(step).setIcon(icons.get(status));
				/**
				 * Check if cover image is ready to be shown
				 */
				if(step.equals(Step.SCAN) && status.equals(State.COMPLETED))
				{
					try {
						imagePreview.setIcon(
							new ImageIcon(
								javax.imageio.ImageIO.read(
									new File(DoujinshiDBScanner.PLUGIN_HOME, task.getId() + ".png"))));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				if(step.equals(Step.PARSE) && status.equals(State.WARNING))
				{
					Map<String, XMLParser.XML_Book> results = task.getResults();
					buttonResults = new TreeMap<Double, JButton>(Collections.reverseOrder());
					for(String id : results.keySet())
					{
						XMLParser.XML_Book book = results.get(id);
						JButton buttonResult = new JButton();
						try {
							File file = new File(new File(DoujinshiDBScanner.PLUGIN_HOME, ".cache"), book.ID + ".png");
							if(!file.exists())
								System.out.println(file);
							buttonResult.setIcon(new ImageIcon(file.toURI().toURL()));
						} catch (MalformedURLException murle) {
							buttonResult.setIcon(new ImageIcon());
						}
						buttonResult.setText("");
						buttonResult.addActionListener(this);
						buttonResult.addMouseListener(this);
						buttonResult.setActionCommand("setResult:" + book.ID);
						buttonResult.setFocusable(false);
						buttonResult.setPreferredSize(new Dimension(25, 155));
						buttonResults.put(Double.parseDouble(book.search.replaceAll("%", "").replaceAll(",", ".")),
								buttonResult);
						add(buttonResult);
					}
					// First result is already expanded
					JButton firstResult = buttonResults.values().iterator().next();
					firstResult.setPreferredSize(new Dimension(110, 155));
					resultId = firstResult.getActionCommand().substring(firstResult.getActionCommand().indexOf(':') + 1);
					doLayout();
					validate();
				}
			}

			@Override
			public void stepChanged(Step step) {
				titleBar.setText(task.getMessage());
			}

			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(me.getClickCount() == 2 && me.getSource() instanceof JButton) {
					JButton button = (JButton) me.getSource();
					String bookid = button.getActionCommand();
					bookid = bookid.substring(bookid.indexOf(':') + 2); // Also remove the 'B'
					URI uri;
					try {
						uri = new URI("http://doujinshi.mugimugi.org/book/" + bookid + "/");
						Desktop.getDesktop().browse(uri);
					} catch (URISyntaxException urise) {
						urise.printStackTrace();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent me) { }

			@Override
			public void mouseExited(MouseEvent me) { }

			@Override
			public void mousePressed(MouseEvent me) { }

			@Override
			public void mouseReleased(MouseEvent me) { }
		}
	}

	@Override
	protected void install() throws PluginException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void update() throws PluginException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void uninstall() throws PluginException {
		// TODO Auto-generated method stub
	}
	
	private static Set<Task> unserialize()
	{
		Set<Task> tasks = new HashSet<Task>();
		File file = new File(PLUGIN_HOME, "tasks.xml");
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(file);
			JAXBContext context = JAXBContext.newInstance(TaskSet.class);
			Unmarshaller um = context.createUnmarshaller();
			TaskSet set = (TaskSet) um.unmarshal(in);
			for(Task task : set.tasks)
				tasks.add(task);
			return tasks;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (JAXBException jaxbe) {
			jaxbe.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} finally {
			try { in.close(); } catch (Exception e) { }
		}
		return tasks;
	}
	
	private static void serialize(Set<Task> tasks)
	{
		File file = new File(PLUGIN_HOME, "tasks.xml");
		FileOutputStream out = null;
		TaskSet set = new TaskSet();
		try
		{
			for(Task task : tasks)
				set.tasks.add(task);
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
}