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

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.event.*;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.DesktopEx.*;
import org.dyndns.doujindb.ui.dialog.*;

/**  
* UI.java - DoujinDB graphical user interface.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unused")
public final class UI extends JFrame implements LayoutManager, ActionListener, WindowListener, ComponentListener, ConfigurationListener
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	static JComponent ModalLayer;
	
	private JTabbedPane uiPanelTabbed;
	private TrayIcon uiTrayIcon;
	private JPopupMenu uiTrayPopup;
	private JMenuItem uiTrayPopupExit;

	private PanelSettings uiPanelSettings;
	private JButton uiPanelSettingsSave;
	private JButton uiPanelSettingsLoad;
	
	private PanelPlugins uiPanelPlugins;
	private JScrollPane uiPanelPluginsScroll;
	private JButton uiPanelPluginsReload;
	
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
	
	private static final String TAG = "UI : ";
	
	public static final Icons Icon = new Icons();
	public static final Font Font = loadFont();
	private static final Color foreground = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.color");
	private static final Color background = (Color) Configuration.configRead("org.dyndns.doujindb.ui.theme.background");
	
	//Load system Theme first, then other UI components
	public static final Theme Theme = loadTheme();
	public static final DesktopEx Desktop = loadDesktop();
	public static final JFileChooser FileChooser = loadFileChooser();
	
	private static Font loadFont()
	{
		return new java.awt.Font(
			((Font)Configuration.configRead("org.dyndns.doujindb.ui.font")).getFontName(),
			java.awt.Font.PLAIN,
			((Integer)Configuration.configRead("org.dyndns.doujindb.ui.font_size")));
	}
	
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
		} catch(Exception e) {
			Logger.logError(TAG + e.getMessage(), e);
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
		filechooser.setFileView(new javax.swing.filechooser.FileView()
    	{
    		private Hashtable<String,ImageIcon> bundleIcon;
    		
    		{
    			bundleIcon = new Hashtable<String,ImageIcon>();
    			bundleIcon.put("zip",  Icon.fileview_archive);
    			bundleIcon.put("rar",  Icon.fileview_archive);
    			bundleIcon.put("gz",   Icon.fileview_archive);
    			bundleIcon.put("tar",  Icon.fileview_archive);
    			bundleIcon.put("bz2",  Icon.fileview_archive);
    			bundleIcon.put("xz",   Icon.fileview_archive);
    			bundleIcon.put("cpio", Icon.fileview_archive);
    			bundleIcon.put("jpg",  Icon.fileview_image);
    			bundleIcon.put("jpeg", Icon.fileview_image);
    			bundleIcon.put("gif",  Icon.fileview_image);
    			bundleIcon.put("png",  Icon.fileview_image);
    			bundleIcon.put("tiff", Icon.fileview_image);
    			bundleIcon.put("txt",  Icon.fileview_text);
    			bundleIcon.put("sql",  Icon.fileview_text);
    			bundleIcon.put("db",   Icon.fileview_database);
    			bundleIcon.put("csv",  Icon.fileview_database);
    		}
    		
    		@Override
    		public String getName(File file)
    		{
    			return super.getName(file);
    		}

    		@Override
    		public Icon getIcon(File file)
    		{
    			String filename = file.getName();
    			if(file.isDirectory())
    				return (Icon)Icon.fileview_folder;
    			String ext = (filename.lastIndexOf(".") == -1) ? "" : filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
    			if(bundleIcon.containsKey(ext))
    	            return (Icon)bundleIcon.get(ext);
    	        else
    	        	return (Icon)Icon.fileview_default;
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
		super.setAlwaysOnTop((Boolean) Configuration.configRead("org.dyndns.doujindb.ui.always_on_top"));
		
		super.getContentPane().setBackground(background);
		Logger.logInfo(TAG + "basic user interface loaded.");
		
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
		bogus.setLayout(null);
		uiPanelSettings = new PanelSettings();
		bogus.add(uiPanelSettings);
		uiPanelSettingsSave = new JButton(Icon.window_tab_settings_save);
		uiPanelSettingsSave.addActionListener(this);
		uiPanelSettingsSave.setBorder(null);
		uiPanelSettingsSave.setFocusable(false);
		uiPanelSettingsSave.setToolTipText("Save");
		bogus.add(uiPanelSettingsSave);
		uiPanelSettingsLoad = new JButton(Icon.window_tab_settings_load);
		uiPanelSettingsLoad.addActionListener(this);
		uiPanelSettingsLoad.setBorder(null);
		uiPanelSettingsLoad.setFocusable(false);
		uiPanelSettingsLoad.setToolTipText("Load");
		bogus.add(uiPanelSettingsLoad);
		uiPanelTabbed.addTab("Settings", Icon.window_tab_settings, bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelPlugins = new PanelPlugins();
		uiPanelPluginsScroll = new JScrollPane(uiPanelPlugins);
		bogus.add(uiPanelPluginsScroll);
		uiPanelPluginsReload = new JButton(Icon.window_tab_plugins_reload);
		uiPanelPluginsReload.addActionListener(this);
		uiPanelPluginsReload.setBorder(null);
		uiPanelPluginsReload.setFocusable(false);
		uiPanelPluginsReload.setToolTipText("Reload");
		bogus.add(uiPanelPluginsReload);
		
		uiPanelPlugins.clear();
		for(Plugin plugin : PluginManager.listAll())
			uiPanelPlugins.add(plugin);
		
		uiPanelTabbed.addTab("Plugins", Icon.window_tab_plugins, bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelTabbed.addTab("Network", Icon.window_tab_network, bogus);
		uiPanelTabbed.setEnabledAt(uiPanelTabbed.getTabCount()-1, false);
	
		uiPanelTabbed.setFont(Font);
		uiPanelTabbed.setFocusable(false);
		super.add(uiPanelTabbed);
		
		if(SystemTray.isSupported())
		{
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
					if (e.isPopupTrigger())
					{
						uiTrayPopup.setVisible(true);
						uiTrayPopup.setLocation(e.getX(), e.getY() - uiTrayPopup.getHeight());
						uiTrayPopup.setInvoker(uiTrayPopup);
					}
				}
			});
			uiTrayIcon.addActionListener(this);
			Logger.logInfo(TAG + "system tray loaded.");
		} else
			Logger.logWarning(TAG + "system tray not supported.");

		/*
		 * 16/4/2011 - Bug introduced migrating from JDK6 to JDK7
		 * java.lang.IllegalStateException: This function should be called while holding treeLock
		 * super.validateTree();
		 */
		super.setVisible(true);
		
		Configuration.addConfigurationListener(this);
		
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
		uiPanelSettingsLoad.setBounds(1,1,20,20);
		uiPanelSettingsSave.setBounds(21,1,20,20);
		uiPanelSettings.setBounds(0,22,width-5,height-48);
		uiPanelPluginsScroll.setBounds(0,22,width-5,height-48);
		uiPanelPluginsReload.setBounds(1,1,20,20);
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
		if(event.getSource() == uiPanelSettingsSave)
		{
			try
			{
				Configuration.configSave();
				Logger.logInfo(TAG + "system properties saved.");
			}catch(Exception e)
			{
				Logger.logError(TAG + e.getMessage(), e);
			}
			return;
		}
		if(event.getSource() == uiPanelPluginsReload)
		{
			try
			{
				uiPanelPlugins.clear();
				for(Plugin plugin : PluginManager.listAll())
					uiPanelPlugins.add(plugin);
			}catch(Exception e)
			{
				Logger.logError(TAG + e.getMessage(), e);
			}
			return;
		}
		if(event.getSource() == uiPanelSettingsLoad)
		{
			try
			{
				Configuration.configLoad();
				uiPanelSettings.reload();
				Logger.logInfo(TAG + "system properties loaded.");
			}catch(Exception e)
			{
				Logger.logError(TAG + e.getMessage(), e);
			}
			return;
		}
		if(event.getSource() == menuHelpAbout)
		{
			try {
				Desktop.showDialog(new DialogEx(new DialogAbout(),
					Icon.menubar_help_about,
					"About"));
			} catch (PropertyVetoException pve) {
				Logger.logWarning(pve.getMessage(), pve);
			}
			return;
		}
		if(event.getSource() == menuHelpBugtrack)
		{
			try
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				desktop.browse(new URI("https://github.com/loli10K/doujindb/issues/new"));
			} catch (IOException ioe) {
				Logger.logError(TAG + ioe.getMessage(), ioe);
			} catch (URISyntaxException use) {
				Logger.logError(TAG + use.getMessage(), use);
			}
			return;
		}
		if(event.getSource() == uiPanelDesktopShow)
		{
			Desktop.showDesktop();
		}
		if(event.getSource() == uiPanelDesktopSearch)
		{
			try {
				Desktop.showSearchWindow();
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getSource() == uiTrayIcon)
		{
			try
			{
				SystemTray uiTray = SystemTray.getSystemTray();
				uiTray.remove(uiTrayIcon);
				setVisible(true);
				setState(JFrame.NORMAL);
			}catch(Exception e){
				Logger.logError(TAG + e.getMessage(), e);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Artist}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Book}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Circle}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Convention}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Content}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Parody}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, null);
			} catch (DataBaseException dbe) {
				Logger.logError(TAG + dbe.getMessage(), dbe);
			}
			return;
		}
		if(event.getSource() == uiTrayPopupExit)
		{
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
							Logger.logInfo(TAG + "disconnecting from DataBase ...");
							DataBase.disconnect();
						} catch (DataBaseException dbe) {
							Logger.logError(TAG + dbe.getMessage(), dbe);
						}
						try {
							Logger.logInfo(TAG + "closing DataStore ...");
							DataStore.close();
						} catch (DataStoreException dse) {
							Logger.logError(TAG + dse.getMessage(), dse);
						}
					} else {
						try {
							Logger.logInfo(TAG + "connecting to DataBase ...");
							DataBase.connect();
						} catch (DataBaseException dbe) {
							Logger.logError(TAG + dbe.getMessage(), dbe);
							return null;
						}
						try {
							Logger.logInfo(TAG + "opening DataStore ...");
							DataStore.open();
						} catch (DataStoreException dse) {
							Logger.logError(TAG + dse.getMessage(), dse);
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
		if(! ((Boolean)Configuration.configRead("org.dyndns.doujindb.ui.tray_on_exit")))
			System.exit(0);
		setState(JFrame.ICONIFIED);
		try
		{
			SystemTray uiTray = SystemTray.getSystemTray();
			uiTray.add(uiTrayIcon);
			setVisible(false);
		} catch(AWTException awte) {
			Logger.logError(TAG + awte.getMessage(), awte);
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
	
	@SuppressWarnings("serial")
	private static final class PanelSettings extends JSplitPane
	{
		private JTree tree;
		private TreeCellRenderer render;
		private DefaultTreeModel model = new DefaultTreeModel(null);
		
		private static enum Type {
			BOOLEAN,
			NUMBER,
			STRING,
			COLOR,
			FONT,
			FILE,
			UNKNOWN
		}

	public PanelSettings()
	{
		super();
		tree = new JTree();
		tree.setModel(model);
		tree.setFocusable(false);
		tree.setFont(Font);
		tree.setEditable(false);
		tree.setRootVisible(true);
		tree.setScrollsOnExpand(true);
		tree.addTreeSelectionListener(new TreeSelectionListener()
	    {
	    	public void valueChanged(TreeSelectionEvent e)
	    	{
	    		DefaultMutableTreeNode dmtnode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    		// Return if no TreeNode is selected
	            if(dmtnode == null)
	            	return;
	            // Return if it's a 'directory' TreeNode
	            if(!dmtnode.isLeaf())
	            	return;
	            // Calculate configuration key based on TreeNodes path
				Object paths[] = dmtnode.getUserObjectPath();
				String key = "";
				for(Object path : paths)
				    key += path + ".";
				// Remove leading '.' character
				key = key.substring(0, key.length() - 1);
				// Refresh editor component
	            Editor editor = new Editor(key);
	            setRightComponent(editor);
	    	}
	    });
		render = new Renderer();
		tree.setCellRenderer(render);
		super.setResizeWeight(1);
		super.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		super.setLeftComponent(new JScrollPane(tree));
		super.setRightComponent(null);
		super.setContinuousLayout(true);
		//super.setDividerSize(0);
		//super.setEnabled(false);	
		reload();
	}
	
	public void reload()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("org.dyndns.doujindb");
		for(String key : Configuration.keys())
		{
			if(key.startsWith("org.dyndns.doujindb."))
				addNode(root, key.substring("org.dyndns.doujindb.".length()));
			else
				;
		}
		model = new DefaultTreeModel(root);
		tree.setModel(model);
	}
	
	public void reload(String key) //TODO
	{
		;
	}
	
	private void addNode(DefaultMutableTreeNode node, String key)
	{
		if(key.indexOf(".") != -1)
		{
		    loop:
		    {
				String subkey = key.substring(0, key.indexOf("."));
		        key = key.substring(key.indexOf(".") + 1);
		        Enumeration<?> e = node.children();
		        while(e.hasMoreElements())
		        {
			       DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) e.nextElement();
			       if(subkey.equals(subnode.getUserObject()))
			       {
			          addNode(subnode, key);
			          break loop;
			       }
		        }
		        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(subkey);
		        node.add(dmtn);
		        addNode(dmtn, key);
		     }
		} else {
			node.add(new DefaultMutableTreeNode(key));
		}
	}

	private final class Renderer extends DefaultTreeCellRenderer
	{
		public Renderer()
		{
		    setBackgroundSelectionColor(background);
		}
	
		public Component getTreeCellRendererComponent(
			JTree tree,
		    Object value,
		    boolean sel,
		    boolean expanded,
		    boolean leaf,
		    int row,
		    boolean hasFocus)
		{
			super.getTreeCellRendererComponent(
                tree,
		        value,
		        sel,
		        expanded,
		        leaf,
		        row,
		        hasFocus);
			if(!((DefaultMutableTreeNode)value).isLeaf())
				setIcon((ImageIcon)Icon.window_tab_settings_tree_directory);
			else
				setIcon((ImageIcon)Icon.window_tab_settings_tree_value);
		    return this;
		}
	}
	
	private class Editor extends JPanel implements LayoutManager
	{
		private JButton fButtonClose;
		private JLabel fLabelTitle;
		private JLabel fLabelDescription;
		private JComponent panel;
		private JButton fButtonApply;
		private JButton fButtonDiscard;
		
		private String key;
		private Object value;
		private Object valueNew;
		
		public Editor(final String key)
		{
			super();
			this.key = key;
			this.value = Configuration.configRead(this.key);
			setLayout(this);
			fLabelTitle = new JLabel(this.key.substring(this.key.lastIndexOf('.') + 1), Icon.window_tab_settings_tree_value, JLabel.LEFT);
			fLabelTitle.setFont(Font);
			add(fLabelTitle);
			fLabelDescription = new JLabel("<html>" + 
				"<body>" + 
				"<b>Type</b> : " + value.getClass().getCanonicalName() + "<br/>" + 
				"<b>Description</b> : " + Configuration.configInfo(this.key) + 
				"</body>" + 
				"</html>");
			fLabelDescription.setVerticalAlignment(JLabel.TOP);
			fLabelDescription.setFont(Font);
			add(fLabelDescription);
			fButtonClose = new JButton(Icon.window_tab_settings_editor_close);
			fButtonClose.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			fButtonClose.setBorder(null);
			fButtonClose.setFocusable(false);
			add(fButtonClose);
			fButtonApply = new JButton("Apply", Icon.window_tab_settings_editor_apply);
			fButtonApply.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					Configuration.configWrite(key, valueNew);
				}
			});
			fButtonApply.setFont(Font);
			fButtonApply.setFocusable(false);
			add(fButtonApply);
			fButtonDiscard = new JButton("Discard", Icon.window_tab_settings_editor_discard);
			fButtonDiscard.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			fButtonDiscard.setFont(Font);
			fButtonDiscard.setFocusable(false);
			add(fButtonDiscard);
			panel = new JPanel();
			{
				valueNew = null;
				if(value instanceof Boolean)
				{
					boolean bool = ((Boolean)value).booleanValue();
					valueNew = new Boolean(bool);
					final JCheckBox field = new JCheckBox((bool) ? "True" : "False");
					field.setSelected(bool);
					field.setFocusable(false);
					field.setFont(Font);
					field.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							boolean bool = field.isSelected();
							field.setText((bool) ? "True" : "False");
							valueNew = new Boolean(bool);
						}
					});
					panel.add(field);
					panel.setLayout(new LayoutManager()
					{

						@Override
						public void addLayoutComponent(String name, Component comp) { }
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth();
							field.setBounds(35, 5, width - 30, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public Dimension preferredLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public void removeLayoutComponent(Component comp) { }
						
					});
				}
				if(value instanceof Integer)
				{
					int ivalue = ((Integer)value).intValue();
					valueNew = new Integer(ivalue);
					final KBigIntegerField field = new KBigIntegerField();
					field.setInt(ivalue);
					field.setFont(Font);
					field.getDocument().addDocumentListener(new DocumentListener()
					{
						@Override
						public void changedUpdate(DocumentEvent de) {
							int ivalue = field.getInt();
							valueNew = new Integer(ivalue);
						}
						@Override
						public void insertUpdate(DocumentEvent de) {
							int ivalue = field.getInt();
							valueNew = new Integer(ivalue);
						}
						@Override
						public void removeUpdate(DocumentEvent de) {
							int ivalue = field.getInt();
							valueNew = new Integer(ivalue);
						}
					});
					panel.add(field);
					panel.setLayout(new LayoutManager()
					{
						@Override
						public void addLayoutComponent(String name, Component comp) { }
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth();
							field.setBounds(45, 5, width - 90, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public Dimension preferredLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public void removeLayoutComponent(Component comp) { }
						
					});
				}
				if(value instanceof String)
				{
					valueNew = "" + ((String)value);
					final JTextField field = new JTextField((String)value);
					field.setFont(Font);
					field.getDocument().addDocumentListener(new DocumentListener()
					{
						@Override
						public void changedUpdate(DocumentEvent de) {
							valueNew = field.getText();
						}
						@Override
						public void insertUpdate(DocumentEvent de) {
							valueNew = field.getText();
						}
						@Override
						public void removeUpdate(DocumentEvent de) {
							valueNew = field.getText();
						}
					});
					panel.add(field);
					panel.setLayout(new LayoutManager()
					{

						@Override
						public void addLayoutComponent(String name, Component comp) {}
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth();
							field.setBounds(15, 5, width - 30, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public Dimension preferredLayoutSize(Container comp) { return new Dimension(200, 200); }
						@Override
						public void removeLayoutComponent(Component comp) { }
						
					});
				}
				if(value instanceof Color)
				{
					int r = ((Color)value).getRed();
					int g = ((Color)value).getGreen();
					int b = ((Color)value).getBlue();
					int a = ((Color)value).getAlpha();
					valueNew = new Color(r,g,b,a);
					final JLabel label = new JLabel();
					label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					label.setOpaque(true);
					label.setBackground((Color)valueNew);
					panel.add(label);
					final KSmallIntegerField field_r = new KSmallIntegerField();
					final JSlider slide_r = new JSlider(JSlider.HORIZONTAL);
					field_r.setInt(r);
					field_r.setFont(Font);
					field_r.addKeyListener(new KeyListener()
					{
						@Override
						public void keyPressed(KeyEvent ke) {
							slide_r.setValue(Integer.decode(field_r.getText()));
						}
						@Override
						public void keyReleased(KeyEvent ke) {
							slide_r.setValue(Integer.decode(field_r.getText()));
						}
						@Override
						public void keyTyped(KeyEvent ke) {
							slide_r.setValue(Integer.decode(field_r.getText()));
						}
					});
					panel.add(field_r);
					slide_r.setMaximum(255);
					slide_r.setMinimum(0);
					slide_r.setValue(r);
					slide_r.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							int r = slide_r.getValue();
							int g = ((Color)valueNew).getGreen();
							int b = ((Color)valueNew).getBlue();
							int a = ((Color)valueNew).getAlpha();
							valueNew = new Color(r,g,b,a);
							field_r.setInt(slide_r.getValue());
							label.setBackground((Color)valueNew);
						}
					});
					panel.add(slide_r);
					final KSmallIntegerField field_g = new KSmallIntegerField();
					final JSlider slide_g = new JSlider(JSlider.HORIZONTAL);
					field_g.setInt(g);
					field_g.setFont(Font);
					field_g.addKeyListener(new KeyListener()
					{
						@Override
						public void keyPressed(KeyEvent ke) {
							slide_g.setValue(Integer.decode(field_g.getText()));
						}
						@Override
						public void keyReleased(KeyEvent ke) {
							slide_g.setValue(Integer.decode(field_g.getText()));
						}
						@Override
						public void keyTyped(KeyEvent ke) {
							slide_g.setValue(Integer.decode(field_g.getText()));
						}
					});
					panel.add(field_g);
					slide_g.setMaximum(255);
					slide_g.setMinimum(0);
					slide_g.setValue(g);
					slide_g.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							int r = ((Color)valueNew).getRed();
							int g = slide_g.getValue();
							int b = ((Color)valueNew).getBlue();
							int a = ((Color)valueNew).getAlpha();
							valueNew = new Color(r,g,b,a);
							field_g.setInt(slide_g.getValue());
							label.setBackground((Color)valueNew);
						}
					});
					panel.add(slide_g);
					final KSmallIntegerField field_b = new KSmallIntegerField();
					final JSlider slide_b = new JSlider(JSlider.HORIZONTAL);
					field_b.setInt(b);
					field_b.setFont(Font);
					field_b.addKeyListener(new KeyListener()
					{
						@Override
						public void keyPressed(KeyEvent ke) {
							slide_b.setValue(Integer.decode(field_b.getText()));
						}
						@Override
						public void keyReleased(KeyEvent ke) {
							slide_b.setValue(Integer.decode(field_b.getText()));
						}
						@Override
						public void keyTyped(KeyEvent ke) {
							slide_b.setValue(Integer.decode(field_b.getText()));
						}
					});
					panel.add(field_b);
					slide_b.setMaximum(255);
					slide_b.setMinimum(0);
					slide_b.setValue(b);
					slide_b.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							int r = ((Color)valueNew).getRed();
							int g = ((Color)valueNew).getGreen();
							int b = slide_b.getValue();
							int a = ((Color)valueNew).getAlpha();
							valueNew = new Color(r,g,b,a);
							field_b.setInt(slide_b.getValue());
							label.setBackground((Color)valueNew);
						}
					});
					panel.add(slide_b);
					final KSmallIntegerField field_a = new KSmallIntegerField();
					final JSlider slide_a = new JSlider(JSlider.HORIZONTAL);
					field_a.setInt(a);
					field_a.setFont(Font);
					field_a.addKeyListener(new KeyListener()
					{
						@Override
						public void keyPressed(KeyEvent ke) {
							slide_a.setValue(Integer.decode(field_a.getText()));
						}
						@Override
						public void keyReleased(KeyEvent ke) {
							slide_a.setValue(Integer.decode(field_a.getText()));
						}
						@Override
						public void keyTyped(KeyEvent ke) {
							slide_a.setValue(Integer.decode(field_a.getText()));
						}
					});
					panel.add(field_a);
					slide_a.setMaximum(255);
					slide_a.setMinimum(0);
					slide_a.setValue(a);
					slide_a.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							int r = ((Color)valueNew).getRed();
							int g = ((Color)valueNew).getGreen();
							int b = ((Color)valueNew).getBlue();
							int a = slide_a.getValue();
							valueNew = new Color(r,g,b,a);
							field_a.setInt(slide_a.getValue());
							label.setBackground((Color)valueNew);
						}
					});
					panel.add(slide_a);
					panel.setLayout(new LayoutManager()
					{

						@Override
						public void addLayoutComponent(String name, Component comp) {}
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth();
							slide_r.setBounds(5, 5, width - 55, 20);
							field_r.setBounds(width - 50, 5, 45, 20);
							slide_g.setBounds(5, 25, width - 55, 20);
							field_g.setBounds(width - 50, 25, 45, 20);
							slide_b.setBounds(5, 45, width - 55, 20);
							field_b.setBounds(width - 50, 45, 45, 20);
							slide_a.setBounds(5, 65, width - 55, 20);
							field_a.setBounds(width - 50, 65, 45, 20);
							label.setBounds(60, 125, width - 120, 60);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
					});
				}
				if(value instanceof Font)
				{
					String fontName = ((Font)value).getFontName();
					int fontSize = ((Font)value).getSize();
					int fontStyle = ((Font)value).getStyle();
					valueNew = new Font(fontName, fontSize, fontStyle);
					final JList<Font> list = new JList<Font>();
					final JTextField field = new JTextField("Test string / 年");
					field.setFont((Font)value);
					list.setFont(Font);
					DefaultListModel<Font> model = new DefaultListModel<Font>();
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					list.setModel(model);
					GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
					Font envfonts[] = gEnv.getAllFonts();
					for(Font envfont : envfonts)
						if(envfont.canDisplay('年'))
							model.addElement(envfont.deriveFont(12f));
					list.addListSelectionListener(new ListSelectionListener()
					{
						@Override
						public void valueChanged(ListSelectionEvent lse)
						{
							valueNew = (Font)list.getSelectedValue();
							field.setFont((Font)valueNew);
						}
					});
					list.setCellRenderer(new DefaultListCellRenderer()
					{
						@Override
						public Component getListCellRendererComponent(JList<?> list, Object value,
							    int index, boolean isSelected, boolean cellHasFocus) {
							super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
							if(value instanceof Font)
								setText(((Font)value).getFontName());
							return this;
						}
					});
					final JScrollPane scroll = new JScrollPane(list);
					panel.add(scroll);
					panel.add(field);
					panel.setLayout(new LayoutManager()
					{

						@Override
						public void addLayoutComponent(String name, Component comp) {}
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth(),
								height = comp.getHeight();
							scroll.setBounds(5, 5, width - 10, height - 75);
							field.setBounds(5, height - 60, width - 10, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
					});
				}
				if(value instanceof File)
				{
					File file = new File(((File)value).getAbsolutePath());
					valueNew = file;
					final JButton chooser = new JButton(Icon.window_tab_settings_tree_directory);
					final JLabel directory = new JLabel(file.getAbsolutePath());
					directory.setUI(new BasicLabelUI()
					{
						@Override
						protected String layoutCL(JLabel label,
		                          FontMetrics fontMetrics,
		                          String text,
		                          Icon icon,
		                          Rectangle viewR,
		                          Rectangle iconR,
		                          Rectangle textR)
						{
							boolean hasBeenCut = false;
							while(true)
								if(fontMetrics.stringWidth("..." + text) > label.getWidth())
								{
									hasBeenCut = true;
									text = text.substring(1);
								}else
									break;
							if(hasBeenCut)
								return "..." + text;
							else
								return text;
						}
					});
					chooser.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae)
						{
							JFileChooser fc = FileChooser;
							fc.setMultiSelectionEnabled(false);
							int prev_option = fc.getFileSelectionMode();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(Desktop) != JFileChooser.APPROVE_OPTION)
							{
								fc.setFileSelectionMode(prev_option);
								return;
							}
							File file = fc.getSelectedFile();
							fc.setFileSelectionMode(prev_option);
							valueNew = file;
							directory.setText(file.getAbsolutePath());
						}
					});
					panel.add(chooser);
					panel.add(directory);
					panel.setLayout(new LayoutManager()
					{

						@Override
						public void addLayoutComponent(String name, Component comp) {}
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth(),
								height = comp.getHeight();
							directory.setBounds(5, 5, width - 10 - 30, 20);
							chooser.setBounds(width - 10 - 10, 5, 20 ,20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
					});
				}
			}
			add(panel);
		}
		
		@Override
		public void addLayoutComponent(String name, Component comp) { }
		@Override
		public void layoutContainer(Container comp)
		{
			int width = comp.getWidth(),
				height = comp.getHeight();
			fLabelTitle.setBounds(1, 1, width - 21, 20);
			fButtonClose.setBounds(width - 21, 1, 20, 20);
			fLabelDescription.setBounds(1, 21, width - 2, 75);
			panel.setBounds(1, 100, width - 2, height - 100 - 65);
			fButtonApply.setBounds((width - 125) / 2, height - 60, 125, 20);
			fButtonDiscard.setBounds((width - 125) / 2, height - 40, 125, 20);
		}
		@Override
		public Dimension minimumLayoutSize(Container comp) {
			return new Dimension(200, 200);
		}
		@Override
		public Dimension preferredLayoutSize(Container comp) {
			return new Dimension(250, 250);
		}
		@Override
		public void removeLayoutComponent(Component comp) { }
		
		private final class KBigIntegerField extends JTextField
		{
			public KBigIntegerField()
			{
				super();
				super.setHorizontalAlignment(JTextField.CENTER);
			}
			public KBigIntegerField(int cols)
			{
				super(cols);
			}
			public int getInt()
			{
			    final String text = getText();
			    if (text == null || text.length() == 0)
			    {
			      return 0;
			    }
			    return Integer.parseInt(text);
		    }
			public void setInt(int value)
			{
				setText(String.valueOf(value));
		    }
			protected Document createDefaultModel()
			{
				return new IntegerDocument();
		    }

			private final class IntegerDocument extends PlainDocument
			{
				private int max = 0xffff;
				private int min = 0;
			public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException
			{
				if (str != null)
				{
					try
					{
						Integer.decode(str);
						int value = Integer.decode(super.getText(0, super.getLength()) +  str);
						if(value < min)
							return;
						if(value > max)
							return;
						super.insertString(offs, str, a);
					}catch (NumberFormatException ex)
					{
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		}
	}
		private final class KSmallIntegerField extends JTextField
		{
			public KSmallIntegerField()
			{
				super();
				super.setHorizontalAlignment(JTextField.CENTER);
			}
			public KSmallIntegerField(int cols)
			{
				super(cols);
			}
			public int getInt()
			{
			    final String text = getText();
			    if (text == null || text.length() == 0)
			    {
			      return 0;
			    }
			    return Integer.parseInt(text);
		    }
			public void setInt(int value)
			{
				setText(String.valueOf(value));
		    }
			protected Document createDefaultModel()
			{
				return new IntegerDocument();
		    }

			private final class IntegerDocument extends PlainDocument
			{
				private int max = 0xff;
				private int min = 0;
			public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException
			{
				if (str != null)
				{
					try
					{
						Integer.decode(str);
						int value = Integer.decode(super.getText(0, super.getLength()) +  str);
						if(value < min)
							return;
						if(value > max)
							return;
						super.insertString(offs, str, a);
					}catch (NumberFormatException ex)
					{
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		}
	}
	}
	
	}

	@Override
	public void configAdded(String key)
	{
		//TODO dynamically add new config keys to UI tree
	}

	@Override
	public void configDeleted(String key)
	{
		//TODO dynamically remove config keys to UI tree
	}

	@Override
	public void configUpdated(final String key)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				uiPanelSettings.reload(key);
			}
		});
	}
}
