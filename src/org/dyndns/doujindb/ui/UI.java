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
import org.dyndns.doujindb.conf.Properties;
import org.dyndns.doujindb.conf.event.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.plug.PluginManager;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.DesktopEx.*;
import org.dyndns.doujindb.ui.rc.*;

/**  
* UI.java - DoujinDB graphical user interface.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unused")
public final class UI extends JFrame implements LayoutManager, ActionListener, WindowListener, ComponentListener, ConfigurationListener
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	private JTabbedPane uiPanelTabbed;
	private TrayIcon uiTrayIcon;
	private JPopupMenu uiTrayPopup;
	private JMenuItem uiTrayPopupExit;

	private JFileChooser uiFileChooser = new JFileChooser();
	
	private PanelLogs uiPanelLogs;
	private JScrollPane uiPanelLogsScroll;
	private JButton uiPanelLogsClear;
	
	private PanelSettings uiPanelSettings;
	private JButton uiPanelSettingsSave;
	private JButton uiPanelSettingsLoad;
	
	private PanelPlugins uiPanelPlugins;
	private JScrollPane uiPanelPluginsScroll;
	private JButton uiPanelPluginsReload;
	
	public DesktopEx Desktop;
	private JButton uiPanelDesktopShow;
	private JButton uiPanelDesktopSearch;
	private JButton uiPanelDesktopAdd;
	private JPopupMenu uiPanelDesktopAddPopup;
	private JLabel m_LabelConnectionStatus;
	private JButton m_ButtonConnectionCtl;

	private JMenuBar menuBar;
	private JMenu menuLogs;
	private JCheckBoxMenuItem menuLogsMessage;
	private JCheckBoxMenuItem menuLogsWarning;
	private JCheckBoxMenuItem menuLogsError;
	private JMenu menuHelp;
	private JMenuItem menuHelpAbout;
	private JMenuItem menuHelpBugtrack;

	public JFileChooser getFileChooser()
	{
		return uiFileChooser;
	}
	
	public void showConfigurationWizard()
	{
		ConfigurationWizard firstRun = new ConfigurationWizard();
		firstRun.setSize(300,300);
		try {
			Core.UI.Desktop.showDialog(
					UI.this,
					firstRun,
					Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Icon"),
					"Configuration Wizard");
		} catch (PropertyVetoException pve) { } 
	}
	
	@SuppressWarnings("serial")
	public UI()
	{
		super();
		super.setTitle("DoujinDB v" + getClass().getPackage().getSpecificationVersion());
		super.setBounds(0,0,550,550);
		super.setMinimumSize(new Dimension(400,350));
		super.setIconImage(Core.Resources.Icons.get("Frame/Icon").getImage());
		if((Core.Properties.get("org.dyndns.doujindb.ui.always_on_top").asBoolean()) == true)
			super.setAlwaysOnTop(true);
		super.getContentPane().setBackground(Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
		Core.Logger.log("Basic user interface loaded.", Level.INFO);
			
		try
		{
			Theme theme = new Theme(
					Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor(),
					Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor(),
					Core.Resources.Font);
		    MetalLookAndFeel.setCurrentTheme(theme);
		    UIManager.setLookAndFeel(new MetalLookAndFeel());
		    SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e)
		{
			Core.Logger.log(e.getMessage(), Level.ERROR);
			return;
		}
		Core.Logger.log("Theme loaded.", Level.INFO);
		
		LoadingDialog loading = new LoadingDialog(super.getTitle());
		
	    JComponent glassPane = new JComponent()
	    {
			@Override
			public void setVisible(boolean visible)
			{
				if(!isEnabled())
					return;
				super.setVisible(visible);
				if(!visible)
					super.removeAll();
			}
			@Override
	    	protected void paintComponent(Graphics g)
	    	{
	    		g.setColor(getBackground());
	    		g.fillRect(0, 0, getSize().width, getSize().height);
	    	}
			@Override
	    	public void setBackground(Color background)
	    	{
	    		super.setBackground( background );
	    	}
	    };
	    glassPane.addMouseListener(new MouseAdapter(){});
	    glassPane.setOpaque(false);
	    glassPane.setVisible(false);
	    glassPane.setEnabled(false);
	    glassPane.setBackground(new Color(0x22, 0x22, 0x22, 0xae));
		setGlassPane(glassPane);
		Core.Logger.log("Glass panel added.", Level.INFO);
		
		uiPanelTabbed = new JTabbedPane();
		super.addWindowListener(this);
		super.addComponentListener(this);
		
		{
			UIManager.put("ComboBox.selectionBackground", Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
			UIManager.put("InternalFrame.inactiveTitleForeground", Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor().darker());
			UIManager.put("InternalFrame.activeTitleForeground", Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor());
			UIManager.put("InternalFrame.inactiveTitleBackground", Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor().darker());
			UIManager.put("InternalFrame.activeTitleBackground", Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
			UIManager.put("InternalFrame.font",Core.Resources.Font);
			UIManager.put("InternalFrame.titleFont",Core.Resources.Font);
	    	UIManager.put("InternalFrame.iconifyIcon",Core.Resources.Icons.get("JDesktop/IFrame/Iconify"));
	    	UIManager.put("InternalFrame.minimizeIcon",Core.Resources.Icons.get("JDesktop/IFrame/Minimize"));
	    	UIManager.put("InternalFrame.maximizeIcon",Core.Resources.Icons.get("JDesktop/IFrame/Maximize"));
	    	UIManager.put("InternalFrame.closeIcon",Core.Resources.Icons.get("JDesktop/IFrame/Close"));
	    	UIManager.put("InternalFrame.border",javax.swing.BorderFactory.createEtchedBorder(0));
	    	UIManager.put("Slider.horizontalThumbIcon",Core.Resources.Icons.get("JSlider/ThumbIcon"));
	    	UIManager.put("Desktop.background",new ColorUIResource(Color.BLACK));
	    	UIManager.put("FileChooser.listFont",Core.Resources.Font);
	    	UIManager.put("FileChooser.detailsViewIcon",Core.Resources.Icons.get("FileChooser/detailsViewIcon"));
	        UIManager.put("FileChooser.homeFolderIcon",Core.Resources.Icons.get("FileChooser/homeFolderIcon"));
	        UIManager.put("FileChooser.listViewIcon",Core.Resources.Icons.get("FileChooser/listViewIcon"));
	        UIManager.put("FileChooser.newFolderIcon",Core.Resources.Icons.get("FileChooser/newFolderIcon"));
	    	UIManager.put("FileChooser.upFolderIcon",Core.Resources.Icons.get("FileChooser/upFolderIcon"));
	    	UIManager.put("FileChooser.computerIcon",Core.Resources.Icons.get("FileChooser/computerIcon"));
	    	UIManager.put("FileChooser.directoryIcon",Core.Resources.Icons.get("FileChooser/directoryIcon"));
	    	UIManager.put("FileChooser.fileIcon",Core.Resources.Icons.get("FileChooser/fileIcon"));
	    	UIManager.put("FileChooser.floppyDriveIcon",Core.Resources.Icons.get("FileChooser/floppyDriveIcon"));
	    	UIManager.put("FileChooser.hardDriveIcon",Core.Resources.Icons.get("FileChooser/hardDriveIcon"));
	    	UIManager.put("Tree.expandedIcon",Core.Resources.Icons.get("JTree/Node-"));
	    	UIManager.put("Tree.collapsedIcon",Core.Resources.Icons.get("JTree/Node+"));
	    	UIManager.put("ToolTip.foreground", Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor());
	    	UIManager.put("ToolTip.background", Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor());
	    	uiFileChooser = new JFileChooser(new File(System.getProperty("user.home")));
	    	uiFileChooser.setFont(Core.Resources.Font);
	    	uiFileChooser.setFileView(new javax.swing.filechooser.FileView()
	    	{
	    		private Hashtable<String,ImageIcon> bundleIcon;
	    		
	    		{
	    			bundleIcon = new Hashtable<String,ImageIcon>();
	    			bundleIcon.put("zip",  Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("rar",  Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("gz",   Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("tar",  Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("bz2",  Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("xz",   Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("cpio", Core.Resources.Icons.get("FileView/Archive"));
	    			bundleIcon.put("jpg",  Core.Resources.Icons.get("FileView/Image"));
	    			bundleIcon.put("jpeg", Core.Resources.Icons.get("FileView/Image"));
	    			bundleIcon.put("gif",  Core.Resources.Icons.get("FileView/Image"));
	    			bundleIcon.put("png",  Core.Resources.Icons.get("FileView/Image"));
	    			bundleIcon.put("tiff", Core.Resources.Icons.get("FileView/Image"));
	    			bundleIcon.put("txt",  Core.Resources.Icons.get("FileView/Text"));
	    			bundleIcon.put("sql",  Core.Resources.Icons.get("FileView/Text"));
	    			bundleIcon.put("db",   Core.Resources.Icons.get("FileView/Database"));
	    			bundleIcon.put("csv",  Core.Resources.Icons.get("FileView/Database"));
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
	    				return (Icon)Core.Resources.Icons.get("FileView/Folder");
	    			String ext = (filename.lastIndexOf(".") == -1) ? "" : filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
	    			if(bundleIcon.containsKey(ext))
	    	            return (Icon)bundleIcon.get(ext);
	    	        else
	    	        	return (Icon)Core.Resources.Icons.get("FileView/Default");
	    	        	// return javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(file);
	    		}
	
	    	});
	    	uiFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    	uiFileChooser.setMultiSelectionEnabled(true);
	    }
		Core.Logger.log("JFileChooser loaded.", Level.INFO);
		
		menuBar = new JMenuBar();
		menuBar.setFont(Core.Resources.Font);
		menuLogs = new JMenu("Logs");
		menuLogs.setMnemonic(KeyEvent.VK_L);
		menuLogs.setIcon(Core.Resources.Icons.get("MenuBar/Logs"));
		menuLogs.setFont(Core.Resources.Font);
		menuLogsMessage = new JCheckBoxMenuItem("Messages",Core.Resources.Icons.get("MenuBar/Logs/Message"),true);
		menuLogsMessage.setMnemonic(KeyEvent.VK_M);
		menuLogsMessage.setFont(Core.Resources.Font);
		menuLogsMessage.addActionListener(this);
		menuLogsMessage.setSelected(Core.Properties.get("org.dyndns.doujindb.log.info").asBoolean());
		menuLogsWarning = new JCheckBoxMenuItem("Warnings",Core.Resources.Icons.get("MenuBar/Logs/Warning"),true);
		menuLogsWarning.setMnemonic(KeyEvent.VK_W);
		menuLogsWarning.setFont(Core.Resources.Font);
		menuLogsWarning.addActionListener(this);
		menuLogsWarning.setSelected(Core.Properties.get("org.dyndns.doujindb.log.warning").asBoolean());
		menuLogsError = new JCheckBoxMenuItem("Errors",Core.Resources.Icons.get("MenuBar/Logs/Error"),true);
		menuLogsError.setMnemonic(KeyEvent.VK_E);
		menuLogsError.setFont(Core.Resources.Font);
		menuLogsError.addActionListener(this);
		menuLogsError.setSelected(Core.Properties.get("org.dyndns.doujindb.log.error").asBoolean());
		menuLogs.add(menuLogsMessage);
		menuLogs.add(menuLogsWarning);
		menuLogs.add(menuLogsError);
		menuBar.add(menuLogs);
		
		menuHelp = new JMenu("Help");
		menuHelp.setIcon(Core.Resources.Icons.get("MenuBar/Help"));
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuHelp.setFont(Core.Resources.Font);
		menuHelpAbout = new JMenuItem("About",Core.Resources.Icons.get("MenuBar/Help/About"));
		menuHelpAbout.setMnemonic(KeyEvent.VK_A);
		menuHelpAbout.setFont(Core.Resources.Font);
		menuHelpAbout.addActionListener(this);
		menuHelp.add(menuHelpAbout);
		menuHelpBugtrack = new JMenuItem("Report Bug",Core.Resources.Icons.get("MenuBar/Help/Bugtrack"));
		menuHelpBugtrack.setMnemonic(KeyEvent.VK_R);
		menuHelpBugtrack.setFont(Core.Resources.Font);
		menuHelpBugtrack.addActionListener(this);
		menuHelp.add(menuHelpBugtrack);
		menuBar.add(menuHelp);
		super.setJMenuBar(menuBar);
		Core.Logger.log("JMenuBar added.", Level.INFO);
		
		JPanel bogus;
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelDesktopShow = new JButton(Core.Resources.Icons.get("Frame/Tab/Explorer/Desktop"));
		uiPanelDesktopShow.addActionListener(this);
		uiPanelDesktopShow.setBorder(null);
		uiPanelDesktopShow.setFocusable(false);
		uiPanelDesktopShow.setToolTipText("Show Desktop");
		bogus.add(uiPanelDesktopShow);
		uiPanelDesktopSearch = new JButton(Core.Resources.Icons.get("Frame/Tab/Explorer/Search"));
		uiPanelDesktopSearch.addActionListener(this);
		uiPanelDesktopSearch.setBorder(null);
		uiPanelDesktopSearch.setFocusable(false);
		uiPanelDesktopSearch.setToolTipText("Search");
		bogus.add(uiPanelDesktopSearch);
		uiPanelDesktopAdd = new JButton(Core.Resources.Icons.get("Frame/Tab/Explorer/Add"))
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
		uiPanelDesktopAdd.setToolTipText("Add Item");
		uiPanelDesktopAddPopup = new JPopupMenu();
		JMenuItem itm0 = new JMenuItem("Artist",Core.Resources.Icons.get("Desktop/Explorer/Artist"));
		itm0.setActionCommand("Add:{Artist}");
		itm0.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm0);
		JMenuItem itm1 = new JMenuItem("Book",Core.Resources.Icons.get("Desktop/Explorer/Book"));
		itm1.setActionCommand("Add:{Book}");
		itm1.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm1);
		JMenuItem itm2 = new JMenuItem("Circle",Core.Resources.Icons.get("Desktop/Explorer/Circle"));
		itm2.setActionCommand("Add:{Circle}");
		itm2.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm2);
		JMenuItem itm3 = new JMenuItem("Parody",Core.Resources.Icons.get("Desktop/Explorer/Parody"));
		itm3.setActionCommand("Add:{Parody}");
		itm3.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm3);
		JMenuItem itm4 = new JMenuItem("Convention",Core.Resources.Icons.get("Desktop/Explorer/Convention"));
		itm4.setActionCommand("Add:{Convention}");
		itm4.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm4);
		JMenuItem itm5 = new JMenuItem("Content",Core.Resources.Icons.get("Desktop/Explorer/Content"));
		itm5.setActionCommand("Add:{Content}");
		itm5.addActionListener(UI.this);
		uiPanelDesktopAddPopup.add(itm5);
		bogus.add(uiPanelDesktopAdd);
		m_LabelConnectionStatus = new JLabel(Core.Resources.Icons.get("Frame/Tab/Explorer/StatusBar/Disconnected"));
		m_LabelConnectionStatus.setText("Disconnected.");
		m_LabelConnectionStatus.setHorizontalAlignment(JLabel.LEFT);
		m_LabelConnectionStatus.setBorder(null);
		m_LabelConnectionStatus.setFocusable(false);
		bogus.add(m_LabelConnectionStatus);
		m_ButtonConnectionCtl = new JButton(Core.Resources.Icons.get("Frame/Tab/Explorer/StatusBar/Connect"));
		m_ButtonConnectionCtl.addActionListener(this);
		m_ButtonConnectionCtl.setBorder(null);
		m_ButtonConnectionCtl.setFocusable(false);
		m_ButtonConnectionCtl.setToolTipText("Connect");
		m_ButtonConnectionCtl.setDisabledIcon(Core.Resources.Icons.get("Frame/Tab/Explorer/StatusBar/Connecting"));
		bogus.add(m_ButtonConnectionCtl);
		Desktop = new DesktopEx();
		bogus.add(Desktop);
		uiPanelTabbed.addTab("Explorer", Core.Resources.Icons.get("Frame/Tab/Explorer"), bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		Hashtable<String,ImageIcon> data = new Hashtable<String,ImageIcon>();
		data.put("Icon:Console.Message",Core.Resources.Icons.get("Frame/Tab/Logs/Message"));
		data.put("Icon:Console.Warning",Core.Resources.Icons.get("Frame/Tab/Logs/Warning"));
		data.put("Icon:Console.Error",Core.Resources.Icons.get("Frame/Tab/Logs/Error"));
		data.put("Icon:Console.Fatal",Core.Resources.Icons.get("Frame/Tab/Logs/Fatal"));
		data.put("Icon:Console.Debug",Core.Resources.Icons.get("Frame/Tab/Logs/Debug"));
		uiPanelLogs = new PanelLogs(data);
		uiPanelLogsScroll = new JScrollPane(uiPanelLogs);
		bogus.add(uiPanelLogsScroll);
		uiPanelLogsClear = new JButton(Core.Resources.Icons.get("Frame/Tab/Logs/Clear"));
		uiPanelLogsClear.addActionListener(this);
		uiPanelLogsClear.setBorder(null);
		uiPanelLogsClear.setFocusable(false);
		uiPanelLogsClear.setToolTipText("Clear");
		bogus.add(uiPanelLogsClear);
		uiPanelTabbed.addTab("Logs", Core.Resources.Icons.get("Frame/Tab/Logs"), bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelSettings = new PanelSettings();
		bogus.add(uiPanelSettings);
		uiPanelSettingsSave = new JButton(Core.Resources.Icons.get("Frame/Tab/Settings/Save"));
		uiPanelSettingsSave.addActionListener(this);
		uiPanelSettingsSave.setBorder(null);
		uiPanelSettingsSave.setFocusable(false);
		uiPanelSettingsSave.setToolTipText("Save");
		bogus.add(uiPanelSettingsSave);
		uiPanelSettingsLoad = new JButton(Core.Resources.Icons.get("Frame/Tab/Settings/Load"));
		uiPanelSettingsLoad.addActionListener(this);
		uiPanelSettingsLoad.setBorder(null);
		uiPanelSettingsLoad.setFocusable(false);
		uiPanelSettingsLoad.setToolTipText("Load");
		bogus.add(uiPanelSettingsLoad);
		uiPanelTabbed.addTab("Settings", Core.Resources.Icons.get("Frame/Tab/Settings"), bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelPlugins = new PanelPlugins();
		uiPanelPluginsScroll = new JScrollPane(uiPanelPlugins);
		bogus.add(uiPanelPluginsScroll);
		uiPanelPluginsReload = new JButton(Core.Resources.Icons.get("Frame/Tab/Plugins/Reload"));
		uiPanelPluginsReload.addActionListener(this);
		uiPanelPluginsReload.setBorder(null);
		uiPanelPluginsReload.setFocusable(false);
		uiPanelPluginsReload.setToolTipText("Reload");
		bogus.add(uiPanelPluginsReload);
		
		uiPanelPlugins.clear();
		for(Plugin plugin : PluginManager.plugins())
			uiPanelPlugins.add(plugin);
		
		uiPanelTabbed.addTab("Plugins", Core.Resources.Icons.get("Frame/Tab/Plugins"), bogus);
		
		bogus = new JPanel();
		bogus.setLayout(null);
		uiPanelTabbed.addTab("Network", Core.Resources.Icons.get("Frame/Tab/Network"), bogus);
		uiPanelTabbed.setEnabledAt(uiPanelTabbed.getTabCount()-1, false);
	
		uiPanelTabbed.setFont(Core.Resources.Font);
		uiPanelTabbed.setFocusable(false);
		super.add(uiPanelTabbed);
		
		Core.Logger.log("JTabbedPane added.", Level.INFO);
		
		if(SystemTray.isSupported())
		{
			uiTrayPopup = new JPopupMenu();
			uiTrayPopup.setFont(Core.Resources.Font);
			uiTrayPopupExit = new JMenuItem("Exit",Core.Resources.Icons.get("Frame/Tray/Exit"));
			uiTrayPopupExit.setActionCommand("Exit");
			uiTrayPopupExit.addActionListener(this);
			uiTrayPopup.add(uiTrayPopupExit);
			uiTrayIcon = new TrayIcon(Core.Resources.Icons.get("Frame/Tray").getImage(),this.getTitle(),null);
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
			Core.Logger.log("SystemTray loaded.", Level.INFO);
		}else
		Core.Logger.log("SystemTray not supported.", Level.WARNING);

		super.setLayout(this);
		/*
		 * 16/4/2011 - Bug introduced migrating from JDK6 to JDK7
		 * java.lang.IllegalStateException: This function should be called while holding treeLock
		 * super.validateTree();
		 */
		super.setVisible(true);
		
		loading.dispose();
	
		Core.Logger.loggerAttach(uiPanelLogs);
	}

	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		m_ButtonConnectionCtl.setBounds(width - 22,Desktop.getParent().getHeight()-20,20,20);
		m_LabelConnectionStatus.setBounds(1,Desktop.getParent().getHeight()-20,width-25,20);
		if(Core.Database.isConnected())
		{
			uiPanelDesktopShow.setBounds(1,1,20,20);
			uiPanelDesktopSearch.setBounds(21,1,20,20);
			uiPanelDesktopAdd.setBounds(41,1,20,20);
		}
		else
		{
			uiPanelDesktopShow.setBounds(-1,-1,0,0);
			uiPanelDesktopSearch.setBounds(-1,-1,0,0);
			uiPanelDesktopAdd.setBounds(-1,-1,0,0);
		}
		Desktop.setBounds(1,22,width-5,Desktop.getParent().getHeight()-42);
		uiPanelLogsClear.setBounds(1,1,20,20);
		uiPanelLogsScroll.setBounds(0,22,width-5,height-48);
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
		if(event.getSource() == menuLogsMessage ||
			event.getSource() == menuLogsWarning ||
			event.getSource() == menuLogsError)
		{
			String sh_message = menuLogsMessage.isSelected() ? "Message" : "-",
					sh_warning = menuLogsWarning.isSelected() ? "Warning" : "-",
					sh_error = menuLogsError.isSelected() ? "Error" : "-";
			String pattern = sh_message + "|" + sh_warning + "|" + sh_error;
			uiPanelLogs.setFilter(pattern);
			uiPanelLogs.validate();
			return;
		}
		if(event.getSource() == uiPanelLogsClear)
		{
			uiPanelLogs.clear();
			return;
		}
		if(event.getSource() == uiPanelSettingsSave)
		{
			try
			{
				Core.Properties.save();
				Core.Logger.log("System properties saved.", Level.INFO);
			}catch(Exception e)
			{
				Core.Logger.log(e.getMessage(), Level.ERROR);
			}
			return;
		}
		if(event.getSource() == uiPanelPluginsReload)
		{
			try
			{
				uiPanelPlugins.clear();
				for(Plugin plugin : PluginManager.plugins())
					uiPanelPlugins.add(plugin);
			}catch(Exception e)
			{
				Core.Logger.log(e.getMessage(), Level.ERROR);
			}
			return;
		}
		if(event.getSource() == uiPanelSettingsLoad)
		{
			try
			{
				Core.Properties.load();
				uiPanelSettings.reload();
				Core.Logger.log("System properties loaded.", Level.INFO);
			}catch(Exception e)
			{
				Core.Logger.log(e.getMessage(), Level.ERROR);
			}
			return;
		}
		if(event.getSource() == menuHelpAbout)
		{
			{
				JPanel panel = new JPanel();
				panel.setSize(240, 400);
				panel.setLayout(new GridLayout(2,1));
				JLabel lab = new JLabel(Core.Resources.Icons.get("Frame/Dialog/About"));
				lab.setOpaque(true);
				panel.add(lab);
				JLabel l = new JLabel("<html><body style='margin:5px'>" +
						"<b style='font-size:10px'>DoujinDB</b><br>" +
						"<span style='font-size:9px'>" +
						UI.class.getPackage().getSpecificationVersion() +
						"</span><br>" +
						"<br>" +
						"<span style='font-size:9px'>" +
						"Doujin Database written in Java™<br>" +
						"JVM Version : " + System.getProperty("java.runtime.version") + "<br>" +
						"Build ID : " + UI.class.getPackage().getImplementationVersion() + "<br>" +
						"Copyright : " + UI.class.getPackage().getImplementationVendor() + "<br>" +
						"eMail : N/A<br>" +
						"Website : <br/><a href='http://code.google.com/p/doujindb/'>http://code.google.com/p/doujindb/</a><br>" +
						"</span></body></html>");
				JPanel bottom = new JPanel();
				bottom.setLayout(new BorderLayout(5, 5));
				bottom.add(l, BorderLayout.CENTER);
				bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
				JButton ok = new JButton("Ok");
				ok.setFont(Core.Resources.Font);
				ok.setMnemonic('O');
				ok.setFocusable(false);
				ok.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
						window.dispose();
					}					
				});
				bottom.add(ok, BorderLayout.SOUTH);
				panel.add(bottom);
				try {
					Core.UI.Desktop.showDialog(
							UI.this,
							panel,
							Core.Resources.Icons.get("MenuBar/Help/About"),
							"About");
				} catch (PropertyVetoException pve) { } 
			}
			return;
		}
		if(event.getSource() == menuHelpBugtrack)
		{
			try
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				desktop.browse(new URI("http://code.google.com/p/doujindb/issues/"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				Core.Logger.log(ioe.getMessage(), Level.WARNING);
			} catch (URISyntaxException use) {
				use.printStackTrace();
				Core.Logger.log(use.getMessage(), Level.WARNING);
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
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
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
				e.printStackTrace();
				Core.Logger.log(e.getMessage(), Level.ERROR);
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Artist}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Book}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Circle}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Convention}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONVENTION, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Content}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
			return;
		}
		if(event.getActionCommand().equals("Add:{Parody}"))
		{
			try {
				Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, null);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
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
					if(Core.Database.isConnected())
					{
						try
						{
							Core.Database.disconnect();
							Core.Logger.log("Disconnected.", Level.INFO);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
						}
					} else {
						try
						{
							Core.Database.connect();
							Core.Database.doRollback();
							Core.Logger.log("Connected to " + Core.Database.getConnection() + ".", Level.INFO);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
						}
					}
					return null;
				}
				@Override
				protected void done()
				{
					if(Core.Database.isConnected())
					{
						m_ButtonConnectionCtl.setIcon(Core.Resources.Icons.get("Frame/Tab/Explorer/StatusBar/Disconnect"));
						m_LabelConnectionStatus.setText("Connected to " + Core.Database.getConnection() + ".");
					} else {
						m_ButtonConnectionCtl.setIcon(Core.Resources.Icons.get("Frame/Tab/Explorer/StatusBar/Connect"));
						m_LabelConnectionStatus.setText("Disonnected.");
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
		if((Core.Properties.get("org.dyndns.doujindb.ui.tray_on_exit").asBoolean()) == false)
			System.exit(0);
		setState(JFrame.ICONIFIED);
		try
		{
			SystemTray uiTray = SystemTray.getSystemTray();
			uiTray.add(uiTrayIcon);
			setVisible(false);
		}catch(AWTException awte){
			awte.printStackTrace();
			Core.Logger.log(awte.getMessage(), Level.ERROR);
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
	private static final class PanelLogs extends JTable implements Logger
	{
		private Renderer TableRender;
		private Editor TableEditor;
		private DefaultTableModel TableModel;
		private TableRowSorter<DefaultTableModel> TableSorter;
		private String FilterPattern = "";

		public PanelLogs(Hashtable<String,ImageIcon> renderingData)
		{
			super();
			TableModel = new DefaultTableModel();
			TableModel.addColumn("");
			TableModel.addColumn("Time");
			TableModel.addColumn("Component");
			TableModel.addColumn("Message");
			super.setModel(TableModel);
			TableRender = new Renderer(renderingData);
			TableEditor = new Editor();
			TableSorter = new TableRowSorter<DefaultTableModel>(TableModel);
			super.setRowSorter(TableSorter);
			TableSorter.setRowFilter(RowFilter.regexFilter("", 0));
			super.setFont(Core.Resources.Font);
			super.setColumnSelectionAllowed(false);
			super.setRowSelectionAllowed(false);
			super.setCellSelectionEnabled(false);
			super.getTableHeader().setFont(Core.Resources.Font);
			super.getTableHeader().setReorderingAllowed(false);
			for(int k = 0;k<super.getColumnModel().getColumnCount();k++)
			{
				super.getColumnModel().getColumn(k).setCellRenderer(TableRender);
				super.getColumnModel().getColumn(k).setCellEditor(TableEditor);
			}
			super.getColumnModel().getColumn(0).setResizable(false);
			super.getColumnModel().getColumn(0).setMaxWidth(20);
			super.getColumnModel().getColumn(0).setMinWidth(20);
			super.getColumnModel().getColumn(0).setWidth(20);
			super.getColumnModel().getColumn(2).setMinWidth(150);
			super.getColumnModel().getColumn(2).setWidth(150);
			super.getColumnModel().getColumn(2).setPreferredWidth(150);
			super.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		}
	
		public void setFilter(String pattern)
		{
			FilterPattern = pattern;
		}
		
		@Override
		public void validate()
		{
			TableSorter.setRowFilter(RowFilter.regexFilter(FilterPattern, 0));
			super.validate();
		}
	
		public void clear()
		{
			while(TableModel.getRowCount()>0)
				TableModel.removeRow(0);
		}
		
		private static final class Renderer extends DefaultTableCellRenderer
		{
		    private Hashtable<String,ImageIcon> renderIcon;
		    private Color backgroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor();
			private Color foregroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
			
			private static SimpleDateFormat sdf;
			
			{
				sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			}
		
			public Renderer(Hashtable<String,ImageIcon> renderingData)
			{
			    super();
			    super.setFont(Core.Resources.Font);
			    renderIcon = new Hashtable<String,ImageIcon>();
			    renderIcon.put("Message",(ImageIcon)renderingData.get("Icon:Console.Message"));
			    renderIcon.put("Warning",(ImageIcon)renderingData.get("Icon:Console.Warning"));
			    renderIcon.put("Error",(ImageIcon)renderingData.get("Icon:Console.Error"));
			    renderIcon.put("Fatal",(ImageIcon)renderingData.get("Icon:Console.Fatal"));
			    renderIcon.put("Debug",(ImageIcon)renderingData.get("Icon:Console.Debug"));
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
			        if(value.equals("{Message}"))
			        {
				        super.setText("");
				        super.setIcon(renderIcon.get("Message"));
				        return this;
				    }
				    if(value.equals("{Warning}"))
				    {
				    	super.setText("");
				        super.setIcon(renderIcon.get("Warning"));
				        return this;
				    }
				    if(value.equals("{Error}"))
				    {
				       	super.setText("");
				       	super.setIcon(renderIcon.get("Error"));
				       	return this;
				    }
				    if(value.equals("{Fatal}"))
				    {
				       	super.setText("");
				       	super.setIcon(renderIcon.get("Fatal"));
				       	return this;
				    }
				    if(value.equals("{Debug}"))
				    {
				       	super.setText("");
				       	super.setIcon(renderIcon.get("Debug"));
				       	return this;
				    }
				    super.setText(value.toString());
				    if(column == 1)
					{
						// It's a timestamp
						super.setText(sdf.format(new Date((Long)value)));
					}
			        super.setIcon(null);
			        super.setForeground(foregroundColor);
			        if(table.getValueAt(row, 0).equals("{Warning}"))
				       	super.setForeground(Color.ORANGE);
				    else
				       	if(table.getValueAt(row, 0).equals("{Error}"))
				       		super.setForeground(Color.RED);
				       	else
					       	if(table.getValueAt(row, 0).equals("{Fatal}"))
					       	{
					       		super.setForeground(Color.RED);
					       		super.setFont(super.getFont().deriveFont(Font.BOLD));
					       	}
					       	else
						       	if(table.getValueAt(row, 0).equals("{Debug}"))
						       		super.setForeground(Color.GREEN);
			        
			        return this;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					// OH WELL
				}
		        return this;
			}
		}
	
		private final class Editor extends AbstractCellEditor implements TableCellEditor
		{
			public Editor()	{
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
			    int column)
			{
			    super.cancelCellEditing();
			    return null;
			}
		}
	
		@Override
		public void log(LogEvent event)
		{
			switch(event.getLevel())
			{
			case INFO:
				TableModel.addRow(new Object[]{"{Message}",
					event.getTime(),
					event.getSource(),
					event.getMessage()});
				break;
			case WARNING:
				TableModel.addRow(new Object[]{"{Warning}",
					event.getTime(),
					event.getSource(),
					event.getMessage()});
				break;
			case ERROR:
				TableModel.addRow(new Object[]{"{Error}",
					event.getTime(),
					event.getSource(),
					event.getMessage()});
				break;
			case FATAL:
				TableModel.addRow(new Object[]{"{Fatal}",
					event.getTime(),
					event.getSource(),
					event.getMessage()});
				break;
			case DEBUG:
				TableModel.addRow(new Object[]{"{Debug}",
					event.getTime(),
					event.getSource(),
					event.getMessage()});
				break;
			}
		}
	
		@Override
		public void loggerAttach(Logger logger) { }
		
		@Override
		public void loggerDetach(Logger logger) { }
	
		@Override
		public void log(String message, Level level) { }
	}
	
    @SuppressWarnings("serial")
	private final class PanelPlugins extends JTable
	{
		private Renderer TableRender;
		private Editor TableEditor;
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
			super.setFont(Core.Resources.Font);
			super.setColumnSelectionAllowed(false);
			super.setRowSelectionAllowed(false);
			super.setCellSelectionEnabled(false);
			super.getTableHeader().setFont(Core.Resources.Font);
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
		    private Color backgroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor();
			private Color foregroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
		
			public Renderer()
			{
			    super();
			    super.setFont(Core.Resources.Font);
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
				        super.setIcon(Core.Resources.Icons.get("Frame/Tab/Plugins"));
				        return this;
				    }
				    super.setText(value.toString());
			        super.setIcon(null);
			        super.setForeground(foregroundColor);
			        
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
		private TreeRenderer render;
		private DefaultTreeModel model = new DefaultTreeModel(null);
		
		public static final int TYPE_BOOLEAN = 0x0000;
		public static final int TYPE_INTEGER = 0x0001;
		public static final int TYPE_STRING = 0x0002;
		public static final int TYPE_COLOR = 0x0003;
		public static final int TYPE_FONT = 0x0004;
		public static final int TYPE_FILE = 0x0005;
		public static final int TYPE_UNKNOWN = 0xffff;

	public PanelSettings()
	{
		super();
		tree = new JTree();
		tree.setModel(model);
		tree.setFocusable(false);
		tree.setFont(Core.Resources.Font);
		tree.setEditable(false);
		tree.setRootVisible(true);
		tree.setScrollsOnExpand(true);
		tree.addTreeSelectionListener(new TreeSelectionListener()
	    {
	    	public void valueChanged(TreeSelectionEvent e)
	    	{
	    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	            if (node == null) return;
	            Object nodeInfo = node.getUserObject();
	            NodeValue value = (NodeValue) nodeInfo;
	    		if(value.getValue() == null)
	    				return;
	            Object path_objects[] = node.getUserObjectPath();
	            String key = "";
	            for(Object o : path_objects)
	            	key += o + ".";
	            key = "org.dyndns.doujindb." + key.substring(0, key.length() - 1).substring("org.dyndns.doujindb.".length());
	            ValueEditor editor = new ValueEditor(key);
	            setRightComponent(editor);
	    	}
	    });
		render = new TreeRenderer();
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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new NodeValue(null, "org.dyndns.doujindb"));
		for(String key : Core.Properties.keys())
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
		String key2;
		DefaultMutableTreeNode node2 = node;
		if(key.indexOf(".") != -1)
		{
			_l00p:
			{
				key2 = key.substring(0, key.indexOf("."));
				key = key.substring(key.indexOf(".") + 1);
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> e = node.children();
				while(e.hasMoreElements())
				{
					DefaultMutableTreeNode node3 = e.nextElement();
					NodeValue value = (NodeValue) node3.getUserObject();
					if(key2.equals(value.getName()))
					{
						addNode(node3, key);
						break _l00p;
					}
				}
				node2 = new DefaultMutableTreeNode(new NodeValue(null, key2));
				((DefaultMutableTreeNode)node).add(node2);
				addNode(node2, key);
			}
		}else
		{
			Object path_objects[] = node.getUserObjectPath();
			String path = "";
			for(Object o : path_objects)
				path += o + ".";
			path = "org.dyndns.doujindb." + path.substring(0, path.length() - 1).substring("org.dyndns.doujindb.".length());
			node2 = new DefaultMutableTreeNode(new NodeValue(Core.Properties.get(path + "." + key), key));
			((DefaultMutableTreeNode)node).add(node2);
		}
	}
	
	private final class NodeValue
	{
		private Object value;
		private String name;
		private int type;
		
		public NodeValue(Object value)
		{
			this(value, value.toString());
		}
		public NodeValue(Object value, String name)
		{
			this.value = value;
			this.name = name;
			this.type = TYPE_UNKNOWN;
			if(value instanceof Boolean)
				type = TYPE_BOOLEAN;
			if(value instanceof Integer)
				type = TYPE_INTEGER;
			if(value instanceof String)
				type = TYPE_STRING;
			if(value instanceof Color)
				type = TYPE_COLOR;
			if(value instanceof Font)
				type = TYPE_FONT;
			if(value instanceof File)
				type = TYPE_FILE;
		}
		public int getType() {
			return type;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	private final class TreeRenderer extends DefaultTreeCellRenderer
	{
		private Hashtable<String,Icon> renderIcon;

	public TreeRenderer()
	{
		renderIcon=new Hashtable<String,Icon>();
	    setBackgroundSelectionColor(MetalLookAndFeel.getWindowBackground());
	    renderIcon.put("Directory",Core.Resources.Icons.get("Frame/Tab/Settings/Tree/Directory"));
	    renderIcon.put("Value",Core.Resources.Icons.get("Frame/Tab/Settings/Tree/Value"));
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
		NodeValue node = (NodeValue) ((DefaultMutableTreeNode)value).getUserObject();
		if(node.getValue() == null)
			setIcon((ImageIcon)renderIcon.get("Directory"));
		else
			setIcon((ImageIcon)renderIcon.get("Value"));
		setText(node.getName());
	    return this;
	}
	}
	
	private class ValueEditor extends JPanel implements LayoutManager
	{
		private JButton close;
		private JLabel title;
		private JLabel description;
		private JComponent panel;
		private JButton editApply;
		private JButton editDiscard;
		
		private String key;
		private Object value;
		private Object valueNew;
		
		public ValueEditor(String key2)
		{
			super();
			setLayout(this);
			this.key = key2;
			this.value = Core.Properties.get(key).getValue();
			title = new JLabel(key.substring(key.lastIndexOf('.')+1), Core.Resources.Icons.get("Frame/Tab/Settings/Tree/Value"), JLabel.LEFT);
			title.setFont(Core.Resources.Font);
			add(title);
			description = new JLabel("<html><body><b>Type</b> : " + value.getClass().getCanonicalName() + "<br/><b>Description</b> : " + Core.Properties.get(key).getDescription() + "</body></html>");
			description.setVerticalAlignment(JLabel.TOP);
			description.setFont(Core.Resources.Font);
			add(description);
			close = new JButton(Core.Resources.Icons.get("Frame/Tab/Settings/Editor/Close"));
			close.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			close.setBorder(null);
			close.setFocusable(false);
			add(close);
			editApply = new JButton("Apply", Core.Resources.Icons.get("Frame/Tab/Settings/Editor/Apply"));
			editApply.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					Core.Properties.get(key).setValue(valueNew);
				}
			});
			editApply.setFont(Core.Resources.Font);
			editApply.setFocusable(false);
			add(editApply);
			editDiscard = new JButton("Discard", Core.Resources.Icons.get("Frame/Tab/Settings/Editor/Discard"));
			editDiscard.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					setRightComponent(null);
					tree.setSelectionRow(0);
				}
			});
			editDiscard.setFont(Core.Resources.Font);
			editDiscard.setFocusable(false);
			add(editDiscard);
			panel = new JPanel();
			{
				valueNew = null;
				if(value instanceof Boolean)
				{
					boolean bool = ((Boolean)value).booleanValue();
					valueNew = new Boolean(bool);
					final JCheckBox field = new JCheckBox((bool)?"True":"False");
					field.setSelected(bool);
					field.setFocusable(false);
					field.setFont(Core.Resources.Font);
					field.addChangeListener(new ChangeListener()
					{
						@Override
						public void stateChanged(ChangeEvent ce)
						{
							boolean bool = field.isSelected();
							field.setText((bool)?"True":"False");
							valueNew = new Boolean(bool);
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
							field.setBounds(35, 5, width - 30, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
					});
				}
				if(value instanceof Integer)
				{
					int ivalue = ((Integer)value).intValue();
					valueNew = new Integer(ivalue);
					final KBigIntegerField field = new KBigIntegerField();
					field.setInt(ivalue);
					field.setFont(Core.Resources.Font);
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
						public void addLayoutComponent(String name, Component comp) {}
						@Override
						public void layoutContainer(Container comp)
						{
							int width = comp.getWidth();
							field.setBounds(45, 5, width - 90, 20);
						}
						@Override
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
					});
				}
				if(value instanceof String)
				{
					valueNew = "" + ((String)value);
					final JTextField field = new JTextField((String)value);
					field.setFont(Core.Resources.Font);
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
						public Dimension minimumLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public Dimension preferredLayoutSize(Container comp) {return new Dimension(200, 200);}
						@Override
						public void removeLayoutComponent(Component comp) {}
						
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
					field_r.setFont(Core.Resources.Font);
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
					field_g.setFont(Core.Resources.Font);
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
					field_b.setFont(Core.Resources.Font);
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
					field_a.setFont(Core.Resources.Font);
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
					list.setFont(Core.Resources.Font);
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
					final JButton chooser = new JButton(Core.Resources.Icons.get("Frame/Tab/Settings/Tree/Directory"));
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
							JFileChooser fc = Core.UI.getFileChooser();
							fc.setMultiSelectionEnabled(false);
							int prev_option = fc.getFileSelectionMode();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
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
			title.setBounds(1, 1, width - 21, 20);
			close.setBounds(width - 21, 1, 20, 20);
			description.setBounds(1, 21, width - 2, 75);
			panel.setBounds(1, 100, width - 2, height - 100 - 65);
			editApply.setBounds((width - 125) / 2, height - 60, 125, 20);
			editDiscard.setBounds((width - 125) / 2, height - 40, 125, 20);
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
	
	@SuppressWarnings("serial")
	private final class LoadingDialog extends JWindow implements Runnable,LayoutManager
	{

		private JPanel cpane;
		private IProgressBar bar;
		private JLabel title;
		private JLabel status;
		private Thread thread;
		
		private int size = 30;
		private int direction = 1;

		public LoadingDialog(String string_title)
		{
			super();
			getRootPane().setBorder(BorderFactory.createLineBorder(new Color(75,75,75),1));
			setBounds(0,0,75+2,25+2);
			setAlwaysOnTop(true);
			setVisible(true);
			title = new JLabel(string_title);
			add(title);
			cpane=new JPanel();
			cpane.setSize(145,55);
			cpane.setLayout(null);
			bar=new IProgressBar(0,1,100);
			bar.setEnabled(true);
			bar.setValue(24);
			cpane.add(bar);
			status=new JLabel("Loading...");
			status.setHorizontalAlignment(JLabel.CENTER);
			cpane.add(status);
			add(cpane);
			setLayout(this);
			setSize(cpane.getWidth()+5,cpane.getHeight()+20);
			validate();
			repaint();
			thread = new Thread(this, getClass().getCanonicalName());
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}

		public void addLayoutComponent(String name, Component comp){}
		public void layoutContainer(Container parent)
		{
			cpane.setBounds(3,16,getWidth()-8,getHeight()-21);
			status.setBounds(1,1,cpane.getWidth()-2,15);
			bar.setBounds(1,20,cpane.getWidth()-2,15);
			title.setBounds(2,2,getWidth()-17,13);
		}
		public Dimension minimumLayoutSize(Container parent){return null;}
		public Dimension preferredLayoutSize(Container parent){return null;}
		public void removeLayoutComponent(Component comp){}

		@SuppressWarnings("static-access")
		public void run()
		{
			while(isShowing())
			{
				bar.setValue(bar.getValue()+direction*5);
				if(bar.getValue() == bar.getMaximum() || bar.getValue() == bar.getMinimum())
					direction *= -1;
				try { thread.sleep(100); } catch (InterruptedException e) {;}
			}
		}

		private final class IProgressBar extends JProgressBar
		{
			public IProgressBar(int orient,int min,int max)
			{
				super(orient,min,max);
			}
			public void paint(Graphics g)
			{
				g.setColor(getBackground());
				g.fillRect(0,0,getWidth(),getHeight());
				g.setColor(getForeground());
				g.fillRect(getValue(),2,size,getHeight()-3);
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static final class ConfigurationWizard extends JComponent implements Runnable, LayoutManager
	{
		private Color color = Color.DARK_GRAY.darker();
		private JLabel uiBottomDivisor;
		private JButton uiButtonNext;
		private JButton uiButtonPrev;
		private JButton uiButtonFinish;
		private JButton uiButtonCanc;
		private JLabel uiLabelHeader;
		private JLabel uiLabelHeaderImage;
		// STEP 1
		private JLabel uiLabelWelcome;
		// STEP 2
		private JComponent uiCompDatabase;
		private JLabel uiCompDatabaseLabelDriver;
		private JTextField uiCompDatabaseTextDriver;
		private JLabel uiCompDatabaseLabelURL;
		private JTextField uiCompDatabaseTextURL;
		private JLabel uiCompDatabaseLabelUsername;
		private JTextField uiCompDatabaseTextUsername;
		private JLabel uiCompDatabaseLabelPassword;
		private JTextField uiCompDatabaseTextPassword;
		private JButton uiCompDatabaseTest;
		private JLabel uiCompDatabaseLabelResult;
		// STEP 3
		private JComponent uiCompDatastore;
		private JLabel uiCompDatastoreLabelStore;
		private JTextField uiCompDatastoreTextStore;
		private JLabel uiCompDatastoreLabelTemp;
		private JTextField uiCompDatastoreTextTemp;
		private JButton uiCompDatastoreTest;
		private JLabel uiCompDatastoreLabelResult;
		// STEP 4
		private JLabel uiLabelFinish;
		
		enum Step
		{
			WELCOME (1),
			DATABASE (2),
			DATASTORE (3),
			//TODO ? INTERFACE (4),
			FINISH (5);
			
			private final double value;
			
			Step()
			{
				this(1);
			}
			Step(int value)
			{
				this.value = value;
			}
		}
		
		private Step progress = Step.WELCOME;
		
		public ConfigurationWizard()
		{
			uiLabelHeader = new JLabel();
			uiLabelHeader.setOpaque(true);
			uiLabelHeader.setBackground(color);
			super.add(uiLabelHeader);
			uiLabelHeaderImage = new JLabel(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Header"));
			uiLabelHeaderImage.setOpaque(true);
			uiLabelHeaderImage.setBackground(color);
			super.add(uiLabelHeaderImage);
			uiLabelWelcome = new JLabel("<html>Welcome to DoujinDB.<br/>" +
					"<br/>" +
					"We couldn't find any configuration file, so either you deleted it or this is the first time you run DoujinDB.<br/>" +
					"<br/>" +
					"This wizard will help you through the process of configuring the program.<br/>" +
					"<br/>" +
					"Click <b>Next</b> to proceed." +
					"</html>");
			uiLabelWelcome.setOpaque(false);
			super.add(uiLabelWelcome);
			{
				uiCompDatabase = new JPanel();
				uiCompDatabaseLabelDriver = new JLabel("Driver");
				uiCompDatabase.add(uiCompDatabaseLabelDriver);
				uiCompDatabaseTextDriver = new JTextField("org.sqlite.JDBC");
				uiCompDatabase.add(uiCompDatabaseTextDriver);
				uiCompDatabaseLabelURL = new JLabel("URL");
				uiCompDatabase.add(uiCompDatabaseLabelURL);
				uiCompDatabaseTextURL = new JTextField("jdbc:sqlite:doujindb.sqlite");
				uiCompDatabase.add(uiCompDatabaseTextURL);
				uiCompDatabaseLabelUsername = new JLabel("Username");
				uiCompDatabase.add(uiCompDatabaseLabelUsername);
				uiCompDatabaseTextUsername = new JTextField("");
				uiCompDatabase.add(uiCompDatabaseTextUsername);
				uiCompDatabaseLabelPassword = new JLabel("Password");
				uiCompDatabase.add(uiCompDatabaseLabelPassword);
				uiCompDatabaseTextPassword = new JTextField("");
				uiCompDatabase.add(uiCompDatabaseTextPassword);
				uiCompDatabaseLabelResult = new JLabel("");
				uiCompDatabase.add(uiCompDatabaseLabelResult);
				uiCompDatabaseTest = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/DBTest"));
				uiCompDatabaseTest.setBorder(null);
				uiCompDatabaseTest.setFocusable(false);
				uiCompDatabaseTest.setText("Test");
				uiCompDatabaseTest.setToolTipText("Test");
				uiCompDatabaseTest.setMnemonic('T');
				uiCompDatabaseTest.setBorderPainted(true);
				uiCompDatabaseTest.setBorder(BorderFactory.createLineBorder(color));
				uiCompDatabaseTest.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						uiCompDatabaseTest.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Loading"));
						uiCompDatabaseTextDriver.setEditable(false);
						uiCompDatabaseTextURL.setEditable(false);
						uiCompDatabaseTextUsername.setEditable(false);
						uiCompDatabaseTextPassword.setEditable(false);
						;
						new Thread()
						{
							public void run()
							{
								try {
									Class.forName(uiCompDatabaseTextDriver.getText());
									ExecutorService executor = Executors.newCachedThreadPool();
									Callable<Connection> task = new Callable<Connection>()
									{
									   public Connection call()
									   {
									      try {
											return DriverManager.getConnection(uiCompDatabaseTextURL.getText(),
													uiCompDatabaseTextUsername.getText(),
													uiCompDatabaseTextPassword.getText());
										} catch (SQLException sqle) {
											/**
											 * SQL error messages are too verbose,
											 * mask them off with a common error message
											 * and print the stack trace to the standard output.
											 */
											uiCompDatabaseLabelResult.setText("<html>Error connecting to SQL resource '" + uiCompDatabaseTextURL.getText() + "'.</html>");
											uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
											uiCompDatabaseLabelResult.setForeground(Color.RED);
											sqle.printStackTrace();
											return null;
										}
									   }
									};
									Future<Connection> future = executor.submit(task);
									try
									{
										Connection conn = future.get(3, TimeUnit.SECONDS);
										if(conn != null)
										{
											try
											{
												uiCompDatabaseLabelResult.setText("<html>Connection established.</html>");
												uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Success"));
												uiCompDatabaseLabelResult.setForeground(Color.GREEN);
												conn.close();
											} catch (Exception e) {}
										} else {
											uiCompDatabaseLabelResult.setText("<html>Error connecting to SQL resource '" + uiCompDatabaseTextURL.getText() + "'.</html>");
											uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
											uiCompDatabaseLabelResult.setForeground(Color.RED);
										}
									} catch (TimeoutException te) {
										uiCompDatabaseLabelResult.setText("<html>Timeout Exception while obtaining SQL connection.</html>");
										uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
										uiCompDatabaseLabelResult.setForeground(Color.RED);
									} catch (InterruptedException ie) {
										uiCompDatabaseLabelResult.setText("<html>Interrupted Exception while obtaining SQL connection.</html>");
										uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
										uiCompDatabaseLabelResult.setForeground(Color.RED);
									} catch (ExecutionException ee) {
										uiCompDatabaseLabelResult.setText("<html>Execution Exception while obtaining SQL connection.</html>");
										uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
										uiCompDatabaseLabelResult.setForeground(Color.RED);
									} finally {
									   future.cancel(true);
									}
								} catch (ClassNotFoundException cnfe) {
									uiCompDatabaseLabelResult.setText("<html>Cannot load jdbc driver '" + uiCompDatabaseTextDriver.getText() + "' : Class not found.</html>");
									uiCompDatabaseLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
									uiCompDatabaseLabelResult.setForeground(Color.RED);
								}
								uiCompDatabaseTextPassword.setEditable(true);
								uiCompDatabaseTextUsername.setEditable(true);
								uiCompDatabaseTextURL.setEditable(true);
								uiCompDatabaseTextDriver.setEditable(true);
								uiCompDatabaseTest.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/DBTest"));
							}
						}.start();
					}					
				});
				uiCompDatabase.add(uiCompDatabaseTest);
				uiCompDatabase.setLayout(new LayoutManager()
				{
					@Override
					public void addLayoutComponent(String key,Component c){}
					@Override
					public void removeLayoutComponent(Component c){}
					@Override
					public Dimension minimumLayoutSize(Container parent)
					{
						return new Dimension(250,200);
					}
					@Override
					public Dimension preferredLayoutSize(Container parent)
					{
						return new Dimension(250,200);
					}
					@Override
					public void layoutContainer(Container parent)
					{
						int width = parent.getWidth(),
							height = parent.getHeight();
						int labelLength = 85;
						uiCompDatabaseLabelDriver.setBounds(5,5,labelLength,20);
						uiCompDatabaseTextDriver.setBounds(labelLength+5,5,width-labelLength-5,20);
						uiCompDatabaseLabelURL.setBounds(5,25,labelLength,20);
						uiCompDatabaseTextURL.setBounds(labelLength+5,25,width-labelLength-5,20);
						uiCompDatabaseLabelUsername.setBounds(5,45,labelLength,20);
						uiCompDatabaseTextUsername.setBounds(labelLength+5,45,width-labelLength-5,20);
						uiCompDatabaseLabelPassword.setBounds(5,65,labelLength,20);
						uiCompDatabaseTextPassword.setBounds(labelLength+5,65,width-labelLength-5,20);
						uiCompDatabaseLabelResult.setBounds(5,90,width-10,45);
						uiCompDatabaseTest.setBounds(width/2-40,height-25,80,20);
					}
				});
				super.add(uiCompDatabase);
			}
			{
				uiCompDatastore = new JPanel();
				uiCompDatastoreLabelStore = new JLabel("Store Directory");
				uiCompDatastore.add(uiCompDatastoreLabelStore);
				uiCompDatastoreTextStore = new JTextField("/path/to/store/");
				uiCompDatastore.add(uiCompDatastoreTextStore);
				uiCompDatastoreLabelTemp = new JLabel("Temporary Directory");
				uiCompDatastore.add(uiCompDatastoreLabelTemp);
				uiCompDatastoreTextTemp = new JTextField("/tmp/");
				uiCompDatastore.add(uiCompDatastoreTextTemp);
				uiCompDatastoreLabelResult = new JLabel("");
				uiCompDatastore.add(uiCompDatastoreLabelResult);
				uiCompDatastoreTest = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/DSTest"));
				uiCompDatastoreTest.setBorder(null);
				uiCompDatastoreTest.setFocusable(false);
				uiCompDatastoreTest.setText("Test");
				uiCompDatastoreTest.setToolTipText("Test");
				uiCompDatastoreTest.setMnemonic('T');
				uiCompDatastoreTest.setBorderPainted(true);
				uiCompDatastoreTest.setBorder(BorderFactory.createLineBorder(color));
				uiCompDatastoreTest.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae) 
					{
						uiCompDatastoreTest.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Loading"));
						uiCompDatastoreTextStore.setEditable(false);
						uiCompDatastoreTextTemp.setEditable(false);
						;
						new Thread()
						{
							public void run()
							{
								try {
									File store = new File(uiCompDatastoreTextStore.getText());
									if(!store.exists() || !store.isDirectory())
										throw new RuntimeException("Store folder is not a valid directory path.");
									File store_rw = new File(store, ".rw-store");
									store_rw.createNewFile();
									store_rw.deleteOnExit();
									if(!store_rw.exists())
										throw new RuntimeException("Store directory is not writable: check your permissions.");
									if(!store_rw.canRead())
										throw new RuntimeException("Store directory is not readable: check your permissions.");
									if(!store_rw.canWrite())
										throw new RuntimeException("Store directory is not writable: check your permissions.");
									File temp = new File(uiCompDatastoreTextTemp.getText());
									if(!temp.exists() || !temp.isDirectory())
										throw new RuntimeException("Temporary folder is not a valid directory path.");
									File temp_rw = new File(temp, ".rw-temp");
									temp_rw.createNewFile();
									temp_rw.deleteOnExit();
									if(!temp_rw.exists())
										throw new RuntimeException("Temporary directory is not writable: check your permissions.");
									if(!temp_rw.canRead())
										throw new RuntimeException("Temporary directory is not readable: check your permissions.");
									if(!temp_rw.canWrite())
										throw new RuntimeException("Temporary directory is not writable: check your permissions.");
									;
									uiCompDatastoreLabelResult.setText("<html>Both directories are valid.</html>");
									uiCompDatastoreLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Success"));
									uiCompDatastoreLabelResult.setForeground(Color.GREEN);
								} catch (RuntimeException re) {
									uiCompDatastoreLabelResult.setText("<html>" + re.getMessage() + "</html>");
									uiCompDatastoreLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
									uiCompDatastoreLabelResult.setForeground(Color.RED);
								} catch (IOException ioe) {
									uiCompDatastoreLabelResult.setText("<html>" + ioe.getMessage() + "</html>");
									uiCompDatastoreLabelResult.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Error"));
									uiCompDatastoreLabelResult.setForeground(Color.RED);
								}
								uiCompDatastoreTextTemp.setEditable(true);
								uiCompDatastoreTextStore.setEditable(true);
								uiCompDatastoreTest.setIcon(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/DSTest"));
							}
						}.start();
					}					
				});
				uiCompDatastore.add(uiCompDatastoreTest);
				uiCompDatastore.setLayout(new LayoutManager()
				{
					@Override
					public void addLayoutComponent(String key,Component c){}
					@Override
					public void removeLayoutComponent(Component c){}
					@Override
					public Dimension minimumLayoutSize(Container parent)
					{
						return new Dimension(250,200);
					}
					@Override
					public Dimension preferredLayoutSize(Container parent)
					{
						return new Dimension(250,200);
					}
					@Override
					public void layoutContainer(Container parent)
					{
						int width = parent.getWidth(),
							height = parent.getHeight();
						uiCompDatastoreLabelStore.setBounds(5,5,width-10,20);
						uiCompDatastoreTextStore.setBounds(5,25,width-10,20);
						uiCompDatastoreLabelTemp.setBounds(5,45,width-10,20);
						uiCompDatastoreTextTemp.setBounds(5,65,width-10,20);
						uiCompDatastoreLabelResult.setBounds(5,90,width-10,45);
						uiCompDatastoreTest.setBounds(width/2-40,height-25,80,20);
					}
				});
				super.add(uiCompDatastore);
			}
			uiLabelFinish = new JLabel("<html>DoujinDB is now configured.<br/>" +
					"<br/>" +
					"You can later change all these settings from the <b>Settings</b> tab (where you'll find more things to be customized).<br/>" +
					"<br/>" +
					"Click <b>Finish</b> to end this Wizard." +
					"</html>");
			uiLabelFinish.setOpaque(false);
			super.add(uiLabelFinish);
			uiBottomDivisor = new JLabel();
			uiBottomDivisor.setOpaque(true);
			uiBottomDivisor.setBackground(color);
			super.add(uiBottomDivisor);
			uiButtonNext = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Next"));
			uiButtonNext.setBorder(null);
			uiButtonNext.setFocusable(false);
			uiButtonNext.setText("Next");
			uiButtonNext.setToolTipText("Next");
			uiButtonNext.setMnemonic('N');
			uiButtonNext.setBorderPainted(true);
			uiButtonNext.setBorder(BorderFactory.createLineBorder(color));
			uiButtonNext.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					next();
				}					
			});
			super.add(uiButtonNext);
			uiButtonPrev = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Prev"));
			uiButtonPrev.setEnabled(false);
			uiButtonPrev.setBorder(null);
			uiButtonPrev.setFocusable(false);
			uiButtonPrev.setText("Back");
			uiButtonPrev.setToolTipText("Back");
			uiButtonPrev.setMnemonic('B');
			uiButtonPrev.setBorderPainted(true);
			uiButtonPrev.setBorder(BorderFactory.createLineBorder(color));
			uiButtonPrev.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					back();
				}					
			});
			super.add(uiButtonPrev);
			uiButtonFinish = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Finish"));
			uiButtonFinish.setVisible(false);
			uiButtonFinish.setBorder(null);
			uiButtonFinish.setFocusable(false);
			uiButtonFinish.setText("Finish");
			uiButtonFinish.setToolTipText("Finish");
			uiButtonFinish.setMnemonic('F');
			uiButtonFinish.setBorderPainted(true);
			uiButtonFinish.setBorder(BorderFactory.createLineBorder(color));
			uiButtonFinish.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					Core.Properties.get("org.dyndns.doujindb.db.driver").setValue(uiCompDatabaseTextDriver.getText());
					Core.Properties.get("org.dyndns.doujindb.db.url").setValue(uiCompDatabaseTextURL.getText());
					Core.Properties.get("org.dyndns.doujindb.db.username").setValue(uiCompDatabaseTextUsername.getText());
					Core.Properties.get("org.dyndns.doujindb.db.password").setValue(uiCompDatabaseTextPassword.getText());
					Core.Properties.get("org.dyndns.doujindb.dat.datastore").setValue(uiCompDatastoreTextStore.getText());
					Core.Properties.get("org.dyndns.doujindb.dat.temp").setValue(uiCompDatastoreTextTemp.getText());
					Core.Properties.save();
					DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
					window.dispose();
				}					
			});
			super.add(uiButtonFinish);
			uiButtonCanc = new JButton(Core.Resources.Icons.get("Frame/Dialog/ConfigurationWizard/Cancel"));
			uiButtonCanc.setBorder(null);
			uiButtonCanc.setFocusable(false);
			uiButtonCanc.setText("Cancel");
			uiButtonCanc.setToolTipText("Cancel");
			uiButtonCanc.setMnemonic('C');
			uiButtonCanc.setBorderPainted(true);
			uiButtonCanc.setBorder(BorderFactory.createLineBorder(color));
			uiButtonCanc.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) 
				{
					DialogEx window = (DialogEx)((JComponent)ae.getSource()).getRootPane().getParent();
					window.dispose();
				}					
			});
			super.add(uiButtonCanc);
			super.setLayout(this);
		}
		
		@Override
		public void addLayoutComponent(String key,Component c){}
		@Override
		public void removeLayoutComponent(Component c){}
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			return new Dimension(300,250);
		}
		@Override
		public Dimension preferredLayoutSize(Container parent)
		{
			return new Dimension(300,250);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			uiLabelHeader.setBounds(0,0,width-48,48);
			uiLabelHeaderImage.setBounds(width-48,0,48,48);
			uiBottomDivisor.setBounds(5,height-30,width-10,1);
			uiButtonNext.setBounds(width-80,height-25,75,20);
			uiButtonFinish.setBounds(width-80,height-25,75,20);
			uiButtonPrev.setBounds(width-160,height-25,75,20);
			uiButtonCanc.setBounds(5,height-25,75,20);
			uiLabelWelcome.setBounds(0,0,0,0);
			uiCompDatabase.setBounds(0,0,0,0);
			uiCompDatastore.setBounds(0,0,0,0);
			uiLabelFinish.setBounds(0,0,0,0);
			switch(progress)
			{
			case WELCOME:
				uiLabelWelcome.setBounds(5,50,width-10,height-85);
				break;
			case DATABASE:
				uiCompDatabase.setBounds(5,50,width-10,height-85);
				uiCompDatabase.getLayout().layoutContainer(uiCompDatabase);
				break;
			case DATASTORE:
				uiCompDatastore.setBounds(5,50,width-10,height-85);
				uiCompDatastore.getLayout().layoutContainer(uiCompDatastore);
				break;
			case FINISH:
				uiLabelFinish.setBounds(5,50,width-10,height-85);
				break;
			}
		}
		@Override
		public void run()
		{
			
		}
		private void next() throws RuntimeException
		{
			switch(progress)
			{
			case WELCOME:
				progress = Step.DATABASE;
				uiButtonPrev.setEnabled(true);
				break;
			case DATABASE:
				progress = Step.DATASTORE;
				break;
			case DATASTORE:
				progress = Step.FINISH;
				uiButtonNext.setEnabled(false);
				uiButtonNext.setVisible(false);
				uiButtonFinish.setVisible(true);
				break;
			case FINISH:
				throw new RuntimeException("Already reached the last step.");
			}
			super.getLayout().layoutContainer(this);
		}
		private void back() throws RuntimeException
		{
			switch(progress)
			{
			case WELCOME:
				throw new RuntimeException("Already reached the first step.");
			case DATABASE:
				progress = Step.WELCOME;
				uiButtonPrev.setEnabled(false);
				break;
			case DATASTORE:
				progress = Step.DATABASE;
				break;
			case FINISH:
				progress = Step.DATASTORE;
				uiButtonNext.setEnabled(true);
				uiButtonNext.setVisible(true);
				uiButtonFinish.setVisible(false);
				break;
			}
			super.getLayout().layoutContainer(this);
		}
	}

	@Override
	public void propertyAdded(String prop) {}

	@Override
	public void propertyDeleted(String prop) {}

	@Override
	public void propertyUpdated(String prop)
	{
		uiPanelSettings.reload(prop);
	}
}