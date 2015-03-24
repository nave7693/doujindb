package org.dyndns.doujindb.plug.impl.dataimport;

import static org.dyndns.doujindb.ui.UI.Icon;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.conf.ConfigurationParser;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.plug.impl.dataimport.Task.Duplicate.Option;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.PanelBook;
import org.dyndns.doujindb.ui.dialog.PanelBookMedia;
import org.dyndns.doujindb.ui.dialog.PanelConfiguration;
import org.dyndns.doujindb.ui.dialog.util.BookRatingEditor;
import org.dyndns.doujindb.ui.dialog.util.TabbedPaneUIEx;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxConvention;
import org.dyndns.doujindb.ui.dialog.util.list.ListArtist;
import org.dyndns.doujindb.ui.dialog.util.list.ListCircle;
import org.dyndns.doujindb.ui.dialog.util.list.ListContent;
import org.dyndns.doujindb.ui.dialog.util.list.ListParody;
import org.dyndns.doujindb.ui.dialog.util.list.RecordList;
import org.dyndns.doujindb.util.ImageTool;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**  
* DoujinshiDBScanner.java - Plugin to batch process media files thanks to the DoujinshiDB project APIs.
* @author  nozomu
* @version 1.3
*/
public final class DataImport extends Plugin
{
	static final String mAuthor = "loli10K";
	static final String mVersion = "0.1";
	static final String mWeblink = "https://github.com/loli10K";
	static final String mName = "Data Import";
	static final String mDescription = "Batch process media files";
	private PluginUI mUI;
	
	private static Icons mIcons = new Icons();
	
	private TaskManager mTaskManager = new TaskManager(PLUGIN_HOME);
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataImport.class);
	
	@Override
	public Icon getIcon() {
		return mIcons.icon;
	}
	
	@Override
	public String getName() {
		return mName;
	}
	
	@Override
	public String getDescription() {
		return mDescription;
	}
	
	@Override
	public String getVersion() {
		return mVersion;
	}
	
	@Override
	public String getAuthor() {
		return mAuthor;
	}
	
	@Override
	public String getWeblink() {
		return mWeblink;
	}
	
	@Override
	public JComponent getUI() {
		return mUI;
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
			m_TabbedPane.setFont(UI.Font);
			m_TabbedPane.setFocusable(false);
			m_TabTasks = new JPanel();
			m_TabTasks.setLayout(null);
			m_ButtonTaskAdd = new JButton(mIcons.add);
			m_ButtonTaskAdd.addActionListener(this);
			m_ButtonTaskAdd.setBorder(null);
			m_ButtonTaskAdd.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskAdd);
			m_ButtonTaskManagerCtl = new JButton(mIcons.task_resume);
			m_ButtonTaskManagerCtl.addActionListener(this);
			m_ButtonTaskManagerCtl.setBorder(null);
			m_ButtonTaskManagerCtl.setToolTipText("Resume Worker");
			m_ButtonTaskManagerCtl.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskManagerCtl);
			m_LabelTasks = new JLabel("");
			m_LabelTasks.setText("Tasks : " + mTaskManager.size());
			m_TabTasks.add(m_LabelTasks);
			m_ButtonTaskDelete = new JButton(mIcons.task_delete);
			m_ButtonTaskDelete.addActionListener(this);
			m_ButtonTaskDelete.setBorder(null);
			m_ButtonTaskDelete.setToolTipText("Delete");
			m_ButtonTaskDelete.setFocusable(false);
			m_TabTasks.add(m_ButtonTaskDelete);
			m_ButtonTaskReset = new JButton(mIcons.task_reset);
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
			m_TabbedPane.addTab("Tasks", mIcons.tasks, m_TabTasks);
			PanelConfiguration panelConfig = new PanelConfiguration(Configuration.class);
			panelConfig.setConfigurationFile(CONFIG_FILE);
			m_TabbedPane.addTab("Configuration", mIcons.settings, m_TabConfiguration = panelConfig);
			super.add(m_TabbedPane);
			mTaskManager.registerListener(this);
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
						mTaskManager.add(file);
					}
					m_PanelTasks.dataChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskManagerCtl) {
				if(mTaskManager.isRunning())
				{
					m_ButtonTaskManagerCtl.setIcon(mIcons.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							mTaskManager.pause();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(mIcons.task_resume);
						}
					}.execute();
				} else {
					m_ButtonTaskManagerCtl.setIcon(mIcons.loading);
					new SwingWorker<Void,Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							mTaskManager.resume();
							return null;
						}
						@Override
						protected void done() {
							m_ButtonTaskManagerCtl.setIcon(mIcons.task_pause);
						}
					}.execute();
				}
				return;
			}
			if(ae.getSource() == m_ButtonTaskDelete) {
				List<Task> selected = new Vector<Task>();
				for(Task task : mTaskManager.tasks()) {
					if(task.isSelected()) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected) {
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					mTaskManager.remove(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_ButtonTaskReset) {
				List<Task> selected = new Vector<Task>();
				for(Task task : mTaskManager.tasks()) {
					if(task.isSelected()) {
						selected.add(task);
					}
				}
				if(selected.isEmpty())
					return;
				for(Task task : selected) {
					// If details panel is open, close it
					if(task.equals(m_PanelTask.m_Task))
						m_SplitPane.setBottomComponent(null);
					mTaskManager.reset(task);
				}
				m_PanelTasks.dataChanged();
			}
			if(ae.getSource() == m_CheckboxSelection) {
				for(Task task : mTaskManager.tasks())
					task.setSelected(m_CheckboxSelection.isSelected());
				m_PanelTasks.dataChanged();
				return;
			}
		}
		
		/**
		 * Return a "display" (Image)Icon based on current Task status
		 */
		private Icon getDisplayIcon(Task task) {
			// Task is running => "Running" icon
			if(task.equals(mTaskManager.getRunningTask()))
				return mIcons.task_state_running;
			// Task is brand new => "New" icon
			if(task.getState().equals(Task.State.NEW))
				return mIcons.task_state_new;
			else {
				// Task has experienced at least 1 error => "Error" icon
				if(task.hasErrors())
					return mIcons.task_state_error;
				// Task has experienced at least 1 warning => "Warning" icon
				if(task.hasWarnings())
					return mIcons.task_state_warning;
				// At least 1 Metadata has experienced errors => "Warning" icon
				for(Metadata md : task.metadata())
					if(md.exception != null)
						return mIcons.task_state_warning;
				// Task is done => "Complete" icon
				if(task.getState().equals(Task.State.DONE))
					return mIcons.task_state_complete;
			}
			// Task is in unknown state => "Unknown" icon
			return mIcons.task_state_unknow;
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
				m_TableModel.addColumn("State"); // State
				m_TableModel.addColumn("Task"); // Name | Id | File
				m_TableModel.addColumn(""); // Selection
				m_TableRender = new TaskRenderer();
				m_TableEditor = new TaskEditor();
				m_TableSorter = new TableRowSorter<DefaultTableModel>(m_TableModel);
				super.setRowSorter(m_TableSorter);
				super.setModel(m_TableModel);
				super.setFont(UI.Font);
				super.getTableHeader().setReorderingAllowed(false);
				super.getColumnModel().getColumn(0).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(1).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(1).setCellEditor(m_TableEditor);
				super.getColumnModel().getColumn(2).setCellRenderer(m_TableRender);
				super.getColumnModel().getColumn(0).setResizable(false);
				super.getColumnModel().getColumn(0).setMaxWidth(150);
				super.getColumnModel().getColumn(0).setMinWidth(140);
				super.getColumnModel().getColumn(0).setWidth(140);
				super.getColumnModel().getColumn(1).setResizable(true);
				super.getColumnModel().getColumn(2).setResizable(false);
				super.getColumnModel().getColumn(2).setMaxWidth(20);
				super.getColumnModel().getColumn(2).setMinWidth(20);
				super.getColumnModel().getColumn(2).setWidth(20);
				super.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
							task.setSelected(true);
						}
						if ((me.getModifiersEx() & (BUTTON1 | BUTTON2 | BUTTON3)) == BUTTON3) {
							task.setSelected(false);
						}
						dataChanged();
					}
				});
				
				mTaskManager.registerListener(this);
			}
			
			public void dataChanged() {
				m_TableModel.fireTableDataChanged();
			}
			
			private final class TaskSetTableModel extends DefaultTableModel
			{
				private TaskSetTableModel() { }
				
				@Override
				public int getRowCount() {
					return mTaskManager.size();
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
					Task task = mTaskManager.get(rowIndex);
					switch(columnIndex) {
						case -1:
							return task;
						case 0:
							return task.getState();
						case 1:
							return task.getFile();
						case 2:
							return task.isSelected();
					}
					throw new IllegalArgumentException("Argument columnIndex (= " + columnIndex + ") must be 0 < X < " + m_Types.length);
				}
				
				@Override
				public void setValueAt(Object value, int rowIndex, int columnIndex) {
					Task task = mTaskManager.get(rowIndex);
				    if (columnIndex == 2) {
				    	task.setSelected((Boolean)value);
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
				    super.setFont(UI.Font);
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
						m_Label.setIcon(getDisplayIcon(task));
						m_Label.setText(task.getState().toString());
						m_Label.setForeground(mLabelForeground);
						m_Label.setBackground(mLabelBackground);
						return m_Label;
					}
					if(column == 1) {
						m_Label.setIcon(null);
						m_Label.setText(task.getFile());
						if(task.isSelected()) {
							m_Label.setBackground(mLabelForeground);
							m_Label.setForeground(mLabelBackground);
						} else {
							m_Label.setForeground(mLabelForeground);
							m_Label.setBackground(mLabelBackground);
						}
						return m_Label;
					}
					if(column == 2) {
						m_CheckBox.setSelected(task.isSelected());
						return m_CheckBox;
					}
					return this;
				}
			}

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				if(pce.getPropertyName().equals("task-info"))
					dataChanged();
				if(pce.getPropertyName().equals("taskmanager-info"))
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
			private JSplitPane mSplitPane;
//			private JButton m_ButtonOpenBook;
//			private JButton m_ButtonRunAgain;
//			private JButton m_ButtonSkipDuplicate;
//			private JButton m_ButtonImportBID;
//			private JTabbedPane m_TabbedPaneImage;
			private JComponent mStateUI;
			
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
				m_ButtonClose = new JButton();
				m_ButtonClose.setText("");
				m_ButtonClose.setToolTipText("Close");
				m_ButtonClose.setIcon(mIcons.cancel);
				m_ButtonClose.setSelected(true);
				m_ButtonClose.setFocusable(false);
				m_ButtonClose.addActionListener(this);
				add(m_ButtonClose);
				m_ButtonOpenFolder = new JButton();
				m_ButtonOpenFolder.setText("");
				m_ButtonOpenFolder.setToolTipText("View Folder");
				m_ButtonOpenFolder.setIcon(mIcons.task_folder);
				m_ButtonOpenFolder.setSelected(false);
				m_ButtonOpenFolder.setFocusable(false);
				m_ButtonOpenFolder.addActionListener(this);
				add(m_ButtonOpenFolder);
				m_ButtonOpenXML = new JButton();
				m_ButtonOpenXML.setText("");
				m_ButtonOpenXML.setToolTipText("View XML");
				m_ButtonOpenXML.setIcon(mIcons.task_xml);
				m_ButtonOpenXML.setSelected(false);
				m_ButtonOpenXML.setFocusable(false);
				m_ButtonOpenXML.addActionListener(this);
				add(m_ButtonOpenXML);
				m_LabelPreview = new JLabel();
				m_LabelPreview.setIcon(mIcons.task_preview_missing);
				m_LabelPreview.setHorizontalAlignment(JLabel.CENTER);
				m_LabelPreview.setVerticalAlignment(JLabel.CENTER);
				add(m_LabelPreview);
				
				mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				mSplitPane.setLeftComponent(new JPanel());
				mSplitPane.setRightComponent(new JPanel());
				mSplitPane.setDividerSize(1);
				mSplitPane.setEnabled(false);
				add(mSplitPane);
				
//				m_TabbedPaneMetadata = new JTabbedPane();
//				m_TabbedPaneMetadata.setFocusable(false);
//				m_TabbedPaneMetadata.setTabPlacement(JTabbedPane.TOP);
//				m_ButtonOpenBook = new JButton();
//				
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
								Transferable transferable = clipboard.getContents(this);
								if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
									continue;
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
				mTaskManager.registerListener(this);
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
				m_LabelTitle.setText(m_Task.getFile());
				m_LabelTitle.setIcon(getDisplayIcon(task));
				try {
					m_LabelPreview.setIcon(new ImageIcon(javax.imageio.ImageIO.read(new File(task.getThumbnail()))));
				} catch (Exception e) {
					m_LabelPreview.setIcon(mIcons.task_preview_missing);
				}
				mSplitPane.setLeftComponent(m_LabelPreview);
//				m_TabbedPaneMetadata.removeAll();
				if(task.hasDuplicates()) {
					mSplitPane.setRightComponent(mStateUI = new DuplicateUI(task.duplicates()));
				} else
				if(!task.hasErrors()) {
//					for(Metadata md : task.metadata) {
//						try {
//							m_TabbedPaneMetadata.addTab(String.format("%s (%d%%)", md.provider(), md.score), mIcons.task_result_info, new BookCoverLabel(new ImageIcon(new URL("" + md.thumbnail)), md.score));
//						} catch (MalformedURLException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					for(Metadata md : task.metadata) {
//						if(md.exception == null) {
//							m_TabbedPaneMetadata.addTab(md.provider(), mIcons.task_state_complete, new MetadataUI(md));
//						} else {
//							m_TabbedPaneMetadata.addTab(md.provider(), mIcons.task_state_error, new MetadataUI(md));
//						}
//					}
					mSplitPane.setRightComponent(mStateUI = new MetadataUI(task.metadata()));
				} else {
					JTextArea text = new JTextArea();
					for(String message : task.errors().keySet())
						text.append(message + "\n" + task.errors().get(message) + "\n");
					text.setEditable(false);
					text.setFocusable(false);
					text.setMargin(new Insets(5,5,5,5)); 
//					m_TabbedPaneMetadata.addTab("exception", mIcons.task_state_error, new JScrollPane(text));
					mSplitPane.setRightComponent(new JScrollPane(text));
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						doLayout();
					}
				});
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
//				m_LabelPreview.setBounds(0, 20, 200, 256);
				m_ButtonClose.setBounds(width - 20, 0, 20, 20);
				m_ButtonOpenFolder.setBounds(width - 40, 0, 20, 20);
				m_ButtonOpenXML.setBounds(width - 60, 0, 20, 20);
				mSplitPane.setBounds(0, 20, width, height - 20);
//				m_TabbedPaneMetadata.setBounds(200, 20, width - 200, height - 24);
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
								URI uri = new File(m_Task.getFile()).toURI();
								Desktop.getDesktop().browse(uri);
							} catch (IOException ioe) {
								LOG.error("Error opening Task folder {}", m_Task.getFile(), ioe);
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
								File file = File.createTempFile("task-" + m_Task.getId(), ".xml");
								file.deleteOnExit();
								mTaskManager.save(m_Task, file);
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
				if(pce.getPropertyName().equals("taskmanager-info"))
					doLayout();
			}
			
			private final class BookCoverButton extends JButton {
				private Image image;
				private BookCoverButton(ImageIcon icon) {
					super(icon);
					this.image = icon.getImage();
					this.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent ce) {
                            Dimension size = getScaledDimension(ce.getComponent().getSize());
                            Image scaled = image.getScaledInstance(size.width, size.height, java.awt.Image.SCALE_SMOOTH);
                            setIcon(new ImageIcon(scaled));
                        }
                        /**
                         * Java image resize, maintain aspect ratio
                         * @see http://stackoverflow.com/questions/10245220/java-image-resize-maintain-aspect-ratio
                         */
                        public Dimension getScaledDimension(Dimension boundary) {
    					    int original_width = image.getWidth(null);
    					    int original_height = image.getHeight(null);
    					    int bound_width = boundary.width;
    					    int bound_height = boundary.height;
    					    int new_width = original_width;
    					    int new_height = original_height;
    					    // first check if we need to scale width
    					    if (original_width > bound_width) {
    					        //scale width to fit
    					        new_width = bound_width;
    					        //scale height to maintain aspect ratio
    					        new_height = (new_width * original_height) / original_width;
    					    }
    					    // then check if we need to scale even with the new height
    					    if (new_height > bound_height) {
    					        //scale height to fit instead
    					        new_height = bound_height;
    					        //scale width to maintain aspect ratio
    					        new_width = (new_height * original_width) / original_height;
    					    }
    					    return new Dimension(new_width, new_height);
    					}
                    });
				}
			}
			
			private final class BookCoverLabel extends JLabel {
				private Integer mValue;
				private Color color;
				private BookCoverLabel(final ImageIcon icon, final String url, final Integer value) {
					super(icon);
					this.mValue = value % 255;
					if(mValue != null) {
						color = new Color((255 * (100 - mValue)) / 100, (255 * mValue) / 100, 0);
					} else {
						color = Color.gray;
					}
					super.setBorder(BorderFactory.createLineBorder(color));
					super.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent me) {
							try {
								if(url != null)
									Desktop.getDesktop().browse(new URI(url));
							} catch (IOException | URISyntaxException e) {
								LOG.error("Cannot browse URL", e);
							}
						}
					});
				}
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					int width = 40;
					g.setColor(color);
					g.fillRect(super.getWidth() - width, 0, width, 20);
					g.setColor(super.getBackground());
					int ascent = g.getFontMetrics().getAscent();
					int descent = g.getFontMetrics().getDescent();
					int top = 0;
					int bottom = 20;
					/**
					 * Taken from StackOverflow
					 * @see http://stackoverflow.com/questions/1055851/how-do-you-draw-a-string-centered-vertically-in-java
					 */
					String label;
					if(mValue != null) {
						label = String.format(" %s%% ", mValue);
					} else {
						label = " - ";
					}
					int baseline = (top+((bottom+1-top)/2) - ((ascent + descent)/2) + ascent);
					Rectangle2D r = g.getFontMetrics().getStringBounds(label, g);
					g.drawString(label, (int) (super.getWidth() - width / 2 - r.getWidth() / 2), baseline);
				}
			}
			
			private final class DuplicateUI extends JPanel implements LayoutManager, ActionListener
			{
				private JTabbedPane mTabbedPane;

				public DuplicateUI(Set<Task.Duplicate> duplicates) {
					super.setLayout(this);
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					mTabbedPane = new JTabbedPane();
					mTabbedPane.setFocusable(false);
					super.add(mTabbedPane);
					super.doLayout();
					for(Task.Duplicate duplicate : duplicates)
						addDuplicate(duplicate);
				}

				@Override
				public void actionPerformed(ActionEvent ae) {
					final Integer bookId = Integer.parseInt(ae.getActionCommand());
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							QueryBook qid = new QueryBook();
							qid.Id = bookId;
							RecordSet<Book> set = DataBase.getBooks(qid);
							if(set.size() == 1)
								UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, set.iterator().next());
							return null;
						}
					}.execute();
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
					mTabbedPane.setBounds(0, 0, width, height);
				}

				private void addDuplicate(final Task.Duplicate duplicate) {
					ImageIcon duplicateImage;
					try {
						duplicateImage = new ImageIcon(javax.imageio.ImageIO.read(DataStore.getThumbnail(duplicate.id).openInputStream()));
					} catch (Exception e) {
						duplicateImage = mIcons.task_preview_missing;
						LOG.warn("Error loading cover image for Book {}", duplicate.id, e);
					}
					JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
					split.setDividerSize(1);
					split.setEnabled(false);
					{
						JButton duplicateButton = new BookCoverButton(duplicateImage);
						duplicateButton.setActionCommand("" + duplicate.id);
						duplicateButton.addActionListener(this);
						duplicateButton.setFocusable(false);
						duplicateButton.setMinimumSize(new Dimension(180, 180));
						split.setLeftComponent(duplicateButton);
					}
					{
						JPanel options = new JPanel();
						options.setLayout(new GridBagLayout());
						JPanel metadataPanel = new JPanel();
						metadataPanel.setLayout(new GridLayout(3, 1));
						metadataPanel.setBorder(BorderFactory.createTitledBorder("Metadata"));
						{
							ButtonGroup group = new ButtonGroup();
							JRadioButton radioButtonIgnore = new JRadioButton("IGNORE Metadata for this item");
							group.add(radioButtonIgnore);
							radioButtonIgnore.setFocusable(false);
							radioButtonIgnore.setSelected(false);
							radioButtonIgnore.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.metadataOption = Option.IGNORE;
								}
							});
							if(duplicate.metadataOption.equals(Option.IGNORE))
								radioButtonIgnore.setSelected(true);
							metadataPanel.add(radioButtonIgnore);
							JRadioButton radioButtonMerge = new JRadioButton("MERGE fetched Metadata with existing content");
							group.add(radioButtonMerge);
							radioButtonMerge.setFocusable(false);
							radioButtonMerge.setSelected(false);
							radioButtonMerge.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.metadataOption = Option.MERGE;
								}
							});
							if(duplicate.metadataOption.equals(Option.MERGE))
								radioButtonMerge.setSelected(true);
							metadataPanel.add(radioButtonMerge);
							JRadioButton radioButtonReplace = new JRadioButton("REPLACE existing content with fetched Metedata");
							group.add(radioButtonReplace);
							radioButtonReplace.setFocusable(false);
							radioButtonReplace.setSelected(false);
							radioButtonReplace.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.metadataOption = Option.REPLACE;
								}
							});
							if(duplicate.metadataOption.equals(Option.REPLACE))
								radioButtonReplace.setSelected(true);
							metadataPanel.add(radioButtonReplace);
							GridBagConstraints c = new GridBagConstraints();
							c.fill = GridBagConstraints.HORIZONTAL;
							c.ipady = 0;
							c.ipadx = 0;
							c.gridx = 0;
							c.gridy = 0;
							c.gridwidth = 1;
							c.weightx = 1.0;
							c.weighty = 0.0;
							c.anchor = GridBagConstraints.FIRST_LINE_END;
							options.add(metadataPanel, c);
						}
						JPanel dataPanel = new JPanel();
						dataPanel.setLayout(new GridLayout(3, 1));
						dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
						{
							ButtonGroup group = new ButtonGroup();
							JRadioButton radioButtonIgnore = new JRadioButton("IGNORE Data (files) for this item");
							group.add(radioButtonIgnore);
							radioButtonIgnore.setFocusable(false);
							radioButtonIgnore.setSelected(false);
							radioButtonIgnore.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.dataOption = Option.IGNORE;
								}
							});
							if(duplicate.dataOption.equals(Option.IGNORE))
								radioButtonIgnore.setSelected(true);
							dataPanel.add(radioButtonIgnore);
							JRadioButton radioButtonMerge = new JRadioButton("MERGE new Data into existing files/folders without overwriting");
							group.add(radioButtonMerge);
							radioButtonMerge.setFocusable(false);
							radioButtonMerge.setSelected(false);
							radioButtonMerge.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.dataOption = Option.MERGE;
								}
							});
							if(duplicate.dataOption.equals(Option.MERGE))
								radioButtonMerge.setSelected(true);
							dataPanel.add(radioButtonMerge);
							JRadioButton radioButtonReplace = new JRadioButton("REPLACE existing files/folders with new Data");
							group.add(radioButtonReplace);
							radioButtonReplace.setFocusable(false);
							radioButtonReplace.setSelected(false);
							radioButtonReplace.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent ie) {
									if(((JRadioButton)ie.getSource()).isSelected())
										duplicate.dataOption = Option.REPLACE;
								}
							});
							if(duplicate.dataOption.equals(Option.REPLACE))
								radioButtonReplace.setSelected(true);
							dataPanel.add(radioButtonReplace);
							GridBagConstraints c = new GridBagConstraints();
							c.fill = GridBagConstraints.HORIZONTAL;
							c.ipady = 0;
							c.ipadx = 0;
							c.gridx = 2;
							c.gridy = 0;
							c.gridwidth = 1;
							c.weightx = 1.0;
							c.weighty = 0.0;
							c.anchor = GridBagConstraints.FIRST_LINE_START;
							options.add(dataPanel, c);
						}
						{
							JPanel annotations = new JPanel();
							annotations.setBorder(BorderFactory.createTitledBorder("Annotations"));
							for(String annotation : duplicate.annotations) {
								JButton btnContent = new JButton(annotation);
								btnContent.setFocusable(false);
								btnContent.setBorderPainted(true);
								btnContent.setContentAreaFilled(false);
								btnContent.setFocusPainted(false);
								btnContent.setIcon(mIcons.task_annotation);
								btnContent.setHorizontalTextPosition(SwingConstants.RIGHT);
								btnContent.setMargin(new Insets(2, 2, 2, 2));
								annotations.add(btnContent);
							}
							GridBagConstraints c = new GridBagConstraints();
							c.fill = GridBagConstraints.BOTH;
							c.ipady = 0;
							c.ipadx = 0;
							c.gridx = 0;
							c.gridy = 1;
							c.gridwidth = 3;
							c.weightx = 1.0;
							c.weighty = 1.0;
							c.anchor = GridBagConstraints.PAGE_END;
							options.add(annotations, c);
						}
						split.setRightComponent(options);
					}
					
					mTabbedPane.addTab("Book [" + duplicate.id + "]", mIcons.task_metadata_book, split);
				}
			}

			private final class MetadataUI extends JPanel implements LayoutManager, ActionListener
			{
//				private JLabel mMessage;
//				private JLabel mMetaName;
//				private JLabel mMetaThumbnail;
//				private JLabel mMetaAlias;
//				private JLabel mMetaTranslation;
//				private JLabel mMetaPages;
//				private JLabel mMetaTimestamp;
//				private JLabel mMetaType;
//				private JLabel mMetaAdult;
//				private JLabel mMetaInfo;
//				private JLabel mMetaSize;
//				private JLabel mMetaConvention;
//				private JButton mMetaURI;
//				private JList<MetaMedia> mMetaList;
//				private DefaultListModel<MetaMedia> mMetaListModel;
//				private JScrollPane mMetaListScroll;
				
				private JTabbedPane mPanelProviders;
				private JPanel mPanelInfo;
				private JLabel mLabelJapaneseName;
				private JComboBox<String> mTextJapaneseName;
				private JLabel mLabelTranslatedName;
				private JComboBox<String> mTextTranslatedName;
				private JLabel mLabelRomajiName;
				private JComboBox<String> mTextRomajiName;
				private JLabel mLabelInfo;
				private JTextArea mTextInfo;
				private JScrollPane mScrollInfo;
				private JLabel mLabelDate;
				private JComboBox<String> mTextDate;
				private JLabel mLabelPages;
				private JComboBox<Integer> mTextPages;
				private JLabel mLabelType;
				private JComboBox<String> mComboboxType;
				private JLabel mLabelConvention;
				private JComboBox<String> mComboboxConvention;
				private JLabel mLabelAdult;
				private JCheckBox mCheckboxAdult;
				private JTabbedPane mTabbedPane;
				private JList<MetaWrapper> mListArtists;
				private JList<MetaWrapper> mListCircles;
				private JList<MetaWrapper> mListContents;
				private JList<MetaWrapper> mListParodies;
				
				private final SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd");
				
				public MetadataUI(Iterable<Metadata> list) {
					super.setLayout(this);
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					
//					// Message
//					if(md.message != null) {
//						mMessage  = new JLabel(md.message);
//						mMessage.setHorizontalAlignment(JLabel.CENTER);
//						if(md.exception != null) {
//							mMessage.setForeground(Color.orange);
//							mMessage.setBorder(BorderFactory.createLineBorder(Color.orange));
//						}
//						add(mMessage);
//					}
//					
//					// Metadata.thumbnail
//					try {
//						mMetaThumbnail  = new BookCoverLabel(new ImageIcon(new URL("" + md.thumbnail)), md.score);
//					} catch (MalformedURLException murle) {
//						mMetaThumbnail  = new JLabel(mIcons.task_preview_missing);
//					}
//					add(mMetaThumbnail);
//					// Metadata.name
//					mMetaName = new JLabel(String.format("name : %s", ifNull(md.name, "")));
//					add(mMetaName);
//					// Metadata.alias
//					// must first convert Set<String>.class to String.class to be shown in a JLabel
//					String alias = "[";
//					int count = 0;
//					for(String a : md.alias)
//						if(count++ == 0)
//							alias += a;
//						else
//							alias += ", " + a;
//					alias += "]";
//					mMetaAlias = new JLabel(String.format("alias : %s", alias));
//					add(mMetaAlias);
//					// Metadata.translation
//					mMetaTranslation = new JLabel(String.format("translation : %s", ifNull(md.translation, "")));
//					add(mMetaTranslation);
//					// Metadata.pages
//					mMetaPages = new JLabel(String.format("pages : %d", ifNull(md.pages, 0)));
//					add(mMetaPages);
//					// Metadata.timestamp
//					mMetaTimestamp = new JLabel(String.format("timestamp : %s", (md.timestamp == null ? "" : mSDF.format(new Date(md.timestamp * 1000)))));
//					add(mMetaTimestamp);
//					// Metadata.type
//					mMetaType = new JLabel(String.format("type : %s", ifNull(md.type, "")));
//					add(mMetaType);
//					// Metadata.adult
//					mMetaAdult = new JLabel(String.format("adult : %s", ifNull(md.adult, "")));
//					add(mMetaAdult);
//					// Metadata.info
//					mMetaInfo = new JLabel(String.format("info : %s", ifNull(md.info, "")));
//					add(mMetaInfo);
//					// Metadata.size
//					mMetaSize = new JLabel(String.format("size : %s", format(md.size == null ? 0 : md.size)));
//					add(mMetaSize);
//					// Metadata.convention
//					mMetaConvention = new JLabel(String.format("convention : %s", ifNull(md.convention, "")));
//					add(mMetaConvention);
//					// Metadata.uri
//					// don't add to content pane if it's null
//					mMetaURI = new JButton(md.uri);
//					mMetaURI.addActionListener(this);
//					mMetaURI.setFocusable(false);
//					mMetaURI.setBorder(null);
//					mMetaURI.setHorizontalAlignment(JButton.LEFT);
//					if(md.uri != null)
//						add(mMetaURI);
//					// Metadata.[artist, circle, content, parody]
//					mMetaList = new JList<MetaMedia>();
//					mMetaList.setModel(mMetaListModel = new DefaultListModel<MetaMedia>());
//					for(String a : md.artist)
//						mMetaListModel.addElement(new MetaMediaArtist(a));
//					for(String a : md.circle)
//						mMetaListModel.addElement(new MetaMediaCircle(a));
//					for(String a : md.content)
//						mMetaListModel.addElement(new MetaMediaContent(a));
//					for(String a : md.parody)
//						mMetaListModel.addElement(new MetaMediaParody(a));
//					mMetaList.setCellRenderer(new MetadataListCellRenderer());
//					add(mMetaListScroll = new JScrollPane(mMetaList));
					
					mTabbedPane = new JTabbedPane();
					mTabbedPane.setFocusable(false);
					mLabelJapaneseName = new JLabel("Japanese Name");
					mTextJapaneseName = new JComboBox<String>();
					mTextJapaneseName.setFocusable(false);
					mLabelTranslatedName = new JLabel("Translated Name");
					mTextTranslatedName = new JComboBox<String>();
					mTextTranslatedName.setFocusable(false);
					mLabelRomajiName = new JLabel("Romaji Name");
					mTextRomajiName = new JComboBox<String>();
					mTextRomajiName.setFocusable(false);
					mLabelInfo = new JLabel("Info");
					mTextInfo = new JTextArea("");
					mScrollInfo = new JScrollPane(mTextInfo);
					mLabelType = new JLabel("Type");
					mComboboxType = new JComboBox<String>();
					mComboboxType.setFocusable(false);
					mLabelConvention = new JLabel("Convention");
					mComboboxConvention = new JComboBox<String>();
					mComboboxConvention.setFocusable(false);
					mLabelAdult = new JLabel("Adult");
					mCheckboxAdult = new JCheckBox("", false);
					mCheckboxAdult.setFocusable(false);
					mLabelDate = new JLabel("Date");
					mTextDate = new JComboBox<String>();
					mTextDate.setFocusable(false);
					mLabelPages = new JLabel("Pages");
					mTextPages = new JComboBox<Integer>();
					mTextPages.setFocusable(false);
					mPanelInfo = new JPanel();
					mPanelInfo.setLayout(new LayoutManager()
					{
						@Override
						public void layoutContainer(Container parent) {
							int width = parent.getWidth(),
								height = parent.getHeight(),
								hsize = 18;
							mLabelJapaneseName.setBounds(3, 3, 100, hsize);
							mTextJapaneseName.setBounds(103, 3, width - 106, hsize);
							mLabelTranslatedName.setBounds(3, 3 + hsize, 100, hsize);
							mTextTranslatedName.setBounds(103, 3 + hsize, width - 106, hsize);
							mLabelRomajiName.setBounds(3, 3 + hsize*2, 100, hsize);
							mTextRomajiName.setBounds(103, 3 + hsize*2, width - 106, hsize);
							mLabelConvention.setBounds(3, 3 + hsize*3, 100, hsize);
							mComboboxConvention.setBounds(103, 3 + hsize*3, width - 106, hsize);
							mLabelType.setBounds(3, 3 + hsize*4, 100, hsize);
							mComboboxType.setBounds(103, 3 + hsize*4, 100, hsize);
							mLabelDate.setBounds(3, 3 + hsize*5, 100, hsize);
							mTextDate.setBounds(103, 3 + hsize*5, 100, hsize);
							mLabelPages.setBounds(3, 3 + hsize*6, 100, hsize);
							mTextPages.setBounds(103, 3 + hsize*6, 100, hsize);
							mLabelAdult.setBounds(3, 3 + hsize*7, 100, hsize);
							mCheckboxAdult.setBounds(103, 3 + hsize*7, 100, hsize);
							mLabelInfo.setBounds(3, 3 + hsize*8, width - 6, hsize);
							mScrollInfo.setBounds(3, 3 + hsize*9, width - 6, height - hsize*9 - 6);
						}
						@Override
						public void addLayoutComponent(String key, Component c) { }
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
					});
					mPanelInfo.add(mLabelJapaneseName);
					mPanelInfo.add(mTextJapaneseName);
					mPanelInfo.add(mLabelTranslatedName);
					mPanelInfo.add(mTextTranslatedName);
					mPanelInfo.add(mLabelRomajiName);
					mPanelInfo.add(mTextRomajiName);
					mPanelInfo.add(mLabelInfo);
					mPanelInfo.add(mScrollInfo);
					mPanelInfo.add(mLabelAdult);
					mPanelInfo.add(mCheckboxAdult);
					mPanelInfo.add(mLabelConvention);
					mPanelInfo.add(mComboboxConvention);
					mPanelInfo.add(mLabelDate);
					mPanelInfo.add(mTextDate);
					mPanelInfo.add(mLabelPages);
					mPanelInfo.add(mTextPages);
					mPanelInfo.add(mLabelType);
					mPanelInfo.add(mComboboxType);
					
					mPanelProviders = new JTabbedPane();
					mPanelProviders.setFocusable(false);
					mPanelProviders.setMinimumSize(new Dimension(256, 256));
					mPanelProviders.setTabPlacement(JTabbedPane.LEFT);
					mPanelProviders.setPreferredSize(mPanelProviders.getMinimumSize());
					
					JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
					split.setLeftComponent(mPanelProviders);
					split.setRightComponent(mPanelInfo);
					split.setDividerSize(1);
					split.setEnabled(false);
					
					mTabbedPane.addTab("General", mIcons.task_result_info, split);
					mListArtists = new JList<MetaWrapper>();
					mListArtists.setModel(new DefaultListModel<MetaWrapper>());
					mTabbedPane.addTab("Artists", mIcons.task_result_artist, new JScrollPane(mListArtists));
					mListCircles = new JList<MetaWrapper>();
					mListCircles.setModel(new DefaultListModel<MetaWrapper>());
					mTabbedPane.addTab("Circles", mIcons.task_result_circle, new JScrollPane(mListCircles));
					mListContents = new JList<MetaWrapper>();
					mListContents.setModel(new DefaultListModel<MetaWrapper>());
					mTabbedPane.addTab("Contents", mIcons.task_result_content, new JScrollPane(mListContents));
					mListParodies = new JList<MetaWrapper>();
					mListParodies.setModel(new DefaultListModel<MetaWrapper>());
					mTabbedPane.addTab("Parodies", mIcons.task_result_parody, new JScrollPane(mListParodies));
					MetadataListCellRenderer lcr = new MetadataListCellRenderer();
					MouseAdapter ma = new MouseAdapter() {
						@SuppressWarnings("unchecked")
						public void mousePressed(MouseEvent me) {
							JList<MetaWrapper> list = (JList<MetaWrapper>) me.getSource();
							int index = list.locationToIndex(me.getPoint());
							if (index != -1) {
								MetaWrapper meta = (MetaWrapper) ((DefaultListModel<MetaWrapper>)list.getModel()).getElementAt(index);
								meta.setSelected(!meta.isSelected());
								repaint();
							}
						}
					};
					mListArtists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					mListArtists.setCellRenderer(lcr);
					mListArtists.addMouseListener(ma);
					mListCircles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					mListCircles.setCellRenderer(lcr);
					mListCircles.addMouseListener(ma);
					mListContents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					mListContents.setCellRenderer(lcr);
					mListContents.addMouseListener(ma);
					mListParodies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					mListParodies.setCellRenderer(lcr);
					mListParodies.addMouseListener(ma);
					
					super.add(mTabbedPane);
					super.doLayout();
					
					for(Metadata md : list)
						addMetadata(md);
				}
				
				private void addMetadata(Metadata md) {
					if(!isNull(md.thumbnail))
						try {
							mPanelProviders.addTab(md.provider(), new BookCoverLabel(new ImageIcon(new URL(md.thumbnail)), md.uri, md.score));
						} catch (MalformedURLException murle) { }
					else
						mPanelProviders.addTab(md.provider(), new BookCoverLabel(mIcons.task_preview_missing, null, 0));
					if(!isNull(md.name))
						mTextJapaneseName.addItem(md.name);
					for(String a : md.alias)
						if(!isNull(a))
							mTextJapaneseName.addItem(a);
					if(!isNull(md.translation))
						mTextTranslatedName.addItem(md.translation);
					if(!isNull(md.pages))
						mTextPages.addItem(md.pages);
					if(!isNull(md.timestamp))
						mTextDate.addItem(mSDF.format(new Date(md.timestamp * 1000)));
					if(!isNull(md.type))
						mComboboxType.addItem(md.type);
					if(!isNull(md.adult))
						mCheckboxAdult.setSelected(md.adult);
					if(!isNull(md.convention))
						mComboboxConvention.addItem(md.convention);
					for(String a : md.artist)
						((DefaultListModel<MetaWrapper>)mListArtists.getModel()).addElement(new MetaWrapperArtist(a, md.provider()));
					for(String c : md.circle)
						((DefaultListModel<MetaWrapper>)mListCircles.getModel()).addElement(new MetaWrapperCircle(c, md.provider()));
					for(String c : md.content)
						((DefaultListModel<MetaWrapper>)mListContents.getModel()).addElement(new MetaWrapperContent(c, md.provider()));
					for(String p : md.parody)
						((DefaultListModel<MetaWrapper>)mListParodies.getModel()).addElement(new MetaWrapperParody(p, md.provider()));
				}
				
				private final boolean isNull(String text) {
					return (text == null || text.length() == 0);
				}
				
				private final boolean isNull(Integer number) {
					return (number == null || number < 0);
				}
				
				private final boolean isNull(Long number) {
					return (number == null || number < 0);
				}
				
				private final boolean isNull(Boolean bool) {
					return bool == null;
				}
				
				private abstract class MetaWrapper
				{
					private final String mValue;
					private final String mProvider;
					private boolean mSelected;
					private MetaWrapper(String value, String provider) {
						this.mValue = value;
						this.mProvider = provider;
						this.mSelected = true;
					}
					public final String getValue() {
						return mValue;
					}
					public final String getProvider() {
						return mProvider;
					}
					protected abstract Icon getIcon();
					public boolean isSelected() {
						return mSelected;
					}
					public void setSelected(boolean selected) {
						mSelected = selected;
					}
				}
				
				private final class MetaWrapperArtist extends MetaWrapper
				{
					private MetaWrapperArtist(String value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_artist;
					}
				}
				private final class MetaWrapperCircle extends MetaWrapper
				{
					private MetaWrapperCircle(String value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_circle;
					}
				}
				private final class MetaWrapperContent extends MetaWrapper
				{
					private MetaWrapperContent(String value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_content;
					}
				}
				private final class MetaWrapperParody extends MetaWrapper
				{
					private MetaWrapperParody(String value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_parody;
					}
				}
				
				private final class MetadataListCellRenderer extends DefaultListCellRenderer
				{
					private final JCheckBox mDisplay;
//					private final Color backgroundSelectionColor;
//					private final Color textSelectionColor;
					private MetadataListCellRenderer() {
						mDisplay = new JCheckBox();
						mDisplay.setOpaque(true);
//						backgroundSelectionColor = mDisplay.getBackground();
//						textSelectionColor = mDisplay.getForeground();
					}
					@SuppressWarnings("rawtypes")
					@Override
				    public Component getListCellRendererComponent(
				            JList list,
				            Object value,
				            int index,
				            boolean selected,
				            boolean expanded) {
						MetaWrapper meta = (MetaWrapper) value;
						mDisplay.setIcon(meta.getIcon());
						mDisplay.setText(meta.getValue());
						boolean isSelected = meta.isSelected();
						mDisplay.setSelected(isSelected);
				        if (isSelected) {
				        	mDisplay.setIcon(meta.getIcon());
				        } else {
				        	mDisplay.setIcon(null);
				        }
//				        if (isSelected) {
//				        	mDisplay.setBackground(textSelectionColor);
//				        	mDisplay.setForeground(backgroundSelectionColor);
//				        } else {
//				        	mDisplay.setBackground(backgroundSelectionColor);
//				        	mDisplay.setForeground(textSelectionColor);
//				        }
				        return mDisplay;
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
				public void actionPerformed(ActionEvent ae) {
					/**
					 * Browse Metadata URI in user Desktop
					 */
//					if(ae.getSource().equals(mMetaURI)) {
//						String uri = mMetaURI.getText();
//						try {
//							Desktop.getDesktop().browse(new URI(uri));
//						} catch (URISyntaxException | IOException e) {
//							LOG.error("Error opening URI {}", uri,  e);
//						}
//						return;
//					}
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
					mTabbedPane.setBounds(0, 0, width, height);
//					int yoffset = 0;
//					int xoffset = width / 3;
//					if(mMessage != null) {
//						mMessage.setBounds(0,0,width,20);
//						yoffset = 20;
//					}
//					mMetaThumbnail.setBounds(0,yoffset,xoffset,height-yoffset);
//					mMetaListScroll.setBounds(xoffset*2,yoffset,xoffset,height);
//					mMetaName.setBounds(xoffset,yoffset,xoffset,20);
//					mMetaAlias.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaTranslation.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaPages.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaTimestamp.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaType.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaAdult.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaInfo.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaSize.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaConvention.setBounds(xoffset,yoffset+=20,xoffset,20);
//					mMetaURI.setBounds(xoffset,yoffset+=20,xoffset,20);
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent pce) {
			if(pce.getPropertyName().equals("taskmanager-info")) {
				m_LabelTasks.setText("Tasks : " + mTaskManager.size());
				if(mTaskManager.isRunning())
					m_ButtonTaskManagerCtl.setIcon(mIcons.task_pause);
				else
					m_ButtonTaskManagerCtl.setIcon(mIcons.task_resume);
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
		mTaskManager.load();
		mTaskManager.start();
		/** 
		 * UI should be loaded after TaskManager data (TaskSet) 
		 * so every graphical compontent (JTable.tableModel) touching 
		 * the actual data (TaskManager.taskSet) doesn't need to be 
		 * updated specifically.
		 */
		// FIXME Find a method to notify JTable.tableModel that TaskManager.taskSet was updated
		mUI = new PluginUI();
	}
	
	@Override
	protected void doShutdown() throws TaskException {
		try {
			ConfigurationParser.toXML(Configuration.class, CONFIG_FILE);
			LOG.debug("Saved Configuration to {}", CONFIG_FILE.getName());
		} catch (IOException ioe) {
			LOG.error("Error saving Configuration to {}", CONFIG_FILE.getName(), ioe);
		}
		mTaskManager.stop();
		mTaskManager.save();
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
