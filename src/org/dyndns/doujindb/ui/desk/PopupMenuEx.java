package org.dyndns.doujindb.ui.desk;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.dyndns.doujindb.ui.UI;

public final class PopupMenuEx implements ActionListener
{
	private JPopupMenu popup;
	private JMenuItem[] items;
	public static final int SELECTION_NONE = -1;
	public static final int SELECTION_CANCELED = -2;
	private int selection = SELECTION_NONE;
	private String choice = "";
	
	public PopupMenuEx(String title, Hashtable<String,ImageIcon> items)
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
		lab.setFont(UI.Font);
		popup.add(lab);
		this.items = new JMenuItem[items.size()];
		int k = 0;
		for(String i : items.keySet())
		{
			this.items[k] = new JMenuItem(i, items.get(i));
			this.items[k].addActionListener(this);
			this.items[k].setName(i);
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
	
	public String getChoice()
	{
		return choice;
	}
	
	public void show(Component comp, int x, int y)
	{
		popup.show(comp, x, y);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		int i = 0;
		for(JMenuItem item : items)
			if(item == ae.getSource())
			{
				selection = i;
				choice = item.getName();
				return;
			}				
			else
				i++;
	}
}