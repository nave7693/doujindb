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
	private int selection = -1;
	
	public DouzPopupMenu(String title, Hashtable<String,ImageIcon> items)
	{
		popup = new JPopupMenu();
		popup.addPopupMenuListener(new PopupMenuListener()
		{

			@Override
			public void popupMenuCanceled(PopupMenuEvent pme) { selection = -2; }

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {}
			
		});
		JLabel lab = new JLabel(title);
		lab.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		lab.setFont((Font)Core.Settings.getValue("org.dyndns.doujindb.ui.font"));
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
		return selection == -1;
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