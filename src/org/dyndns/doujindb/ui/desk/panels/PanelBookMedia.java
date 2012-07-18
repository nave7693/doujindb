package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

@SuppressWarnings("serial")
public class PanelBookMedia extends JPanel implements DataBaseListener
{
	private Book tokenBook;
	private JButton buttonReload;
	private JButton buttonUpload;
	private JButton buttonDownload;
	private JButton buttonDelete;
	private JButton buttonPackage;
	private MediaTree treeMedia;
	private JScrollPane treeMediaScroll;
	
	public PanelBookMedia(Book book)
	{
		super();
		tokenBook = book;
		treeMedia = new MediaTree(null);
		treeMediaScroll = new JScrollPane(treeMedia);
		add(treeMediaScroll);
		buttonReload = new JButton(Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Reload"));
		buttonReload.setFocusable(false);
		buttonReload.setToolTipText("Reload");
		buttonReload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				displayUI();
			}			
		});
		add(buttonReload);
		buttonUpload = new JButton(Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Upload"));
		buttonUpload.setFocusable(false);
		buttonUpload.setToolTipText("Upload");
		buttonUpload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread(getClass().getName()+"/ActionPerformed/Upload")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = Core.UI.getFileChooser();
							fc.setMultiSelectionEnabled(true);
							int prev_option = fc.getFileSelectionMode();
							fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
							if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
							{
								fc.setMultiSelectionEnabled(false);
								fc.setFileSelectionMode(prev_option);
								return;
							}
							File files[] = fc.getSelectedFiles();
							DataFile up_folder = Core.Repository.child(tokenBook.getID());
							Thread uploader = new Uploader(up_folder, files);
							uploader.start();
							try { while(uploader.isAlive()) sleep(10); } catch (Exception e) {}
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(prev_option);
							displayUI();	
						} catch (Exception e) {
							Core.Logger.log(e.getMessage(), Level.ERROR);
							e.printStackTrace();
						}
					}
				}.start();
			}
		});
		add(buttonUpload);
		buttonDownload = new JButton(Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Download"));
		buttonDownload.setFocusable(false);
		buttonDownload.setToolTipText("Download");
		buttonDownload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(treeMedia.CheckBoxRenderer.getCheckedPaths().length < 1)
					return;
				new Thread(getClass().getName()+"/ActionPerformed/Download")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = Core.UI.getFileChooser();
							fc.setMultiSelectionEnabled(false);
							int prev_option = fc.getFileSelectionMode();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
							{
								fc.setMultiSelectionEnabled(false);
								fc.setFileSelectionMode(prev_option);
								return;
							}
							File dl_folder = fc.getSelectedFile();
							Set<DataFile> dss = new TreeSet<DataFile>();
							{
								TreePath[] paths = treeMedia.CheckBoxRenderer.getCheckedPaths();
								DataFile root_ds = Core.Repository.child(tokenBook.getID());
								for(TreePath path : paths)
								try
								{
									StringBuilder sb = new StringBuilder();
									Object[] nodes = path.getPath();
									for(int i = 0;i < nodes.length;i++) {
										sb.append(nodes[i].toString());
									}
									DataFile ds = root_ds.child(sb.toString());
									dss.add(ds);
								} catch (Exception e) { e.printStackTrace(); }
							}
							Thread downloader = new Downloader(dl_folder, dss);
							downloader.start();
							try { while(downloader.isAlive()) sleep(10); } catch (Exception e) {}
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(prev_option);
							displayUI();
						} catch (Exception e) {
							Core.Logger.log(e.getMessage(), Level.ERROR);
							e.printStackTrace();
						}
					}
				}.start();				
			}
		});
		add(buttonDownload);
		buttonDelete = new JButton(Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Delete"));
		buttonDelete.setFocusable(false);
		buttonDelete.setToolTipText("Delete");
		buttonDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(treeMedia.CheckBoxRenderer.getCheckedPaths().length < 1)
					return;
				JPanel panel = new JPanel();
				panel.setSize(250, 150);
				panel.setLayout(new GridLayout(2, 1));
				JLabel lab = new JLabel("<html><body>Delete selected file/folders?<br/><i>(This cannot be undone)</i></body></html>");
				lab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				lab.setFont(Core.Resources.Font);
				panel.add(lab);
				JPanel bottom = new JPanel();
				bottom.setLayout(new GridLayout(1, 2));
				JButton canc = new JButton("Cancel");
				canc.setFont(Core.Resources.Font);
				canc.setMnemonic('C');
				canc.setFocusable(false);
				canc.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DouzDialog window = (DouzDialog) ((JComponent)ae.getSource()).getRootPane().getParent();
						window.dispose();
					}					
				});
				JButton ok = new JButton("Ok");
				ok.setFont(Core.Resources.Font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						try
						{
							TreePath[] paths = treeMedia.CheckBoxRenderer.getCheckedPaths();
							DataFile root_ds = Core.Repository.child(tokenBook.getID());
							for(TreePath path : paths)
							try
							{
								Object os[] = path.getPath();
								DataFile ds = root_ds;
								for(int k=1;k<os.length;k++)
									if(os[k].toString().startsWith("/"))
										ds = ds.child(os[k].toString().substring(1));
									else
										ds = ds.child(os[k].toString());
								ds.delete();
							} catch (Exception e) { e.printStackTrace(); }
						} catch (Exception e) {
							Core.Logger.log(e.getMessage(), Level.ERROR);
						}
						displayUI();
						DouzDialog window = (DouzDialog) ((JComponent)ae.getSource()).getRootPane().getParent();
						window.dispose();
					}					
				});
				bottom.add(ok);
				bottom.add(canc);
				bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
				panel.add(bottom);
				try {
					Core.UI.Desktop.showDialog(
							panel,
							Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Delete"),
							"Delete");
				} catch (PropertyVetoException pve)
				{
					Core.Logger.log(pve.getMessage(), Level.WARNING);
				}
			}
		});
		add(buttonDelete);
		buttonPackage = new JButton(Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Package"));
		buttonPackage.setFocusable(false);
		buttonPackage.setToolTipText("Package");
		buttonPackage.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread(getClass().getName()+"/ActionPerformed/Package")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = Core.UI.getFileChooser();
							fc.setMultiSelectionEnabled(true);
							int prev_option = fc.getFileSelectionMode();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
							{
								fc.setMultiSelectionEnabled(false);
								fc.setFileSelectionMode(prev_option);
								return;
							}
							File file = fc.getSelectedFile();
							Thread packager = new Packager(tokenBook, file);
							packager.start();
							try { while(packager.isAlive()) sleep(10); } catch (Exception e) {}
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(prev_option);
						} catch (Exception e) {
							Core.Logger.log(e.getMessage(), Level.ERROR);
							e.printStackTrace();
						}
					}
				}.start();
			}
		});
		add(buttonPackage);
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth(),
					height = parent.getHeight();
				buttonReload.setBounds(1,1,20,20);
				buttonUpload.setBounds(width-20,1,20,20);
				buttonDownload.setBounds(width-40,1,20,20);
				buttonDelete.setBounds(width-60,1,20,20);
				buttonPackage.setBounds(width-80,1,20,20);
				treeMediaScroll.setBounds(1,21,width-2,height-25);
			}
			
			@Override
			public void addLayoutComponent(String key,Component c) {}
			
			@Override
			public void removeLayoutComponent(Component c) {}
			
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
			     return new Dimension(256,256+20);
			}
			
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
			     return parent.getPreferredSize();
			}
		});
		new SwingWorker<Void, Object>() {
			@Override
			public Void doInBackground() {
				displayUI();
				return null;
			}
		}.execute();
	}
	
	private void displayUI()
	{
		try {
			if(!Core.Database.getBooks(null).contains(tokenBook))
				return;
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String root_id = "/";
					MutableTreeNode root = new DefaultMutableTreeNode(root_id);
					buildTree(Core.Repository.child(tokenBook.getID()), root);
					treeMedia.clearSelection();
					treeMedia.CheckBoxRenderer.clearSelection();
					DefaultTreeModel dtm = (DefaultTreeModel) treeMedia.getModel();
					dtm.setRoot(root);
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							PanelBookMedia.super.validate();
						}
					});
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				}
			}
		});
	}
	
	private final class MediaTree extends JTree
	{
		private MediaTreeRenderer renderer;
		public DouzCheckBoxTreeCellRenderer CheckBoxRenderer;

	public MediaTree(TreeNode root)
	{
		super(root);
		super.setFocusable(false);
		super.setFont(Core.Resources.Font);
		super.setEditable(false);
		super.setRootVisible(true);
		super.setScrollsOnExpand(true);
		renderer = new MediaTreeRenderer();
		super.setCellRenderer(renderer);
		CheckBoxRenderer = new DouzCheckBoxTreeCellRenderer(this, super.getCellRenderer()); 
		super.setCellRenderer(CheckBoxRenderer);
	}

	private final class MediaTreeRenderer extends DefaultTreeCellRenderer
	{
		private Hashtable<String,Icon> renderIcon;

	public MediaTreeRenderer()
	{
		renderIcon=new Hashtable<String,Icon>();
	    setBackgroundSelectionColor(MetalLookAndFeel.getWindowBackground());
	    renderIcon.put("/",Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Repository"));
	    renderIcon.put("?",Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Types/Unknown"));
	    renderIcon.put("Folder",Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Types/Folder"));
	    renderIcon.put(".zip",Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Types/Archive"));
	}
	private String getExtension(String file)
	{
		if(file.lastIndexOf(".") == -1)
			return "";
		return file.toLowerCase().substring(file.lastIndexOf("."));
	}
	public Component getTreeCellRendererComponent(
			JTree tree,
	    Object value,
	    boolean sel,
	    boolean expanded,
	    boolean leaf,
	    int row,
	    boolean hasFocus){
		super.getTreeCellRendererComponent(
	    		tree,
	        value,
	        sel,
	        expanded,
	        leaf,
	        row,
	        hasFocus);
	    setIcon((ImageIcon)renderIcon.get("?"));
	    if(tree.getModel().getRoot()==value)
	    {
	    	setIcon((ImageIcon)renderIcon.get("/"));
	    	try {
				setText("datastore://" + tokenBook.getID());
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
	    	return this;
	    }
	    if(value.toString().endsWith("/"))
	    {
	    	setIcon((ImageIcon)renderIcon.get("Folder"));
	    	super.setText(super.getText().substring(0, super.getText().length()-1));
	    	return this;
	    }
	    if(renderIcon.containsKey(getExtension(value.toString())))
	    {
	    	setIcon((ImageIcon)renderIcon.get(getExtension(value.toString())));
	    }
	    return this;
	}
	}
	}
	
	private void buildTree(DataFile dss, MutableTreeNode parent) throws RepositoryException, DataBaseException
	{
		int k = 0; 
		for(DataFile ds : dss.children())
		{
	        if(ds.isDirectory())
	        {
		        DefaultMutableTreeNode sub = new DefaultMutableTreeNode(ds.getName() + "/");
		        parent.insert(sub, k);
		        buildTree(ds, sub);
	        }else{
	        	if(ds.getName().startsWith("."))
	        		continue;
		        DefaultMutableTreeNode sub = new DefaultMutableTreeNode(ds.getName());
		        parent.insert(sub, k);
	        }
	        k++;
	    }
	}
	
	/*private void buildTree(String path, MutableTreeNode parent)
	{
	     File root = new File(path);
		 String fs[] = root.list();
		 if(fs == null)
			 return;
		 for(int k=0;k<fs.length;k++){
	         if(new File(path + File.separator + fs[k]).isDirectory())
	         {
		         DefaultMutableTreeNode sub = new DefaultMutableTreeNode(fs[k] + "/");
		         parent.insert(sub, k);
		         buildTree(path + File.separator + fs[k], sub);
	         }else{
		         DefaultMutableTreeNode sub = new DefaultMutableTreeNode(fs[k]);
		         parent.insert(sub, k);
	         }
	     }
	}*/
	
	private final class Downloader extends Thread
	{
		private String label_file_current = "";
		private long progress_file_current = 0;
		private long progress_file_max = 1;
		private long progress_overall_current = 0;
		private long progress_overall_max = 1;
		private JProgressBar progressbar_file;
		private JProgressBar progressbar_overall;
		private JButton cancel;
		private boolean stopped = false;
		private Timer clock;
		
		private File dl_root;
		private Set<DataFile> dss;
		
		public Downloader(File dl_root, Set<DataFile> dss)
		{
			this.dl_root = dl_root;
			this.dss = dss;
		}
		
		private int count(DataFile ds_root) throws RepositoryException, DataBaseException
		{
			int count = 0;
			for(DataFile ds : ds_root.children())
				if(ds.isDirectory())
					count += count(ds);
				else
					count += 1;
			return count;
		}
		
		private void download(DataFile dl) throws IOException, Exception
		{
			File dst = new File(dl_root, dl.getPath());
			label_file_current = dl.getName();
			if(dl.isDirectory())
			{
				progress_overall_current++;
				dst.mkdirs();
				for(DataFile ds : dl.children())
					download(ds);
			}else
			{
				progress_overall_current++;
				dst.getParentFile().mkdirs();
				OutputStream out = new FileOutputStream(dst);
				InputStream in = dl.getInputStream();
				byte[] buff = new byte[0x800];
				int read;
				progress_file_current = 0;
				progress_file_max = dl.size();
				while((read = in.read(buff)) != -1)
				{
					out.write(buff, 0, read);		
					progress_file_current += read;
					if(stopped)
						throw new Exception("Thread stopped by user input.");
				}
				in.close();
				out.close();
			}
		}
		
		@Override
		public void run()
		{
			super.setPriority(Thread.MIN_PRIORITY);
			JPanel comp = new JPanel();
			comp.setMinimumSize(new Dimension(250,75));
			comp.setPreferredSize(new Dimension(250,75));
			comp.setLayout(new GridLayout(3,1));
			cancel = new JButton("Cancel");
			cancel.setFont(Core.Resources.Font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DouzDialog window = (DouzDialog) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(Core.Resources.Font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
			comp.add(progressbar_overall);
			comp.add(progressbar_file);
			comp.add(cancel);
			clock = new Timer(50, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					int file = (int)(progress_file_current*100/(progress_file_max==0?1:progress_file_max));
					progressbar_file.setString(file+"%");
					progressbar_file.setValue(file);
					int overall = (int)(progress_overall_current*100/(progress_overall_max==0?1:progress_overall_max));
					progressbar_overall.setString(label_file_current + " (" + overall+"%)");
					progressbar_overall.setValue(overall);
				}					
			});
			clock.start();
			Vector<String> errors = new Vector<String>();
			try
			{
				Core.UI.Desktop.showDialog(
						comp,
						Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Download"),
						"Downloading ...");
				progress_overall_max = 0;
				for(DataFile ds : dss)
					progress_overall_max += count(ds);
				for(DataFile ds : dss)
				{
					try
					{
						download(ds);
					} catch (Exception e) {}
					/*try
					{
						File dst = new File(dl_root, ds.getName());
						if(ds.isDirectory())
						{
							dst.mkdirs();
						}else
						{
							ds.getParent().mkdirs();
							OutputStream out = new FileOutputStream(dst);
							InputStream in = ds.getInputStream();
							byte[] buff = new byte[0x800];
							int read;
							progress_bytes_current = 0;
							progress_bytes_max = ds.size();
							while((read = in.read(buff)) != -1)
							{
								out.write(buff, 0, read);		
								progress_bytes_current += read;
								if(stopped)
									throw new Exception("Thread stopped by user input.");
							}
							in.close();
							out.close();
						}
						progress_file_current++;
					} catch (Exception e) {
						progress_file_current++;
						errors.add(ds.getPath());
					}*/
				}
			} catch (PropertyVetoException pve) {
				Core.Logger.log(pve.getMessage(), Level.WARNING);
				pve.printStackTrace();
			} catch (RepositoryException dse) {
				Core.Logger.log(dse.getMessage(), Level.WARNING);
				dse.printStackTrace();
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.WARNING);
				dbe.printStackTrace();
			}
			clock.stop();
			DouzDialog window = (DouzDialog) comp.getRootPane().getParent();
			window.dispose();
			;
			if(errors.size() == 0)
				return;
			{
				JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
				panel.setLayout(new BorderLayout(5, 5));
				JLabel lab = new JLabel("<html><body>" +
						"The following entries were not downloaded.<br>" +
						"Make sure the download directory is writable and empty.<br>" +
						"</body></html>");
				panel.add(lab, BorderLayout.NORTH);
				JList<String> list = new JList<String>(errors);
				list.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
				list.setSelectionBackground(list.getSelectionForeground());
				list.setSelectionForeground(Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
				panel.add(new JScrollPane(list), BorderLayout.CENTER);
				JButton ok = new JButton("Ok");
				ok.setFont(Core.Resources.Font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
						window.dispose();
					}					
				});
				JPanel centered = new JPanel();
				centered.setLayout(new GridLayout(1,3));
				centered.add(new JLabel());
				centered.add(ok);
				centered.add(new JLabel());
				panel.add(centered, BorderLayout.SOUTH);
				try
				{
					Core.UI.Desktop.showDialog(
							panel,
							Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Download"),
							"Downloading - Error");
				} catch (PropertyVetoException pve) {} 
			}
		}
	}
	
	private final class Uploader extends Thread
	{
		private String label_file_current = "";
		private long progress_file_current = 0;
		private long progress_file_max = 1;
		private long progress_overall_current = 0;
		private long progress_overall_max = 1;
		private JProgressBar progressbar_file;
		private JProgressBar progressbar_overall;
		private JButton cancel;
		private boolean stopped = false;
		private Timer clock;
		
		private DataFile up_root;
		private File[] files;
		
		public Uploader(DataFile up_root, File[] files)
		{
			this.up_root = up_root;
			this.files = files;
		}
		
		private int count(File base)
		{
			int count = 0;
			for(File file : base.listFiles())
				if(file.isDirectory())
					count += count(file);
				else
					count++;
			return count;
		}
		
		private void upload(File up, DataFile path) throws IOException, Exception
		{
			DataFile dst = path.child(up.getName());
			label_file_current = up.getName();
			if(up.isDirectory())
			{
				progress_overall_current++;
				dst.mkdirs();
				for(File file : up.listFiles())
					upload(file, dst);
			}else
			{
				progress_overall_current++;
				dst.getParent().mkdirs();
				dst.touch();
				OutputStream out = dst.getOutputStream();
				InputStream in = new FileInputStream(up);
				byte[] buff = new byte[0x800];
				int read;
				progress_file_current = 0;
				progress_file_max = up.length() + 1;
				while((read = in.read(buff)) != -1)
				{
					out.write(buff, 0, read);		
					progress_file_current += read;
					if(stopped)
						throw new Exception("Thread stopped by user input.");
				}
				in.close();
				out.close();
			}
		}
		
		@Override
		public void run()
		{
			super.setPriority(Thread.MIN_PRIORITY);
			JPanel comp = new JPanel();
			comp.setMinimumSize(new Dimension(250,75));
			comp.setPreferredSize(new Dimension(250,75));
			comp.setLayout(new GridLayout(3,1));
			cancel = new JButton("Cancel");
			cancel.setFont(Core.Resources.Font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DouzDialog window = (DouzDialog) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(Core.Resources.Font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
			comp.add(progressbar_overall);
			comp.add(progressbar_file);
			comp.add(cancel);
			clock = new Timer(50, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					int file = (int)(progress_file_current*100/(progress_file_max==0?1:progress_file_max));
					progressbar_file.setString(file+"%");
					progressbar_file.setValue(file);
					int overall = (int)(progress_overall_current*100/(progress_overall_max==0?1:progress_overall_max));
					progressbar_overall.setString(label_file_current + " (" + overall+"%)");
					progressbar_overall.setValue(overall);
				}					
			});
			clock.start();
			Vector<String> errors = new Vector<String>();
			try
			{
				Core.UI.Desktop.showDialog(
						comp,
						Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Upload"),
						"Uploading ...");
				progress_overall_max = 0;
				for(File file : files)
					if(file.isDirectory())
						progress_overall_max += count(file);
					else
						progress_overall_max += 1;
				for(File file : files)
				{
					try
					{
						upload(file, up_root);
					} catch (Exception e) { e.printStackTrace(); }
				}
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
				Core.Logger.log(pve.getMessage(), Level.WARNING);
			}
			clock.stop();
			DouzDialog window = (DouzDialog) comp.getRootPane().getParent();
			window.dispose();
			;
			if(errors.size() == 0)
				return;
			{
				JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
				panel.setLayout(new BorderLayout(5, 5));
				JLabel lab = new JLabel("<html><body>" +
						"The following entries were not uploaded.<br>" +
						"Make sure the upload directory is writable and empty.<br>" +
						"</body></html>");
				panel.add(lab, BorderLayout.NORTH);
				JList<String> list = new JList<String>(errors);
				list.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
				list.setSelectionBackground(list.getSelectionForeground());
				list.setSelectionForeground(Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
				panel.add(new JScrollPane(list), BorderLayout.CENTER);
				JButton ok = new JButton("Ok");
				ok.setFont(Core.Resources.Font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DouzDialog window = (DouzDialog)((JComponent)ae.getSource()).getRootPane().getParent();
						window.dispose();
					}					
				});
				JPanel centered = new JPanel();
				centered.setLayout(new GridLayout(1,3));
				centered.add(new JLabel());
				centered.add(ok);
				centered.add(new JLabel());
				panel.add(centered, BorderLayout.SOUTH);
				try
				{
					Core.UI.Desktop.showDialog(
							panel,
							Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Upload"),
							"Uploading - Error");
				} catch (PropertyVetoException pve) {} 
			}
		}
	}
	
	private static final class Packager extends Thread
	{
		private final String PACKAGE_INDEX = ".xml";
		private final String PACKAGE_PREVIEW = ".preview";
		
		private long progress_bytes_current = 0;
		private long progress_bytes_max = 1;
		private long progress_file_current = 0;
		private long progress_file_max = 1;
		private long progress_overall_current = 0;
		private long progress_overall_max = 1;
		private JProgressBar progressbar_bytes;
		private JProgressBar progressbar_file;
		private JProgressBar progressbar_overall;
		private JButton cancel;
		private boolean stopped = false;
		private Timer clock;
		private Book book;
		private File destdir;
		
		public Packager(Book book, File dest)
		{
			this.book = book;
			this.destdir = dest;
		}
		
		@Override
		public void run()
		{
			super.setPriority(Thread.MIN_PRIORITY);
			JPanel comp = new JPanel();
			comp.setMinimumSize(new Dimension(250,75));
			comp.setPreferredSize(new Dimension(250,75));
			comp.setLayout(new GridLayout(4,1));
			cancel = new JButton("Cancel");
			cancel.setFont(Core.Resources.Font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DouzDialog window = (DouzDialog) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_bytes = new JProgressBar(1,100);
			progressbar_bytes.setStringPainted(true);
			progressbar_bytes.setFont(Core.Resources.Font);
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(Core.Resources.Font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
			comp.add(progressbar_overall);
			comp.add(progressbar_file);
			comp.add(progressbar_bytes);
			comp.add(cancel);
			clock = new Timer(50, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					int bytes = (int)(progress_bytes_current*100/progress_bytes_max);
					progressbar_bytes.setString(bytes+"%");
					progressbar_bytes.setValue(bytes);
					int file = (int)(progress_file_current*100/progress_file_max);
					progressbar_file.setString(file+"%");
					progressbar_file.setValue(file);
					int overall = (int)(progress_overall_current*100/progress_overall_max);
					//progressbar_overall.setString(overall+"%");
					progressbar_overall.setValue(overall);
				}					
			});
			clock.start();
			try
			{
				Core.UI.Desktop.showDialog(
						comp,
						Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Package"),
						"Exporting ...");
				progress_overall_max = 1;
				{
					File zip = new File(destdir, book + Core.Properties.get("org.dyndns.doujindb.dat.file_extension").asString());
					DataFile ds = Core.Repository.child(book.getID());
					progress_file_max = count(ds);
					progress_file_current = 0;
					progressbar_overall.setString(book.toString());
					try
					{
						ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zip));
						zout.setLevel(9);
						;
						ZipEntry entry = new ZipEntry(PACKAGE_INDEX);
						zout.putNextEntry(entry);
						writeMetadata(book, zout);
						zout.closeEntry();
						;
						zip("", ds.children(), zout);
						zout.close();
					} catch (IOException ioe) {
						zip.delete();
						Core.Logger.log(ioe.getMessage(), Level.WARNING);
					}
					progress_overall_current++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Core.Logger.log(e.getMessage(), Level.WARNING);
			}
			clock.stop();
			DouzDialog window = (DouzDialog) comp.getRootPane().getParent();
			window.dispose();
		}
		
		private int count(DataFile ds_root) throws RepositoryException
		{
			int count = 0;
			for(DataFile ds : ds_root.children())
				if(ds.isDirectory())
					count += count(ds);
				else
					count += 1;
			return count;
		}
		
		private void zip(String base, Set<DataFile> dss, ZipOutputStream zout) throws IOException, Exception
		{
			for(DataFile ds : dss)
			{
				if(ds.isDirectory())
				{
					ZipEntry entry = new ZipEntry(base + ds.getName() + "/");
					entry.setMethod(ZipEntry.DEFLATED);
					try {
						zout.putNextEntry(entry);
						zout.closeEntry();
					} catch (IOException ioe) {
						ioe.printStackTrace();
						Core.Logger.log(ioe.getMessage(), Level.WARNING);
					}
					zip(base + ds.getName() + "/", ds.children(), zout);
				}else
				{
					if(ds.getName().equals(PACKAGE_PREVIEW))
						continue;
					int read;
					byte[] buff = new byte[0x800];
					ZipEntry entry = new ZipEntry(base + ds.getName());
					entry.setMethod(ZipEntry.DEFLATED);
					try
					{
						InputStream in = ds.getInputStream();
						zout.putNextEntry(entry);
						progress_bytes_max = ds.size();
						progress_bytes_current = 0;
						while ((read = in.read(buff)) != -1)
						{
							zout.write(buff, 0, read);
							progress_bytes_current += read;
							if(stopped)
								throw new Exception("Thread stopped by user input.");
						}
						zout.closeEntry();
						progress_file_current++;
						in.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
						Core.Logger.log(ioe.getMessage(), Level.WARNING);
					}
				}
			}
		}
		
		private void writeMetadata(Book book, OutputStream dest) throws DataBaseException
		{
			XMLBook doujin = new XMLBook();
			doujin.japaneseName = book.getJapaneseName();
			doujin.translatedName = book.getTranslatedName();
			doujin.romanjiName = book.getRomanjiName();
			doujin.Convention = book.getConvention() == null ? "" : book.getConvention().getTagName();
			doujin.Released = book.getDate();
			doujin.Type = book.getType();
			doujin.Pages = book.getPages();
			doujin.Adult = book.isAdult();
			doujin.Decensored = book.isDecensored();
			doujin.Colored = book.isColored();
			doujin.Translated = book.isTranslated();
			doujin.Rating = book.getRating();
			doujin.Info = book.getInfo();
			for(Artist a : book.getArtists())
				doujin.artists.add(a.getJapaneseName());
			for(Circle c : book.getCircles())
				doujin.circles.add(c.getJapaneseName());
			for(Parody p : book.getParodies())
				doujin.parodies.add(p.getJapaneseName());
			for(Content ct : book.getContents())
				doujin.contents.add(ct.getTagName());
			try
			{
				JAXBContext context = JAXBContext.newInstance(XMLBook.class);
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
				m.marshal(doujin, dest);
			} catch (Exception e) {
				Core.Logger.log("Error parsing XML file (" + e.getMessage() + ").", Level.WARNING);
				e.printStackTrace();
			}
		}
		
		@XmlRootElement(name="Doujin")
		@SuppressWarnings("unused")
		private static final class XMLBook
		{
			@XmlElement(required=true)
			private String japaneseName;
			@XmlElement(required=false)
			private String translatedName = "";
			@XmlElement(required=false)
			private String romanjiName = "";
			@XmlElement(required=false)
			private String Convention = "";
			@XmlElement(required=false)
			private Date Released;
			@XmlElement(required=false)
			private Type Type;
			@XmlElement(required=false)
			private int Pages;
			@XmlElement(required=false)
			private boolean Adult;
			@XmlElement(required=false)
			private boolean Decensored;
			@XmlElement(required=false)
			private boolean Translated;
			@XmlElement(required=false)
			private boolean Colored;
			@XmlElement(required=false)
			private Rating Rating;
			@XmlElement(required=false)
			private String Info;
			@XmlElement(name="Artist", required=false)
			private List<String> artists = new Vector<String>();
			@XmlElement(name="Circle", required=false)
			private List<String> circles = new Vector<String>();
			@XmlElement(name="Parody", required=false)
			private List<String> parodies = new Vector<String>();
			@XmlElement(name="Content", required=false)
			private List<String> contents = new Vector<String>();
		}
	}

	@Override
	public void recordAdded(Record rcd) {}
	
	@Override
	public void recordDeleted(Record rcd) {}
	
	@Override
	public void recordUpdated(Record rcd)
	{
		if(tokenBook.equals(rcd) && tokenBook.isRecycled())
		{
			buttonReload.setEnabled(false);
			buttonUpload.setEnabled(false);
			buttonDownload.setEnabled(false);
			buttonDelete.setEnabled(false);
			treeMedia.setEnabled(false);
			treeMediaScroll.setEnabled(false);
		}
	}
	
	@Override
	public void databaseConnected() {}
	
	@Override
	public void databaseDisconnected() {}
	
	@Override
	public void databaseCommit() {}
	
	@Override
	public void databaseRollback() {}
}