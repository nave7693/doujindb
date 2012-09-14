package org.dyndns.doujindb.ui.desk.panels.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;

@SuppressWarnings({"serial", "rawtypes","unused"})
public final class RecordList<T extends Record> extends JPanel implements LayoutManager
{
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JScrollPane scrollPane;
	private JTable tableData;
	private RecordTableModel<T> tableModel;
	private RecordTableRenderer tableRenderer;
	private RecordTableEditor tableEditor;
	private TableRowSorter<RecordTableModel<T>> tableSorter;
	private RecordTableRowFilter<RecordTableModel<T>,Integer> tableFilter;
	private Hashtable<Class,ImageIcon> iconData;
	
	private String filterTerm;
	
	private Type type;
	
	private static DataFlavor flavorArtist;
	private static DataFlavor flavorBook;
	private static DataFlavor flavorCircle;
	private static DataFlavor flavorConvention;
	private static DataFlavor flavorContent;
	private static DataFlavor flavorParody;
	{
		flavorArtist = new DataFlavor("doujindb/record-artist", "DoujinDB.Record.Artist");
		flavorBook = new DataFlavor("doujindb/record-book", "DoujinDB.Record.Book");
		flavorCircle = new DataFlavor("doujindb/record-circle", "DoujinDB.Record.Circle");
		flavorConvention = new DataFlavor("doujindb/record-convention", "DoujinDB.Record.Convention");
		flavorContent = new DataFlavor("doujindb/record-content", "DoujinDB.Record.Content");
		flavorParody = new DataFlavor("doujindb/record-parody", "DoujinDB.Record.Parody");
	}
	
	private enum Type
	{
		ARTIST,
		BOOK,
		CIRCLE,
		CONTENT,
		CONVENTION,
		PARODY
	}
	
	public RecordList(Iterable<T> data, Class<?> clazz)
	{
		this(data, clazz, null);
	}
	
	public RecordList(Iterable<T> data, Class<?> clazz, Hashtable<Class,ImageIcon> icons)
	{
		super();
		super.setLayout(this);
		iconData = icons;
		if(clazz.equals(Artist.class))
			type = Type.ARTIST;
		if(clazz.equals(Book.class))
			type = Type.BOOK;
		if(clazz.equals(Circle.class))
			type = Type.CIRCLE;
		if(clazz.equals(Content.class))
			type = Type.CONTENT;
		if(clazz.equals(Convention.class))
			type = Type.CONVENTION;
		if(clazz.equals(Parody.class))
			type = Type.PARODY;
		tableData = new JTable();
		tableModel = new RecordTableModel<T>(clazz);
		tableData.setModel(tableModel);
		tableSorter = new TableRowSorter<RecordTableModel<T>>(tableModel);
		tableFilter = new RecordTableRowFilter<RecordTableModel<T>,Integer>();
		tableSorter.setRowFilter(tableFilter);
		tableData.setRowSorter(tableSorter);
		tableRenderer = new RecordTableRenderer(getBackground(), getForeground());
		tableEditor = new RecordTableEditor();
		tableData.setFont(font);
		/**
		 * The reason is that the empty table (unlike an empty list or an empty tree) does not occupy any space in the scroll pane.
		 * The JTable does not automatically stretch to fill the height of a JScrollPane's viewport — it only takes up as much vertical room as needed for the rows that it contains.
		 * So, when you drag over the empty table, you are not actually over the table and the drop fails.
		 * 
		 * @see http://docs.oracle.com/javase/tutorial/uiswing/dnd/emptytable.html
		 */
		tableData.setFillsViewportHeight(true);
		TransferHandlerEx thex;
		switch(type)
        {
        case ARTIST:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.ARTIST);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        case BOOK:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.BOOK);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        case CIRCLE:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.CIRCLE);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        case CONTENT:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.CONTENT);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        case CONVENTION:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.CONVENTION);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        case PARODY:
        	thex = new TransferHandlerEx(TransferHandlerEx.Type.PARODY);
			thex.setDragEnabled(true);
			thex.setDropEnabled(true);
			tableData.setTransferHandler(thex);
        	break;
        default:
        	return;
        }
		tableData.setDragEnabled(true);
		tableData.setDropMode(DropMode.ON);
		tableData.getTableHeader().setFont(font);
		tableData.getTableHeader().setReorderingAllowed(true);
		tableData.getColumnModel().getColumn(0).setCellRenderer(tableRenderer);
		tableData.getColumnModel().getColumn(0).setCellEditor(tableEditor);
		tableData.getColumnModel().getColumn(0).setResizable(false);
		tableData.getColumnModel().getColumn(0).setMinWidth(0);
		tableData.getColumnModel().getColumn(0).setMaxWidth(0);
		tableData.getColumnModel().getColumn(0).setWidth(0);
		for(int k = 1;k<tableData.getColumnModel().getColumnCount();k++)
		{
			tableData.getColumnModel().getColumn(k).setCellRenderer(tableRenderer);
			tableData.getColumnModel().getColumn(k).setCellEditor(tableEditor);
			tableData.getColumnModel().getColumn(k).setResizable(true);
			tableData.getColumnModel().getColumn(k).setMinWidth(125);
		}
		scrollPane = new JScrollPane(tableData);
		tableData.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {
						final Record item = (Record)tableData.getModel()
							.getValueAt(
									tableSorter.convertRowIndexToModel(
										tableData.rowAtPoint(e.getPoint())), 0);
						switch(type)
		                {
		                case ARTIST:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_ARTIST, item);
		                	break;
		                case BOOK:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_BOOK, item);
		                	break;
		                case CIRCLE:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CIRCLE, item);
		                	break;
		                case CONTENT:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CONTENT, item);
		                	break;
		                case CONVENTION:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_CONVENTION, item);
		                	break;
		                case PARODY:
		                	Core.UI.Desktop.openWindow(WindowEx.Type.WINDOW_PARODY, item);
		                	break;
		                default:
		                	return;
		                }
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(tableData.getSelectedRowCount() < 1)
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
									Vector<Record> deleted = new Vector<Record>();
									for(int index : tableData.getSelectedRows())
									{
										Record rcd = (Record)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
										deleted.add(rcd);
									}
									for(Record rcd : deleted)
										for(int index=0; index<tableModel.getRowCount();index++)
											if(((Record)tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0)).equals(rcd))
											{
												tableModel.removeRow(index);
												break;
											}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								tableData.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
			});
		for(Record rcd : data)
			tableModel.addRecord(rcd);
		add(scrollPane);
   		setVisible(true);
	}
	
	@Override
	public void addMouseListener(MouseListener listener)
	{
		tableData.addMouseListener(listener);
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
	public void setEnabled(boolean enabled)
	{
		tableData.setDragEnabled(enabled);
		tableData.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public Iterable<T> getRecords()
	{
		return tableModel.getRecords();
	}
	
	public void addRecord(T rcd)
	{
		tableModel.addRecord(rcd);
		tableData.validate();
	}
	
	public void removeRecord(T rcd)
	{
		tableModel.removeRecord(rcd);
		tableData.validate();
	}
	
	public void recordsChanged()
	{
		tableSorter.allRowsChanged();
	}
	
	public int getRecordCount()
	{
		return tableModel.getRecordCount();
	}
	
	public void filterChanged(String term)
	{
		this.filterTerm = term;
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
	
	@SuppressWarnings("all")
	private final class RecordTableRowFilter<M extends RecordTableModel<T>, I extends Integer> extends RowFilter<M, I>
	{

		@Override
		public boolean include(javax.swing.RowFilter.Entry<? extends M, ? extends I> entry)
		{
			Record rcd = (Record) entry.getModel().getValueAt(entry.getIdentifier(), 0);
			return (!rcd.isRecycled());
		}
	}
	
	class RecordTableModel<R extends T> extends DefaultTableModel
	{
		public RecordTableModel(Class<?> clazz)
		{
			super();
			if(clazz == Artist.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
		    }
			if(clazz == Book.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
		    }
			if(clazz == Circle.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
		    }
			if(clazz == Convention.class)
		    {
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
		    }
			if(clazz == Content.class)
		    {
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
		    }
			if(clazz == Parody.class)
		    {
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
		    }
		}

		@SuppressWarnings("unchecked")
		public int getRecordCount()
		{
			/**
			 * We don't want to count in 'recycled' records
			 */
			//return super.getRowCount();
			int count = 0;
			for(int i=0;i<tableModel.getRowCount();i++)
				if(!((T)tableModel.getValueAt(i, 0)).isRecycled())
					count++;
			return count;
		}

		@SuppressWarnings("unchecked")
		public boolean containsRecord(Record rcd)
		{
			for(int i=0;i<tableModel.getRowCount();i++)
				if(((T)tableModel.getValueAt(i, 0)).equals(rcd))
					return true;
			return false;
		}

		@SuppressWarnings("unchecked")
		public Iterable<T> getRecords()
		{
			Vector<T> items = new Vector<T>();
			for(int i=0;i<tableModel.getRowCount();i++)
				items.add((T)tableModel.getValueAt(i, 0));
			return items;
		}
		
		public void removeRecord(Record record)
		{
			for(int i=0;i<tableModel.getRowCount();i++)
				if(tableModel.getValueAt(i, 0).equals(record))
				{
					tableModel.removeRow(i);
					return;
				}
		}

		public void addRecord(Record record)
		{
			if(containsRecord(record))
				return;
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
}