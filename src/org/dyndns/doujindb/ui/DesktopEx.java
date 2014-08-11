package org.dyndns.doujindb.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.plug.*;
import org.dyndns.doujindb.ui.dialog.*;
import org.dyndns.doujindb.util.ImageTool;

import static org.dyndns.doujindb.ui.UI.Icon;
import static org.dyndns.doujindb.ui.UI.ModalLayer;

@SuppressWarnings("serial")
public final class DesktopEx extends JDesktopPane implements DataBaseListener
{
	private JLabel wallpaper;
	private ImageIcon wallpaperImage;
	private JButton buttonWallpaper;
	
	private JButton m_ButtonTrash;
	
	private Vector<JButton> buttonPlugins;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DesktopEx.class);
	
	public DesktopEx()
	{
		super();
		setOpaque(false);
		wallpaperImage = new ImageIcon(new File(Core.DOUJINDB_HOME,"doujindb.wallpaper").getAbsolutePath());
		wallpaper = new JLabel(wallpaperImage);
		buttonWallpaper = new JButton(Icon.desktop_wallpaper_import);
		buttonWallpaper.setFocusable(false);
		buttonWallpaper.setOpaque(false);
		buttonWallpaper.setBackground(new Color(255,255,255,0));
		buttonWallpaper.setBorder(null);
		buttonWallpaper.setToolTipText("Select Wallpaper");
		buttonWallpaper.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				new SwingWorker<Void, Object>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						try
						{
							JFileChooser fc = UI.FileChooser;
							fc.setMultiSelectionEnabled(false);
							fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
							if(fc.showOpenDialog(DesktopEx.this) != JFileChooser.APPROVE_OPTION)
								return null;
							File file = fc.getSelectedFile();
							wallpaperImage = new ImageIcon(file.getAbsolutePath());
							Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
							BufferedImage im = new BufferedImage((int)screenSize.getWidth(), (int)screenSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
							Graphics2D graphics2D = im.createGraphics();
							graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
							graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							graphics2D.drawImage(wallpaperImage.getImage(), 0, 0, (int)screenSize.getWidth(), (int)screenSize.getHeight(), null);
							ImageTool.write(im, new File(Core.DOUJINDB_HOME, "doujindb.wallpaper"));
							wallpaperImage = new ImageIcon(im);
							wallpaper.setIcon(wallpaperImage);
						} catch (IOException e) {
							e.printStackTrace();
							LOG.error("Error updating wallpaper image", e);
						}
						return null;
					}
				}.execute();
			}			
		});
		super.add(buttonWallpaper);
		m_ButtonTrash = new JButton(Icon.desktop_trash_empty);
		m_ButtonTrash.setDisabledIcon(Icon.desktop_trash_disabled);
		m_ButtonTrash.setToolTipText("Trash");
		m_ButtonTrash.setFocusable(false);
		m_ButtonTrash.setEnabled(false);
		m_ButtonTrash.setContentAreaFilled(false);
		m_ButtonTrash.setBorder(null);
		m_ButtonTrash.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				try {
					showTrashWindow();
				} catch (DataBaseException dbe) {
					LOG.error("Error displaying Trash Window", dbe);
					dbe.printStackTrace();
				}
			}
		});
		super.add(m_ButtonTrash);
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth();
				wallpaper.setBounds(0,0,wallpaperImage.getIconWidth(),wallpaperImage.getIconHeight());
				setComponentZOrder(wallpaper,getComponentCount()-1);
				m_ButtonTrash.setBounds(5,5,32,32);
				int spacing = 0;
				for(JButton plugin : buttonPlugins)
				{
					plugin.setBounds(5,5 + 40 + spacing,32,32);
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
		for(Plugin plug : PluginManager.listAll())
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
			buttonPlugin.setEnabled(false);
			buttonPlugin.setContentAreaFilled(false);
			buttonPlugin.setBorder(null);
			buttonPlugin.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae)
				{
					try {
						new SwingWorker<Void, Object>()
						{
							@Override
							protected Void doInBackground() throws Exception
							{
								showPluginWindow(plugin);
								return null;
							}
						}.execute();
					} catch (DataBaseException dbe) {
						LOG.error("Error displaying Plugin Window", dbe);
						dbe.printStackTrace();
					}
				}
			});
			super.add(buttonPlugin);
			buttonPlugins.add(buttonPlugin);
		}
		
		DataBase.addDataBaseListener(this);
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
				try {
					window.setVisible(true);
					window.setSelected(true);
					window.setIcon(false);
				} catch (PropertyVetoException pve) {
					LOG.error("Error displaying Window [{}]", window, pve);
				}
			}
			
			@Override
			public void drop(DropTargetDropEvent dtde) { }
			
		});
		window.setDropTarget(dt);
		super.add(window);
		try {
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve) {
			LOG.error("Error displaying Window [{}]", window, pve);
		}
		return window;
	}
	
	public WindowEx showTrashWindow() throws DataBaseException
	{
		if(checkWindow(WindowEx.Type.WINDOW_TRASH))
			return null;
		WindowEx window = new WindowTrashImpl();
		window.setBounds(0,0,450,450);
		window.setMinimumSize(new Dimension(400, 350));
		super.add(window);
		try {
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve) {
			LOG.error("Error displaying Window [{}]", window, pve);
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
		try {
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve) {
			LOG.error("Error displaying Window [{}]", window, pve);
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
		try {
			window.setVisible(true);
			window.setSelected(true);
		} catch (PropertyVetoException pve) {
			LOG.error("Error displaying Window [{}]", window, pve);
		}
		return window;
	}

	private boolean checkWindow(WindowEx.Type type)
	{
		for(JInternalFrame jif : getAllFrames())
		{
			WindowEx window = (WindowEx)jif;
			if(window.getType().equals(type)) {
				try {
					window.setVisible(true);
					window.setSelected(true);
					window.setIcon(false);
				} catch (PropertyVetoException pve) {
					LOG.error("Error displaying Window [{}]", window, pve);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean checkWindow(WindowEx.Type type, Object token)
	{
		for(JInternalFrame jif : getAllFrames()) {
			WindowEx window = (WindowEx)jif;
			
			if(window instanceof WindowPluginImpl) {
				Plugin plug = ((WindowPluginImpl)window).plugin;
				if(plug.equals(token)) {
					try {
						window.setVisible(true);
						window.setSelected(true);
						window.setIcon(false);
					} catch (PropertyVetoException pve) {
						LOG.error("Error displaying Window [{}]", window, pve);
					}
					return true;
				}
			} else {
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
				
				if(token == null && rcd == null) {
					if(window.getType().equals(type)) {
						try {
							window.setVisible(true);
							window.setSelected(true);
							window.setIcon(false);
						} catch (PropertyVetoException pve) {
							LOG.error("Error displaying Window [{}]", window, pve);
						}
						return true;
					}
				}
				if(token != null && rcd != null) {
					if(rcd.equals(token)) {
						try {
							window.setVisible(true);
							window.setSelected(true);
							window.setIcon(false);
						} catch (PropertyVetoException pve) {
							LOG.error("Error displaying Window [{}]", window, pve);
						}
						return true;
					}
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
			} catch (PropertyVetoException pve) { }
				catch (ClassCastException cce) { }
	}
	
	public void showDialog(WindowEx parent, final DialogEx dialog) throws PropertyVetoException
	{
		final JComponent modalLayer = parent.ModalLayer;
		// check if other modal dialogs are already open
		if(modalLayer.isEnabled())
			throw new PropertyVetoException("Another DialogEx is already open.", null);
		// add dialog to modal layer and enable it
		modalLayer.add(dialog);
		modalLayer.setEnabled(true);
		// install dialog dispose()/close() hooks
		dialog.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent ife)
			{
				modalLayer.setEnabled(false);
				modalLayer.setVisible(false);
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent ife)
			{
				modalLayer.setEnabled(false);
				modalLayer.setVisible(false);
			}
		});
		// display dialog
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try {
            		modalLayer.setVisible(true);
            		getRootPane().repaint();
            		dialog.setVisible(true);
            		dialog.setSelected(true);
            	} catch (PropertyVetoException pve) {
            		LOG.error("Error displaying Dialog [{}]", dialog, pve);
        		}
            }
        });
	}
	
	public void showDialog(final DialogEx dialog) throws PropertyVetoException
	{
		// check if other modal dialogs are already open
		if(ModalLayer.isEnabled())
			throw new PropertyVetoException("Another DialogEx is already open.", null);
		// add dialog to modal layer and enable it
		ModalLayer.add(dialog);
		ModalLayer.setEnabled(true);
		// install dialog dispose()/close() hooks
		dialog.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent ife)
			{
				ModalLayer.setEnabled(false);
				ModalLayer.setVisible(false);
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent ife)
			{
				ModalLayer.setEnabled(false);
				ModalLayer.setVisible(false);
			}
		});
		// display dialog
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try {
            		ModalLayer.setVisible(true);
            		getRootPane().repaint();
            		dialog.setVisible(true);
            		dialog.setSelected(true);
            	} catch (PropertyVetoException pve) {
            		LOG.error("Error displaying Dialog [{}]", dialog, pve);
        		}
            }
        });
	}
	
	private void loadData()
	{
		new SwingWorker<Void, Object>()
		{
			private boolean isEmpty = true;
			private String toolTip = "";
			@Override
			public Void doInBackground()
			{
				try {
					RecordSet<Record> recycled = DataBase.getRecycled();
					long countArtist = 0;
					long countBook = 0;
					long countCircle = 0;
					long countConvention = 0;
					long countContent = 0;
					long countParody = 0;
					
					isEmpty = recycled.size() < 1;
					for(Record r : recycled) {
						if(r instanceof Artist) {
							countArtist++;
							continue;
						}
						if(r instanceof Book) {
							countBook++;
							continue;
						}
						if(r instanceof Circle) {
							countCircle++;
							continue;
						}
						if(r instanceof Convention) {
							countConvention++;
							continue;
						}
						if(r instanceof Content) {
							countContent++;
							continue;
						}
						if(r instanceof Parody) {
							countParody++;
							continue;
						}
					}
					toolTip = "<html><body>";
					if(countArtist > 0)
						toolTip += "Artist : " + countArtist + "<br/>";
					if(countBook> 0)
						toolTip += "Book : " + countBook + "<br/>";
					if(countCircle > 0)
						toolTip += "Circle : " + countCircle + "<br/>";
					if(countConvention > 0)
						toolTip += "Convention : " + countConvention + "<br/>";
					if(countContent > 0)
						toolTip += "Content : " + countContent + "<br/>";
					if(countParody > 0)
						toolTip += "Parody : " + countParody + "<br/>";
					toolTip += "</body></html>";
				} catch (DataBaseException dbe) {
					LOG.error("Error loading data", dbe);
				}
				return null;
			}
			@Override
			protected void done() {
				if(isEmpty) {
					m_ButtonTrash.setToolTipText("Trash");
					m_ButtonTrash.setIcon(Icon.desktop_trash_empty);
				} else {
					m_ButtonTrash.setToolTipText(toolTip);
					m_ButtonTrash.setIcon(Icon.desktop_trash_full);
				}
			}
		}.execute();
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
		m_ButtonTrash.setEnabled(true);
		for(JButton plugin : buttonPlugins)
			plugin.setEnabled(true);
		loadData();
	}

	@Override
	public void databaseDisconnected()
	{
		m_ButtonTrash.setEnabled(false);
		for(JButton plugin : buttonPlugins)
			plugin.setEnabled(false);
		new SwingWorker<Void, Object>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				for(JInternalFrame jif : getAllFrames())
				{
					try
					{
						((WindowEx)jif).dispose();
					} catch(Exception e) { }
				}
				return null;
			}
		}.execute();
	}

	@Override
	public void databaseCommit()
	{
		loadData();
	}

	@Override
	public void databaseRollback()
	{
		loadData();
	}
	
	@Override
	public void recordRecycled(Record rcd) { }

	@Override
	public void recordRestored(Record rcd) { }
	
	private final class WindowArtistImpl extends WindowEx
	{
		private PanelArtist panel;
		
		WindowArtistImpl(Record artist) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_artist);
			this.type = Type.WINDOW_ARTIST;
			if(artist == null)
				super.setTitle("Add Artist");
			else
				super.setTitle(artist.toString());
			panel = new PanelArtist((Artist)artist);
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
				remove(this);
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
	}
	
	private final class WindowBookImpl extends WindowEx
	{
		private PanelBook panel;
		
		WindowBookImpl(Record book) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_book);
			this.type = Type.WINDOW_BOOK;
			if(book == null)
				super.setTitle("Add Book");
			else
				super.setTitle(book.toString());
			panel = new PanelBook((Book)book);
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
				remove(this);
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
	}
	
	private final class WindowCircleImpl extends WindowEx
	{
		private PanelCircle panel;
		
		WindowCircleImpl(Record circle) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_circle);
			this.type = Type.WINDOW_CIRCLE;
			if(circle == null)
				super.setTitle("Add Circle");
			else
				super.setTitle(circle.toString());
			panel = new PanelCircle((Circle)circle);
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
				remove(this);
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
	}
	
	private final class WindowContentImpl extends WindowEx
	{
		private PanelContent panel;
		
		WindowContentImpl(Record content) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_content);
			this.type = Type.WINDOW_CONTENT;
			if(content == null)
				super.setTitle("Add Content");
			else
				super.setTitle(content.toString());
			panel = new PanelContent((Content)content);
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
				remove(this);
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
	}
	
	private final class WindowConventionImpl extends WindowEx
	{
		private PanelConvention panel;
		
		WindowConventionImpl(Record convention) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_convention);
			this.type = Type.WINDOW_CONVENTION;
			if(convention == null)
				super.setTitle("Add Convention");
			else
				super.setTitle(convention.toString());
			panel = new PanelConvention((Convention)convention);
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
				remove(this);
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
	}
	
	private final class WindowParodyImpl extends WindowEx
	{
		private PanelParody panel;
		
		WindowParodyImpl(Record parody) throws DataBaseException
		{
			super();
			super.setFrameIcon(Icon.desktop_explorer_parody);
			this.type = Type.WINDOW_PARODY;
			if(parody == null)
				super.setTitle("Add Parody");
			else
				super.setTitle(parody.toString());
			panel = new PanelParody((Parody)parody);
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
				remove(this);
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
	}
	
	private final class WindowSearchImpl extends WindowEx
	{
		WindowSearchImpl() throws DataBaseException
		{
			super();
			this.type = Type.WINDOW_SEARCH;
			super.setFrameIcon(Icon.window_tab_explorer_search);
			super.setTitle("Search");
			JTabbedPane pane = new JTabbedPane();
			pane.setFocusable(false);
			PanelSearch<?> subPane;
			subPane = new org.dyndns.doujindb.ui.dialog.PanelSearch.IArtist(pane, 0);
			listeners.add(subPane);
			pane.addTab("Artist", Icon.desktop_explorer_artist, subPane);
			subPane = new PanelSearch.IBook(pane, 1);
			listeners.add(subPane);
			pane.addTab("Book", Icon.desktop_explorer_book, subPane);
			subPane = new PanelSearch.ICircle(pane, 2);
			listeners.add(subPane);
			pane.addTab("Circle", Icon.desktop_explorer_circle, subPane);
			subPane = new PanelSearch.IConvention(pane, 3);
			listeners.add(subPane);
			pane.addTab("Convention", Icon.desktop_explorer_convention, subPane);
			subPane = new PanelSearch.IContent(pane, 4);
			listeners.add(subPane);
			pane.addTab("Content", Icon.desktop_explorer_content, subPane);
			subPane = new PanelSearch.IParody(pane, 5);
			listeners.add(subPane);
			pane.addTab("Parody", Icon.desktop_explorer_parody, subPane);
			super.add(pane);
			super.setVisible(true);
		}
	}
	
	private final class WindowTrashImpl extends WindowEx
	{
		WindowTrashImpl() throws DataBaseException
		{
			super();
			this.type = Type.WINDOW_TRASH;
			super.setFrameIcon(Icon.desktop_explorer_trash);
			super.setTitle("Trash");
			PanelTrash panel = new PanelTrash();
			listeners.add(panel);
			super.add(panel);
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
			super.setFrameIcon(Icon.window_tab_plugins);
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