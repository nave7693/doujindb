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
	private JButton[] buttons = new JButton[5];
	private Rating bookRating;
	private final ImageIcon CHECKED = Icon.desktop_explorer_book_rating_checked;
	private final ImageIcon UNCHECKED = Icon.desktop_explorer_book_rating_unchecked;
	
	public BookRatingEditor(Rating rating)
	{
		super();
		bookRating = rating;
		for(int k=0;k<buttons.length;k++)
		{
			buttons[k] = new JButton(UNCHECKED);
			buttons[k].setSize(16,16);
			buttons[k].setFocusable(false);
			buttons[k].setBorder(null);
			add(buttons[k]);
		}
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
			     return parent.getMinimumSize();
			}
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
			     return parent.getPreferredSize();
			}
		});
		buttons[0].addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				bookRating = Rating.R1;
				_render();
			}			
		});
		buttons[1].addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				bookRating = Rating.R2;
				_render();
			}			
		});
		buttons[2].addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				bookRating = Rating.R3;
				_render();
			}			
		});
		buttons[3].addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				bookRating = Rating.R4;
				_render();
			}			
		});
		buttons[4].addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				bookRating = Rating.R5;
				_render();
			}			
		});
		_render();
	}
	
	private void _render()
	{
		for(int k=0;k<buttons.length;k++)
			buttons[k].setIcon(UNCHECKED);
		switch(bookRating)
		{
		case R5:
			buttons[4].setIcon(CHECKED);
		case R4:
			buttons[3].setIcon(CHECKED);
		case R3:
			buttons[2].setIcon(CHECKED);
		case R2:
			buttons[1].setIcon(CHECKED);
		case R1:
			buttons[0].setIcon(CHECKED);
		case UNRATED:
			;
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
