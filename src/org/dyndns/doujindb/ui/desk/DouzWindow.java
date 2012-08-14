package org.dyndns.doujindb.ui.desk;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.*;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.ui.desk.panels.*;

@SuppressWarnings("serial")
public final class DouzWindow extends JInternalFrame implements LayoutManager, DataBaseListener
{
	private JComponent root = new JPanel();
	private Vector<DataBaseListener> listeners = new Vector<DataBaseListener>();
	private Type type;
	private Object item;
	
	public static enum Type
	{
		WINDOW_SEARCH,
		WINDOW_RECYCLEBIN,
		WINDOW_TOOLS,
		WINDOW_PLUGIN,
		WINDOW_ARTIST,
		WINDOW_BOOK,
		WINDOW_CIRCLE,
		WINDOW_CONTENT,
		WINDOW_CONVENTION,
		WINDOW_PARODY
	}
	
	DouzWindow(Type type) throws DataBaseException, RemoteException
	{
		this(type, null);		
	}
	DouzWindow(Type type, Object param) throws DataBaseException
	{
		super("", true, true, true, true);
		{
			this.type = type;
			this.item = param;
		}
		setLayout(this);
		getDesktopIcon().setUI(new BasicDesktopIconUI()
		{
			protected void installComponents()
			{
				super.installComponents();
			}
			@Override public Dimension getPreferredSize(JComponent c)
			{
				return new Dimension(145, 25);
			}
			protected void uninstallComponents()
			{
				super.uninstallComponents();
			}        
		});
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		// Dispose itself when ESC is pressed from the keyboard
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	    rootPane.registerKeyboardAction(new ActionListener()
		    {
		    	public void actionPerformed(ActionEvent actionEvent)
		    	{
		    		dispose();
		    	}
		    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		switch(type)
		{
			case WINDOW_SEARCH:
			{
				setFrameIcon(Core.Resources.Icons.get("JFrame/Tab/Explorer/Search"));
				setTitle("Search");
				JTabbedPane pane = new JTabbedPane();
				pane.setFocusable(false);
				PanelSearch sp;
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_ARTIST, pane, 0);
				listeners.add(sp);
				pane.addTab("Artist", Core.Resources.Icons.get("JDesktop/Explorer/Artist"), sp);
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_BOOK, pane, 1);
				listeners.add(sp);
				pane.addTab("Book", Core.Resources.Icons.get("JDesktop/Explorer/Book"), sp);
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_CIRCLE, pane, 2);
				listeners.add(sp);
				pane.addTab("Circle", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), sp);
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_CONVENTION, pane, 3);
				listeners.add(sp);
				pane.addTab("Convention", Core.Resources.Icons.get("JDesktop/Explorer/Convention"), sp);
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_CONTENT, pane, 4);
				listeners.add(sp);
				pane.addTab("Content", Core.Resources.Icons.get("JDesktop/Explorer/Content"), sp);
				sp = new PanelSearch(PanelSearch.Type.ISEARCH_PARODY, pane, 5);
				listeners.add(sp);
				pane.addTab("Parody", Core.Resources.Icons.get("JDesktop/Explorer/Parody"), sp);
				add(pane);
				root = pane;
				break;
			}
			case WINDOW_ARTIST:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
				if(item == null)
					setTitle("Add Artist");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_ARTIST, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_BOOK:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Book"));
				if(item == null)
					setTitle("Add Book");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_BOOK, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_CIRCLE:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
				if(item == null)
					setTitle("Add Circle");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_CIRCLE, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_CONTENT:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Content"));
				if(item == null)
					setTitle("Add Content");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_CONTENT, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_CONVENTION:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
				if(item == null)
					setTitle("Add Convention");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_CONVENTION, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_PARODY:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
				if(item == null)
					setTitle("Add Parody");
				else
					setTitle(((Record)item).toString());
				EditPanel ep = new EditPanel(this, Type.WINDOW_PARODY, item);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_RECYCLEBIN:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/RecycleBin"));
				setTitle("Recycle Bin");
				EditPanel ep = new EditPanel(this, Type.WINDOW_RECYCLEBIN, null);
				root = ep;
				listeners.add(ep);
				add(root);
				break;
			}
			case WINDOW_TOOLS:
			{
				setFrameIcon(Core.Resources.Icons.get("JDesktop/Explorer/Tools"));
				setTitle("Tools");
				EditPanel ep = new EditPanel(this, Type.WINDOW_TOOLS, null);
				root = ep;
				listeners.add(ep);
				add(root);
				//FIXME org.dyndns.doujindb.util.RepositoryIndexer.rebuildIndexes();
				break;
			}
			case WINDOW_PLUGIN:
			{
				Plugin plugin = (Plugin) item;
				setFrameIcon(Core.Resources.Icons.get("JFrame/Tab/Plugins"));
				setTitle(plugin.getName());
				root = plugin.getUI();
				//TODO listeners.add(ep);
				add(root);
				break;
			}
		}
		super.setVisible(true);
	}
	
	public Type getType()
	{
		return type;
	}
	
	public Object getItem()
	{
		return item;
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		root.setBounds(0, 0, width, height);
	}
	
	@Override
	public void addLayoutComponent(String key,Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return getMinimumSize();
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return getPreferredSize();
	}
	
	@Override
	public void recordAdded(Record rcd)
	{
		for(DataBaseListener l : listeners)
			l.recordAdded(rcd);
	}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		if(item.equals(rcd))
		{
			super.dispose();
			Core.UI.Desktop.remove(this);
		}
		for(DataBaseListener l : listeners)
			l.recordDeleted(rcd);
	}
	
	@Override
	public void recordUpdated(Record rcd)
	{
		for(DataBaseListener l : listeners)
			l.recordUpdated(rcd);
	}
	
	@Override
	public void databaseConnected()
	{
		for(DataBaseListener l : listeners)
			l.databaseConnected();
	}
	
	@Override
	public void databaseDisconnected()
	{
		for(DataBaseListener l : listeners)
			l.databaseDisconnected();
	}
	
	@Override
	public void databaseCommit()
	{
		for(DataBaseListener l : listeners)
			l.databaseCommit();
	}
	
	@Override
	public void databaseRollback()
	{
		for(DataBaseListener l : listeners)
			l.databaseRollback();
	}
}
