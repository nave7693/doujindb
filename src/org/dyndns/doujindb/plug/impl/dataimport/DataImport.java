package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import org.dyndns.doujindb.conf.ConfigurationParser;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.plug.impl.dataimport.Metadata.Artist;
import org.dyndns.doujindb.plug.impl.dataimport.Metadata.Circle;
import org.dyndns.doujindb.plug.impl.dataimport.Metadata.Content;
import org.dyndns.doujindb.plug.impl.dataimport.Metadata.Parody;
import org.dyndns.doujindb.plug.impl.dataimport.Task.Duplicate.Option;
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
	private final class PluginUI extends JPanel implements LayoutManager, ActionListener, TaskListener
	{
		private JTabbedPane m_TabbedPane;
		@SuppressWarnings("unused")
		private JPanel m_TabConfiguration;
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
			mTaskManager.addTaskListener(this);
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
		@SuppressWarnings("incomplete-switch")
		private Icon getDisplayIcon(Task task) {
			if(task.equals(mTaskManager.getRunningTask()))
				return mIcons.task_state_running;
			switch(task.getState()) {
			case ERROR_RAISE:
				return mIcons.task_state_error;
			case TASK_COMPLETE:
				return mIcons.task_state_complete;
			case METADATA_SELECT:
				return mIcons.task_state_userinput;
			case DUPLICATE_SELECT:
				return mIcons.task_state_userinput;
			case SIMILAR_SELECT:
				return mIcons.task_state_userinput;
			case FACTORY_RESET:
				return mIcons.task_state_idle;
			}
			return mIcons.task_state_idle;
		}
		
		private final class PanelTaskUI extends JTable implements TaskListener
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
				
				mTaskManager.addTaskListener(this);
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
			public void taskChanged(Task task) {
				dataChanged();
			}
			
			@Override
			public void taskmanagerChanged() {
				dataChanged();
			}
		}
		
		private final class TaskUI extends JPanel implements LayoutManager, ActionListener, TaskListener
		{
			private Task m_Task;
			private JLabel m_LabelState;
			private JLabel m_LabelId;
			private JLabel m_LabelPreview;
			private JButton m_ButtonOpenFolder;
			private JButton m_ButtonOpenXML;
			private JButton m_ButtonClose;
			private JSplitPane mSplitPane;
			private JComponent mStateUI;
			
			public TaskUI() {
				super();
				setLayout(this);
				setSize(280, 280);
				setMinimumSize(new Dimension(280, 280));
				setMaximumSize(new Dimension(280, 280));
				setPreferredSize(new Dimension(280, 280));
				
				m_LabelState = new JLabel();
				m_LabelState.setText("");
				m_LabelState.setIcon(null);
				add(m_LabelState);
				m_LabelId = new JLabel();
				m_LabelId.setText("");
				m_LabelId.setIcon(null);
				add(m_LabelId);
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
				
				mTaskManager.addTaskListener(this);
			}
			

			public void setTask(Task task) {
				m_Task = task;
				m_LabelState.setText(m_Task.getState().toString());
				m_LabelState.setIcon(getDisplayIcon(task));
				m_LabelId.setText(m_Task.getFile());
				try {
					m_LabelPreview.setIcon(new ImageIcon(javax.imageio.ImageIO.read(new File(task.getThumbnail()))));
				} catch (Exception e) {
					m_LabelPreview.setIcon(null);
				}
				mSplitPane.setLeftComponent(m_LabelPreview);
				switch(task.getState()) {
					case ERROR_RAISE:
						mStateUI = new ErrorUI(task);
						break;
					case TASK_COMPLETE:
						mStateUI = new DoneUI(task);
						break;
					case METADATA_SELECT:
						mStateUI = new MetadataUI(task.metadata());
						break;
					case DUPLICATE_SELECT:
						mStateUI = new DuplicateUI(task.duplicates());
						break;
					case SIMILAR_SELECT:
						mStateUI = new DuplicateUI(task.duplicates());
						break;
					default:
						mStateUI = new JPanel();
				}
				mSplitPane.setRightComponent(mStateUI);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						doLayout();
					}
				});
			}

			@Override
			public void layoutContainer(Container parent) {
				int width = parent.getWidth(),
					height = parent.getHeight();
				m_LabelState.setBounds(0, 0, 140, 20);
				m_LabelId.setBounds(140, 0, width - 140 - 80, 20);
				m_ButtonClose.setBounds(width - 20, 0, 20, 20);
				m_ButtonOpenFolder.setBounds(width - 40, 0, 20, 20);
				m_ButtonOpenXML.setBounds(width - 60, 0, 20, 20);
				mSplitPane.setBounds(0, 20, width, height - 20);
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
			}

			@Override
			public void taskmanagerChanged() {
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
			
			private final class ErrorUI extends JPanel
			{
				private JScrollPane mScrollPane;
				
				public ErrorUI(Task task) {
					super.setLayout(new GridLayout(1,1));
					JTextArea text = new JTextArea();
					for(String message : task.errors().keySet())
						text.append(message + "\n" + task.errors().get(message) + "\n");
					text.setEditable(false);
					text.setFocusable(false);
					text.setMargin(new Insets(5,5,5,5)); 
					mScrollPane = new JScrollPane(text);
					add(mScrollPane);
				}
			}
			
			private final class DoneUI extends JPanel implements ActionListener
			{
				private JTabbedPane mTabbedPane;
				
				public DoneUI(Task task) {
					super.setLayout(new GridLayout(1,1));
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					mTabbedPane = new JTabbedPane();
					mTabbedPane.setFocusable(false);
					super.add(mTabbedPane);
					super.doLayout();
					if(task.getResult() != null)
						addResult(task.getResult());
					for(Task.Duplicate duplicate : task.duplicates()) {
						if(duplicate.dataOption == Option.IGNORE && duplicate.metadataOption == Option.IGNORE)
							continue;
						addResult(duplicate.id);
					}
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
				
				private void addResult(final Integer bookId) {
					ImageIcon duplicateImage;
					try {
						duplicateImage = new ImageIcon(javax.imageio.ImageIO.read(DataStore.getThumbnail(bookId).openInputStream()));
					} catch (Exception e) {
						duplicateImage = mIcons.task_preview_missing;
						LOG.warn("Error loading cover image for Book Id={}", bookId, e);
					}
					JButton duplicateButton = new BookCoverButton(duplicateImage);
					duplicateButton.setActionCommand("" + bookId);
					duplicateButton.addActionListener(this);
					duplicateButton.setFocusable(false);
					duplicateButton.setMinimumSize(new Dimension(180, 180));
					mTabbedPane.addTab("Book (Id=" + bookId + ")", mIcons.task_metadata_book, duplicateButton);
				}
			}
			
			private final class DuplicateUI extends JPanel implements LayoutManager, ActionListener
			{
				private JTabbedPane mTabbedPane;
				private JButton mButtonNext;

				public DuplicateUI(Set<Task.Duplicate> duplicates) {
					super.setLayout(this);
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					mTabbedPane = new JTabbedPane();
					mTabbedPane.setFocusable(false);
					mButtonNext = new JButton();
					mButtonNext.setText("Next");
					mButtonNext.setToolTipText("Next");
					mButtonNext.setIcon(mIcons.task_state_userinput);
					mButtonNext.setSelected(true);
					mButtonNext.setFocusable(false);
					mButtonNext.addActionListener(this);
					add(mButtonNext);
					super.add(mTabbedPane);
					super.doLayout();
					for(Task.Duplicate duplicate : duplicates)
						addDuplicate(duplicate);
				}

				@Override
				public void actionPerformed(ActionEvent ae) {
					if(ae.getSource() == mButtonNext) {
						m_Task.unlock();
						m_SplitPane.setBottomComponent(null);
						m_PanelTasks.clearSelection();
						return;
					}
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
					mTabbedPane.setBounds(0, 0, width, height - 30);
					mButtonNext.setBounds(width / 2 - 40, height - 25, 80, 20);
				}

				private void addDuplicate(final Task.Duplicate duplicate) {
					ImageIcon duplicateImage;
					try {
						duplicateImage = new ImageIcon(javax.imageio.ImageIO.read(DataStore.getThumbnail(duplicate.id).openInputStream()));
					} catch (Exception e) {
						duplicateImage = mIcons.task_preview_missing;
						LOG.warn("Error loading cover image for Book Id={}", duplicate.id, e);
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
					
					mTabbedPane.addTab("Book (Id=" + duplicate.id + ")", mIcons.task_metadata_book, split);
				}
			}

			private final class MetadataUI extends JPanel implements LayoutManager, ActionListener
			{
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
				private JComboBox<Metadata.Convention> mComboboxConvention;
				private JLabel mLabelAdult;
				private JCheckBox mCheckboxAdult;
				private JTabbedPane mTabbedPane;
				private JList<MetaWrapper> mListArtists;
				private JList<MetaWrapper> mListCircles;
				private JList<MetaWrapper> mListContents;
				private JList<MetaWrapper> mListParodies;
				private JButton mButtonNext;
				
				public MetadataUI(Iterable<Metadata> list) {
					super.setLayout(this);
					super.setMinimumSize(new Dimension(100,100));
					super.setPreferredSize(new Dimension(100,100));
					
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
					mComboboxConvention = new JComboBox<Metadata.Convention>();
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
					
					mButtonNext = new JButton();
					mButtonNext.setText("Next");
					mButtonNext.setToolTipText("Next");
					mButtonNext.setIcon(mIcons.task_state_userinput);
					mButtonNext.setSelected(true);
					mButtonNext.setFocusable(false);
					mButtonNext.addActionListener(this);
					add(mButtonNext);
					
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
						mTextDate.addItem(Metadata.toDate(md.timestamp));
					if(!isNull(md.type))
						mComboboxType.addItem(md.type);
					if(!isNull(md.adult))
						mCheckboxAdult.setSelected(md.adult);
					if(!isNull(md.convention))
						mComboboxConvention.addItem(md.convention);
					for(Metadata.Artist a : md.artist)
						((DefaultListModel<MetaWrapper>)mListArtists.getModel()).addElement(new MetaWrapperArtist(a, md.provider()));
					for(Metadata.Circle c : md.circle)
						((DefaultListModel<MetaWrapper>)mListCircles.getModel()).addElement(new MetaWrapperCircle(c, md.provider()));
					for(Metadata.Content c : md.content)
						((DefaultListModel<MetaWrapper>)mListContents.getModel()).addElement(new MetaWrapperContent(c, md.provider()));
					for(Metadata.Parody p : md.parody)
						((DefaultListModel<MetaWrapper>)mListParodies.getModel()).addElement(new MetaWrapperParody(p, md.provider()));
				}
				
				private final boolean isNull(Metadata.Item item) {
					return (item == null || item.getName().length() == 0);
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
					private final Metadata.Item mValue;
					private final String mProvider;
					private boolean mSelected;
					private MetaWrapper(Metadata.Item value, String provider) {
						this.mValue = value;
						this.mProvider = provider;
						if(Configuration.options_autoadd.get())
							this.mSelected = true;
						else
							this.mSelected = (value.getId() != null);
					}
					public final Metadata.Item getValue() {
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
					private MetaWrapperArtist(Metadata.Artist value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_artist;
					}
				}
				private final class MetaWrapperCircle extends MetaWrapper
				{
					private MetaWrapperCircle(Metadata.Circle value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_circle;
					}
				}
				private final class MetaWrapperContent extends MetaWrapper
				{
					private MetaWrapperContent(Metadata.Content value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_content;
					}
				}
				private final class MetaWrapperParody extends MetaWrapper
				{
					private MetaWrapperParody(Metadata.Parody value, String provider) {
						super(value, provider);
					}
					protected final Icon getIcon() {
						return mIcons.task_metadata_parody;
					}
				}
				
				private final class MetadataListCellRenderer extends DefaultListCellRenderer
				{
					private final JCheckBox mDisplay;
					private MetadataListCellRenderer() {
						mDisplay = new JCheckBox();
						mDisplay.setOpaque(true);
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
						mDisplay.setText(meta.getValue().getName() + (meta.getValue().getId() == null ? " *" : ""));
						boolean isSelected = meta.isSelected();
						mDisplay.setSelected(isSelected);
				        if (isSelected) {
				        	mDisplay.setIcon(meta.getIcon());
				        } else {
				        	mDisplay.setIcon(null);
				        }
				        return mDisplay;
				    }
				}
				
				private <T> T ifNull(T couldBeNull, T returnNotNull) {
					return (couldBeNull == null ? returnNotNull : couldBeNull);
				}
				
				@Override
				public void actionPerformed(ActionEvent ae) {
					if(ae.getSource() == mButtonNext) {
						Metadata md = new Metadata.Default();
						md.name = ifNull(mTextJapaneseName.getItemAt(mTextJapaneseName.getSelectedIndex()), "");
						md.translation = ifNull(mTextTranslatedName.getItemAt(mTextTranslatedName.getSelectedIndex()), "");
						md.pages = ifNull(mTextPages.getItemAt(mTextPages.getSelectedIndex()), 0);
						try {
							md.timestamp = Metadata.toTimestamp(mTextDate.getItemAt(mTextDate.getSelectedIndex()));
						} catch (ParseException pe) {
							md.timestamp = new Date().getTime();
						}
						md.type = ifNull(mComboboxType.getItemAt(mComboboxType.getSelectedIndex()), Book.Type.不詳.toString());
						md.adult = mCheckboxAdult.isSelected();
						md.convention = mComboboxConvention.getItemAt(mComboboxConvention.getSelectedIndex());
						md.info = ifNull(mTextInfo.getText(), "");
						{
							Enumeration<MetaWrapper> elems = ((DefaultListModel<MetaWrapper>)mListArtists.getModel()).elements();
							while(elems.hasMoreElements()) {
								MetaWrapper e = elems.nextElement();
								if(e.isSelected())
									md.artist.add((Artist)e.getValue());
							}
						}
						{
							Enumeration<MetaWrapper> elems = ((DefaultListModel<MetaWrapper>)mListCircles.getModel()).elements();
							while(elems.hasMoreElements()) {
								MetaWrapper e = elems.nextElement();
								if(e.isSelected())
									md.circle.add((Circle)e.getValue());
							}
						}
						{
							Enumeration<MetaWrapper> elems = ((DefaultListModel<MetaWrapper>)mListContents.getModel()).elements();
							while(elems.hasMoreElements()) {
								MetaWrapper e = elems.nextElement();
								if(e.isSelected())
									md.content.add((Content)e.getValue());
							}
						}
						{
							Enumeration<MetaWrapper> elems = ((DefaultListModel<MetaWrapper>)mListParodies.getModel()).elements();
							while(elems.hasMoreElements()) {
								MetaWrapper e = elems.nextElement();
								if(e.isSelected())
									md.parody.add((Parody)e.getValue());
							}
						}
						m_Task.setMetadata(md);
						m_Task.unlock();
						m_SplitPane.setBottomComponent(null);
						m_PanelTasks.clearSelection();
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
					mTabbedPane.setBounds(0, 0, width, height - 30);
					mButtonNext.setBounds(width / 2 - 40, height - 25, 80, 20);
				}
			}

			@Override
			public void taskChanged(Task task) { }
		}

		@Override
		public void taskmanagerChanged() {
			m_LabelTasks.setText("Tasks : " + mTaskManager.size());
			if(mTaskManager.isRunning())
				m_ButtonTaskManagerCtl.setIcon(mIcons.task_pause);
			else
				m_ButtonTaskManagerCtl.setIcon(mIcons.task_resume);
		}

		@Override
		public void taskChanged(Task task) { }
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
