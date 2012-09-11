package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.beans.PropertyVetoException;
import java.util.Hashtable;
import java.util.Vector;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;

public final class PanelRecycleBin implements DataBaseListener, LayoutManager, ListSelectionListener
{
	@SuppressWarnings("unused")
	private WindowEx parentWindow;
	
	private JSplitPane split;
	private JLabel recycleLabelInfo;
	private JLabel recycleInfo;
	private JLabel recycleLabelTasks;
	private JButton recycleRestore;
	private JButton recycleDelete;
	private JButton recycleEmpty;
	
	private DynamicPanel panelFramed[] = new DynamicPanel[6];
	private JScrollPane scrollPanelBase;
	private JPanel panelBase;
	private JList<Artist> listArtist;
	private JLabel labelListArtist;
	private JList<Circle> listCircle;
	private JLabel labelListCircle;
	private JList<Book> listBook;
	private JLabel labelListBook;
	private JList<Convention> listConvention;
	private JLabel labelListConvention;
	private JList<Content> listContent;
	private JLabel labelListContent;
	private JList<Parody> listParody;
	private JLabel labelListParody;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PanelRecycleBin(WindowEx parent, JComponent pane)
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
							DefaultListModel model;
							Vector<Artist> artists = new Vector<Artist>();
							Vector<Book> books = new Vector<Book>();
							Vector<Circle> circles = new Vector<Circle>();
							Vector<Content> contents = new Vector<Content>();
							Vector<Convention> conventions = new Vector<Convention>();
							Vector<Parody> parodies = new Vector<Parody>();
							for(int index : listArtist.getSelectedIndices())
								artists.add(listArtist.getModel().getElementAt(index));
							for(int index : listBook.getSelectedIndices())
								books.add(listBook.getModel().getElementAt(index));
							for(int index : listCircle.getSelectedIndices())
								circles.add(listCircle.getModel().getElementAt(index));
							for(int index : listContent.getSelectedIndices())
								contents.add(listContent.getModel().getElementAt(index));
							for(int index : listConvention.getSelectedIndices())
								conventions.add(listConvention.getModel().getElementAt(index));
							for(int index : listParody.getSelectedIndices())
								parodies.add(listParody.getModel().getElementAt(index));
							model = (DefaultListModel) listArtist.getModel();
							for(Artist artist : artists)
							{
								artist.doRestore();
								model.removeElement(artist);
							}
							model = (DefaultListModel) listBook.getModel();
							for(Book book : books)
							{
								book.doRestore();
								model.removeElement(book);
							}
							model = (DefaultListModel) listCircle.getModel();
							for(Circle circle : circles)
							{
								circle.doRestore();
								model.removeElement(circle);
							}
							model = (DefaultListModel) listContent.getModel();
							for(Content content : contents)
							{
								content.doRestore();
								model.removeElement(content);
							}
							model = (DefaultListModel) listConvention.getModel();
							for(Convention convention : conventions)
							{
								convention.doRestore();
								model.removeElement(convention);
							}
							model = (DefaultListModel) listParody.getModel();
							for(Parody parody : parodies)
							{
								parody.doRestore();
								model.removeElement(parody);
							}
			
							if(Core.Database.isAutocommit())
								Core.Database.doCommit();
							
							syncData();
						}catch(ArrayIndexOutOfBoundsException aioobe)
						{
							aioobe.printStackTrace();
							Core.Logger.log(aioobe.getMessage(), Level.WARNING);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
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
						if(listArtist.getSelectedIndices().length == 0 &&
								listBook.getSelectedIndices().length == 0 &&
								listCircle.getSelectedIndices().length == 0 &&
								listContent.getSelectedIndices().length == 0 &&
								listConvention.getSelectedIndices().length == 0 &&
								listParody.getSelectedIndices().length == 0)
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
									DialogEx window = (DialogEx) ((JComponent)ae.getSource()).getRootPane().getParent();
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
									
									try
									{
										DefaultListModel model;
										Vector<Artist> artists = new Vector<Artist>();
										Vector<Book> books = new Vector<Book>();
										Vector<Circle> circles = new Vector<Circle>();
										Vector<Content> contents = new Vector<Content>();
										Vector<Convention> conventions = new Vector<Convention>();
										Vector<Parody> parodies = new Vector<Parody>();
										for(int index : listArtist.getSelectedIndices())
											artists.add(listArtist.getModel().getElementAt(index));
										for(int index : listBook.getSelectedIndices())
											books.add(listBook.getModel().getElementAt(index));
										for(int index : listCircle.getSelectedIndices())
											circles.add(listCircle.getModel().getElementAt(index));
										for(int index : listContent.getSelectedIndices())
											contents.add(listContent.getModel().getElementAt(index));
										for(int index : listConvention.getSelectedIndices())
											conventions.add(listConvention.getModel().getElementAt(index));
										for(int index : listParody.getSelectedIndices())
											parodies.add(listParody.getModel().getElementAt(index));
										model = (DefaultListModel) listArtist.getModel();
										for(Artist artist : artists)
										{
											artist.removeAll();
											model.removeElement(artist);
											Core.Database.doDelete(artist);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listBook.getModel();
										for(Book book : books)
										{
											book.removeAll();
											model.removeElement(book);
											Core.Database.doDelete(book);
											Core.Database.doCommit();
											try { Core.Repository.child(book.getID()).delete(); } catch (RepositoryException dse) { ; }
										}
										model = (DefaultListModel) listCircle.getModel();
										for(Circle circle : circles)
										{
											circle.removeAll();
											model.removeElement(circle);
											Core.Database.doDelete(circle);
											Core.Database.doCommit();
											try { Core.Repository.child(circle.getID()).delete(); } catch (RepositoryException dse) { ; }
										}
										model = (DefaultListModel) listContent.getModel();
										for(Content content : contents)
										{
											content.removeAll();
											model.removeElement(content);
											Core.Database.doDelete(content);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listConvention.getModel();
										for(Convention convention : conventions)
										{
											convention.removeAll();
											model.removeElement(convention);
											Core.Database.doDelete(convention);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listParody.getModel();
										for(Parody parody : parodies)
										{
											parody.removeAll();
											model.removeElement(parody);
											Core.Database.doDelete(parody);
											Core.Database.doCommit();
										}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}

									recycleRestore.setEnabled(true);
									recycleDelete.setEnabled(true);
									recycleEmpty.setEnabled(true);
									recycleDelete.setIcon(Core.Resources.Icons.get("JFrame/RecycleBin/Delete"));

									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									
									syncData();

									DialogEx window = (DialogEx) ((JComponent)ae.getSource()).getRootPane().getParent();
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
									DialogEx window = (DialogEx) ((JComponent)ae.getSource()).getRootPane().getParent();
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
									recycleEmpty.setIcon(Core.Resources.Icons.get("JFrame/Loading"));
									recycleRestore.setEnabled(false);
									recycleDelete.setEnabled(false);
									recycleEmpty.setEnabled(false);

									try
									{
										DefaultListModel model;
										Vector<Artist> artists = new Vector<Artist>();
										Vector<Book> books = new Vector<Book>();
										Vector<Circle> circles = new Vector<Circle>();
										Vector<Content> contents = new Vector<Content>();
										Vector<Convention> conventions = new Vector<Convention>();
										Vector<Parody> parodies = new Vector<Parody>();
										for(int i=0;i<listArtist.getModel().getSize();i++)
											artists.add(listArtist.getModel().getElementAt(i));
										for(int i=0;i<listBook.getModel().getSize();i++)
											books.add(listBook.getModel().getElementAt(i));
										for(int i=0;i<listCircle.getModel().getSize();i++)
											circles.add(listCircle.getModel().getElementAt(i));
										for(int i=0;i<listContent.getModel().getSize();i++)
											contents.add(listContent.getModel().getElementAt(i));
										for(int i=0;i<listConvention.getModel().getSize();i++)
											conventions.add(listConvention.getModel().getElementAt(i));
										for(int i=0;i<listParody.getModel().getSize();i++)
											parodies.add(listParody.getModel().getElementAt(i));
										model = (DefaultListModel) listArtist.getModel();
										for(Artist artist : artists)
										{
											artist.removeAll();
											model.removeElement(artist);
											Core.Database.doDelete(artist);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listBook.getModel();
										for(Book book : books)
										{
											book.removeAll();
											model.removeElement(book);
											Core.Database.doDelete(book);
											Core.Database.doCommit();
											try { Core.Repository.child(book.getID()).delete(); } catch (RepositoryException dse) { ; }
										}
										model = (DefaultListModel) listCircle.getModel();
										for(Circle circle : circles)
										{
											circle.removeAll();
											model.removeElement(circle);
											Core.Database.doDelete(circle);
											Core.Database.doCommit();
											try { Core.Repository.child(circle.getID()).delete(); } catch (RepositoryException dse) { ; }
										}
										model = (DefaultListModel) listContent.getModel();
										for(Content content : contents)
										{
											content.removeAll();
											model.removeElement(content);
											Core.Database.doDelete(content);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listConvention.getModel();
										for(Convention convention : conventions)
										{
											convention.removeAll();
											model.removeElement(convention);
											Core.Database.doDelete(convention);
											Core.Database.doCommit();
										}
										model = (DefaultListModel) listParody.getModel();
										for(Parody parody : parodies)
										{
											parody.removeAll();
											model.removeElement(parody);
											Core.Database.doDelete(parody);
											Core.Database.doCommit();
										}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}

									recycleRestore.setEnabled(true);
									recycleDelete.setEnabled(true);
									recycleEmpty.setEnabled(true);
									recycleEmpty.setIcon(Core.Resources.Icons.get("JFrame/RecycleBin/Empty"));

									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									
									syncData();

									DialogEx window = (DialogEx) ((JComponent)ae.getSource()).getRootPane().getParent();
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
			for(Record r : Core.Database.getRecycled())
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
		}
		JSplitPane splitListA = new JSplitPane();
		JSplitPane splitListB = new JSplitPane();
		JSplitPane splitListC = new JSplitPane();
		JSplitPane splitListCV = new JSplitPane();
		JSplitPane splitListCN = new JSplitPane();
		JSplitPane splitListP = new JSplitPane();
		Color foreground = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
		Color background = (Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker();
		{
			DefaultListModel model = new DefaultListModel();
			for(Artist o : deleted_a)
				model.add(0, o);
			listArtist = new JList<Artist>();
			listArtist.setModel(model);
			listArtist.setBackground(background);
			listArtist.setForeground(foreground);
			listArtist.setSelectionBackground(foreground);
			listArtist.setSelectionForeground(background);
			listArtist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListA.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListA.setBottomComponent(new JScrollPane(listArtist));
			splitListA.setDividerSize(0);
			splitListA.setEnabled(false);	
		}
		{
			DefaultListModel model = new DefaultListModel();
			for(Book o : deleted_b)
				model.add(0, o);
			listBook = new JList<Book>();
			listBook.setModel(model);
			listBook.setBackground(background);
			listBook.setForeground(foreground);
			listBook.setSelectionBackground(foreground);
			listBook.setSelectionForeground(background);
			listBook.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListB.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListB.setBottomComponent(new JScrollPane(listBook));
			splitListB.setDividerSize(0);
			splitListB.setEnabled(false);	
		}
		{
			DefaultListModel model = new DefaultListModel();
			for(Circle o : deleted_c)
				model.add(0, o);
			listCircle = new JList<Circle>();
			listCircle.setModel(model);
			listCircle.setBackground(background);
			listCircle.setForeground(foreground);
			listCircle.setSelectionBackground(foreground);
			listCircle.setSelectionForeground(background);
			listCircle.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListC.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListC.setBottomComponent(new JScrollPane(listCircle));
			splitListC.setDividerSize(0);
			splitListC.setEnabled(false);	
		}
		{
			DefaultListModel model = new DefaultListModel();
			for(Convention o : deleted_cv)
				model.add(0, o);
			listConvention = new JList<Convention>();
			listConvention.setModel(model);
			listConvention.setBackground(background);
			listConvention.setForeground(foreground);
			listConvention.setSelectionBackground(foreground);
			listConvention.setSelectionForeground(background);
			listConvention.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListCV.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListCV.setBottomComponent(new JScrollPane(listConvention));
			splitListCV.setDividerSize(0);
			splitListCV.setEnabled(false);	
		}
		{
			DefaultListModel model = new DefaultListModel();
			for(Content o : deleted_cn)
				model.add(0, o);
			listContent = new JList<Content>();
			listContent.setModel(model);
			listContent.setBackground(background);
			listContent.setForeground(foreground);
			listContent.setSelectionBackground(foreground);
			listContent.setSelectionForeground(background);
			listContent.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListCN.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListCN.setBottomComponent(new JScrollPane(listContent));
			splitListCN.setDividerSize(0);
			splitListCN.setEnabled(false);	
		}
		{
			DefaultListModel model = new DefaultListModel();
			for(Parody o : deleted_p)
				model.add(0, o);
			listParody = new JList<Parody>();
			listParody.setModel(model);
			listParody.setBackground(background);
			listParody.setForeground(foreground);
			listParody.setSelectionBackground(foreground);
			listParody.setSelectionForeground(background);
			listParody.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			splitListP.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitListP.setBottomComponent(new JScrollPane(listParody));
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
		panelFramed[0] = new DynamicPanel(labelListArtist, splitListA, panelBase);
		labelListBook = new JLabel("Books", Core.Resources.Icons.get("JDesktop/Explorer/Book"), JLabel.LEFT);
		panelFramed[1] = new DynamicPanel(labelListBook, splitListB, panelBase);
		labelListCircle = new JLabel("Circles", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), JLabel.LEFT);
		panelFramed[2] = new DynamicPanel(labelListCircle, splitListC, panelBase);
		labelListConvention = new JLabel("Conventions", Core.Resources.Icons.get("JDesktop/Explorer/Convention"), JLabel.LEFT);
		panelFramed[3] = new DynamicPanel(labelListConvention, splitListCV, panelBase);
		labelListContent = new JLabel("Contents", Core.Resources.Icons.get("JDesktop/Explorer/Content"), JLabel.LEFT);
		panelFramed[4] = new DynamicPanel(labelListContent, splitListCN, panelBase);
		labelListParody = new JLabel("Parodies", Core.Resources.Icons.get("JDesktop/Explorer/Parody"), JLabel.LEFT);
		panelFramed[5] = new DynamicPanel(labelListParody, splitListP, panelBase);
		for(DynamicPanel panel : panelFramed)
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
					final PopupMenuEx pop = new PopupMenuEx("Recycle Bin", tbl);
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
								listArtist.setSelectionInterval(0, listArtist.getModel().getSize() - 1);
								listBook.setSelectionInterval(0, listBook.getModel().getSize() - 1);
								listCircle.setSelectionInterval(0, listCircle.getModel().getSize() - 1);
								listContent.setSelectionInterval(0, listContent.getModel().getSize() - 1);
								listConvention.setSelectionInterval(0, listConvention.getModel().getSize() - 1);
								listParody.setSelectionInterval(0, listParody.getModel().getSize() - 1);
								syncData();
								break;
							}
							case 1:{
								listArtist.clearSelection();
								listBook.clearSelection();
								listCircle.clearSelection();
								listContent.clearSelection();
								listConvention.clearSelection();
								listParody.clearSelection();
								syncData();
								break;
							}
							}
						}
					}.start();
				 }
			  }
		});
		;
		listArtist.addListSelectionListener(this);
		listBook.addListSelectionListener(this);
		listCircle.addListSelectionListener(this);
		listConvention.addListSelectionListener(this);
		listContent.addListSelectionListener(this);
		listParody.addListSelectionListener(this);
		;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				split.revalidate();
				syncData();
			}
		});
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
	public void addLayoutComponent(String key,Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
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
	
	private void syncData()
	{
		long count;
		try {
			count = Core.Database.getRecycled().size();
			recycleInfo.setText((count==1)?("Item : 1"):("Items : "+count));
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
		labelListArtist.setText("Artists (" + (listArtist.getSelectedIndices().length) + "/" + listArtist.getModel().getSize() + ")");
		labelListBook.setText("Books (" + (listBook.getSelectedIndices().length) + "/" + listBook.getModel().getSize() + ")");
		labelListCircle.setText("Circles (" + (listCircle.getSelectedIndices().length) + "/" + listCircle.getModel().getSize() + ")");
		labelListConvention.setText("Conventions (" + (listConvention.getSelectedIndices().length) + "/" + listConvention.getModel().getSize() + ")");
		labelListContent.setText("Contents (" + (listContent.getSelectedIndices().length) + "/" + listContent.getModel().getSize() + ")");
		labelListParody.setText("Parodies (" + (listParody.getSelectedIndices().length) + "/" + listParody.getModel().getSize() + ")");
	}
	
	@SuppressWarnings("serial")
	private final class DynamicPanel extends JPanel implements LayoutManager, ActionListener
	{
		private JLabel titleBar;
		private Component bodyComponent;
		private JButton buttonToggle;
		private Component parentComponent;
		
		private int STATUS;
		private final int STATUS_MINIMIZED = 0x1;
		private final int STATUS_MAXIMIZED = 0x2;
		
		private ImageIcon ICON_CHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Checked");
		private ImageIcon ICON_UNCHECKED = Core.Resources.Icons.get("JPanel/ToggleButton/Unchecked");
		
		public DynamicPanel(JLabel title, Component body, Component parent)
		{
			super();
			setLayout(this);
			STATUS = STATUS_MINIMIZED;
			setSize(100, 21);
			setMinimumSize(new Dimension(100, 21));
			setPreferredSize(new Dimension(250, 250));
			setMaximumSize(new Dimension(1280, 250));
			parentComponent = parent;
			titleBar = title;
			add(titleBar);
			bodyComponent = body;
			add(bodyComponent);
			buttonToggle = new JButton(ICON_CHECKED);
			buttonToggle.setSelected(true);
			buttonToggle.addActionListener(this);
			add(buttonToggle);
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
		public void addLayoutComponent(String key,Component c) {}
		
		@Override
		public void removeLayoutComponent(Component c) {}
		
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
			if(STATUS == STATUS_MAXIMIZED)
			{
				STATUS = STATUS_MINIMIZED;
				buttonToggle.setIcon(ICON_CHECKED);
				setSize(new Dimension(getWidth(), (int)getMinimumSize().getHeight()));
				parentComponent.validate();
			} else {
				STATUS = STATUS_MAXIMIZED;
				buttonToggle.setIcon(ICON_UNCHECKED);
				setSize(new Dimension(getWidth(), (int)getMaximumSize().getHeight()));
				parentComponent.validate();
			}
		}
	}

	@Override
	public void recordAdded(Record rcd) {}
	
	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data) { }
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Artist)
		{
			DefaultListModel model = (DefaultListModel)listArtist.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
		if(rcd instanceof Book)
		{
			DefaultListModel model = (DefaultListModel)listBook.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
		if(rcd instanceof Circle)
		{
			DefaultListModel model = (DefaultListModel)listCircle.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
		if(rcd instanceof Content)
		{
			DefaultListModel model = (DefaultListModel)listContent.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
		if(rcd instanceof Convention)
		{
			DefaultListModel model = (DefaultListModel)listConvention.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
		if(rcd instanceof Parody)
		{
			DefaultListModel model = (DefaultListModel)listParody.getModel();
			model.add(0, rcd);
			syncData();
			return;
		}
	}

	@Override
	public void recordRestored(Record rcd) { }
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		syncData();
	}
}