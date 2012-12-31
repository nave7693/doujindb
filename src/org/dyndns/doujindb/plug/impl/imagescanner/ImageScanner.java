package org.dyndns.doujindb.plug.impl.imagescanner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseContext;
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
		private JPanel tabScanner;
		
		private JButton buttonBuildStart;
		private JButton buttonBuildCancel;
		private JButton buttonBuildConfirm;
		private JLabel labelBuildPreview;
		private JProgressBar barBuild;
		private JTextArea textLogBuild;
		private JScrollPane scrollLogBuild;
		private JLabel labelDensity;
		private JSlider sliderDensity;
		private JLabel labelThreshold;
		private JSlider sliderThreshold;
		private JLabel labelMaxResults;
		private JSlider sliderMaxResults;
		private JCheckBox boxOverwrite;
		
		private JButton buttonScanPreview;
		private JTabbedPane tabsScanResult;
		private JProgressBar barScan;
		private JButton buttonScanCancel;

		private Thread builderTask;
		private boolean builderCompleted = false;
		
		private Thread scannerTask;
		
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
					labelThreshold.setBounds(0,0,0,0);
					sliderThreshold.setBounds(0,0,0,0);
					labelMaxResults.setBounds(0,0,0,0);
					sliderMaxResults.setBounds(0,0,0,0);
					boxOverwrite.setBounds(0,0,0,0);
					if(builderTask.isAlive())
					{
						buttonBuildStart.setBounds(0,0,0,0);
						buttonBuildCancel.setBounds(width / 2 - 50, height - 25, 100,  20);
						buttonBuildConfirm.setBounds(0,0,0,0);
						labelBuildPreview.setBounds(5,30,180,256);
						barBuild.setBounds(5,5,width-10,20);
						scrollLogBuild.setBounds(190,30,width-195,height-75);
					} else {
						if(builderCompleted)
						{
							buttonBuildStart.setBounds(0,0,0,0);
							buttonBuildConfirm.setBounds(width / 2 - 50, height - 25, 100,  20);
							labelBuildPreview.setBounds(5,30,180,256);
							barBuild.setBounds(5,5,width-10,20);
							scrollLogBuild.setBounds(190,30,width-195,height-75);
						} else {
							buttonBuildStart.setBounds(width / 2 - 50, height - 25, 100,  20);
							buttonBuildConfirm.setBounds(0,0,0,0);
							labelBuildPreview.setBounds(0,0,0,0);
							barBuild.setBounds(0,0,0,0);
							scrollLogBuild.setBounds(0,0,0,0);
							labelDensity.setBounds(5,5,width / 2 - 5, 20);
							sliderDensity.setBounds(width / 2 + 5,5,width / 2 - 5, 20);
							labelThreshold.setBounds(5,25,width / 2 - 5, 20);
							sliderThreshold.setBounds(width / 2 + 5,25,width / 2 - 5, 20);
							labelMaxResults.setBounds(5,45,width / 2 - 5, 20);
							sliderMaxResults.setBounds(width / 2 + 5,45,width / 2 - 5, 20);
							boxOverwrite.setBounds(5,65,width-10,20);
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
			buttonBuildStart = new JButton();
			buttonBuildStart.setText("Build");
			buttonBuildStart.setIcon(Resources.Icons.get("Plugin/Settings/Build"));
			buttonBuildStart.addActionListener(this);
			buttonBuildStart.setToolTipText("Build Cache");
			buttonBuildStart.setFocusable(false);
			bogus.add(buttonBuildStart);
			buttonBuildCancel = new JButton();
			buttonBuildCancel.setText("Cancel");
			buttonBuildCancel.setIcon(Resources.Icons.get("Plugin/Settings/Cancel"));
			buttonBuildCancel.addActionListener(this);
			buttonBuildCancel.setToolTipText("Cancel");
			buttonBuildCancel.setFocusable(false);
			bogus.add(buttonBuildCancel);
			buttonBuildConfirm = new JButton();
			buttonBuildConfirm.setText("Ok");
			buttonBuildConfirm.setIcon(Resources.Icons.get("Plugin/Settings/Confirm"));
			buttonBuildConfirm.addActionListener(this);
			buttonBuildConfirm.setToolTipText("Confirm");
			buttonBuildConfirm.setFocusable(false);
			bogus.add(buttonBuildConfirm);
			
			labelDensity = new JLabel("Density : " + 1);
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
					labelDensity.setText("Density : " + (16 - sliderDensity.getValue()));
				}				
			});
			bogus.add(sliderDensity);
			labelThreshold = new JLabel("Threshold : " + 75);
			labelThreshold.setFont(Core.Resources.Font);
			bogus.add(labelThreshold);
			sliderThreshold = new JSlider(1, 100);
			sliderThreshold.setValue(75);
			sliderThreshold.setFont(Core.Resources.Font);
			sliderThreshold.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent ce)
				{
					labelThreshold.setText("Threshold : " + sliderThreshold.getValue());
				}				
			});
			bogus.add(sliderThreshold);
			
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
					if(scannerTask.isAlive())
					{
						barScan.setBounds(5,265,180,20);
						buttonScanCancel.setBounds(5,290,180,20);
					}
					else
					{
						barScan.setBounds(0,0,0,0);
						buttonScanCancel.setBounds(0,0,0,0);
					}
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
			buttonScanPreview.addActionListener(this);
			buttonScanPreview.setBorder(null);
			buttonScanPreview.setOpaque(false);
			buttonScanPreview.setDropTarget(new DropTarget()
			{
				@Override
				public synchronized void dragOver(DropTargetDragEvent dtde)
				{
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						if(scannerTask.isAlive())
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
	                                	if(scannerTask.isAlive())
	                    					return;
	                                	scannerTask = new TaskScanner(file);
	                                	scannerTask.setName(getClass().getName()+"$CacheBuilder");
	                                	scannerTask.setDaemon(true);
	                                	scannerTask.start();
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
			tabScanner = bogus;
			
			super.add(tabs);
			
			builderTask = new TaskBuilder();
			scannerTask = new TaskScanner(null);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			tabs.setBounds(0,0,width,height);
		}
		@Override
		public void addLayoutComponent(String key,Component c) { }
		@Override
		public void removeLayoutComponent(Component c) { }
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
			if(ae.getSource() == buttonBuildStart)
			{
				if(builderTask.isAlive())
					return;
				builderTask = new TaskBuilder();
				builderTask.setName(getClass().getName()+"$CacheBuilder");
				builderTask.setDaemon(true);
				builderTask.start();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tabSettings.doLayout();
					}
				});
				return;
			}
			if(ae.getSource() == buttonBuildCancel)
			{
				if(!builderTask.isAlive())
					return;
				builderTask.interrupt();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tabSettings.doLayout();
					}
				});
				return;
			}
			if(ae.getSource() == buttonBuildConfirm)
			{
				if(builderTask.isAlive() && !builderCompleted)
					return;
				builderCompleted = false;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tabSettings.doLayout();
					}
				});
				return;
			}
			if(ae.getSource() == buttonScanPreview)
			{
				if(scannerTask.isAlive())
					return;
				JFileChooser fc = Core.UI.getFileChooser();
				int prev_option = fc.getFileSelectionMode();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
				{
					fc.setFileSelectionMode(prev_option);
					return;
				}
				final File file = fc.getSelectedFile();
				fc.setFileSelectionMode(prev_option);
            	scannerTask = new TaskScanner(file);
            	scannerTask.setName(getClass().getName()+"$CacheBuilder");
            	scannerTask.setDaemon(true);
            	scannerTask.start();
				tabScanner.doLayout();
				return;
			}
			if(ae.getSource() == buttonScanCancel)
			{
				if(!scannerTask.isAlive())
					return;
				scannerTask.interrupt();
				buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tabScanner.doLayout();
					}
				});
				return;
			}
		}
		
		final class TaskScanner extends Thread
		{
			private boolean running = false;
			
			final private File file;
			
			private TaskScanner(final File file)
			{
				this.file = file;
			}
			
			@Override
			public void interrupt() {
				running = false;
				super.interrupt();
			}

			@Override
			public boolean isInterrupted() {
				return !running;
			}

			@Override
			public synchronized void start() {
				this.running = true;
				super.start();
			}
			
			@Override
			public void run()
			{
				// Reset UI
				while (tabsScanResult.getTabCount() > 0)
					tabsScanResult.remove(0);
				buttonScanPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				barScan.setValue(barBuild.getMinimum());
				barScan.setString("Loading ...");
				
				// Init data
				int threshold = sliderThreshold.getValue();
				int max_results = sliderMaxResults.getValue();
				
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
					
					TreeMap<Double, Book> result = new TreeMap<Double, Book>(new Comparator<Double>()
					{
						@Override
						public int compare(Double a, Double b)
						{
							return b.compareTo(a);
						}
					});
					NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, sliderDensity.getValue());
					
					RecordSet<Book> books = Context.getBooks(null);
					
					barScan.setMaximum(books.size());
					barScan.setMinimum(1);
					barScan.setValue(barBuild.getMinimum());
					
					for(Book book : books)
					{
						try { Thread.sleep(1); } catch (InterruptedException ie) { }
						
						if(!running)
							return;

						File serialized_signature = new File(PLUGIN_HOME, book.getID() + ".ser");
						double similarity = nsf.getPercentSimilarity((int[][])unserialize(serialized_signature));
						
						if(similarity >= threshold)
							if(result.size() >= max_results)
							{
								double remove_me = result.lastKey();
								result.put(similarity, book);
								result.remove(remove_me);
							} else {
								result.put(similarity, book);
							}
						
						int progress = barScan.getValue() * 100 / barScan.getMaximum();
						barScan.setString("[" + barScan.getValue() + " / " + barScan.getMaximum() + "] @ " + progress + "%");
						barScan.setValue(barScan.getValue() + 1);
						if(barScan.getValue() == barScan.getMaximum())
							barScan.setValue(barScan.getMinimum());
					}
					
					boolean first_result = false;
					for(double index : result.keySet())
					{
						final Book book = result.get(index);
						if(!first_result)
						{
							JButton button = new JButton(
								new ImageIcon(
									javax.imageio.ImageIO.read(
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
										javax.imageio.ImageIO.read(
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
					
					barScan.setValue(barScan.getMaximum());
					barScan.setString("Completed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		final class TaskBuilder extends Thread
		{
			private boolean running = false;
			
			private TaskBuilder() { }
						
			@Override
			public void interrupt() {
				running = false;
				super.interrupt();
			}

			@Override
			public boolean isInterrupted() {
				return !running;
			}

			@Override
			public synchronized void start() {
				this.running = true;
				super.start();
			}

			@Override
			public void run()
			{
				// Reset UI
				labelBuildPreview.setIcon(Resources.Icons.get("Plugin/Settings/Preview"));
				textLogBuild.setText("");
				barBuild.setValue(barBuild.getMinimum());
				barBuild.setString("Loading ...");
				
				// Init data
				int density = sliderDensity.getValue();
				boolean overwrite = boxOverwrite.isSelected();
				builderCompleted = false;
				RecordSet<Book> books = Context.getBooks(null);
				
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
				builderCompleted = true;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tabSettings.doLayout();
					}
				});
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
	
	private static Object unserialize(File file)
	{
		Object unserialized = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			unserialized = ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return unserialized;
	}

	@Override
	protected void install() throws PluginException { }

	@Override
	protected void update() throws PluginException { }

	@Override
	protected void uninstall() throws PluginException { }
}