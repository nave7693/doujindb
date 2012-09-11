package org.dyndns.doujindb.ui.desk;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.desk.panels.*;

@SuppressWarnings("serial")
public final class DesktopEx extends JDesktopPane implements DataBaseListener
{
	private JLabel wallpaper;
	private ImageIcon wallpaperImage;
	private JButton buttonWallpaper;
	
	private JButton buttonRecycleBin;
	private JButton buttonTools;
	
	private Vector<JButton> buttonPlugins;
	
	public DesktopEx()
	{
		super();
		setOpaque(false);
		wallpaperImage = new ImageIcon(new File(System.getProperty("doujindb.home"),"doujindb.wallpaper").getAbsolutePath());
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
							javax.imageio.ImageIO.write(im, "PNG", new File(System.getProperty("doujindb.home"), "doujindb.wallpaper"));
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
					Core.UI.Desktop.openRecycleBin();
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				}
			}
		});
		super.add(buttonRecycleBin);
		buttonTools = new JButton(Core.Resources.Icons.get("JDesktop/Tools/Enabled"));
		buttonTools.setDisabledIcon(Core.Resources.Icons.get("JDesktop/Tools/Disabled"));
		buttonTools.setToolTipText("Tools");
		buttonTools.setFocusable(false);
		buttonTools.setContentAreaFilled(false);
		buttonTools.setBorder(null);
		buttonTools.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				try {
					Core.UI.Desktop.openTools();
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				}
			}
		});
		super.add(buttonTools);
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				wallpaper.setBounds(0,0,wallpaperImage.getIconWidth(),wallpaperImage.getIconHeight());
				setComponentZOrder(wallpaper,getComponentCount()-1);
				buttonRecycleBin.setBounds(5,5,32,32);
				buttonTools.setBounds(5+32,5,32,32);
				buttonRecycleBin.setEnabled(Core.Database.isConnected());
				buttonTools.setEnabled(Core.Database.isConnected());
				int spacing = 0;
				for(JButton plugin : buttonPlugins)
				{
					plugin.setBounds(5,5 + 40 + spacing,32,32);
					plugin.setEnabled(Core.Database.isConnected());
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
						openPlugin(plugin);
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}
			});
			super.add(buttonPlugin);
			buttonPlugins.add(buttonPlugin);
		}
		
		Core.Database.addDataBaseListener(this);
	}
	
	@Deprecated
	public Component add(Component comp)
	{
		if(!(comp instanceof WindowEx))
			return super.add(comp);
		else
			return super.add(comp);//throw new InvalidWindowStateException("Don't use Component.add(), use open() instead.");
	}
	
	public WindowEx openWindow(WindowEx.Type type, Record rcd) throws DataBaseException
	{
		if(checkWindow(type, rcd))
			return null;
		WindowEx window;
		switch(type)
		{
		case WINDOW_ARTIST:
			window = new WindowArtistImpl(rcd);
			break;
		case WINDOW_BOOK:
			window = new WindowBookImpl(rcd);
			break;
		case WINDOW_CIRCLE:
			window = new WindowCircleImpl(rcd);
			break;
		case WINDOW_CONTENT:
			window = new WindowContentImpl(rcd);
			break;
		case WINDOW_CONVENTION:
			window = new WindowConventionImpl(rcd);
			break;
		case WINDOW_PARODY:
			window = new WindowParodyImpl(rcd);
			break;
		default:
			throw new DataBaseException("'" + type + "' is not a valid Record type.");
		}
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
		DropTarget dt = new DropTarget(window , new DropTargetAdapter()
		{
			@Override
			public void dragOver(DropTargetDragEvent dtde)
			{
				DropTarget dt = (DropTarget)dtde.getSource();
				WindowEx window = ((WindowEx)dt.getComponent());
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
			}
			
			@Override
			public void drop(DropTargetDropEvent dtde) { }
			
		});
		window.setDropTarget(dt);
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
	
	public WindowEx openRecycleBin() throws DataBaseException
	{
		if(checkWindow(WindowEx.Type.WINDOW_RECYCLEBIN))
			return null;
		WindowEx window = new WindowRecycleBinImpl();
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
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
	
	public WindowEx openSearch() throws DataBaseException
	{
		if(checkWindow(WindowEx.Type.WINDOW_SEARCH))
			return null;
		WindowEx window = new WindowSearchImpl();
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
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
	
	public WindowEx openTools() throws DataBaseException
	{
		if(checkWindow(WindowEx.Type.WINDOW_TOOLS))
			return null;
		WindowEx window = new WindowToolsImpl();
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
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
	
	public WindowEx openPlugin(Plugin plug) throws DataBaseException
	{
		if(plug == null)
			throw new IllegalArgumentException("Argument 'Plugin' cannot be null.");
		if(checkWindow(WindowEx.Type.WINDOW_PLUGIN, plug))
			return null;
		WindowEx window = new WindowPluginImpl(plug);
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
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

	private boolean checkWindow(WindowEx.Type type)
	{
		for(JInternalFrame jif : getAllFrames())
		{
			WindowEx window = (WindowEx)jif;
			if(window.getType().equals(type))
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
	
	private boolean checkWindow(WindowEx.Type type, Object token)
	{
		for(JInternalFrame jif : getAllFrames())
		{
			WindowEx window = (WindowEx)jif;
			
			if(window instanceof WindowPluginImpl)
			{
				Plugin plug = ((WindowPluginImpl)window).plugin;
				if(plug.equals(token))
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
			else
			{
				Record rcd = null;
				if(window instanceof WindowArtistImpl)
					rcd = ((WindowArtistImpl)window).record;
				if(window instanceof WindowBookImpl)
					rcd = ((WindowBookImpl)window).record;
				if(window instanceof WindowCircleImpl)
					rcd = ((WindowCircleImpl)window).record;
				if(window instanceof WindowContentImpl)
					rcd = ((WindowContentImpl)window).record;
				if(window instanceof WindowConventionImpl)
					rcd = ((WindowConventionImpl)window).record;
				if(window instanceof WindowParodyImpl)
					rcd = ((WindowParodyImpl)window).record;
				
				if(token == null && rcd == null)
					if(window.getType().equals(type))
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
				if(token != null && rcd != null)
					if(rcd.equals(token))
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
		}
		return false;
	}
	
	public void showDesktop()
	{
		for(JInternalFrame jif : getAllFrames())
			try
			{	
				WindowEx window = (WindowEx)jif;
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
		final DialogEx window = new DialogEx(comp, icon, title);
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
	
	private void syncData()
	{
		try {
			if(Core.Database.getRecycled().size() > 0)
				buttonRecycleBin.setIcon(Core.Resources.Icons.get("JDesktop/RecycleBin/Full"));
			else
				buttonRecycleBin.setIcon(Core.Resources.Icons.get("JDesktop/RecycleBin/Empty"));
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
	}

	@Override
	public void recordAdded(Record rcd) { }

	@Override
	public void recordDeleted(Record rcd) { }

	@Override
	public void recordUpdated(Record rcd, UpdateData data) { }

	@Override
	public void databaseConnected()
	{
		syncData();
	}

	@Override
	public void databaseDisconnected()
	{
		for(JInternalFrame jif : getAllFrames())
		{
			try{ ((WindowEx)jif).dispose(); } catch(Exception e) { e.printStackTrace(); }
		}
	}

	@Override
	public void databaseCommit()
	{
		syncData();
	}

	@Override
	public void databaseRollback()
	{
		syncData();
	}
	
	@Override
	public void recordRecycled(Record rcd) { }

	@Override
	public void recordRestored(Record rcd) { }
	
	private final class WindowArtistImpl extends WindowEx
	{
		private Record record;
		
		WindowArtistImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_ARTIST;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
			if(this.record == null)
				super.setTitle("Add Artist");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_ARTIST, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowBookImpl extends WindowEx
	{
		private Record record;
		
		WindowBookImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_BOOK;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Book"));
			if(this.record == null)
				super.setTitle("Add Book");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_BOOK, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			/**
			 * BookImpl.toString() includes references to Artists/Circles
			 * we call Window.setTitle() for every type of update
			 */
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}

		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowCircleImpl extends WindowEx
	{
		private Record record;
		
		WindowCircleImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_CIRCLE;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
			if(this.record == null)
				super.setTitle("Add Circle");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_CIRCLE, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowContentImpl extends WindowEx
	{
		private Record record;
		
		WindowContentImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_CONTENT;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Content"));
			if(this.record == null)
				super.setTitle("Add Content");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_CONTENT, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowConventionImpl extends WindowEx
	{
		private Record record;
		
		WindowConventionImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_CONVENTION;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
			if(this.record == null)
				super.setTitle("Add Convention");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_CONVENTION, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowParodyImpl extends WindowEx
	{
		private Record record;
		
		WindowParodyImpl(Record rcd) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_PARODY;
				this.record = rcd;
			}
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
			if(this.record == null)
				super.setTitle("Add Parody");
			else
				super.setTitle(this.record.toString());
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_PARODY, this.record);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(record.equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!record.equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(this.record.toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!record.equals(rcd))
				return;
			super.recordRestored(rcd);
		}
	}
	
	private final class WindowSearchImpl extends WindowEx
	{
		WindowSearchImpl() throws DataBaseException
		{
			super();
			this.type = Type.WINDOW_SEARCH;
			super.setFrameIcon(Core.Resources.Icons.get("JFrame/Tab/Explorer/Search"));
			super.setTitle("Search");
			JTabbedPane pane = new JTabbedPane();
			pane.setFocusable(false);
			PanelSearch sp;
			sp = new PanelSearch(PanelSearch.Type.ARTIST, pane, 0);
			listeners.add(sp);
			pane.addTab("Artist", Core.Resources.Icons.get("JDesktop/Explorer/Artist"), sp);
			sp = new PanelSearch(PanelSearch.Type.BOOK, pane, 1);
			listeners.add(sp);
			pane.addTab("Book", Core.Resources.Icons.get("JDesktop/Explorer/Book"), sp);
			sp = new PanelSearch(PanelSearch.Type.CIRCLE, pane, 2);
			listeners.add(sp);
			pane.addTab("Circle", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), sp);
			sp = new PanelSearch(PanelSearch.Type.CONVENTION, pane, 3);
			listeners.add(sp);
			pane.addTab("Convention", Core.Resources.Icons.get("JDesktop/Explorer/Convention"), sp);
			sp = new PanelSearch(PanelSearch.Type.CONTENT, pane, 4);
			listeners.add(sp);
			pane.addTab("Content", Core.Resources.Icons.get("JDesktop/Explorer/Content"), sp);
			sp = new PanelSearch(PanelSearch.Type.PARODY, pane, 5);
			listeners.add(sp);
			pane.addTab("Parody", Core.Resources.Icons.get("JDesktop/Explorer/Parody"), sp);
			super.add(pane);
			super.setVisible(true);
		}
	}
	
	private final class WindowRecycleBinImpl extends WindowEx
	{
		WindowRecycleBinImpl() throws DataBaseException
		{
			super();
			this.type = Type.WINDOW_RECYCLEBIN;
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/RecycleBin"));
			super.setTitle("Recycle Bin");
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_RECYCLEBIN, null);
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
	}
	
	private final class WindowToolsImpl extends WindowEx
	{
		WindowToolsImpl() throws DataBaseException
		{
			super();
			this.type = Type.WINDOW_TOOLS;
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Tools"));
			super.setTitle("Tools");
			PanelEditor editor = new PanelEditor(this, Type.WINDOW_TOOLS, null);
			//TODO org.dyndns.doujindb.util.RepositoryIndexer.rebuildIndexes();
			listeners.add(editor);
			super.add(editor);
			super.setVisible(true);
		}
	}

	private final class WindowPluginImpl extends WindowEx
	{
		private Plugin plugin;
		
		WindowPluginImpl(Plugin plugin) throws DataBaseException
		{
			super();
			{
				this.type = Type.WINDOW_PLUGIN;
				this.plugin = plugin;
			}			
			super.setFrameIcon(Core.Resources.Icons.get("JFrame/Tab/Plugins"));
			super.setTitle(plugin.getName());
			/**
			 * Interface Plugin does not extends/implement DatabaseListener.
			 * A plugin may not be related to DB operations
			 * Maybe it's better if a plugin register itself for getting DB events
			 */
			// listeners.add(plugin);
			super.add(plugin.getUI());
			super.setVisible(true);
		}
	}
}