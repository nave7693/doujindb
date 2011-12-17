package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

@SuppressWarnings("serial")
public class PanelMediaManager implements Validable, LayoutManager, MouseListener
{
	private final String PACKAGE_INDEX = ".xml";
	private final String PACKAGE_MEDIA = ".data/";
	
	@SuppressWarnings("unused")
	private DouzWindow parentWindow;
	
	private JSplitPane split;
	private JLabel mediaManagerLabelInfo;
	private JLabel mediaManagerInfo;
	private JLabel mediaManagerLabelTasks;
	private JButton mediaManagerRefresh;
	private JButton mediaManagerImport;
	private JButton mediaManagerExport;
	private JButton mediaManagerDelete;
	private JLabel mediaManagerLabelPreview;
	private JLabel mediaManagerPreview;
	
	private JScrollPane scrollListMedia;
	private DouzCheckBoxList<Book> checkboxListMedia;
	
	public PanelMediaManager(DouzWindow parent, JComponent pane) throws DataBaseException
	{
		parentWindow = parent;
		pane.setLayout(this);
		JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		mediaManagerInfo = new JLabel("");
		mediaManagerInfo.setVerticalAlignment(JLabel.TOP);
		mediaManagerInfo.setFont(Core.Resources.Font);
		panel1.add(mediaManagerInfo);
		mediaManagerLabelInfo = new JLabel(" Media files");
		mediaManagerLabelInfo.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		mediaManagerLabelInfo.setOpaque(true);
		mediaManagerLabelInfo.setFont(Core.Resources.Font);
		panel1.add(mediaManagerLabelInfo);
		mediaManagerLabelTasks = new JLabel(" Tasks");
		mediaManagerLabelTasks.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		mediaManagerLabelTasks.setOpaque(true);
		mediaManagerLabelTasks.setFont(Core.Resources.Font);
		panel1.add(mediaManagerLabelTasks);
		mediaManagerRefresh = new JButton("Refresh", Core.Resources.Icons.get("JFrame/MediaManager/Refresh"));
		mediaManagerRefresh.setFocusable(false);
		mediaManagerRefresh.setFont(Core.Resources.Font);
		mediaManagerRefresh.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			}			
		});
		panel1.add(mediaManagerRefresh);
		mediaManagerExport = new JButton("Export", Core.Resources.Icons.get("JFrame/MediaManager/Export"));
		mediaManagerExport.setFocusable(false);
		mediaManagerExport.setFont(Core.Resources.Font);
		mediaManagerExport.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread(getClass().getName()+"/ActionPerformed/Export")
				{
					@Override
					public void run()
					{
						if(!checkboxListMedia.getSelectedItems().iterator().hasNext())
							return;
						JFileChooser fc = Core.UI.getFileChooser();
						fc.setMultiSelectionEnabled(false);
						int prev_option = fc.getFileSelectionMode();
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(fc.showSaveDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
						{
							fc.setFileSelectionMode(prev_option);
							return;
						}
						File dest = fc.getSelectedFile();
						fc.setFileSelectionMode(prev_option);
						Vector<Book> vector = new Vector<Book>();
						for(Book book : checkboxListMedia.getSelectedItems())
							vector.add(book);
						Thread packer = new Packer(vector, dest);
						packer.start();
					}
				}.start();
			}
		});
		panel1.add(mediaManagerExport);
		mediaManagerImport = new JButton("Import", Core.Resources.Icons.get("JFrame/MediaManager/Import"));
		mediaManagerImport.setFocusable(false);
		mediaManagerImport.setFont(Core.Resources.Font);
		mediaManagerImport.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread(getClass().getName()+"/ActionPerformed/Import")
				{
					@Override
					public void run()
					{
						JFileChooser fc = Core.UI.getFileChooser();
						fc.setMultiSelectionEnabled(true);
						int prev_option = fc.getFileSelectionMode();
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
						{
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(prev_option);
							return;
						}
						File[] files = fc.getSelectedFiles();
						Thread unpacker = new Unpacker(files);
						unpacker.start();
						try { while(unpacker.isAlive()) sleep(10); } catch (Exception e) { }
						validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
						fc.setMultiSelectionEnabled(false);
						fc.setFileSelectionMode(prev_option);
					}
				}.start();
			}
		});
		panel1.add(mediaManagerImport);
		mediaManagerDelete = new JButton("Delete", Core.Resources.Icons.get("JFrame/MediaManager/Delete"));
		mediaManagerDelete.setFocusable(false);
		mediaManagerDelete.setFont(Core.Resources.Font);
		mediaManagerDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Thread th = new Thread(getClass().getName()+"/ActionPerformed/Delete")
				{
					@Override
					public void run()
					{
						if(!checkboxListMedia.getSelectedItems().iterator().hasNext())
							return;
						{
							JPanel panel = new JPanel();
							panel.setSize(250, 150);
							panel.setLayout(new GridLayout(2, 1));
							JLabel lab = new JLabel("<html><body>Delete selected media files from the Repository?<br/><i>(This cannot be undone)</i></body></html>");
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
									for(Book key : checkboxListMedia.getSelectedItems())
									{
										try {
											Client.DS.child(key.getID()).delete();
										} catch (RepositoryException dse) {
											Core.Logger.log(dse.getMessage(), Level.ERROR);
											dse.printStackTrace();
										} catch (DataBaseException dbe) {
											Core.Logger.log(dbe.getMessage(), Level.ERROR);
											dbe.printStackTrace();
										}
									}
									;
									validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
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
										Core.Resources.Icons.get("JDesktop/Explorer/MediaManager"),
										"Media Manager");
							} catch (PropertyVetoException pve) { } 
						}
					}
				};
				th.setPriority(Thread.MIN_PRIORITY);
				th.start();
			}
		});
		panel1.add(mediaManagerDelete);
		mediaManagerLabelPreview = new JLabel(" Preview");
		mediaManagerLabelPreview.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		mediaManagerLabelPreview.setOpaque(true);
		mediaManagerLabelPreview.setFont(Core.Resources.Font);
		panel1.add(mediaManagerLabelPreview);
		mediaManagerPreview = new JLabel();
		mediaManagerPreview.setOpaque(true);
		mediaManagerPreview.setBackground(Color.RED); //TODO
		panel1.add(mediaManagerPreview);
		JTextField searchField = new JTextField(".*");
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) {
		    	checkboxListMedia.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	checkboxListMedia.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	checkboxListMedia.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		});
		Vector<Book> files = new Vector<Book>();
		for(Book book : Client.DB.getBooks(null))
			files.add(book);
		checkboxListMedia = new DouzCheckBoxList<Book>(files, searchField);
		scrollListMedia = new JScrollPane(checkboxListMedia);
		JSplitPane splitList = new JSplitPane();
		splitList.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitList.setTopComponent(searchField);
		splitList.setBottomComponent(scrollListMedia);
		splitList.setDividerSize(0);
		splitList.setEnabled(false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, splitList);
		split.setDividerSize(1);
		split.setEnabled(false);
		pane.add(split);
		;
		validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		mediaManagerLabelInfo.setBounds(0,0,130,20);
		mediaManagerInfo.setBounds(2,22,125,55);
		mediaManagerLabelTasks.setBounds(0,75+5,130,20);
		mediaManagerRefresh.setBounds(3,75+25+1,125,20);
		mediaManagerImport.setBounds(3,75+45+1,125,20);
		mediaManagerExport.setBounds(3,75+65+1,125,20);
		mediaManagerDelete.setBounds(3,75+85+1,125,20);
		mediaManagerLabelPreview.setBounds(0,75+125+1,130,20);
		mediaManagerPreview.setBounds(3,75+130+1,125,120);
		//TODO
		mediaManagerPreview.setVisible(false);
		mediaManagerLabelPreview.setVisible(false);
		mediaManagerDelete.setEnabled(false);
		mediaManagerDelete.setVisible(false);
		split.setBounds(0, 0, width,  height);
	}
	@Override
	public void addLayoutComponent(String key,Component c){}
	@Override
	public void removeLayoutComponent(Component c){}
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
	     return parent.getMinimumSize();
	}
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
	     return parent.getPreferredSize();
	}

	@Override
	public void validateUI(DouzEvent ve)
	{
		/**
		Can take some time to complete with big datastores: better make it asynchronous.
		*/
		new Thread(getClass().getName()+"/validateUI")
		{
			@Override
			public void run()
			{
				super.setPriority(Thread.MIN_PRIORITY);
				double size = -1L;
				size = Client.DS.size();
				String label = "Byte";
				if(size >= 1024)
				{
					size = size / 1024;
					label = "KB";
				}
				if(size >= 1024)
				{
					size = size / 1024;
					label = "MB";
				}
				if(size >= 1024)
				{
					size = size / 1024;
					label = "GB";
				}
				if(size >= 1024)
				{
					size = size / 1024;
					label = "TB";
				}
				mediaManagerInfo.setText(String.format("%.2f ", size) + label);
				Vector<Book> files = new Vector<Book>();
				try {
					for(Book book : Client.DB.getBooks(null))
						files.add(book);
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				}
				Iterable<Book> iterable = checkboxListMedia.getSelectedItems();
				checkboxListMedia.setItems(files);
				checkboxListMedia.setSelectedItems(iterable);
			}
		}.start();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes","unused"})
	private final class DouzCheckBoxList<T> extends JPanel implements Validable, LayoutManager
	{
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JScrollPane scrollPane;
		private JList listData;
		private JTextField filterField;
		private Model model;
		private Hashtable<Class,ImageIcon> iconData;
		
		public DouzCheckBoxList(Iterable<T> data, JTextField filter)
		{
			this(data, filter, null);
		}
		public DouzCheckBoxList(Iterable<T> data, JTextField filter, Hashtable<Class,ImageIcon> icons)
		{
			super();
			super.setLayout(this);
			filterField = filter;
			iconData = icons;
			CheckBoxItem<T>[] checkboxItems = buildCheckBoxItems(data);
			listData = new JList();
			listData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listData.setFont(font);
			listData.setCellRenderer(new Renderer());
			listData.addMouseListener(new MouseAdapter()
	   		{
				public void mouseClicked(MouseEvent me)
	   			{
	   				int selectedIndex = listData.locationToIndex(me.getPoint());
	   				if (selectedIndex < 0)
						return;
	   				CheckBoxItem<T> item = (CheckBoxItem<T>)listData.getModel().getElementAt(selectedIndex);
	   				item.setChecked(!item.isChecked());
	   				listData.setSelectedIndex(selectedIndex);
	   				listData.repaint();
	   			}
	   		});
			model = new Model(checkboxItems);
			listData.setModel(model);
			listData.addMouseListener(new MouseAdapter()
			{
	      		public void mouseClicked(MouseEvent me)
	      		{
					if (me.getClickCount() != 2)
						return;
		  			int selectedIndex = listData.locationToIndex(me.getPoint());
		  			if (selectedIndex < 0)
						return;
	        		/*CheckBoxItem item = (CheckBoxItem)listCheckBox.getModel().getElementAt(selectedIndex);
	        		item.setChecked(!item.isChecked());
		  			listCheckBox.repaint();*/
	      		}
			});
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(listData);
			listData.setFixedCellHeight(16);
			add(scrollPane);
	   		setVisible(true);
		}
		
		@Override
		public void addMouseListener(MouseListener listener)
		{
			listData.addMouseListener(listener);
		}
		
		private CheckBoxItem<T>[] buildCheckBoxItems(Iterable<T> items)
		{
			Vector<CheckBoxItem<T>> tmp = new Vector<CheckBoxItem<T>>();
			for(T item : items)
			{
				tmp.add(new CheckBoxItem<T>(item));
			}
			CheckBoxItem<T>[] checkboxItems = tmp.toArray(new CheckBoxItem[0]);
			return checkboxItems;
	  	}
		private class CheckBoxItem<K>
		{
			private boolean isChecked;
			private K item;
			public CheckBoxItem(K o)
			{
				item = o;
				isChecked = false;
			}
			public boolean isChecked()
			{
				return isChecked;
			}
			public void setChecked(boolean value)
			{
				isChecked = value;
			}
			public K getItem()
			{
				return item;
			}
		}
		private class Model extends AbstractListModel implements Validable
		{
			ArrayList<CheckBoxItem<T>> items;
			ArrayList<CheckBoxItem<T>> filterItems;
			
			public Model(CheckBoxItem<T> data[])
			{
				items = new ArrayList<CheckBoxItem<T>>();
				for(CheckBoxItem<T> o : data)
					items.add(o);
				filterItems = new ArrayList<CheckBoxItem<T>>();
				for(CheckBoxItem<T> o : data)
					filterItems.add(o);
			}
			public Object getElementAt (int index)
			{
				if (index < filterItems.size())
					return filterItems.get (index);
				else
					return null;
			}
			public int getSize()
			{
				return filterItems.size();
			}
			private void refreshUI()
			{
				try
				{
					filterItems.clear();
					String term = filterField.getText();
					if(term.equals(""))
						for (int i=0; i<items.size(); i++)
							if (items.get(i).isChecked())
								filterItems.add(items.get(i));
							else
								;
					else
						if(term.equals("!"))
							for (int i=0; i<items.size(); i++)
								if (!items.get(i).isChecked())
									filterItems.add(items.get(i));
								else
									;
						else
							for (int i=0; i<items.size(); i++)
								if (items.get(i).getItem().toString().matches(term))
									filterItems.add(items.get(i));
				}catch(PatternSyntaxException pse){}
				fireContentsChanged(this, 0, getSize());
			}
			@Override
			public void validateUI(DouzEvent ve)
			{
				switch(ve.getType())
				{
				case DouzEvent.DATABASE_REFRESH:
					refreshUI();
					break;
				case DouzEvent.DATABASE_ITEMCHANGED:
					refreshUI();
					break;
				case DouzEvent.DATABASE_ITEMADDED:
					model.items.add(new CheckBoxItem(ve.getParameter()));
					refreshUI();
					break;
				case DouzEvent.DATABASE_ITEMREMOVED:
					CheckBoxItem<?> removed = null;
					for(CheckBoxItem<?> cbi : model.items)
						if(cbi.getItem() == ve.getParameter())
						{
							removed = cbi;
							break;
						}
					if(removed != null)
						model.items.remove(removed);
					refreshUI();
					break;
				default:
					;
				}
			}
		}
		
		private class Renderer extends JCheckBox implements ListCellRenderer
		{ 
			public Renderer()
			{
				setBackground(UIManager.getColor("List.textBackground"));
				setForeground(UIManager.getColor("List.textForeground"));
			}		
			public Component getListCellRendererComponent(JList listBox, Object obj, int currentindex, boolean isChecked, boolean hasFocus)
			{
				setSelected(((CheckBoxItem<T>)obj).isChecked());
				setText(((CheckBoxItem<T>)obj).getItem().toString());
				setFont(font);
				return this;
			}
		}
		
		@Override
		public void addLayoutComponent(String name, Component c) {}

		@Override
		public void layoutContainer(Container c)
		{
			scrollPane.setBounds(0,0,c.getWidth(), c.getHeight());
		}

		@Override
		public Dimension minimumLayoutSize(Container c) {
			return new Dimension(250, 150);
		}

		@Override
		public Dimension preferredLayoutSize(Container c) {
			return new Dimension(250, 250);
		}

		@Override
		public void removeLayoutComponent(Component c) {}
		
		@Override
		public void validateUI(DouzEvent ve)
		{
			model.validateUI(ve);
			listData.validate();
		}
		
		public void setSelectedItems(Iterable<T> items)
		{
			for(CheckBoxItem<T> cb : model.items)
				cb.setChecked(false);
			for(T item : items)
				for(CheckBoxItem<T> cb : model.items)
				{
					if(cb.getItem() == item)
						cb.setChecked(true);
				}
			validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		}
		
		public Iterable<T> getSelectedItems()
		{
			Vector<T> v = new Vector<T>();
			for(CheckBoxItem<T> cb : model.filterItems)
			{
				if(cb.isChecked())
					v.add((T)cb.getItem());
			}
			return v;
		}
		
		public int getSelectedItemCount()
		{
			int count = 0;
			for(CheckBoxItem<T> cb : model.filterItems)
			{
				if(cb.isChecked())
					count++;
			}
			return count;
		}
		
		public void setItems(Iterable<T> items)
		{
			model.items.clear();
			for(T item : items)
				model.items.add(new CheckBoxItem<T>(item));
			validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		}
		
		public Iterable<T> getItems()
		{
			Vector<T> v = new Vector<T>();
			for(CheckBoxItem<T> cb : model.items)
			{
				v.add((T)cb.getItem());
			}
			return v;
		}
		
		public int getItemCount()
		{
			return model.items.size();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent me) {}
	@Override
	public void mouseEntered(MouseEvent me) {}
	@Override
	public void mouseExited(MouseEvent me) {}
	@Override
	public void mousePressed(MouseEvent me) {}
	@Override
	public void mouseReleased(MouseEvent me) {}

	private final class Packer extends Thread
	{
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
		private Vector<Book> books;
		private File dest;
		
		public Packer(Vector<Book> books, File dest)
		{
			this.books = books;
			this.dest = dest;
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
						Core.Resources.Icons.get("JFrame/MediaManager/Export"),
						"Exporting ...");
				progress_overall_max = books.size();
				for(Book book : books)
				{
					File zip = new File(dest, book + Core.Properties.get("org.dyndns.doujindb.dat.file_extension").asString());
					DataFile ds = Client.DS.child(book.getID());
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
						writeXML(book, zout);
						zout.closeEntry();
						;
						entry = new ZipEntry(PACKAGE_MEDIA);
						entry.setMethod(ZipEntry.DEFLATED);
						try
						{
							zout.putNextEntry(entry);
							zout.closeEntry();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							Core.Logger.log(ioe.getMessage(), Level.WARNING);
						}
						;
						zip(PACKAGE_MEDIA, ds.children(), zout);
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
	}
	
	private final class Unpacker extends Thread
	{
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
		private File[] files;
		
		public Unpacker(File[] files)
		{
			this.files = files;
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
			Vector<String> errors = new Vector<String>();
			try
			{
				Core.UI.Desktop.showDialog(
						comp,
						Core.Resources.Icons.get("JFrame/MediaManager/Import"),
						"Importing ...");
				progress_overall_max = files.length;
				for(File file : files)
				{
					try
					{
						ZipFile zip = new ZipFile(file);
						Enumeration<? extends ZipEntry> entries = zip.entries();
						boolean valid = false;
						while(entries.hasMoreElements())
						{
							ZipEntry entry = entries.nextElement();
							if(entry.getName().equals(PACKAGE_INDEX))
							{
								valid = true;
								DataFile ds = Client.DS.child(parseXML(zip.getInputStream(entry)));
								ds.mkdirs();
								;
								entries = zip.entries();
								progress_file_max = zip.size();
								progress_file_current++;
								while (entries.hasMoreElements())
								{
									try
									{
										entry = entries.nextElement();
										if(entry.getName().equals(PACKAGE_INDEX))
										{
											progress_file_current++;
											continue;
										}
										if(!entry.getName().startsWith(PACKAGE_MEDIA))
										{
											progress_file_current++;
											continue;
										}
										DataFile ds0 = ds.child(entry.getName().substring(PACKAGE_MEDIA.length()));
										if(entry.isDirectory())
										{
											ds0.mkdirs();
										}else
										{
											OutputStream out = ds0.getOutputStream();
											InputStream in = zip.getInputStream(entry);
											byte[] buff = new byte[0x800];
											int read;
											progress_bytes_current = 0;
											progress_bytes_max = entry.getSize();
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
									}
								}
								valid = true;
								break;
							}
						}
						if(!valid)
							errors.add(file.getName());
						progress_overall_current++;
					} catch (IOException ioe) {
						errors.add(file.getName());
					} catch (RepositoryException dse) {
						errors.add(file.getName());
					} catch (DataBaseException dbe) {
						errors.add(file.getName());
					}
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
						"The following entries were not imported.<br>" +
						"Make sure the provided files are valid archives.<br>" +
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
							Core.Resources.Icons.get("JDesktop/Explorer/Book/Media/Unpack"),
							"Unpacking - Error");
				} catch (PropertyVetoException pve) { } 
			}
		}
		
		private String parseXML(InputStream in) throws DataBaseException
		{
			Hashtable<String, Set<Record>> imported = readXML(in);
			if(imported == null)
				return null;
			Book book = (Book) imported.get("Book://").toArray()[0];
			progressbar_overall.setString(book.toString());
			for(Record r : imported.get("Artist://"))
			{
				Artist o = (Artist)r;
				book.addArtist(o);
				o.addBook(book);
			}
			for(Record r : imported.get("Convention://"))
			{
				Convention o = (Convention)r;
				book.setConvention(o);
				o.addBook(book);
			}
			for(Record r : imported.get("Content://"))
			{
				Content o = (Content)r;
				book.addContent(o);
				o.addBook(book);
			}
			for(Record r : imported.get("Parody://"))
			{
				Parody o = (Parody)r;
				book.addParody(o);
				o.addBook(book);
			}
			return book.getID();
		}
	}
	
	private Hashtable<String, Set<Record>> readXML(InputStream src) throws DataBaseException
	{
		XMLBook doujin;
		Hashtable<String, Set<Record>> parsed = new Hashtable<String, Set<Record>>();
		parsed.put("Artist://", new HashSet<Record>());
		parsed.put("Book://", new HashSet<Record>());
		parsed.put("Circle://", new HashSet<Record>());
		parsed.put("Convention://", new HashSet<Record>());
		parsed.put("Content://", new HashSet<Record>());
		parsed.put("Parody://", new HashSet<Record>());
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLBook.class);
			Unmarshaller um = context.createUnmarshaller();
			doujin = (XMLBook) um.unmarshal(src);
		} catch (Exception e) {
			Core.Logger.log("Error parsing XML file (" + e.getMessage() + ").", Level.WARNING);
			return null;
		}
		Book book = Client.DB.childContext(getUUID()).doInsert(Book.class);
		book.setJapaneseName(doujin.japaneseName);
		book.setType(doujin.Type);
		book.setTranslatedName(doujin.translatedName);
		book.setRomanjiName(doujin.romanjiName);
		book.setDate(doujin.Released);
		book.setType(doujin.Type);
		book.setPages(doujin.Pages);
		book.setAdult(doujin.Adult);
		book.setDecensored(doujin.Decensored);
		book.setTranslated(doujin.Translated);
		book.setColored(doujin.Colored);
		book.setRating(doujin.Rating);
		book.setInfo(doujin.Info);
		parsed.get("Book://").add(book);
		{
			Vector<Record> temp = new Vector<Record>();
			for(Convention convention : Client.DB.childContext(getUUID()).getConventions(null))
				if(doujin.Convention.matches(convention.getTagName()))
					temp.add(convention);
			if(temp.size() == 0 && !doujin.Convention.equals(""))
			{
				Convention convention = Client.DB.childContext(getUUID()).doInsert(Convention.class);
				convention.setTagName(doujin.Convention);
				parsed.get("Convention://").add(convention);
			}			
			else
				parsed.get("Convention://").addAll(temp);
		}
		{
			for(String japaneseName : doujin.artists)
			{
				Vector<Record> temp = new Vector<Record>();
				for(Artist artist : Client.DB.childContext(getUUID()).getArtists(null))
					if(japaneseName.matches(artist.getJapaneseName()))
						temp.add(artist);
				if(temp.size() == 0)
				{
					Artist artist = Client.DB.childContext(getUUID()).doInsert(Artist.class);
					artist.setJapaneseName(japaneseName);
					parsed.get("Artist://").add(artist);
				}			
				else
					parsed.get("Artist://").addAll(temp);
			}
		}
		{
			for(String japaneseName : doujin.circles)
			{
				Vector<Record> temp = new Vector<Record>();
				for(Circle circle : Client.DB.childContext(getUUID()).getCircles(null))
					if(japaneseName.matches(circle.getJapaneseName()))
						temp.add(circle);
				if(temp.size() == 0)
				{
					Circle circle = Client.DB.childContext(getUUID()).doInsert(Circle.class);
					circle.setJapaneseName(japaneseName);
					parsed.get("Circle://").add(circle);
				}			
				else
					parsed.get("Circle://").addAll(temp);
			}
		}
		{
			for(String tagName : doujin.contents)
			{
				Vector<Record> temp = new Vector<Record>();
				for(Content content : Client.DB.childContext(getUUID()).getContents(null))
					if(tagName.matches(content.getTagName()))
						temp.add(content);
				if(temp.size() == 0)
				{
					Content content = Client.DB.childContext(getUUID()).doInsert(Content.class);
					content.setTagName(tagName);
					parsed.get("Content://").add(content);
				}			
				else
					parsed.get("Content://").addAll(temp);
			}
		}
		{
			for(String japaneseName : doujin.parodies)
			{
				Vector<Record> temp = new Vector<Record>();
				for(Parody parody : Client.DB.childContext(getUUID()).getParodies(null))
					if(japaneseName.matches(parody.getJapaneseName()))
						temp.add(parody);
				if(temp.size() == 0)
				{
					Parody parody = Client.DB.childContext(getUUID()).doInsert(Parody.class);
					parody.setJapaneseName(japaneseName);
					parsed.get("Parody://").add(parody);
				}			
				else
					parsed.get("Parody://").addAll(temp);
			}
		}
		Client.DB.childContext(getUUID()).doCommit();
		return parsed;
	}
	
	private void writeXML(Book book, OutputStream dest) throws DataBaseException
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
			m.marshal(doujin, dest);
		} catch (Exception e) {
			Core.Logger.log("Error parsing XML file (" + e.getMessage() + ").", Level.WARNING);
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
	
	public static String getUUID()
	{
		return "{7c483dce-d1ee-4484-840d-37152ef5c4e2}";
	}
}