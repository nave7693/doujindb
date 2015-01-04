package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.conf.ConfigurationParser;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.PanelConfiguration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**  
* DoujinshiDBScanner.java - Plugin to batch process media files thanks to the DoujinshiDB project APIs.
* @author  nozomu
* @version 1.3
*/
public final class DataImport extends Plugin
{
	static final String Author = "loli10K";
	static final String Version = "0.1";
	static final String Weblink = "https://github.com/loli10K";
	static final String Name = "Data Import";
	static final String Description = "Batch process media files";
	private PluginUI m_UI;
	
	private static SimpleDateFormat sdf;
	private static Font font;
	private static Icons Icon = new Icons();
	
	private TaskManager TaskManager = new TaskManager(PLUGIN_HOME);
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataImport.class);
	
	static {
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public Icon getIcon() {
		return Icon.icon;
	}
	
	@Override
	public String getName() {
		return Name;
	}
	
	@Override
	public String getDescription() {
		return Description;
	}
	
	@Override
	public String getVersion() {
		return Version;
	}
	
	@Override
	public String getAuthor() {
		return Author;
	}
	
	@Override
	public String getWeblink() {
		return Weblink;
	}
	
	@Override
	public JComponent getUI() {
		return m_UI;
	}
	
	@SuppressWarnings("serial")
	private final class PluginUI extends JPanel implements LayoutManager, ActionListener, PropertyChangeListener
	{
		private JTabbedPane m_TabbedPane;
		@SuppressWarnings("unused")
		private JPanel m_TabConfiguration;
		@SuppressWarnings("unused")
		private JPanel m_TabTasks;
		private JButton m_ButtonTaskAdd;
		private JButton m_ButtonTaskManagerCtl;
		private JLabel m_LabelTasks;
		private JButton m_ButtonTaskDelete;
		private JButton m_ButtonTaskReset;
		private JCheckBox m_CheckboxSelection;
		private JSplitPane m_SplitPane;
		private PanelTaskUI m_PanelTasks;
		private JScrollPane m_ScrollPanelTasks;
		private TaskUI m_PanelTask;
		
		public PluginUI()
		{
			super();
			super.setLayout(this);
			super.setPreferredSize(new Dimension(350, 350));
			super.setMinimumSize(new Dimension(350, 350));
			m_TabbedPane = new JTabbedPane();
			m_TabbedPane.setFont(font = UI.Font);
			m_TabbedPane.setFocusable(false);
			m_TabTasks = new JPanel();
			m_TabTasks.setLayout(null);
			m_ButtonTaskAdd = new JButton(Icon.add);
			m_ButtonTaskAdd.addActionListener(this);
			m_ButtonTaskAdd.setBorder(null);
			m_ButtonTaskAdd.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskAdd);
			m_ButtonTaskManagerCtl = new JButton(Icon.task_resume);
			m_ButtonTaskManagerCtl.addActionListener(this);
			m_ButtonTaskManagerCtl.setBorder(null);
			m_ButtonTaskManagerCtl.setToolTipText("Resume Worker");
			m_ButtonTaskManagerCtl.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskManagerCtl);
			m_LabelTasks = new JLabel("");
			m_LabelTasks.setText("Tasks : " + TaskManager.size());
			m_TabTasks.add(m_LabelTasks);
			m_ButtonTaskDelete = new JButton(Icon.task_delete);
			m_ButtonTaskDelete.addActionListener(this);
			m_ButtonTaskDelete.setBorder(null);
			m_ButtonTaskDelete.setToolTipText("Delete");
			m_ButtonTaskDelete.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskDelete);
			m_ButtonTaskReset = new JButton(Icon.task_reset);
			m_ButtonTaskReset.addActionListener(this);
			m_ButtonTaskReset.setBorder(null);
			m_ButtonTaskReset.setToolTipText("Reset");
			m_ButtonTaskReset.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskReset);
			m_CheckboxSelection = new JCheckBox();
			m_CheckboxSelection.setSelected(false);
			m_CheckboxSelection.addActionListener(this);
			m_TabTasks.add(m_CheckboxSelection);
			m_SplitPane = new JSplitPane();
			m_SplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			m_SplitPane.setResizeWeight(1);
			m_PanelTasks = new PanelTaskUI();
			m_ScrollPanelTasks = new JScrollPane(m_PanelTasks);
			m_ScrollPanelTasks.getVerticalScrollBar().setUnitIncrement(10);
			m_SplitPane.setTopComponent(m_ScrollPanelTasks);
			m_PanelTask = new TaskUI();
			m_SplitPane.setBottomComponent(null);
			m_TabTasks.add(m_SplitPane);
			m_TabbedPane.addTab("Tasks", Icon.tasks, m_TabTasks);
			PanelConfiguration panelConfig = new PanelConfiguration(Configuration.class);
			panelConfig.setConfigurationFile(CONFIG_FILE);
			m_TabbedPane.addTab("Configuration", Icon.settings, m_TabConfiguration = panelConfig);
			super.add(m_TabbedPane);
			TaskManager.registerListener(this);
		}
		
		@Override
		public void layoutContainer(Container parent) {
			int width = parent.getWidth(),
				height = parent.getHeight();
			m_TabbedPane.setBounds(0,0,width,height);
			m_ButtonTaskAdd.setBounds(1,1,20,20);
			m_ButtonTaskManagerCtl.setBounds(21,1,20,20);
			m_LabelTasks.setBounds(41,1,width-125,20);
			m_ButtonTaskDelete.setBounds(width-65,1,20,20);
			m_ButtonTaskReset.setBounds(width-45,1,20,20);
			m_CheckboxSelection.setBounds(width-25,1,20,20);
			m_SplitPane.setBounds(1,21,width-5,height-45);
		}
		@Override
		public void addLayoutComponent(String key,Component c) { }
		@Override
		public void removeLayoutComponent(Component c) { }
		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return getPreferredSize();
		}
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return getMinimumSize();
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() == m_ButtonTaskAdd) {
				try 
				{
					JFileChooser fc = UI.FileChooser;
					fc.setMultiSelectionEnabled(true);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if(fc.showOpenDialog(PluginUI.this) != JFileChooser.APPROVE_OPTION)
						return;
					final File files[] = fc.getSelectedFiles();
					for(File file : files)
					{
						TaskManager.add(file);
					}
					m_PanelTasks.dataChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskManagerCtl) {
				if(TaskManager.isRunning())
				{
					m_ButtonTaskManagerCtl.setIcon(Icon.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							TaskManager.pause();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(Icon.task_resume);
						}
					}.execute();
				} else {
					m_ButtonTaskManagerCtl.setIcon(Icon.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							TaskManager.resume();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(Icon.task_pause);
						}
					}.execute();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskDelete) {
				List<Task> selected = new Vector<Task>();
				for(Task task : TaskManager.tasks()) {
					if(task.selected) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected) {
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					TaskManager.remove(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_ButtonTaskReset) {
				List<Task> selected = new Vector<Task>();
				for(Task task : TaskManager.tasks()) {
					if(task.selected) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected) {
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					TaskManager.reset(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_CheckboxSelection) {
				for(Task task : TaskManager.tasks())
					task.selected = m_CheckboxSelection.isSelected();
				m_PanelTasks.dataChanged();
				return;
			}
		}
		
		private final class PanelTaskUI extends JTable implements PropertyChangeListener
		{
			private Class<?>[] m_Types = new Class[] {
				Task.State.class,
				String.class,
				Boolean.class
			};
			private TaskSetTableModel m_TableModel;
			private TaskRenderer m_TableRender;
			private TaskEditor m_TableEditor;
			private TableRowSorter<DefaultTableModel> m_TableSorter;
			
			private PanelTaskUI() {
				m_TableModel = new TaskSetTableModel();
				m_TableModel.addColumn(""); // State
				m_TableModel.addColumn("Task"); // Name | Id | File
				m_TableModel.addColumn(""); // Selection
				m_TableRender = new TaskRenderer();
				m_TableEditor = new TaskEditor();
				m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
				super.setRowSorter(m_TableSorter);
				super.setModel(m_TableModel);
				super.setFont(font);
				super.getTableHeader().setReorderingAllowed(false);
				super.getColumnModel().getColumn(0).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(1).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(1).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(2).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setResizable(false);
				super.getColumnModel().getColumn(0).setMaxWidth(20);
				super.getColumnModel().getColumn(0).setMinWidth(20);
				super.getColumnModel().getColumn(0).setWidth(20);
				super.getColumnModel().getColumn(1).setResizable(true);
//				super.getColumnModel().getColumn(1).setMaxWidth(20);
//				super.getColumnModel().getColumn(1).setMinWidth(20);
//				super.getColumnModel().getColumn(1).setWidth(20);
//				super.getColumnModel().getColumn(1).setMinWidth(150);
//				super.getColumnModel().getColumn(1).setWidth(150);
//				super.getColumnModel().getColumn(1).setPreferredWidth(150);
				super.getColumnModel().getColumn(2).setResizable(false);
				super.getColumnModel().getColumn(2).setMaxWidth(20);
				super.getColumnModel().getColumn(2).setMinWidth(20);
				super.getColumnModel().getColumn(2).setWidth(20);
				super.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				super.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				super.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
					@Override
					public void valueChanged(ListSelectionEvent lse) {
						if(lse.getValueIsAdjusting())
							return;
						int rowNumber = getSelectedRow();
						if(rowNumber == -1)
							return;
						Task task = (Task) getValueAt(rowNumber, -1);
						m_PanelTask.setTask(task);
						m_SplitPane.setBottomComponent(m_PanelTask);
					}
				});

				super.addMouseMotionListener(new MouseMotionAdapter()
				{
					private int BUTTON1 = MouseEvent.BUTTON1_DOWN_MASK;
					private int BUTTON2 = MouseEvent.BUTTON2_DOWN_MASK;
					private int BUTTON3 = MouseEvent.BUTTON3_DOWN_MASK;
					
					@Override
					public void mouseDragged(MouseEvent me) {
						int rowNumber = rowAtPoint(me.getPoint());
						int colNumber = columnAtPoint(me.getPoint());
						
						if(rowNumber == -1 || colNumber == getColumnCount() -1)
							return;
						Task task = (Task) getValueAt(rowNumber, -1);
						if ((me.getModifiersEx() & (BUTTON1 | BUTTON2 | BUTTON3)) == BUTTON1) {
							task.selected = true;
						}
						if ((me.getModifiersEx() & (BUTTON1 | BUTTON2 | BUTTON3)) == BUTTON3) {
							task.selected = false;
						}
						dataChanged();
					}
				});
				
				TaskManager.registerListener(this);
			}
			
			public void dataChanged() {
				m_TableModel.fireTableDataChanged();
			}
			
			private final class TaskSetTableModel extends DefaultTableModel
			{
				private TaskSetTableModel() { }
				
				@Override
				public int getRowCount() {
					return TaskManager.size();
				}

				@Override
				public int getColumnCount() {
					return m_Types.length;
				}
				
				public Class<?> getColumnClass(int columnIndex) {
					return m_Types[columnIndex];
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					Task task = TaskManager.get(rowIndex);
					switch(columnIndex) {
						case -1:
							return task;
						case 0:
							return task.state;
						case 1:
							return task.file;
						case 2:
							return task.selected;
					}
					throw new IllegalArgumentException("Argument columnIndex (= " + columnIndex + ") must be 0 < X < " + m_Types.length);
				}
				
				@Override
				public void setValueAt(Object value, int rowIndex, int columnIndex) {
					Task task = TaskManager.get(rowIndex);
				    if (columnIndex == 2) {
				    	task.selected = (Boolean)value;
				        fireTableCellUpdated(rowIndex, columnIndex);
				    }
				}
			}
			
			private final class TaskEditor extends AbstractCellEditor implements TableCellEditor
			{
				private TaskEditor() {
					super();
				}
				
				public Object getCellEditorValue() {
					return 0;
				}
			
				public Component getTableCellEditorComponent(
				    JTable table,
				    Object value,
				    boolean isSelected,
				    int row,
				    int column) {
					    super.cancelCellEditing();
					    return null;
				}
			}
			
			private final class TaskRenderer extends DefaultTableCellRenderer
			{
				private JLabel m_Label;
				private JCheckBox m_CheckBox;
				
				private Color mLabelForeground;
				private Color mLabelBackground;
				
				public TaskRenderer() {
				    super();
				    super.setFont(font);
				    m_Label = new JLabel();
				    m_Label.setOpaque(true);
				    mLabelForeground = m_Label.getForeground();
				    mLabelBackground = m_Label.getBackground();
					m_CheckBox = new JCheckBox();
				}
			
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					super.getTableCellRendererComponent(
				        table,
				        value,
				        isSelected,
				        hasFocus,
				        row,
				        column);
					if(table.getModel().getRowCount() < 1)
						return this;
					Task task = (Task) getValueAt(row, -1);
					if(column == 0) {
						if(task.equals(TaskManager.getRunningTask()))
							m_Label.setIcon(Icon.task_state_running);
						else
							switch (task.state)
							{
							case NEW:
								m_Label.setIcon(Icon.task_state_new);
								break;
							case COMPLETE:
								m_Label.setIcon(Icon.task_state_complete);
								break;
							case ERROR:
								m_Label.setIcon(Icon.task_state_error);
								break;
							case WARNING:
								m_Label.setIcon(Icon.task_state_warning);
								break;
							case ABORT:
								m_Label.setIcon(Icon.task_state_abort);
								break;
							case UNKNOW:
								m_Label.setIcon(Icon.task_state_unknow);
								break;
							}
						m_Label.setText("");
						m_Label.setForeground(mLabelForeground);
						m_Label.setBackground(mLabelBackground);
						return m_Label;
					}
					if(column == 1) {
						m_Label.setIcon(null);
						m_Label.setText(task.file);
						if(task.selected) {
							m_Label.setBackground(mLabelForeground);
							m_Label.setForeground(mLabelBackground);
						} else {
							m_Label.setForeground(mLabelForeground);
							m_Label.setBackground(mLabelBackground);
						}
						return m_Label;
					}
					if(column == 2) {
						m_CheckBox.setSelected(task.selected);
						return m_CheckBox;
					}
					return this;
				}
			}

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				if(pce.getPropertyName().equals("task-exec"))
					dataChanged();
				if(pce.getPropertyName().equals("task-info"))
					dataChanged();
			}
		}
		
		private final class TaskUI extends JPanel implements LayoutManager, ActionListener, PropertyChangeListener
		{
			private Task m_Task;
			private JLabel m_LabelTitle;
			private JLabel m_LabelPreview;
			private JButton m_ButtonClose;
			private JButton m_ButtonOpenFolder;
			private JButton m_ButtonOpenXML;
			private JTabbedPane m_TabbedPaneMetadata;
			private JPanel m_TabbedPaneMetadata2;
//			private JButton m_ButtonOpenBook;
//			private JButton m_ButtonRunAgain;
//			private JButton m_ButtonSkipDuplicate;
//			private JButton m_ButtonImportBID;
//			private JTabbedPane m_TabbedPaneImage;
			
			public TaskUI() {
				super();
				setLayout(this);
				setSize(280, 280);
				setMinimumSize(new Dimension(280, 280));
				setMaximumSize(new Dimension(280, 280));
				setPreferredSize(new Dimension(280, 280));
				
				m_LabelTitle = new JLabel();
				m_LabelTitle.setText("");
				m_LabelTitle.setIcon(null);
				add(m_LabelTitle);
				m_LabelPreview = new JLabel();
				m_LabelPreview.setIcon(Icon.task_preview_missing);
				m_LabelPreview.setHorizontalAlignment(JLabel.CENTER);
				m_LabelPreview.setVerticalAlignment(JLabel.CENTER);
				m_LabelPreview.setOpaque(false);
				add(m_LabelPreview);
				m_ButtonClose = new JButton();
				m_ButtonClose.setText("");
				m_ButtonClose.setToolTipText("Close");
				m_ButtonClose.setIcon(Icon.cancel);
				m_ButtonClose.setSelected(true);
				m_ButtonClose.setFocusable(false);
				m_ButtonClose.addActionListener(this);
				add(m_ButtonClose);
				m_ButtonOpenFolder = new JButton();
				m_ButtonOpenFolder.setText("");
				m_ButtonOpenFolder.setToolTipText("View Folder");
				m_ButtonOpenFolder.setIcon(Icon.task_folder);
				m_ButtonOpenFolder.setSelected(false);
				m_ButtonOpenFolder.setFocusable(false);
				m_ButtonOpenFolder.addActionListener(this);
				add(m_ButtonOpenFolder);
				m_ButtonOpenXML = new JButton();
				m_ButtonOpenXML.setText("");
				m_ButtonOpenXML.setToolTipText("View XML");
				m_ButtonOpenXML.setIcon(Icon.task_xml);
				m_ButtonOpenXML.setSelected(false);
				m_ButtonOpenXML.setFocusable(false);
				m_ButtonOpenXML.addActionListener(this);
				add(m_ButtonOpenXML);
				
				m_TabbedPaneMetadata = new JTabbedPane();
				m_TabbedPaneMetadata.setFocusable(false);
				m_TabbedPaneMetadata.setTabPlacement(JTabbedPane.TOP);
				add(m_TabbedPaneMetadata);
				
//				m_ButtonOpenBook = new JButton();
//				m_ButtonOpenBook.setText("Open Book");
//				m_ButtonOpenBook.setIcon(Icon.task_book);
//				m_ButtonOpenBook.setSelected(false);
//				m_ButtonOpenBook.setFocusable(false);
//				m_ButtonOpenBook.addActionListener(this);
//				m_ButtonOpenBook.setOpaque(false);
//				m_ButtonOpenBook.setIconTextGap(5);
//				m_ButtonOpenBook.setMargin(new Insets(0,0,0,0));
//				m_ButtonOpenBook.setHorizontalAlignment(SwingConstants.LEFT);
//				m_ButtonOpenBook.setHorizontalTextPosition(SwingConstants.RIGHT);
//				add(m_ButtonOpenBook);
//				
//				m_ButtonRunAgain = new JButton();
//				m_ButtonRunAgain.setText("Re-run Step");
//				m_ButtonRunAgain.setIcon(Icon.task_reset);
//				m_ButtonRunAgain.setSelected(false);
//				m_ButtonRunAgain.setFocusable(false);
//				m_ButtonRunAgain.addActionListener(this);
//				m_ButtonRunAgain.setOpaque(false);
//				m_ButtonRunAgain.setIconTextGap(5);
//				m_ButtonRunAgain.setMargin(new Insets(0,0,0,0));
//				m_ButtonRunAgain.setHorizontalAlignment(SwingConstants.LEFT);
//				m_ButtonRunAgain.setHorizontalTextPosition(SwingConstants.RIGHT);
//				add(m_ButtonRunAgain);
//				m_ButtonSkipDuplicate = new JButton();
//				m_ButtonSkipDuplicate.setText("Skip Duplicate");
//				m_ButtonSkipDuplicate.setIcon(Icon.task_skip);
//				m_ButtonSkipDuplicate.setSelected(false);
//				m_ButtonSkipDuplicate.setFocusable(false);
//				m_ButtonSkipDuplicate.addActionListener(this);
//				m_ButtonSkipDuplicate.setOpaque(false);
//				m_ButtonSkipDuplicate.setIconTextGap(5);
//				m_ButtonSkipDuplicate.setMargin(new Insets(0,0,0,0));
//				m_ButtonSkipDuplicate.setHorizontalAlignment(SwingConstants.LEFT);
//				m_ButtonSkipDuplicate.setHorizontalTextPosition(SwingConstants.RIGHT);
//				add(m_ButtonSkipDuplicate);
//				m_ButtonImportBID = new JButton();
//				m_ButtonImportBID.setText("Import from mugimugi ID");
//				m_ButtonImportBID.setIcon(Icon.task_import);
//				m_ButtonImportBID.setSelected(false);
//				m_ButtonImportBID.setFocusable(false);
//				m_ButtonImportBID.addActionListener(this);
//				m_ButtonImportBID.setOpaque(false);
//				m_ButtonImportBID.setIconTextGap(5);
//				m_ButtonImportBID.setMargin(new Insets(0,0,0,0));
//				m_ButtonImportBID.setHorizontalAlignment(SwingConstants.LEFT);
//				m_ButtonImportBID.setHorizontalTextPosition(SwingConstants.RIGHT);
//				add(m_ButtonImportBID);
				
//				m_TabbedPaneImage = new JTabbedPane();
//				m_TabbedPaneImage.setFocusable(false);
//				m_TabbedPaneImage.setTabPlacement(JTabbedPane.RIGHT);
//				m_TabbedPaneImage.addChangeListener(new ChangeListener()
//				{
//                    @Override
//                    public void stateChanged(ChangeEvent ce) {
//                        if (ce.getSource() instanceof JTabbedPane) {
//                            JTabbedPane pane = (JTabbedPane) ce.getSource();
//                            if(pane.getSelectedIndex() == -1)
//                            	return;
//                            JButton selectedTab = (JButton) pane.getComponentAt(pane.getSelectedIndex());
//                            m_ButtonImportBID.setActionCommand(selectedTab.getActionCommand());
//                            m_ButtonImportBID.setText("Import from mugimugi ID [" + m_ButtonImportBID.getActionCommand() + "]");
//                        }
//                    }
//                });
//				add(m_TabbedPaneImage);
				
				new SwingWorker<Void,String>()
				{
					private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					private String prevData = "";
					
					@Override
					protected Void doInBackground() throws Exception {
						Thread.currentThread().setName("plugin-dataimport-clipboardmonitor");
						String data = "";
						while(true)
							try
							{
								// Prevent CPU hogging
								Thread.sleep(1000);
								// Read Clipboard data
								data = (String) clipboard.getData(DataFlavor.stringFlavor);
								// Skip parsing data if it's the same as before
								if(data.equals(prevData))
									continue;
								else
									prevData = data;
								// Parse Clipboard data
								publish(new URL(data).toExternalForm());
							} catch (MalformedURLException murle) {
								; // Not a valid URL, do nothing
							} catch (Exception e) {
								LOG.error("Error parsing Clipboard data {}", data, e);
							}
					}
					@Override
				    protected void process(List<String> chunks) {
						String url = chunks.iterator().next();
						//TODO Parse Metadata from URL
					}
					@Override
					protected void done() { }
				}.execute();
				TaskManager.registerListener(this);
			}
			
//			private void fireInfoUpdated()
//			{
//				switch (m_Task.getInfo())
//				{
//				case COMPLETED:
//					m_LabelTitle.setIcon(Icon.task_info_completed);
//					break;
//				case ERROR:
//					m_LabelTitle.setIcon(Icon.task_info_error);
//					break;
//				case IDLE:
//					m_LabelTitle.setIcon(Icon.task_info_idle);
//					break;
//				case PAUSED:
//					m_LabelTitle.setIcon(Icon.task_info_paused);
//					break;
//				case RUNNING:
//					m_LabelTitle.setIcon(Icon.task_info_running);
//					break;
//				case WARNING:
//					m_LabelTitle.setIcon(Icon.task_info_warning);
//					break;
//				}
//			}
//			
//			private void fireImageUpdated()
//			{
//				m_LabelPreview.setIcon(Icon.loading);
//				new SwingWorker<ImageIcon, Void>()
//				{
//					@Override
//					protected ImageIcon doInBackground() throws Exception
//					{
//						return new ImageIcon(javax.imageio.ImageIO.read(new File(PLUGIN_QUERY, m_Task.getId() + ".png")));
//					}
//					@Override
//				    protected void process(List<Void> chunks) { ; }
//					@Override
//				    public void done() {
//				        ImageIcon icon;
//				        try {
//				        	icon = get();
//				        	m_LabelPreview.setIcon(icon);
//				        } catch (Exception e) {
//				        	m_LabelPreview.setIcon(Icon.task_preview_missing);
//				        }
//				    }
//				}.execute();
//			}
//			
//			private void fireItemsUpdated()
//			{
//				m_TabbedPaneImage.removeAll();
//				m_ButtonImportBID.setText("Import from mugimugi ID");
//				
//				new SwingWorker<Void, Map<String,Object>>()
//				{
//					private transient int selectedTab = -1;
//					
//					@Override
//					protected Void doInBackground() throws Exception
//					{
//						if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) || m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
//						{
//							for(Integer id : m_Task.getDuplicatelist())
//							{
//								try
//								{
//									// Load images from local DataStore
//									ImageIcon ii = new ImageIcon(javax.imageio.ImageIO.read(DataStore.getThumbnail(id).openInputStream()));
//									Map<String,Object> data = new HashMap<String,Object>();
//									data.put("id", id);
//									data.put("imageicon", ii);
//									publish(data);
//								} catch (Exception e) { e.printStackTrace(); }
//							}
//						}
//						if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
//						{
//							for(Integer id : m_Task.getMugimugiList())
//							{
//								try
//								{
//									File file = new File(PLUGIN_IMAGECACHE, "B" + id + ".jpg");
//									ImageIcon ii = new ImageIcon(ImageIO.read(file));
//									Map<String,Object> data = new HashMap<String,Object>();
//									data.put("id", id);
//									data.put("imageicon", ii);
//									publish(data);
//								} catch (Exception e) { e.printStackTrace(); }
//							}
//						}
//						return null;
//					}
//					@Override
//				    protected void process(List<Map<String,Object>> chunks)
//					{
//						for(Map<String,Object> data : chunks)
//						{
//							final Integer id = (Integer) data.get("id");
//							final ImageIcon imageicon = (ImageIcon) data.get("imageicon");
//							
//							JButton button = new JButton(imageicon);
//							button.setActionCommand(id.toString());
//							button.setFocusable(false);
//							if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) || m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
//							{
//								// Open local Book
//								button.addActionListener(new ActionListener()
//								{
//									@Override
//									public void actionPerformed(ActionEvent ae) {
//										new SwingWorker<Void, Void>()
//										{
//											@Override
//											protected Void doInBackground() throws Exception
//											{
//												QueryBook qid = new QueryBook();
//												qid.Id = id;
//												RecordSet<Book> set = DataBase.getBooks(qid);
//												if(set.size() == 1)
//													UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
//												return null;
//											}
//										}.execute();
//									}
//								});
//								m_TabbedPaneImage.addTab("", button);
//							}
//							if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
//							{
//								// Open mugimugi Book
//								button.addActionListener(new ActionListener()
//								{
//									@Override
//									public void actionPerformed(ActionEvent ae) {
//										try {
//											URI uri = new URI(DataImport.DOUJINSHIDB_URL + "book/" + id + "/");
//											Desktop.getDesktop().browse(uri);
//										} catch (URISyntaxException urise) {
//											urise.printStackTrace();
//										} catch (IOException ioe) {
//											ioe.printStackTrace();
//										}
//									}
//								});
//								if((m_Task.getMugimugiBid()).equals(id))
//								{
//									m_TabbedPaneImage.addTab("", Icon.task_searchquery_star, button);
//									selectedTab = m_TabbedPaneImage.getTabCount() - 1;
//								}
//								else
//									m_TabbedPaneImage.addTab("", button);
//							}
//						}
//					}
//					@Override
//				    protected void done()
//					{
//						if(selectedTab != -1)
//							m_TabbedPaneImage.setSelectedIndex(selectedTab);
//					}
//				}.execute();
//			}

			public void setTask(Task task) {
				m_Task = task;
				m_LabelTitle.setText(m_Task.file);
				if(task.equals(TaskManager.getRunningTask()))
					m_LabelTitle.setIcon(Icon.task_state_running);
				else
					switch (task.state)
					{
					case NEW:
						m_LabelTitle.setIcon(Icon.task_state_new);
						break;
					case COMPLETE:
						m_LabelTitle.setIcon(Icon.task_state_complete);
						break;
					case ERROR:
						m_LabelTitle.setIcon(Icon.task_state_error);
						break;
					case WARNING:
						m_LabelTitle.setIcon(Icon.task_state_warning);
						break;
					case ABORT:
						m_LabelTitle.setIcon(Icon.task_state_abort);
						break;
					case UNKNOW:
						m_LabelTitle.setIcon(Icon.task_state_unknow);
						break;
					}
				try {
					m_LabelPreview.setIcon(TaskManager.getImage(task));
				} catch (IOException ioe) {
					m_LabelPreview.setIcon(Icon.task_preview_missing);
				}
				m_TabbedPaneMetadata.removeAll();
				if(task.exception == null) {
					for(Metadata md : task.metadata) {
						if(md.exception == null) {
							m_TabbedPaneMetadata.addTab(md.provider(), Icon.task_state_complete, new MetadataUI(md));
						} else {
							JTextArea text = new JTextArea(md.exception);
							text.setEditable(false);
							text.setFocusable(false);
							text.setMargin(new Insets(5,5,5,5)); 
							m_TabbedPaneMetadata.addTab(md.provider(), Icon.task_state_error, new JScrollPane(text));
						}
					}
				} else {
					JTextArea text = new JTextArea(task.exception);
					text.setEditable(false);
					text.setFocusable(false);
					text.setMargin(new Insets(5,5,5,5)); 
					m_TabbedPaneMetadata.addTab("exception", Icon.task_state_error, new JScrollPane(text));
				}
//				// Display 'status' Icon
//				fireInfoUpdated();
//				// Display scanned Image
//				fireImageUpdated();
//				// Display duplicate/queried Items
//				fireItemsUpdated();
			}

			@Override
			public void layoutContainer(Container parent) {
				int width = parent.getWidth(),
					height = parent.getHeight();
				m_LabelTitle.setBounds(0, 0, width - 80, 20);
				m_LabelPreview.setBounds(0, 20, 200, 256);
				m_ButtonClose.setBounds(width - 20, 0, 20, 20);
				m_ButtonOpenFolder.setBounds(width - 40, 0, 20, 20);
				m_ButtonOpenXML.setBounds(width - 60, 0, 20, 20);
				m_TabbedPaneMetadata.setBounds(200, 20, width - 200, height - 24);
//				m_ButtonRunAgain.setBounds(200, 40, (width - 200) / 2, 20);
//				m_ButtonOpenBook.setBounds(200 + (width - 200) / 2, 20, (width - 200) / 2, 20);
//				m_ButtonSkipDuplicate.setBounds(200 + (width - 200) / 2, 40, (width - 200) / 2, 20);
//				m_ButtonImportBID.setBounds(200 + (width - 200) / 2, 60, (width - 200) / 2, 20);
//				m_TabbedPaneImage.setBounds(0, 0, 0, 0);
//				if(m_Task.isRunning()) {
//					m_ButtonOpenFolder.setEnabled(true);
//					m_ButtonRunAgain.setEnabled(false);
//					m_ButtonOpenXML.setEnabled(false);
//					m_ButtonOpenBook.setEnabled(false);
//					m_ButtonSkipDuplicate.setEnabled(false);
//					m_ButtonImportBID.setEnabled(false);
//					return;
//				}
//				if(m_Task.getInfo().equals(Task.Info.COMPLETED)) {
//					m_ButtonOpenFolder.setEnabled(true);
//					m_ButtonRunAgain.setEnabled(false);
//					m_ButtonOpenXML.setEnabled(false);
//					m_ButtonOpenBook.setEnabled(true);
//					m_ButtonSkipDuplicate.setEnabled(false);
//					m_ButtonImportBID.setEnabled(false);
//					return;
//				}
//				if(m_Task.getInfo().equals(Task.Info.IDLE))
//				{
//					m_ButtonOpenFolder.setEnabled(true);
//					m_ButtonRunAgain.setEnabled(false);
//					m_ButtonOpenXML.setEnabled(false);
//					m_ButtonOpenBook.setEnabled(false);
//					m_ButtonSkipDuplicate.setEnabled(false);
//					m_ButtonImportBID.setEnabled(false);
//					return;
//				}
//				if(m_Task.getInfo().equals(Task.Info.WARNING))
//				{
//					m_ButtonOpenFolder.setEnabled(true);
//					m_ButtonRunAgain.setEnabled(false);
//					m_ButtonOpenBook.setEnabled(false);
//					if(m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) ||
//						m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY))
//					{
//						m_ButtonOpenXML.setEnabled(false);
//						m_ButtonSkipDuplicate.setEnabled(true);
//						m_ButtonImportBID.setEnabled(false);
//						m_TabbedPaneImage.setBounds(200, 80, width - 200, height - 80);
//					}
//					if(m_Task.getExec().equals(Task.Exec.PARSE_XML))
//					{
//						m_ButtonOpenXML.setEnabled(true);
//						m_ButtonSkipDuplicate.setEnabled(false);
//						m_ButtonImportBID.setEnabled(true);
//						m_TabbedPaneImage.setBounds(200, 80, width - 200, height - 80);
//					}
//					return;
//				}
//				if(m_Task.getInfo().equals(Task.Info.ERROR))
//				{
//					m_ButtonOpenFolder.setEnabled(true);
//					m_ButtonRunAgain.setEnabled(true);
//					if(m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY) ||
//						m_Task.getExec().equals(Task.Exec.PARSE_XML) ||
//						m_Task.getExec().equals(Task.Exec.PARSE_BID) ||
//						m_Task.getExec().equals(Task.Exec.SAVE_DATABASE) ||
//						m_Task.getExec().equals(Task.Exec.SAVE_DATASTORE))
//						m_ButtonOpenXML.setEnabled(true);
//					m_ButtonOpenBook.setEnabled(false);
//					m_ButtonSkipDuplicate.setEnabled(false);
//					m_ButtonImportBID.setEnabled(false);
//					return;
//				}
			}
			
			@Override
			public void addLayoutComponent(String key,Component c) { }
			
			@Override
			public void removeLayoutComponent(Component c) { }
			
			@Override
			public Dimension minimumLayoutSize(Container parent) {
			    return getMinimumSize();
			}
			
			@Override
			public Dimension preferredLayoutSize(Container parent) {
			    return getPreferredSize();
			}
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(ae.getSource() == m_ButtonClose) {
					m_SplitPane.setBottomComponent(null);
					m_PanelTasks.clearSelection();
					return;
				}
				if(ae.getSource() == m_ButtonOpenFolder) {
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							try {
								URI uri = new File(m_Task.file).toURI();
								Desktop.getDesktop().browse(uri);
							} catch (IOException ioe) {
								LOG.error("Error opening Task folder {}", m_Task.file, ioe);
							}
							return null;
						}
					}.execute();
					return;
				}
				if(ae.getSource() == m_ButtonOpenXML) {
					new SwingWorker<Void,Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							try {
								File file = File.createTempFile("task-" + m_Task.id, ".xml");
								file.deleteOnExit();
								TaskManager.save(m_Task, file);
								URI uri = file.toURI();
								Desktop.getDesktop().browse(uri);
							} catch (IOException ioe) {
								LOG.error("Error opening Task raw XML for {}", m_Task, ioe);
							}
							return null;
						}
					}.execute();
					return;
				}
//				if(ae.getSource() == m_ButtonOpenBook) {
//					if(m_Task.getBook() == null)
//						return;
//					new SwingWorker<Void,Void>()
//					{
//						@Override
//						protected Void doInBackground() throws Exception {
//							Integer bookid = m_Task.getBook();
//							if(bookid != null)
//							{
//								QueryBook qid = new QueryBook();
//								qid.Id = bookid;
//								RecordSet<Book> set = DataBase.getBooks(qid);
//								if(set.size() == 1)
//									UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
//							}
//							return null;
//						}
//					}.execute();
//					return;
//				}
//				if(ae.getSource() == m_ButtonImportBID) {
//					m_Task.setMugimugiBid(Integer.parseInt(m_ButtonImportBID.getActionCommand()));
//					m_Task.setInfo(Task.Info.IDLE);
//					return;
//				}
//				if(ae.getSource() == m_ButtonSkipDuplicate) {
//					m_Task.setInfo(Task.Info.IDLE);
//					return;
//				}
			}

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				if(m_Task == null)
					return;
				if(pce.getPropertyName().equals("taskmanager-info")) {
					doLayout();
					return;
				}
				if(pce.getPropertyName().equals("task-exec")) {
					;
				}
//				if(pce.getPropertyName().equals("task-info")) {
//					fireInfoUpdated();
//					if(m_Task.getInfo().equals(Task.Info.WARNING) && (
//						m_Task.getExec().equals(Task.Exec.CHECK_DUPLICATE) ||
//						m_Task.getExec().equals(Task.Exec.CHECK_SIMILARITY) ||
//						m_Task.getExec().equals(Task.Exec.PARSE_XML)))
//						fireItemsUpdated();
//					doLayout();
//					return;
//				}
//				if(pce.getPropertyName().equals("task-image")) {
//					m_LabelPreview.setIcon(Icon.loading);
//					new SwingWorker<ImageIcon, Void>()
//					{
//						@Override
//						protected ImageIcon doInBackground() throws Exception
//						{
//							return new ImageIcon(javax.imageio.ImageIO.read(new File(PLUGIN_QUERY, m_Task.getId() + ".png")));
//						}
//						@Override
//					    protected void process(List<Void> chunks) { ; }
//						@Override
//					    public void done() {
//					        ImageIcon icon;
//					        try {
//					        	icon = get();
//					        	m_LabelPreview.setIcon(icon);
//					        } catch (Exception e) {
//					        	m_LabelPreview.setIcon(Icon.task_preview_missing);
//					        	doLayout();
//					        }
//					    }
//					}.execute();
//					return;
//				}
			}
			
			private final class MetadataUI extends JPanel implements LayoutManager, ActionListener, PropertyChangeListener
			{
				private JLabel mMetaName;
				private JLabel mMetaThumbnail;
				private JLabel mMetaAlias;
				private JLabel mMetaTranslation;
				private JLabel mMetaPages;
				private JLabel mMetaTimestamp;
				private JLabel mMetaType;
				private JLabel mMetaAdult;
				private JLabel mMetaInfo;
				private JLabel mMetaSize;
				private JLabel mMetaConvention;
				private JButton mMetaURI;
				private JList<MetaMedia> mMetaList;
				private DefaultListModel<MetaMedia> mMetaListModel;
				private JScrollPane mMetaListScroll;
				
				private final SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd");
				
				public MetadataUI(Metadata md) {
					super.setLayout(this);
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					
					// Metadata.thumbnail
					try {
						mMetaThumbnail  = new JLabel(new ImageIcon(new URL("" + md.thumbnail)));
					} catch (MalformedURLException murle) {
						mMetaThumbnail  = new JLabel(Icon.task_preview_missing);
					}
					add(mMetaThumbnail);
					// Metadata.name
					mMetaName = new JLabel(String.format("name : %s", ifNull(md.name, "")));
					add(mMetaName);
					// Metadata.alias
					// must first convert Set<String>.class to String.class to be shown in a JLabel
					String alias = "[";
					int count = 0;
					for(String a : md.alias)
						if(count++ == 0)
							alias += a;
						else
							alias += ", " + a;
					alias += "]";
					mMetaAlias = new JLabel(String.format("alias : %s", alias));
					add(mMetaAlias);
					// Metadata.translation
					mMetaTranslation = new JLabel(String.format("translation : %s", ifNull(md.translation, "")));
					add(mMetaTranslation);
					// Metadata.pages
					mMetaPages = new JLabel(String.format("pages : %d", ifNull(md.pages, 0)));
					add(mMetaPages);
					// Metadata.timestamp
					mMetaTimestamp = new JLabel(String.format("timestamp : %s", (md.timestamp == null ? "" : mSDF.format(new Date(md.timestamp)))));
					add(mMetaTimestamp);
					// Metadata.type
					mMetaType = new JLabel(String.format("type : %s", ifNull(md.type, "")));
					add(mMetaType);
					// Metadata.adult
					mMetaAdult = new JLabel(String.format("adult : %s", ifNull(md.adult, "")));
					add(mMetaAdult);
					// Metadata.info
					mMetaInfo = new JLabel(String.format("info : %s", ifNull(md.info, "")));
					add(mMetaInfo);
					// Metadata.size
					mMetaSize = new JLabel(String.format("size : %s", format(md.size == null ? 0 : md.size)));
					add(mMetaSize);
					// Metadata.convention
					mMetaConvention = new JLabel(String.format("convention : %s", ifNull(md.convention, "")));
					add(mMetaConvention);
					// Metadata.uri
					// don't add to content pane if it's null
					mMetaURI = new JButton(md.uri);
					mMetaURI.addActionListener(this);
					mMetaURI.setFocusable(false);
					mMetaURI.setBorder(null);
					mMetaURI.setHorizontalAlignment(JButton.LEFT);
					if(md.uri != null)
						add(mMetaURI);
					// Metadata.[artist, circle, content, parody]
					mMetaList = new JList<MetaMedia>();
					mMetaList.setModel(mMetaListModel = new DefaultListModel<MetaMedia>());
					for(String a : md.artist)
						mMetaListModel.addElement(new MetaMediaArtist(a));
					for(String a : md.circle)
						mMetaListModel.addElement(new MetaMediaCircle(a));
					for(String a : md.content)
						mMetaListModel.addElement(new MetaMediaContent(a));
					for(String a : md.parody)
						mMetaListModel.addElement(new MetaMediaParody(a));
					mMetaList.setCellRenderer(new MetadataListCellRenderer());
					add(mMetaListScroll = new JScrollPane(mMetaList));
				}
				
				private abstract class MetaMedia
				{
					private final String mValue;
					private MetaMedia() {
						this.mValue = "";
					}
					private MetaMedia(String value) {
						this.mValue = value;
					}
					private final String getValue() {
						return mValue;
					}
					protected abstract Icon getIcon();
				}
				
				private final class MetaMediaArtist extends MetaMedia
				{
					private MetaMediaArtist(String value) {
						super(value);
					}
					protected final Icon getIcon() {
						return Icon.task_metadata_artist;
					}
				}
				private final class MetaMediaCircle extends MetaMedia
				{
					private MetaMediaCircle(String value) {
						super(value);
					}
					protected final Icon getIcon() {
						return Icon.task_metadata_circle;
					}
				}
				private final class MetaMediaContent extends MetaMedia
				{
					private MetaMediaContent(String value) {
						super(value);
					}
					protected final Icon getIcon() {
						return Icon.task_metadata_content;
					}
				}
				private final class MetaMediaParody extends MetaMedia
				{
					private MetaMediaParody(String value) {
						super(value);
					}
					protected final Icon getIcon() {
						return Icon.task_metadata_parody;
					}
				}
				
				private final class MetadataListCellRenderer extends DefaultListCellRenderer
				{
					private final JLabel mLabel = new JLabel();
					@SuppressWarnings("rawtypes")
					@Override
				    public Component getListCellRendererComponent(
				            JList list,
				            Object value,
				            int index,
				            boolean selected,
				            boolean expanded) {
						MetaMedia media = (MetaMedia) value;
						mLabel.setIcon(media.getIcon());
						mLabel.setText(media.getValue());
//				        if (selected) {
//				            label.setBackground(backgroundSelectionColor);
//				            label.setForeground(textSelectionColor);
//				        } else {
//				            label.setBackground(backgroundNonSelectionColor);
//				            label.setForeground(textNonSelectionColor);
//				        }
				        return mLabel;
				    }
				}
				
				private String format(long bytes)
				{
					int unit = 1024;
				    if (bytes < unit) return bytes + " B";
				    int exp = (int) (Math.log(bytes) / Math.log(unit));
				    String pre = ("KMGTPE").charAt(exp-1) + ("i");
				    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
				}
				
				private <T> T ifNull(T couldBeNull, T returnNotNull) {
					return (couldBeNull == null ? returnNotNull : couldBeNull);
				}
				
				@Override
				public void propertyChange(PropertyChangeEvent pce) {
					// TODO Auto-generated method stub
				}

				@Override
				public void actionPerformed(ActionEvent ae) {
					/**
					 * Browse Metadata URI in user Desktop
					 */
					if(ae.getSource().equals(mMetaURI)) {
						String uri = mMetaURI.getText();
						try {
							Desktop.getDesktop().browse(new URI(uri));
						} catch (URISyntaxException | IOException e) {
							LOG.error("Error opening URI {}", uri,  e);
						}
						return;
					}
				}

				@Override
				public void addLayoutComponent(String name, Component comp) { }

				@Override
				public void removeLayoutComponent(Component comp) { }

				@Override
				public Dimension preferredLayoutSize(Container parent) {
					return getPreferredSize();
				}

				@Override
				public Dimension minimumLayoutSize(Container parent) {
					return getMinimumSize();
				}

				@Override
				public void layoutContainer(Container parent) {
					int width = parent.getWidth(),
						height = parent.getHeight();
					int offset = 0;
					int xoffset = width / 3;
					mMetaThumbnail.setBounds(0,0,xoffset,height);
					mMetaName.setBounds(xoffset,0,xoffset,20);
					mMetaAlias.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaTranslation.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaPages.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaTimestamp.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaType.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaAdult.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaInfo.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaSize.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaConvention.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaURI.setBounds(xoffset,offset+=20,xoffset,20);
					mMetaListScroll.setBounds(xoffset*2,0,xoffset,height);
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent pce) {
			if(pce.getPropertyName().equals("taskmanager-info")) {
				m_LabelTasks.setText("Tasks : " + TaskManager.size());
				if(TaskManager.isRunning())
					m_ButtonTaskManagerCtl.setIcon(Icon.task_pause);
				else
					m_ButtonTaskManagerCtl.setIcon(Icon.task_resume);
				return;
			}
			if(pce.getPropertyName().equals("api-info")) {
				doLayout();
				return;
			}
		}
	}

	@Override
	protected void doInstall() throws TaskException { }

	@Override
	protected void doUpdate() throws TaskException { }

	@Override
	protected void doUninstall() throws TaskException { }
	
	@Override
	protected void doStartup() throws TaskException {
		try {
			ConfigurationParser.fromXML(Configuration.class, CONFIG_FILE);
			LOG.debug("Loaded Configuration from {}", CONFIG_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error loading Configuration from {}", CONFIG_FILE.getName(), ioe);
		}
		TaskManager.load();
		TaskManager.start();
		/** 
		 * UI should be loaded after TaskManager data (TaskSet) 
		 * so every graphical compontent (JTable.tableModel) touching 
		 * the actual data (TaskManager.taskSet) doesn't need to be 
		 * updated specifically.
		 */
		// FIXME Find a method to notify JTable.tableModel that TaskManager.taskSet was updated
		m_UI = new PluginUI();
	}
	
	@Override
	protected void doShutdown() throws TaskException {
		try {
			ConfigurationParser.toXML(Configuration.class, CONFIG_FILE);
			LOG.debug("Saved Configuration to {}", CONFIG_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error saving Configuration to {}", CONFIG_FILE.getName(), ioe);
		}
		TaskManager.stop();
		TaskManager.save();
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
