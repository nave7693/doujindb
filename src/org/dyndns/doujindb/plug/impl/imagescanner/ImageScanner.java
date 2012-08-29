package org.dyndns.doujindb.plug.impl.imagescanner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseContext;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.plug.Plugin;

/**  
* ImageScanner.java - Plugin to search for matching images in your Datastore.
* @author  nozomu
* @version 1.0
*/
public final class ImageScanner implements Plugin
{

	private static String UUID = "{D18B8C85-BE10-4937-9C5A-885CEAD64D35}";
	private static DataBaseContext Context;
	
	private JComponent UI;
	private static ImageIcon PluginIcon = new ImageIcon(ImageScanner.class.getResource("rc/plugin-icon.png"));
	private static ImageIcon IconAdd = new ImageIcon(ImageScanner.class.getResource("rc/task-add.png"));
	private static ImageIcon IconStop = new ImageIcon(ImageScanner.class.getResource("rc/task-stop.png"));
	private static ImageIcon IconDelete = new ImageIcon(ImageScanner.class.getResource("rc/task-delete.png"));
	private static ImageIcon IconCompleted = new ImageIcon(ImageScanner.class.getResource("rc/task-completed.png"));
	private static ImageIcon IconTask = new ImageIcon(ImageScanner.class.getResource("rc/task.png"));
	private static ImageIcon IconLoading = new ImageIcon(ImageScanner.class.getResource("rc/loading.gif"));
	
	public ImageScanner()
	{
		Context = Core.Database.getContext(UUID);
		
		UI = new PluginUI();
	}
	
	@Override
	public Icon getIcon() {
		return PluginIcon;
	}
	@Override
	public String getName() {
		return "Image Scanner";
	}
	@Override
	public String getDescription() {
		return "The Image Scanner plugin lets you search for matching images in your Datastore.";
	}
	@Override
	public String getVersion() {
		return "0.1";
	}
	@Override
	public String getAuthor() {
		return "Nozomu";
	}
	@Override
	public String getWeblink() {
		return "http://code.google.com/p/doujindb/";
	}
	@Override
	public JComponent getUI() {
		return UI;
	}

	@SuppressWarnings("serial")
	private final class PluginUI extends JPanel implements LayoutManager, ActionListener
	{
		private JTabbedPane tabs;
		private JButton buttonAddTask;
		
		public PluginUI()
		{
			super();
			super.setLayout(this);
			tabs = new JTabbedPane();
			tabs.setFont(Core.Resources.Font);
			tabs.setFocusable(false);
			super.add(tabs);
			
			buttonAddTask = new JButton(IconAdd);
			buttonAddTask.addActionListener(this);
			buttonAddTask.setBorder(null);
			buttonAddTask.setFocusable(false);
			super.add(buttonAddTask);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			buttonAddTask.setBounds(0,0,20,20);
			tabs.setBounds(0,21,width,height);
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
			if(ae.getSource() == buttonAddTask)
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
				int wi = image.getWidth(null),
				hi = image.getHeight(null),
				wl = 256, 
				hl = 256; 
				if(!(wi < wl) && !(hi < hl)) // Cannot scale an image smaller than 256x256, or getScaledInstance is going to loop
					if ((double)wl/wi > (double)hl/hi)
					{
						wi = (int) (wi * (double)hl/hi);
						hi = (int) (hi * (double)hl/hi);
					}else{
						hi = (int) (hi * (double)wl/wi);
						wi = (int) (wi * (double)wl/wi);
					}
				image = org.dyndns.doujindb.util.Image.getScaledInstance(image, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
				tabs.addTab("", IconLoading, new JLabel(new ImageIcon(NaiveSimilarityFinder.getInstance(image).getSignature())));
				} catch (IOException e) {
					e.printStackTrace();
				}
//				new Thread()
//				{
//					@Override
//					public void run()
//					{
//						super.setPriority(Thread.MIN_PRIORITY);
//						
//						JFileChooser fc = Core.UI.getFileChooser();
//						int prev_option = fc.getFileSelectionMode();
//						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//						fc.setMultiSelectionEnabled(false);
//						if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
//						{
//							fc.setFileSelectionMode(prev_option);
//							return;
//						}
//						fc.setFileSelectionMode(prev_option);
//						Map<Integer, BufferedImage> images;
//						try {
//							BufferedImage image = javax.imageio.ImageIO.read(fc.getSelectedFile());
//							int wi = image.getWidth(null),
//							hi = image.getHeight(null),
//							wl = 256, 
//							hl = 256; 
//							if(!(wi < wl) && !(hi < hl)) // Cannot scale an image smaller than 256x256, or getScaledInstance is going to loop
//								if ((double)wl/wi > (double)hl/hi)
//								{
//									wi = (int) (wi * (double)hl/hi);
//									hi = (int) (hi * (double)hl/hi);
//								}else{
//									hi = (int) (hi * (double)wl/wi);
//									wi = (int) (wi * (double)wl/wi);
//								}
//							image = org.dyndns.doujindb.util.Image.getScaledInstance(image, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
//							NaiveSimilarityFinder nsf = NaiveSimilarityFinder.getInstance(image);
//					tabs.addTab("", IconLoading, new JLabel(new ImageIcon(image)));		
//							Vector<BufferedImage> bis = new Vector<BufferedImage>();
//							for(Book book : Context.getBooks(null))
//								try {
//									System.out.println("Loading " + book.getID() + " ...");
//									bis.add(javax.imageio.ImageIO.read(Core.Repository.getPreview(book.getID()).getInputStream()));
//								} catch (RepositoryException re) {
//									re.printStackTrace();
//								} catch (DataBaseException dbe) {
//									dbe.printStackTrace();
//								} catch (IOException ioe) {
//									ioe.printStackTrace();
//								}
//							images = nsf.getSimilarity(bis);
//							int results = 0;
//							System.out.println(images.size());
//							for(Integer key : images.keySet())
//							{
//								System.out.println(key);
//								tabs.addTab("" + key, new JLabel(new ImageIcon(images.get(key))));
//								if(results++ > 10)
//									break;
//							}
//						} catch (IOException ioe) {
//							ioe.printStackTrace();
//						}
//					}
//				}.start();
			}
		}
		
		@SuppressWarnings("unused")
		private final class Task implements Runnable
		{
			
			public static final int TASK_RUNNING = 0x01;
			public static final int TASK_QUEUED = 0x02;
			public static final int TASK_ERROR = 0x03;
			public static final int TASK_WARNING = 0x04;
			public static final int TASK_COMPLETED = 0x05;
			private int status = TASK_QUEUED;
			
			public Task()
			{
				
			}

			@Override
			public void run() { }
		}
		
		private class TaskUI extends JPanel
		{
			private JLabel Status;
			private JLabel Icon;
			
			public TaskUI()
			{
				
			}
		}
	}
}
