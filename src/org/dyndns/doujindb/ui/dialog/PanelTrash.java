package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import java.beans.*;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.ui.UI;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class PanelTrash extends JPanel implements DataBaseListener, LayoutManager
{
	private JTabbedPane m_TabbedPane;
	private Vector<TrashTab<?>> m_Tabs;
	private TrashTab<Artist> m_TabArtist;
	private TrashTab<Book> m_TabBook;
	private TrashTab<Circle> m_TabCircle;
	private TrashTab<Convention> m_TabConvention;
	private TrashTab<Content> m_TabContent;
	private TrashTab<Parody> m_TabParody;
	
	private JSplitPane m_SplitPane;
	private JLabel m_LabelInfo;
	private JLabel m_LabelCount;
	private JLabel m_LabelTask;
	private JButton m_ButtonRestore;
	private JButton m_ButtonDelete;
	private JButton m_ButtonEmpty;
	
	private static DialogTrash m_PopupDialog = null;
	
	private static final Font font = UI.Font;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PanelTrash.class);
	
	public PanelTrash()
	{
		super();
		super.setLayout(this);
		JPanel panel1 = new JPanel();
		panel1.setLayout(null);
		panel1.setMaximumSize(new Dimension(130,130));
		panel1.setMinimumSize(new Dimension(130,130));
		m_LabelCount = new JLabel("Items : 0");
		m_LabelCount.setVerticalAlignment(JLabel.TOP);
		m_LabelCount.setFont(font);
		panel1.add(m_LabelCount);
		m_LabelInfo = new JLabel(" Info");
		m_LabelInfo.setOpaque(true);
		m_LabelInfo.setFont(font);
		panel1.add(m_LabelInfo);
		m_LabelTask = new JLabel(" Tasks");
		m_LabelTask.setOpaque(true);
		m_LabelTask.setFont(font);
		panel1.add(m_LabelTask);
		m_ButtonRestore = new JButton("Restore", Icon.window_trash_restore);
		m_ButtonRestore.setFocusable(false);
		m_ButtonRestore.setFont(font);
		m_ButtonRestore.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				int selectedCount = 0;
				for(TrashTab<?> tab : m_Tabs)
					selectedCount += tab.m_Table.getSelectedRowCount();
				if(selectedCount == 0)
					return;
				
				m_PopupDialog = new DialogTrash(PanelTrash.this,
					"Restore",
					"<html>" +
					"<body>" +
					"Restore selected items?<br/>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Iterable<Record>>()
				{
					@Override
					protected Void doInBackground() throws Exception {
						int selectedCount = 0,
							processedCount = 0;
						Vector<Record> selected = new Vector<Record>();
						
						for(TrashTab<?> tab : m_Tabs)
							selectedCount += tab.m_Table.getSelectedRowCount();
						
						for(TrashTab<?> tab : m_Tabs)
							for(int index : tab.m_Table.getSelectedRows()) {
								try {
									Record o = (Record) tab.m_TableModel.getValueAt(tab.m_TableSorter.convertRowIndexToModel(index), 0);
									o.doRestore();
									super.setProgress(100 * ++processedCount / selectedCount);
									selected.add(o);
								} catch (DataBaseException dbe) {
									LOG.error("Error restoring deleted record", dbe);
								}
							}
						
						try {
							if(DataBase.isAutocommit())
								DataBase.doCommit();
						} catch (DataBaseException dbe) {
							LOG.error("Error restoring deleted record", dbe);
						}
						
						publish(selected);
						
						return null;
					}
					@Override
					protected void process(java.util.List<Iterable<Record>> data) {
						for(Iterable<Record> records : data)
							for(Record record : records) {
								if(record instanceof Artist) {
									removeFromTable(m_TabArtist.m_TableModel, (Artist) record);
									continue;
								}
								if(record instanceof Book) {
									removeFromTable(m_TabBook.m_TableModel, (Book) record);
									continue;
								}
								if(record instanceof Circle) {
									removeFromTable(m_TabCircle.m_TableModel, (Circle) record);
									continue;
								}
								if(record instanceof Convention) {
									removeFromTable(m_TabConvention.m_TableModel, (Convention) record);
									continue;
								}
								if(record instanceof Content) {
									removeFromTable(m_TabContent.m_TableModel, (Content) record);
									continue;
								}
								if(record instanceof Parody) {
									removeFromTable(m_TabParody.m_TableModel, (Parody) record);
									continue;
								}
							}
					}
					@Override
					protected void done() {
						m_PopupDialog.dispose();
				    }
					private <T extends Record> void removeFromTable(TrashTab.RecordTableModel<T> model, T record) {
						for(int index=0; index<model.getRowCount(); index++)
							if((model.getValueAt(index, 0)).equals(record)) {
								model.removeRow(index);
								break;
							}
						model.fireTableDataChanged();
					}
				});
			}			
		});
		panel1.add(m_ButtonRestore);
		m_ButtonDelete = new JButton("Delete", Icon.window_trash_delete);
		m_ButtonDelete.setFocusable(false);
		m_ButtonDelete.setFont(font);
		m_ButtonDelete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				int selectedCount = 0;
				for(TrashTab<?> tab : m_Tabs)
					selectedCount += tab.m_Table.getSelectedRowCount();
				if(selectedCount == 0)
					return;
				
				m_PopupDialog = new DialogTrash(PanelTrash.this,
					"Delete",
					"<html>" +
					"<body>" +
					"Delete selected items?<br/>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Iterable<Record>>()
				{
					@Override
					protected Void doInBackground() throws Exception {
						int selectedCount = 0,
							processedCount = 0;
						Vector<Record> selected = new Vector<Record>();
						
						for(TrashTab<?> tab : m_Tabs)
							selectedCount += tab.m_Table.getSelectedRowCount();
						
						for(TrashTab<?> tab : m_Tabs)
							for(int index : tab.m_Table.getSelectedRows()) {
								try {
									Record o = (Record) tab.m_TableModel.getValueAt(tab.m_TableSorter.convertRowIndexToModel(index), 0);
									if(o instanceof Artist)
										((Artist)o).removeAll();
									if(o instanceof Book)
										((Book)o).removeAll();
									if(o instanceof Circle)
										((Circle)o).removeAll();
									if(o instanceof Convention)
										((Convention)o).removeAll();
									if(o instanceof Content)
										((Content)o).removeAll();
									if(o instanceof Parody)
										((Parody)o).removeAll();
									DataBase.doDelete(o);
									super.setProgress(100 * ++processedCount / selectedCount);
									selected.add(o);
								} catch (DataBaseException dbe) {
									LOG.error("Error deleting record", dbe);
								}
							}
						
						try {
							if(DataBase.isAutocommit())
								DataBase.doCommit();
						} catch (DataBaseException dbe) {
							LOG.error("Error deleting record", dbe);
						}
						
						publish(selected);
						
						return null;
					}
					@Override
					protected void process(java.util.List<Iterable<Record>> data) {
						for(Iterable<Record> records : data)
							for(Record record : records) {
								if(record instanceof Artist) {
									removeFromTable(m_TabArtist.m_TableModel, (Artist) record);
									continue;
								}
								if(record instanceof Book) {
									removeFromTable(m_TabBook.m_TableModel, (Book) record);
									continue;
								}
								if(record instanceof Circle) {
									removeFromTable(m_TabCircle.m_TableModel, (Circle) record);
									continue;
								}
								if(record instanceof Convention) {
									removeFromTable(m_TabConvention.m_TableModel, (Convention) record);
									continue;
								}
								if(record instanceof Content) {
									removeFromTable(m_TabContent.m_TableModel, (Content) record);
									continue;
								}
								if(record instanceof Parody) {
									removeFromTable(m_TabParody.m_TableModel, (Parody) record);
									continue;
								}
							}
					}
					@Override
					protected void done() {
						m_PopupDialog.dispose();
				    }
					private <T extends Record> void removeFromTable(TrashTab.RecordTableModel<T> model, T record) {
						for(int index=0; index<model.getRowCount(); index++)
							if((model.getValueAt(index, 0)).equals(record)) {
								model.removeRow(index);
								break;
							}
						model.fireTableDataChanged();
					}
				});
			}			
		});
		panel1.add(m_ButtonDelete);
		m_ButtonEmpty = new JButton("Empty", Icon.window_trash_empty);
		m_ButtonEmpty.setFocusable(false);
		m_ButtonEmpty.setFont(font);
		m_ButtonEmpty.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				m_PopupDialog = new DialogTrash(PanelTrash.this,
					"Empty",
					"<html>" +
					"<body>" +
					"Empty all of the items from the trash?<br/>" +
					"</body>" +
					"</html>", new SwingWorker<Void,Iterable<Record>>()
				{
					@Override
					protected Void doInBackground() throws Exception {
						int selectedCount = 0,
							processedCount = 0;
						Vector<Record> selected = new Vector<Record>();
						
						for(TrashTab<?> tab : m_Tabs)
						{
							tab.m_Table.selectAll();
							selectedCount += tab.m_Table.getSelectedRowCount();
						}
						
						for(TrashTab<?> tab : m_Tabs)
							for(int index : tab.m_Table.getSelectedRows()) {
								try {
									Record o = (Record) tab.m_TableModel.getValueAt(tab.m_TableSorter.convertRowIndexToModel(index), 0);
									if(o instanceof Artist)
										((Artist)o).removeAll();
									if(o instanceof Book)
										((Book)o).removeAll();
									if(o instanceof Circle)
										((Circle)o).removeAll();
									if(o instanceof Convention)
										((Convention)o).removeAll();
									if(o instanceof Content)
										((Content)o).removeAll();
									if(o instanceof Parody)
										((Parody)o).removeAll();
									DataBase.doDelete(o);
									super.setProgress(100 * ++processedCount / selectedCount);
									selected.add(o);
								} catch (DataBaseException dbe) {
									LOG.error("Error deleting record", dbe);
								}
							}
						
						try {
							if(DataBase.isAutocommit())
								DataBase.doCommit();
						} catch (DataBaseException dbe) {
							LOG.error("Error deleting record", dbe);
						}
						
						publish(selected);
						
						return null;
					}
					@Override
					protected void process(java.util.List<Iterable<Record>> data) {
						for(Iterable<Record> records : data)
							for(Record record : records) {
								if(record instanceof Artist) {
									removeFromTable(m_TabArtist.m_TableModel, (Artist) record);
									continue;
								}
								if(record instanceof Book) {
									removeFromTable(m_TabBook.m_TableModel, (Book) record);
									continue;
								}
								if(record instanceof Circle) {
									removeFromTable(m_TabCircle.m_TableModel, (Circle) record);
									continue;
								}
								if(record instanceof Convention) {
									removeFromTable(m_TabConvention.m_TableModel, (Convention) record);
									continue;
								}
								if(record instanceof Content) {
									removeFromTable(m_TabContent.m_TableModel, (Content) record);
									continue;
								}
								if(record instanceof Parody) {
									removeFromTable(m_TabParody.m_TableModel, (Parody) record);
									continue;
								}
							}
					}
					@Override
					protected void done() {
						m_PopupDialog.dispose();
				    }
					private <T extends Record> void removeFromTable(TrashTab.RecordTableModel<T> model, T record) {
						for(int index=0; index<model.getRowCount(); index++)
							if((model.getValueAt(index, 0)).equals(record)) {
								model.removeRow(index);
								break;
							}
						model.fireTableDataChanged();
					}
				});
			}			
		});
		panel1.add(m_ButtonEmpty);
		
		m_TabbedPane = new JTabbedPane();
		m_TabbedPane.setFocusable(false);
		m_Tabs = new Vector<TrashTab<?>>();
		
		m_TabArtist = new IArtist(m_TabbedPane, 0);
		m_Tabs.add(m_TabArtist);
		m_TabbedPane.insertTab("Artist", Icon.desktop_explorer_artist, m_TabArtist, "", 0);
		m_TabBook = new IBook(m_TabbedPane, 1);
		m_Tabs.add(m_TabBook);
		m_TabbedPane.insertTab("Book", Icon.desktop_explorer_book, m_TabBook, "", 1);
		m_TabCircle = new ICircle(m_TabbedPane, 2);
		m_Tabs.add(m_TabCircle);
		m_TabbedPane.insertTab("Circle", Icon.desktop_explorer_circle, m_TabCircle, "", 2);
		m_TabConvention = new IConvention(m_TabbedPane, 3);
		m_Tabs.add(m_TabConvention);
		m_TabbedPane.insertTab("Convention", Icon.desktop_explorer_convention, m_TabConvention, "", 3);
		m_TabContent = new IContent(m_TabbedPane, 4);
		m_Tabs.add(m_TabContent);
		m_TabbedPane.insertTab("Content", Icon.desktop_explorer_content, m_TabContent, "", 4);
		m_TabParody = new IParody(m_TabbedPane, 5);
		m_Tabs.add(m_TabParody);
		m_TabbedPane.insertTab("Parody", Icon.desktop_explorer_parody, m_TabParody, "", 5);
		
		m_SplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, m_TabbedPane);
		m_SplitPane.setDividerSize(1);
		m_SplitPane.setEnabled(false);
		m_SplitPane.doLayout();
		super.add(m_SplitPane);

		loadData();
		new SwingWorker<Void,Record>()
		{
			@Override
			protected Void doInBackground() throws Exception {
				// Clean
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						for(TrashTab<?> tab : m_Tabs)
							tab.m_TableModel.setNumRows(0);
					}
				});
				// Fill
				try {
					for(Record record : DataBase.getRecycled()) {
						if(record instanceof Artist) {
							publish(record);
							continue;
						}
						if(record instanceof Book) {
							publish(record);
							continue;
						}
						if(record instanceof Circle) {
							publish(record);
							continue;
						}
						if(record instanceof Convention) {
							publish(record);
							continue;
						}
						if(record instanceof Content) {
							publish(record);
							continue;
						}
						if(record instanceof Parody) {
							publish(record);
							continue;
						}
					}
				} catch (DataBaseException dbe) {
					LOG.error("Error loading deleted record", dbe);
				}
				return null;
			}
			@Override
			protected void process(java.util.List<Record> records)
			{
				for(final Record record : records)
				{
					if(record instanceof Artist)
						m_TabArtist.m_TableModel.addRecord((Artist)record);
					if(record instanceof Book)
						m_TabBook.m_TableModel.addRecord((Book)record);
					if(record instanceof Circle)
						m_TabCircle.m_TableModel.addRecord((Circle)record);
					if(record instanceof Convention)
						m_TabConvention.m_TableModel.addRecord((Convention)record);
					if(record instanceof Content)
						m_TabContent.m_TableModel.addRecord((Content)record);
					if(record instanceof Parody)
						m_TabParody.m_TableModel.addRecord((Parody)record);
				}
			}
			@Override
			protected void done()
			{
				for(TrashTab<?> tab : m_Tabs)
					tab.loadData();
			}
		}.execute();
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		m_LabelInfo.setBounds(0,0,130,20);
		m_LabelCount.setBounds(2,22,125,55);
		m_LabelTask.setBounds(0,75+5,130,20);
		m_ButtonRestore.setBounds(3,75+25+1,125,20);
		m_ButtonDelete.setBounds(3,75+45+2,125,20);
		m_ButtonEmpty.setBounds(3,75+65+2,125,20);
		m_SplitPane.setBounds(0, 0, width,  height);
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
	
	private void loadData()
	{
		new SwingWorker<Void, Object>()
		{
			long recycledCount = 0;
			@Override
			public Void doInBackground()
			{
				try {
					recycledCount = DataBase.getRecycled().size();
				} catch (DataBaseException dbe) {
					LOG.error("Error reloading deleted record count", dbe);
				}
				return null;
			}
			@Override
			protected void done() {
				m_LabelCount.setText(recycledCount == 1 ? "Item : 1" : "Items : " + recycledCount);
			}
		}.execute();
	}

	private final class DialogTrash extends JInternalFrame implements LayoutManager
	{
		private JComponent m_GlassPane;
		private JComponent m_Component;
		private JLabel m_LabelMessage;
		private JButton m_ButtonOk;
		private JButton m_ButtonCancel;
		private JProgressBar m_ProgressBar;
		
		private SwingWorker<?,?> m_Worker;
		
		public DialogTrash(JComponent parent, String title, String message, SwingWorker<?,?> worker)
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_trash);
			super.setTitle(title);
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
	
	@Override
	public void recordAdded(Record record) {}
	
	@Override
	public void recordDeleted(Record record) { }
	
	@Override
	public void recordUpdated(Record record, UpdateData data) { }
	
	@Override
	public void recordRecycled(Record record)
	{
		loadData();
		if(record instanceof Artist) {
			m_TabArtist.m_TableModel.addRecord((Artist)record);
			m_TabArtist.loadData();
			return;
		}
		if(record instanceof Book) {
			m_TabBook.m_TableModel.addRecord((Book)record);
			m_TabBook.loadData();
			return;
		}
		if(record instanceof Circle) {
			m_TabCircle.m_TableModel.addRecord((Circle)record);
			m_TabCircle.loadData();
			return;
		}
		if(record instanceof Content) {
			m_TabContent.m_TableModel.addRecord((Content)record);
			m_TabContent.loadData();
			return;
		}
		if(record instanceof Convention) {
			m_TabConvention.m_TableModel.addRecord((Convention)record);
			m_TabConvention.loadData();
			return;
		}
		if(record instanceof Parody) {
			m_TabParody.m_TableModel.addRecord((Parody)record);
			m_TabParody.loadData();
			return;
		}
	}

	@Override
	public void recordRestored(Record record) {
		loadData();
		if(record instanceof Artist) {
			m_TabArtist.m_TableModel.removeRecord((Artist)record);
			m_TabArtist.loadData();
			return;
		}
		if(record instanceof Book) {
			m_TabBook.m_TableModel.removeRecord((Book)record);
			m_TabBook.loadData();
			return;
		}
		if(record instanceof Circle) {
			m_TabCircle.m_TableModel.removeRecord((Circle)record);
			m_TabCircle.loadData();
			return;
		}
		if(record instanceof Content) {
			m_TabContent.m_TableModel.removeRecord((Content)record);
			m_TabContent.loadData();
			return;
		}
		if(record instanceof Convention) {
			m_TabConvention.m_TableModel.removeRecord((Convention)record);
			m_TabConvention.loadData();
			return;
		}
		if(record instanceof Parody) {
			m_TabParody.m_TableModel.removeRecord((Parody)record);
			m_TabParody.loadData();
			return;
		}
	}
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }
	
	private static abstract class TrashTab<T extends Record> extends JPanel implements LayoutManager
	{
		protected JTabbedPane m_Tab;
		protected int m_Index;
		
		protected JTable m_Table = new JTable();
		protected JScrollPane m_Scroll = new JScrollPane(m_Table);
		protected RecordTableModel<T> m_TableModel;
		protected RecordTableRenderer m_TableRenderer;
		protected RecordTableEditor m_TableEditor;
		protected TableRowSorter<DefaultTableModel> m_TableSorter;
		
		protected String m_Name = "";
		
		private TrashTab(JTabbedPane tab, int index, RecordTableModel<T> model)
		{
			super.setLayout(this);
			m_Tab = tab;
			m_Index = index;
			m_TableModel = model;
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
			for(int i = 1; i < m_Table.getColumnModel().getColumnCount(); i++)
			{
				m_Table.getColumnModel().getColumn(i).setCellRenderer(m_TableRenderer);
				m_Table.getColumnModel().getColumn(i).setCellEditor(m_TableEditor);
				m_Table.getColumnModel().getColumn(i).setResizable(true);
			}
			super.add(m_Scroll);
			m_Table.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent me)
				{
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
					
					JPopupMenu popupMenu = new JPopupMenu();
		    		JMenuItem menuItem;
		    		menuItem = new JMenuItem("Select All", Icon.window_trash_selectall);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_Table.selectAll();
							loadData();
						}
					});
		    		menuItem.setName("select-all");
					menuItem.setActionCommand("select-all");
					popupMenu.add(menuItem);
					menuItem = new JMenuItem("Deselect All", Icon.window_trash_deselectall);
		    		menuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							m_Table.clearSelection();
							loadData();
						}
					});
		    		menuItem.setName("deselect-all");
					menuItem.setActionCommand("deselect-all");
					popupMenu.add(menuItem);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			});
			m_Table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ListSelectionModel selectionModel = m_Table.getSelectionModel();
			selectionModel.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent lse) {
					loadData();
				}
			});
		}
		
		protected void loadData()
		{
			if(m_Table.getRowCount() > 0)
				m_Tab.setTitleAt(m_Index, m_Name + " (" + m_Table.getSelectedRowCount() + "/" + m_Table.getRowCount() + ")");
			else
				m_Tab.setTitleAt(m_Index, m_Name);
		}
		
		static final class RecordTableRenderer extends DefaultTableCellRenderer
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
		
		static abstract class RecordTableModel<R extends Record> extends DefaultTableModel
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
			
			public abstract void addRecord(R record);
			
			public void removeRecord(R record)
			{
				for(int index = 0; index > super.getRowCount(); index++)
					if(super.getValueAt(index, 0).equals(record))
						super.removeRow(index);
			}
		}
		
		static final class RecordTableEditor extends AbstractCellEditor implements TableCellEditor
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
		public void layoutContainer(Container parent)
		{
			m_Scroll.setBounds(0, 0, parent.getWidth(), parent.getHeight());
		}
		
		@Override
		public void addLayoutComponent(String key,Component c) {}
		
		@Override
		public void removeLayoutComponent(Component c) {}
		
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return new Dimension(150, 150);
		}
		
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
		     return new Dimension(350, 350);
		}
	}
	
	public static final class IArtist extends TrashTab<Artist>
	{
		private IArtist(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.IArtist());
			m_Name = "Artist";
		}
	}
	
	public static final class IBook extends TrashTab<Book>
	{
		private IBook(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.IBook());
			m_Name = "Book";
		}
	}
	
	public static final class ICircle extends TrashTab<Circle>
	{
		private ICircle(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.ICircle());
			m_Name = "Circle";
		}
	}
	
	public static final class IConvention extends TrashTab<Convention>
	{
		private IConvention(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.IConvention());
			m_Name = "Convention";
		}
	}
	
	public static final class IContent extends TrashTab<Content>
	{
		private IContent(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.IContent());
			m_Name = "Content";
		}
	}
	
	public static final class IParody extends TrashTab<Parody>
	{
		private IParody(JTabbedPane tab, int index)
		{
			super(tab, index, new RecordTableModel.IParody());
			m_Name = "Parody";
		}
	}
}
