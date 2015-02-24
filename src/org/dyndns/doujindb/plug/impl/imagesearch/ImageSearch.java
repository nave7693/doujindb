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

import javax.imageio.IIOException;
import javax.swing.*;
import javax.swing.Timer;

import org.dyndns.doujindb.conf.ConfigurationParser;
import org.dyndns.doujindb.dat.DataFile;
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
	private final String mAuthor = "loli10K";
	private final String mVersion = "1.1";
	private final String mWeblink = "https://github.com/loli10K";
	private final String mName = "Image Search";
	private final String mDescription = "Search through then whole DataStore for matching cover images.";
	private JComponent mUI = new PluginUI();
	
	private File mHashFile = new File(this.PLUGIN_HOME, "hashdb.ser");
	private static Map<String, Integer> mHashMap = new HashMap<String, Integer>();
	
	private static Icons mIcons = new Icons();
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(ImageSearch.class);
	
	@Override
	public Icon getIcon() {
		return mIcons.icon;
	}
	
	@Override
	public String getName() {
		return mName;
	}
	
	@Override
	public String getDescription() {
		return mDescription;
	}
	
	@Override
	public String getVersion() {
		return mVersion;
	}
	
	@Override
	public String getAuthor() {
		return mAuthor;
	}
	
	@Override
	public String getWeblink() {
		return mWeblink;
	}
	
	@Override
	public JComponent getUI() {
		return mUI;
	}
	
	@SuppressWarnings("serial")
	private final class PluginUI extends JPanel implements ActionListener
	{
		private JTabbedPane mTabbedPane;
		private JPanel mTabSearch;
		@SuppressWarnings("unused")
		private JPanel mTabConfiguration;
		
		private JProgressBar mProgressBarSearch;
		private JButton mButtonBuild;
		private JButton mButtonCancel;
		
		private JButton mButtonSearchInput;
		private JTabbedPane mTabbedPaneSearchResult;
		
		private SwingWorker<Void, Integer> mWorkerSearch = new Search(null);
		private boolean mIsSearchRunning = false;
		private SwingWorker<Void, Integer> mWorkerHasher = new Hasher();
		private boolean mIsHasherRunning = false;
		
		public PluginUI() {
			super();
			super.setLayout(new GridLayout(1,1));
			mTabbedPane = new JTabbedPane();
			mTabbedPane.setFont(UI.Font);
			mTabbedPane.setFocusable(false);
			
			mTabSearch = new JPanel();
			mTabSearch.setMinimumSize(new Dimension(350, 350));
			mTabSearch.setPreferredSize(new Dimension(350, 350));
			mTabSearch.setLayout(new LayoutManager() {
				@Override
				public void layoutContainer(Container parent) {
					int width = parent.getWidth(),
						height = parent.getHeight();
					if(!mIsHasherRunning) {
						mButtonBuild.setBounds(0,0,20,20);
						mButtonCancel.setBounds(0,0,0,0);
					} else {
						mButtonBuild.setBounds(0,0,0,0);
						mButtonCancel.setBounds(0,0,20,20);
					}
					mProgressBarSearch.setBounds(21,1,width-22,20);
					mButtonSearchInput.setBounds(1,25,180,256);
					mTabbedPaneSearchResult.setBounds(190,25,width-190,height-30);
				}
				
				@Override
				public void addLayoutComponent(String key,Component c) { }
				
				@Override
				public void removeLayoutComponent(Component c) { }
				
				@Override
				public Dimension minimumLayoutSize(Container parent) {
					return mTabbedPane.getMinimumSize();
				}
				
				@Override
				public Dimension preferredLayoutSize(Container parent) {
					return mTabbedPane.getPreferredSize();
				}
			});
			mButtonBuild = new JButton(mIcons.worker_start);
			mButtonBuild.addActionListener(this);
			mButtonBuild.setBorder(null);
			mButtonBuild.setFocusable(false);
			mTabSearch.add(mButtonBuild);
			mButtonCancel = new JButton(mIcons.worker_stop);
			mButtonCancel.addActionListener(this);
			mButtonCancel.setBorder(null);
			mButtonCancel.setFocusable(false);
			mTabSearch.add(mButtonCancel);
			mProgressBarSearch = new JProgressBar();
			mProgressBarSearch.setFont(UI.Font);
			mProgressBarSearch.setMaximum(100);
			mProgressBarSearch.setMinimum(1);
			mProgressBarSearch.setValue(mProgressBarSearch.getMinimum());
			mProgressBarSearch.setStringPainted(true);
			mProgressBarSearch.setString("");
			mTabSearch.add(mProgressBarSearch);
			mButtonSearchInput = new JButton();
			mButtonSearchInput.setIcon(mIcons.search_preview);
			mButtonSearchInput.addActionListener(this);
			mButtonSearchInput.setBorder(null);
			mButtonSearchInput.setDropTarget(new DropTarget() {
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde) {
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						if(mIsSearchRunning | mIsHasherRunning) {
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
	                                	if(mIsSearchRunning | mIsHasherRunning)
	                    					return;
	                                	mWorkerSearch = new Search(file);
	                                	new Thread(mWorkerSearch).start();
	                    				mTabSearch.doLayout();
	                                }
	                        	}.start();
	                            dtde.dropComplete(true);
	                        }
	                    } catch (Exception e) {
	                        LOG.error("Error processing drop() method for event {}", dtde, e);
	                    }
	                } else {
	                    dtde.rejectDrop();
	                }
				}
			});
			mTabSearch.add(mButtonSearchInput);
			mTabbedPaneSearchResult = new JTabbedPane();
			mTabbedPaneSearchResult.setFont(UI.Font);
			mTabbedPaneSearchResult.setFocusable(false);
			mTabbedPaneSearchResult.setOpaque(false);
			mTabbedPaneSearchResult.setTabPlacement(JTabbedPane.RIGHT);
			mTabSearch.add(mTabbedPaneSearchResult);
			mTabbedPane.addTab("Search", mIcons.search, mTabSearch);
			
			PanelConfiguration panelConfig = new PanelConfiguration(Configuration.class);
			panelConfig.setConfigurationFile(CONFIG_FILE);
			mTabbedPane.addTab("Configuration", mIcons.settings, mTabConfiguration = panelConfig);
			
			super.add(mTabbedPane);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() == mButtonBuild) {
				if(mIsHasherRunning)
					return;
				mWorkerHasher = new Hasher();
				new Thread(mWorkerHasher).start();
				return;
			}
			if(ae.getSource() == mButtonCancel) {
				mWorkerHasher.cancel(false);
				return;
			}
			if(ae.getSource() == mButtonSearchInput) {
				if(mIsSearchRunning | mIsHasherRunning)
					return;
				new Thread()
            	{
                    @Override
                    public void run() {
                    	JFileChooser fc = UI.FileChooser;
						fc.setMultiSelectionEnabled(false);
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						if(fc.showOpenDialog(mUI) != JFileChooser.APPROVE_OPTION)
							return;
						File file = fc.getSelectedFile();
                    	mWorkerSearch = new Search(file);
                    	new Thread(mWorkerSearch).start();
        				mTabSearch.doLayout();
                    }
            	}.start();
				return;
			}
		}
		
		private final class Hasher extends SwingWorker<Void, Integer>
		{
			private String mLabelETA = "";
			@Override
			protected Void doInBackground() throws Exception {
				Thread.currentThread().setName("plugin-imagesearch-hashdb");
				mIsHasherRunning = true;
				mTabSearch.doLayout();

				mProgressBarSearch.setMaximum(1);
				mProgressBarSearch.setMinimum(1);
				mProgressBarSearch.setValue(1);
				mProgressBarSearch.setString("Loading ...");
				
				final RecordSet<Book> books = DataBase.getBooks(new QueryBook());
				final Iterator<Book> books_q = books.iterator();
				
				mProgressBarSearch.setMaximum(books.size());
				mProgressBarSearch.setMinimum(1);
				mProgressBarSearch.setValue(1);
				
				int threads = Configuration.hashdb_threads.get();
			    List<Future<Void>> futuresList = new ArrayList<Future<Void>>();
			    ExecutorService eservice = Executors.newFixedThreadPool(threads);
			    
			    final long lastModified = mHashFile.lastModified();
			    for(int index=0; index<threads; index++)
			    	futuresList.add(eservice.submit(new Callable<Void>() {
			    		private <T> T next(Iterator<T> i) {
			    			synchronized(i) {
			    				if(i.hasNext())
			    					return i.next();
			    				else
			    					return null;
			    			}
			    		}
			    		@Override
			    		public Void call() throws Exception {
			    			Book book;
			    			while ((book = next(books_q)) != null) {
			    				if(isCancelled())
									return null;
			    				try {
			    					DataFile thumbn = DataStore.getThumbnail(book.getId());
			    					if(!mHashMap.containsValue(book.getId()) || thumbn.lastModified() > lastModified) {
			    						try {
			    							String hash = new ImageAHash().getHash(thumbn.openInputStream());
			    							mHashMap.put(hash, book.getId());
			    						} catch (IIOException iioe) {
					    					LOG.error("Error computing hash for Book {} with image {}", new Object[]{book.getId(), thumbn.getPath(), iioe});
					    				}
			    					}
			    					publish(mHashMap.size());
			    				} catch (Exception e) {
			    					LOG.error("Error computing hash for Book {}", book.getId(), e);
			    				}
			    			}
			    			return null;
			    		}
			    	}));
			    Timer timer = new Timer(1000, new ActionListener() {
			    	long mLastCount = mHashMap.size();
					@Override
					public void actionPerformed(ActionEvent ae) {
						long currCount = mHashMap.size();
						long hps = currCount - mLastCount; // hash per second
						if(hps != 0)
							mLabelETA = format((books.size() - currCount) / hps);
						else
							mLabelETA = "...";
						mLastCount = currCount;
					}
				    public String format(long seconds) {
				        if(seconds < 0)
				        	return "";

				        long days = TimeUnit.SECONDS.toDays(seconds);
				        seconds -= TimeUnit.DAYS.toSeconds(days);
				        long hours = TimeUnit.SECONDS.toHours(seconds);
				        seconds -= TimeUnit.HOURS.toSeconds(hours);
				        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
				        seconds -= TimeUnit.MINUTES.toSeconds(minutes);

				        String result = "";
				        if(days > 0)
				        	result += days + " d ";
				        if(hours > 0)
				        	result += hours + " h ";
				        if(minutes > 0)
				        	result += minutes + " m ";
				        if(seconds > 0)
				        	result += seconds + " s";

				        return result;
				    }
			    });
			    timer.setRepeats(true);
			    timer.setInitialDelay(0);
			    timer.start();
			    for(Future<Void> future : futuresList) {
			    	try {
			    		future.get();
			    	} catch (InterruptedException | ExecutionException e) {
			    		LOG.error("Error processing hashing task", e);
			    	}
			    }
			    eservice.shutdown();
			    timer.stop();
				return null;
			}

			@Override
			protected void done() {
				mIsHasherRunning = false;
				mProgressBarSearch.setValue(0);
				mProgressBarSearch.setString("");
				mTabSearch.doLayout();
			}

			@Override
			protected void process(List<Integer> chunks) {
				int value = chunks.get(chunks.size() - 1);
				int maximum = mProgressBarSearch.getMaximum();
				int progress = value * 100 / maximum;
				mProgressBarSearch.setValue(value);
				mProgressBarSearch.setString(String.format("[%d / %d] @ %d%% (ETA %s)", value, maximum, progress, mLabelETA));
			}
		}
		
		private final class Search extends SwingWorker<Void, Integer>
		{
			final private File mFile;
			
			private Search(final File file) {
				this.mFile = file;
			}
			
			private final class RatedItem implements Comparable<RatedItem>
			{
				final Integer mItem;
				final Integer mValue;
				
				private RatedItem(Integer item, Integer value) {
					this.mItem = item;
					this.mValue = value;
				}
				
				@Override
				public int compareTo(RatedItem o) {
					return mValue.compareTo(o.mValue);
				}
			}
			
			@Override
			protected Void doInBackground() throws Exception {
				Thread.currentThread().setName("plugin-imagesearch-imagequery");
				mIsSearchRunning = true;
				PluginUI.this.doLayout();
				
				while (mTabbedPaneSearchResult.getTabCount() > 0)
					mTabbedPaneSearchResult.remove(0);
				mButtonSearchInput.setIcon(mIcons.search_missing);
				
				BufferedImage bi;
				try {
					bi = javax.imageio.ImageIO.read(mFile);
					if(bi == null)
						throw new RuntimeException("Could not load Image from file " + mFile.getName());
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
					
					mButtonSearchInput.setIcon(new ImageIcon(bi));
					
					String inputHash = new ImageAHash().getHash(new FileInputStream(mFile));
					int threshold = Configuration.query_threshold.get();
					int maxresult = Configuration.query_maxresult.get();
					LOG.info("Input hash is {}", inputHash);
					LOG.info("Searching HashDB for matching hashes (size: {}, threshold: {}) ...", mHashMap.size(), threshold);
					
					long count = 0;
					PriorityQueue<RatedItem> found = new PriorityQueue<RatedItem>(1, new Comparator<RatedItem>() {
						@Override
						public int compare(RatedItem o1, RatedItem o2) {
							return o2.compareTo(o1);
						}
					});
					for(String hash: mHashMap.keySet()) {
						int distance;
						if((distance = new ImageAHash().distance(hash, inputHash)) <= threshold) {
							count++;
							found.add(new RatedItem(mHashMap.get(hash), distance));
							if (found.size() > maxresult)
								found.poll();
						}
					}
					if(count > found.size())
						LOG.info("Found {} matching hash(es), only best {} shown", count, found.size());
					else
						LOG.info("Found {} matching hash(es)", found.size());
					
					while(!found.isEmpty()) {
						RatedItem ratedItem = found.poll();
						final Integer bookId = ratedItem.mItem;
						final Integer distance = ratedItem.mValue;
						JButton button;
						try {
							button = new JButton(new ImageIcon(javax.imageio.ImageIO.read(DataStore.getThumbnail(bookId).openInputStream())));
						} catch (Exception dse) {
							button = new JButton(mIcons.search_missing);
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
							mTabbedPaneSearchResult.insertTab("", mIcons.search_star, button, "", 0);
						else // match within threshold
							mTabbedPaneSearchResult.insertTab(String.format("+%d", distance), null, button, "", 0);
						mTabbedPaneSearchResult.setSelectedIndex(0);
					}
					
				} catch (Exception e) {
					LOG.error("Error processing image search for input file {}", mFile.getName(), e);
				}
				
				return null;
			}
			
			@Override
			protected void done() {
				mIsSearchRunning = false;
				PluginUI.this.doLayout();
				super.done();
			}

			@Override
			protected void process(List<Integer> chunks) {
				super.process(chunks);
			}
		}
	}
	
	public static Integer search(File file) {
		Integer result = null;
		int threshold = Configuration.query_threshold.get();
		int distance = Integer.MAX_VALUE;
		try {
			FileInputStream fis = new FileInputStream(file);
			String input_hash = new ImageAHash().getHash(fis);
			for(String hash : mHashMap.keySet()) {
				int diff = new ImageAHash().distance(hash, input_hash);
				if(threshold < diff)
					continue;
				if(diff < distance) {
					distance = diff;
					result = mHashMap.get(hash);
				}
			}
			return result;
		} catch (FileNotFoundException fnfe) {
			LOG.error("Error loading hash from {}", file, fnfe);
		} catch (Exception e) {
			LOG.error("Error computing hash from {}", file, e);
		}
		return result;
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
		synchronized(mHashMap) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mHashFile));
				mHashMap = (HashMap<String, Integer>) ois.readObject();
				LOG.debug("Loaded HashDB from {} (size: {})", mHashFile.getName(), mHashMap.size());
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				LOG.error("Error loading HashDB from {}", mHashFile.getName(), e);
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
			mHashFile.getParentFile().mkdirs();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mHashFile));
			oos.writeObject(mHashMap);
			oos.flush();
			oos.close();
			LOG.debug("Saved HashDB to {}", mHashFile.getName());
		} catch (IOException ioe) {
			LOG.error("Error saving HashDB to {}", mHashFile.getName(), ioe);
		}
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
