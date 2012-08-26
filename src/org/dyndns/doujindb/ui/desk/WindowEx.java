package org.dyndns.doujindb.ui.desk;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.DataBaseListener;

@SuppressWarnings("serial")
public abstract class WindowEx extends JInternalFrame implements DataBaseListener
{
	protected Vector<DataBaseListener> listeners = new Vector<DataBaseListener>();
	protected Type type;
	
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
	
	WindowEx()
	{
		super("", true, true, true, true);
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
	    javax.swing.plaf.basic.BasicInternalFrameUI ui = new javax.swing.plaf.basic.BasicInternalFrameUI(this);
	    super.setUI(ui);
	    super.setVisible(true);
	    
	    Core.Database.addDataBaseListener(this);
	}
	
	@Override
	public void dispose()
	{
		Core.Database.removeDataBaseListener(this);
		super.dispose();
	}

	public Type getType()
	{
		return type;
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
	public void recordRecycled(Record rcd)
	{
		for(DataBaseListener l : listeners)
			l.recordRecycled(rcd);
	}

	@Override
	public void recordRestored(Record rcd)
	{
		for(DataBaseListener l : listeners)
			l.recordRestored(rcd);
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