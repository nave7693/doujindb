package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.ui.DialogEx;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.*;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxContent;
import org.dyndns.doujindb.ui.dialog.util.dnd.*;
import org.dyndns.doujindb.util.ImageTool;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public abstract class PanelSearch<T extends Record> extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	protected JTabbedPane m_Tab;
	protected int m_Index;
	
	protected SearchWorker<T> m_Worker;
	
	protected JTable m_Table;
	protected RecordTableModel<T> m_TableModel;
	protected RecordTableRenderer m_TableRenderer;
	protected RecordTableEditor m_TableEditor;
	protected TableRowSorter<DefaultTableModel> m_TableSorter;
	protected JButton m_ButtonSearch;
	protected JLabel m_LabelResults;
	
	private static final Font font = UI.Font;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PanelSearch.class);
	
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
			m_Table = new JTable();
			
			m_TableModel = new RecordTableModel.IArtist();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Artist Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Artist>(IArtist.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerArtist thex = new TransferHandlerArtist();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchArtist(new QueryArtist());
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
			if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
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
				RecordSet<Artist> result = DataBase.getArtists((QueryArtist) query);
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
		private DynamicListContent listSearchContent;
		private JScrollPane scrollResults;
		private JPanel recordPreview;
		private JScrollPane scrollRecordPreview;
		private boolean previewToggled = false;
		private boolean previewEnabled = (boolean) Configuration.configRead("org.dyndns.doujindb.ui.book_preview");
		private JButton toggleList;
		private JButton togglePreview;
		
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
			checkAdult = new JCheckBox("Adult", false);
			checkAdult.setFont(font);
			checkAdult.setFocusable(false);
			
			listSearchContent = new DynamicListContent();
					
			toggleList = new JButton(Icon.desktop_explorer_table_view_list);
			toggleList.setToolTipText("Toggle List");
			toggleList.addActionListener(this);
			toggleList.setFocusable(false);
			recordPreview = new JPanel();
			recordPreview.setLayout(new WrapLayout());
			scrollRecordPreview = new JScrollPane(recordPreview);
			scrollRecordPreview.getVerticalScrollBar().setUnitIncrement(25);
			scrollRecordPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			togglePreview = new JButton(Icon.desktop_explorer_table_view_preview);
			togglePreview.setToolTipText("Toggle Preview");
			togglePreview.addActionListener(this);
			togglePreview.setFocusable(false);
			
			m_Table = new JTable();
			m_TableModel = new RecordTableModel.IBook();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Book Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Book>(IBook.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerBook thex = new TransferHandlerBook();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelType);
			super.add(comboType);
			super.add(checkAdult);
			super.add(listSearchContent);
			super.add(scrollResults);
			if(previewEnabled)
			{
				super.add(toggleList);
				super.add(togglePreview);
				super.add(scrollRecordPreview);
			}
			
			m_Worker = new SearchBook(new QueryBook());
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
			checkAdult.setBounds(width - 105, 3 + 55, 100, 15);
			listSearchContent.setBounds(3, 3 + 65, width - 110, 95);
			m_LabelResults.setBounds(3, 3 + 160, width / 2 - 6, 15);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
			if(!previewToggled)
			{
				toggleList.setBounds(0, 0, 0, 0);
				scrollResults.setBounds(3, 3 + 175, width - 5, height - 205);
				togglePreview.setBounds(width - 22, 3 + 155, 20, 20);
				scrollRecordPreview.setBounds(0, 0, 0, 0);
			} else {
				toggleList.setBounds(width - 22, 3 + 155, 20, 20);
				scrollResults.setBounds(0, 0, 0, 0);
				togglePreview.setBounds(0, 0, 0, 0);
				scrollRecordPreview.setBounds(3, 3 + 175, width - 5, height - 205);
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(ae.getSource().equals(toggleList))
			{
				previewToggled = false;
				doLayout();
				toggleList.validate();
				return;
			}
			if(ae.getSource().equals(togglePreview))
			{
				previewToggled = true;
				doLayout();
				scrollRecordPreview.validate();
				return;
			}
			if(ae.getSource().equals(m_ButtonSearch))
			{
				if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
				{
					// Prepare the Query to be run
					QueryBook q = new QueryBook();
					if(!textJapaneseName.getText().equals(""))
						q.JapaneseName = textJapaneseName.getText();
					if(!textTranslatedName.getText().equals(""))
						q.TranslatedName = textTranslatedName.getText();
					if(!textRomajiName.getText().equals(""))
						q.RomajiName = textRomajiName.getText();
					q.Type = (org.dyndns.doujindb.db.record.Book.Type) comboType.getSelectedItem();
					if(checkAdult.isSelected())
						q.Adult = true;
					for(Content tag : listSearchContent.getIncludeContents())
						q.IncludeContents.add(tag);
					for(Content tag : listSearchContent.getExcludeContents())
						q.ExcludeContents.add(tag);

					// Clean result
					while(m_TableModel.getRowCount()>0)
						m_TableModel.removeRow(0);
					recordPreview.removeAll();
					
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
		}
		
		private final class SearchBook extends SearchWorker<Book>
		{
			private HashMap<Book, JButton> previews = new HashMap<Book, JButton>();
			private ActionListener listener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) {
					QueryBook query = new QueryBook();
					query.Id = Integer.parseInt(ae.getActionCommand());
					RecordSet<Book> result = DataBase.getBooks(query);
					UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, result.iterator().next());
				}
			};
			
			private SearchBook(Query<Book> query)
			{
				super(query);
			}
			@Override
			protected Void doInBackground() throws Exception
			{
				RecordSet<Book> result = DataBase.getBooks((QueryBook) query);
				for(final Book o : result)
				{
					if(previewEnabled)
					{
						JButton bookButton;
						try {
							bookButton = new JButton(
							new ImageIcon(
								ImageTool.read(DataStore.getThumbnail(o.getId()).openInputStream())));
						} catch (DataStoreException dse) {
							bookButton = new JButton(Icon.desktop_explorer_book_cover);
						}
						bookButton.setActionCommand(o.getId().toString());
						bookButton.addActionListener(listener);
						bookButton.setBorder(null);
						previews.put(o, bookButton);
					}
					publish(o);
					if(super.isCancelled())
						break;
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Book> data) {
				for(Book o : data)
				{
					m_TableModel.addRecord(o);
					if(previewEnabled)
						recordPreview.add(previews.get(o));
				}
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
			}
			@Override
			protected void done()
			{
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_book);
				recordPreview.validate();
				recordPreview.doLayout();
			}
		}
		
		private final class DynamicListContent extends JPanel implements LayoutManager
		{
			private JLabel labelContent;
			private ComboBoxContent comboboxContent;
			private JButton searchContentInclude;
			private JButton searchContentExclude;
			private JPanel listSearchContent;
			private JScrollPane scrollSearchContent;
			
			private Map<JButton, Content> includeContents = new HashMap<JButton, Content>();
			private Map<JButton, Content> excludeContents = new HashMap<JButton, Content>();
			
			private DynamicListContent()
			{
				super();
				super.setLayout(this);
				
				labelContent = new JLabel("Contents");
				labelContent.setFont(font);
				super.add(labelContent);
				comboboxContent = new ComboBoxContent();
				super.add(comboboxContent);
				searchContentInclude = new JButton(Icon.window_tab_explorer_add);
				searchContentInclude.setBorder(null);
				searchContentInclude.setFocusable(false);
				searchContentInclude.setToolTipText("Include");
				searchContentInclude.addActionListener(new ActionListener()
				{
					ActionListener listener = new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae) {
							JButton btnContent = (JButton) ae.getSource();
							includeContents.remove(btnContent);
							listSearchContent.remove(btnContent);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run()
								{
									listSearchContent.doLayout();
									scrollSearchContent.doLayout();
									listSearchContent.repaint();
								}
							});
						}
					};
					@Override
					public void actionPerformed(ActionEvent ae) {
						Object selectedItem = comboboxContent.getSelectedItem();
						if(selectedItem != null && selectedItem instanceof Content)
						{
							Content content = (Content) selectedItem;
							if(includeContents.containsValue(content))
								return;
							JButton btnContent = new JButton(content.getTagName());
							btnContent.setFocusable(false);
							btnContent.setBorderPainted(true);
							btnContent.setContentAreaFilled(false);
							btnContent.setFocusPainted(false);
							btnContent.setIcon(Icon.window_tab_explorer_add);
							btnContent.setHorizontalTextPosition(SwingConstants.RIGHT);
							btnContent.addActionListener(listener);
							btnContent.setMargin(new Insets(1,1,1,1));
							listSearchContent.add(btnContent);
							includeContents.put(btnContent, content);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run()
								{
									listSearchContent.doLayout();
									scrollSearchContent.doLayout();
									listSearchContent.repaint();
								}
							});
						}
					}
				});
				super.add(searchContentInclude);
				searchContentExclude = new JButton(Icon.window_tab_explorer_remove);
				searchContentExclude.setBorder(null);
				searchContentExclude.setFocusable(false);
				searchContentExclude.setToolTipText("Exclude");
				searchContentExclude.addActionListener(new ActionListener()
				{
					ActionListener listener = new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae) {
							JButton btnContent = (JButton) ae.getSource();
							excludeContents.remove(btnContent);
							listSearchContent.remove(btnContent);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run()
								{
									listSearchContent.doLayout();
									scrollSearchContent.doLayout();
									listSearchContent.repaint();
								}
							});
						}
					};
					@Override
					public void actionPerformed(ActionEvent ae) {
						Object selectedItem = comboboxContent.getSelectedItem();
						if(selectedItem != null && selectedItem instanceof Content)
						{
							Content content = (Content) selectedItem;
							if(excludeContents.containsValue(content))
								return;
							JButton btnContent = new JButton(content.getTagName());
							btnContent.setFocusable(false);
							btnContent.setBorderPainted(true);
							btnContent.setContentAreaFilled(false);
							btnContent.setFocusPainted(false);
							btnContent.setIcon(Icon.window_tab_explorer_remove);
							btnContent.setHorizontalTextPosition(SwingConstants.RIGHT);
							btnContent.addActionListener(listener);
							btnContent.setMargin(new Insets(1,1,1,1));
							listSearchContent.add(btnContent);
							excludeContents.put(btnContent, content);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run()
								{
									listSearchContent.doLayout();
									scrollSearchContent.doLayout();
									listSearchContent.repaint();
								}
							});
						}
					}
				});
				super.add(searchContentExclude);
				listSearchContent = new JPanel();
				FlowLayout layout = new WrapLayout();
				layout.setHgap(2);
				layout.setVgap(2);
				listSearchContent.setLayout(layout);
				scrollSearchContent = new JScrollPane(listSearchContent);
				scrollSearchContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				super.add(scrollSearchContent);
			}
			
			public Iterable<Content> getIncludeContents()
			{
				return includeContents.values();
			}
			public Iterable<Content> getExcludeContents()
			{
				return excludeContents.values();
			}

			@Override
			public void addLayoutComponent(String name, Component comp) { }

			@Override
			public void removeLayoutComponent(Component comp) { }

			@Override
			public Dimension preferredLayoutSize(Container parent) {
				return parent.getPreferredSize();
			}

			@Override
			public Dimension minimumLayoutSize(Container parent) {
				return parent.getMinimumSize();
			}

			@Override
			public void layoutContainer(Container parent) {
				int width = parent.getWidth(),
					height = parent.getHeight();
				labelContent.setBounds(0, 0, 100, 20);
				comboboxContent.setBounds(100, 0, width - 100 - 40, 20);
				searchContentInclude.setBounds(width - 40, 0, 20, 20);
				searchContentExclude.setBounds(width - 20, 0, 20, 20);
				scrollSearchContent.setBounds(0, 20 + 3, width, height - 26);
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
			m_Table = new JTable();
			
			m_TableModel = new RecordTableModel.ICircle();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Circle Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Circle>(ICircle.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerCircle thex = new TransferHandlerCircle();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchCircle(new QueryCircle());
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
			if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
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
				RecordSet<Circle> result = DataBase.getCircles((QueryCircle) query);
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
		private JLabel labelNamespace;
		private JComboBox<Content.Namespace> comboNamespace;
		private JLabel labelResults;
		private JScrollPane scrollResults;
		
		public IContent(JTabbedPane tab, int index)
		{
			super(tab, index);
			super.setLayout(this);
			
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField("");
			textTagName.setFont(font);
			labelNamespace = new JLabel("Type");
			labelNamespace.setFont(font);
			comboNamespace = new JComboBox<Content.Namespace>();
			comboNamespace.setFont(font);
			comboNamespace.setFocusable(false);
			comboNamespace.addItem(null);
			for(Content.Namespace tokenNamespace : Content.Namespace.values())
				comboNamespace.addItem(tokenNamespace);
			comboNamespace.setSelectedItem(null);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			m_Table = new JTable();
			
			m_TableModel = new RecordTableModel.IContent();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Content Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Content>(IContent.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerContent thex = new TransferHandlerContent();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelTagName);
			super.add(textTagName);
			super.add(labelNamespace);
			super.add(comboNamespace);
			super.add(labelResults);
			super.add(scrollResults);
			
			m_Worker = new SearchContent(new QueryContent());
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			labelNamespace.setBounds(3, 3 + 15, 100, 15);
			comboNamespace.setBounds(103, 3 + 15, width - 106, 15);
			m_LabelResults.setBounds(3, 3 + 30, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 45, width - 5, height - 30 - 45);
			m_ButtonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
		}

		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
			{
				// Prepare the Query to be run
				QueryContent q = new QueryContent();
				if(!textTagName.getText().equals(""))
					q.TagName = textTagName.getText();
				q.Namespace = (org.dyndns.doujindb.db.record.Content.Namespace) comboNamespace.getSelectedItem();
				
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
				RecordSet<Content> result = DataBase.getContents((QueryContent) query);
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
			m_Table = new JTable();
			
			m_TableModel = new RecordTableModel.IConvention();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Convention Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Convention>(IConvention.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerConvention thex = new TransferHandlerConvention();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelTagName);
			super.add(textTagName);
			super.add(labelResults);
			super.add(scrollResults);
			
			m_Worker = new SearchConvention(new QueryConvention());
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
			if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
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
				RecordSet<Convention> result = DataBase.getConventions((QueryConvention) query);
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
			m_Table = new JTable();
			
			m_TableModel = new RecordTableModel.IParody();
			m_Table.setModel(m_TableModel);
			m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
			m_Table.setRowSorter(m_TableSorter);
			m_TableRenderer = new RecordTableRenderer(getBackground(), getForeground());
			m_TableEditor = new RecordTableEditor();
			m_Table.setFont(font);
			m_Table.getTableHeader().setFont(font);
			m_Table.getTableHeader().setReorderingAllowed(true);
			m_Table.getColumnModel().getColumn(0).setCellRenderer(m_TableRenderer);
			m_Table.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
			m_Table.getColumnModel().getColumn(0).setResizable(false);
			m_Table.getColumnModel().getColumn(0).setMinWidth(0);
			m_Table.getColumnModel().getColumn(0).setMaxWidth(0);
			m_Table.getColumnModel().getColumn(0).setWidth(0);
			for(int k = 1;k<m_Table.getColumnModel().getColumnCount();k++)
			{
				m_Table.getColumnModel().getColumn(k).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(k).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(k).setResizable(true);
				m_Table.getColumnModel().getColumn(k).setMinWidth(125);
			}
			scrollResults = new JScrollPane(m_Table);
			m_Table.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount() == 2 && !me.isPopupTrigger())
					{
						try {
							final Record item = (Record) m_Table.getModel()
								.getValueAt(
									m_TableSorter.convertRowIndexToModel(
										m_Table.rowAtPoint(me.getPoint())), 0);
							UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, item);
						} catch (DataBaseException dbe) {
							LOG.error("Error displaying Parody Window", dbe);
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
					if(m_Table.getSelectedRowCount() < 1)
						return;
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem = new JMenuItem("Delete", Icon.desktop_explorer_delete);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							try {
								UI.Desktop.showDialog(getTopLevelWindow(m_Table), new DialogTrash<Parody>(IParody.this));
							} catch (PropertyVetoException pve) {
								LOG.error("Error displaying Trash empty-dialog", pve);
							}
						}
					});
		    		menuItem.setName("delete");
					menuItem.setActionCommand("delete");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setDragEnabled(true);
			TransferHandlerParody thex = new TransferHandlerParody();
			thex.setDragEnabled(true);
			thex.setDropEnabled(false);
			m_Table.setTransferHandler(thex);
			super.add(labelJapaneseName);
			super.add(textJapaneseName);
			super.add(labelTranslatedName);
			super.add(textTranslatedName);
			super.add(labelRomajiName);
			super.add(textRomajiName);
			super.add(labelWeblink);
			super.add(textWeblink);
			super.add(scrollResults);
			
			m_Worker = new SearchParody(new QueryParody());
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
			if(m_Worker.isDone() || m_Worker.getState() == StateValue.PENDING)
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
				RecordSet<Parody> result = DataBase.getParodies((QueryParody) query);
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
				m_LabelResults.setText("Found : " + m_TableModel.getRowCount());
				m_ButtonSearch.setText("Search");
				m_ButtonSearch.setMnemonic('S');
				m_Tab.setIconAt(m_Index, Icon.desktop_explorer_parody);
			}
		}
	}
	
	private static WindowEx getTopLevelWindow(Component comp)
	{
		return (WindowEx) SwingUtilities.getAncestorOfClass(WindowEx.class, comp);
	}
	
	private static final class DialogTrash<K extends Record> extends DialogEx
	{
		private SwingWorker<Void,Iterable<K>> swingWorker;
		private JProgressBar message;
		private PanelSearch<K> parentPanel;
		
		protected DialogTrash(PanelSearch<K> parentPanel)
		{
			super(Icon.window_trash_restore, "Trash");
			
			this.parentPanel = parentPanel;
		}

		@Override
		public JComponent createComponent()
		{
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1));
			message = new JProgressBar();
			message.setString("Move selected items to Trash?");
			message.setStringPainted(true);
			message.setMaximum(100);
			message.setMinimum(0);
			message.setValue(0);
			message.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			message.setFont(font);
			panel.add(message);
			JPanel bottom = new JPanel();
			bottom.setLayout(new GridLayout(1, 2));
			JButton canc = new JButton("Cancel");
			canc.setFont(font);
			canc.setMnemonic('C');
			canc.setFocusable(false);
			canc.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					swingWorker.cancel(true);
					dispose();
				}					
			});
			final JButton ok = new JButton("Ok");
			ok.setFont(font);
			ok.setIcon(null);
			ok.setMnemonic('O');
			ok.setFocusable(false);
			ok.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					ok.setEnabled(false);
					message.setString("Moving ...");
					swingWorker.execute();
				}					
			});
			bottom.add(ok);
			bottom.add(canc);
			bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panel.add(bottom);
			
			swingWorker = new SwingWorker<Void,Iterable<K>>()
			{
				@SuppressWarnings("unchecked")
				@Override
				protected Void doInBackground() throws Exception {
					int selectedCount = 0,
						processedCount = 0;
					Vector<K> selected = new Vector<K>();
					
					selectedCount = parentPanel.m_Table.getSelectedRowCount();
					
					for(int index : parentPanel.m_Table.getSelectedRows()) {
						if(super.isCancelled())
							break;
						try {
							K o = (K) parentPanel.m_TableModel.getValueAt(parentPanel.m_TableSorter.convertRowIndexToModel(index), 0);
							o.doRecycle();
							super.setProgress(100 * ++processedCount / selectedCount);
							selected.add(o);
						} catch (DataBaseException dbe) {
							LOG.error("Error recycling record", dbe);
						}
					}
					
					try {
						if(DataBase.isAutocommit())
							DataBase.doCommit();
					} catch (DataBaseException dbe) {
						LOG.error("Error committing changes", dbe);
					}
					
					publish(selected);
					
					return null;
				}
				@Override
				protected void process(java.util.List<Iterable<K>> data) {
					for(Iterable<K> i : data)
						for(K o : i)
							for(int index=0; index<parentPanel.m_TableModel.getRowCount(); index++)
								if((parentPanel.m_TableModel.getValueAt(index, 0)).equals(o))
								{
									parentPanel.m_TableModel.removeRow(index);
									break;
								}
					parentPanel.m_TableModel.fireTableDataChanged();
				}
				@Override
				protected void done() {
					dispose();
			    }
			};
			swingWorker.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						message.setValue((Integer) evt.getNewValue());
						return;
					}
				}
			});
			
			return panel;
		}
	}
}