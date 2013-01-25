package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
import org.dyndns.doujindb.util.ImageTool;

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
	
	private static JComponent UI;
	static XMLParser.XML_User User = new XMLParser.XML_User();
	static TreeMap<String, int[][]> Cache;
	
	static File PLUGIN_CACHE = new File(PLUGIN_HOME, "fingerprint.ser");
	static File PLUGIN_DATA = new File(PLUGIN_HOME, ".data");
	static File PLUGIN_QUERY = new File(PLUGIN_HOME, ".query");
	
	private static SimpleDateFormat sdf;
	
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
		
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		PLUGIN_HOME.mkdirs();
		PLUGIN_DATA.mkdirs();
		PLUGIN_QUERY.mkdirs();
		
		cacheRead();
		
		UI = new PluginUI();
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
		private JPanel tabSettings;
		private JPanel tabTasks;
		private JPanel tabScanner;
		
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
		
		private JCheckBox boxCacheOverwrite;
		private JProgressBar progressBarCache;
		private JButton buttonCacheBuild;
		private JButton buttonCacheCancel;
		private JLabel labelCacheInfo;
		
		private JLabel labelMaxResults;
		private JSlider sliderMaxResults;
		private JButton buttonScanPreview;
		private JTabbedPane tabsScanResult;
		private JProgressBar progressBarScan;
		private JButton buttonScanCancel;
		
		private Thread workerTaskFetcher = new Thread(getClass().getName()+"$Task[null]");
		private boolean fetcherRunning = false;
		private Thread fetcherThread;
		
		private SwingWorker<Void, Integer> workerTaskScanner = new TaskScanner(null);
		private boolean cacheScannerRunning = false;
		private SwingWorker<Void, Integer> workerTaskBuilder = new TaskBuilder();
		private boolean cacheBuilderRunning = false;
		
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
			labelThreshold = new JLabel("Threshold : " + THRESHOLD + "%");
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
					labelThreshold.setText("Threshold : " + THRESHOLD + "%");
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
			boxCacheOverwrite = new JCheckBox();
			boxCacheOverwrite.setSelected(false);
			boxCacheOverwrite.setFocusable(false);
			boxCacheOverwrite.setText("Overwrite existing entries");
			bogus.add(boxCacheOverwrite);
			buttonCacheBuild = new JButton(Resources.Icons.get("Plugin/Cache"));
			buttonCacheBuild.addActionListener(this);
			buttonCacheBuild.setBorder(null);
			buttonCacheBuild.setFocusable(false);
			bogus.add(buttonCacheBuild);
			buttonCacheCancel = new JButton(Resources.Icons.get("Plugin/Cancel"));
			buttonCacheCancel.addActionListener(this);
			buttonCacheCancel.setBorder(null);
			buttonCacheCancel.setFocusable(false);
			bogus.add(buttonCacheCancel);
			progressBarCache = new JProgressBar();
			progressBarCache.setFont(Core.Resources.Font);
			progressBarCache.setMaximum(100);
			progressBarCache.setMinimum(1);
			progressBarCache.setValue(progressBarCache.getMinimum());
			progressBarCache.setStringPainted(true);
			progressBarCache.setString("");
			bogus.add(progressBarCache);
			labelCacheInfo = new JLabel("<html><body>cache-size : " + humanReadableByteCount(PLUGIN_CACHE.length(), true) + "<br/>" +
					"entry-count : " + Cache.size() + "<br/>" +
					"last-build : " + sdf.format(new Date(PLUGIN_CACHE.lastModified())) + "</body></html>");
			labelCacheInfo.setFont(Core.Resources.Font);
			labelCacheInfo.setVerticalAlignment(JLabel.TOP);
			bogus.add(labelCacheInfo);
			tabs.addTab("Settings", Resources.Icons.get("Plugin/Settings"), tabSettings = bogus);
			
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
			tabs.addTab("Tasks", Resources.Icons.get("Plugin/Tasks"), tabTasks = bogus);
			
			bogus = new JPanel();
			bogus.setLayout(null);
			labelMaxResults = new JLabel("Max Results : " + 10);
			labelMaxResults.setFont(Core.Resources.Font);
			bogus.add(labelMaxResults);
			sliderMaxResults = new JSlider(1, 25);
			sliderMaxResults.setValue(10);
			sliderMaxResults.setFont(Core.Resources.Font);
			sliderMaxResults.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					labelMaxResults.setText("Max Results : " + sliderMaxResults.getValue());
				}				
			});
			bogus.add(sliderMaxResults);
			buttonScanPreview = new JButton();
			buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Search/Preview"));
			buttonScanPreview.addActionListener(this);
			buttonScanPreview.setBorder(null);
			buttonScanPreview.setOpaque(false);
			buttonScanPreview.setDropTarget(new DropTarget()
			{
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde)
				{
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						if(cacheScannerRunning)
						{
							dtde.rejectDrag();
							return;
						}
				        dtde.acceptDrag(DnDConstants.ACTION_COPY);
				    } else {
				        dtde.rejectDrag();
				    }
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public synchronized void drop(DropTargetDropEvent dtde)
				{
					Transferable transferable = dtde.getTransferable();
	                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
	                {
	                    dtde.acceptDrop(dtde.getDropAction());
	                    try
	                    {
	                    	final List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
	                        if (transferData != null && transferData.size() == 1)
	                        {
	                        	new Thread()
	                        	{
	                                @Override
	                                public void run() {
	                                	File file = transferData.iterator().next();
	                                	if(cacheScannerRunning)
	                    					return;
	                                	workerTaskScanner = new TaskScanner(file);
	                                	workerTaskScanner.execute();
	                    				tabScanner.doLayout();
	                                }
	                        	}.start();
	                            dtde.dropComplete(true);
	                        }

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                } else {
	                    dtde.rejectDrop();
	                }
				}
			});
			bogus.add(buttonScanPreview);
			tabsScanResult = new JTabbedPane();
			tabsScanResult.setFont(Core.Resources.Font);
			tabsScanResult.setFocusable(false);
			tabsScanResult.setTabPlacement(JTabbedPane.RIGHT);
			bogus.add(tabsScanResult);
			progressBarScan = new JProgressBar();
			progressBarScan.setFont(Core.Resources.Font);
			progressBarScan.setMaximum(100);
			progressBarScan.setMinimum(1);
			progressBarScan.setValue(progressBarScan.getMinimum());
			progressBarScan.setStringPainted(true);
			progressBarScan.setString("");
			bogus.add(progressBarScan);
			buttonScanCancel = new JButton();
			buttonScanCancel.setText("Cancel");
			buttonScanCancel.setIcon(Resources.Icons.get("Plugin/Cancel"));
			buttonScanCancel.addActionListener(this);
			buttonScanCancel.setToolTipText("Cancel");
			buttonScanCancel.setFocusable(false);
			bogus.add(buttonScanCancel);
			tabs.addTab("Search", Resources.Icons.get("Plugin/Search"), tabScanner = bogus);
			
			super.add(tabs);
			
			Set<Task> tasks = taskRead();
			for(Task task : tasks)
				panelTasks.addTask(task);
			
			// Save Tasks every 5 seconds
			new java.util.Timer(true).scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					Set<Task> tasks = new HashSet<Task>();
					for(Task task : panelTasks)
						tasks.add(task);
					taskWrite(tasks);
				}
			}, 1, 5 * 1000);
			
			// Save Tasks on exit
			Runtime.getRuntime().addShutdownHook(new Thread(getClass().getName()+"$TaskSerializer")
			{
				@Override
				public void run()
				{
					Set<Task> tasks = new HashSet<Task>();
					for(Task task : panelTasks)
						tasks.add(task);
					taskWrite(tasks);
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
						if(workerTaskFetcher.isAlive())
							continue;
						else
						{
							for(Task task : panelTasks)
							{
								if(task.isDone())
									continue;
								workerTaskFetcher = new Thread(task, getClass().getName()+"$Task[id:" + task.getId() + "]");
								workerTaskFetcher.start();
								break;
							}
						}
					}
				}
			};
			fetcherThread.start();
		}
		
		/**
		 * @see http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
		 */
		private String humanReadableByteCount(long bytes, boolean si) {
		    int unit = si ? 1000 : 1024;
		    if (bytes < unit) return bytes + " B";
		    int exp = (int) (Math.log(bytes) / Math.log(unit));
		    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
			if(!cacheBuilderRunning)
			{
				buttonCacheBuild.setBounds(5,25+200,20,20);
				buttonCacheCancel.setBounds(0,0,20,20);
			} else {
				buttonCacheBuild.setBounds(0,0,20,20);
				buttonCacheCancel.setBounds(5,25+200,20,20);
			}
			progressBarCache.setBounds(5,25+220,width-15,20);
			boxCacheOverwrite.setBounds(5,25+240,width-15,20);
			labelCacheInfo.setBounds(5,25+270,width,height-280);
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
			buttonScanPreview.setBounds(5,5,180,256);
			if(cacheScannerRunning)
			{
				progressBarScan.setBounds(5,265,180,20);
				buttonScanCancel.setBounds(5,290,180,20);
			}
			else
			{
				progressBarScan.setBounds(0,0,0,0);
				buttonScanCancel.setBounds(0,0,0,0);
			}
			tabsScanResult.setBounds(190,5,width-190,height-10);
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
			if(ae.getSource() == buttonCacheBuild)
			{
				if(cacheBuilderRunning)
					return;
				workerTaskBuilder = new TaskBuilder();
				workerTaskBuilder.execute();
				return;
			}
			if(ae.getSource() == buttonCacheCancel)
			{
				workerTaskBuilder.cancel(true);
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
			private JButton buttonSkip;
			private Map<String, JButton> buttonDupes;
			private JButton buttonRerun;
			private Map<Double, JButton> buttonResults;
			private JButton buttonDirectURL;
			private JCheckBox bottonSelection;
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
				buttonSkip = new JButton(Resources.Icons.get("Plugin/Task/Skip"));
				buttonSkip.setFocusable(false);
				buttonSkip.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) {
						for(JButton button : buttonDupes.values())
							remove(button);
						TaskUI.this.task.skipDuplicates();
						TaskUI.this.task.setDone(false);
						doLayout();
						validate();
					}
				});
				add(buttonSkip);
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
				buttonDirectURL = new JButton(Resources.Icons.get("Plugin/Task/Download"));
				buttonDirectURL.setFocusable(false);
				buttonDirectURL.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) {
						new SwingWorker<Void, Void>()
						{
							@Override
							protected Void doInBackground() throws Exception
							{
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								Transferable contents = clipboard.getContents(null);
							    boolean hasTransferableText = (contents != null) &&
							    	contents.isDataFlavorSupported(DataFlavor.stringFlavor);
							    if(hasTransferableText) {
							    	try {
							    		String book_id = (String)contents.getTransferData(DataFlavor.stringFlavor);
							    		TaskUI.this.task.fromBID(book_id);
										TaskUI.this.task.setDone(false);
										doLayout();
										validate();
							    	} catch (UnsupportedFlavorException ufe){
							    		ufe.printStackTrace();
							    	} catch (MalformedURLException murle) {
										murle.printStackTrace();
									} catch (IOException ioe) {
										ioe.printStackTrace();
									}
							    }
							    return null;
							}
						}.execute();
					}
				});
				add(buttonDirectURL);
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
				if(task.getStatus(Step.SCAN).equals(State.COMPLETED) ||
					task.getStatus(Step.SCAN).equals(State.WARNING))
				{
					try {
						imagePreview.setIcon(
							new ImageIcon(
								ImageTool.read(
									new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png"))));
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
							buttonResult.setIcon(new ImageIcon(new File(PLUGIN_DATA, book.ID + ".png").toURI().toURL()));
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
				if(task.getStatus(Step.CHECK).equals(State.WARNING))
				{
					buttonDupes = new TreeMap<String, JButton>(Collections.reverseOrder());
					for(String dupe : task.getDuplicates())
					{
						JButton buttonDupe = new JButton();
						buttonDupe.setText(dupe);
						buttonDupe.setIcon(Resources.Icons.get("Plugin/Task/Book"));
						buttonDupe.addActionListener(this);
						buttonDupe.setActionCommand("openDupe:" + dupe);
						buttonDupe.setHorizontalAlignment(SwingConstants.LEFT);
						buttonDupe.setFocusable(false);
						buttonDupes.put(dupe,
								buttonDupe);
						add(buttonDupe);
					}
				}
				if(task.getStatus(Step.SCAN).equals(State.WARNING))
				{
					buttonDupes = new TreeMap<String, JButton>(Collections.reverseOrder());
					for(String dupe : task.getDuplicates())
					{
						JButton buttonDupe = new JButton();
						buttonDupe.setText(dupe);
						buttonDupe.setIcon(Resources.Icons.get("Plugin/Task/Book"));
						buttonDupe.addActionListener(this);
						buttonDupe.setActionCommand("openDupe:" + dupe);
						buttonDupe.setHorizontalAlignment(SwingConstants.LEFT);
						buttonDupe.setFocusable(false);
						buttonDupes.put(dupe,
								buttonDupe);
						add(buttonDupe);
					}
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
				if(task.isDone()
					&& task.getStatus(Step.CHECK).equals(State.WARNING))
					buttonSkip.setBounds(width - 80, 0, 20, 20);
				else
					buttonSkip.setBounds(width - 80, 0, 0, 0);
				if(task.isDone()
					&& task.getStatus(Step.PARSE).equals(State.WARNING))
				{
					buttonRerun.setBounds(width - 80, 0, 20, 20);
					buttonDirectURL.setBounds(width - 100, 0, 20, 20);
				}
				else
				{
					buttonRerun.setBounds(width - 80, 0, 0, 0);
					buttonDirectURL.setBounds(width - 100, 0, 0, 0);
				}
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
				if(buttonDupes != null)
				{
					index += 30;
					int margin = index;
					index = 0;
					for(JButton button : buttonDupes.values())
					{
						Dimension prefsize = button.getPreferredSize();
						button.setBounds(200, margin + index++ * 18, width - 210, 18);
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
				if(ae.getActionCommand().startsWith("openDupe:"))
				{
					String dupeId = ae.getActionCommand().substring(ae.getActionCommand().indexOf(':') + 1);
					QueryBook qid = new QueryBook();
					qid.ID = dupeId;
					RecordSet<Book> set = Core.Database.getBooks(qid);
					if(set.size() == 1)
						Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
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
				if(step.equals(Step.SCAN) && (
					status.equals(State.COMPLETED) ||
					status.equals(State.WARNING)))
				{
					try {
						imagePreview.setIcon(
							new ImageIcon(
								ImageTool.read(
									new File(DoujinshiDBScanner.PLUGIN_QUERY, task.getId() + ".png"))));
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
							File file = new File(PLUGIN_DATA, book.ID + ".png");
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
				if(task.getStatus(Step.CHECK).equals(State.WARNING))
				{
					buttonDupes = new TreeMap<String, JButton>(Collections.reverseOrder());
					for(String dupe : task.getDuplicates())
					{
						JButton buttonDupe = new JButton();
						buttonDupe.setText(dupe);
						buttonDupe.setIcon(Resources.Icons.get("Plugin/Task/Book"));
						buttonDupe.addActionListener(this);
						buttonDupe.addMouseListener(this);
						buttonDupe.setActionCommand("openDupe:" + dupe);
						buttonDupe.setFocusable(false);
						buttonDupe.setPreferredSize(new Dimension(16, 16));
						buttonDupes.put(dupe,
								buttonDupe);
						add(buttonDupe);
					}
					doLayout();
					validate();
				}
				if(task.getStatus(Step.SCAN).equals(State.WARNING))
				{
					buttonDupes = new TreeMap<String, JButton>(Collections.reverseOrder());
					for(String dupe : task.getDuplicates())
					{
						JButton buttonDupe = new JButton();
						buttonDupe.setText(dupe);
						buttonDupe.setIcon(Resources.Icons.get("Plugin/Task/Book"));
						buttonDupe.addActionListener(this);
						buttonDupe.addMouseListener(this);
						buttonDupe.setActionCommand("openDupe:" + dupe);
						buttonDupe.setFocusable(false);
						buttonDupe.setPreferredSize(new Dimension(16, 16));
						buttonDupes.put(dupe,
								buttonDupe);
						add(buttonDupe);
					}
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
		
		private final class TaskBuilder extends SwingWorker<Void, Integer>
		{
			@Override
			protected Void doInBackground() throws Exception {
				cacheBuilderRunning = true;
				PluginUI.this.doLayout();

				// Reset UI
				progressBarCache.setValue(progressBarCache.getMinimum());
				progressBarCache.setString("Loading ...");
				
				// Init data
				int density = 15;
				boolean cache_overwrite = boxCacheOverwrite.isSelected();
				RecordSet<Book> books = Context.getBooks(null);
				
				progressBarCache.setMaximum(books.size());
				progressBarCache.setMinimum(1);
				progressBarCache.setValue(progressBarCache.getMinimum());
				
				for(Book book : books)
				{
					try { Thread.sleep(1); } catch (InterruptedException ie) { }
					
					if(isCancelled())
						return null;
						
					int progress = progressBarCache.getValue() * 100 / progressBarCache.getMaximum();
					progressBarCache.setString("[" + progressBarCache.getValue() + " / " + progressBarCache.getMaximum() + "] @ " + progress + "%");
					progressBarCache.setValue(progressBarCache.getValue() + 1);
					
					BufferedImage bi;
					try {
						if(Cache.containsKey(book.getID()) && !cache_overwrite)
							continue;
						
						bi = ImageTool.read(Core.Repository.getPreview(book.getID()).getInputStream());
						bi = ImageTool.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
						
						NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, density);
						int[][] signature = nsf.getSignature();
						Cache.put(book.getID(), signature);
						
					} catch (Exception e) { e.printStackTrace(); }
				}
				
				// Write Cache
				cacheWrite();
				
				// Cache build completed
				labelCacheInfo.setText("<html><body>cache-size : " + humanReadableByteCount(PLUGIN_CACHE.length(), true) + "<br/>" +
						"entry-count : " + Cache.size() + "<br/>" +
						"last-build : " + sdf.format(new Date(PLUGIN_CACHE.lastModified())) + "</body></html>");
				
				return null;
			}

			@Override
			protected void done() {
				cacheBuilderRunning = false;
				PluginUI.this.doLayout();
				
				progressBarCache.setValue(0);
				progressBarCache.setString("");
				
				super.done();
			}

			@Override
			protected void process(List<Integer> chunks) {
				super.process(chunks);
			}
		}

		private final class TaskScanner extends SwingWorker<Void, Integer>
		{
			final private File file;
			
			private TaskScanner(final File file)
			{
				this.file = file;
			}
			
			@Override
			protected Void doInBackground() throws Exception
			{
				cacheScannerRunning = true;
				PluginUI.this.doLayout();
				
				// Reset UI
				while (tabsScanResult.getTabCount() > 0)
					tabsScanResult.remove(0);
				buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				progressBarScan.setValue(progressBarScan.getMinimum());
				progressBarScan.setString("Loading ...");
				
				// Init data
				int threshold = sliderThreshold.getValue();
				int max_results = sliderMaxResults.getValue();
				
				BufferedImage bi;
				try {
					bi = ImageTool.read(file);
					BufferedImage resized_bi;
					if(bi.getWidth() > bi.getHeight())
						resized_bi = new BufferedImage(bi.getWidth() / 2, bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					else
						resized_bi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics g = resized_bi.getGraphics();
					g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), null);
					g.dispose();
					bi = ImageTool.getScaledInstance(resized_bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
					
					buttonScanPreview.setIcon(new ImageIcon(bi));
					
					TreeMap<Double, Book> result = new TreeMap<Double, Book>(new Comparator<Double>()
					{
						@Override
						public int compare(Double a, Double b)
						{
							return b.compareTo(a);
						}
					});
					NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, 15); //FIXME ? Hardcoded Density
					RecordSet<Book> books = Context.getBooks(null);
					
					progressBarScan.setMaximum(books.size());
					progressBarScan.setMinimum(1);
					progressBarScan.setValue(progressBarScan.getMinimum());
					
					for(Book book : books)
					{
						try { Thread.sleep(1); } catch (InterruptedException ie) { }
						
						if(isCancelled())
							return null;
						
						String book_id = book.getID();
						if(Cache.containsKey(book_id))
						{
							double similarity = nsf.getPercentSimilarity(Cache.get(book_id));
							if(similarity >= threshold)
								if(result.size() >= max_results)
								{
									double remove_me = result.lastKey();
									result.put(similarity, book);
									result.remove(remove_me);
								} else {
									result.put(similarity, book);
								}
						}
						
						int progress = progressBarScan.getValue() * 100 / progressBarScan.getMaximum();
						progressBarScan.setString("[" + progressBarScan.getValue() + " / " + progressBarScan.getMaximum() + "] @ " + progress + "%");
						progressBarScan.setValue(progressBarScan.getValue() + 1);
						if(progressBarScan.getValue() == progressBarScan.getMaximum())
							progressBarScan.setValue(progressBarScan.getMinimum());
					}
					
					boolean first_result = false;
					for(double index : result.keySet())
					{
						final Book book = result.get(index);
						if(!first_result)
						{
							JButton button = new JButton(
								new ImageIcon(
									ImageTool.read(
										Core.Repository.getPreview(book.getID()).getInputStream())));
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									new SwingWorker<Void, Void>()
									{
										@Override
										protected Void doInBackground() throws Exception
										{
											Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
											return null;
										}
									}.execute();
								}
							});
							first_result = true;
							tabsScanResult.addTab(String.format("%3.2f", index) + "%", Resources.Icons.get("Plugin/Search/Star"), button);
						} else
						{
							JButton button = new JButton(
									new ImageIcon(
										ImageTool.read(
											Core.Repository.getPreview(book.getID()).getInputStream())));
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									new SwingWorker<Void, Void>()
									{
										@Override
										protected Void doInBackground() throws Exception
										{
											Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
											return null;
										}
									}.execute();								}
							});
							tabsScanResult.addTab(String.format("%3.2f", index) + "%", button);
						}
					}
					
					progressBarScan.setValue(progressBarScan.getMaximum());
					progressBarScan.setString("Completed");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
			
			@Override
			protected void done() {
				cacheScannerRunning = false;
				PluginUI.this.doLayout();
				super.done();
			}

			@Override
			protected void process(List<Integer> chunks) {
				super.process(chunks);
			}
		}
	}

	@Override
	protected void install() throws PluginException { }

	@Override
	protected void update() throws PluginException { }

	@Override
	protected void uninstall() throws PluginException { }
	
	private static Set<Task> taskRead()
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
			{
				task.loadResults();
				tasks.add(task);
			}
			return tasks;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (JAXBException jaxbe) {
			jaxbe.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			;
		} finally {
			try { in.close(); } catch (Exception e) { }
		}
		return tasks;
	}
	
	private static void taskWrite(Set<Task> tasks)
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
	
	private static void cacheWrite()
	{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(PLUGIN_CACHE)));
			oos.writeObject(Cache);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void cacheRead()
	{
		try {
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(PLUGIN_CACHE)));
			Cache = (TreeMap<String, int[][]>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(Cache == null)
			Cache = new TreeMap<String, int[][]>();
	}
}