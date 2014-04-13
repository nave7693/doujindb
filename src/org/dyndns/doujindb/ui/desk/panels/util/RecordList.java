package org.dyndns.doujindb.ui.desk.panels.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.desk.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class RecordList<T extends Record> extends JPanel implements LayoutManager
{
	private JScrollPane scrollPane;
	private JTable tableData;
	private JPopupMenu m_PopupAction;
	
	private RecordTableModel<T> tableModel;
	private RecordTableRenderer tableRenderer;
	private RecordTableEditor tableEditor;
	private TableRowSorter<RecordTableModel<T>> tableSorter;
	private RecordTableRowFilter<RecordTableModel<T>,Integer> tableFilter;
	
	private String filterRegex;
	
	protected static final Font font = UI.Font;
	
	private Type m_Type;
	
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
		super();
		super.setLayout(this);
		
		m_PopupAction = new JPopupMenu();
		
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
		if(clazz.equals(Artist.class))
			m_Type = Type.ARTIST;
		if(clazz.equals(Book.class))
			m_Type = Type.BOOK;
		if(clazz.equals(Circle.class))
			m_Type = Type.CIRCLE;
		if(clazz.equals(Content.class))
			m_Type = Type.CONTENT;
		if(clazz.equals(Convention.class))
			m_Type = Type.CONVENTION;
		if(clazz.equals(Parody.class))
			m_Type = Type.PARODY;
		switch(m_Type)
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
        	throw new IllegalArgumentException("Invalid Class<?> provided : " + clazz);
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
			public void mouseClicked(MouseEvent me)
			{
				if(me.getClickCount() == 2 &&
					me.getButton() == MouseEvent.BUTTON1)
				{
					try {
						final Record item = (Record) tableData.getModel().getValueAt(tableSorter.convertRowIndexToModel(tableData.rowAtPoint(me.getPoint())), 0);
						switch(m_Type)
		                {
		                case ARTIST:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, item);
		                	break;
		                case BOOK:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, item);
		                	break;
		                case CIRCLE:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, item);
		                	break;
		                case CONTENT:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, item);
		                	break;
		                case CONVENTION:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, item);
		                	break;
		                case PARODY:
		                	UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, item);
		                	break;
		                default:
		                	return;
		                }
					} catch (DataBaseException dbe) {
						Logger.logError(dbe.getMessage(), dbe);
					}
				}
			}
			public void mousePressed(MouseEvent me)
		    {
				popup(me);
		    }
			public void mouseReleased(MouseEvent me)
		    {
				popup(me);
		    }
			@SuppressWarnings("unchecked")
			private void popup(MouseEvent me)
		    {
		    	if (me.isPopupTrigger())
		    	{
		    		// Reset PopupMenu
		    		m_PopupAction.removeAll();
		    		
		    		// Get all selected Records
		    		final Vector<T> selected = new Vector<T>();
					for(int index : tableData.getSelectedRows())
					{
						T record = (T) tableModel.getValueAt(tableSorter.convertRowIndexToModel(index), 0);
						selected.add(record);
					}
		    		
		    		// If no data is selected don't show the PopupMenu
		    		if(selected.isEmpty())
		    			return;
		    		
					JMenuItem menuItem = new JMenuItem("Remove", Icon.desktop_explorer_remove);
					menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							new SwingWorker<Void,T>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									for(T record : selected)
										publish(record);
									return null;
								}
								@Override
								protected void process(List<T> chunks) {
									for (T record : chunks) {
							        	for(int index=0; index<tableModel.getRowCount();index++)
											if(((T)tableModel.getValueAt(index, 0)).equals(record))
											{
												tableModel.removeRow(index);
												break;
											}
							         }
							     }
								@Override
								protected void done()
								{
									tableModel.fireTableDataChanged();
								}
							}.execute();
						}
					});
					menuItem.setName("remove");
					menuItem.setActionCommand("remove");
					m_PopupAction.add(menuItem);
					if(selected.size() == 1 && selected.get(0) instanceof Book)
					{
						menuItem = new JMenuItem("Clone", Icon.desktop_explorer_clone);
						menuItem.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent ae)
							{
								new SwingWorker<Void,Book>()
								{
									@Override
									protected Void doInBackground() throws Exception
									{
										for(T record : selected)
										{
											Book book = (Book) record;
											Book clone = DataBase.doInsert(Book.class);
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
											for(Circle c : book.getCircles())
												clone.addCircle(c);
											for(Content c : book.getContents())
												clone.addContent(c);
											for(Parody p : book.getParodies())
												clone.addParody(p);
											if(DataBase.isAutocommit())
												DataBase.doCommit();
											publish(clone);
										}
										return null;
									}
									@Override
									protected void process(List<Book> chunks) {
										for (Book book : chunks)
										{
											WindowEx window = UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
											window.setTitle("(Clone) " + window.getTitle());
										}
								    }
								}.execute();
							}
						});
						menuItem.setName("clone");
						menuItem.setActionCommand("clone");
						m_PopupAction.add(menuItem);
					}
		            m_PopupAction.show(me.getComponent(), me.getX(), me.getY());
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
	
	public boolean filterChanged(String regex)
	{
		try
		{
			Pattern.compile(regex);
			this.filterRegex = regex;
			tableModel.fireTableDataChanged();
			return true;
		} catch (PatternSyntaxException | NullPointerException e)
		{
			return false;
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
	
	@SuppressWarnings("all")
	private final class RecordTableRowFilter<M extends RecordTableModel<T>, I extends Integer> extends RowFilter<M, I>
	{

		@Override
		public boolean include(javax.swing.RowFilter.Entry<? extends M, ? extends I> entry)
		{
			String regex = filterRegex == null ? ".*" : filterRegex;
			switch(m_Type)
            {
            case ARTIST:
            	Artist a = (Artist) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(a.isRecycled())
            		return false;
            	return (a.getJapaneseName().matches(regex) ||
            			a.getTranslatedName().matches(regex) ||
            			a.getRomajiName().matches(regex));
            case BOOK:
            	Book b = (Book) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(b.isRecycled())
            		return false;
            	return (b.getJapaneseName().matches(regex) ||
            			b.getTranslatedName().matches(regex) ||
            			b.getRomajiName().matches(regex));
            case CIRCLE:
            	Circle c = (Circle) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(c.isRecycled())
            		return false;
            	return (c.getJapaneseName().matches(regex) ||
            			c.getTranslatedName().matches(regex) ||
            			c.getRomajiName().matches(regex));
            case CONTENT:
            	Content t = (Content) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(t.isRecycled())
            		return false;
            	return (t.getTagName().matches(regex) ||
            			t.getInfo().matches(regex));
            case CONVENTION:
            	Convention e = (Convention) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(e.isRecycled())
            		return false;
            	return (e.getTagName().matches(regex) ||
            			e.getInfo().matches(regex));
            case PARODY:
            	Parody p = (Parody) entry.getModel().getValueAt(entry.getIdentifier(), 0);
            	if(p.isRecycled())
            		return false;
            	return (p.getJapaneseName().matches(regex) ||
            			p.getTranslatedName().matches(regex) ||
            			p.getRomajiName().matches(regex));
            default:
            	return false;
            }
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