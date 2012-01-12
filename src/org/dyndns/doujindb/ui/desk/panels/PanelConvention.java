package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;

public final class PanelConvention implements Validable, LayoutManager, ActionListener
{
	private DouzWindow parentWindow;
	private Convention tokenConvention;
	
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JLabel labelTagName;
	private JTextField textTagName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
	private JLabel labelInfo;
	private JTextArea textInfo;
	private JScrollPane scrollInfo;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private JButton buttonConfirm;
	
	public PanelConvention(DouzWindow parent, JComponent pane, Convention token) throws DataBaseException
	{
		parentWindow = parent;
		
		if(token != null)
			tokenConvention = token;
		else
			tokenConvention = new NullConvention();
		
		pane.setLayout(this);
		labelTagName = new JLabel("Tag Name");
		labelTagName.setFont(font);
		textTagName = new JTextField(tokenConvention.getTagName());
		textTagName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField(tokenConvention.getWeblink());
		textWeblink.setFont(font);
		labelInfo = new JLabel("Info");
		labelInfo.setFont(font);
		textInfo = new JTextArea(tokenConvention.getInfo());
		textInfo.setFont(font);
		scrollInfo = new JScrollPane(textInfo);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenConvention);
		tabLists.addTab("Works", Core.Resources.Icons.get("JDesktop/Explorer/Book"), editorWorks);
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		pane.add(labelTagName);
		pane.add(textTagName);
		pane.add(labelWeblink);
		pane.add(textWeblink);
		pane.add(labelInfo);
		pane.add(scrollInfo);
		pane.add(tabLists);
		pane.add(buttonConfirm);
		validateUI(new DouzEvent(DouzEvent.Type.DATABASE_REFRESH, null));
	}
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		labelTagName.setBounds(3, 3, 100, 15);
		textTagName.setBounds(103, 3, width - 106, 15);
		labelWeblink.setBounds(3, 3 + 15, 100, 15);
		textWeblink.setBounds(103, 3 + 15, width - 106, 15);
		labelInfo.setBounds(3, 3 + 30, 100, 15);
		scrollInfo.setBounds(3, 3 + 45, width - 6, 60);
		tabLists.setBounds(3, 3 + 105, width - 6, height - 135);
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
	@SuppressWarnings("serial")
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		buttonConfirm.setEnabled(false);
		buttonConfirm.setIcon(Core.Resources.Icons.get("JFrame/Loading"));
		if(textTagName.getText().length()<1)
		{
			final Border brd1 = textTagName.getBorder();
			final Border brd2 = BorderFactory.createLineBorder(Color.ORANGE);
			final Timer tmr = new Timer(100, new AbstractAction ()
			{
				boolean hasBorder = true;
				int count = 0;
				public void actionPerformed (ActionEvent e) {
					if(count++ > 4)
						((javax.swing.Timer)e.getSource()).stop();
					if (hasBorder)
						textTagName.setBorder(brd2);
					else
						textTagName.setBorder(brd1);
					hasBorder = !hasBorder;
				}
			});
			tmr.start();
		}else
		{
			Rectangle rect = parentWindow.getBounds();
			parentWindow.dispose();
			Core.UI.Desktop.remove(parentWindow);
			try
			{
				if(tokenConvention instanceof NullConvention)
					tokenConvention = Core.Database.doInsert(Convention.class);
				tokenConvention.setTagName(textTagName.getText());
				tokenConvention.setWeblink(textWeblink.getText());
				tokenConvention.setInfo(textInfo.getText());
				for(Book b : tokenConvention.getBooks())
					if(!editorWorks.contains(b))
						tokenConvention.removeBook(b);
				java.util.Iterator<Book> books = editorWorks.iterator();
				while(books.hasNext())
					tokenConvention.addBook(books.next());
				if(Core.Database.isAutocommit())
					Core.Database.doCommit();
				Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_UPDATE, tokenConvention));			
				Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONVENTION, tokenConvention, rect);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		}
		buttonConfirm.setEnabled(true);
		buttonConfirm.setIcon(null);
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(tokenConvention.isRecycled())
		{
			textTagName.setEditable(false);
			textInfo.setEditable(false);
			editorWorks.setEnabled(false);
			buttonConfirm.setEnabled(false);
		}
		if(ve.getType() != DouzEvent.Type.DATABASE_UPDATE)
		{
			if(ve.getParameter() instanceof Book)
				editorWorks.validateUI(ve);
		}else
			editorWorks.validateUI(ve);
	}
	
	private final class NullConvention implements Convention
	{
		@Override
		public String getID() throws DataBaseException { return null; }

		@Override
		public void doRecycle() throws DataBaseException { }

		@Override
		public void doRestore() throws DataBaseException { }

		@Override
		public boolean isRecycled() throws DataBaseException { return false; }

		@Override
		public String getTagName() throws DataBaseException { return ""; }

		@Override
		public String getInfo() throws DataBaseException { return ""; }

		@Override
		public void setTagName(String tagName) throws DataBaseException { }

		@Override
		public void setInfo(String info) throws DataBaseException { }

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Book> getBooks() throws DataBaseException
		{
			return new RecordSet()
			{

				@Override
				public Iterator iterator() { return new java.util.ArrayList().iterator(); }

				@Override
				public boolean contains(Object o) throws DataBaseException { return false; }

				@Override
				public int size() throws DataBaseException { return 0; }
				
			};
		}

		@Override
		public void addBook(Book book) throws DataBaseException { }

		@Override
		public void removeBook(Book book) throws DataBaseException { }

		@Override
		public String getWeblink() throws DataBaseException { return ""; }

		@Override
		public void setWeblink(String weblink) throws DataBaseException { }
	}
}