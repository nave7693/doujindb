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
	private static final String fUUID = "{b8dba99a-8320-4ea4-8891-7fd78555d4fe}";
	private static final String fAuthor = "loli10K";
	private static final String fVersion = "1.0";
	private static final String fWeblink = "https://github.com/loli10K";
	private static final String fName = "Image Search";
	private static final String fDescription = "Search through then whole DataStore for matching cover images.";
	private static JComponent fUI;
	
	static int fThreshold = 75;
	static int fMaxResults = 25;
	static int fImageScaling = 16;
	
	static DataBaseContext fContext;
	
	static final File PLUGIN_HOME = new File(Core.DOUJINDB_HOME, "plugins" + File.separator + fUUID);
	static final File PLUGIN_IMAGEINDEX = new File(PLUGIN_HOME, "imageindex.ser");
	
	private static SimpleDateFormat fSDF;
	private static String fConfigBase = "org.dyndns.doujindb.plugin.imagesearch.";
	private static Font fFont;
	private static Icons fIcons = new Icons();
	
	static
	{
		Configuration.configAdd(fConfigBase + "threshold", "<html><body>Threshold limit for matching cover queries.</body></html>", 75);
		Configuration.configAdd(fConfigBase + "max_result", "<html><body>Max results returned by a single image search.</body></html>", 25);
		Configuration.configAdd(fConfigBase + "image_scaling", "<html><body>Scaling factor of cover image in index file.</body></html>", 16);
	}
	
	@Override
	public String getUUID() {
		return fUUID;
	}
	
	@Override
	public Icon getIcon() {
		return fIcons.icon;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getDescription() {
		return fDescription;
	}
	
	@Override
	public String getVersion() {
		return fVersion;
	}
	
	@Override
	public String getAuthor() {
		return fAuthor;
	}
	
	@Override
	public String getWeblink() {
		return fWeblink;
	}
	
	@Override
	public JComponent getUI() {
		return fUI;
	}
	
	@SuppressWarnings("serial")
	private static final class PluginUI extends JPanel implements LayoutManager, ActionListener, ConfigurationListener {
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
		
		public PluginUI() {
			super();
			setLayout(this);
			m_TabbedPane = new JTabbedPane();
			m_TabbedPane.setFont(fFont = UI.Font);
			m_TabbedPane.setFocusable(false);
			
			JPanel bogus;
			
			bogus = new JPanel();
			bogus.setLayout(null);
			m_LabelThreshold = new JLabel();
			m_LabelThreshold.setText("Threshold : " + fThreshold);
			m_LabelThreshold.setFont(fFont);
			bogus.add(m_LabelThreshold);
			m_SliderThreshold = new JSlider(1, 100);
			m_SliderThreshold.setValue(fThreshold);
			m_SliderThreshold.setFont(fFont);
			m_SliderThreshold.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					fThreshold = m_SliderThreshold.getValue();
					m_LabelThreshold.setText("Threshold : " + fThreshold);
				}				
			});
			bogus.add(m_SliderThreshold);
			m_LabelMaxResults = new JLabel();
			m_LabelMaxResults.setText("Max Results : " + fMaxResults);
			m_LabelMaxResults.setFont(fFont);
			bogus.add(m_LabelMaxResults);
			m_SliderMaxResults = new JSlider(1, 25);
			m_SliderMaxResults.setValue(fMaxResults);
			m_SliderMaxResults.setFont(fFont);
			m_SliderMaxResults.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					fMaxResults = m_SliderMaxResults.getValue();
					m_LabelMaxResults.setText("Max Results : " + fMaxResults);
				}				
			});
			bogus.add(m_SliderMaxResults);
			m_LabelScaling = new JLabel();
			m_LabelScaling.setText("Scaling : " + fImageScaling);
			m_LabelScaling.setFont(fFont);
			bogus.add(m_LabelScaling);
			m_SliderScaling = new JSlider(5, 25);
			m_SliderScaling.setValue(fImageScaling);
			m_SliderScaling.setFont(fFont);
			m_SliderScaling.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					fImageScaling = m_SliderScaling.getValue();
					m_LabelScaling.setText("Scaling : " + fImageScaling);
				}				
			});
			bogus.add(m_SliderScaling);
			m_CheckboxCacheOverwrite = new JCheckBox();
			m_CheckboxCacheOverwrite.setSelected(false);
			m_CheckboxCacheOverwrite.setFocusable(false);
			m_CheckboxCacheOverwrite.setText("Overwrite existing entries");
			bogus.add(m_CheckboxCacheOverwrite);
			m_ButtonCacheBuild = new JButton(fIcons.worker_start);
			m_ButtonCacheBuild.addActionListener(this);
			m_ButtonCacheBuild.setBorder(null);
			m_ButtonCacheBuild.setFocusable(false);
			bogus.add(m_ButtonCacheBuild);
			m_ButtonCacheCancel = new JButton(fIcons.worker_stop);
			m_ButtonCacheCancel.addActionListener(this);
			m_ButtonCacheCancel.setBorder(null);
			m_ButtonCacheCancel.setFocusable(false);
			bogus.add(m_ButtonCacheCancel);
			m_ProgressBarCache = new JProgressBar();
			m_ProgressBarCache.setFont(fFont);
			m_ProgressBarCache.setMaximum(100);
			m_ProgressBarCache.setMinimum(1);
			m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
			m_ProgressBarCache.setStringPainted(true);
			m_ProgressBarCache.setString("");
			bogus.add(m_ProgressBarCache);
			m_LabelCacheInfo = new JLabel("<html><body>cache-size : " + CacheManager.size() + "<br/>" +
					"last-build : " + fSDF.format(CacheManager.timestamp()) + "</body></html>");
			m_LabelCacheInfo.setFont(fFont);
			m_LabelCacheInfo.setVerticalAlignment(JLabel.TOP);
			bogus.add(m_LabelCacheInfo);
			m_TabbedPane.addTab("Settings", fIcons.settings, m_TabSettings = bogus);

			bogus = new JPanel();
			bogus.setLayout(null);
			m_ButtonScanPreview = new JButton();
			m_ButtonScanPreview.setIcon(fIcons.search_preview);
			m_ButtonScanPreview.addActionListener(this);
			m_ButtonScanPreview.setBorder(null);
			m_ButtonScanPreview.setOpaque(false);
			m_ButtonScanPreview.setDropTarget(new DropTarget() {
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde) {
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						if(m_ScannerRunning) {
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
				public synchronized void drop(DropTargetDropEvent dtde) {
					Transferable transferable = dtde.getTransferable();
	                if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	                    dtde.acceptDrop(dtde.getDropAction());
	                    try {
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
			m_TabbedPaneScanResult.setFont(fFont);
			m_TabbedPaneScanResult.setFocusable(false);
			m_TabbedPaneScanResult.setTabPlacement(JTabbedPane.RIGHT);
			bogus.add(m_TabbedPaneScanResult);
			m_TabbedPane.addTab("Search", fIcons.search, m_TabSearch = bogus);
			
			super.add(m_TabbedPane);
			
			Configuration.addConfigurationListener(this);
		}
		
		@Override
		public void layoutContainer(Container parent) {
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_TabbedPane.setBounds(0,0,width,height);
			m_LabelThreshold.setBounds(5,5,(width-10)/2,20);
			m_SliderThreshold.setBounds(5+(width-10)/2,5,(width-10)/2,20);
			m_LabelMaxResults.setBounds(5,25,(width-10)/2,20);
			m_SliderMaxResults.setBounds(5+(width-10)/2,25,(width-10)/2,20);
			m_LabelScaling.setBounds(5,45,(width-10)/2,20);
			m_SliderScaling.setBounds(5+(width-10)/2,45,(width-10)/2,20);
			if(!m_BuilderRunning)
			{
				m_ButtonCacheBuild.setBounds(5,75,20,20);
				m_ButtonCacheCancel.setBounds(0,0,0,0);
			} else {
				m_ButtonCacheBuild.setBounds(0,0,0,0);
				m_ButtonCacheCancel.setBounds(5,75,20,20);
			}
			m_ProgressBarCache.setBounds(30,75,width-40,20);
			m_CheckboxCacheOverwrite.setBounds(5,95,width-10,20);
			m_LabelCacheInfo.setBounds(5,115,width,50);
			m_ButtonScanPreview.setBounds(5,5,180,256);
			m_TabbedPaneScanResult.setBounds(190,5,width-190,height-10);
		}
		
		@Override
		public void addLayoutComponent(String key,Component c) { }
		
		@Override
		public void removeLayoutComponent(Component c) { }
		
		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(350,350);
		}
		
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(350,350);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
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
		}
		
		private final class TaskBuilder extends SwingWorker<Void, Integer> {
			@Override
			protected Void doInBackground() throws Exception {
				m_BuilderRunning = true;
				PluginUI.this.doLayout();

				// Reset UI
				m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
				m_ProgressBarCache.setString("Loading ...");
				
				// Init data
				boolean cache_overwrite = m_CheckboxCacheOverwrite.isSelected();
				RecordSet<Book> books = fContext.getBooks(null);
				
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
						"last-build : " + fSDF.format(CacheManager.timestamp()) + "</body></html>");
				
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

		private final class TaskScanner extends SwingWorker<Void, Integer> {
			final private File file;
			
			private TaskScanner(final File file) {
				this.file = file;
			}
			
			@Override
			protected Void doInBackground() throws Exception {
				m_ScannerRunning = true;
				PluginUI.this.doLayout();
				
				// Reset UI
				while (m_TabbedPaneScanResult.getTabCount() > 0)
					m_TabbedPaneScanResult.remove(0);
				m_ButtonScanPreview.setIcon(fIcons.search_missing);
				
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
								button = new JButton(fIcons.search_missing);
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
							m_TabbedPaneScanResult.addTab(String.format("%3.2f", index) + "%", fIcons.search_star, button);
						} else
						{
							JButton button;
							try {
								button = new JButton(new ImageIcon(ImageTool.read(DataStore.getThumbnail(book_id).getInputStream())));
							} catch (DataStoreException dse) {
								button = new JButton(fIcons.search_missing);
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
		public void configUpdated(final String key) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run()
				{
					if(key.equals(fConfigBase + "threshold"))
					{
						if(m_SliderThreshold.getValue() != (Integer) Configuration.configRead(key))
							m_SliderThreshold.setValue((Integer) Configuration.configRead(key));
						return;
					}
					if(key.equals(fConfigBase + "max_result"))
					{
						if(m_SliderMaxResults.getValue() != (Integer) Configuration.configRead(key))
							m_SliderMaxResults.setValue((Integer) Configuration.configRead(key));
						return;
					}
					if(key.equals(fConfigBase + "image_scaling"))
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
	protected void startup() {
		fContext = DataBase.getContext(fUUID);
		
		fThreshold = (Integer) Configuration.configRead(fConfigBase + "threshold");
		fMaxResults = (Integer) Configuration.configRead(fConfigBase + "max_result");
		fImageScaling = (Integer) Configuration.configRead(fConfigBase + "image_scaling");
		
		fSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		fSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		PLUGIN_HOME.mkdirs();

		CacheManager.read();

		fUI = new PluginUI();
	}
	
	@Override
	protected void shutdown() {
		CacheManager.write();
	}
}
