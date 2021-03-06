package org.dyndns.doujindb.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;

@SuppressWarnings("serial")
public abstract class WindowEx extends JInternalFrame implements DataBaseListener
{
	JComponent ModalLayer;
	
	protected Vector<DataBaseListener> listeners = new Vector<DataBaseListener>();
	protected Type type;
	
	public static enum Type
	{
		WINDOW_SEARCH,
		WINDOW_TRASH,
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
	    ui.getNorthPane().setPreferredSize(new Dimension(ui.getNorthPane().getPreferredSize().width, 22));
	    super.setVisible(true);
	    
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
	    
	    DataBase.addDataBaseListener(this);
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		/**
		 * Why would we spawn a Thread just for removing 'this' from the DataBase Listeners?
		 * Even if we put 'synchronized' blocks in the EventPoller thread and methods add/removeDataBaseListener() we are still calling this from the same stack.
		 * We have to somehow avoid this, so we spawn a separate Thread here.
		 */
		final WindowEx window = this;
		new Thread()
		{
			@Override
			public void run() {
				DataBase.removeDataBaseListener(window);
			}
		}.start();
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
	public void recordUpdated(Record rcd, UpdateData data)
	{
		for(DataBaseListener l : listeners)
			l.recordUpdated(rcd, data);
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