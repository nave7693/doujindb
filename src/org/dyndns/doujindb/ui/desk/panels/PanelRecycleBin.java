package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

public final class PanelRecycleBin implements Validable, LayoutManager, MouseListener
{
	@SuppressWarnings("unused")
	private DouzWindow parentWindow;
	
	private JSplitPane split;
	private JLabel recycleLabelInfo;
	private JLabel recycleInfo;
	private JLabel recycleLabelTasks;
	private JButton recycleRestore;
	private JButton recycleDelete;
	private JButton recycleEmpty;
	
	private KFramedPanel panelFramed[] = new KFramedPanel[6];
	private JScrollPane scrollPanelBase;
	private JPanel panelBase;
	private DouzCheckBoxList<Artist> checkboxListArtist;
	private JLabel labelListArtist;
	private DouzCheckBoxList<Circle> checkboxListCircle;
	private JLabel labelListCircle;
	private DouzCheckBoxList<Book> checkboxListBook;
	private JLabel labelListBook;
	private DouzCheckBoxList<Convention> checkboxListConvention;
	private JLabel labelListConvention;
	private DouzCheckBoxList<Content> checkboxListContent;
	private JLabel labelListContent;
	private DouzCheckBoxList<Parody> checkboxListParody;
	private JLabel labelListParody;
	
	public PanelRecycleBin(DouzWindow parent, JComponent pane)
	{
		parentWindow = parent;
		pane.setLayout(this);
		JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		recycleInfo = new JLabel("Items : 0");
		recycleInfo.setVerticalAlignment(JLabel.TOP);
		recycleInfo.setFont(Core.Resources.Font);
		panel1.add(recycleInfo);
		recycleLabelInfo = new JLabel(" Info");
		recycleLabelInfo.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		recycleLabelInfo.setOpaque(true);
		recycleLabelInfo.setFont(Core.Resources.Font);
		panel1.add(recycleLabelInfo);
		recycleLabelTasks = new JLabel(" Tasks");
		recycleLabelTasks.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		recycleLabelTasks.setOpaque(true);
		recycleLabelTasks.setFont(Core.Resources.Font);
		panel1.add(recycleLabelTasks);
		recycleRestore = new JButton("Restore", Core.Resources.Icons.get("JFrame/RecycleBin/Restore"));
		recycleRestore.setFocusable(false);
		recycleRestore.setFont(Core.Resources.Font);
		recycleRestore.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Thread th = new Thread(getClass().getName()+"/ActionPerformed/Restore")
				{
					@Override
					public void run()
					{
						try
						{
							for(Artist value : checkboxListArtist.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							for(Book value : checkboxListBook.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							for(Circle value : checkboxListCircle.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							for(Convention value : checkboxListConvention.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							for(Content value : checkboxListContent.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							for(Parody value : checkboxListParody.getSelectedItems())
							{
								Client.DB.getDeleted().delete(value);
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, value));
							}
							Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
						}catch(ArrayIndexOutOfBoundsException aioobe)
						{
							aioobe.printStackTrace();
							Core.Logger.log(aioobe.getMessage(), Level.WARNING);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (RemoteException re) {
							Core.Logger.log(re.getMessage(), Level.ERROR);
							re.printStackTrace();
						}
					}
				};
				th.setPriority(Thread.MIN_PRIORITY);
				th.start();
			}			
		});
		panel1.add(recycleRestore);
		recycleDelete = new JButton("Delete", Core.Resources.Icons.get("JFrame/RecycleBin/Delete"));
		recycleDelete.setFocusable(false);
		recycleDelete.setFont(Core.Resources.Font);
		recycleDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Thread th = new Thread(getClass().getName()+"/ActionPerformed/Delete")
				{
					@Override
					public void run()
					{
						if(!checkboxListArtist.getSelectedItems().iterator().hasNext() &&
								!checkboxListBook.getSelectedItems().iterator().hasNext() &&
								!checkboxListCircle.getSelectedItems().iterator().hasNext() &&
								!checkboxListConvention.getSelectedItems().iterator().hasNext() &&
								!checkboxListContent.getSelectedItems().iterator().hasNext() &&
								!checkboxListParody.getSelectedItems().iterator().hasNext())
							return;
						{
							JPanel panel = new JPanel();
							panel.setSize(250, 150);
							panel.setLayout(new GridLayout(2, 1));
							JLabel lab = new JLabel("<html><body>Delete selected entries from the Recycle Bin?<br/><i>(This cannot be undone)</i></body></html>");
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
									recycleDelete.setIcon(Core.Resources.Icons.get("JFrame/Loading"));
									recycleRestore.setEnabled(false);
									recycleDelete.setEnabled(false);
									recycleEmpty.setEnabled(false);
									;
									try {
									;
									for(Artist value : checkboxListArtist.getSelectedItems())
									{
										Artist o = (Artist)value;
										for(Book b : o.getBooks().elements())
										{
											b.getArtists().remove(o);
											//o.getBooks().remove(b);
										}
										for(Circle c : o.getCircles().elements())
										{
											c.getArtists().remove(o);
											//o.getCircles().remove(c);
										}
										Client.DB.getArtists().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Book value : checkboxListBook.getSelectedItems())
									{
										Book o = (Book)value;
										for(Circle c : o.getCircles().elements())
										{
											c.getBooks().remove(o);
											//o.getCircles().remove(c);
										}
										for(Artist a : o.getArtists().elements())
										{
											a.getBooks().remove(o);
											//o.getArtists().remove(a);
										}
										for(Content c : o.getContents().elements())
										{
											c.getBooks().remove(o);
											//o.getContents().remove(c);
										}
										for(Parody p : o.getParodies().elements())
										{
											p.getBooks().remove(o);
											//o.getParodies().remove(p);
										}
										Convention c = o.getConvention();
										try { c.getBooks().remove(o); } catch (NullPointerException npe) { ; }
										Client.DB.getBooks().delete(o);
										Client.DB.getDeleted().delete(value);
										try { Client.DS.child(o.getID()).delete(); } catch (DataStoreException dse) { ; }
									}
									for(Circle value : checkboxListCircle.getSelectedItems())
									{
										Circle o = (Circle)value;
										for(Book b : o.getBooks().elements())
										{
											b.getCircles().remove(o);
											//o.getBooks().remove(b);
										}
										for(Artist a : o.getArtists().elements())
										{
											a.getCircles().remove(o);
											//o.getArtists().remove(a);
										}
										Client.DB.getCircles().delete(o);
										Client.DB.getDeleted().delete(value);
										try { Client.DS.child(o.getID()).delete(); } catch (DataStoreException dse) { ; }
									}
									for(Convention value : checkboxListConvention.getSelectedItems())
									{
										Convention o = (Convention)value;
										for(Book b : o.getBooks().elements())
										{
											b.setConvention(null);
											//o.getBooks().remove(b);
										}
										Client.DB.getConventions().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Content value : checkboxListContent.getSelectedItems())
									{
										Content o = (Content)value;
										for(Book b : o.getBooks().elements())
										{
											b.getContents().remove(o);
											//o.getBooks().remove(b);
										}
										Client.DB.getContents().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Parody value : checkboxListParody.getSelectedItems())
									{
										Parody o = (Parody)value;
										for(Book b : o.getBooks().elements())
										{
											b.getParodies().remove(o);
											//o.getBooks().remove(b);
										}
										Client.DB.getParodies().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									System.gc();
									;
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									} catch (RemoteException re) {
										Core.Logger.log(re.getMessage(), Level.ERROR);
										re.printStackTrace();
									}
									;
									recycleRestore.setEnabled(true);
									recycleDelete.setEnabled(true);
									recycleEmpty.setEnabled(true);
									recycleDelete.setIcon(Core.Resources.Icons.get("JFrame/RecycleBin/Delete"));
									;
									Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
									;
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
										Core.Resources.Icons.get("JDesktop/Explorer/RecycleBin"),
										"Recycle Bin");
							} catch (PropertyVetoException pve)
							{
								Core.Logger.log(pve.getMessage(), Level.WARNING);
							} 
						}
					}
				};
				th.setPriority(Thread.MIN_PRIORITY);
				th.start();
			}			
		});
		panel1.add(recycleDelete);
		recycleEmpty = new JButton("Empty", Core.Resources.Icons.get("JFrame/RecycleBin/Empty"));
		recycleEmpty.setFocusable(false);
		recycleEmpty.setFont(Core.Resources.Font);
		recycleEmpty.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Thread th = new Thread(getClass().getName()+"/ActionPerformed/Empty")
				{
					@Override
					public void run()
					{
						if(!checkboxListArtist.getItems().iterator().hasNext() &&
								!checkboxListBook.getItems().iterator().hasNext() &&
								!checkboxListCircle.getItems().iterator().hasNext() &&
								!checkboxListConvention.getItems().iterator().hasNext() &&
								!checkboxListContent.getItems().iterator().hasNext() &&
								!checkboxListParody.getItems().iterator().hasNext())
							return;
						{
							JPanel panel = new JPanel();
							panel.setSize(250, 150);
							panel.setLayout(new GridLayout(2, 1));
							JLabel lab = new JLabel("<html><body>Delete all the entries from the Recycle Bin?<br/><i>(This cannot be undone)</i></body></html>");
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
									recycleDelete.setIcon(Core.Resources.Icons.get("JFrame/Loading"));
									recycleRestore.setEnabled(false);
									recycleDelete.setEnabled(false);
									recycleEmpty.setEnabled(false);
									;
									try {
									;
									for(Artist value : checkboxListArtist.getItems())
									{
										Artist o = (Artist)value;
										for(Book b : o.getBooks().elements())
										{
											b.getArtists().remove(o);
											//o.getBooks().remove(b);
										}
										for(Circle c : o.getCircles().elements())
										{
											c.getArtists().remove(o);
											//o.getCircles().remove(c);
										}
										Client.DB.getArtists().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Book value : checkboxListBook.getItems())
									{
										Book o = (Book)value;
										for(Circle c : o.getCircles().elements())
										{
											c.getBooks().remove(o);
											//o.getCircles().remove(c);
										}
										for(Artist a : o.getArtists().elements())
										{
											a.getBooks().remove(o);
											//o.getArtists().remove(a);
										}
										for(Content c : o.getContents().elements())
										{
											c.getBooks().remove(o);
											//o.getContents().remove(c);
										}
										for(Parody p : o.getParodies().elements())
										{
											p.getBooks().remove(o);
											//o.getParodies().remove(p);
										}
										Convention c = o.getConvention();
										try { c.getBooks().remove(o); } catch (NullPointerException npe) { ; }
										Client.DB.getBooks().delete(o);
										Client.DB.getDeleted().delete(value);
										try { Client.DS.child(o.getID()).delete(); } catch (DataStoreException dse) { ; }
									}
									for(Circle value : checkboxListCircle.getItems())
									{
										Circle o = (Circle)value;
										for(Book b : o.getBooks().elements())
										{
											b.getCircles().remove(o);
											//o.getBooks().remove(b);
										}
										for(Artist a : o.getArtists().elements())
										{
											a.getCircles().remove(o);
											//o.getArtists().remove(a);
										}
										Client.DB.getCircles().delete(o);
										Client.DB.getDeleted().delete(value);
										try { Client.DS.child(o.getID()).delete(); } catch (DataStoreException dse) { ; }
									}
									for(Convention value : checkboxListConvention.getItems())
									{
										Convention o = (Convention)value;
										for(Book b : o.getBooks().elements())
										{
											b.setConvention(null);
											//o.getBooks().remove(b);
										}
										Client.DB.getConventions().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Content value : checkboxListContent.getItems())
									{
										Content o = (Content)value;
										for(Book b : o.getBooks().elements())
										{
											b.getContents().remove(o);
											//o.getBooks().remove(b);
										}
										Client.DB.getContents().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									for(Parody value : checkboxListParody.getItems())
									{
										Parody o = (Parody)value;
										for(Book b : o.getBooks().elements())
										{
											b.getParodies().remove(o);
											//o.getBooks().remove(b);
										}
										Client.DB.getParodies().delete(o);
										Client.DB.getDeleted().delete(value);
									}
									System.gc();
									;
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									} catch (RemoteException re) {
										Core.Logger.log(re.getMessage(), Level.ERROR);
										re.printStackTrace();
									}
									;
									recycleRestore.setEnabled(true);
									recycleDelete.setEnabled(true);
									recycleEmpty.setEnabled(true);
									recycleDelete.setIcon(Core.Resources.Icons.get("JFrame/RecycleBin/Empty"));
									;
									Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
									;
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
										Core.Resources.Icons.get("JDesktop/Explorer/RecycleBin"),
										"Recycle Bin");
							} catch (PropertyVetoException pve)
							{
								Core.Logger.log(pve.getMessage(), Level.WARNING);
							} 
						}
					}
				};
				th.setPriority(Thread.MIN_PRIORITY);
				th.start();
			}			
		});
		panel1.add(recycleEmpty);
		Vector<Artist> deleted_a = new Vector<Artist>();
		Vector<Book> deleted_b = new Vector<Book>();
		Vector<Circle> deleted_c = new Vector<Circle>();
		Vector<Convention> deleted_cv = new Vector<Convention>();
		Vector<Content> deleted_cn = new Vector<Content>();
		Vector<Parody> deleted_p = new Vector<Parody>();
		try {
			for(Record r : Client.DB.getDeleted().elements())
			{
				if(r instanceof Artist)
				{
					deleted_a.add((Artist)r);
					continue;
				}
				if(r instanceof Book)
				{
					deleted_b.add((Book)r);
					continue;
				}
				if(r instanceof Circle)
				{
					deleted_c.add((Circle)r);
					continue;
				}
				if(r instanceof Convention)
				{
					deleted_cv.add((Convention)r);
					continue;
				}
				if(r instanceof Content)
				{
					deleted_cn.add((Content)r);
					continue;
				}
				if(r instanceof Parody)
				{
					deleted_p.add((Parody)r);
					continue;
				}
			}
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		} catch (RemoteException re) {
			Core.Logger.log(re.getMessage(), Level.ERROR);
			re.printStackTrace();
		}
		JSplitPane splitListA = new JSplitPane();
		JSplitPane splitListB = new JSplitPane();
		JSplitPane splitListC = new JSplitPane();
		JSplitPane splitListCV = new JSplitPane();
		JSplitPane splitListCN = new JSplitPane();
		JSplitPane splitListP = new JSplitPane();
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListArtist.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListArtist.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListArtist.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListArtist = new DouzCheckBoxList<Artist>(deleted_a, searchField);
			splitListA.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListA.setTopComponent(searchField);
			splitListA.setBottomComponent(checkboxListArtist);
			splitListA.setDividerSize(0);
			splitListA.setEnabled(false);	
		}
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListBook.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListBook.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListBook.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListBook = new DouzCheckBoxList<Book>(deleted_b, searchField);
			splitListB.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListB.setTopComponent(searchField);
			splitListB.setBottomComponent(checkboxListBook);
			splitListB.setDividerSize(0);
			splitListB.setEnabled(false);	
		}
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListCircle.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListCircle.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListCircle.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListCircle = new DouzCheckBoxList<Circle>(deleted_c, searchField);
			splitListC.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListC.setTopComponent(searchField);
			splitListC.setBottomComponent(checkboxListCircle);
			splitListC.setDividerSize(0);
			splitListC.setEnabled(false);	
		}
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListConvention.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListConvention.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListConvention.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListConvention = new DouzCheckBoxList<Convention>(deleted_cv, searchField);
			splitListCV.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListCV.setTopComponent(searchField);
			splitListCV.setBottomComponent(checkboxListConvention);
			splitListCV.setDividerSize(0);
			splitListCV.setEnabled(false);	
		}
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListContent.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListContent.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListContent.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListContent = new DouzCheckBoxList<Content>(deleted_cn, searchField);
			splitListCN.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListCN.setTopComponent(searchField);
			splitListCN.setBottomComponent(checkboxListContent);
			splitListCN.setDividerSize(0);
			splitListCN.setEnabled(false);	
		}
		{
			JTextField searchField = new JTextField(".*");
			searchField.getDocument().addDocumentListener(new DocumentListener()
			{
			    public void insertUpdate(DocumentEvent e) {
			    	checkboxListParody.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void removeUpdate(DocumentEvent e) {
			    	checkboxListParody.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			    public void changedUpdate(DocumentEvent e) {
			    	checkboxListParody.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
			    }
			});
			checkboxListParody = new DouzCheckBoxList<Parody>(deleted_p, searchField);
			splitListP.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListP.setTopComponent(searchField);
			splitListP.setBottomComponent(checkboxListParody);
			splitListP.setDividerSize(0);
			splitListP.setEnabled(false);	
		}
		panelBase = new JPanel();
		panelBase.setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				int posy = 0;
				panelFramed[0].setBounds(0, posy, width, panelFramed[0].getHeight());
				posy += panelFramed[0].getHeight();
				panelFramed[1].setBounds(0, posy, width, panelFramed[1].getHeight());
				posy += panelFramed[1].getHeight();
				panelFramed[2].setBounds(0, posy, width, panelFramed[2].getHeight());
				posy += panelFramed[2].getHeight();
				panelFramed[3].setBounds(0, posy, width, panelFramed[3].getHeight());
				posy += panelFramed[3].getHeight();
				panelFramed[4].setBounds(0, posy, width, panelFramed[4].getHeight());
				posy += panelFramed[4].getHeight();
				panelFramed[5].setBounds(0, posy, width, panelFramed[5].getHeight());
				panelBase.setPreferredSize(new Dimension(250, panelFramed[0].getHeight() +
											panelFramed[1].getHeight() +
											panelFramed[2].getHeight() +
											panelFramed[3].getHeight() +
											panelFramed[4].getHeight() +
											panelFramed[5].getHeight()));
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
			     return new Dimension(250, panelFramed[0].getHeight() +
							panelFramed[1].getHeight() +
							panelFramed[2].getHeight() +
							panelFramed[3].getHeight() +
							panelFramed[4].getHeight() +
							panelFramed[5].getHeight());
			}
		});
		labelListArtist = new JLabel("Artists", Core.Resources.Icons.get("JDesktop/Explorer/Artist"), JLabel.LEFT);
		panelFramed[0] = new KFramedPanel(labelListArtist, splitListA, panelBase);
		labelListBook = new JLabel("Books", Core.Resources.Icons.get("JDesktop/Explorer/Book"), JLabel.LEFT);
		panelFramed[1] = new KFramedPanel(labelListBook, splitListB, panelBase);
		labelListCircle = new JLabel("Circles", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), JLabel.LEFT);
		panelFramed[2] = new KFramedPanel(labelListCircle, splitListC, panelBase);
		labelListConvention = new JLabel("Conventions", Core.Resources.Icons.get("JDesktop/Explorer/Convention"), JLabel.LEFT);
		panelFramed[3] = new KFramedPanel(labelListConvention, splitListCV, panelBase);
		labelListContent = new JLabel("Contents", Core.Resources.Icons.get("JDesktop/Explorer/Content"), JLabel.LEFT);
		panelFramed[4] = new KFramedPanel(labelListContent, splitListCN, panelBase);
		labelListParody = new JLabel("Parodies", Core.Resources.Icons.get("JDesktop/Explorer/Parody"), JLabel.LEFT);
		panelFramed[5] = new KFramedPanel(labelListParody, splitListP, panelBase);
		for(KFramedPanel panel : panelFramed)
			panelBase.add(panel);
		scrollPanelBase = new JScrollPane(panelBase);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, scrollPanelBase);
		split.setDividerSize(1);
		split.setEnabled(false);
		pane.add(split);
		;
		panelBase.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Select All", Core.Resources.Icons.get("JFrame/RecycleBin/SelectAll"));
					tbl.put("Deselect All", Core.Resources.Icons.get("JFrame/RecycleBin/DeselectAll"));
					final DouzPopupMenu pop = new DouzPopupMenu("Recycle Bin", tbl);
					pop.show((Component)e.getSource(), e.getX(), e.getY());
					new Thread(getClass().getName()+"/MouseClicked")
					{
						@Override
						public void run()
						{
							while(pop.isValid())
								try { sleep(1); } catch (InterruptedException ie) { ; }
							int selected = pop.getResult();
							switch(selected)
							{
							case 0:{
								checkboxListArtist.setSelectedItems(checkboxListArtist.getItems());
								checkboxListBook.setSelectedItems(checkboxListBook.getItems());
								checkboxListCircle.setSelectedItems(checkboxListCircle.getItems());
								checkboxListContent.setSelectedItems(checkboxListContent.getItems());
								checkboxListConvention.setSelectedItems(checkboxListConvention.getItems());
								checkboxListParody.setSelectedItems(checkboxListParody.getItems());
								labelListArtist.setText("Artists (" + checkboxListArtist.getSelectedItemCount() + "/" + checkboxListArtist.getItemCount() + ")");
								labelListBook.setText("Books (" + checkboxListBook.getSelectedItemCount() + "/" + checkboxListBook.getItemCount() + ")");
								labelListCircle.setText("Circles (" + checkboxListCircle.getSelectedItemCount() + "/" + checkboxListCircle.getItemCount() + ")");
								labelListConvention.setText("Conventions (" + checkboxListConvention.getSelectedItemCount() + "/" + checkboxListConvention.getItemCount() + ")");
								labelListContent.setText("Contents (" + checkboxListContent.getSelectedItemCount() + "/" + checkboxListContent.getItemCount() + ")");
								labelListParody.setText("Parodies (" + checkboxListParody.getSelectedItemCount() + "/" + checkboxListParody.getItemCount() + ")");
								break;
							}
							case 1:{
								checkboxListArtist.setSelectedItems(new Vector<Artist>());
								checkboxListBook.setSelectedItems(new Vector<Book>());
								checkboxListCircle.setSelectedItems(new Vector<Circle>());
								checkboxListContent.setSelectedItems(new Vector<Content>());
								checkboxListConvention.setSelectedItems(new Vector<Convention>());
								checkboxListParody.setSelectedItems(new Vector<Parody>());
								labelListArtist.setText("Artists (" + checkboxListArtist.getSelectedItemCount() + "/" + checkboxListArtist.getItemCount() + ")");
								labelListBook.setText("Books (" + checkboxListBook.getSelectedItemCount() + "/" + checkboxListBook.getItemCount() + ")");
								labelListCircle.setText("Circles (" + checkboxListCircle.getSelectedItemCount() + "/" + checkboxListCircle.getItemCount() + ")");
								labelListConvention.setText("Conventions (" + checkboxListConvention.getSelectedItemCount() + "/" + checkboxListConvention.getItemCount() + ")");
								labelListContent.setText("Contents (" + checkboxListContent.getSelectedItemCount() + "/" + checkboxListContent.getItemCount() + ")");
								labelListParody.setText("Parodies (" + checkboxListParody.getSelectedItemCount() + "/" + checkboxListParody.getItemCount() + ")");
								break;
							}
							}
						}
					}.start();
				 }
			  }
		});
		;
		checkboxListArtist.addMouseListener(this);
		checkboxListBook.addMouseListener(this);
		checkboxListCircle.addMouseListener(this);
		checkboxListConvention.addMouseListener(this);
		checkboxListContent.addMouseListener(this);
		checkboxListParody.addMouseListener(this);
		;
		SwingUtilities.invokeLater(new Runnable(){public void run(){split.revalidate();}});
		validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		recycleLabelInfo.setBounds(0,0,130,20);
		recycleInfo.setBounds(2,22,125,55);
		recycleLabelTasks.setBounds(0,75+5,130,20);
		recycleRestore.setBounds(3,75+25+1,125,20);
		recycleDelete.setBounds(3,75+45+2,125,20);
		recycleEmpty.setBounds(3,75+65+2,125,20);
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
		/*Iterable<Record> it = checkboxList.getSelectedItems();
		checkboxList.setItems(Client.DB.getDeleted());
		checkboxList.setSelectedItems(it);*/
		Vector<Artist> deleted_a = new Vector<Artist>();
		Vector<Book> deleted_b = new Vector<Book>();
		Vector<Circle> deleted_c = new Vector<Circle>();
		Vector<Convention> deleted_cv = new Vector<Convention>();
		Vector<Content> deleted_cn = new Vector<Content>();
		Vector<Parody> deleted_p = new Vector<Parody>();
		try {
			for(Record r : Client.DB.getDeleted().elements())
			{
				if(r instanceof Artist)
				{
					deleted_a.add((Artist)r);
					continue;
				}
				if(r instanceof Book)
				{
					deleted_b.add((Book)r);
					continue;
				}
				if(r instanceof Circle)
				{
					deleted_c.add((Circle)r);
					continue;
				}
				if(r instanceof Convention)
				{
					deleted_cv.add((Convention)r);
					continue;
				}
				if(r instanceof Content)
				{
					deleted_cn.add((Content)r);
					continue;
				}
				if(r instanceof Parody)
				{
					deleted_p.add((Parody)r);
					continue;
				}
			}
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		} catch (RemoteException re) {
			Core.Logger.log(re.getMessage(), Level.ERROR);
			re.printStackTrace();
		}
		{
			Iterable<Artist> iterable = checkboxListArtist.getSelectedItems();
			checkboxListArtist.setItems(deleted_a);
			checkboxListArtist.setSelectedItems(iterable);
		}
		{
			Iterable<Book> iterable = checkboxListBook.getSelectedItems();
			checkboxListBook.setItems(deleted_b);
			checkboxListBook.setSelectedItems(iterable);
		}
		{
			Iterable<Circle> iterable = checkboxListCircle.getSelectedItems();
			checkboxListCircle.setItems(deleted_c);
			checkboxListCircle.setSelectedItems(iterable);
		}
		{
			Iterable<Convention> iterable = checkboxListConvention.getSelectedItems();
			checkboxListConvention.setItems(deleted_cv);
			checkboxListConvention.setSelectedItems(iterable);
		}
		{
			Iterable<Content> iterable = checkboxListContent.getSelectedItems();
			checkboxListContent.setItems(deleted_cn);
			checkboxListContent.setSelectedItems(iterable);
		}
		{
			Iterable<Parody> iterable = checkboxListParody.getSelectedItems();
			checkboxListParody.setItems(deleted_p);
			checkboxListParody.setSelectedItems(iterable);
		}
		long count;
		try {
			count = Client.DB.getDeleted().count();
			recycleInfo.setText((count==1)?("Item : 1"):("Items : "+count));
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		} catch (RemoteException re) {
			Core.Logger.log(re.getMessage(), Level.ERROR);
			re.printStackTrace();
		}
		labelListArtist.setText("Artists (" + checkboxListArtist.getSelectedItemCount() + "/" + checkboxListArtist.getItemCount() + ")");
		labelListBook.setText("Books (" + checkboxListBook.getSelectedItemCount() + "/" + checkboxListBook.getItemCount() + ")");
		labelListCircle.setText("Circles (" + checkboxListCircle.getSelectedItemCount() + "/" + checkboxListCircle.getItemCount() + ")");
		labelListConvention.setText("Conventions (" + checkboxListConvention.getSelectedItemCount() + "/" + checkboxListConvention.getItemCount() + ")");
		labelListContent.setText("Contents (" + checkboxListContent.getSelectedItemCount() + "/" + checkboxListContent.getItemCount() + ")");
		labelListParody.setText("Parodies (" + checkboxListParody.getSelectedItemCount() + "/" + checkboxListParody.getItemCount() + ")");
	}
	
	@SuppressWarnings("serial")
	private final class KFramedPanel extends JPanel implements LayoutManager, Runnable, ActionListener
	{
		private JLabel titleBar;
		private Component bodyComponent;
		private JCheckBox buttonToggle;
		private Component parentComponent;
		
		private int STATUS;
		private Thread process;
		private final int STATUS_MINIMIZED = 0x01;
		private final int STATUS_MINIMIZING = 0x02;
		private final int STATUS_MAXIMIZED = 0x03;
		private final int STATUS_MAXIMIZING = 0x04;
		private int shadowHeight = 0;
		
		public KFramedPanel(JLabel title, Component body, Component parent)
		{
			super();
			setLayout(this);
			STATUS = STATUS_MINIMIZED;
			setSize(100, 21);
			shadowHeight = 21;
			setMinimumSize(new Dimension(100, 21));
			setPreferredSize(new Dimension(250, 250));
			setMaximumSize(new Dimension(1280, 250));
			parentComponent = parent;
			titleBar = title;
			add(titleBar);
			bodyComponent = body;
			add(bodyComponent);
			buttonToggle = new JCheckBox()
			{
				@Override
				public void paint(Graphics g)
				{
					//super.paint(g);
					if(STATUS == STATUS_MAXIMIZED || STATUS == STATUS_MAXIMIZING)
						g.drawImage(Core.Resources.Icons.get("JPanel/ToggleButton/Checked").getImage(), 2, 2, this);
					if(STATUS == STATUS_MINIMIZED || STATUS == STATUS_MINIMIZING)
						g.drawImage(Core.Resources.Icons.get("JPanel/ToggleButton/Unchecked").getImage(), 2, 2, this);
				}
			};
			buttonToggle.setSelected(true);
			buttonToggle.addActionListener(this);
			add(buttonToggle);
		}
		@Override
		public void run()
		{
			while(true)
			{
				if(STATUS == STATUS_MINIMIZING)
				{
					if(shadowHeight <= getMinimumSize().getHeight())
					{
						STATUS = STATUS_MINIMIZED;
						parentComponent.validate();
						//SwingUtilities.invokeLater(new Runnable(){public void run(){getParent().validate();}});
						return;
					}
					//setPreferredSize(new Dimension(250, getHeight() - 1));
					setSize(new Dimension(getWidth(), --shadowHeight));
				}
				if(STATUS == STATUS_MAXIMIZING)
				{
					if(shadowHeight >= getMaximumSize().getHeight())
					{
						STATUS = STATUS_MAXIMIZED;
						parentComponent.validate();
						//SwingUtilities.invokeLater(new Runnable(){public void run(){getParent().validate();}});
						return;
					}
					//setPreferredSize(new Dimension(250, getHeight() + 1));
					setSize(new Dimension(getWidth(), ++shadowHeight));
				}
				SwingUtilities.invokeLater(new Runnable(){public void run(){parentComponent.validate();}});
				try { Thread.sleep(2); } catch (InterruptedException ie) { }
			}
		}
		
		@Override
		public int getHeight()
		{
			return shadowHeight;
		}

		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
			height = parent.getHeight();
			buttonToggle.setBounds(width - 20, 0, 20, 20);
			titleBar.setBounds(0, 0, width - 20, 20);
			bodyComponent.setBounds(0, 20, width, height - 20);
		}
		@Override
		public void addLayoutComponent(String key,Component c){}
		@Override
		public void removeLayoutComponent(Component c){}
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
		    return new Dimension(0, 20);
		}
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
		    return new Dimension(250, 250);
		}
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(buttonToggle.isSelected())
			{
				if(STATUS != STATUS_MAXIMIZED)
				{
					buttonToggle.setSelected(false);
					return;
				}
				buttonToggle.setSelected(true);
				shadowHeight = getHeight();
				STATUS = STATUS_MINIMIZING;
				process = new Thread(this, getClass().getCanonicalName()+"/ActionPerformed/Toggle");
				process.start();
			}else
			{
				if(STATUS != STATUS_MINIMIZED)
				{
					buttonToggle.setSelected(true);
					return;
				}
				buttonToggle.setSelected(false);
				shadowHeight = getHeight();
				STATUS = STATUS_MAXIMIZING;
				process = new Thread(this, getClass().getCanonicalName()+"/ActionPerformed/Toggle");
				process.start();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent me)
	{
		labelListArtist.setText("Artists (" + checkboxListArtist.getSelectedItemCount() + "/" + checkboxListArtist.getItemCount() + ")");
		labelListBook.setText("Books (" + checkboxListBook.getSelectedItemCount() + "/" + checkboxListBook.getItemCount() + ")");
		labelListCircle.setText("Circles (" + checkboxListCircle.getSelectedItemCount() + "/" + checkboxListCircle.getItemCount() + ")");
		labelListConvention.setText("Conventions (" + checkboxListConvention.getSelectedItemCount() + "/" + checkboxListConvention.getItemCount() + ")");
		labelListContent.setText("Contents (" + checkboxListContent.getSelectedItemCount() + "/" + checkboxListContent.getItemCount() + ")");
		labelListParody.setText("Parodies (" + checkboxListParody.getSelectedItemCount() + "/" + checkboxListParody.getItemCount() + ")");
	}
	@Override
	public void mouseEntered(MouseEvent me) {}
	@Override
	public void mouseExited(MouseEvent me) {}
	@Override
	public void mousePressed(MouseEvent me) {}
	@Override
	public void mouseReleased(MouseEvent me) {}
}