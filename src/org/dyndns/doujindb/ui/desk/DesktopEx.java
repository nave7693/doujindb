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
import org.dyndns.doujindb.db.records.*;
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
				new Thread(getClass().getName()+"$ActionPerformed$SetWallpaper")
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
					Core.UI.Desktop.showRecycleBinWindow();
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
					Core.UI.Desktop.showToolsWindow();
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
		for(Plugin plug : PluginManager.plugins())
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
						showPluginWindow(plugin);
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
	
	public WindowEx showRecordWindow(WindowEx.Type type, Record rcd) throws DataBaseException
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
	
	public WindowEx showRecycleBinWindow() throws DataBaseException
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
	
	public WindowEx showSearchWindow() throws DataBaseException
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
	
	public WindowEx showToolsWindow() throws DataBaseException
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
	
	public WindowEx showPluginWindow(Plugin plug) throws DataBaseException
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
					rcd = ((WindowArtistImpl)window).getRecord();
				if(window instanceof WindowBookImpl)
					rcd = ((WindowBookImpl)window).getRecord();
				if(window instanceof WindowCircleImpl)
					rcd = ((WindowCircleImpl)window).getRecord();
				if(window instanceof WindowContentImpl)
					rcd = ((WindowContentImpl)window).getRecord();
				if(window instanceof WindowConventionImpl)
					rcd = ((WindowConventionImpl)window).getRecord();
				if(window instanceof WindowParodyImpl)
					rcd = ((WindowParodyImpl)window).getRecord();
				
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
	
	public void showDialog(RootPaneContainer parent, JComponent dialog, Icon icon, String title) throws PropertyVetoException
	{
		final JComponent glassPane = (JComponent) parent.getGlassPane();
		if(glassPane.isVisible())
			throw new PropertyVetoException("Dialog already open.", null);
		final DialogEx window = new DialogEx(dialog, icon, title);
		window.pack();
		window.setMaximizable(false);
		window.setIconifiable(false);
		window.setResizable(false);
		window.setClosable(false);
		Rectangle bounds = glassPane.getBounds();
		Rectangle rect = window.getBounds();
		int x = (bounds.width - rect.width) / 2;
		int y = (bounds.height - rect.height) / 2;
		window.setLocation(x, y);
		glassPane.add(window);
		glassPane.setEnabled(true);
		glassPane.setVisible(true);
		glassPane.setEnabled(false);
		window.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent ife)
			{
				glassPane.add(ife.getInternalFrame());
				glassPane.setEnabled(true);
				glassPane.setVisible(false);
				glassPane.setEnabled(false);
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent ife)
			{
				glassPane.add(ife.getInternalFrame());
				glassPane.setEnabled(true);
				glassPane.setVisible(false);
				glassPane.setEnabled(false);
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
		private PanelArtist panel;
		
		WindowArtistImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
			this.type = Type.WINDOW_ARTIST;
			if(rcd == null)
			{
				rcd = new NullArtist();
				super.setTitle("Add Artist");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelArtist((Artist)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Artist getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullArtist implements Artist
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Book> getBooks() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addBook(Book book) throws DataBaseException { }

			@Override
			public void removeBook(Book book) throws DataBaseException { }

			@Override
			public String getJapaneseName() throws DataBaseException { return ""; }

			@Override
			public String getTranslatedName() throws DataBaseException { return ""; }

			@Override
			public String getRomajiName() throws DataBaseException { return ""; }

			@Override
			public String getWeblink() throws DataBaseException { return ""; }

			@Override
			public void setJapaneseName(String japaneseName) throws DataBaseException { }

			@Override
			public void setTranslatedName(String translatedName) throws DataBaseException { }

			@Override
			public void setRomajiName(String romajiName) throws DataBaseException { }

			@Override
			public void setWeblink(String weblink) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Circle> getCircles() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addCircle(Circle circle) throws DataBaseException { }

			@Override
			public void removeCircle(Circle circle) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
		}
	}
	
	private final class WindowBookImpl extends WindowEx
	{
		private PanelBook panel;
		
		WindowBookImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Book"));
			this.type = Type.WINDOW_BOOK;
			if(rcd == null)
			{
				rcd = new NullBook();
				super.setTitle("Add Book");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelBook((Book)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Book getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			/**
			 * BookImpl.toString() includes references to Artists/Circles
			 * we call Window.setTitle() for every type of update
			 */
			super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}

		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullBook implements Book
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@Override
			public String getJapaneseName() throws DataBaseException { return ""; }

			@Override
			public String getTranslatedName() throws DataBaseException { return ""; }

			@Override
			public String getRomajiName() throws DataBaseException { return ""; }

			@Override
			public void setJapaneseName(String japaneseName) throws DataBaseException { }

			@Override
			public void setTranslatedName(String translatedName) throws DataBaseException { }

			@Override
			public void setRomajiName(String romajiName) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Circle> getCircles() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public Date getDate() throws DataBaseException { return new Date(); }

			@Override
			public Type getType() throws DataBaseException { return Type.不詳; }

			@Override
			public int getPages() throws DataBaseException { return 0; }

			@Override
			public void setPages(int pages) throws DataBaseException { }

			@Override
			public void setDate(Date date) throws DataBaseException { }

			@Override
			public void setType(Type type) throws DataBaseException { }

			@Override
			public boolean isAdult() throws DataBaseException { return false; }

			@Override
			public boolean isDecensored() throws DataBaseException { return false; }

			@Override
			public boolean isTranslated() throws DataBaseException { return false; }

			@Override
			public boolean isColored() throws DataBaseException { return false; }

			@Override
			public void setAdult(boolean adult) throws DataBaseException { }

			@Override
			public void setDecensored(boolean decensored) throws DataBaseException { }

			@Override
			public void setTranslated(boolean translated) throws DataBaseException { }

			@Override
			public void setColored(boolean colored) throws DataBaseException { }

			@Override
			public Rating getRating() throws DataBaseException { return Rating.UNRATED; }

			@Override
			public String getInfo() throws DataBaseException { return ""; }

			@Override
			public void setRating(Rating rating) throws DataBaseException { }

			@Override
			public void setInfo(String info) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Artist> getArtists() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Content> getContents() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public Convention getConvention() throws DataBaseException { return null; }

			@Override
			public void setConvention(Convention convention) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Parody> getParodies() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addArtist(Artist artist) throws DataBaseException { }

			@Override
			public void addContent(Content content) throws DataBaseException { }

			@Override
			public void addParody(Parody parody) throws DataBaseException { }

			@Override
			public void removeArtist(Artist artist) throws DataBaseException { }

			@Override
			public void removeContent(Content content) throws DataBaseException { }

			@Override
			public void removeParody(Parody parody) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
		}
	}
	
	private final class WindowCircleImpl extends WindowEx
	{
		private PanelCircle panel;
		
		WindowCircleImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
			this.type = Type.WINDOW_CIRCLE;
			if(rcd == null)
			{
				rcd = new NullCircle();
				super.setTitle("Add Circle");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelCircle((Circle)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Circle getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullCircle implements Circle
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Book> getBooks() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public String getJapaneseName() throws DataBaseException { return ""; }

			@Override
			public String getTranslatedName() throws DataBaseException { return ""; }

			@Override
			public String getRomajiName() throws DataBaseException { return ""; }

			@Override
			public String getWeblink() throws DataBaseException { return ""; }

			@Override
			public void setJapaneseName(String japaneseName) throws DataBaseException { }

			@Override
			public void setTranslatedName(String translatedName) throws DataBaseException { }

			@Override
			public void setRomajiName(String romajiName) throws DataBaseException { }

			@Override
			public void setWeblink(String weblink) throws DataBaseException { }
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Artist> getArtists() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addArtist(Artist artist) throws DataBaseException { }

			@Override
			public void removeArtist(Artist artist) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
		}
	}
	
	private final class WindowContentImpl extends WindowEx
	{
		private PanelContent panel;
		
		WindowContentImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Content"));
			this.type = Type.WINDOW_CONTENT;
			if(rcd == null)
			{
				rcd = new NullContent();
				super.setTitle("Add Content");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelContent((Content)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Content getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullContent implements Content
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@Override
			public String getTagName() throws DataBaseException { return ""; }

			@Override
			public String getInfo() throws DataBaseException { return ""; }

			@Override
			public void setTagName(String tagName) throws DataBaseException { }

			@Override
			public void setInfo(String info) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Book> getBooks() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addBook(Book book) throws DataBaseException { }

			@Override
			public void removeBook(Book book) throws DataBaseException { }

			@Override
			public Set<String> getAliases() throws DataBaseException { return new java.util.TreeSet<String>(); }

			@Override
			public void addAlias(String alias) throws DataBaseException { }

			@Override
			public void removeAlias(String alias) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
		}
	}
	
	private final class WindowConventionImpl extends WindowEx
	{
		private PanelConvention panel;
		
		WindowConventionImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
			this.type = Type.WINDOW_CONVENTION;
			if(rcd == null)
			{
				rcd = new NullConvention();
				super.setTitle("Add Convention");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelConvention((Convention)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Convention getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullConvention implements Convention
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@Override
			public String getTagName() throws DataBaseException { return ""; }

			@Override
			public String getInfo() throws DataBaseException { return ""; }

			@Override
			public void setTagName(String tagName) throws DataBaseException { }

			@Override
			public void setInfo(String info) throws DataBaseException { }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Book> getBooks() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addBook(Book book) throws DataBaseException { }

			@Override
			public void removeBook(Book book) throws DataBaseException { }

			@Override
			public String getWeblink() throws DataBaseException { return ""; }

			@Override
			public void setWeblink(String weblink) throws DataBaseException { }

			@Override
			public Set<String> getAliases() throws DataBaseException { return new java.util.TreeSet<String>(); }

			@Override
			public void addAlias(String alias) throws DataBaseException { }

			@Override
			public void removeAlias(String alias) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
		}
	}
	
	private final class WindowParodyImpl extends WindowEx
	{
		private PanelParody panel;
		
		WindowParodyImpl(Record rcd) throws DataBaseException
		{
			super();
			super.setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
			this.type = Type.WINDOW_PARODY;
			if(rcd == null)
			{
				rcd = new NullParody();
				super.setTitle("Add Parody");
			}
			else
				super.setTitle(rcd.toString());
			panel = new PanelParody((Parody)rcd);
			listeners.add(panel);
			super.add(panel);
			super.setVisible(true);
		}
		
		public Parody getRecord()
		{
			return panel.getRecord();
		}
		
		@Override
		public void recordRecycled(Record rcd)
		{
			if(getRecord().equals(rcd))
			{
				super.dispose();
				Core.UI.Desktop.remove(this);
			}
		}

		@Override
		public void recordUpdated(Record rcd, UpdateData data)
		{
			if(!getRecord().equals(rcd))
				return;
			if(data.getType() == UpdateData.Type.PROPERTY)
				super.setTitle(getRecord().toString());
			super.recordUpdated(rcd, data);
		}
		
		@Override
		public void recordAdded(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordAdded(rcd);
		}

		@Override
		public void recordDeleted(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordDeleted(rcd);
		}

		@Override
		public void recordRestored(Record rcd)
		{
			if(!getRecord().equals(rcd))
				return;
			super.recordRestored(rcd);
		}
		
		private final class NullParody implements Parody
		{
			@Override
			public String getID() throws DataBaseException { return null; }

			@Override
			public void doRecycle() throws DataBaseException { }

			@Override
			public void doRestore() throws DataBaseException { }

			@Override
			public boolean isRecycled() throws DataBaseException { return false; }

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public RecordSet<Book> getBooks() throws DataBaseException
			{
				return new RecordSet()
				{

					@Override
					public Iterator iterator() { return new java.util.ArrayList().iterator(); }

					@Override
					public boolean contains(Object o) throws DataBaseException { return false; }

					@Override
					public int size() throws DataBaseException { return 0; }
					
				};
			}

			@Override
			public void addBook(Book book) throws DataBaseException { }

			@Override
			public void removeBook(Book book) throws DataBaseException { }

			@Override
			public String getJapaneseName() throws DataBaseException { return ""; }

			@Override
			public String getTranslatedName() throws DataBaseException { return ""; }

			@Override
			public String getRomajiName() throws DataBaseException { return ""; }

			@Override
			public String getWeblink() throws DataBaseException { return ""; }

			@Override
			public void setJapaneseName(String japaneseName) throws DataBaseException { }

			@Override
			public void setTranslatedName(String translatedName) throws DataBaseException { }

			@Override
			public void setRomajiName(String romajiName) throws DataBaseException { }

			@Override
			public void setWeblink(String weblink) throws DataBaseException { }

			@Override
			public void removeAll() throws DataBaseException { }
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
			PanelRecycleBin panel = new PanelRecycleBin();
			listeners.add(panel);
			super.add(panel);
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
			//TODO org.dyndns.doujindb.util.RepositoryIndexer.rebuildIndexes();
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