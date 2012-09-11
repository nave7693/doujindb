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
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.panels.util.TransferHandlerEx;

@SuppressWarnings("serial")
public final class PanelSearch extends JPanel implements DataBaseListener
{
	private DataBaseListener child;
	
	public enum Type
	{
		ARTIST,
		BOOK,
		CIRCLE,
		CONTENT,
		CONVENTION,
		PARODY
	}
	
	public PanelSearch(Type tokenType, JTabbedPane tab, int index)
	{
		super();
		switch(tokenType)
		{
		case ARTIST:
		{
			child = new IPanelArtist(this, tab, index);
			break;
		}
		case BOOK:
		{
			child = new IPanelBook(this, tab, index);
			break;
		}
		case CIRCLE:
		{
			child = new IPanelCircle(this, tab, index);
			break;
		}
		case CONTENT:
		{
			child = new IPanelContent(this, tab, index);
			break;
		}
		case CONVENTION:
		{
			child = new IPanelConvention(this, tab, index);
			break;
		}
		case PARODY:
		{
			child = new IPanelParody(this, tab, index);
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
				addColumn("Romaji");
		    }
			if(record == Book.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
		    }
			if(record == Circle.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
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
				addColumn("Romaji");
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
						a.getRomajiName()});
			}
			if(record instanceof Book)
			{
				Book b = (Book)record;
				super.addRow(new Object[]{b,
						b.getJapaneseName(),
						b.getTranslatedName(),
						b.getRomajiName()});
			}
			if(record instanceof Circle)
			{
				Circle c = (Circle)record;
				super.addRow(new Object[]{c,
						c.getJapaneseName(),
						c.getTranslatedName(),
						c.getRomajiName()});
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
						p.getRomajiName()});
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
	
	private final class IPanelArtist extends DataBaseAdapter implements LayoutManager, ActionListener
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
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
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
			labelRomajiName = new JLabel("Romaji Name");
			labelRomajiName.setFont(font);
			textRomajiName = new JTextField("");
			textRomajiName.setFont(font);
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
						final Record item = (Record)tableResults.getModel()
							.getValueAt(
									tableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_ARTIST, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Artist> deleted = new Vector<Artist>();
									for(int index : tableResults.getSelectedRows())
									{
										Artist a = (Artist)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										a.doRecycle();
										deleted.add(a);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Artist a : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Artist)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(a))
											{
												tableModel.removeRow(index);
												break;
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
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.ARTIST);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomajiName);
			pane.add(textRomajiName);
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
			labelRomajiName.setBounds(3, 3 + 30, 100, 15);
			textRomajiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
							if(!textRomajiName.getText().equals(""))
								query.RomajiName = textRomajiName.getText();
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	private final class IPanelCircle extends DataBaseAdapter implements LayoutManager, ActionListener
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
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
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
			labelRomajiName = new JLabel("Romaji Name");
			labelRomajiName.setFont(font);
			textRomajiName = new JTextField("");
			textRomajiName.setFont(font);
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
						final Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CIRCLE, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Circle> deleted = new Vector<Circle>();
									for(int index : tableResults.getSelectedRows())
									{
										Circle c = (Circle)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										c.doRecycle();
										deleted.add(c);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Circle c : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Circle)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(c))
											{
												tableModel.removeRow(index);
												break;
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
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CIRCLE);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomajiName);
			pane.add(textRomajiName);
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
			labelRomajiName.setBounds(3, 3 + 30, 100, 15);
			textRomajiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
							if(!textRomajiName.getText().equals(""))
								query.RomajiName = textRomajiName.getText();
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	private final class IPanelBook extends DataBaseAdapter implements LayoutManager, ActionListener
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
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
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
			labelRomajiName = new JLabel("Romaji Name");
			labelRomajiName.setFont(font);
			textRomajiName = new JTextField("");
			textRomajiName.setFont(font);
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
						final Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_BOOK, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Book> deleted = new Vector<Book>();
									for(int index : tableResults.getSelectedRows())
									{
										Book b = (Book)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										b.doRecycle();
										deleted.add(b);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Book b : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Book)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(b))
											{
												tableModel.removeRow(index);
												break;
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
								clone.setRomajiName(book.getRomajiName());
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
								WindowEx window = Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_BOOK, clone);
								window.setTitle("(Clone) " + window.getTitle());
							}
						}
					}.start();
				}
			  }
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.BOOK);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomajiName);
			pane.add(textRomajiName);
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
			labelRomajiName.setBounds(3, 3 + 30, 100, 15);
			textRomajiName.setBounds(103, 3 + 30, width - 106, 15);		
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
							if(!textRomajiName.getText().equals(""))
								query.RomajiName = textRomajiName.getText();
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	private final class IPanelContent extends DataBaseAdapter implements LayoutManager, ActionListener
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
						final Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CONTENT, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Content> deleted = new Vector<Content>();
									for(int index : tableResults.getSelectedRows())
									{
										Content t = (Content)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										t.doRecycle();
										deleted.add(t);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Content t : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Content)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(t))
											{
												tableModel.removeRow(index);
												break;
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
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CONTENT);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	private final class IPanelConvention extends DataBaseAdapter implements LayoutManager, ActionListener
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
						final Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CONVENTION, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Convention> deleted = new Vector<Convention>();
									for(int index : tableResults.getSelectedRows())
									{
										Convention e = (Convention)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										e.doRecycle();
										deleted.add(e);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Convention e : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Convention)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(e))
											{
												tableModel.removeRow(index);
												break;
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
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CONVENTION);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	private final class IPanelParody extends DataBaseAdapter implements LayoutManager, ActionListener
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
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
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
			labelRomajiName = new JLabel("Romaji Name");
			labelRomajiName.setFont(font);
			textRomajiName = new JTextField("");
			textRomajiName.setFont(font);
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
						final Record item = (Record)tableResults.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableResults.rowAtPoint(e.getPoint())), 0);
						Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_PARODY, item);
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
					final PopupMenuEx pop = new PopupMenuEx("Options", tbl);
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
									Vector<Parody> deleted = new Vector<Parody>();
									for(int index : tableResults.getSelectedRows())
									{
										Parody p = (Parody)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										p.doRecycle();
										deleted.add(p);
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									}
									for(Parody p : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Parody)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(p))
											{
												tableModel.removeRow(index);
												break;
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
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.PARODY);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			// Simulate search button press
			KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			pane.registerKeyboardAction(new ActionListener()
			    {
			    	public void actionPerformed(ActionEvent actionEvent)
			    	{
			    		buttonSearch.doClick();
			    	}
			    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomajiName);
			pane.add(textRomajiName);
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
			labelRomajiName.setBounds(3, 3 + 30, 100, 15);
			textRomajiName.setBounds(103, 3 + 30, width - 106, 15);
			labelWeblink.setBounds(3, 3 + 45, 100, 15);
			textWeblink.setBounds(103, 3 + 45, width - 106, 15);
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
							if(!textRomajiName.getText().equals(""))
								query.RomajiName = textRomajiName.getText();
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
									labelResults.setText("Found : " + tableModel.getRowCount());
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
	}
	
	@Override
	public void recordAdded(Record rcd)
	{
		child.recordAdded(rcd);
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		child.recordDeleted(rcd);
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		child.recordUpdated(rcd, data);
	}
	
	@Override
	public void databaseConnected()
	{
		child.databaseConnected();
	}
	
	@Override
	public void databaseDisconnected()
	{
		child.databaseDisconnected();
	}
	
	@Override
	public void databaseCommit()
	{
		child.databaseCommit();
	}
	
	@Override
	public void databaseRollback()
	{
		child.databaseRollback();
	}

	@Override
	public void recordRecycled(Record rcd)
	{
		child.databaseRollback();
	}

	@Override
	public void recordRestored(Record rcd)
	{
		child.databaseRollback();
	}
}