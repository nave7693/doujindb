package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.panels.util.TransferHandlerEx;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public abstract class PanelSearch<T extends Record> extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	protected JTabbedPane m_Tab;
	protected int m_Index;
	
	protected SearchWorker<T> m_Worker;
	
	protected RecordTableModel<T> m_TableModel;
	protected RecordTableRenderer m_TableRenderer;
	protected RecordTableEditor m_TableEditor;
	protected TableRowSorter<DefaultTableModel> m_TableSorter;
	protected JButton m_ButtonSearch;
	protected JLabel m_LabelResults;
	
	private static DialogSearch m_PopupDialog = null;
	
	protected static final Font font = UI.Font;
	
	public PanelSearch(JTabbedPane tab, int index)
	{
		this.m_Tab = tab;
		this.m_Index = index;
		
		this.m_LabelResults = new JLabel("Found");
		this.m_LabelResults.setFont(font);
		super.add(m_LabelResults);
		this.m_ButtonSearch = new JButton("Search");
		this.m_ButtonSearch.setMnemonic('S');
		this.m_ButtonSearch.setFocusable(false);
		this.m_ButtonSearch.addActionListener(this);
		super.add(m_ButtonSearch);
		
		// Simulate search button press
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		super.registerKeyboardAction(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				m_ButtonSearch.doClick();
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
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
	
	private static abstract class SearchWorker<R extends Record> extends SwingWorker<Void, R>
	{
		protected Query<R> query;
		
		private SearchWorker(Query<R> query)
		{
			this.query = query;
		}
	}
	
	private static final class RecordTableRenderer extends DefaultTableCellRenderer
	{
		private Color background;
		private Color foreground;
		
		public RecordTableRenderer(Color background, Color foreground)
		{
		    this.background = background;
		    this.foreground = foreground;
		}
	
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
		    super.setBorder(null);
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
	
	private static abstract class RecordTableModel<R extends Record> extends DefaultTableModel
	{
		private static final class IArtist extends RecordTableModel<Artist>
		{
			private IArtist()
			{
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
			}
			
			@Override
			public void addRecord(Artist a)
			{
				super.addRow(new Object[]{a,
					a.getJapaneseName(),
					a.getTranslatedName(),
					a.getRomajiName()});
			}
		}
		private static final class IBook extends RecordTableModel<Book>
		{
			private IBook()
			{
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
			}
			
			@Override
			public void addRecord(Book b)
			{
				super.addRow(new Object[]{b,
					b.getJapaneseName(),
					b.getTranslatedName(),
					b.getRomajiName()});
			}
		}
		private static final class ICircle extends RecordTableModel<Circle>
		{
			private ICircle()
			{
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
			}
			
			@Override
			public void addRecord(Circle c)
			{
				super.addRow(new Object[]{c,
					c.getJapaneseName(),
					c.getTranslatedName(),
					c.getRomajiName()});
			}
		}
		private static final class IContent extends RecordTableModel<Content>
		{
			private IContent()
			{
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
			}
			
			@Override
			public void addRecord(Content t)
			{
				super.addRow(new Object[]{t,
					t.getTagName(),
					t.getInfo()});
			}
		}
		private static final class IConvention extends RecordTableModel<Convention>
		{
			private IConvention()
			{
				addColumn("");
				addColumn("Tag Name");
				addColumn("Information");
			}
			
			@Override
			public void addRecord(Convention e)
			{
				super.addRow(new Object[]{e,
					e.getTagName(),
					e.getInfo()});
			}
		}
		private static final class IParody extends RecordTableModel<Parody>
		{
			private IParody()
			{
				addColumn("");
				addColumn("Japanese");
				addColumn("Translated");
				addColumn("Romaji");
			}
			
			public void addRecord(Parody p)
			{
				super.addRow(new Object[]{p,
					p.getJapaneseName(),
					p.getTranslatedName(),
					p.getRomajiName()});
			}
		}
		public RecordTableModel() { }
		
		public void addRecord(R record) { }
	}
	
	private static final class RecordTableEditor extends AbstractCellEditor implements TableCellEditor
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
	public void recordAdded(Record rcd)
	{
		//TODO
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		//TODO
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		//TODO
	}
	
	@Override
	public void databaseConnected()
	{
		//TODO
	}
	
	@Override
	public void databaseDisconnected()
	{
		//TODO
	}
	
	@Override
	public void databaseCommit()
	{
		//TODO
	}
	
	@Override
	public void databaseRollback()
	{
		//TODO
	}

	@Override
	public void recordRecycled(Record rcd)
	{
		//TODO
	}

	@Override
	public void recordRestored(Record rcd)
	{
		//TODO
	}
	
	public static final class IArtist extends PanelSearch<Artist>
	{
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public IArtist(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
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
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.IArtist();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(IArtist.this,
								"<html>" +
								"<body>" +
								"Move selected items to the Trash?<br/>" +
								"</body>" +
								"</html>", new SwingWorker<Void,Iterable<Artist>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Artist> selected = new Vector<Artist>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										if(super.isCancelled())
											break;
										try
										{
											Artist o = (Artist) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Artist>> data) {
									for(Iterable<Artist> i : data)
										for(Artist o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.ARTIST);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchArtist(null);
			m_Worker.cancel(true);
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
			m_LabelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryArtist q = new QueryArtist();
				if(!textJapaneseName.getText().equals(""))
					q.JapaneseName = textJapaneseName.getText();
				if(!textTranslatedName.getText().equals(""))
					q.TranslatedName = textTranslatedName.getText();
				if(!textRomajiName.getText().equals(""))
					q.RomajiName = textRomajiName.getText();
				if(!textWeblink.getText().equals(""))
					q.Weblink = textWeblink.getText();
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchArtist(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchArtist extends SearchWorker<Artist>
		{
			private SearchArtist(Query<Artist> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Artist> result = Core.Database.getArtists((QueryArtist) query);
				for(Artist o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Artist> data) {
				for(Artist o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_artist);
			}
		}
	}
	
	public static final class IBook extends PanelSearch<Book>
	{
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
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public IBook(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
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
			comboType.addItem(null);
			for(Book.Type tokenType : Book.Type.values())
				comboType.addItem(tokenType);
			comboType.setSelectedItem(null);
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
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.IBook();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(IBook.this,
									"<html>" +
									"<body>" +
									"Move selected items to the Trash?<br/>" +
									"</body>" +
									"</html>", new SwingWorker<Void,Iterable<Book>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Book> selected = new Vector<Book>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										try
										{
											Book o = (Book) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									try
									{
										if(Core.Database.isAutocommit())
											Core.Database.doCommit();
									} catch (DataBaseException dbe)
									{
										Logger.logError(dbe.getMessage(), dbe);
									} catch (Exception e)
									{
										Logger.logError(e.getMessage(), e);
									}
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Book>> data) {
									for(Iterable<Book> i : data)
										for(Book o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.BOOK);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelType);
			super.add(comboType);
			super.add(checkAdult);
			super.add(checkDecensored);
			super.add(checkTranslated);
			super.add(checkColored);
			super.add(scrollResults);
			
			m_Worker = new SearchBook(null);
			m_Worker.cancel(true);
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
			m_LabelResults.setBounds(3, 3 + 130, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 145, width - 5, height - 175);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryBook q = new QueryBook();
				if(!textJapaneseName.getText().equals(""))
					q.JapaneseName = textJapaneseName.getText();
				if(!textTranslatedName.getText().equals(""))
					q.TranslatedName = textTranslatedName.getText();
				if(!textRomajiName.getText().equals(""))
					q.RomajiName = textRomajiName.getText();
				q.Type = (org.dyndns.doujindb.db.records.Book.Type) comboType.getSelectedItem();
				if(checkAdult.isSelected())
					q.Adult = true;
				if(checkColored.isSelected())
					q.Colored = true;
				if(checkTranslated.isSelected())
					q.Translated = true;
				if(checkDecensored.isSelected())
					q.Decensored = true;
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchBook(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchBook extends SearchWorker<Book>
		{
			private SearchBook(Query<Book> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Book> result = Core.Database.getBooks((QueryBook) query);
				for(Book o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Book> data) {
				for(Book o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_book);
			}
		}
	}
	
	public static final class ICircle extends PanelSearch<Circle>
	{
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public ICircle(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
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
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.ICircle();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(ICircle.this,
									"<html>" +
									"<body>" +
									"Move selected items to the Trash?<br/>" +
									"</body>" +
									"</html>", new SwingWorker<Void,Iterable<Circle>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Circle> selected = new Vector<Circle>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										try
										{
											Circle o = (Circle) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Circle>> data) {
									for(Iterable<Circle> i : data)
										for(Circle o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CIRCLE);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchCircle(null);
			m_Worker.cancel(true);
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
			m_LabelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryCircle q = new QueryCircle();
				if(!textJapaneseName.getText().equals(""))
					q.JapaneseName = textJapaneseName.getText();
				if(!textTranslatedName.getText().equals(""))
					q.TranslatedName = textTranslatedName.getText();
				if(!textRomajiName.getText().equals(""))
					q.RomajiName = textRomajiName.getText();
				if(!textWeblink.getText().equals(""))
					q.Weblink = textWeblink.getText();
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchCircle(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchCircle extends SearchWorker<Circle>
		{
			private SearchCircle(Query<Circle> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Circle> result = Core.Database.getCircles((QueryCircle) query);
				for(Circle o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Circle> data) {
				for(Circle o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_circle);
			}
		}
	}
	
	public static final class IContent extends PanelSearch<Content>
	{
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public IContent(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField("");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.IContent();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(IContent.this,
									"<html>" +
									"<body>" +
									"Move selected items to the Trash?<br/>" +
									"</body>" +
									"</html>", new SwingWorker<Void,Iterable<Content>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Content> selected = new Vector<Content>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										try
										{
											Content o = (Content) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Content>> data) {
									for(Iterable<Content> i : data)
										for(Content o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CONTENT);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelTagName);
			super.add(textTagName);
			super.add(labelResults);
			super.add(scrollResults);
			
			m_Worker = new SearchContent(null);
			m_Worker.cancel(true);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			m_LabelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryContent q = new QueryContent();
				if(!textTagName.getText().equals(""))
					q.TagName = textTagName.getText();
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchContent(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchContent extends SearchWorker<Content>
		{
			private SearchContent(Query<Content> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Content> result = Core.Database.getContents((QueryContent) query);
				for(Content o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Content> data) {
				for(Content o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_parody);
			}
		}
	}
	
	public static final class IConvention extends PanelSearch<Convention>
	{
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public IConvention(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField("");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.IConvention();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(IConvention.this,
									"<html>" +
									"<body>" +
									"Move selected items to the Trash?<br/>" +
									"</body>" +
									"</html>", new SwingWorker<Void,Iterable<Convention>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Convention> selected = new Vector<Convention>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										try
										{
											Convention o = (Convention) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Convention>> data) {
									for(Iterable<Convention> i : data)
										for(Convention o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.CONVENTION);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelTagName);
			super.add(textTagName);
			super.add(labelResults);
			super.add(scrollResults);
			
			m_Worker = new SearchConvention(null);
			m_Worker.cancel(true);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			m_LabelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryConvention q = new QueryConvention();
				if(!textTagName.getText().equals(""))
					q.TagName = textTagName.getText();
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchConvention(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchConvention extends SearchWorker<Convention>
		{
			private SearchConvention(Query<Convention> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Convention> result = Core.Database.getConventions((QueryConvention) query);
				for(Convention o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Convention> data) {
				for(Convention o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_convention);
			}
		}
	}
	
	public static final class IParody extends PanelSearch<Parody>
	{
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomajiName;
		private JTextField textRomajiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JTable tableResults;
		private JScrollPane scrollResults;
		
		public IParody(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
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
			tableResults = new JTable();
			
			m_TableModel = new RecordTableModel.IParody();
			tableResults.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			tableResults.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			tableResults.setFont(font);
			tableResults.getTableHeader().setFont(font);
			tableResults.getTableHeader().setReorderingAllowed(true);
			tableResults.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			tableResults.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			tableResults.getColumnModel().getColumn(0).setResizable(false);
			tableResults.getColumnModel().getColumn(0).setMinWidth(0);
			tableResults.getColumnModel().getColumn(0).setMaxWidth(0);
			tableResults.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<tableResults.getColumnModel().getColumnCount();k++)
			{
				tableResults.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				tableResults.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				tableResults.getColumnModel().getColumn(k).setResizable(true);
				tableResults.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(tableResults);
			tableResults.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) tableResults.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										tableResults.rowAtPoint(me.getPoint())), 0);
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, item);
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
						}
						return;
					}
					checkPopup(me);
				}

				@Override
				public void mousePressed(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					checkPopup(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					checkPopup(me);
				}
				
				private void checkPopup(MouseEvent me)
				{
					if(!me.isPopupTrigger())
						return;
					
					// If not item is selected don't show any popup
					if(tableResults.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_PopupDialog = new DialogSearch(IParody.this,
									"<html>" +
									"<body>" +
									"Move selected items to the Trash?<br/>" +
									"</body>" +
									"</html>", new SwingWorker<Void,Iterable<Parody>>()
							{
								@Override
								protected Void doInBackground() throws Exception
								{
									int cSelected, cProcessed;
									Vector<Parody> selected = new Vector<Parody>();
									
									cProcessed = 0;
									cSelected = tableResults.getSelectedRowCount();
									
									for(int index : tableResults.getSelectedRows())
									{
										try
										{
											Parody o = (Parody) m_TableModel.getValueAt(m_TableSorter.convertRowIndexToModel(index), 0);
											o.doRecycle();
											super.setProgress(100 * ++cProcessed / cSelected);
											selected.add(o);
										} catch (DataBaseException dbe)
										{
											Logger.logError(dbe.getMessage(), dbe);
										}
									}
									if(Core.Database.isAutocommit())
										Core.Database.doCommit();
									publish(selected);
									return null;
								}
								@Override
								protected void process(java.util.List<Iterable<Parody>> data) {
									for(Iterable<Parody> i : data)
										for(Parody o : i)
											for(int index=0; index<m_TableModel.getRowCount(); index++)
												if((m_TableModel.getValueAt(index, 0)).equals(o))
												{
													m_TableModel.removeRow(index);
													break;
												}
									m_TableModel.fireTableDataChanged();
							    }
								@Override
								protected void done() {
									tableResults.clearSelection();
									m_PopupDialog.dispose();
							    }
							});
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			tableResults.setDragEnabled(true);
			TransferHandlerEx thex = new TransferHandlerEx(TransferHandlerEx.Type.PARODY);
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			tableResults.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchParody(null);
			m_Worker.cancel(true);
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
			m_LabelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone())
			{
				// Prepare the Query to be run
				QueryParody q = new QueryParody();
				if(!textJapaneseName.getText().equals(""))
					q.JapaneseName = textJapaneseName.getText();
				if(!textTranslatedName.getText().equals(""))
					q.TranslatedName = textTranslatedName.getText();
				if(!textRomajiName.getText().equals(""))
					q.RomajiName = textRomajiName.getText();
				if(!textWeblink.getText().equals(""))
					q.Weblink = textWeblink.getText();
				
				// Clean result
				while(m_TableModel.getRowCount()>0)
					m_TableModel.removeRow(0);
				
				// UI feedback
				m_ButtonSearch.setText("Cancel");
				m_ButtonSearch.setMnemonic('C');
				m_Tab.setIconAt(m_Index, Icon.window_loading);
				
				// Run the Worker
				m_Worker = new SearchParody(q);
				m_Worker.execute();
			} else {
				m_Worker.cancel(true);
			}
		}
		
		private final class SearchParody extends SearchWorker<Parody>
		{
			private SearchParody(Query<Parody> query)
			{
				super(query);
			}
			
			@Override
			protected Void doInBackground() {
				RecordSet<Parody> result = Core.Database.getParodies((QueryParody) query);
				for(Parody o : result)
				{
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Parody> data) {
				for(Parody o : data)
					m_TableModel.addRecord(o);
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				if(query == null)
					return;
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_parody);
			}
		}
	}
	
	private static final class DialogSearch extends JInternalFrame implements LayoutManager
	{
		private JComponent m_GlassPane;
		private JComponent m_Component;
		private JLabel m_LabelMessage;
		private JButton m_ButtonOk;
		private JButton m_ButtonCancel;
		private JProgressBar m_ProgressBar;
		
		private SwingWorker<?,?> m_Worker;
		
		public DialogSearch(JComponent parent, String message, SwingWorker<?,?> worker)
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_trash);
			super.setTitle("Trash");
			super.setMaximizable(false);
			super.setIconifiable(false);
			super.setResizable(false);
			super.setClosable(false);
			super.setPreferredSize(new Dimension(300, 150));
			super.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			super.addInternalFrameListener(new InternalFrameAdapter()
			{
				@Override
				public void internalFrameClosed(InternalFrameEvent ife)
				{
					hideDialog();
				}

				@Override
				public void internalFrameClosing(InternalFrameEvent ife)
				{
					hideDialog();
				}
			});
			
			m_GlassPane = (JComponent) ((RootPaneContainer) parent.getRootPane().getParent()).getGlassPane();
			
			m_Worker = worker;
			m_Worker.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						m_ProgressBar.setValue((Integer) evt.getNewValue());
						return;
					}
				}
			});
			
			m_Component = new JPanel();
			m_Component.setSize(250, 150);
			m_Component.setLayout(new GridLayout(3, 1));
			m_LabelMessage = new JLabel(message);
			m_LabelMessage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			m_LabelMessage.setVerticalAlignment(JLabel.CENTER);
			m_LabelMessage.setHorizontalAlignment(JLabel.CENTER);
			m_LabelMessage.setFont(font);
			m_Component.add(m_LabelMessage);
			
			m_ProgressBar = new JProgressBar();
			m_ProgressBar.setValue(0);
			m_ProgressBar.setMinimum(0);
			m_ProgressBar.setMaximum(100);
			m_ProgressBar.setStringPainted(true);
			m_ProgressBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			m_Component.add(m_ProgressBar);
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridLayout(1, 2));
			m_ButtonCancel = new JButton("Cancel");
			m_ButtonCancel.setFont(font);
			m_ButtonCancel.setMnemonic('C');
			m_ButtonCancel.setFocusable(false);
			m_ButtonCancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					m_Worker.cancel(true);
					dispose();
				}					
			});
			m_ButtonOk = new JButton("Ok");
			m_ButtonOk.setFont(font);
			m_ButtonOk.setMnemonic('O');
			m_ButtonOk.setFocusable(false);
			m_ButtonOk.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					m_ButtonOk.setEnabled(false);
					m_Worker.execute();
				}					
			});
			bottomPanel.add(m_ButtonOk);
			bottomPanel.add(m_ButtonCancel);
			m_Component.add(bottomPanel);
			super.add(m_Component);
			super.setVisible(true);
			
			showDialog();
		}
		
		
		private void showDialog()
		{
			m_GlassPane.add(this);
			m_GlassPane.setEnabled(true);
			m_GlassPane.setVisible(true);
			m_GlassPane.setEnabled(false);
			Dimension size = super.getPreferredSize();
			int x = (int) (m_GlassPane.getWidth() - size.getWidth()) / 2;
			int y = (int) (m_GlassPane.getHeight() - size.getHeight()) / 2;
			setBounds(x, y, (int) size.getWidth(), (int) size.getHeight());
			try {
				setSelected(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
		}
		
		private void hideDialog()
		{
			m_GlassPane.remove(this);
			m_GlassPane.setEnabled(true);
			m_GlassPane.setVisible(false);
			m_GlassPane.setEnabled(false);
		}

		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_Component.setBounds(0, 0, width, height);
		}
		
		@Override
		public void addLayoutComponent(String key,Component c) {}
		
		@Override
		public void removeLayoutComponent(Component c) {}
		
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return getMinimumSize();
		}
		
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
			return getPreferredSize();
		}
	}
}