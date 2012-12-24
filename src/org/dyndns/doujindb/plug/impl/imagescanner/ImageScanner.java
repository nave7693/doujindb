package org.dyndns.doujindb.plug.impl.imagescanner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseContext;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.plug.PluginException;
import org.dyndns.doujindb.plug.impl.imagescanner.rc.Resources;

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
		
		private JButton buttonBuild;
		private JButton buttonBuildCancel;
		private JLabel labelBuildPreview;
		private JProgressBar barBuild;
		private JTextArea textLogBuild;
		private JScrollPane scrollLogBuild;
		private JLabel labelDensity;
		private JSlider sliderDensity;
		private JCheckBox boxOverwrite;

		private boolean builderRunning = false;
		private Runnable builderThread;
		
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
			buttonBuild = new JButton(Resources.Icons.get("Plugin/Settings/Build"));
			buttonBuild.addActionListener(this);
			buttonBuild.setBorder(null);
			buttonBuild.setToolTipText("Build Cache");
			buttonBuild.setFocusable(false);
			bogus.add(buttonBuild);
			buttonBuildCancel = new JButton(Resources.Icons.get("Plugin/Settings/Cancel"));
			buttonBuildCancel.addActionListener(this);
			buttonBuildCancel.setBorder(null);
			buttonBuildCancel.setToolTipText("Cancel");
			buttonBuildCancel.setFocusable(false);
			bogus.add(buttonBuildCancel);
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
			sliderDensity = new JSlider(1, 15);
			sliderDensity.setValue(3);
			sliderDensity.setFont(Core.Resources.Font);
			bogus.add(sliderDensity);
			tabs.addTab("Settings", Resources.Icons.get("Plugin/Settings"), bogus);
			
			bogus = new JPanel();
			tabs.addTab("Search", Resources.Icons.get("Plugin/Search"), new JLabel(new ImageIcon()));
			
			super.add(tabs);
			
			builderThread = new Runnable()
			{
				@Override
				public void run()
				{
					int density = sliderDensity.getValue();
					RecordSet<Book> books = Core.Database.getBooks(null);
					
					barBuild.setMaximum(books.size());
					barBuild.setMinimum(1);
					barBuild.setValue(barBuild.getMinimum());
					
					for(Book book : books)
					{
						/**
						 * Put the sleep() here and not at the end so
						 * it doesn't get skipped by calling those 'continue'
						 */
						try { Thread.sleep(100); } catch (InterruptedException e) { }
						
						if(!builderRunning)
							continue;
						
						int progress = barBuild.getValue() * 100 / barBuild.getMaximum();
						barBuild.setString(progress + "%");
						barBuild.setValue(barBuild.getValue() + 1);
						if(barBuild.getValue() == barBuild.getMaximum())
							barBuild.setValue(barBuild.getMinimum());
						
						textLogBuild.append("Building [" + book.getID() + "] ...");
						
						BufferedImage bi;
						try {
							bi = javax.imageio.ImageIO.read(Core.Repository.getPreview(book.getID()).getInputStream());
							bi = org.dyndns.doujindb.util.Image.getScaledInstance(bi, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
							
							NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(bi, density);
							int[][] signature = nsf.getSignature();
//							int[][] signature = NaiveSimilarityFinder.getSignature(bi, 5);
							serialize(signature, new File(PLUGIN_HOME, book.getID() + ".ser"));
							
//							labelBuildPreview.setIcon(new ImageIcon(NaiveSimilarityFinder.getImage(signature, 5)));
							labelBuildPreview.setIcon(new ImageIcon(nsf.getImage()));
							
							textLogBuild.append(" done.\n");
						} catch (Exception e) {
							e.printStackTrace();
							textLogBuild.append(" " + e.getMessage() + ".\n");
						}
						
						textLogBuild.setCaretPosition(textLogBuild.getText().length() - 1);
						
//						if(panelTasks.countTasks() < 1)NaiveSimilarityFinder
//							continue;
//						if(workerThread.isAlive())
//							continue;
//						else
//						{
//							for(Task task : panelTasks)
//							{
//								if(task.isDone())
//									continue;
//								workerThread = new Thread(task, getClass().getName()+"$Task[id:" + task.getId() + "]");
//								workerThread.start();
//								break;
//							}
//						}
					}
				}
			};
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			if(builderRunning)
			{
				buttonBuild.setBounds(0,0,0,0);
				buttonBuildCancel.setBounds(0,0,20,20);
				labelBuildPreview.setBounds(5,25,180,256);
				barBuild.setBounds(21,0,width-30,20);
				scrollLogBuild.setBounds(190,25,width-200,height-55);
			} else {
				buttonBuild.setBounds(0,0,20,20);
				buttonBuildCancel.setBounds(0,0,0,0);
				labelBuildPreview.setBounds(0,0,0,0);
				barBuild.setBounds(0,0,0,0);
				scrollLogBuild.setBounds(0,0,0,0);
			}
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
				builderRunning = true;
				doLayout();
				Thread process = new Thread(builderThread);
				process.setName(getClass().getName()+"$Builder");
				process.start();
				return;
			}
			if(ae.getSource() == buttonBuildCancel)
			{
				builderRunning = false;
				doLayout();
				return;
			}
			if(ae.getSource() == "")
			{
				try {
					JFileChooser fc = Core.UI.getFileChooser();
					int prev_option = fc.getFileSelectionMode();
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fc.setMultiSelectionEnabled(false);
					if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
					{
						fc.setFileSelectionMode(prev_option);
						return;
					}
					fc.setFileSelectionMode(prev_option);
					BufferedImage image = javax.imageio.ImageIO.read(fc.getSelectedFile());
					image = org.dyndns.doujindb.util.Image.getScaledInstance(image, 256, 256, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
					NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(image, 3);
					for(int i=1;i<=15;i++)
						serialize(NaiveSimilarityFinder.getInstance(image, i).getSignature(), new File("X:/signature" + String.format("%02d", i) + ".ser"));
					Iterable<BufferedImage> previews = new Iterable<BufferedImage>()
					{
						@Override
						public Iterator<BufferedImage> iterator()
						{
							return new Iterator<BufferedImage>()
							{
								Iterator<Book> books = Core.Database.getBooks(null).iterator();
								
								@Override
								public boolean hasNext() {
									return books.hasNext();
								}
	
								@Override
								public BufferedImage next() {
									try {
										System.out.print(".");
										return javax.imageio.ImageIO.read(Core.Repository.getPreview(books.next().getID()).getInputStream());
									} catch (Exception e) {
										return null;
									}
								}
	
								@Override
								public void remove() {
									/**
									 * Removes from the underlying collection the last element returned by the iterator (optional operation).
									 * This method can be called only once per call to next.
									 * The behavior of an iterator is unspecified if the underlying collection is modified while the iteration is in progress in any way other than by calling this method. 
									 */
								}
								
							};
						}
					};
					;
					int index = 0;
					tabs.setTabPlacement(SwingConstants.LEFT);
					tabs.addTab("[Original]", Resources.Icons.get("Plugin/Search"), new JLabel(new ImageIcon(image)));
//					serialize(nsf.getSignature(), new File("X:/signature.ser"));
					for(BufferedImage bi : nsf.getSimilarity(previews, 15))
						tabs.addTab("[" + index++ + "]", Resources.Icons.get("Plugin/Search"), new JLabel(new ImageIcon(bi)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				new Thread()
				{
					@Override
					public void run()
					{
						super.setPriority(Thread.MIN_PRIORITY);
						try {
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
		
//		private final class TaskUI extends JPanel implements LayoutManager, ActionListener, MouseListener
//		{
//			;
//		}
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