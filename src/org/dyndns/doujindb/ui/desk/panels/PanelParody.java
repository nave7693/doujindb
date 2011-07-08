package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.core.Database;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;




@SuppressWarnings("serial")
public final class PanelParody implements Validable, LayoutManager, ActionListener
{
	private DouzWindow parentWindow;
	private Parody tokenParody;
	private boolean isModify;
	
	private final Font font = (Font)Core.Settings.getValue("org.dyndns.doujindb.ui.font");
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomanjiName;
	private JTextField textRomanjiName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private JButton buttonConfirm;
	
	public PanelParody(DouzWindow parent, JComponent pane, Parody token)
	{
		parentWindow = parent;
		if(token == null)
		{
			tokenParody = Database.newParody();
			tokenParody.setJapaneseName("");
			isModify = false;
		}else
		{
			tokenParody = token;
			isModify = true;
		}
		pane.setLayout(this);
		labelJapaneseName = new JLabel("Japanese Name");
		labelJapaneseName.setFont(font);
		textJapaneseName = new JTextField(tokenParody.getJapaneseName());
		textJapaneseName.setFont(font);
		labelTranslatedName = new JLabel("Translated Name");
		labelTranslatedName.setFont(font);
		textTranslatedName = new JTextField(tokenParody.getTranslatedName());
		textTranslatedName.setFont(font);
		labelRomanjiName = new JLabel("Romanji Name");
		labelRomanjiName.setFont(font);
		textRomanjiName = new JTextField(tokenParody.getRomanjiName());
		textRomanjiName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField(tokenParody.getWeblink());
		textWeblink.setFont(font);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenParody);
		tabLists.addTab("Works", Core.Resources.Icons.get("JDesktop/Explorer/Book"), editorWorks);
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		pane.add(labelJapaneseName);
		pane.add(textJapaneseName);
		pane.add(labelTranslatedName);
		pane.add(textTranslatedName);
		pane.add(labelRomanjiName);
		pane.add(textRomanjiName);
		pane.add(labelWeblink);
		pane.add(textWeblink);
		pane.add(tabLists);
		pane.add(buttonConfirm);
	}
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		labelJapaneseName.setBounds(3, 3, 100, 15);
		textJapaneseName.setBounds(103, 3, width - 106, 15);
		labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
		textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
		labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
		textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);
		labelWeblink.setBounds(3, 3 + 45, 100, 15);
		textWeblink.setBounds(103, 3 + 45, width - 106, 15);
		tabLists.setBounds(3, 3 + 60, width - 6, height - 90);
		buttonConfirm.setBounds(width / 2 - 40, height - 25, 80,  20);
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
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		buttonConfirm.setEnabled(false);
		buttonConfirm.setIcon(Core.Resources.Icons.get("JFrame/Loading"));
		if(textJapaneseName.getText().length()<1)
		{
			final Border brd1 = textJapaneseName.getBorder();
			final Border brd2 = BorderFactory.createLineBorder(Color.ORANGE);
			final Timer tmr = new Timer(100, new AbstractAction () {
				boolean hasBorder = true;
				int count = 0;
				public void actionPerformed (ActionEvent e) {
					if(count++ > 4)
						((javax.swing.Timer)e.getSource()).stop();
					if (hasBorder)
						textJapaneseName.setBorder(brd2);
					else
						textJapaneseName.setBorder(brd1);
					hasBorder = !hasBorder;
				}
			});
			tmr.start();
		}else
		{
			Rectangle rect = parentWindow.getBounds();
			parentWindow.dispose();
			Core.UI.Desktop.remove(parentWindow);
			{
				tokenParody.setJapaneseName(textJapaneseName.getText());
				tokenParody.setTranslatedName(textTranslatedName.getText());
				tokenParody.setRomanjiName(textRomanjiName.getText());
				tokenParody.setWeblink(textWeblink.getText());
				for(Book b : tokenParody.getBooks())
				{
					if(!editorWorks.contains(b))
					{
						b.getParodies().remove(tokenParody);
						tokenParody.getBooks().remove(b);
					}
				}
				java.util.Iterator<Book> books = editorWorks.iterator();
				while(books.hasNext())
				{
					Book b = books.next();
					b.getParodies().add(tokenParody);
					tokenParody.getBooks().add(b);
				}
				if(!isModify)
				{
					Database.getParodies().insert(tokenParody);
					Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, tokenParody));
				}else
					Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMCHANGED, tokenParody));				
			}
			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_PARODY, tokenParody, rect);
		}
		buttonConfirm.setEnabled(true);
		buttonConfirm.setIcon(null);
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(ve.getType() != DouzEvent.DATABASE_ITEMCHANGED)
		{
			if(ve.getParameter() instanceof Book)
				editorWorks.validateUI(ve);
		}else
			editorWorks.validateUI(ve);
	}	
}