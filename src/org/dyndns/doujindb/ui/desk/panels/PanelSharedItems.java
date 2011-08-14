package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.DouzCheckBoxList;




@SuppressWarnings("serial")
public class PanelSharedItems implements Validable, LayoutManager, MouseListener
{
	@SuppressWarnings("unused")
	private DouzWindow parentWindow;
	
	private JSplitPane split;
	private JLabel sharedItemsLabelInfo;
	private JLabel sharedItemsInfo;
	private JLabel sharedItemsLabelTasks;
	private JButton sharedItemsConnect;
	private JButton sharedItemsDisconnect;
	
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
	
	public PanelSharedItems(DouzWindow parent, JComponent pane)
	{
		parentWindow = parent;
		pane.setLayout(this);
		JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		sharedItemsInfo = new JLabel("Items : 0");
		sharedItemsInfo.setVerticalAlignment(JLabel.TOP);
		sharedItemsInfo.setFont(Core.Resources.Font);
		panel1.add(sharedItemsInfo);
		sharedItemsLabelInfo = new JLabel(" Status");
		sharedItemsLabelInfo.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		sharedItemsLabelInfo.setOpaque(true);
		sharedItemsLabelInfo.setFont(Core.Resources.Font);
		panel1.add(sharedItemsLabelInfo);
		sharedItemsLabelTasks = new JLabel(" Tasks");
		sharedItemsLabelTasks.setBackground((Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor()).darker().darker());
		sharedItemsLabelTasks.setOpaque(true);
		sharedItemsLabelTasks.setFont(Core.Resources.Font);
		panel1.add(sharedItemsLabelTasks);
		sharedItemsDisconnect = new JButton("Disconnect", Core.Resources.Icons.get("JDesktop/SharedItems/Disconnect"));
		sharedItemsDisconnect.setFocusable(false);
		sharedItemsDisconnect.setFont(Core.Resources.Font);
		sharedItemsDisconnect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				//TODO Core.Network.disconnect();
			}			
		});
		panel1.add(sharedItemsDisconnect);
		sharedItemsConnect = new JButton("Connect", Core.Resources.Icons.get("JDesktop/SharedItems/Connect"));
		sharedItemsConnect.setFocusable(false);
		sharedItemsConnect.setFont(Core.Resources.Font);
		sharedItemsConnect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				//TODO Core.Network.connect();
			}			
		});
		panel1.add(sharedItemsConnect);
		Vector<Artist> deleted_a = new Vector<Artist>();
		Vector<Book> deleted_b = new Vector<Book>();
		Vector<Circle> deleted_c = new Vector<Circle>();
		Vector<Convention> deleted_cv = new Vector<Convention>();
		Vector<Content> deleted_cn = new Vector<Content>();
		Vector<Parody> deleted_p = new Vector<Parody>();
		for(Record r : Client.DB.getShared())
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
		checkboxListArtist.addMouseListener(this);
		checkboxListBook.addMouseListener(this);
		checkboxListCircle.addMouseListener(this);
		checkboxListConvention.addMouseListener(this);
		checkboxListContent.addMouseListener(this);
		checkboxListParody.addMouseListener(this);
		;
		SwingUtilities.invokeLater(new Runnable(){public void run(){split.revalidate();}});
		
		/*JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		sharedItemsInfo = new JLabel("<html>Status :<br/>Disconnected</html>");
		sharedItemsInfo.setVerticalAlignment(JLabel.TOP);
		sharedItemsInfo.setFont(Core.Resources.Font);
		panel1.add(sharedItemsInfo);
		sharedItemsLabelInfo = new JLabel(" Info");
		sharedItemsLabelInfo.setBackground(new Color(45,45,45));
		sharedItemsLabelInfo.setOpaque(true);
		sharedItemsLabelInfo.setFont(Core.Resources.Font);
		panel1.add(sharedItemsLabelInfo);
		sharedItemsLabelTasks = new JLabel(" Tasks");
		sharedItemsLabelTasks.setBackground(new Color(45,45,45));
		sharedItemsLabelTasks.setOpaque(true);
		sharedItemsLabelTasks.setFont(Core.Resources.Font);
		panel1.add(sharedItemsLabelTasks);
		sharedItemsDisconnect = new JButton("Disconnect", Core.Resources.get("Icon:Frame.SharedItems.Disconnect"));
		sharedItemsDisconnect.setFocusable(false);
		sharedItemsDisconnect.setFont(Core.Resources.Font);
		sharedItemsDisconnect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Network.disconnect();
				Core.UI.Desktop.validateUI(new ValidateEvent(ValidateEvent.DATABASE_REFRESH, null));
			}			
		});
		panel1.add(sharedItemsDisconnect);
		sharedItemsConnect = new JButton("Connect", Core.Resources.get("Icon:Frame.SharedItems.Connect"));
		sharedItemsConnect.setFocusable(false);
		sharedItemsConnect.setFont(Core.Resources.Font);
		sharedItemsConnect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				Network.connect();
				Core.UI.Desktop.validateUI(new ValidateEvent(ValidateEvent.DATABASE_REFRESH, null));
			}			
		});
		panel1.add(sharedItemsConnect);
		dtm = new DefaultTableModel()
		{
			@Override
			public Class<?> getColumnClass(int c) {
	            return getValueAt(0, c).getClass();
	        }
		};
		dtm.addColumn("");
		dtm.addColumn(" ");
		sharedItemsItems = new JTable()
		{
			@Override
			public void changeSelection(int rowIndex,
                    int columnIndex,
                    boolean toggle,
                    boolean extend)
			{
				;
			}
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return column==0;
			}
		};
		sharedItemsItems.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent me)
			{
				if(me.getClickCount() == 2)
				{
					Record item = (Record)dtm.getValueAt(sharedItemsItems.rowAtPoint(me.getPoint()), 1);
					if(item instanceof Artist)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_ARTIST, item);
					if(item instanceof Circle)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_CIRCLE, item);
					if(item instanceof Book)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_BOOK, item);
					if(item instanceof Content)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_CONTENT, item);
					if(item instanceof Convention)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_CONVENTION, item);
					if(item instanceof Parody)
						Core.UI.Desktop.openWindow(ExWindowType.WINDOW_PARODY, item);
				}
			}
		});
		sharedItemsItems.setModel(dtm);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(
				    JTable table,
				    Object value,
				    boolean isSelected,
				    boolean hasFocus,
				    int row,
				    int column)
			{
				super.getTableCellRendererComponent(
						table,
						value,
						isSelected,
						hasFocus,
						row,
						column);
				super.setIcon((ImageIcon)null);
				if(value instanceof Artist)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Artist"));
					return this;
				}
				if(value instanceof Circle)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Circle"));
					return this;
				}
				if(value instanceof Book)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Book"));
					return this;
				}
				if(value instanceof Content)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Content"));
					return this;
				}
				if(value instanceof Convention)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Convention"));
					return this;
				}
				if(value instanceof Parody)
				{
					super.setIcon(Core.Resources.get("Icon:Frame.Database.Explorer.Parody"));
					return this;
				}
				return this;
			}			
		};
		sharedItemsItems.getColumnModel().getColumn(1).setCellRenderer(renderer); 
		sharedItemsItems.setFont(Core.Resources.Font);
		sharedItemsItems.getColumnModel().getColumn(0).setMaxWidth(16);
		sharedItemsItems.getColumnModel().getColumn(0).setMinWidth(16);
		sharedItemsItems.getTableHeader().setReorderingAllowed(false);
		sharedItemsItems.getTableHeader().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				JTableHeader header = (JTableHeader) me.getSource();
				DefaultTableModel dtm = (DefaultTableModel)sharedItemsItems.getModel();
				int index = header.getColumnModel().getColumnIndexAtX(me.getX());
				if(index != 0)
					return;
				if(me.getButton() == MouseEvent.BUTTON1)
				{
					for(int i=0;i<dtm.getRowCount();i++)
						dtm.setValueAt(new Boolean(true), i, 0);
				}else{
					for(int i=0;i<dtm.getRowCount();i++)
						dtm.setValueAt(new Boolean(false), i, 0);
				}
			}			
		});
		sharedItemsScrollItems = new JScrollPane(sharedItemsItems);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, sharedItemsScrollItems);
		split.setDividerSize(1);
		split.setEnabled(false);
		pane.add(split);
		;
		split.validate();
		*/
		validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		sharedItemsLabelInfo.setBounds(0,0,130,20);
		sharedItemsInfo.setBounds(2,22,125,55);
		sharedItemsLabelTasks.setBounds(0,75+5,130,20);
		//TODO
		/*switch(Core.Network.getStatus())
		{
		case CONNECTED:
			sharedItemsConnect.setVisible(false);
			sharedItemsConnect.setEnabled(true);
			sharedItemsDisconnect.setVisible(true);
			sharedItemsDisconnect.setEnabled(true);
			sharedItemsInfo.setText("Connected");
			break;
		case DISCONNECTED:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(true);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(true);
			sharedItemsInfo.setText("Disconnected");
			break;
		case CONNECTING:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(false);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(false);
			sharedItemsInfo.setText("Connecting ...");
			break;
		case DISCONNECTING:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(false);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(false);
			sharedItemsInfo.setText("Disconnecting ...");
			break;
		}*/
		sharedItemsConnect.setBounds(3,75+25+1,125,20);
		sharedItemsDisconnect.setBounds(3,75+25+1,125,20);
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
		Vector<Artist> deleted_a = new Vector<Artist>();
		Vector<Book> deleted_b = new Vector<Book>();
		Vector<Circle> deleted_c = new Vector<Circle>();
		Vector<Convention> deleted_cv = new Vector<Convention>();
		Vector<Content> deleted_cn = new Vector<Content>();
		Vector<Parody> deleted_p = new Vector<Parody>();
		for(Record r : Client.DB.getShared())
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
		labelListArtist.setText("Artists (" + checkboxListArtist.getSelectedItemCount() + "/" + checkboxListArtist.getItemCount() + ")");
		labelListBook.setText("Books (" + checkboxListBook.getSelectedItemCount() + "/" + checkboxListBook.getItemCount() + ")");
		labelListCircle.setText("Circles (" + checkboxListCircle.getSelectedItemCount() + "/" + checkboxListCircle.getItemCount() + ")");
		labelListConvention.setText("Conventions (" + checkboxListConvention.getSelectedItemCount() + "/" + checkboxListConvention.getItemCount() + ")");
		labelListContent.setText("Contents (" + checkboxListContent.getSelectedItemCount() + "/" + checkboxListContent.getItemCount() + ")");
		labelListParody.setText("Parodies (" + checkboxListParody.getSelectedItemCount() + "/" + checkboxListParody.getItemCount() + ")");
		//TODO 
		/*switch(Core.Network.getStatus())
		{
		case CONNECTED:
			sharedItemsConnect.setVisible(false);
			sharedItemsConnect.setEnabled(true);
			sharedItemsDisconnect.setVisible(true);
			sharedItemsDisconnect.setEnabled(true);
			sharedItemsInfo.setText("Connected");
			break;
		case DISCONNECTED:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(true);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(true);
			sharedItemsInfo.setText("Disconnected");
			break;
		case CONNECTING:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(false);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(false);
			sharedItemsInfo.setText("Connecting ...");
			break;
		case DISCONNECTING:
			sharedItemsConnect.setVisible(true);
			sharedItemsConnect.setEnabled(false);
			sharedItemsDisconnect.setVisible(false);
			sharedItemsDisconnect.setEnabled(false);
			sharedItemsInfo.setText("Disconnecting ...");
			break;
		}*/
	}
	
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