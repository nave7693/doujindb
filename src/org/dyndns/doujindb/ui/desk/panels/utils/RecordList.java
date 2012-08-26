package org.dyndns.doujindb.ui.desk.panels.utils;

import java.awt.*;
import java.awt.event.*;
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
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;

@SuppressWarnings({"serial", "rawtypes","unused"})
public final class RecordList<T extends Record> extends JPanel implements DataBaseListener, LayoutManager
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
	
	private WindowEx.Type type;
	
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
			type = WindowEx.Type.WINDOW_ARTIST;
		if(clazz.equals(Book.class))
			type = WindowEx.Type.WINDOW_BOOK;
		if(clazz.equals(Circle.class))
			type = WindowEx.Type.WINDOW_CIRCLE;
		if(clazz.equals(Content.class))
			type = WindowEx.Type.WINDOW_CONTENT;
		if(clazz.equals(Convention.class))
			type = WindowEx.Type.WINDOW_CONVENTION;
		if(clazz.equals(Parody.class))
			type = WindowEx.Type.WINDOW_PARODY;
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
		tableData.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
			{
				try {
					final Record item = (Record)tableData.getModel()
						.getValueAt(
								tableSorter.convertRowIndexToModel(
									tableData.rowAtPoint(e.getPoint())), 0);
					Core.UI.Desktop.openWindow(type, item);
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				}
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
	
	public Iterable<T> getRecords()
	{
		return tableModel.getRecords();
	}
	
	public int getRecordCount()
	{
		return tableModel.getRecordCount();
	}
	
	@Override
	public void recordAdded(Record rcd) { }
	
	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd)
	{
		//FIXME tableModel.recordUpdated(rcd);
		tableData.validate();
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		tableSorter.allRowsChanged();
	}

	@Override
	public void recordRestored(Record rcd)
	{
		tableSorter.allRowsChanged();
	}
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }
	
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
	
	private class RecordTableModel<R extends T> extends DefaultTableModel
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