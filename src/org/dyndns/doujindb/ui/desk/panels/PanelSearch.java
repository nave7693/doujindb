package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.PropertyException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;

@SuppressWarnings("serial")
public final class PanelSearch extends JPanel implements Validable
{
	private Validable panel;
	
	public enum Type
	{
		ISEARCH_ARTIST,
		ISEARCH_BOOK,
		ISEARCH_CIRCLE,
		ISEARCH_CONTENT,
		ISEARCH_CONVENTION,
		ISEARCH_PARODY
	}
	
	public PanelSearch(Type tokenType, JTabbedPane tab, int index)
	{
		super();
		switch(tokenType)
		{
		case ISEARCH_ARTIST:
		{
			panel = new IPanelArtist(this, tab, index);
			break;
		}
		case ISEARCH_BOOK:
		{
			panel = new IPanelBook(this, tab, index);
			break;
		}
		case ISEARCH_CIRCLE:
		{
			panel = new IPanelCircle(this, tab, index);
			break;
		}
		case ISEARCH_CONTENT:
		{
			panel = new IPanelContent(this, tab, index);
			break;
		}
		case ISEARCH_CONVENTION:
		{
			panel = new IPanelConvention(this, tab, index);
			break;
		}
		case ISEARCH_PARODY:
		{
			panel = new IPanelParody(this, tab, index);
			break;
		}
		}
	}
	
	private final class RecordTableRenderer extends DefaultTableCellRenderer
	{
		private Color background;
		private Color foreground;
		
		public RecordTableRenderer(Color background, Color foreground)
		{
		    super();
		    this.background = background;
		    this.foreground = foreground;
		}
	
		public Component getTableCellRendererComponent(
		    JTable table,
		    Object value,
		    boolean isSelected,
		    boolean hasFocus,
		    int row,
		    int column) {
		    super.getTableCellRendererComponent(
		        table,
		        value,
		        isSelected,
		        hasFocus,
		        row,
		        column);
		    super.setBorder(null);
		    super.setText(" " + super.getText());
		    if(isSelected)
			{
				setBackground(foreground);
				setForeground(background);
			}else{
				setBackground(background);
				setForeground(foreground);
			}
		    return this;
		}
	}
	
	private final class RecordTableModel extends DefaultTableModel
	{
		public RecordTableModel(Class<?> record)
		{
			super();
			if(record == Artist.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romanji");
		    }
			if(record == Book.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romanji");
		    }
			if(record == Circle.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romanji");
		    }
			if(record == Convention.class)
		    {
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
		    }
			if(record == Content.class)
		    {
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
		    }
			if(record == Parody.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romanji");
		    }
		}
		
		public void addRecord(Record record)
		{
			if(record instanceof Artist)
			{
				Artist a = (Artist)record;
				super.addRow(new Object[]{a,
						a.getJapaneseName(),
						a.getTranslatedName(),
						a.getRomanjiName()});
			}
			if(record instanceof Book)
			{
				Book b = (Book)record;
				super.addRow(new Object[]{b,
						b.getJapaneseName(),
						b.getTranslatedName(),
						b.getRomanjiName()});
			}
			if(record instanceof Circle)
			{
				Circle c = (Circle)record;
				super.addRow(new Object[]{c,
						c.getJapaneseName(),
						c.getTranslatedName(),
						c.getRomanjiName()});
			}
			if(record instanceof Convention)
			{
				Convention e = (Convention)record;
				super.addRow(new Object[]{e,
						e.getTagName(),
						e.getInfo()});
			}
			if(record instanceof Content)
			{
				Content t = (Content)record;
				super.addRow(new Object[]{t,
						t.getTagName(),
						t.getInfo()});
			}
			if(record instanceof Parody)
			{
				Parody p = (Parody)record;
				super.addRow(new Object[]{p,
						p.getJapaneseName(),
						p.getTranslatedName(),
						p.getRomanjiName()});
			}
		}
	}

	private final class RecordTableEditor extends AbstractCellEditor implements TableCellEditor
	{
		public RecordTableEditor()
		{
			super();
		}
	
		public Object getCellEditorValue()
		{
			return 0;
		}

		public Component getTableCellEditorComponent(
		    JTable table,
		    Object value,
		    boolean isSelected,
		    int row,
		    int column)
			{
			    super.cancelCellEditing();
			    return null;
			}
	}
	
	private final class IPanelArtist implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelArtist(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField("");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField("");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField("");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField("");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Artist.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
							.getValueAt(
									tableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_ARTIST, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
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
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Artist a = (Artist)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										a.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, a));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelJapaneseName.setBounds(3, 3, 100, 15);
			textJapaneseName.setBounds(103, 3, width - 106, 15);
			labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
			textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
			labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
			textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryArtist query = new QueryArtist();
							if(!textJapaneseName.getText().equals(""))
								query.JapaneseName = textJapaneseName.getText();
							if(!textTranslatedName.getText().equals(""))
								query.TranslatedName = textTranslatedName.getText();
							if(!textRomanjiName.getText().equals(""))
								query.RomanjiName = textRomanjiName.getText();
							if(!textWeblink.getText().equals(""))
								query.Weblink = textWeblink.getText();
							RecordSet<Artist> result = Core.Database.getArtists(query);
							for(Artist a : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(a);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelCircle implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelCircle(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField("");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField("");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField("");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField("");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Circle.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CIRCLE, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
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
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Circle c = (Circle)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										c.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, c));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelJapaneseName.setBounds(3, 3, 100, 15);
			textJapaneseName.setBounds(103, 3, width - 106, 15);
			labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
			textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
			labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
			textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryCircle query = new QueryCircle();
							if(!textJapaneseName.getText().equals(""))
								query.JapaneseName = textJapaneseName.getText();
							if(!textTranslatedName.getText().equals(""))
								query.TranslatedName = textTranslatedName.getText();
							if(!textRomanjiName.getText().equals(""))
								query.RomanjiName = textRomanjiName.getText();
							if(!textWeblink.getText().equals(""))
								query.Weblink = textWeblink.getText();
							RecordSet<Circle> result = Core.Database.getCircles(query);
							for(Circle c : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(c);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelBook implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelType;
		private JComboBox<Book.Type> comboType;
		private JCheckBox checkAdult;
		private JCheckBox checkDecensored;
		private JCheckBox checkTranslated;
		private JCheckBox checkColored;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelBook(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField("");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField("");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField("");
			textRomanjiName.setFont(font);
			labelType = new JLabel("Type");
			labelType.setFont(font);
			comboType = new JComboBox<Book.Type>();
			comboType.setFont(font);
			comboType.setFocusable(false);
			for(Book.Type tokenType : Book.Type.values())
				comboType.addItem(tokenType);
			comboType.setSelectedItem(Book.Type.同人誌);
			checkAdult = new JCheckBox("Adult", true);
			checkAdult.setFont(font);
			checkAdult.setFocusable(false);
			checkDecensored = new JCheckBox("Decensored", false);
			checkDecensored.setFont(font);
			checkDecensored.setFocusable(false);
			checkTranslated = new JCheckBox("Translated", false);
			checkTranslated.setFont(font);
			checkTranslated.setFocusable(false);
			checkColored = new JCheckBox("Colored", false);
			checkColored.setFont(font);
			checkColored.setFocusable(false);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Book.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					if(tableResults.getSelectedRowCount() == 1)
						tbl.put("Clone", Core.Resources.Icons.get("JDesktop/Explorer/Clone"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
					pop.show((Component)e.getSource(), e.getX(), e.getY());
					new Thread(getClass().getName()+"/MouseClicked")
					{
						@Override
						public void run()
						{
							while(pop.isValid())
								try { sleep(1); } catch (InterruptedException ie) { ; }
							String choice = pop.getChoice();
							if(choice.equals("Delete"))
							{
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Book b = (Book)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										b.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, b));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
							}
							if(choice.equals("Clone"))
							{
								Book book = ((Book)tableModel.getValueAt(tableSorter.convertRowIndexToModel(tableResults.getSelectedRow()), 0));

								Book clone = Core.Database.doInsert(Book.class);
								clone.setJapaneseName(book.getJapaneseName());
								clone.setTranslatedName(book.getTranslatedName());
								clone.setRomanjiName(book.getRomanjiName());
								clone.setInfo(book.getInfo());
								clone.setDate(book.getDate());
								clone.setRating(book.getRating());
								clone.setConvention(book.getConvention());
								clone.setType(book.getType());
								clone.setPages(book.getPages());
								clone.setAdult(book.isAdult());
								clone.setDecensored(book.isDecensored());
								clone.setTranslated(book.isTranslated());
								clone.setColored(book.isColored());
								for(Artist a : book.getArtists())
									clone.addArtist(a);
								for(Content c : book.getContents())
									clone.addContent(c);
								for(Parody p : book.getParodies())
									clone.addParody(p);
								if(Core.Database.isAutocommit())
									Core.Database.doCommit();
								Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_INSERT, clone));			
								DouzWindow window = Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, clone);
								window.setTitle("(Clone) " + window.getTitle());
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelType);
			pane.add(comboType);
			pane.add(checkAdult);
			pane.add(checkDecensored);
			pane.add(checkTranslated);
			pane.add(checkColored);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelJapaneseName.setBounds(3, 3, 100, 15);
			textJapaneseName.setBounds(103, 3, width - 106, 15);
			labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
			textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
			labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
			textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);		
			labelType.setBounds(3, 3 + 45, 100, 20);
			comboType.setBounds(103, 3 + 45, 100, 20);
			checkAdult.setBounds(3, 3 + 70, 100, 15);
			checkDecensored.setBounds(3, 3 + 85, 100, 15);
			checkTranslated.setBounds(3, 3 + 100, 100, 15);
			checkColored.setBounds(3, 3 + 115, 100, 15);
			labelResults.setBounds(3, 3 + 130, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 145, width - 5, height - 175);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryBook query = new QueryBook();
							if(!textJapaneseName.getText().equals(""))
								query.JapaneseName = textJapaneseName.getText();
							if(!textTranslatedName.getText().equals(""))
								query.TranslatedName = textTranslatedName.getText();
							if(!textRomanjiName.getText().equals(""))
								query.RomanjiName = textRomanjiName.getText();
							query.Type = (org.dyndns.doujindb.db.records.Book.Type) comboType.getSelectedItem();
							query.Adult = checkAdult.isSelected();
							query.Colored = checkColored.isSelected();
							query.Translated = checkTranslated.isSelected();
							query.Decensored = checkDecensored.isSelected();
							RecordSet<Book> result = Core.Database.getBooks(query);
							for(Book b : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(b);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Book"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Book"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}		
	}
	private final class IPanelContent implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelContent(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField("");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Content.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONTENT, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
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
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Content t = (Content)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										t.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, t));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelTagName);
			pane.add(textTagName);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			labelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryContent query = new QueryContent();
							if(!textTagName.getText().equals(""))
								query.TagName = textTagName.getText();
							RecordSet<Content> result = Core.Database.getContents(query);
							for(Content t : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(t);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Content"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Content"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}		
	}
	private final class IPanelConvention implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelConvention(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField("");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Convention.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONVENTION, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
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
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Convention e = (Convention)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										e.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, e));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelTagName);
			pane.add(textTagName);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			labelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryConvention query = new QueryConvention();
							if(!textTagName.getText().equals(""))
								query.TagName = textTagName.getText();
							RecordSet<Convention> result = Core.Database.getConventions(query);
							for(Convention e : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(e);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelParody implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JTable tableResults;
		private RecordTableModel tableModel;
		private RecordTableRenderer tableRenderer;
		private RecordTableEditor tableEditor;
		private TableRowSorter<DefaultTableModel> tableSorter;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelParody(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField("");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField("");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField("");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField("");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			tableModel = new RecordTableModel(Parody.class);
			tableResults.setModel(tableModel);
			tableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
			tableResults.setRowSorter(tableSorter);
			tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			tableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(tableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(tableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(!stopped) // check if JTable is not being populated or suffer a java.util.ConcurrentModificationException
					return;
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_PARODY, item);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
					final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
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
								try {
									for(int index : tableResults.getSelectedRows())
									{
										Parody p = (Parody)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										p.doRecycle();
										tableModel.removeRow(index);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
										Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, p));
									}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableResults.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelJapaneseName.setBounds(3, 3, 100, 15);
			textJapaneseName.setBounds(103, 3, width - 106, 15);
			labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
			textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
			labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
			textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
		public void actionPerformed(ActionEvent ae)
		{
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						while(tableModel.getRowCount()>0)
							tableModel.removeRow(0);
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							QueryParody query = new QueryParody();
							if(!textJapaneseName.getText().equals(""))
								query.JapaneseName = textJapaneseName.getText();
							if(!textTranslatedName.getText().equals(""))
								query.TranslatedName = textTranslatedName.getText();
							if(!textRomanjiName.getText().equals(""))
								query.RomanjiName = textRomanjiName.getText();
							if(!textWeblink.getText().equals(""))
								query.Weblink = textWeblink.getText();
							RecordSet<Parody> result = Core.Database.getParodies(query);
							for(Parody p : result)
							{
								if(stopped)
								{
									labelResults.setText("Found : " + tableModel.getRowCount());
									break;
								}
								try
								{
									tableModel.addRecord(p);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
							}
							labelResults.setText("Found : " + tableModel.getRowCount());
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();	
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		panel.validateUI(ve);
		super.validate();
	}
}