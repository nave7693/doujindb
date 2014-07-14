package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.*;
import org.dyndns.doujindb.ui.dialog.util.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings({"serial", "unused"})
public class PanelBookMedia extends JPanel
{
	private final Book tokenBook;
	
	private JButton buttonReload;
	private JLabel labelDiskUsage;
	private JButton buttonUpload;
	private JButton buttonDownload;
	private JButton buttonDelete;
	private JButton buttonPackage;
	private JButton buttonBrowse;
	private MediaTree treeMedia;
	private JScrollPane treeMediaScroll;
	
	private static final Color foreground = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.color");
	private static final Color background = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.background");
	
	public static final Icons Icon = UI.Icon;
	public static final Font Font = UI.Font;
	
	private static final String TAG = "PanelBook.Media : ";
	
	public PanelBookMedia(Book book)
	{
		tokenBook = book;
		treeMedia = new MediaTree(null);
		treeMediaScroll = new JScrollPane(treeMedia);
		add(treeMediaScroll);
		buttonReload = new JButton(Icon.desktop_explorer_book_media_reload);
		buttonReload.setFocusable(false);
		buttonReload.setToolTipText("Reload");
		buttonReload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				loadData();
			}			
		});
		add(buttonReload);
		labelDiskUsage = new JLabel();
		add(labelDiskUsage);
		buttonUpload = new JButton(Icon.desktop_explorer_book_media_upload);
		buttonUpload.setFocusable(false);
		buttonUpload.setToolTipText("Upload");
		buttonUpload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread("bookmedia-upload")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = UI.FileChooser;
							fc.setMultiSelectionEnabled(true);
							fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
							if(fc.showOpenDialog(PanelBookMedia.this) != JFileChooser.APPROVE_OPTION)
								return;
							File dataFiles[] = fc.getSelectedFiles();
							DataFile destFolder = DataStore.getFile(tokenBook.getID());
							UI.Desktop.showDialog(getTopLevelWindow(), new DialogUpload(destFolder, dataFiles));
						} catch (Exception e) {
							Logger.logError(TAG + e.getMessage(), e);
						}
					}
				}.start();
			}
		});
		add(buttonUpload);
		buttonDownload = new JButton(Icon.desktop_explorer_book_media_download);
		buttonDownload.setFocusable(false);
		buttonDownload.setToolTipText("Download");
		buttonDownload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(treeMedia.CheckBoxRenderer.getCheckedPaths().length < 1)
					return;
				new Thread("bookmedia-download")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = UI.FileChooser;
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(PanelBookMedia.this) != JFileChooser.APPROVE_OPTION)
								return;
							File destFolder = fc.getSelectedFile();
							Set<DataFile> dataFiles = new TreeSet<DataFile>();
							TreePath[] paths = treeMedia.CheckBoxRenderer.getCheckedPaths();
							DataFile rootDf = DataStore.getFile(tokenBook.getID());
							for(TreePath path : paths)
							try
							{
								StringBuilder sb = new StringBuilder();
								Object[] nodes = path.getPath();
								for(int i = 0;i < nodes.length;i++) {
									sb.append(nodes[i].toString());
								}
								DataFile dataFile = rootDf.getFile(sb.toString());
								dataFiles.add(dataFile);
							} catch (Exception e) {
								Logger.logError(TAG + e.getMessage(), e);
							}
							UI.Desktop.showDialog(getTopLevelWindow(), new DialogDownload(destFolder, dataFiles.toArray(new DataFile[]{})));
						} catch (Exception e) {
							Logger.logError(TAG + e.getMessage(), e);
						}
					}
				}.start();				
			}
		});
		add(buttonDownload);
		buttonDelete = new JButton(Icon.desktop_explorer_book_media_delete);
		buttonDelete.setFocusable(false);
		buttonDelete.setToolTipText("Delete");
		buttonDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				if(treeMedia.CheckBoxRenderer.getCheckedPaths().length < 1)
					return;
				try {
					UI.Desktop.showDialog(getTopLevelWindow(), new DialogDelete());
				} catch (PropertyVetoException pve) {
					Logger.logWarning(TAG + pve.getMessage(), pve);
				}
			}
		});
		add(buttonDelete);
		buttonPackage = new JButton(Icon.desktop_explorer_book_media_package);
		buttonPackage.setFocusable(false);
		buttonPackage.setToolTipText("Package");
		buttonPackage.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread("bookmedia-package")
				{
					@Override
					public void run()
					{
						try
						{
							JFileChooser fc = UI.FileChooser;
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(PanelBookMedia.this) != JFileChooser.APPROVE_OPTION)
								return;
							File destFolder = fc.getSelectedFile();
							UI.Desktop.showDialog(getTopLevelWindow(), new DialogArchive(destFolder));
						} catch (Exception e) {
							Logger.logError(TAG + e.getMessage(), e);
						}
					}
				}.start();
			}
		});
		add(buttonPackage);
		buttonBrowse = new JButton(Icon.desktop_explorer_book_media_browse);
		buttonBrowse.setFocusable(false);
		buttonBrowse.setToolTipText("Browse");
		buttonBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					DataStore.getFile(tokenBook.getID()).browse();
				} catch (DataBaseException dbe) {
					Logger.logError(TAG + "cannot browse Book '" + tokenBook + "'", dbe);
				} catch (DataStoreException dse) {
					Logger.logError(TAG + "cannot browse Book '" + tokenBook + "'", dse);
				}
			}
		});
		add(buttonBrowse);
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth(),
					height = parent.getHeight();
				buttonReload.setBounds(1,1,20,20);
				labelDiskUsage.setBounds(21,1,width-20*6,20);
				buttonUpload.setBounds(width-20,1,20,20);
				buttonDownload.setBounds(width-40,1,20,20);
				buttonDelete.setBounds(width-60,1,20,20);
				buttonPackage.setBounds(width-80,1,20,20);
				buttonBrowse.setBounds(width-100,1,20,20);
				treeMediaScroll.setBounds(1,21,width-2,height-25);
				
				buttonReload.setEnabled(!tokenBook.isRecycled());
				buttonUpload.setEnabled(!tokenBook.isRecycled());
				buttonDownload.setEnabled(!tokenBook.isRecycled());
				buttonDelete.setEnabled(!tokenBook.isRecycled());
				buttonPackage.setEnabled(!tokenBook.isRecycled());
				buttonBrowse.setEnabled(!tokenBook.isRecycled());
				treeMedia.setEnabled(!tokenBook.isRecycled());
				treeMediaScroll.setEnabled(!tokenBook.isRecycled());
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
		loadData();
	}
	
	private void loadData()
	{
		final MutableTreeNode nodeRoot = new DefaultMutableTreeNode("/");
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				filesWalk(DataStore.getFile(tokenBook.getID()), nodeRoot);
				return null;
			}
			@Override
			protected void done() {
				try {
					labelDiskUsage.setText(toSize(DataStore.diskUsage(DataStore.getFile(tokenBook.getID()))));
				} catch (Exception e) { }
				treeMedia.clearSelection();
				treeMedia.CheckBoxRenderer.clearSelection();
				((DefaultTreeModel) treeMedia.getModel()).setRoot(nodeRoot);
				PanelBookMedia.super.validate();
			}
		}.execute();
	}
	
	private WindowEx getTopLevelWindow()
	{
		return (WindowEx) SwingUtilities.getAncestorOfClass(WindowEx.class, this);
	}
	
	private static String toSize(long bytes)
	{
		int unit = 1024;
	    if (bytes < unit)
	    	return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = ("KMGTPE").charAt(exp-1) + ("i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	private final class MediaTree extends JTree
	{
		private MediaTreeRenderer renderer;
		public CheckBoxTreeCellRendererEx CheckBoxRenderer;

		public MediaTree(TreeNode root)
		{
			super(root);
			super.setFocusable(false);
			super.setFont(Font);
			super.setEditable(false);
			super.setRootVisible(true);
			super.setScrollsOnExpand(true);
			renderer = new MediaTreeRenderer();
			super.setCellRenderer(renderer);
			CheckBoxRenderer = new CheckBoxTreeCellRendererEx(this, super.getCellRenderer());
			super.setCellRenderer(CheckBoxRenderer);
		}

		private final class MediaTreeRenderer extends DefaultTreeCellRenderer
		{
			private Hashtable<String,ImageIcon> renderIcon;
	
			public MediaTreeRenderer()
			{
			    setBackgroundSelectionColor(MetalLookAndFeel.getWindowBackground());
			    renderIcon = new Hashtable<String,ImageIcon>();
			    renderIcon.put(".zip",  Icon.fileview_archive);
			    renderIcon.put(".rar",  Icon.fileview_archive);
    			renderIcon.put(".gz",   Icon.fileview_archive);
    			renderIcon.put(".tar",  Icon.fileview_archive);
    			renderIcon.put(".bz2",  Icon.fileview_archive);
    			renderIcon.put(".xz",   Icon.fileview_archive);
    			renderIcon.put(".cpio", Icon.fileview_archive);
    			renderIcon.put(".jpg",  Icon.fileview_image);
    			renderIcon.put(".jpeg", Icon.fileview_image);
    			renderIcon.put(".gif",  Icon.fileview_image);
    			renderIcon.put(".png",  Icon.fileview_image);
    			renderIcon.put(".tiff", Icon.fileview_image);
    			renderIcon.put(".txt",  Icon.fileview_text);
    			renderIcon.put(".sql",  Icon.fileview_text);
    			renderIcon.put(".db",   Icon.fileview_database);
    			renderIcon.put(".csv",  Icon.fileview_database);
			}
			private String getExtension(String file)
			{
				if(file.lastIndexOf(".") == -1)
					return "";
				return file.toLowerCase().substring(file.lastIndexOf("."));
			}
			public Component getTreeCellRendererComponent(JTree tree,
			    Object value,
			    boolean sel,
			    boolean expanded,
			    boolean leaf,
			    int row,
			    boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree,
			        value,
			        sel,
			        expanded,
			        leaf,
			        row,
			        hasFocus);
			    setIcon(Icon.desktop_explorer_book_media_types_unknown);
			    if(tree.getModel().getRoot().equals(value)) {
			    	setIcon(Icon.desktop_explorer_book_media_repository);
			    	try {
						try {
							setText(DataStore.getFile(tokenBook.getID()).toString());
						} catch (DataStoreException dse) {
							setText("???");
						}
					} catch (DataBaseException dbe) {
						Logger.logError(dbe.getMessage(), dbe);
					}
			    	return this;
			    }
			    if(value.toString().endsWith("/")) {
			    	setIcon(Icon.desktop_explorer_book_media_types_folder);
			    	super.setText(super.getText().substring(0, super.getText().length()-1));
			    	return this;
			    }
			    if(renderIcon.containsKey(getExtension(value.toString()))) {
			    	setIcon((ImageIcon)renderIcon.get(getExtension(value.toString())));
			    }
			    return this;
			}
		}
	}
	
	private static void filesWalk(DataFile file, MutableTreeNode parent) throws DataStoreException, DataBaseException
	{
		int index = 0; 
		for(DataFile df : file.listFiles())
		{
	        if(df.isDirectory())
	        {
		        DefaultMutableTreeNode sub = new DefaultMutableTreeNode(df.getName() + "/");
		        parent.insert(sub, index);
		        filesWalk(df, sub);
	        } else {
	        	if(df.getName().startsWith("."))
	        		continue;
		        DefaultMutableTreeNode sub = new DefaultMutableTreeNode(df.getName());
		        parent.insert(sub, index);
	        }
	        index ++;
	    }
	}
	
	private int filesCount(DataFile rootDf) throws DataStoreException, DataBaseException
	{
		if(!rootDf.isDirectory())
			return 1;
		int count = 0;
		for(DataFile df : rootDf.listFiles())
			if(df.isDirectory())
				count += filesCount(df);
			else
				count += 1;
		return count;
	}
	
	private int filesCount(File rootFile) throws DataStoreException, DataBaseException
	{
		if(!rootFile.isDirectory())
			return 1;
		int count = 0;
		for(File f : rootFile.listFiles())
			if(f.isDirectory())
				count += filesCount(f);
			else
				count += 1;
		return count;
	}
	
	private final class DialogDelete extends DialogEx
	{
		protected DialogDelete()
		{
			super(Icon.desktop_explorer_book_media_delete, "Delete");
		}

		@Override
		public JComponent createComponent()
		{
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1));
			JLabel lab = new JLabel("<html><body>Permanently delete selected files?</body></html>");
			lab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			lab.setFont(Font);
			panel.add(lab);
			JPanel bottom = new JPanel();
			bottom.setLayout(new GridLayout(1, 2));
			JButton canc = new JButton("Cancel");
			canc.setFont(Font);
			canc.setMnemonic('C');
			canc.setFocusable(false);
			canc.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					dispose();
				}					
			});
			final JButton ok = new JButton("Ok");
			ok.setFont(Font);
			ok.setMnemonic('O');
			ok.setFocusable(false);
			ok.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					ok.setEnabled(false);
					ok.setIcon(Icon.window_loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception
						{
							try
							{
								TreePath[] paths = treeMedia.CheckBoxRenderer.getCheckedPaths();
								DataFile root_ds = DataStore.getFile(tokenBook.getID());
								for(TreePath path : paths)
									try
									{
										Object os[] = path.getPath();
										DataFile ds = root_ds;
										for(int k=1;k<os.length;k++)
											if(os[k].toString().startsWith("/"))
												ds = ds.getFile(os[k].toString().substring(1));
											else
												ds = ds.getFile(os[k].toString());
										ds.delete(true);
									} catch (Exception e) {
										Logger.logError(TAG + e.getMessage(), e);
									}
							} catch (Exception e) {
								Logger.logError(e.getMessage(), e);
							}
							return null;
						}
						@Override
						protected void done()
						{
							loadData();
							dispose();
						}
					}.execute();
				}					
			});
			bottom.add(ok);
			bottom.add(canc);
			bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panel.add(bottom);
			return panel;
		}
	}
	
	private final class DialogUpload extends DialogEx
	{
		private final DataFile destFolder;
		private final File[] dataFiles;
		private SwingWorker<Void,File> swingWorker;
		private JProgressBar progressbar;
		
		protected DialogUpload(DataFile destFolder, File[] dataFiles)
		{
			super(Icon.desktop_explorer_book_media_upload, "Upload");
			
			this.destFolder = destFolder;
			this.dataFiles = dataFiles;
			swingWorker.execute();
		}

		@Override
		public JComponent createComponent()
		{
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1));
			progressbar = new JProgressBar(1, 1);
			progressbar.setStringPainted(false);
			progressbar.setFont(Font);
			progressbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panel.add(progressbar);
			JButton canc = new JButton("Cancel");
			canc.setFont(Font);
			canc.setMnemonic('C');
			canc.setFocusable(false);
			canc.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					swingWorker.cancel(true);
					loadData();
					dispose();
				}					
			});
			canc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panel.add(canc);
			
			swingWorker = new SwingWorker<Void,File>()
			{
				@Override
				protected Void doInBackground() throws Exception {
					int totalFiles = 0;
					for(File file : dataFiles)
						try {
							totalFiles += filesCount(file);
						} catch (Exception e) { }
					progressbar.setMaximum(totalFiles);
					progressbar.setMinimum(1);
					progressbar.setValue(1);
					for(File file : dataFiles)
					{
						try
						{
							doUpload(file, destFolder);
						} catch (Exception e) {
							Logger.logError(e.getMessage(), e);
						}
					}
					return null;
				}
				@Override
				protected void done() {
					loadData();
					dispose();
				}
			};
			
			return panel;
		}
		
		private void doUpload(File up, DataFile path) throws IOException, Exception
		{
			DataFile dst = path.getFile(up.getName());
			if(up.isDirectory())
			{
				dst.mkdirs();
				for(File file : up.listFiles())
					doUpload(file, dst);
			} else {
				dst.touch();
				OutputStream out = dst.getOutputStream();
				InputStream in = new FileInputStream(up);
				byte[] buff = new byte[0x800];
				int read;
				while((read = in.read(buff)) != -1)
				{
					out.write(buff, 0, read);
					if(swingWorker.isCancelled())
					{
						try { in.close(); } catch (Exception e) {}
						try { out.close(); } catch (Exception e) {}
						throw new Exception("Upload stopped by user input.");
					}
				}
				progressbar.setValue(progressbar.getValue() + 1);
				in.close();
				out.close();
			}
		}
	}
	
	private final class DialogDownload extends DialogEx
	{
		protected DialogDownload(File destFolder, DataFile[] dataFiles)
		{
			super(Icon.desktop_explorer_book_media_download, "Download");
		}

		@Override
		public JComponent createComponent()
		{
			
		}
	}
	
	private final class DialogArchive extends DialogEx
	{
		protected DialogArchive(File dstFolder)
		{
			super(Icon.desktop_explorer_book_media_package, "Archive");
		}

		@Override
		public JComponent createComponent()
		{
			
		}
	}
	
	/*
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
		
		private int count(DataFile ds_root) throws DataStoreException, DataBaseException
		{
			int count = 0;
			for(DataFile ds : ds_root.listFiles())
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
				for(DataFile ds : dl.listFiles())
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
				progress_file_max = dl.length();
				while((read = in.read(buff)) != -1)
				{
					out.write(buff, 0, read);		
					progress_file_current += read;
					if(stopped)
					{
						try { out.close(); } catch (Exception e) {}
						throw new Exception("Thread stopped by user input.");
					}
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
			cancel.setFont(font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DialogEx window = (DialogEx) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(font);
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
				UI.Desktop.showDialog(
						(JRootPane) getRootPane(),
						comp,
						Icon.desktop_explorer_book_media_download,
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
//					try
//					{
//						File dst = new File(dl_root, ds.getName());
//						if(ds.isDirectory())
//						{
//							dst.mkdirs();
//						}else
//						{
//							ds.getParent().mkdirs();
//							OutputStream out = new FileOutputStream(dst);
//							InputStream in = ds.getInputStream();
//							byte[] buff = new byte[0x800];
//							int read;
//							progress_bytes_current = 0;
//							progress_bytes_max = ds.size();
//							while((read = in.read(buff)) != -1)
//							{
//								out.write(buff, 0, read);		
//								progress_bytes_current += read;
//								if(stopped)
//									throw new Exception("Thread stopped by user input.");
//							}
//							in.close();
//							out.close();
//						}
//						progress_file_current++;
//					} catch (Exception e) {
//						progress_file_current++;
//						errors.add(ds.getPath());
//					}
				}
			} catch (PropertyVetoException pve) {
				Logger.logError(pve.getMessage(), pve);
			} catch (DataStoreException re) {
				Logger.logError(re.getMessage(), re);
			} catch (DataBaseException dbe) {
				Logger.logError(dbe.getMessage(), dbe);
			}
			clock.stop();
			DialogEx window = (DialogEx) comp.getRootPane().getParent();
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
				list.setFont(font);
				list.setSelectionBackground(list.getSelectionForeground());
				list.setSelectionForeground(background);
				panel.add(new JScrollPane(list), BorderLayout.CENTER);
				JButton ok = new JButton("Ok");
				ok.setFont(font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
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
					UI.Desktop.showDialog(
							(JRootPane) getRootPane(),
							panel,
							Icon.desktop_explorer_book_media_download,
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
			DataFile dst = path.getFile(up.getName());
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
					{
						try { in.close(); } catch (Exception e) {}
						try { out.close(); } catch (Exception e) {}
						throw new Exception("Thread stopped by user input.");
					}
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
			cancel.setFont(font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DialogEx window = (DialogEx) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(font);
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
				UI.Desktop.showDialog(
						(JRootPane) getRootPane(),
						comp,
						Icon.desktop_explorer_book_media_upload,
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
				Logger.logError(pve.getMessage(), pve);
			}
			clock.stop();
			DialogEx window = (DialogEx) comp.getRootPane().getParent();
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
				list.setFont(font);
				list.setSelectionBackground(list.getSelectionForeground());
				list.setSelectionForeground(background);
				panel.add(new JScrollPane(list), BorderLayout.CENTER);
				JButton ok = new JButton("Ok");
				ok.setFont(font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
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
					UI.Desktop.showDialog(
							(JRootPane) getRootPane(),
							panel,
							Icon.desktop_explorer_book_media_upload,
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
			cancel.setFont(font);
			cancel.setMnemonic('C');
			cancel.setFocusable(false);
			cancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					clock.stop();
					stopped = true;
					DialogEx window = (DialogEx) cancel.getRootPane().getParent();
					window.dispose();
				}					
			});
			progressbar_bytes = new JProgressBar(1,100);
			progressbar_bytes.setStringPainted(true);
			progressbar_bytes.setFont(font);
			progressbar_file = new JProgressBar(1,100);
			progressbar_file.setStringPainted(true);
			progressbar_file.setFont(font);
			progressbar_overall = new JProgressBar(1,100);
			progressbar_overall.setStringPainted(true);
			progressbar_overall.setFont(font);
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
				//FIXME
//				UI.Desktop.showDialog(
//						(RootPaneContainer) PanelBookMedia.this.getRootPane().getParent(),
//						comp,
//						Icon.desktop_explorer_book_media_package,
//						"Exporting ...");
				progress_overall_max = 1;
				{
					File zip = new File(destdir, book + "" + Configuration.configRead("org.dyndns.doujindb.dat.file_extension"));
					DataFile ds = DataStore.getFile(book.getID());
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
						metadata(book, zout);
						zout.closeEntry();
						;
						zip("", ds.listFiles(), zout);
						zout.close();
					} catch (IOException ioe) {
						zip.delete();
						Logger.logWarning(ioe.getMessage(), ioe);
					}
					progress_overall_current++;
				}
			} catch (Exception e) {
				Logger.logError(e.getMessage(), e);
			}
			clock.stop();
			DialogEx window = (DialogEx) comp.getRootPane().getParent();
			window.dispose();
		}
		
		private int count(DataFile ds_root) throws DataStoreException
		{
			int count = 0;
			for(DataFile ds : ds_root.listFiles())
				if(ds.isDirectory())
					count += count(ds);
				else
					count += 1;
			return count;
		}
		
		private void zip(String base, DataFile[] files, ZipOutputStream zout) throws IOException, Exception
		{
			for(DataFile ds : files)
			{
				if(ds.isDirectory())
				{
					ZipEntry entry = new ZipEntry(base + ds.getName() + "/");
					entry.setMethod(ZipEntry.DEFLATED);
					try {
						zout.putNextEntry(entry);
						zout.closeEntry();
					} catch (IOException ioe) {
						Logger.logWarning(ioe.getMessage(), ioe);
					}
					zip(base + ds.getName() + "/", ds.listFiles(), zout);
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
						progress_bytes_max = ds.length();
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
						Logger.logWarning(ioe.getMessage(), ioe);
					}
				}
			}
		}
		
		private void metadata(Book book, OutputStream dest) throws DataBaseException
		{
			XMLBook doujin = new XMLBook();
			doujin.japaneseName = book.getJapaneseName();
			doujin.translatedName = book.getTranslatedName();
			doujin.romajiName = book.getRomajiName();
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
				Logger.logWarning("Error parsing XML file : " + e.getMessage(), e);
			}
		}
		
		@XmlRootElement(name="Doujin")
		private static final class XMLBook
		{
			@XmlElement(required=true)
			private String japaneseName;
			@XmlElement(required=false)
			private String translatedName = "";
			@XmlElement(required=false)
			private String romajiName = "";
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
	
	*/
}