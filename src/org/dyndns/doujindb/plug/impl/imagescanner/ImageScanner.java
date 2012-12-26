package org.dyndns.doujindb.plug.impl.imagescanner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseContext;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.plug.PluginException;
import org.dyndns.doujindb.plug.impl.imagescanner.rc.Resources;
import org.dyndns.doujindb.ui.desk.WindowEx;

/**  
* ImageScanner.java - Plugin to search for matching images in your Datastore.
* @author  nozomu
* @version 1.0
*/
public final class ImageScanner extends Plugin
{	
	private static Resources Resources = new Resources();
	
	static final String Author = "Nozomu";
	static final String Version = "0.2";
	static final String Weblink = "http://code.google.com/p/doujindb/";
	static final String Name = "Image Scanner";
	static final String Description = "The Image Scanner plugin lets you search for similar items in the DataStore.";
	static final ImageIcon Icon = new ImageIcon(ImageScanner.class.getResource("rc/icons/plugin.png"));
	
	static final String UUID = "{D18B8C85-BE10-4937-9C5A-885CEAD64D35}";
	static final File PLUGIN_HOME = new File(System.getProperty("doujindb.home"),  "plugins/" + UUID);
	private static DataBaseContext Context;
	
	private JComponent UI;
	
	static
	{
		File file = new File(System.getProperty("doujindb.home"), "plugins/" + UUID);
		file.mkdirs();
	}
	
	public ImageScanner()
	{
		Context = Core.Database.getContext(UUID);
		
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
	private final class PluginUI extends JPanel implements LayoutManager, ActionListener
	{
		private JTabbedPane tabs;
		private JPanel tabSettings;
		private JPanel tabSearch;
		
		private JButton buttonBuild;
		private JButton buttonBuildCancel;
		private JButton buttonBuildConfirm;
		private JLabel labelBuildPreview;
		private JProgressBar barBuild;
		private JTextArea textLogBuild;
		private JScrollPane scrollLogBuild;
		private JLabel labelDensity;
		private JSlider sliderDensity;
		private JCheckBox boxOverwrite;
		
		private JButton buttonScanPreview;
		private JTabbedPane tabsScanResult;
		private JProgressBar barScan;
		private JButton buttonScanCancel;

		private TaskCacheBuilder builderTask;
		private Thread builderWorker;
		
		private boolean scannerRunning = false;
		private boolean scannerCompleted = false;
		private TaskScanner scannerTask;
		private Thread scannerWorker;
		
		public PluginUI()
		{
			super();
			super.setLayout(this);
			tabs = new JTabbedPane();
			tabs.setFont(Core.Resources.Font);
			tabs.setFocusable(false);
			
			JPanel bogus;
			bogus = new JPanel();
			bogus.setLayout(new LayoutManager()
			{
				@Override
				public void layoutContainer(Container parent)
				{
					int width = parent.getWidth(),
						height = parent.getHeight();
					labelDensity.setBounds(0,0,0,0);
					sliderDensity.setBounds(0,0,0,0);
					boxOverwrite.setBounds(0,0,0,0);
					if(builderTask.running)
					{
						buttonBuild.setBounds(0,0,0,0);
						buttonBuildCancel.setBounds(width / 2 - 50, height - 25, 100,  20);
						buttonBuildConfirm.setBounds(0,0,0,0);
						labelBuildPreview.setBounds(5,30,180,256);
						barBuild.setBounds(5,5,width-10,20);
						scrollLogBuild.setBounds(190,30,width-195,height-75);
					} else {
						if(builderTask.completed)
						{
							buttonBuild.setBounds(0,0,0,0);
							buttonBuildConfirm.setBounds(width / 2 - 50, height - 25, 100,  20);
							labelBuildPreview.setBounds(5,30,180,256);
							barBuild.setBounds(5,5,width-10,20);
							scrollLogBuild.setBounds(190,30,width-195,height-75);
						} else {
							buttonBuild.setBounds(width / 2 - 50, height - 25, 100,  20);
							buttonBuildConfirm.setBounds(0,0,0,0);
							labelBuildPreview.setBounds(0,0,0,0);
							barBuild.setBounds(0,0,0,0);
							scrollLogBuild.setBounds(0,0,0,0);
							labelDensity.setBounds(5,5,width / 2 - 5, 20);
							sliderDensity.setBounds(width / 2 + 5,5,width / 2 - 5, 20);
							boxOverwrite.setBounds(5,25,width-10,20);
						}
						buttonBuildCancel.setBounds(0,0,0,0);
					}
				}

				@Override
				public void addLayoutComponent(String name, Component comp) { }

				@Override
				public Dimension minimumLayoutSize(Container cont) { return null; }

				@Override
				public Dimension preferredLayoutSize(Container cont) { return null; }

				@Override
				public void removeLayoutComponent(Component comp) { }
			});
			buttonBuild = new JButton();
			buttonBuild.setText("Build");
			buttonBuild.setIcon(Resources.Icons.get("Plugin/Settings/Build"));
			buttonBuild.addActionListener(this);
			buttonBuild.setToolTipText("Build Cache");
			buttonBuild.setFocusable(false);
			bogus.add(buttonBuild);
			buttonBuildCancel = new JButton();
			buttonBuildCancel.setText("Cancel");
			buttonBuildCancel.setIcon(Resources.Icons.get("Plugin/Settings/Cancel"));
			buttonBuildCancel.addActionListener(this);
			buttonBuildCancel.setToolTipText("Cancel");
			buttonBuildCancel.setFocusable(false);
			bogus.add(buttonBuildCancel);
			buttonBuildConfirm = new JButton();
			buttonBuildConfirm.setText("Confirm");
			buttonBuildConfirm.setIcon(Resources.Icons.get("Plugin/Settings/Confirm"));
			buttonBuildConfirm.addActionListener(this);
			buttonBuildConfirm.setToolTipText("Confirm");
			buttonBuildConfirm.setFocusable(false);
			bogus.add(buttonBuildConfirm);
			
			labelDensity = new JLabel("Density : " + 15);
			labelDensity.setFont(Core.Resources.Font);
			bogus.add(labelDensity);
			sliderDensity = new JSlider(1, 15);
			sliderDensity.setValue(15);
			sliderDensity.setFont(Core.Resources.Font);
			sliderDensity.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					labelDensity.setText("Density : " + sliderDensity.getValue());
				}				
			});
			bogus.add(sliderDensity);
			boxOverwrite = new JCheckBox();
			boxOverwrite.setSelected(false);
			boxOverwrite.setFocusable(false);
			boxOverwrite.setText("Overwrite existing entries");
			bogus.add(boxOverwrite);
			
			labelBuildPreview = new JLabel();
			labelBuildPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
			labelBuildPreview.setBorder(null);
			labelBuildPreview.setOpaque(false);
			bogus.add(labelBuildPreview);
			barBuild = new JProgressBar();
			barBuild.setFont(Core.Resources.Font);
			barBuild.setMaximum(100);
			barBuild.setMinimum(1);
			barBuild.setValue(barBuild.getMinimum());
			barBuild.setStringPainted(true);
			barBuild.setString("");
			bogus.add(barBuild);
			textLogBuild = new JTextArea();
			textLogBuild.setText("");
			textLogBuild.setFocusable(false);
			textLogBuild.setEditable(false);
			textLogBuild.setFont(Core.Resources.Font);
			scrollLogBuild = new JScrollPane(textLogBuild);
			scrollLogBuild.setEnabled(false);
			scrollLogBuild.getVerticalScrollBar().setEnabled(false);
			scrollLogBuild.getHorizontalScrollBar().setEnabled(false);
			bogus.add(scrollLogBuild);
			tabs.addTab("Settings", Resources.Icons.get("Plugin/Settings"), bogus);
			tabSettings = bogus;
			
			bogus = new JPanel();
			bogus.setLayout(new LayoutManager()
			{
				@Override
				public void layoutContainer(Container parent)
				{
					int width = parent.getWidth(),
						height = parent.getHeight();
					buttonScanPreview.setBounds(5,5,180,256);
					barScan.setBounds(5,265,180,20);
					if(scannerRunning)
						buttonScanCancel.setBounds(5,290,180,20);
					else
						buttonScanCancel.setBounds(0,0,0,0);
					tabsScanResult.setBounds(190,5,width-190,height-10);
				}

				@Override
				public void addLayoutComponent(String name, Component comp) { }

				@Override
				public Dimension minimumLayoutSize(Container cont) { return null; }

				@Override
				public Dimension preferredLayoutSize(Container cont) { return null; }

				@Override
				public void removeLayoutComponent(Component comp) { }
			});
			buttonScanPreview = new JButton();
			buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
			buttonScanPreview.setBorder(null);
			buttonScanPreview.setOpaque(false);
			buttonScanPreview.setDropTarget(new DropTarget()
			{
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde)
				{
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
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
	                                	//FIXME doScan(file);
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
			barScan = new JProgressBar();
			barScan.setFont(Core.Resources.Font);
			barScan.setMaximum(100);
			barScan.setMinimum(1);
			barScan.setValue(barBuild.getMinimum());
			barScan.setStringPainted(true);
			barScan.setString("");
			bogus.add(barScan);
			buttonScanCancel = new JButton();
			buttonScanCancel.setText("Cancel");
			buttonScanCancel.setIcon(Resources.Icons.get("Plugin/Settings/Cancel"));
			buttonScanCancel.addActionListener(this);
			buttonScanCancel.setToolTipText("Cancel");
			buttonScanCancel.setFocusable(false);
			bogus.add(buttonScanCancel);
			tabs.addTab("Search", Resources.Icons.get("Plugin/Search"), bogus);
			tabSearch = bogus;
			
			super.add(tabs);
			
			builderTask = new TaskCacheBuilder();
			builderWorker = new Thread(builderTask);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			tabs.setBounds(0,0,width,height);
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
			if(ae.getSource() == buttonBuild)
			{
				if(builderTask.running || builderWorker.isAlive())
					return;
				builderTask.running = true;
				builderTask.completed = false;
				builderWorker = new Thread(builderTask);
				builderWorker.setName(getClass().getName()+"$CacheBuilder");
				builderWorker.setDaemon(true);
				builderWorker.start();
				tabSettings.doLayout();
				return;
			}
			if(ae.getSource() == buttonBuildCancel)
			{
				if(!builderTask.running)
					return;
				builderTask.running = false;
				builderTask.completed = false;
				tabSettings.doLayout();
				return;
			}
			if(ae.getSource() == buttonBuildConfirm)
			{
				builderTask.running = false;
				builderTask.completed = false;
				tabSettings.doLayout();
				return;
			}
		}
		
		final class TaskScanner implements Runnable
		{
			private boolean running = false;
			private boolean completed = false;
			
			final private File file;
			
			private TaskScanner(final File file)
			{
				this.file = file;
			}
			
			@Override
			public void run()
			{
				// Reset UI
				while (tabsScanResult.getTabCount() > 0)
					tabsScanResult.remove(0);
				buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				barScan.setValue(barBuild.getMinimum());
				barScan.setString("Initializing ...");
				
				BufferedImage bi;
				try {
					bi = javax.imageio.ImageIO.read(file);
					BufferedImage resized_bi;
					if(bi.getWidth() > bi.getHeight())
						resized_bi = new BufferedImage(bi.getWidth() / 2, bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					else
						resized_bi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics g = resized_bi.getGraphics();
					g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), null);
					g.dispose();
					bi = org.dyndns.doujindb.util.Image.getScaledInstance(resized_bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
					
					buttonScanPreview.setIcon(new ImageIcon(bi));
					
					TreeMap<Long, BufferedImage> result = new TreeMap<Long, BufferedImage>();
					TreeMap<Long, Book> result_books = new TreeMap<Long, Book>();
					NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, sliderDensity.getValue());
					
					RecordSet<Book> books = Core.Database.getBooks(null);
					
					barScan.setMaximum(books.size());
					barScan.setMinimum(1);
					barScan.setValue(barBuild.getMinimum());
					
					for(Book book : books)
					{
						try { Thread.sleep(1); } catch (InterruptedException ie) { }
						
						if(!scannerRunning)
							return;
						
						bi = javax.imageio.ImageIO.read(Core.Repository.getPreview(book.getID()).getInputStream());
						bi = org.dyndns.doujindb.util.Image.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);

						long similarity = nsf.getSimilarity(bi);
						if(result.size() >= 10)
						{
							long remove_me = result.lastKey();
							result.put(similarity, bi);
							result_books.put(similarity, book);
							result.remove(remove_me);
							result_books.remove(remove_me);
						} else {
							result.put(similarity, bi);
							result_books.put(similarity, book);
						}
						
						int progress = barScan.getValue() * 100 / barScan.getMaximum();
						barScan.setString("[" + barScan.getValue() + " / " + barScan.getMaximum() + "] @ " + progress + "%");
						barScan.setValue(barScan.getValue() + 1);
						if(barScan.getValue() == barScan.getMaximum())
							barScan.setValue(barScan.getMinimum());
					}
					
					boolean first_result = false;
					for(long index : result.keySet())
					{
						final String book_id = result_books.get(index).getID();
						final Book book = result_books.get(index);
						if(!first_result)
						{
							JButton button = new JButton(new ImageIcon(result.get(index)));
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
								}
							});
							first_result = true;
							tabsScanResult.addTab(book_id, Resources.Icons.get("Plugin/Search/Star"), button);
						} else
						{
							JButton button = new JButton(new ImageIcon(result.get(index)));
							button.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent ae) {
									Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
								}
							});
							tabsScanResult.addTab(book_id, button);
						}
					}
					
					barScan.setValue(barScan.getMaximum());
					barScan.setString("Completed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		final class TaskCacheBuilder implements Runnable
		{
			private boolean running = false;
			private boolean completed = false;
			
			private TaskCacheBuilder() { }
			
			@Override
			public void run()
			{
				// Reset UI
				labelBuildPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				textLogBuild.setText("");
				barBuild.setValue(barBuild.getMinimum());
				barBuild.setString("Initializing ...");
				
				// Init data
				int density = sliderDensity.getValue();
				boolean overwrite = boxOverwrite.isSelected();
				RecordSet<Book> books = Core.Database.getBooks(null);
				
				barBuild.setMaximum(books.size());
				barBuild.setMinimum(1);
				barBuild.setValue(barBuild.getMinimum());
				
				for(Book book : books)
				{
					try { Thread.sleep(1); } catch (InterruptedException ie) { }
					
					if(!running)
						return;
					
					int progress = barBuild.getValue() * 100 / barBuild.getMaximum();
					barBuild.setString("[" + barBuild.getValue() + " / " + barBuild.getMaximum() + "] @ " + progress + "%");
					barBuild.setValue(barBuild.getValue() + 1);
					if(barBuild.getValue() == barBuild.getMaximum())
						barBuild.setValue(barBuild.getMinimum());
					
					textLogBuild.append("Building [" + book.getID() + "] ...");
					
					BufferedImage bi;
					try {
						File serialized_file = new File(PLUGIN_HOME, book.getID() + ".ser");
						if(serialized_file.exists() && !overwrite)
						{
							textLogBuild.append(" skipped\n");
							textLogBuild.setCaretPosition(textLogBuild.getText().length());
							continue;
						}
						bi = javax.imageio.ImageIO.read(Core.Repository.getPreview(book.getID()).getInputStream());
						bi = org.dyndns.doujindb.util.Image.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
						
						NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, density);
						int[][] signature = nsf.getSignature();
						serialize(signature, serialized_file);
						
						labelBuildPreview.setIcon(new ImageIcon(nsf.getImage()));
						
						textLogBuild.append(" done\n");
					} catch (Exception e) {
						textLogBuild.append(" " + e.getMessage() + ".\n");
					}
					
					textLogBuild.setCaretPosition(textLogBuild.getText().length());
				}
				/**
				 * Cache build completed
				 */
				barBuild.setValue(barBuild.getMaximum());
				barBuild.setString("Completed");
				completed = true;
				running = false;
				tabSettings.doLayout();
			}
		}
	}
	
	private static void serialize(Serializable object, File file)
	{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(object);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void install() throws PluginException { }

	@Override
	protected void update() throws PluginException { }

	@Override
	protected void uninstall() throws PluginException { }
}