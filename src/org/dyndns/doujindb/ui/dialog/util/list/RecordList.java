package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.combobox.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public abstract class RecordList<T extends Record> extends JPanel implements DataBaseListener, LayoutManager
{
	protected JScrollPane scrollPane;
	protected JTable tableData;
	protected RecordTableModel<T> tableModel;
	protected RecordTableRenderer tableRenderer;
	protected RecordTableEditor tableEditor;
	protected TableRowSorter<RecordTableModel<T>> tableSorter;
	
	protected Iterator<T> rowsData;
	protected final int rowsPageSize = 25;

	protected SearchComboBox<T> searchComboBox;
	protected JButton addRecord;

	protected JPopupMenu popupAction;
	
	protected static final Font font = UI.Font;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(RecordList.class);
	
	public RecordList(RecordSet<T> data)
	{
		super();
		super.setLayout(this);
		
		rowsData = data.iterator();
		
		popupAction = new JPopupMenu();
		
		addRecord = new JButton(Icon.window_tab_explorer_add);
		addRecord.setBorder(null);
		addRecord.setFocusable(false);
		
		tableData = new JTable();
		tableModel = getModel();
		tableData.setModel(tableModel);
		tableSorter = new TableRowSorter<RecordTableModel<T>>(tableModel);
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
		
		registerTransferHandler();
		
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
		tableData.addMouseListener(new MouseAdapter()
		{
			@SuppressWarnings("unchecked")
			public void mouseClicked(MouseEvent me)
			{
				if(me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1)
				{
					try {
						openRecordWindow((T) tableData.getModel().getValueAt(tableSorter.convertRowIndexToModel(tableData.rowAtPoint(me.getPoint())), 0));
					} catch (DataBaseException dbe) {
						LOG.error("Error displaying record Window", dbe);
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
		    		popupAction.removeAll();
		    		
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
					popupAction.add(menuItem);
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
						popupAction.add(menuItem);
					}
					popupAction.show(me.getComponent(), me.getX(), me.getY());
		        }
		    }
		});
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent ae) {
		        int extent = scrollPane.getVerticalScrollBar().getModel().getExtent();
		        if((scrollPane.getVerticalScrollBar().getValue() + extent) == scrollPane.getVerticalScrollBar().getMaximum())
		        	loadData();
		    }
		});
		
		add(scrollPane);
   		setVisible(true);
	}
	
	protected void loadData()
	{
		new SwingWorker<Void, T>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				for(int index = 0; index < rowsPageSize; index++)
					if(rowsData.hasNext())
						publish(rowsData.next());
					else
						break;
				return null;
			}
			@Override
			protected void process(List<T> chunks) {
				for(T record : chunks)
					addRecord(record);
			}
		}.execute();
	}
	
	protected abstract void openRecordWindow(T record);
	
	protected abstract void registerTransferHandler();
	
	protected abstract RecordTableModel<T> getModel();
	
	@Override
	public void addMouseListener(MouseListener listener)
	{
		tableData.addMouseListener(listener);
	}
	
	@Override
	public void addLayoutComponent(String name, Component c) {}

	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		searchComboBox.setBounds(0, 0, width - 21, 20);
		addRecord.setBounds(width - 20, 0, 20, 20);
		scrollPane.setBounds(0, 21, width, height - 20);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(250, 150);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
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
	
	public void addRecord(T record)
	{
		tableModel.addRecord(record);
	}
	
	public void removeRecord(T record)
	{
		tableModel.removeRecord(record);
	}
	
	public void recordsChanged()
	{
		tableSorter.allRowsChanged();
	}
	
	public int getRecordCount()
	{
		return tableModel.getRecordCount();
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
	
	public abstract class RecordTableRowFilter<M extends RecordTableModel<T>> extends RowFilter<M, Integer>
	{

	}
	
	public abstract class RecordTableModel<R extends T> extends DefaultTableModel
	{
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

		abstract public void addRecord(R record);
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
	
	@Override
	public void recordAdded(Record r) { }
	
	@Override
	public void recordDeleted(Record r) { }
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }

	@Override
	public void recordRecycled(Record r)
	{
		recordsChanged();
	}

	@Override
	public void recordRestored(Record r)
	{
		recordsChanged();
	}
}
