package org.dyndns.doujindb.plug.impl.mugimugi;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.event.ConfigurationListener;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.desk.WindowEx;
import org.dyndns.doujindb.util.ImageTool;

/**  
* DoujinshiDBScanner.java - Plugin to batch process media files thanks to the DoujinshiDB project APIs.
* @author  nozomu
* @version 1.3
*/
public final class DoujinshiDBScanner extends Plugin
{
	private static final String UUID = "{CB123239-06D1-4FB6-A4CC-05C4B436DF73}";
	private static final String Author = "Nozomu";
	private static final String Version = "1.3";
	private static final String Weblink = "http://code.google.com/p/doujindb/";
	private static final String Name = "DoujinshiDB Scanner";
	private static final String Description = "The DoujinshiDB plugin lets you batch process media files thanks to DoujinshiDB API.";
	
	// DoujinshiDB URLs
	static final String DOUJINSHIDB_URL    = "http://www.doujinshi.org/";
	static final String DOUJINSHIDB_APIURL = "http://www.doujinshi.org/api/";
	static final String DOUJINSHIDB_IMGURL = "http://img.doujinshi.org/";
	static final String DOUJINSHIDB_REGEXP = "(http://(www\\.)?doujinshi\\.org/book/)?([0-9]+)(/)?";
	
	static String APIKEY = "";
	static int THRESHOLD = 75;
	static boolean RESIZE_COVER = false;
	static int QUERIES;
	static int IMAGE_QUERIES;
	static String USERID;
	static String USERNAME;
	static String USER_AGENT = "Mozilla/5.0 (compatible; " + Name + "/" + Version + "; +" + Weblink + ")";
	
	static DataBaseContext Context;
	
	private static JComponent m_UI;
	static XMLParser.XML_User UserInfo = new XMLParser.XML_User();
	
	static final File PLUGIN_HOME = new File(Core.DOUJINDB_HOME, "plugins" + File.separator + UUID);

	static final File PLUGIN_IMAGECACHE = new File(PLUGIN_HOME, "imagecache");
	static final File PLUGIN_QUERY = new File(PLUGIN_HOME, "query");
	
	private static SimpleDateFormat sdf;
	private static String configBase = "org.dyndns.doujindb.plugin.mugimugi.";
	private static Font font;
	private static Icons Icon = new Icons();
	
	static
	{
		Configuration.configAdd(configBase + "apikey", "<html><body>Apikey used to query the doujinshidb database.</body></html>", "");
		Configuration.configAdd(configBase + "threshold", "<html><body>Threshold limit for matching cover queries.</body></html>", 75);
		Configuration.configAdd(configBase + "resize_cover", "<html><body>Whether to resize covers before uploading them.</body></html>", true);
	}
	
	@Override
	public String getUUID() {
		return UUID;
	}
	
	@Override
	public Icon getIcon() {
		return Icon.icon;
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
		return m_UI;
	}
	
	@SuppressWarnings("serial")
	private static final class PluginUI extends JPanel implements LayoutManager, ActionListener, PropertyChangeListener, ConfigurationListener
	{
		private JTabbedPane m_TabbedPane;
		@SuppressWarnings("unused")
		private JPanel m_TabSettings;
		@SuppressWarnings("unused")
		private JPanel m_TabTasks;
		private JPanel m_TabScanner;
		
		private JButton m_ButtonApiRefresh;
		private JLabel m_LabelApikey;
		private JTextField m_TextApikey;
		private JLabel m_LabelApiThreshold;
		private JSlider m_SliderApiThreshold;
		private JLabel m_LabelApiUserid;
		private JTextField m_TextApiUserid;
		private JLabel m_LabelApiUsername;
		private JTextField m_TextApiUsername;
		private JLabel m_LabelApiQueryCount;
		private JTextField m_TextApiQueryCount;
		private JLabel m_LabelApiImageQueryCount;
		private JTextField m_TextApiImageQueryCount;
		private JCheckBox m_CheckboxApiResizeImage;
		
		private JButton m_ButtonTaskAdd;
		private JButton m_ButtonTaskManagerCtl;
		private JButton m_ButtonTaskDelete;
		private JButton m_ButtonTaskReset;
		private JCheckBox m_CheckboxSelection;
		
		private JSplitPane m_SplitPane;
		private PanelTaskUI m_PanelTasks;
		private JScrollPane m_ScrollPanelTasks;
		private TaskUI m_PanelTask;
		
		private JCheckBox m_CheckboxCacheOverwrite;
		private JProgressBar m_ProgressBarCache;
		private JButton m_ButtonCacheBuild;
		private JButton m_ButtonCacheCancel;
		private JLabel m_LabelCacheInfo;
		
		private JLabel m_LabelMaxResults;
		private JSlider m_SliderMaxResults;
		private JButton m_ButtonScanPreview;
		private JTabbedPane m_TabbedPaneScanResult;
		private JProgressBar m_ProgressBarScan;
		private JButton m_ButtonScanCancel;
		
		private SwingWorker<Void, Integer> m_WorkerScanner = new TaskScanner(null);
		private boolean m_ScannerRunning = false;
		private SwingWorker<Void, Integer> m_WorkerBuilder = new TaskBuilder();
		private boolean m_BuilderRunning = false;
		
		public PluginUI()
		{
			super();
			setLayout(this);
			m_TabbedPane = new JTabbedPane();
			m_TabbedPane.setFont(font = UI.Font);
			m_TabbedPane.setFocusable(false);
			
			JPanel bogus;
			bogus = new JPanel();
			bogus.setLayout(null);
			m_ButtonApiRefresh = new JButton(Icon.refresh);
			m_ButtonApiRefresh.addActionListener(this);
			m_ButtonApiRefresh.setBorder(null);
			m_ButtonApiRefresh.setFocusable(false);
			bogus.add(m_ButtonApiRefresh);
			m_LabelApikey = new JLabel("Api Key :");
			m_LabelApikey.setFont(font);
			bogus.add(m_LabelApikey);
			m_TextApikey = new JTextField(APIKEY);
			m_TextApikey.setFont(font);
			m_TextApikey.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void changedUpdate(DocumentEvent de)
				{
					APIKEY = m_TextApikey.getText();
					Configuration.configWrite(configBase + "apikey", APIKEY);
				}
				@Override
				public void insertUpdate(DocumentEvent de)
				{
					APIKEY = m_TextApikey.getText();
					Configuration.configWrite(configBase + "apikey", APIKEY);
				}
				@Override
				public void removeUpdate(DocumentEvent de)
				{
					APIKEY = m_TextApikey.getText();
					Configuration.configWrite(configBase + "apikey", APIKEY);
				}				
			});
			bogus.add(m_TextApikey);
			m_LabelApiThreshold = new JLabel("Threshold : " + THRESHOLD + "%");
			m_LabelApiThreshold.setFont(font);
			bogus.add(m_LabelApiThreshold);
			m_SliderApiThreshold = new JSlider(0, 100, THRESHOLD);
			m_SliderApiThreshold.setFont(font);
			m_SliderApiThreshold.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					THRESHOLD = m_SliderApiThreshold.getValue();
					m_LabelApiThreshold.setText("Threshold : " + THRESHOLD + "%");
					if(m_SliderApiThreshold.getValueIsAdjusting())
						return;
					Configuration.configWrite(configBase + "threshold", THRESHOLD);
				}				
			});
			bogus.add(m_SliderApiThreshold);
			m_LabelApiUserid = new JLabel("User ID :");
			m_LabelApiUserid.setFont(font);
			bogus.add(m_LabelApiUserid);
			m_TextApiUserid = new JTextField(USERID);
			m_TextApiUserid.setFont(font);
			m_TextApiUserid.setEditable(false);
			bogus.add(m_TextApiUserid);
			m_LabelApiUsername = new JLabel("User Name :");
			m_LabelApiUsername.setFont(font);
			bogus.add(m_LabelApiUsername);
			m_TextApiUsername = new JTextField(USERNAME);
			m_TextApiUsername.setFont(font);
			m_TextApiUsername.setEditable(false);
			bogus.add(m_TextApiUsername);
			m_LabelApiQueryCount = new JLabel("Queries :");
			m_LabelApiQueryCount.setFont(font);
			bogus.add(m_LabelApiQueryCount);
			m_TextApiQueryCount = new JTextField("" + QUERIES);
			m_TextApiQueryCount.setFont(font);
			m_TextApiQueryCount.setEditable(false);
			bogus.add(m_TextApiQueryCount);
			m_LabelApiImageQueryCount = new JLabel("Image Queries :");
			m_LabelApiImageQueryCount.setFont(font);
			bogus.add(m_LabelApiImageQueryCount);
			m_TextApiImageQueryCount = new JTextField("" + IMAGE_QUERIES);
			m_TextApiImageQueryCount.setFont(font);
			m_TextApiImageQueryCount.setEditable(false);
			bogus.add(m_TextApiImageQueryCount);
			m_CheckboxApiResizeImage = new JCheckBox("<html><body>Resize covers before uploading*<br><i>(*will speed up searches and preserve bandwidth)</i></body></html>");
			m_CheckboxApiResizeImage.setFont(font);
			m_CheckboxApiResizeImage.setFocusable(false);
			m_CheckboxApiResizeImage.setSelected(RESIZE_COVER);
			m_CheckboxApiResizeImage.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					RESIZE_COVER = m_CheckboxApiResizeImage.isSelected();
					Configuration.configWrite(configBase + "resize_cover", RESIZE_COVER);
				}				
			});
			bogus.add(m_CheckboxApiResizeImage);
			m_CheckboxCacheOverwrite = new JCheckBox();
			m_CheckboxCacheOverwrite.setSelected(false);
			m_CheckboxCacheOverwrite.setFocusable(false);
			m_CheckboxCacheOverwrite.setText("Overwrite existing entries");
			bogus.add(m_CheckboxCacheOverwrite);
			m_ButtonCacheBuild = new JButton(Icon.cache);
			m_ButtonCacheBuild.addActionListener(this);
			m_ButtonCacheBuild.setBorder(null);
			m_ButtonCacheBuild.setFocusable(false);
			bogus.add(m_ButtonCacheBuild);
			m_ButtonCacheCancel = new JButton(Icon.cancel);
			m_ButtonCacheCancel.addActionListener(this);
			m_ButtonCacheCancel.setBorder(null);
			m_ButtonCacheCancel.setFocusable(false);
			bogus.add(m_ButtonCacheCancel);
			m_ProgressBarCache = new JProgressBar();
			m_ProgressBarCache.setFont(font);
			m_ProgressBarCache.setMaximum(100);
			m_ProgressBarCache.setMinimum(1);
			m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
			m_ProgressBarCache.setStringPainted(true);
			m_ProgressBarCache.setString("");
			bogus.add(m_ProgressBarCache);
			m_LabelCacheInfo = new JLabel("<html><body>cache-size : " + CacheManager.size() + "<br/>" +
					"last-build : " + sdf.format(CacheManager.timestamp()) + "</body></html>");
			m_LabelCacheInfo.setFont(font);
			m_LabelCacheInfo.setVerticalAlignment(JLabel.TOP);
			bogus.add(m_LabelCacheInfo);
			m_LabelMaxResults = new JLabel("Max Results : " + 10);
			m_LabelMaxResults.setFont(font);
			bogus.add(m_LabelMaxResults);
			m_SliderMaxResults = new JSlider(1, 25);
			m_SliderMaxResults.setValue(10);
			m_SliderMaxResults.setFont(font);
			m_SliderMaxResults.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					m_LabelMaxResults.setText("Max Results : " + m_SliderMaxResults.getValue());
				}				
			});
			bogus.add(m_SliderMaxResults);
			m_TabbedPane.addTab("Settings", Icon.settings, m_TabSettings = bogus);
			
			bogus = new JPanel();
			bogus.setLayout(null);
			m_ButtonTaskAdd = new JButton(Icon.add);
			m_ButtonTaskAdd.addActionListener(this);
			m_ButtonTaskAdd.setBorder(null);
			m_ButtonTaskAdd.setFocusable(false);
			bogus.add(m_ButtonTaskAdd);
			m_ButtonTaskManagerCtl = new JButton(Icon.task_resume);
			m_ButtonTaskManagerCtl.addActionListener(this);
			m_ButtonTaskManagerCtl.setBorder(null);
			m_ButtonTaskManagerCtl.setToolTipText("Resume Worker");
			m_ButtonTaskManagerCtl.setFocusable(false);
			bogus.add(m_ButtonTaskManagerCtl);
			m_ButtonTaskDelete = new JButton(Icon.task_delete);
			m_ButtonTaskDelete.addActionListener(this);
			m_ButtonTaskDelete.setBorder(null);
			m_ButtonTaskDelete.setToolTipText("Detele");
			m_ButtonTaskDelete.setFocusable(false);
			bogus.add(m_ButtonTaskDelete);
			m_ButtonTaskReset = new JButton(Icon.task_reset);
			m_ButtonTaskReset.addActionListener(this);
			m_ButtonTaskReset.setBorder(null);
			m_ButtonTaskReset.setToolTipText("Reset");
			m_ButtonTaskReset.setFocusable(false);
			bogus.add(m_ButtonTaskReset);
			m_CheckboxSelection = new JCheckBox();
			m_CheckboxSelection.setSelected(false);
			m_CheckboxSelection.addActionListener(this);
			bogus.add(m_CheckboxSelection);
			
			m_SplitPane = new JSplitPane();
			m_SplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			m_SplitPane.setResizeWeight(1);
			m_PanelTasks = new PanelTaskUI();
			m_ScrollPanelTasks = new JScrollPane(m_PanelTasks);
			m_ScrollPanelTasks.getVerticalScrollBar().setUnitIncrement(10);
			m_SplitPane.setTopComponent(m_ScrollPanelTasks);
			m_PanelTask = new TaskUI();
			m_SplitPane.setBottomComponent(null);
			bogus.add(m_SplitPane);
			m_TabbedPane.addTab("Tasks", Icon.tasks, m_TabTasks = bogus);
			
			bogus = new JPanel();
			bogus.setLayout(null);
			m_ButtonScanPreview = new JButton();
			m_ButtonScanPreview.setIcon(Icon.search_preview);
			m_ButtonScanPreview.addActionListener(this);
			m_ButtonScanPreview.setBorder(null);
			m_ButtonScanPreview.setOpaque(false);
			m_ButtonScanPreview.setDropTarget(new DropTarget()
			{
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde)
				{
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						if(m_ScannerRunning)
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
	                                	if(m_ScannerRunning)
	                    					return;
	                                	m_WorkerScanner = new TaskScanner(file);
	                                	m_WorkerScanner.execute();
	                    				m_TabScanner.doLayout();
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
			bogus.add(m_ButtonScanPreview);
			m_TabbedPaneScanResult = new JTabbedPane();
			m_TabbedPaneScanResult.setFont(font);
			m_TabbedPaneScanResult.setFocusable(false);
			m_TabbedPaneScanResult.setTabPlacement(JTabbedPane.RIGHT);
			bogus.add(m_TabbedPaneScanResult);
			//FIXME DiujinshiDB Plugin image scanner task should be cancellable
//			m_ProgressBarScan = new JProgressBar();
			m_ProgressBarScan = new JProgressBar()
			{
				private int size = 30;
				private int direction = 1;
				{
					new SwingWorker<Void,Void>()
					{
						@Override
						public Void doInBackground()
						{
							while(true)
							{
								try
								{
									Thread.sleep(100);
									publish();
								} catch (InterruptedException ie) { ; }
								
							}
						}
						@Override
						public void process(java.util.List<Void> chunks)
						{
							setValue(getValue()+direction*5);
							if(getValue() == getMaximum() || getValue() == getMinimum())
								direction *= -1;
						}
					}.execute();
				}
				public void paint(Graphics g)
				{
					g.setColor(getBackground());
					g.fillRect(0,0,getWidth(),getHeight());
					g.setColor(getForeground());
					g.fillRect(getValue(),2,size,getHeight()-3);
				}
			};
			m_ProgressBarScan.setFont(font);
			m_ProgressBarScan.setMaximum(100);
			m_ProgressBarScan.setMinimum(1);
			m_ProgressBarScan.setValue(m_ProgressBarScan.getMinimum());
			m_ProgressBarScan.setStringPainted(true);
			m_ProgressBarScan.setString("");
			bogus.add(m_ProgressBarScan);
			m_ButtonScanCancel = new JButton();
			m_ButtonScanCancel.setText("Cancel");
			m_ButtonScanCancel.setIcon(Icon.cancel);
			m_ButtonScanCancel.addActionListener(this);
			m_ButtonScanCancel.setToolTipText("Cancel");
			m_ButtonScanCancel.setFocusable(false);
			bogus.add(m_ButtonScanCancel);
			m_TabbedPane.addTab("Search", Icon.search, m_TabScanner = bogus);
			super.add(m_TabbedPane);
			
			TaskManager.registerListener(this);
			
			Configuration.addConfigurationListener(this);

			// Load UserInfo by simulating a click
			m_ButtonApiRefresh.doClick();
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_TabbedPane.setBounds(0,0,width,height);
			m_ButtonApiRefresh.setBounds(1,1,20,20);
			m_LabelApikey.setBounds(5,25,120,15);
			m_TextApikey.setBounds(125,25,width-130,15);
			m_LabelApiThreshold.setBounds(5,25+20,120,15);
			m_SliderApiThreshold.setBounds(125,25+20,width-130,15);
			m_LabelApiUserid.setBounds(5,25+40,120,15);
			m_TextApiUserid.setBounds(125,25+40,width-130,15);
			m_LabelApiUsername.setBounds(5,25+60,120,15);
			m_TextApiUsername.setBounds(125,25+60,width-130,15);
			m_LabelApiQueryCount.setBounds(5,25+80,120,15);
			m_TextApiQueryCount.setBounds(125,25+80,width-130,15);
			m_LabelApiImageQueryCount.setBounds(5,25+100,120,15);
			m_TextApiImageQueryCount.setBounds(125,25+100,width-130,15);
			m_CheckboxApiResizeImage.setBounds(5,25+120,width,45);
			m_ButtonTaskAdd.setBounds(1,1,20,20);
			m_ButtonTaskManagerCtl.setBounds(21,1,20,20);
			if(!m_BuilderRunning)
			{
				m_ButtonCacheBuild.setBounds(5,25+200,20,20);
				m_ButtonCacheCancel.setBounds(0,0,20,20);
			} else {
				m_ButtonCacheBuild.setBounds(0,0,20,20);
				m_ButtonCacheCancel.setBounds(5,25+200,20,20);
			}
			m_ProgressBarCache.setBounds(5,25+220,width-15,20);
			m_CheckboxCacheOverwrite.setBounds(5,25+240,width-15,20);
			m_LabelCacheInfo.setBounds(5,25+270,width,50);
			m_LabelMaxResults.setBounds(5,25+320,100,25);
			m_SliderMaxResults.setBounds(105,25+320,100,25);
			m_ButtonTaskDelete.setBounds(width-65,1,20,20);
			m_ButtonTaskReset.setBounds(width-45,1,20,20);
			m_CheckboxSelection.setBounds(width-25,1,20,20);
			m_SplitPane.setBounds(1,21,width-5,height-45);
			if(UserInfo != null)
			{
				m_TextApiUserid.setText(UserInfo.id);
				m_TextApiUsername.setText(UserInfo.User);
				m_TextApiQueryCount.setText("" + UserInfo.Queries);
				m_TextApiImageQueryCount.setText("" + UserInfo.Image_Queries);
			}else
			{
				m_TextApiUserid.setText("");
				m_TextApiUsername.setText("");
				m_TextApiQueryCount.setText("");
				m_TextApiImageQueryCount.setText("");
			}
			m_ButtonScanPreview.setBounds(5,5,180,256);
			//FIXME DiujinshiDB Plugin image scanner task should be cancellable
//			if(m_ScannerRunning)
//			{
//				m_ProgressBarScan.setBounds(5,265,180,20);
//				m_ButtonScanCancel.setBounds(5,290,180,20);
//			}
//			else
//			{
//				m_ProgressBarScan.setBounds(0,0,0,0);
//				m_ButtonScanCancel.setBounds(0,0,0,0);
//			}
			if(m_ScannerRunning)
				m_ProgressBarScan.setBounds(5,265,180,20);
			else
				m_ProgressBarScan.setBounds(0,0,0,0);
			m_ButtonScanCancel.setBounds(0,0,0,0);
			m_TabbedPaneScanResult.setBounds(190,5,width-190,height-10);
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
			if(ae.getSource() == m_ButtonApiRefresh)
			{
				m_TextApikey.setText(APIKEY);
				m_SliderApiThreshold.setValue(THRESHOLD);
				m_ButtonApiRefresh.setEnabled(false);
				m_TextApikey.setEnabled(false);
				m_SliderApiThreshold.setEnabled(false);
				m_ButtonApiRefresh.setIcon(Icon.loading);
				new SwingWorker<Void,Void>()
				{
					@Override
					protected Void doInBackground() throws Exception {
						try
						{
							if(APIKEY == null || APIKEY.equals(""))
								throw new Exception("Invalid API key provided.");
							URLConnection urlc = new java.net.URL(DoujinshiDBScanner.DOUJINSHIDB_APIURL + APIKEY + "/").openConnection();
							urlc.setRequestProperty("User-Agent", USER_AGENT);
							InputStream in = new ClientHttpRequest(urlc).post();
							XMLParser.XML_User parsedUser = XMLParser.readUser(in);
							UserInfo = (parsedUser == null ? UserInfo : parsedUser);
						} catch (Exception e)
						{
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void done() {
						m_ButtonApiRefresh.setIcon(Icon.refresh);
						m_ButtonApiRefresh.setEnabled(true);
						m_TextApikey.setEnabled(true);
						m_SliderApiThreshold.setEnabled(true);
						doLayout();
						validate();
					}
				}.execute();
				return;
			}
			if(ae.getSource() == m_ButtonTaskAdd)
			{
				try 
				{
					JFileChooser fc = UI.FileChooser;
					int prev_option = fc.getFileSelectionMode();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setMultiSelectionEnabled(true);
					if(fc.showOpenDialog(PluginUI.this) != JFileChooser.APPROVE_OPTION)
					{
						fc.setFileSelectionMode(prev_option);
						return;
					}
					final File files[] = fc.getSelectedFiles();
					for(File file : files)
					{
						TaskManager.add(file);
					}
					m_PanelTasks.dataChanged();
					fc.setFileSelectionMode(prev_option);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskManagerCtl)
			{
				if(TaskManager.isRunning())
				{
					m_ButtonTaskManagerCtl.setIcon(Icon.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							TaskManager.stop();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(Icon.task_resume);
						}
					}.execute();
				} else {
					m_ButtonTaskManagerCtl.setIcon(Icon.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							TaskManager.start();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(Icon.task_pause);
						}
					}.execute();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskDelete)
			{
				List<Task> selected = new Vector<Task>();
				for(Task task : TaskManager.tasks()) {
					if(task.isSelected() && !task.isRunning()) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected)
				{
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					TaskManager.remove(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_ButtonTaskReset)
			{
				List<Task> selected = new Vector<Task>();
				for(Task task : TaskManager.tasks()) {
					if(task.isSelected() && !task.isRunning()) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected)
				{
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					TaskManager.reset(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_CheckboxSelection)
			{
				for(Task task : TaskManager.tasks())
					task.setSelected(m_CheckboxSelection.isSelected());
				m_PanelTasks.dataChanged();
				return;
			}
			if(ae.getSource() == m_ButtonCacheBuild)
			{
				if(m_BuilderRunning)
					return;
				m_WorkerBuilder = new TaskBuilder();
				m_WorkerBuilder.execute();
				return;
			}
			if(ae.getSource() == m_ButtonCacheCancel)
			{
				m_WorkerBuilder.cancel(true);
				return;
			}
			if(ae.getSource() == m_ButtonScanCancel)
			{
				m_WorkerScanner.cancel(true);
				return;
			}
		}
		
		private final class PanelTaskUI extends JTable implements PropertyChangeListener
		{
			private Class<?>[] m_Types = new Class[] {
				Task.Info.class,		// Task info
				Integer.class,			// Task Progress
				Boolean.class			// Selection
			};
			
			private TaskSetTableModel m_TableModel;
			private TaskRenderer m_TableRender;
			private TaskEditor m_TableEditor;
			private TableRowSorter<DefaultTableModel> m_TableSorter;
			
			private PanelTaskUI()
			{
				m_TableModel = new TaskSetTableModel();
				m_TableModel.addColumn("");
				m_TableModel.addColumn("progress");
				m_TableModel.addColumn("");
				m_TableRender = new TaskRenderer();
				m_TableEditor = new TaskEditor();
				m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
				super.setRowSorter(m_TableSorter);
				super.setModel(m_TableModel);
				super.setFont(font);
				super.getTableHeader().setReorderingAllowed(false);
				super.getColumnModel().getColumn(0).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(1).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(1).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(2).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setResizable(false);
				super.getColumnModel().getColumn(0).setMaxWidth(20);
				super.getColumnModel().getColumn(0).setMinWidth(20);
				super.getColumnModel().getColumn(0).setWidth(20);
				super.getColumnModel().getColumn(1).setMinWidth(150);
				super.getColumnModel().getColumn(1).setWidth(150);
				super.getColumnModel().getColumn(1).setPreferredWidth(150);
				super.getColumnModel().getColumn(2).setMaxWidth(20);
				super.getColumnModel().getColumn(2).setMinWidth(20);
				super.getColumnModel().getColumn(2).setWidth(20);
				super.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				
				super.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				super.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
					@Override
					public void valueChanged(ListSelectionEvent lse) {
						if(lse.getValueIsAdjusting())
							return;
						int rowNumber = getSelectedRow();
						if(rowNumber == -1)
							return;
						Task task = (Task) getValueAt(rowNumber, -1);
						m_PanelTask.setTask(task);
						m_SplitPane.setBottomComponent(m_PanelTask);
					}
				});

				super.addMouseMotionListener(new MouseMotionAdapter()
				{
					private int BUTTON1 = MouseEvent.BUTTON1_DOWN_MASK;
					private int BUTTON2 = MouseEvent.BUTTON2_DOWN_MASK;
					private int BUTTON3 = MouseEvent.BUTTON3_DOWN_MASK;
					
					@Override
					public void mouseDragged(MouseEvent me)
					{
						int rowNumber = rowAtPoint(me.getPoint());
						int colNumber = columnAtPoint(me.getPoint());
						
						if(rowNumber == -1 || colNumber == getColumnCount() -1)
							return;
						Task task = (Task) getValueAt(rowNumber, -1);
						if ((me.getModifiersEx() & (BUTTON1 | BUTTON2 | BUTTON3)) == BUTTON1) {
							task.setSelected(true);
						}
						if ((me.getModifiersEx() & (BUTTON1 | BUTTON2 | BUTTON3)) == BUTTON3) {
							task.setSelected(false);
						}
						dataChanged(rowNumber);
					}
				});
				
				TaskManager.registerListener(this);
			}
			
			public void dataChanged() {
				m_TableModel.fireTableDataChanged();
			}
			
			public void dataChanged(int row) {
				for(int idx = 0; idx < m_TableModel.getColumnCount(); idx++)
					m_TableModel.fireTableCellUpdated(row, idx);
			}

			private final class TaskSetTableModel extends DefaultTableModel
			{
				private TaskSetTableModel()
				{
					
				}
				
				@Override
				public int getRowCount() {
					return TaskManager.size();
				}

				@Override
				public int getColumnCount() {
					return m_Types.length;
				}
				
				public Class<?> getColumnClass(int columnIndex) {
					return m_Types[columnIndex];
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					Task row = TaskManager.get(rowIndex);
					switch(columnIndex)
					{
						case -1:
							return row;
						case 0:
							return row.getInfo();
						case 1:
							return row.getProgress();
						case 2:
							return row.isSelected();
					}
					throw new IllegalArgumentException("Argument columnIndex (= " + columnIndex + ") must be 0 < X < " + m_Types.length);
				}
				
				@Override
				public void setValueAt(Object value, int rowIndex, int columnIndex) {
					Task row = TaskManager.get(rowIndex);
				    if (columnIndex == 2) {
				    	row.setSelected((Boolean)value);
				        fireTableCellUpdated(rowIndex, columnIndex);
				    }
				}
			}
			
			private final class TaskEditor extends AbstractCellEditor implements TableCellEditor
			{
				private TaskEditor()
				{
					super();
				}
				
				public Object getCellEditorValue()
				{
					return 0;
				}
			
				public Component getTableCellEditorComponent(
				    JTable table,
				    Object value,
				    boolean isSelected,
				    int row,
				    int column)
					{
					    super.cancelCellEditing();
					    return null;
					}
			}
			
			private final class TaskRenderer extends DefaultTableCellRenderer
			{
				private JProgressBar m_ProgressBar;
				private JLabel m_LabelIcon;
				private JCheckBox m_CheckBox;
				
				private Color foreground;
				private Color background;
				private Color foregroundString;
				private Color backgroundString;
				
				public TaskRenderer()
				{
				    super();
				    super.setFont(font);
				    
				    m_ProgressBar = new JProgressBar();
				    m_ProgressBar.setMaximum(100);
					m_ProgressBar.setMinimum(0);
					m_ProgressBar.setValue(0);
					m_ProgressBar.setStringPainted(true);
					m_ProgressBar.setString("");
					foreground = m_ProgressBar.getForeground();
					background = m_ProgressBar.getBackground();
					foregroundString = foreground;
					backgroundString = background;
					m_ProgressBar.setUI(new BasicProgressBarUI() {
						protected Color getSelectionBackground() { return backgroundString; }
						protected Color getSelectionForeground() { return foregroundString; }
					});
					
					m_LabelIcon = new JLabel();
					
					m_CheckBox = new JCheckBox();
				}
			
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					super.getTableCellRendererComponent(
				        table,
				        value,
				        isSelected,
				        hasFocus,
				        row,
				        column);
					if(table.getModel().getRowCount() < 1)
						return this;
					if(column == 1)
					{
						Task task = (Task) getValueAt(row, -1);
						m_ProgressBar.setValue(task.getProgress());
						m_ProgressBar.setString(task.getMessage());
						if(!isSelected) {
							m_ProgressBar.setBackground(background);
							m_ProgressBar.setForeground(foreground);
							foregroundString = background;
							backgroundString = foreground;
						} else {
							m_ProgressBar.setBackground(foreground);
							m_ProgressBar.setForeground(background);
							foregroundString = foreground;
							backgroundString = background;
						}
						return m_ProgressBar;
					}
					if(value instanceof Task.Info)
					{
						Task.Info info = (Task.Info) value;
						switch (info)
						{
						case COMPLETED:
							m_LabelIcon.setIcon(Icon.task_info_completed);
							break;
						case ERROR:
							m_LabelIcon.setIcon(Icon.task_info_error);
							break;
						case IDLE:
							m_LabelIcon.setIcon(Icon.task_info_idle);
							break;
						case PAUSED:
							m_LabelIcon.setIcon(Icon.task_info_paused);
							break;
						case RUNNING:
							m_LabelIcon.setIcon(Icon.task_info_running);
							break;
						case WARNING:
							m_LabelIcon.setIcon(Icon.task_info_warning);
							break;
						}
						m_LabelIcon.setToolTipText("" + info);
						return m_LabelIcon;
					}
					if(value instanceof Boolean)
					{
						Boolean selected = (Boolean) value;
						m_CheckBox.setSelected(selected);
						return m_CheckBox;
					}
					return this;
				}
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("task-exec"))
					dataChanged();
				if(evt.getPropertyName().equals("task-info"))
					dataChanged();
			}
		}
		
		private final class TaskUI extends JPanel implements LayoutManager, ActionListener, PropertyChangeListener
		{
			private Task m_Task;
			private JLabel m_LabelTitle;
			private JLabel m_LabelPreview;
			private JButton m_ButtonClose;
			
			private JButton m_ButtonOpenFolder;
			private JButton m_ButtonOpenXML;
			private JButton m_ButtonOpenBook;
			private JButton m_ButtonRunAgain;
			private JButton m_ButtonSkipDuplicate;
			private JButton m_ButtonImportBID;
			
			private JTabbedPane m_TabbedPaneImage;
			
			public TaskUI()
			{
				super();
				setLayout(this);
				setSize(280, 280);
				setMinimumSize(new Dimension(280, 280));
				setMaximumSize(new Dimension(280, 280));
				setPreferredSize(new Dimension(280, 280));
				
				m_LabelTitle = new JLabel();
				m_LabelTitle.setText("");
				m_LabelTitle.setIcon(null);
				add(m_LabelTitle);
				m_LabelPreview = new JLabel();
				m_LabelPreview.setIcon(Icon.task_preview_missing);
				m_LabelPreview.setHorizontalAlignment(JLabel.CENTER);
				m_LabelPreview.setVerticalAlignment(JLabel.CENTER);
				add(m_LabelPreview);
				m_ButtonClose = new JButton(Icon.cancel);
				m_ButtonClose.setSelected(true);
				m_ButtonClose.addActionListener(this);
				add(m_ButtonClose);
				
				m_ButtonOpenFolder = new JButton();
				m_ButtonOpenFolder.setText("Open Folder");
				m_ButtonOpenFolder.setIcon(Icon.task_folder);
				m_ButtonOpenFolder.setSelected(false);
				m_ButtonOpenFolder.setFocusable(false);
				m_ButtonOpenFolder.addActionListener(this);
				m_ButtonOpenFolder.setOpaque(false);
				m_ButtonOpenFolder.setIconTextGap(5);
				m_ButtonOpenFolder.setMargin(new Insets(0,0,0,0));
				m_ButtonOpenFolder.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonOpenFolder.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonOpenFolder);
				m_ButtonOpenXML = new JButton();
				m_ButtonOpenXML.setText("View Response");
				m_ButtonOpenXML.setIcon(Icon.task_xml);
				m_ButtonOpenXML.setSelected(false);
				m_ButtonOpenXML.setFocusable(false);
				m_ButtonOpenXML.addActionListener(this);
				m_ButtonOpenXML.setOpaque(false);
				m_ButtonOpenXML.setIconTextGap(5);
				m_ButtonOpenXML.setMargin(new Insets(0,0,0,0));
				m_ButtonOpenXML.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonOpenXML.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonOpenXML);
				m_ButtonOpenBook = new JButton();
				m_ButtonOpenBook.setText("Open Book");
				m_ButtonOpenBook.setIcon(Icon.task_book);
				m_ButtonOpenBook.setSelected(false);
				m_ButtonOpenBook.setFocusable(false);
				m_ButtonOpenBook.addActionListener(this);
				m_ButtonOpenBook.setOpaque(false);
				m_ButtonOpenBook.setIconTextGap(5);
				m_ButtonOpenBook.setMargin(new Insets(0,0,0,0));
				m_ButtonOpenBook.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonOpenBook.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonOpenBook);
				
				m_ButtonRunAgain = new JButton();
				m_ButtonRunAgain.setText("Re-run Step");
				m_ButtonRunAgain.setIcon(Icon.task_reset);
				m_ButtonRunAgain.setSelected(false);
				m_ButtonRunAgain.setFocusable(false);
				m_ButtonRunAgain.addActionListener(this);
				m_ButtonRunAgain.setOpaque(false);
				m_ButtonRunAgain.setIconTextGap(5);
				m_ButtonRunAgain.setMargin(new Insets(0,0,0,0));
				m_ButtonRunAgain.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonRunAgain.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonRunAgain);
				m_ButtonSkipDuplicate = new JButton();
				m_ButtonSkipDuplicate.setText("Skip Duplicate");
				m_ButtonSkipDuplicate.setIcon(Icon.task_skip);
				m_ButtonSkipDuplicate.setSelected(false);
				m_ButtonSkipDuplicate.setFocusable(false);
				m_ButtonSkipDuplicate.addActionListener(this);
				m_ButtonSkipDuplicate.setOpaque(false);
				m_ButtonSkipDuplicate.setIconTextGap(5);
				m_ButtonSkipDuplicate.setMargin(new Insets(0,0,0,0));
				m_ButtonSkipDuplicate.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonSkipDuplicate.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonSkipDuplicate);
				m_ButtonImportBID = new JButton();
				m_ButtonImportBID.setText("Import from mugimugi ID");
				m_ButtonImportBID.setIcon(Icon.task_import);
				m_ButtonImportBID.setSelected(false);
				m_ButtonImportBID.setFocusable(false);
				m_ButtonImportBID.addActionListener(this);
				m_ButtonImportBID.setOpaque(false);
				m_ButtonImportBID.setIconTextGap(5);
				m_ButtonImportBID.setMargin(new Insets(0,0,0,0));
				m_ButtonImportBID.setHorizontalAlignment(SwingConstants.LEFT);
				m_ButtonImportBID.setHorizontalTextPosition(SwingConstants.RIGHT);
				add(m_ButtonImportBID);
				
				m_TabbedPaneImage = new JTabbedPane();
				m_TabbedPaneImage.setFocusable(false);
				m_TabbedPaneImage.setTabPlacement(JTabbedPane.RIGHT);
				m_TabbedPaneImage.addChangeListener(new ChangeListener()
				{
                    @Override
                    public void stateChanged(ChangeEvent ce) {
                        if (ce.getSource() instanceof JTabbedPane) {
                            JTabbedPane pane = (JTabbedPane) ce.getSource();
                            if(pane.getSelectedIndex() == -1)
                            	return;
                            JButton selectedTab = (JButton) pane.getComponentAt(pane.getSelectedIndex());
                            m_ButtonImportBID.setActionCommand(selectedTab.getActionCommand());
                            m_ButtonImportBID.setText("Import from mugimugi ID [" + m_ButtonImportBID.getActionCommand() + "]");
                        }
                    }
                });
				add(m_TabbedPaneImage);
				
				new SwingWorker<Void,String>()
				{
					private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					private String prevData = "";
					private Pattern pattern = Pattern.compile(DOUJINSHIDB_REGEXP);
					
					@Override
					protected Void doInBackground() throws Exception {
						Thread.currentThread().setName("plugin/doujinshidb-scanner/clipboard-monitor");
						while(true)
							try
							{
								// Prevent CPU hogging
								Thread.sleep(500);
								// Read clipboard data
								String data = (String) clipboard.getData(DataFlavor.stringFlavor);
								// Skip parsing data if it's the same as before
								if(data.equals(prevData))
									continue;
								else
									prevData = data;
								// Parse clipboard data
								Matcher matcher = pattern.matcher(data);
								if(matcher.find())
								{
									String mugimugi_id = "B" + matcher.group(3);
									publish(mugimugi_id);
								}
							} catch (ClassCastException | UnsupportedFlavorException | PatternSyntaxException | IOException ee) {
							} catch (Exception e)
							{
								e.printStackTrace();
							}
					}
					@Override
				    protected void process(List<String> chunks) {
						String mugimugi_id = chunks.iterator().next();
						m_ButtonImportBID.setActionCommand(mugimugi_id);
	                    m_ButtonImportBID.setText("Import from mugimugi ID [" + mugimugi_id + "]");
					}
					@Override
					protected void done() { }
				}.execute();
				
				TaskManager.registerListener(this);
			}
			
			private void fireInfoUpdated()
			{
				switch (m_Task.getInfo())
				{
				case COMPLETED:
					m_LabelTitle.setIcon(Icon.task_info_completed);
					break;
				case ERROR:
					m_LabelTitle.setIcon(Icon.task_info_error);
					break;
				case IDLE:
					m_LabelTitle.setIcon(Icon.task_info_idle);
					break;
				case PAUSED:
					m_LabelTitle.setIcon(Icon.task_info_paused);
					break;
				case RUNNING:
					m_LabelTitle.setIcon(Icon.task_info_running);
					break;
				case WARNING:
					m_LabelTitle.setIcon(Icon.task_info_warning);
					break;
				}
			}
			
			private void fireImageUpdated()
			{
				m_LabelPreview.setIcon(Icon.loading);
				new SwingWorker<ImageIcon, Void>()
				{
					@Override
					protected ImageIcon doInBackground() throws Exception
					{
						return new ImageIcon(
							ImageTool.read(
								new File(DoujinshiDBScanner.PLUGIN_QUERY, m_Task.getId() + ".png")));
					}
					@Override
				    protected void process(List<Void> chunks) { ; }
					@Override
				    public void done() {
				        ImageIcon icon;
				        try {
				        	icon = get();
				        	m_LabelPreview.setIcon(icon);
				        } catch (Exception e) {
				        	m_LabelPreview.setIcon(Icon.task_preview_missing);
				        }
				    }
				}.execute();
			}
			
			private void fireItemsUpdated()
			{
				m_TabbedPaneImage.removeAll();
				m_ButtonImportBID.setText("Import from mugimugi ID");
				
				new SwingWorker<Void, Map<String,Object>>()
				{
					private transient int selectedTab = -1;
					
					@Override
					protected Void doInBackground() throws Exception
					{
						if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) || m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
						{
							for(String id : m_Task.getDuplicatelist())
							{
								try
								{
									// Load images from local DataStore
									ImageIcon ii = new ImageIcon(
										ImageTool.read(
											DataStore.getCover(id).getInputStream()));
									Map<String,Object> data = new HashMap<String,Object>();
									data.put("id", id);
									data.put("imageicon", ii);
									publish(data);
								} catch (Exception e) { e.printStackTrace(); }
							}
						}
						if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
						{
							for(String id : m_Task.getMugimugiList())
							{
								try
								{
									File file = new File(DoujinshiDBScanner.PLUGIN_IMAGECACHE, id + ".jpg");
									ImageIcon ii = new ImageIcon(ImageIO.read(file));
									Map<String,Object> data = new HashMap<String,Object>();
									data.put("id", id);
									data.put("imageicon", ii);
									publish(data);
								} catch (Exception e) { e.printStackTrace(); }
							}
						}
						return null;
					}
					@Override
				    protected void process(List<Map<String,Object>> chunks)
					{
						for(Map<String,Object> data : chunks)
						{
							final String id = (String) data.get("id");
							final ImageIcon imageicon = (ImageIcon) data.get("imageicon");
							
							JButton button = new JButton(imageicon);
							button.setActionCommand(id);
							button.setFocusable(false);
							if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) || m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
							{
								// Open local Book
								button.addActionListener(new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent ae) {
										new SwingWorker<Void, Void>()
										{
											@Override
											protected Void doInBackground() throws Exception
											{
												QueryBook qid = new QueryBook();
												qid.ID = id;
												RecordSet<Book> set = DataBase.getBooks(qid);
												if(set.size() == 1)
													UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
												return null;
											}
										}.execute();
									}
								});
								m_TabbedPaneImage.addTab("", button);
							}
							if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
							{
								// Open mugimugi Book
								button.addActionListener(new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent ae) {
										try {
											int bid = Integer.parseInt(id.substring(1));
											URI uri = new URI(DoujinshiDBScanner.DOUJINSHIDB_URL + "book/" + bid + "/");
											Desktop.getDesktop().browse(uri);
										} catch (URISyntaxException urise) {
											urise.printStackTrace();
										} catch (IOException ioe) {
											ioe.printStackTrace();
										}
									}
								});
								if((m_Task.getMugimugiBid()).equals(id))
								{
									m_TabbedPaneImage.addTab("", Icon.task_searchquery_star, button);
									selectedTab = m_TabbedPaneImage.getTabCount() - 1;
								}
								else
									m_TabbedPaneImage.addTab("", button);
							}
						}
					}
					@Override
				    protected void done()
					{
						if(selectedTab != -1)
							m_TabbedPaneImage.setSelectedIndex(selectedTab);
					}
				}.execute();
			}

			public void setTask(Task task)
			{
				// Set displaying Task
				this.m_Task = task;
				// Display UUID
				m_LabelTitle.setText("UUID : " + m_Task.getId());
				// Display 'status' Icon
				fireInfoUpdated();
				// Display scanned Image
				fireImageUpdated();
				// Display duplicate/queried Items
				fireItemsUpdated();
			}

			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth(),
					height = parent.getHeight();
				
				m_LabelTitle.setBounds(0, 0, width - 80, 20);
				m_LabelPreview.setBounds(0, 20, 200, 256);
				m_ButtonClose.setBounds(width - 20, 0, 20, 20);
				
				m_ButtonOpenFolder.setBounds(200, 20, (width - 200) / 2, 20);
				m_ButtonRunAgain.setBounds(200, 40, (width - 200) / 2, 20);
				m_ButtonOpenXML.setBounds(200, 60, (width - 200) / 2, 20);
				m_ButtonOpenBook.setBounds(200 + (width - 200) / 2, 20, (width - 200) / 2, 20);
				m_ButtonSkipDuplicate.setBounds(200 + (width - 200) / 2, 40, (width - 200) / 2, 20);
				m_ButtonImportBID.setBounds(200 + (width - 200) / 2, 60, (width - 200) / 2, 20);
				m_TabbedPaneImage.setBounds(0, 0, 0, 0);
				if(m_Task.isRunning())
				{
					m_ButtonOpenFolder.setEnabled(true);
					m_ButtonRunAgain.setEnabled(false);
					m_ButtonOpenXML.setEnabled(false);
					m_ButtonOpenBook.setEnabled(false);
					m_ButtonSkipDuplicate.setEnabled(false);
					m_ButtonImportBID.setEnabled(false);
					return;
				}
				if(m_Task.getInfo().equals(Task.Info.COMPLETED))
				{
					m_ButtonOpenFolder.setEnabled(true);
					m_ButtonRunAgain.setEnabled(false);
					m_ButtonOpenXML.setEnabled(false);
					m_ButtonOpenBook.setEnabled(true);
					m_ButtonSkipDuplicate.setEnabled(false);
					m_ButtonImportBID.setEnabled(false);
					return;
				}
				if(m_Task.getInfo().equals(Task.Info.IDLE))
				{
					m_ButtonOpenFolder.setEnabled(true);
					m_ButtonRunAgain.setEnabled(false);
					m_ButtonOpenXML.setEnabled(false);
					m_ButtonOpenBook.setEnabled(false);
					m_ButtonSkipDuplicate.setEnabled(false);
					m_ButtonImportBID.setEnabled(false);
					return;
				}
				if(m_Task.getInfo().equals(Task.Info.WARNING))
				{
					m_ButtonOpenFolder.setEnabled(true);
					m_ButtonRunAgain.setEnabled(false);
					m_ButtonOpenBook.setEnabled(false);
					if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) ||
						m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
					{
						m_ButtonOpenXML.setEnabled(false);
						m_ButtonSkipDuplicate.setEnabled(true);
						m_ButtonImportBID.setEnabled(false);
						m_TabbedPaneImage.setBounds(200, 80, width - 200, height - 80);
					}
					if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
					{
						m_ButtonOpenXML.setEnabled(true);
						m_ButtonSkipDuplicate.setEnabled(false);
						m_ButtonImportBID.setEnabled(true);
						m_TabbedPaneImage.setBounds(200, 80, width - 200, height - 80);
					}
					return;
				}
				if(m_Task.getInfo().equals(Task.Info.ERROR))
				{
					m_ButtonOpenFolder.setEnabled(true);
					m_ButtonRunAgain.setEnabled(true);
					if(m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY) ||
						m_Task.getExec().equals(Task.Exec.PARSE_XML) ||
						m_Task.getExec().equals(Task.Exec.PARSE_BID) ||
						m_Task.getExec().equals(Task.Exec.SAVE_DATABASE) ||
						m_Task.getExec().equals(Task.Exec.SAVE_DATASTORE))
						m_ButtonOpenXML.setEnabled(true);
					m_ButtonOpenBook.setEnabled(false);
					m_ButtonSkipDuplicate.setEnabled(false);
					m_ButtonImportBID.setEnabled(false);
					return;
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
			    return new Dimension(280, 280);
			}
			
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(ae.getSource() == m_ButtonClose)
				{
					m_SplitPane.setBottomComponent(null);
					m_PanelTasks.clearSelection();
					return;
				}
				if(ae.getSource() == m_ButtonOpenFolder)
				{
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							try {
								URI uri = new File(m_Task.getPath()).toURI();
								Desktop.getDesktop().browse(uri);
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
							return null;
						}
					}.execute();
					return;
				}
				if(ae.getSource() == m_ButtonOpenXML)
				{
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							try {
								URI uri = new File(DoujinshiDBScanner.PLUGIN_QUERY, m_Task.getId() + ".xml").toURI();
								Desktop.getDesktop().browse(uri);
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
							return null;
						}
					}.execute();
					return;
				}
				if(ae.getSource() == m_ButtonOpenBook)
				{
					if(m_Task.getBook() == null)
						return;
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							String bookid = m_Task.getBook();
							if(bookid != null)
							{
								QueryBook qid = new QueryBook();
								qid.ID = bookid;
								RecordSet<Book> set = DataBase.getBooks(qid);
								if(set.size() == 1)
									UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
							}
							return null;
						}
					}.execute();
					return;
				}
				if(ae.getSource() == m_ButtonImportBID)
				{
					m_Task.setMugimugiBid(m_ButtonImportBID.getActionCommand());
					m_Task.setInfo(Task.Info.IDLE);
					return;
				}
				if(ae.getSource() == m_ButtonSkipDuplicate)
				{
					m_Task.setInfo(Task.Info.IDLE);
					return;
				}
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(m_Task == null)
					return;
				if(evt.getPropertyName().equals("taskmanager-info"))
				{
					doLayout();
					return;
				}
				if(evt.getPropertyName().equals("task-exec"))
				{
					;
				}
				if(evt.getPropertyName().equals("task-info"))
				{
					fireInfoUpdated();
					if(m_Task.getInfo().equals(Task.Info.WARNING) && (
						m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) ||
						m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY) ||
						m_Task.getExec().equals(Task.Exec.PARSE_XML)))
						fireItemsUpdated();
					doLayout();
					return;
				}
				if(evt.getPropertyName().equals("task-image"))
				{
					m_LabelPreview.setIcon(Icon.loading);
					new SwingWorker<ImageIcon, Void>()
					{
						@Override
						protected ImageIcon doInBackground() throws Exception
						{
							return new ImageIcon(
								ImageTool.read(
									new File(DoujinshiDBScanner.PLUGIN_QUERY, m_Task.getId() + ".png")));
						}
						@Override
					    protected void process(List<Void> chunks) { ; }
						@Override
					    public void done() {
					        ImageIcon icon;
					        try {
					        	icon = get();
					        	m_LabelPreview.setIcon(icon);
					        } catch (Exception e) {
					        	m_LabelPreview.setIcon(Icon.task_preview_missing);
					        	doLayout();
					        }
					    }
					}.execute();
					return;
				}
			}
		}

		private final class TaskBuilder extends SwingWorker<Void, Integer>
		{
			@Override
			protected Void doInBackground() throws Exception {
				m_BuilderRunning = true;
				PluginUI.this.doLayout();

				// Reset UI
				m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
				m_ProgressBarCache.setString("Loading ...");
				
				// Init data
				boolean cache_overwrite = m_CheckboxCacheOverwrite.isSelected();
				RecordSet<Book> books = Context.getBooks(null);
				
				m_ProgressBarCache.setMaximum(books.size());
				if(cache_overwrite)
					m_ProgressBarCache.setMinimum(1);
				else
					m_ProgressBarCache.setMinimum((int) CacheManager.size());
				m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
				
				for(Book book : books)
				{
					if(isCancelled())
						return null;
						
					BufferedImage bi;
					try {
						if(CacheManager.contains(book.getID()) && !cache_overwrite)
							continue;
						
						bi = ImageTool.read(DataStore.getCover(book.getID()).getInputStream());
						bi = ImageTool.getScaledInstance(bi, 256, 256, true);
						
						CacheManager.put(book.getID(), bi);
						
						publish(m_ProgressBarCache.getValue()+1);
						
					} catch (Exception e) { e.printStackTrace(); }
				}
				
				// Write Cache
				CacheManager.write();
				
				// Cache build completed
				m_LabelCacheInfo.setText("<html><body>cache-size : " + CacheManager.size() + "<br/>" +
						"last-build : " + sdf.format(CacheManager.timestamp()) + "</body></html>");
				
				return null;
			}

			@Override
			protected void done() {
				m_BuilderRunning = false;
				PluginUI.this.doLayout();
				
				m_ProgressBarCache.setValue(0);
				m_ProgressBarCache.setString("");
				
				super.done();
			}

			@Override
			protected void process(List<Integer> chunks) {
				m_ProgressBarCache.setValue(chunks.get(chunks.size() - 1));
				int progress = m_ProgressBarCache.getValue() * 100 / m_ProgressBarCache.getMaximum();
				m_ProgressBarCache.setString("[" + m_ProgressBarCache.getValue() + " / " + m_ProgressBarCache.getMaximum() + "] @ " + progress + "%");
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
				m_ScannerRunning = true;
				PluginUI.this.doLayout();
				
				// Reset UI
				while (m_TabbedPaneScanResult.getTabCount() > 0)
					m_TabbedPaneScanResult.remove(0);
				m_ButtonScanPreview.setIcon(Icon.task_preview_missing);
				m_ProgressBarScan.setValue(m_ProgressBarScan.getMinimum());
				m_ProgressBarScan.setString("Loading ...");
				
				// Init data
				int max_results = m_SliderMaxResults.getValue();
				
				BufferedImage bi;
				try {
					bi = ImageTool.read(file);
					BufferedImage resized_bi;
					int img_width = bi.getWidth(),
						img_height = bi.getHeight();
					if(img_width > bi.getHeight())
						resized_bi = new BufferedImage(img_width / 2, img_height, BufferedImage.TYPE_INT_RGB);
					else
						resized_bi = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);
					Graphics g = resized_bi.getGraphics();
					g.drawImage(bi, 0, 0, img_width, img_height, null);
					g.dispose();
					bi = ImageTool.getScaledInstance(resized_bi, 256, 256, true);
					
					m_ButtonScanPreview.setIcon(new ImageIcon(bi));
					
					TreeMap<Double, String> result = CacheManager.search(bi, max_results);
					
					boolean first_result = false;
					for(double index : result.keySet())
					{
						final String book_id = result.get(index);
						if(!first_result)
						{
							JButton button;
							if(DataStore.getCover(book_id).exists())
								button = new JButton(new ImageIcon(ImageTool.read(DataStore.getCover(book_id).getInputStream())));
							else
								button = new JButton(Icon.task_preview_missing);
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									new SwingWorker<Void, Void>()
									{
										@Override
										protected Void doInBackground() throws Exception
										{
											QueryBook qid = new QueryBook();
											qid.ID = book_id;
											RecordSet<Book> set = DataBase.getBooks(qid);
											if(set.size() == 1)
												UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
											return null;
										}
									}.execute();
								}
							});
							first_result = true;
							m_TabbedPaneScanResult.addTab(String.format("%3.2f", index) + "%", Icon.task_searchquery_star, button);
						} else
						{
							JButton button;
							if(DataStore.getCover(book_id).exists())
								button = new JButton(new ImageIcon(ImageTool.read(DataStore.getCover(book_id).getInputStream())));
							else
								button = new JButton(Icon.task_preview_missing);
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									new SwingWorker<Void, Void>()
									{
										@Override
										protected Void doInBackground() throws Exception
										{
											QueryBook qid = new QueryBook();
											qid.ID = book_id;
											RecordSet<Book> set = DataBase.getBooks(qid);
											if(set.size() == 1)
												UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
											return null;
										}
									}.execute();								}
							});
							m_TabbedPaneScanResult.addTab(String.format("%3.2f", index) + "%", button);
						}
					}
					
					m_ProgressBarScan.setValue(m_ProgressBarScan.getMaximum());
					m_ProgressBarScan.setString("Completed");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
			
			@Override
			protected void done() {
				m_ScannerRunning = false;
				PluginUI.this.doLayout();
				super.done();
			}

			@Override
			protected void process(List<Integer> chunks) {
				super.process(chunks);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("taskmanager-info"))
			{
				if(TaskManager.isRunning())
					m_ButtonTaskManagerCtl.setIcon(Icon.task_pause);
				else
					m_ButtonTaskManagerCtl.setIcon(Icon.task_resume);
				return;
			}
			if(evt.getPropertyName().equals("api-info"))
			{
				doLayout();
				return;
			}
		}
		
		@Override
		public void configAdded(String key) { }

		@Override
		public void configDeleted(String key) { }

		@Override
		public void configUpdated(final String key)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if(key.equals(configBase + "apikey"))
					{
						if(!m_TextApikey.getText().equals((String) Configuration.configRead(key)))
							m_TextApikey.setText((String) Configuration.configRead(key));
						return;
					}
					if(key.equals(configBase + "threshold"))
					{
						if(m_SliderApiThreshold.getValue() != (Integer) Configuration.configRead(key))
							m_SliderApiThreshold.setValue((Integer) Configuration.configRead(key));
						return;
					}
					if(key.equals(configBase + "resize_cover"))
					{
						if(m_CheckboxApiResizeImage.isSelected() != (Boolean) Configuration.configRead(key))
							m_CheckboxApiResizeImage.setSelected((Boolean) Configuration.configRead(key));
						return;
					}
				}
			});
		}
	}

	@Override
	protected void install() throws TaskErrorException { }

	@Override
	protected void update() throws TaskErrorException { }

	@Override
	protected void uninstall() throws TaskErrorException { }
	
	@Override
	protected void startup() throws TaskErrorException
	{
		Context = DataBase.getContext(UUID);
		
		APIKEY = (String) Configuration.configRead(configBase + "apikey");
		THRESHOLD = (Integer) Configuration.configRead(configBase + "threshold");
		RESIZE_COVER = (Boolean) Configuration.configRead(configBase + "resize_cover");
		
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		PLUGIN_HOME.mkdirs();
		PLUGIN_IMAGECACHE.mkdirs();
		PLUGIN_QUERY.mkdirs();

		CacheManager.read();

		TaskManager.loadTasks();
		
		m_UI = new PluginUI();
	}
	
	@Override
	protected void shutdown() throws TaskErrorException
	{
		TaskManager.saveTasks();

		CacheManager.write();
	}
}
