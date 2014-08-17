package org.dyndns.doujindb.ui.dialog.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.dyndns.doujindb.db.record.Book.Rating;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public class BookRatingEditor extends JPanel
{
	private JButton[] buttons = new JButton[Rating.values().length - 1]; // UNRATED should not be "counted"
	private Rating bookRating;
	private final ImageIcon CHECKED = Icon.desktop_explorer_book_rating_checked;
	private final ImageIcon UNCHECKED = Icon.desktop_explorer_book_rating_unchecked;
	
	public BookRatingEditor(Rating rating)
	{
		bookRating = rating;
		int index = 0;
		for(final Rating r : Rating.values())
		{
			if(r.equals(Rating.UNRATED))
				continue; // skip UNRATED
			buttons[index] = new JButton(UNCHECKED);
			buttons[index].setSize(16,16);
			buttons[index].setFocusable(false);
			buttons[index].setBorder(null);
			buttons[index].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) {
					bookRating = r;
					doRender();
				}			
			});
			add(buttons[index]);
			index++;
		}
		super.setPreferredSize(new Dimension(16*buttons.length, 20));
		setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				for(int k=0;k<buttons.length;k++)
					buttons[k].setBounds(0 + k * 16,0,16,16);
			}
			@Override
			public void addLayoutComponent(String key,Component c){}
			@Override
			public void removeLayoutComponent(Component c){}
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
			     return getPreferredSize();
			}
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
			     return getPreferredSize();
			}
		});
		doRender();
	}
	
	private void doRender()
	{
		if(bookRating.equals(Rating.UNRATED))
		{
			for(JButton b : buttons)
				b.setIcon(UNCHECKED);
			return;
		}
		boolean unchecked = false;
		int index = 0;
		for(Rating r : Rating.values())
		{
			if(r.equals(Rating.UNRATED))
				continue; // skip UNRATED
			JButton b = buttons[index++];
			if(unchecked)
				b.setIcon(UNCHECKED);
			else
				b.setIcon(CHECKED);
			if(r.equals(bookRating))
				unchecked = true;
		}
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		for(JButton b : buttons)
			b.setEnabled(enabled);
	}
	
	public Rating getRating()
	{
		return bookRating;
	}
}
