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
import org.dyndns.doujindb.util.Metadata;

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
							DataFile destFolder = DataStore.getStore(tokenBook.getId());
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
							DataFile rootDf = DataStore.getStore(tokenBook.getId());
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
					DataStore.getStore(tokenBook.getId()).browse();
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
				filesWalk(DataStore.getStore(tokenBook.getId()), nodeRoot);
				return null;
			}
			@Override
			protected void done() {
				try {
					labelDiskUsage.setText(toSize(DataStore.diskUsage(DataStore.getStore(tokenBook.getId()))));
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
							setText(DataStore.getStore(tokenBook.getId()).toString());
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
								DataFile root_ds = DataStore.getStore(tokenBook.getId());
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
				OutputStream out = dst.openOutputStream();
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
		private final File destFolder;
		private final DataFile[] dataFiles;
		private SwingWorker<Void,File> swingWorker;
		private JProgressBar progressbar;
		
		protected DialogDownload(File destFolder, DataFile[] dataFiles)
		{
			super(Icon.desktop_explorer_book_media_download, "Download");

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
					for(DataFile file : dataFiles)
						try {
							totalFiles += filesCount(file);
						} catch (Exception e) { }
					progressbar.setMaximum(totalFiles);
					progressbar.setMinimum(1);
					progressbar.setValue(1);
					for(DataFile file : dataFiles)
					{
						try
						{
							doDownload(file, destFolder);
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
		
		private void doDownload(DataFile dw, File path) throws IOException, Exception
		{
			File dst = new File(path, dw.getName());
			if(dw.isDirectory())
			{
				dst.mkdirs();
				for(DataFile file : dw.listFiles())
					doDownload(file, dst);
			} else {
				OutputStream out = new FileOutputStream(dst);
				InputStream in = dw.openInputStream();
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
	
	private final class DialogArchive extends DialogEx
	{
		private final File destFolder;
		private SwingWorker<Void,File> swingWorker;
		private JProgressBar progressbar;
		
		protected DialogArchive(File destFolder)
		{
			super(Icon.desktop_explorer_book_media_package, "Archive");
			
			this.destFolder = destFolder;
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
					DataFile srcDataFile = DataStore.getStore(tokenBook.getId());
					for(DataFile file : srcDataFile.listFiles())
						try {
							totalFiles += filesCount(file);
						} catch (Exception e) { }
					progressbar.setMaximum(totalFiles);
					progressbar.setMinimum(1);
					progressbar.setValue(1);
					File zip = new File(destFolder, tokenBook.toString() + "" + Configuration.configRead("org.dyndns.doujindb.dat.file_extension"));
					try
					{
						ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zip));
						zout.setLevel(9);
						// add metadata to zip files
						ZipEntry entry = new ZipEntry(".xml");
						zout.putNextEntry(entry);
						Metadata.toXML(tokenBook, zout);
						zout.closeEntry();
						// add actual files
						doZip("", srcDataFile.listFiles(), zout);
						zout.close();
					} catch (IOException ioe) {
						zip.delete();
						Logger.logWarning(ioe.getMessage(), ioe);
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
		
		private void doZip(String base, DataFile[] files, ZipOutputStream zout) throws IOException, Exception
		{
			for(DataFile ds : files)
			{
				if(ds.isDirectory())
				{
					ZipEntry entry = new ZipEntry(base + ds.getName() + File.separator);
					entry.setMethod(ZipEntry.DEFLATED);
					try {
						zout.putNextEntry(entry);
						zout.closeEntry();
					} catch (IOException ioe) {
						Logger.logWarning(ioe.getMessage(), ioe);
					}
					doZip(base + ds.getName() + File.separator, ds.listFiles(), zout);
				}else
				{
					int read;
					byte[] buff = new byte[0x800];
					ZipEntry entry = new ZipEntry(base + ds.getName());
					entry.setMethod(ZipEntry.DEFLATED);
					try
					{
						InputStream in = ds.openInputStream();
						zout.putNextEntry(entry);
						while ((read = in.read(buff)) != -1)
						{
							zout.write(buff, 0, read);
							if(swingWorker.isCancelled())
								throw new Exception("Archive stopped by user input.");
						}
						zout.closeEntry();
						in.close();
						progressbar.setValue(progressbar.getValue() + 1);
					} catch (IOException ioe) {
						Logger.logWarning(ioe.getMessage(), ioe);
					}
				}
			}
		}
	}
}
