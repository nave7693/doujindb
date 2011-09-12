package org.dyndns.doujindb.ui.desk;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.desk.events.DouzEvent;
import org.dyndns.doujindb.ui.desk.events.Validable;



@SuppressWarnings("serial")
public final class DouzDesktop extends JDesktopPane implements Validable
{
	private JLabel wallpaper;
	private ImageIcon wallpaperImage;
	private JButton buttonWallpaper;
	
	private JButton buttonRecycleBin;
	private JButton buttonMediaManager;
	
	private Vector<JButton> buttonPlugins;
	
	public DouzDesktop()
	{
		super();
		setOpaque(false);
		wallpaperImage = new ImageIcon(new File(new File(System.getProperty("user.home"), ".doujindb"),"doujindb.wallpaper").getAbsolutePath());
		wallpaper = new JLabel(wallpaperImage);
		buttonWallpaper = new JButton(Core.Resources.Icons.get("JDesktop/Wallpaper/Import"));
		buttonWallpaper.setFocusable(false);
		buttonWallpaper.setOpaque(false);
		buttonWallpaper.setBackground(new Color(255,255,255,0));
		buttonWallpaper.setBorder(null);
		buttonWallpaper.setToolTipText("Select Wallpaper");
		buttonWallpaper.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new Thread(getClass().getName()+"/ActionPerformed/SetWallpaper")
				{
					@Override
					public void run()
					{
						super.setPriority(Thread.MIN_PRIORITY);
						try
						{
							JFileChooser fc = Core.UI.getFileChooser();
							fc.setMultiSelectionEnabled(false);
							if(fc.showOpenDialog(Core.UI) != JFileChooser.APPROVE_OPTION)
								return;
							File file = fc.getSelectedFile();
							wallpaperImage = new ImageIcon(file.getAbsolutePath());
							Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
							BufferedImage im = new BufferedImage((int)screenSize.getWidth(), (int)screenSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
							Graphics2D graphics2D = im.createGraphics();
							graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
							graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							graphics2D.drawImage(wallpaperImage.getImage(), 0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight(), null);
							javax.imageio.ImageIO.write(im, "PNG", new File(new File(System.getProperty("user.home"), ".doujindb"),"doujindb.wallpaper"));
							wallpaperImage = new ImageIcon(im);
							wallpaper.setIcon(wallpaperImage);
						} catch (Exception e) {
							e.printStackTrace();
							Core.Logger.log(e.getMessage(), Level.WARNING);
						}
					}
				}.start();
			}			
		});
		super.add(buttonWallpaper);
		buttonRecycleBin = new JButton(Core.Resources.Icons.get("JDesktop/RecycleBin/Empty"));
		buttonRecycleBin.setDisabledIcon(Core.Resources.Icons.get("JDesktop/RecycleBin/Disabled"));
		buttonRecycleBin.setToolTipText("Recycle Bin");
		buttonRecycleBin.setFocusable(false);
		buttonRecycleBin.setContentAreaFilled(false);
		buttonRecycleBin.setBorder(null);
		buttonRecycleBin.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				try {
					Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_RECYCLEBIN, null);
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				} catch (RemoteException re) {
					Core.Logger.log(re.getMessage(), Level.ERROR);
					re.printStackTrace();
				}
			}
		});
		super.add(buttonRecycleBin);
		buttonMediaManager = new JButton(Core.Resources.Icons.get("JDesktop/MediaManager/Enabled"));
		buttonMediaManager.setDisabledIcon(Core.Resources.Icons.get("JDesktop/MediaManager/Disabled"));
		buttonMediaManager.setToolTipText("Media files");
		buttonMediaManager.setFocusable(false);
		buttonMediaManager.setContentAreaFilled(false);
		buttonMediaManager.setBorder(null);
		buttonMediaManager.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				try {
					Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_MEDIAMANAGER, null);
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				} catch (RemoteException re) {
					Core.Logger.log(re.getMessage(), Level.ERROR);
					re.printStackTrace();
				}
			}
		});
		super.add(buttonMediaManager);
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				wallpaper.setBounds(0,0,wallpaperImage.getIconWidth(),wallpaperImage.getIconHeight());
				setComponentZOrder(wallpaper,getComponentCount()-1);
				buttonRecycleBin.setBounds(5,5,32,32);
				buttonMediaManager.setBounds(5 + 40,5,32,32);
				buttonRecycleBin.setEnabled(Client.isConnected());
				buttonMediaManager.setEnabled(Client.isConnected());
				int spacing = 0;
				for(JButton plugin : buttonPlugins)
				{
					plugin.setBounds(5 + spacing,5 + 40,32,32);
					plugin.setEnabled(Client.isConnected());
					spacing += 40;
				}					
				buttonWallpaper.setBounds(width-20,1,20,20);
			}
			@Override
			public void addLayoutComponent(String key,Component c){}
			@Override
			public void removeLayoutComponent(Component c){}
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
		});
		super.add(wallpaper);
		
		buttonPlugins = new Vector<JButton>();
		for(Plugin plug : Core.Plugins)
		{
			JButton buttonPlugin;
			final Plugin plugin = plug;
			buttonPlugin = new JButton(plug.getIcon());
			buttonPlugin.setToolTipText("<html><body><b>" + plug.getName() + "</b><br>" +
					"<b>Author</b> : " + plug.getAuthor() + "<br>" + 
					"<b>Version</b> : " + plug.getVersion() + "<br>" + 
					"<b>Weblink</b> : " + plug.getWeblink() + 
					"</body></html>");
			buttonPlugin.setFocusable(false);
			buttonPlugin.setContentAreaFilled(false);
			buttonPlugin.setBorder(null);
			buttonPlugin.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					try {
						openWindow(DouzWindow.Type.WINDOW_PLUGIN, plugin);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					} catch (RemoteException re) {
						Core.Logger.log(re.getMessage(), Level.ERROR);
						re.printStackTrace();
					}
				}
			});
			super.add(buttonPlugin);
			buttonPlugins.add(buttonPlugin);
		}
	}
	
	@Deprecated
	public Component add(Component comp)
	{
		if(!(comp instanceof DouzWindow))
			return super.add(comp);
		else
			return super.add(comp);//throw new InvalidWindowStateException("Don't use Component.add(), use open() instead.");
	}
	
	public DouzWindow openWindow(DouzWindow.Type type, Object param) throws DataBaseException, RemoteException
	{
		if(checkWindow(type,param))
			return null;
		DouzWindow window = new DouzWindow(type, param);
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400,350));
		super.add(window);
		try
		{
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve)
		{
			pve.printStackTrace();
			Core.Logger.log(pve.getMessage(), Level.WARNING);
		}
		return window;
	}
	public DouzWindow openWindow(DouzWindow.Type type, Object param, Rectangle bounds) throws DataBaseException, RemoteException
	{
		if(checkWindow(type,param))
			return null;
		DouzWindow window = new DouzWindow(type, param);
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400,350));
		window.setBounds(bounds);
		super.add(window);
		try
		{
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve)
		{
			pve.printStackTrace();
			Core.Logger.log(pve.getMessage(), Level.WARNING);
		}
		return window;
	}
	public DouzWindow openWindow(DouzWindow.Type type, Object param, Icon icon, String title) throws DataBaseException, RemoteException
	{
		if(checkWindow(type,param))
			return null;
		DouzWindow window = new DouzWindow(type, param);
		window.setFrameIcon(icon);
		window.setTitle(title);
		try
		{
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve)
		{
			pve.printStackTrace();
			Core.Logger.log(pve.getMessage(), Level.WARNING);
		}
		return window;
	}
	private boolean checkWindow(DouzWindow.Type type, Object token)
	{
		for(JInternalFrame jif : getAllFrames())
		{
			DouzWindow window = (DouzWindow)jif;
			if(token == null && window.getItem() == null)
				if(window.getType() == type)
				{
					try
					{
						window.setVisible(true);
						window.setSelected(true);
						window.setIcon(false);
					} catch (PropertyVetoException pve)
					{
						pve.printStackTrace();
						Core.Logger.log(pve.getMessage(), Level.WARNING);
					}
					return true;
				}
			if(token != null && window.getItem() != null)
				if(window.getItem() == token)
				{
					try
					{
						window.setVisible(true);
						window.setSelected(true);
						window.setIcon(false);
					} catch (PropertyVetoException pve)
					{
						pve.printStackTrace();
						Core.Logger.log(pve.getMessage(), Level.WARNING);
					}
					return true;
				}
		}
		return false;
	}
	
	public void showDesktop()
	{
		for(JInternalFrame jif : getAllFrames())
			try
			{	
				DouzWindow window = (DouzWindow)jif;
				window.setIcon(true);
			}
			catch (PropertyVetoException pve) { ; }
			catch (ClassCastException cce) { ; }
	}
	
	public void showDialog(JComponent comp, Icon icon, String title) throws PropertyVetoException
	{
		JComponent glass = Core.UI.getGlassPane();
		if(glass.isVisible())
			throw new PropertyVetoException("Dialog already open.", null);
		final DouzDialog window = new DouzDialog(comp, icon, title);
		window.pack();
		window.setMaximizable(false);
		window.setIconifiable(false);
		window.setResizable(false);
		window.setClosable(false);
		Rectangle bounds = Core.UI.getBounds();
		Rectangle rect = window.getBounds();
		int x = (bounds.width - rect.width) / 2;
		int y = (bounds.height - rect.height) / 2;
		window.setLocation(x, y);
		glass.add(window);
		glass.setEnabled(true);
		glass.setVisible(true);
		glass.setEnabled(false);
		window.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent ife)
			{
				JComponent glass = Core.UI.getGlassPane();
				glass.add(ife.getInternalFrame());
				glass.setEnabled(true);
				glass.setVisible(false);
				glass.setEnabled(false);
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent ife)
			{
				JComponent glass = Core.UI.getGlassPane();
				glass.add(ife.getInternalFrame());
				glass.setEnabled(true);
				glass.setVisible(false);
				glass.setEnabled(false);
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try { window.setSelected(true); } catch (PropertyVetoException pve)
            	{
        			pve.printStackTrace();
        			Core.Logger.log(pve.getMessage(), Level.WARNING);
        		}
            }
        });
	}

	@Override
	public void validateUI(DouzEvent ve)
	{
		if(!Client.isConnected())
			return;
		// TODO
		/*switch(Core.Network.getStatus())
		{
		case CONNECTED:
			buttonSharedItems.setIcon(Core.Resources.Icons.get("JDesktop/SharedItems/Connected"));
			break;
		case DISCONNECTED:
			buttonSharedItems.setIcon(Core.Resources.Icons.get("JDesktop/SharedItems/Disconnected"));
			break;
		case CONNECTING:
			buttonSharedItems.setIcon(Core.Resources.Icons.get("JDesktop/SharedItems/Connecting"));
			break;
		case DISCONNECTING:
			buttonSharedItems.setIcon(Core.Resources.Icons.get("JDesktop/SharedItems/Disconnecting"));
			break;
		}*/
		for(JInternalFrame jif : getAllFrames())
		{
			try{ ((DouzWindow)jif).validateUI(ve); }catch(Exception e) { e.printStackTrace(); }
		}
		try {
			if(Client.DB.getRecycled().size() > 0)
				buttonRecycleBin.setIcon(Core.Resources.Icons.get("JDesktop/RecycleBin/Full"));
			else
				buttonRecycleBin.setIcon(Core.Resources.Icons.get("JDesktop/RecycleBin/Empty"));
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
		super.validate();
	}
}