package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.dyndns.doujindb.Core;



public final class DouzPopupMenu implements ActionListener
{
	private JPopupMenu popup;
	private JMenuItem[] items;
	public static final int SELECTION_NONE = -1;
	public static final int SELECTION_CANCELED = -2;
	private int selection = SELECTION_NONE;
	
	public DouzPopupMenu(String title, Hashtable<String,ImageIcon> items)
	{
		popup = new JPopupMenu();
		popup.addPopupMenuListener(new PopupMenuListener()
		{

			@Override
			public void popupMenuCanceled(PopupMenuEvent pme) { selection = SELECTION_CANCELED; }

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {}
			
		});
		JLabel lab = new JLabel(title);
		lab.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		lab.setFont(Core.Properties.get("org.dyndns.doujindb.ui.font").asFont());
		popup.add(lab);
		this.items = new JMenuItem[items.size()];
		int k = 0;
		for(String i : items.keySet())
		{
			this.items[k] = new JMenuItem(i, items.get(i));
			this.items[k].addActionListener(this);
			popup.add(this.items[k]);
			k++;
		}
	}
	
	public boolean isValid()
	{
		return selection == SELECTION_NONE;
	}
	
	public int getResult()
	{
		return selection;
	}
	
	public void show(Component comp, int x, int y)
	{
		popup.show(comp, x, y);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		int i = 0;
		for(JMenuItem itm : items)
			if(itm == ae.getSource())
			{
				selection = i;
				return;
			}				
			else
				i++;
	}
}