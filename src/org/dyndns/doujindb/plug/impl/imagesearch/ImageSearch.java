package org.dyndns.doujindb.plug.impl.imagesearch;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.event.ConfigurationListener;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.desk.WindowEx;
import org.dyndns.doujindb.util.ImageTool;

public final class ImageSearch extends Plugin
{
	private static final String UUID = "{b8dba99a-8320-4ea4-8891-7fd78555d4fe}";
	private static final String Author = "loli10K";
	private static final String Version = "1.0";
	private static final String Weblink = "https://github.com/loli10K";
	private static final String Name = "Image Search";
	private static final String Description = "Search through then whole DataStore for matching cover images.";
	
	static int THRESHOLD = 75;
	static int MAX_RESULT = 25;
	static int IMAGE_SCALING = 16;
	
	static DataBaseContext Context;
	
	private static JComponent m_UI;
	
	static final File PLUGIN_HOME = new File(Core.DOUJINDB_HOME, "plugins" + File.separator + UUID);
	static final File PLUGIN_IMAGEINDEX = new File(PLUGIN_HOME, "imageindex.ser");
	
	private static SimpleDateFormat sdf;
	private static String configBase = "org.dyndns.doujindb.plugin.imagesearch.";
	private static Font font;
	private static Icons Icon = new Icons();
	
	static
	{
		Configuration.configAdd(configBase + "threshold", "<html><body>Threshold limit for matching cover queries.</body></html>", 75);
		Configuration.configAdd(configBase + "max_result", "<html><body>Max results returned by a single image search.</body></html>", 25);
		Configuration.configAdd(configBase + "image_scaling", "<html><body>Scaling factor of cover image in index file.</body></html>", 16);
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
	private static final class PluginUI extends JPanel implements LayoutManager, ActionListener, ConfigurationListener
	{
		private JTabbedPane m_TabbedPane;
		@SuppressWarnings("unused")
		private JPanel m_TabSettings;
		private JPanel m_TabSearch;
		
		private JLabel m_LabelThreshold;
		private JSlider m_SliderThreshold;
		private JLabel m_LabelMaxResults;
		private JSlider m_SliderMaxResults;
		private JLabel m_LabelScaling;
		private JSlider m_SliderScaling;
		private JCheckBox m_CheckboxCacheOverwrite;
		private JProgressBar m_ProgressBarCache;
		private JButton m_ButtonCacheBuild;
		private JButton m_ButtonCacheCancel;
		private JLabel m_LabelCacheInfo;
		
		private JButton m_ButtonScanPreview;
		private JTabbedPane m_TabbedPaneScanResult;
		
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
			m_LabelThreshold = new JLabel();
			m_LabelThreshold.setText("Threshold : " + THRESHOLD);
			m_LabelThreshold.setFont(font);
			bogus.add(m_LabelThreshold);
			m_SliderThreshold = new JSlider(1, 100);
			m_SliderThreshold.setValue(THRESHOLD);
			m_SliderThreshold.setFont(font);
			m_SliderThreshold.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					m_LabelThreshold.setText("Threshold : " + m_SliderThreshold.getValue());
				}				
			});
			bogus.add(m_SliderThreshold);
			m_LabelMaxResults = new JLabel();
			m_LabelMaxResults.setText("Max Results : " + MAX_RESULT);
			m_LabelMaxResults.setFont(font);
			bogus.add(m_LabelMaxResults);
			m_SliderMaxResults = new JSlider(1, 25);
			m_SliderMaxResults.setValue(MAX_RESULT);
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
			m_LabelScaling = new JLabel();
			m_LabelScaling.setText("Scaling : " + IMAGE_SCALING);
			m_LabelScaling.setFont(font);
			bogus.add(m_LabelScaling);
			m_SliderScaling = new JSlider(1, 25);
			m_SliderScaling.setValue(IMAGE_SCALING);
			m_SliderScaling.setFont(font);
			m_SliderScaling.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					m_LabelScaling.setText("Scaling : " + m_SliderScaling.getValue());
				}				
			});
			bogus.add(m_SliderScaling);
			m_TabbedPane.addTab("Settings", Icon.settings, m_TabSettings = bogus);

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
	                    				m_TabSearch.doLayout();
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
			m_TabbedPane.addTab("Search", Icon.search, m_TabSearch = bogus);
			
			super.add(m_TabbedPane);
			
			Configuration.addConfigurationListener(this);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_TabbedPane.setBounds(0,0,width,height);
			m_LabelThreshold.setBounds(5,5,(width-10)/2,20);
			m_SliderThreshold.setBounds(5+(width-10)/2,5,(width-10)/2,20);
			m_LabelMaxResults.setBounds(5,25,(width-10)/2,20);
			m_SliderMaxResults.setBounds(5+(width-10)/2,25,(width-10)/2,20);
			m_LabelScaling.setBounds(5,45,(width-10)/2,20);
			m_SliderScaling.setBounds(5+(width-10)/2,45,(width-10)/2,20);
			m_ButtonScanPreview.setBounds(5,5,180,256);
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
						
						bi = ImageTool.read(DataStore.getThumbnail(book.getID()).getInputStream());
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
				m_ButtonScanPreview.setIcon(Icon.search_missing);
				
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
							try {
								button = new JButton(new ImageIcon(ImageTool.read(DataStore.getThumbnail(book_id).getInputStream())));
							} catch (DataStoreException dse) {
								button = new JButton(Icon.search_missing);
							}
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
							m_TabbedPaneScanResult.addTab(String.format("%3.2f", index) + "%", Icon.search_star, button);
						} else
						{
							JButton button;
							try {
								button = new JButton(new ImageIcon(ImageTool.read(DataStore.getThumbnail(book_id).getInputStream())));
							} catch (DataStoreException dse) {
								button = new JButton(Icon.search_missing);
							}
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
					if(key.equals(configBase + "threshold"))
					{
						if(m_SliderThreshold.getValue() != (Integer) Configuration.configRead(key))
							m_SliderThreshold.setValue((Integer) Configuration.configRead(key));
						return;
					}
					if(key.equals(configBase + "max_result"))
					{
						if(m_SliderMaxResults.getValue() != (Integer) Configuration.configRead(key))
							m_SliderMaxResults.setValue((Integer) Configuration.configRead(key));
						return;
					}
					if(key.equals(configBase + "image_scaling"))
					{
						if(m_SliderScaling.getValue() != (Integer) Configuration.configRead(key))
							m_SliderScaling.setValue((Integer) Configuration.configRead(key));
						return;
					}
				}
			});
		}
	}

	@Override
	protected void install() { }

	@Override
	protected void update() { }

	@Override
	protected void uninstall() { }
	
	@Override
	protected void startup()
	{
		Context = DataBase.getContext(UUID);
		
		THRESHOLD = (Integer) Configuration.configRead(configBase + "threshold");
		MAX_RESULT = (Integer) Configuration.configRead(configBase + "max_result");
		IMAGE_SCALING = (Integer) Configuration.configRead(configBase + "image_scaling");
		
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		PLUGIN_HOME.mkdirs();

		CacheManager.read();

		m_UI = new PluginUI();
	}
	
	@Override
	protected void shutdown()
	{
		CacheManager.write();
	}
}
