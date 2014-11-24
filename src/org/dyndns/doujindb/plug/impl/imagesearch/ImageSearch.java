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
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import org.dyndns.doujindb.conf.ConfigurationParser;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.PanelConfiguration;
import org.dyndns.doujindb.util.ImageTool;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public final class ImageSearch extends Plugin
{
	private final String fAuthor = "loli10K";
	private final String fVersion = "1.0";
	private final String fWeblink = "https://github.com/loli10K";
	private final String fName = "Image Search";
	private final String fDescription = "Search through then whole DataStore for matching cover images.";
	private JComponent fUI = new PluginUI();
	
	private File HASHDB_FILE = new File(this.PLUGIN_HOME, "hashdb.ser");
	private Map<String, Integer> HASHDB_MAP = new HashMap<String, Integer>();
	
	private static Icons fIcons = new Icons();
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(ImageSearch.class);
	
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
	private final class PluginUI extends JPanel implements ActionListener
	{
		private JTabbedPane m_TabbedPane;
		private JPanel m_TabSearch;
		@SuppressWarnings("unused")
		private JPanel m_TabConfiguration;
		
		private JProgressBar m_ProgressBarCache;
		private JButton m_ButtonCacheBuild;
		private JButton m_ButtonCacheCancel;
		
		private JButton m_ButtonScanPreview;
		private JTabbedPane m_TabbedPaneScanResult;
		
		private SwingWorker<Void, Integer> m_WorkerScanner = new TaskScanner(null);
		private boolean m_ScannerRunning = false;
		private SwingWorker<Void, Integer> m_WorkerBuilder = new TaskBuilder();
		private boolean m_BuilderRunning = false;
		
		public PluginUI() {
			super();
			super.setLayout(new GridLayout(1,1));
			m_TabbedPane = new JTabbedPane();
			m_TabbedPane.setFont(UI.Font);
			m_TabbedPane.setFocusable(false);
			
			m_TabSearch = new JPanel();
			m_TabSearch.setMinimumSize(new Dimension(350, 350));
			m_TabSearch.setPreferredSize(new Dimension(350, 350));
			m_TabSearch.setLayout(new LayoutManager() {
				@Override
				public void layoutContainer(Container parent) {
					int width = parent.getWidth(),
						height = parent.getHeight();
					if(!m_BuilderRunning) {
						m_ButtonCacheBuild.setBounds(0,0,20,20);
						m_ButtonCacheCancel.setBounds(0,0,0,0);
					} else {
						m_ButtonCacheBuild.setBounds(0,0,0,0);
						m_ButtonCacheCancel.setBounds(0,0,20,20);
					}
					m_ProgressBarCache.setBounds(21,1,width-22,20);
					m_ButtonScanPreview.setBounds(1,25,180,256);
					m_TabbedPaneScanResult.setBounds(190,25,width-190,height-30);
				}
				
				@Override
				public void addLayoutComponent(String key,Component c) { }
				
				@Override
				public void removeLayoutComponent(Component c) { }
				
				@Override
				public Dimension minimumLayoutSize(Container parent) {
					return m_TabbedPane.getMinimumSize();
				}
				
				@Override
				public Dimension preferredLayoutSize(Container parent) {
					return m_TabbedPane.getPreferredSize();
				}
			});
			m_ButtonCacheBuild = new JButton(fIcons.worker_start);
			m_ButtonCacheBuild.addActionListener(this);
			m_ButtonCacheBuild.setBorder(null);
			m_ButtonCacheBuild.setFocusable(false);
			m_TabSearch.add(m_ButtonCacheBuild);
			m_ButtonCacheCancel = new JButton(fIcons.worker_stop);
			m_ButtonCacheCancel.addActionListener(this);
			m_ButtonCacheCancel.setBorder(null);
			m_ButtonCacheCancel.setFocusable(false);
			m_TabSearch.add(m_ButtonCacheCancel);
			m_ProgressBarCache = new JProgressBar();
			m_ProgressBarCache.setFont(UI.Font);
			m_ProgressBarCache.setMaximum(100);
			m_ProgressBarCache.setMinimum(1);
			m_ProgressBarCache.setValue(m_ProgressBarCache.getMinimum());
			m_ProgressBarCache.setStringPainted(true);
			m_ProgressBarCache.setString("");
			m_TabSearch.add(m_ProgressBarCache);
			m_ButtonScanPreview = new JButton();
			m_ButtonScanPreview.setIcon(fIcons.search_preview);
			m_ButtonScanPreview.addActionListener(this);
			m_ButtonScanPreview.setBorder(null);
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
	                        if (transferData != null && transferData.size() == 1) {
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
			m_TabSearch.add(m_ButtonScanPreview);
			m_TabbedPaneScanResult = new JTabbedPane();
			m_TabbedPaneScanResult.setFont(UI.Font);
			m_TabbedPaneScanResult.setFocusable(false);
			m_TabbedPaneScanResult.setOpaque(false);
			m_TabbedPaneScanResult.setTabPlacement(JTabbedPane.RIGHT);
			m_TabSearch.add(m_TabbedPaneScanResult);
			m_TabbedPane.addTab("Search", fIcons.search, m_TabSearch);
			
			PanelConfiguration panelConfig = new PanelConfiguration(Configuration.class);
			panelConfig.setConfigurationFile(CONFIG_FILE);
			m_TabbedPane.addTab("Configuration", fIcons.settings, m_TabConfiguration = panelConfig);
			
			super.add(m_TabbedPane);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() == m_ButtonCacheBuild) {
				if(m_BuilderRunning)
					return;
				m_WorkerBuilder = new TaskBuilder();
				m_WorkerBuilder.execute();
				return;
			}
			if(ae.getSource() == m_ButtonCacheCancel) {
				m_WorkerBuilder.cancel(true);
				return;
			}
		}
		
		private final class TaskBuilder extends SwingWorker<Void, Integer>
		{
			@Override
			protected Void doInBackground() throws Exception {
				Thread.currentThread().setName("plugin/image-search/cache-builder");
				m_BuilderRunning = true;
				m_TabSearch.doLayout();

				m_ProgressBarCache.setMaximum(1);
				m_ProgressBarCache.setMinimum(1);
				m_ProgressBarCache.setValue(1);
				m_ProgressBarCache.setString("Loading ...");
				
				RecordSet<Book> books = DataBase.getBooks(new QueryBook());
				final Iterator<Book> books_q = books.iterator();
				
				m_ProgressBarCache.setMaximum(books.size());
				m_ProgressBarCache.setMinimum(1);
				m_ProgressBarCache.setValue(1);
				
				int threads = Configuration.hashdb_threads.get();
			    List<Future<Void>> futuresList = new ArrayList<Future<Void>>();
			    ExecutorService eservice = Executors.newFixedThreadPool(threads);
			    
			    for(int index=0; index<threads; index++)
			    	futuresList.add(eservice.submit(new Callable<Void>() {
			    		@Override
			    		public Void call() throws Exception {
			    			Book book;
			    			while ((book = books_q.next()) != null) {
			    				if(isCancelled())
									return null;
			    				try {
			    					if(!HASHDB_MAP.containsValue(book.getId())) {
			    						String hash = new ImagePHash().getHash(DataStore.getThumbnail(book.getId()).openInputStream());
			    						HASHDB_MAP.put(hash, book.getId());
			    					}
			    					publish(HASHDB_MAP.size());
			    				} catch (Exception e) {
			    					LOG.error("Error computing hash for Book {}", book.getId(), e);
			    				}
			    			}
			    			return null;
			    		}
			    	}));
			    for(Future<Void> future : futuresList) {
			    	try {
			    		future.get();
			    	} catch (InterruptedException | ExecutionException e) {
			    		LOG.error("Error processing hashing task", e);
			    	}
			    }
				return null;
			}

			@Override
			protected void done() {
				m_BuilderRunning = false;
				m_ProgressBarCache.setValue(0);
				m_ProgressBarCache.setString("");
				m_TabSearch.doLayout();
			}

			@Override
			protected void process(List<Integer> chunks) {
				int value = chunks.get(chunks.size() - 1);
				int maximum = m_ProgressBarCache.getMaximum();
				int progress = value * 100 / maximum;
				m_ProgressBarCache.setValue(value);
				m_ProgressBarCache.setString("[" + value + " / " + maximum + "] @ " + progress + "%");
			}
		}
		
		private final class TaskScanner extends SwingWorker<Void, Integer>
		{
			final private File file;
			
			private TaskScanner(final File file) {
				this.file = file;
			}
			
			private final class RatedItem implements Comparable<RatedItem>
			{
				public final Integer item;
				public final Integer value;
				private RatedItem(Integer item, Integer value) {
					this.item = item;
					this.value = value;
				}
				@Override
				public int compareTo(RatedItem o) {
					return value.compareTo(o.value);
				}
			}
			
			@Override
			protected Void doInBackground() throws Exception {
				Thread.currentThread().setName("plugin/image-search/cache-scanner");
				m_ScannerRunning = true;
				PluginUI.this.doLayout();
				
				while (m_TabbedPaneScanResult.getTabCount() > 0)
					m_TabbedPaneScanResult.remove(0);
				m_ButtonScanPreview.setIcon(fIcons.search_missing);
				
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
					
					String inputHash = new ImagePHash().getHash(new FileInputStream(file));
					int threshold = Configuration.query_threshold.get();
					int maxresult = Configuration.query_maxresult.get();
					LOG.info("Input hash is {}", inputHash);
					LOG.info("Searching HashDB for matching hashes (size: {}, threshold: {}) ...", HASHDB_MAP.size(), threshold);
					
					PriorityQueue<RatedItem> found = new PriorityQueue<RatedItem>(1, new Comparator<RatedItem>() {
						@Override
						public int compare(RatedItem o1, RatedItem o2) {
							return o2.compareTo(o1);
						}
					});
					for(String hash: HASHDB_MAP.keySet()) {
						int distance;
						if((distance = new ImagePHash().distance(hash, inputHash)) <= threshold) {
							found.add(new RatedItem(HASHDB_MAP.get(hash), distance));
							if (found.size() > maxresult)
								found.poll();
						}
					}
					LOG.info("Found {} matching hash(es)", found.size());
					
					while(!found.isEmpty()) {
						RatedItem ratedItem = found.poll();
						final Integer bookId = ratedItem.item;
						final Integer distance = ratedItem.value;
						JButton button;
						try {
							button = new JButton(new ImageIcon(ImageTool.read(DataStore.getThumbnail(bookId).openInputStream())));
						} catch (Exception dse) {
							button = new JButton(fIcons.search_missing);
						}
						button.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent ae) {
								new SwingWorker<Void, Void>() {
									@Override
									protected Void doInBackground() throws Exception {
										QueryBook qid = new QueryBook();
										qid.Id = bookId;
										RecordSet<Book> set = DataBase.getBooks(qid);
										if(set.size() == 1)
											UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
										return null;
									}
								}.execute();
							}
						});
						if(distance.equals(0)) // exact hash match
							m_TabbedPaneScanResult.insertTab("", fIcons.search_star, button, "", 0);
						else // match within threshold
							m_TabbedPaneScanResult.insertTab(String.format("+%d", distance), null, button, "", 0);
						m_TabbedPaneScanResult.setSelectedIndex(0);
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
	}

	@Override
	protected void doInstall() { }

	@Override
	protected void doUpdate() { }

	@Override
	protected void doUninstall() { }
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doStartup() {
		try {
			ConfigurationParser.fromXML(Configuration.class, CONFIG_FILE);
			LOG.debug("Loaded Configuration from {}", CONFIG_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error loading Configuration from {}", CONFIG_FILE.getName(), ioe);
		}
		synchronized(HASHDB_MAP) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HASHDB_FILE));
				HASHDB_MAP = (HashMap<String, Integer>) ois.readObject();
				LOG.debug("Loaded HashDB from {} (size: {})", HASHDB_FILE.getName(), HASHDB_MAP.size());
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				LOG.error("Error loading HashDB from {}", HASHDB_FILE.getName(), e);
			}
		}
	}
	
	@Override
	protected void doShutdown() {
		try {
			ConfigurationParser.toXML(Configuration.class, CONFIG_FILE);
			LOG.debug("Saved Configuration to {}", CONFIG_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error saving Configuration to {}", CONFIG_FILE.getName(), ioe);
		}
		try {
			HASHDB_FILE.getParentFile().mkdirs();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HASHDB_FILE));
			oos.writeObject(HASHDB_MAP);
			oos.flush();
			oos.close();
			LOG.debug("Saved HashDB to {}", HASHDB_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error saving HashDB to {}", HASHDB_FILE.getName(), ioe);
		}
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
