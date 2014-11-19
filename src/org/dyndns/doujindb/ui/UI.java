package org.dyndns.doujindb.ui;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.event.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.plug.event.PluginListener;
import org.dyndns.doujindb.ui.DesktopEx.*;
import org.dyndns.doujindb.ui.dialog.*;

/**  
* UI.java - DoujinDB graphical user interface.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unused")
public final class UI extends JFrame implements LayoutManager, ActionListener, WindowListener, ComponentListener, PluginListener
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	static JComponent ModalLayer;
	
	private JTabbedPane uiPanelTabbed;
	private TrayIcon uiTrayIcon;
	private JPopupMenu uiTrayPopup;
	private JMenuItem uiTrayPopupExit;

	private PanelConfiguration uiPanelConfiguration;
	
	private PanelPlugins uiPanelPlugins;
	private JScrollPane uiPanelPluginsScroll;
	private JButton uiPanelPluginsUpdate;
	
	private JButton uiPanelDesktopShow;
	private JButton uiPanelDesktopSearch;
	private JButton uiPanelDesktopAdd;
	private JPopupMenu uiPanelDesktopAddPopup;
	private JLabel m_LabelConnectionStatus;
	private JButton m_ButtonConnectionCtl;

	private JMenuBar menuBar;
	private JMenu menuLogs;
	private JMenu menuHelp;
	private JMenuItem menuHelpAbout;
	private JMenuItem menuHelpBugtrack;
	private JMenuItem menuHelpUpdate;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(UI.class);
	
	public static final Icons Icon = new Icons();
	public static final Font Font = Configuration.ui_font.get();
	private static final Color foreground = Configuration.ui_theme_foreground.get();
	private static final Color background = Configuration.ui_theme_background.get();
	
	//Load system Theme first, then other UI components
	public static final Theme Theme = loadTheme();
	public static final DesktopEx Desktop = loadDesktop();
	public static final JFileChooser FileChooser = loadFileChooser();
	
	private static Theme loadTheme()
	{
		try
		{
			Theme theme = new Theme(
				foreground,
				background,
				Font);
		    MetalLookAndFeel.setCurrentTheme(theme);
		    UIManager.setLookAndFeel(new MetalLookAndFeel());
			UIManager.put("ComboBox.selectionBackground", background);
			UIManager.put("InternalFrame.inactiveTitleForeground", foreground.darker());
			UIManager.put("InternalFrame.activeTitleForeground", foreground);
			UIManager.put("InternalFrame.inactiveTitleBackground", background);
			UIManager.put("InternalFrame.activeTitleBackground", background.brighter());
			UIManager.put("InternalFrame.font", Font);
			UIManager.put("InternalFrame.titleFont", Font);
			UIManager.put("InternalFrame.iconifyIcon", Icon.jdesktop_iframe_iconify);
			UIManager.put("InternalFrame.minimizeIcon", Icon.jdesktop_iframe_minimize);
			UIManager.put("InternalFrame.maximizeIcon", Icon.jdesktop_iframe_maximize);
			UIManager.put("InternalFrame.closeIcon", Icon.jdesktop_iframe_close);
			UIManager.put("InternalFrame.border", javax.swing.BorderFactory.createEtchedBorder(0));
			UIManager.put("Slider.horizontalThumbIcon", Icon.jslider_thumbicon);
			UIManager.put("Desktop.background", background);
			UIManager.put("FileChooser.listFont", Font);
			UIManager.put("FileChooser.detailsViewIcon", Icon.filechooser_detailsview);
			UIManager.put("FileChooser.homeFolderIcon", Icon.filechooser_homefolder);
			UIManager.put("FileChooser.listViewIcon", Icon.filechooser_listview);
			UIManager.put("FileChooser.newFolderIcon", Icon.filechooser_newfolder);
			UIManager.put("FileChooser.upFolderIcon", Icon.filechooser_upfolder);
			UIManager.put("FileChooser.computerIcon", Icon.filechooser_computer);
			UIManager.put("FileChooser.directoryIcon", Icon.filechooser_directory);
			UIManager.put("FileChooser.fileIcon", Icon.filechooser_file);
			UIManager.put("FileChooser.floppyDriveIcon", Icon.filechooser_floppydrive);
			UIManager.put("FileChooser.hardDriveIcon", Icon.filechooser_harddrive);
			UIManager.put("Tree.expandedIcon", Icon.jtree_node_collapse);
			UIManager.put("Tree.collapsedIcon", Icon.jtree_node_expand);
			UIManager.put("ToolTip.foreground", foreground);
			UIManager.put("ToolTip.background", background);
		    return theme;
		} catch (UnsupportedLookAndFeelException ulafe) {
			LOG.error("Error setting Look&Fell", ulafe);
		}
		return null;
	}
	
	private static DesktopEx loadDesktop()
	{
		return new DesktopEx();
	}
	
	private static JFileChooser loadFileChooser()
	{
		JFileChooser filechooser = new JFileChooser(Core.DOUJINDB_HOME);
		filechooser.setFont(Font);
		filechooser.setFileView(new javax.swing.filechooser.FileView() {
    		private Hashtable<String,ImageIcon> bundleIcon;
    		{
    			bundleIcon = new Hashtable<String,ImageIcon>();
    			for(String ext : "zip:rar:gz:tar:bz2:xz:cpio:7z".split(":"))
    				bundleIcon.put(ext,  Icon.fileview_archive);
    			for(String ext : "jpg:jpeg:png:gif:tiff".split(":"))
    				bundleIcon.put(ext,  Icon.fileview_image);
    			for(String ext : "txt:sql:log".split(":"))
    				bundleIcon.put(ext,  Icon.fileview_text);
    			for(String ext : "db:csv".split(":"))
    				bundleIcon.put(ext,  Icon.fileview_database);
    			for(String ext : "exe:dll:com:bat:sh:bin".split(":"))
    				bundleIcon.put(ext,  Icon.fileview_executable);
    		}
    		@Override
    		public String getName(File file) {
    			return super.getName(file);
    		}
    		@Override
    		public Icon getIcon(File file) {
    			String filename = file.getName();
    			if(file.isDirectory())
    				return (Icon) Icon.fileview_folder;
    			String ext = (filename.lastIndexOf(".") == -1) ? "" : filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
    			if(bundleIcon.containsKey(ext))
    	            return (Icon) bundleIcon.get(ext);
    	        else
    	        	return (Icon) Icon.fileview_default;
    	        	// return javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(file);
    		}
    	});
		filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    	filechooser.setMultiSelectionEnabled(true);
		return filechooser;
	}
	
	@SuppressWarnings("serial")
	public UI()
	{
		super();
		super.setLayout(this);
		super.setTitle("DoujinDB v" + getClass().getPackage().getSpecificationVersion());
		super.setBounds(0,0,550,550);
		super.setMinimumSize(new Dimension(400,350));
		super.setIconImage(Icon.window_icon.getImage());
		super.setAlwaysOnTop(Configuration.ui_alwaysontop.get());
		super.getContentPane().setBackground(background);
		
		LOG.debug("Basic user interface loaded");
		
		ModalLayer = new JComponent()
	    {
			@Override
	    	protected void paintComponent(Graphics g)
	    	{
		        g.setColor(getBackground());
		        g.fillRect(0,0,getWidth(),getHeight());
	    	}
			@Override
	    	public void setBackground(Color background)
	    	{
	    		super.setBackground( background );
	    	}
	    };
	    ModalLayer.addMouseListener(new MouseAdapter(){});
	    ModalLayer.addMouseMotionListener(new MouseMotionAdapter(){});
		ModalLayer.setOpaque(true);
		ModalLayer.setVisible(false);
		ModalLayer.setEnabled(false);
		ModalLayer.setBackground(new Color(0x22, 0x22, 0x22, 0xae));
		ModalLayer.setLayout(new GridBagLayout());
		super.addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent ce) {
				ModalLayer.setBounds(1, 1, getRootPane().getWidth() - 2, getRootPane().getHeight() - 2);
				ModalLayer.doLayout();
			}
		});
	    super.getLayeredPane().add(ModalLayer, JLayeredPane.PALETTE_LAYER);
		
		uiPanelTabbed = new JTabbedPane();
		super.addWindowListener(this);
		super.addComponentListener(this);
		
		menuBar = new JMenuBar();
		menuBar.setFont(Font);
		menuHelp = new JMenu("Help");
		menuHelp.setIcon(Icon.menubar_help);
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuHelp.setFont(Font);
		menuHelpAbout = new JMenuItem("About",Icon.menubar_help_about);
		menuHelpAbout.setMnemonic(KeyEvent.VK_A);
		menuHelpAbout.setFont(Font);
		menuHelpAbout.addActionListener(this);
		menuHelp.add(menuHelpAbout);
		menuHelpBugtrack = new JMenuItem("Report Bug",Icon.menubar_help_bugtrack);
		menuHelpBugtrack.setMnemonic(KeyEvent.VK_R);
		menuHelpBugtrack.setFont(Font);
		menuHelpBugtrack.addActionListener(this);
		menuHelp.add(menuHelpBugtrack);
		menuBar.add(menuHelp);
		super.setJMenuBar(menuBar);
		
		JPanel bogus;
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelDesktopShow = new JButton(Icon.window_tab_explorer_desktop);
		uiPanelDesktopShow.addActionListener(this);
		uiPanelDesktopShow.setBorder(null);
		uiPanelDesktopShow.setFocusable(false);
		uiPanelDesktopShow.setEnabled(false);
		uiPanelDesktopShow.setToolTipText("Show Desktop");
		bogus.add(uiPanelDesktopShow);
		uiPanelDesktopSearch = new JButton(Icon.window_tab_explorer_search);
		uiPanelDesktopSearch.addActionListener(this);
		uiPanelDesktopSearch.setBorder(null);
		uiPanelDesktopSearch.setFocusable(false);
		uiPanelDesktopSearch.setEnabled(false);
		uiPanelDesktopSearch.setToolTipText("Search");
		bogus.add(uiPanelDesktopSearch);
		uiPanelDesktopAdd = new JButton(Icon.window_tab_explorer_add)
		{
			@Override
			public void processMouseEvent(MouseEvent e) {
				if (e.getClickCount() > 0) { 
					uiPanelDesktopAddPopup.show(e.getComponent(), e.getX(), e.getY());
				}
				super.processMouseEvent(e);
			}
		};
		uiPanelDesktopAdd.setBorder(null);
		uiPanelDesktopAdd.setFocusable(false);
		uiPanelDesktopAdd.setEnabled(false);
		uiPanelDesktopAdd.setToolTipText("Add Item");
		uiPanelDesktopAddPopup = new JPopupMenu();
		JMenuItem itm0 = new JMenuItem("Artist",Icon.desktop_explorer_artist);
		itm0.setActionCommand("Add:{Artist}");
		itm0.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm0);
		JMenuItem itm1 = new JMenuItem("Book",Icon.desktop_explorer_book);
		itm1.setActionCommand("Add:{Book}");
		itm1.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm1);
		JMenuItem itm2 = new JMenuItem("Circle",Icon.desktop_explorer_circle);
		itm2.setActionCommand("Add:{Circle}");
		itm2.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm2);
		JMenuItem itm3 = new JMenuItem("Parody",Icon.desktop_explorer_parody);
		itm3.setActionCommand("Add:{Parody}");
		itm3.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm3);
		JMenuItem itm4 = new JMenuItem("Convention",Icon.desktop_explorer_convention);
		itm4.setActionCommand("Add:{Convention}");
		itm4.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm4);
		JMenuItem itm5 = new JMenuItem("Content",Icon.desktop_explorer_content);
		itm5.setActionCommand("Add:{Content}");
		itm5.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm5);
		bogus.add(uiPanelDesktopAdd);
		m_LabelConnectionStatus = new JLabel(Icon.window_tab_explorer_statusbar_disconnected);
		m_LabelConnectionStatus.setText("Disconnected.");
		m_LabelConnectionStatus.setHorizontalAlignment(JLabel.LEFT);
		m_LabelConnectionStatus.setBorder(null);
		m_LabelConnectionStatus.setFocusable(false);
		bogus.add(m_LabelConnectionStatus);
		m_ButtonConnectionCtl = new JButton(Icon.window_tab_explorer_statusbar_connect);
		m_ButtonConnectionCtl.addActionListener(this);
		m_ButtonConnectionCtl.setBorder(null);
		m_ButtonConnectionCtl.setFocusable(false);
		m_ButtonConnectionCtl.setToolTipText("Connect");
		m_ButtonConnectionCtl.setDisabledIcon(Icon.window_loading);
		bogus.add(m_ButtonConnectionCtl);
		bogus.add(Desktop);
		uiPanelTabbed.addTab("Explorer", Icon.window_tab_explorer, bogus);
		
		bogus = new JPanel();
		uiPanelConfiguration = new PanelConfiguration(Configuration.class);
		uiPanelConfiguration.setConfigurationFile(Configuration.CONFIG_FILE);
		uiPanelTabbed.addTab("Configuration", Icon.window_tab_settings, uiPanelConfiguration);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelPlugins = new PanelPlugins();
		uiPanelPluginsScroll = new JScrollPane(uiPanelPlugins);
		bogus.add(uiPanelPluginsScroll);
		uiPanelPluginsUpdate = new JButton(Icon.window_tab_plugins_update);
		uiPanelPluginsUpdate.addActionListener(this);
		uiPanelPluginsUpdate.setBorder(null);
		uiPanelPluginsUpdate.setFocusable(false);
		uiPanelPluginsUpdate.setToolTipText("Update");
		bogus.add(uiPanelPluginsUpdate);
		
		uiPanelTabbed.addTab("Plugins", Icon.window_tab_plugins, bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelTabbed.addTab("Network", Icon.window_tab_network, bogus);
		uiPanelTabbed.setEnabledAt(uiPanelTabbed.getTabCount()-1, false);
	
		uiPanelTabbed.setFont(Font);
		uiPanelTabbed.setFocusable(false);
		super.add(uiPanelTabbed);
		
		if(SystemTray.isSupported()) {
			uiTrayPopup = new JPopupMenu();
			uiTrayPopup.setFont(Font);
			uiTrayPopupExit = new JMenuItem("Exit",Icon.window_tray_exit);
			uiTrayPopupExit.setActionCommand("Exit");
			uiTrayPopupExit.addActionListener(this);
			uiTrayPopup.add(uiTrayPopupExit);
			uiTrayIcon = new TrayIcon(Icon.window_tray.getImage(),this.getTitle(),null);
			uiTrayIcon.addMouseListener(new MouseAdapter()
			{
				public void mouseReleased(MouseEvent e)
				{
					if (e.isPopupTrigger()) {
						uiTrayPopup.setVisible(true);
						uiTrayPopup.setLocation(e.getX(), e.getY() - uiTrayPopup.getHeight());
						uiTrayPopup.setInvoker(uiTrayPopup);
					}
				}
			});
			uiTrayIcon.addActionListener(this);
			LOG.debug("System tray loaded.");
		} else
			LOG.warn("System tray not supported.");

		/*
		 * 16/4/2011 - Bug introduced migrating from JDK6 to JDK7
		 * java.lang.IllegalStateException: This function should be called while holding treeLock
		 * super.validateTree();
		 */
		super.setVisible(true);
		
		PluginManager.addPluginListener(this);
		
		DataBase.addDataBaseListener(new DataBaseAdapter()
		{
			@Override
			public void databaseConnected() {
				uiPanelDesktopShow.setEnabled(true);
				uiPanelDesktopSearch.setEnabled(true);
				uiPanelDesktopAdd.setEnabled(true);
			}
			@Override
			public void databaseDisconnected() {
				uiPanelDesktopShow.setEnabled(false);
				uiPanelDesktopSearch.setEnabled(false);
				uiPanelDesktopAdd.setEnabled(false);
			}
		});
		
		if(Configuration.sys_check_updates.get())
		{
			new SwingWorker<Void,Void>()
			{
				private boolean updateAvailable = false;
				@Override
				protected Void doInBackground() throws Exception {
					URL updateURL = new URL("https://github.com/loli10K/doujindb/releases/latest");
					HttpURLConnection httpConn = (HttpURLConnection) updateURL.openConnection();
					httpConn.setInstanceFollowRedirects(false);
					httpConn.connect();
					String location = httpConn.getHeaderField("Location");
					if(!location.endsWith(Core.class.getPackage().getSpecificationVersion()))
						updateAvailable = true;
					return null;
				}
				@Override
				protected void done() {
					if(!updateAvailable)
						return;
					menuHelpUpdate = new JMenuItem("Update",Icon.menubar_help_update);
					menuHelpUpdate.setMnemonic(KeyEvent.VK_U);
					menuHelpUpdate.setFont(Font);
					menuHelpUpdate.addActionListener(UI.this);
					menuHelp.addSeparator();
					menuHelp.add(menuHelpUpdate);
				}
			}.execute();
		}
	}

	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		m_ButtonConnectionCtl.setBounds(width - 22,Desktop.getParent().getHeight()-20,20,20);
		m_LabelConnectionStatus.setBounds(1,Desktop.getParent().getHeight()-20,width-25,20);
		if(uiPanelDesktopShow.isEnabled())
			uiPanelDesktopShow.setBounds(1,1,20,20);
		else
			uiPanelDesktopShow.setBounds(-1,-1,0,0);
		
		if(uiPanelDesktopSearch.isEnabled())
			uiPanelDesktopSearch.setBounds(21,1,20,20);
		else
			uiPanelDesktopSearch.setBounds(-1,-1,0,0);
		
		if(uiPanelDesktopAdd.isEnabled())
			uiPanelDesktopAdd.setBounds(41,1,20,20);
		else
			uiPanelDesktopAdd.setBounds(-1,-1,0,0);
		Desktop.setBounds(1,22,width-5,Desktop.getParent().getHeight()-42);
		uiPanelPluginsScroll.setBounds(0,22,width-5,height-48);
		uiPanelPluginsUpdate.setBounds(1,1,20,20);
		uiPanelTabbed.setBounds(0,0,width,height);
	}
	
	@Override
	public void addLayoutComponent(String key, Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return new Dimension(400,350);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return new Dimension(400,350);
	}
	
	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(event.getSource() == uiPanelPluginsUpdate) {
			//TODO
		}
		if(event.getSource() == menuHelpAbout) {
			try {
				Desktop.showDialog(new DialogAbout());
			} catch (PropertyVetoException pve) {
				LOG.error("Error displaying About Dialog", pve);
			}
			return;
		}
		if(event.getSource() == menuHelpBugtrack) {
			try {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				desktop.browse(new URI("https://github.com/loli10K/doujindb/issues/new"));
			} catch (IOException | URISyntaxException e) {
				LOG.error("Error opening issue page in browser", e);
			}
			return;
		}
		if(event.getSource() == menuHelpUpdate) {
			try
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				desktop.browse(new URI("https://github.com/loli10K/doujindb/releases/latest"));
			} catch (IOException | URISyntaxException e) {
				LOG.error("Error opening download page in browser", e);
			}
			return;
		}
		if(event.getSource() == uiPanelDesktopShow) {
			Desktop.showDesktop();
			return;
		}
		if(event.getSource() == uiPanelDesktopSearch) {
			try {
				Desktop.showSearchWindow();
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Search Window", dbe);
			}
			return;
		}
		if(event.getSource() == uiTrayIcon) {
			SystemTray uiTray = SystemTray.getSystemTray();
			setVisible(true);
			setState(JFrame.NORMAL);
			uiTray.remove(uiTrayIcon);
			return;
		}
		if(event.getActionCommand().equals("Add:{Artist}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Artist Window", dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Book}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Book Window", dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Circle}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Circle Window", dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Convention}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Convention Window", dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Content}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Content Window", dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Parody}")) {
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, null);
			} catch (DataBaseException dbe) {
				LOG.error("Error displaying Parody Window", dbe);
			}
			return;
		}
		if(event.getSource() == uiTrayPopupExit) {
			System.exit(0);
			return;
		}
		if(event.getSource() == m_ButtonConnectionCtl)
		{
			if(!m_ButtonConnectionCtl.isEnabled())
				return;
			m_ButtonConnectionCtl.setEnabled(false);
			new SwingWorker<Void,Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					if(DataBase.isConnected()) {
						try {
							DataBase.disconnect();
						} catch (DataBaseException dbe) {
							LOG.error("Error disconnecting from DataBase", dbe);
						}
						try {
							DataStore.close();
						} catch (DataStoreException dse) {
							LOG.error("Error closing DataStore", dse);
						}
					} else {
						try {
							DataBase.connect();
						} catch (DataBaseException dbe) {
							LOG.error("Error connecting to DataBase", dbe);
							return null;
						}
						try {
							DataStore.open();
						} catch (DataStoreException dse) {
							LOG.error("Error opening DataStore", dse);
							return null;
						}
					}
					return null;
				}
				@Override
				protected void done()
				{
					if(DataBase.isConnected()) {
						m_ButtonConnectionCtl.setIcon(Icon.window_tab_explorer_statusbar_disconnect);
						m_LabelConnectionStatus.setText("Connected to " + DataBase.getConnection() + ".");
					} else {
						m_ButtonConnectionCtl.setIcon(Icon.window_tab_explorer_statusbar_connect);
						m_LabelConnectionStatus.setText("Disconnected.");
					}
					m_ButtonConnectionCtl.setEnabled(true);
					Desktop.revalidate();
				}
			}.execute();
			return;
		}
	}
	
	public void windowDeactivated(WindowEvent event) {}
	
	public void windowActivated(WindowEvent event) {}
	
	public void windowDeiconified(WindowEvent event) {}
	
	public void windowIconified(WindowEvent event) {}
	
	public void windowClosed(WindowEvent event) {}
	
	public void windowClosing(WindowEvent event)
	{
		if(!Configuration.ui_trayonexit.get())
			System.exit(0);
		setState(JFrame.ICONIFIED);
		try {
			SystemTray uiTray = SystemTray.getSystemTray();
			uiTray.add(uiTrayIcon);
			setVisible(false);
		} catch(AWTException awte) {
			LOG.error("Error minimizing User Interface to Tray icon", awte);
		}
	}
	
	public void windowOpened(WindowEvent event) {}
	
	public void componentHidden(ComponentEvent event) {}
	
	public void componentShown(ComponentEvent event) {}
	
	public void componentMoved(ComponentEvent event) {}
	
    public void componentResized(ComponentEvent event)
    {
         layoutContainer(getContentPane());
    }

    @SuppressWarnings("serial")
	private final class PanelPlugins extends JTable
	{
		private TableCellRenderer TableRender;
		private TableCellEditor TableEditor;
		private DefaultTableModel TableModel;
		private TableRowSorter<DefaultTableModel> TableSorter;

		public PanelPlugins()
		{
			super();
			TableModel = new DefaultTableModel();
			TableModel.addColumn("");
			TableModel.addColumn("Name");
			TableModel.addColumn("Version");
			super.setModel(TableModel);
			TableRender = new Renderer();
			TableEditor = new Editor();
			TableSorter = new TableRowSorter<DefaultTableModel>(TableModel);
			super.setRowSorter(TableSorter);
			TableSorter.setRowFilter(RowFilter.regexFilter("", 0));
			super.setFont(Font);
			super.setColumnSelectionAllowed(false);
			super.setRowSelectionAllowed(false);
			super.setCellSelectionEnabled(false);
			super.getTableHeader().setFont(Font);
			super.getTableHeader().setReorderingAllowed(false);
			super.getTableHeader().setDefaultRenderer(TableRender);
			for(int k = 0;k<super.getColumnModel().getColumnCount();k++)
			{
				super.getColumnModel().getColumn(k).setCellRenderer(TableRender);
				super.getColumnModel().getColumn(k).setCellEditor(TableEditor);
			}
			super.getColumnModel().getColumn(0).setResizable(false);
			super.getColumnModel().getColumn(0).setMaxWidth(20);
			super.getColumnModel().getColumn(0).setMinWidth(20);
			super.getColumnModel().getColumn(0).setWidth(20);
			super.getColumnModel().getColumn(1).setMinWidth(150);
			super.getColumnModel().getColumn(2).setResizable(false);
			super.getColumnModel().getColumn(2).setMinWidth(50);
			super.getColumnModel().getColumn(2).setMaxWidth(50);
			super.getColumnModel().getColumn(2).setWidth(50);
			super.getColumnModel().getColumn(2).setPreferredWidth(50);
			super.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}
		
		public void add(Plugin plugin) {
			TableModel.addRow(new Object[]{"{Plugin}",
				plugin.getName(),
				plugin.getVersion()});
		}

		public void clear()
		{
			while(TableModel.getRowCount()>0)
				TableModel.removeRow(0);
		}
		
		private final class Renderer extends DefaultTableCellRenderer
		{
			public Renderer()
			{
			    super();
			    super.setFont(Font);
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
				try
				{
					super.setIcon(null);
					super.setBorder(null);
			        if(value.equals("{Plugin}"))
			        {
				        super.setText("");
				        super.setIcon(Icon.window_tab_plugins);
				        return this;
				    }
				    super.setText(value.toString());
			        super.setIcon(null);
			        super.setForeground(foreground);
			        
			        return this;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					// OH WELL
				}
		        return this;
			}
		}
	
		private final class Editor extends AbstractCellEditor implements TableCellEditor
		{
			public Editor()
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

	@Override
	public void pluginInstalled(Plugin plugin) {
		uiPanelPlugins.add(plugin);
	}

	@Override
	public void pluginUninstalled(Plugin plugin) {
		//TODO uiPanelPlugins.remove(plugin);
	}

	@Override
	public void pluginStarted(Plugin plugin) { }

	@Override
	public void pluginStopped(Plugin plugin) { }

	@Override
	public void pluginUpdated(Plugin plugin) { }
}
