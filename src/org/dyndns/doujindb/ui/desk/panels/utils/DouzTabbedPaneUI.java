package org.dyndns.doujindb.ui.desk.panels.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.dyndns.doujindb.ui.desk.events.DouzEvent;
import org.dyndns.doujindb.ui.desk.events.Validable;

public final class DouzTabbedPaneUI extends BasicTabbedPaneUI implements Validable
{
	private DouzCheckBoxList<?>[] checkBoxLists;
	
	public DouzTabbedPaneUI(DouzCheckBoxList<?>[] cbls)
	{
		checkBoxLists = cbls;
		for(DouzCheckBoxList<?> cbl : cbls)
			if(cbl != null)
				cbl.setValidableParent(this);
	}
	
	@Override
	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics fm)
	{
		if(checkBoxLists[tabIndex] == null)
			return super.calculateTabWidth(tabPlacement, tabIndex, fm);
		else
			return super.calculateTabWidth(tabPlacement, tabIndex, fm) + fm.stringWidth(" (" + checkBoxLists[tabIndex].getVisibleItemCount() + ")");
	}

	@Override
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect)
	{
		if(checkBoxLists[tabIndex] == null)
		{
			super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			return;
		}
		
		Rectangle tabRect = rects[tabIndex];
		int selectedIndex = tabPane.getSelectedIndex();
		boolean isSelected = selectedIndex == tabIndex;

		if (g instanceof Graphics2D)
		{
			paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);

			paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);

			String title = tabPane.getTitleAt(tabIndex);
			Font font = tabPane.getFont();
			FontMetrics metrics = g.getFontMetrics(font);
			Icon icon = getIconForTab(tabIndex);

			layoutLabel(tabPlacement, metrics, tabIndex, title + " (" + checkBoxLists[tabIndex].getVisibleItemCount() + ")", icon, tabRect, iconRect, textRect, isSelected);
			
			paintText(g, tabPlacement, font, metrics, tabIndex, title + " (" + checkBoxLists[tabIndex].getVisibleItemCount() + ")", textRect, isSelected);

			paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);

			paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect, textRect, isSelected);
		}
	}

	@Override
	public void validateUI(DouzEvent ve)
	{
		tabPane.repaint();
	}

}
