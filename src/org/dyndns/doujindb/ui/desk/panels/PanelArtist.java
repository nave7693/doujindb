package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

public final class PanelArtist implements Validable, LayoutManager, ActionListener
{
	private Artist tokenArtist;
	
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
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
	private RecordCircleEditor editorCircles;
	private JButton buttonConfirm;
	
	public PanelArtist(JComponent pane, Artist token) throws DataBaseException
	{
		if(token != null)
			tokenArtist = token;
		else
			tokenArtist = new NullArtist();
		
		pane.setLayout(this);
		labelJapaneseName = new JLabel("Japanese Name");
		labelJapaneseName.setFont(font);
		textJapaneseName = new JTextField("");
		textJapaneseName.setFont(font);
		labelTranslatedName = new JLabel("Translated Name");
		labelTranslatedName.setFont(font);
		textTranslatedName = new JTextField("");
		textTranslatedName.setFont(font);
		labelRomanjiName = new JLabel("Romanji Name");
		labelRomanjiName.setFont(font);
		textRomanjiName = new JTextField("");
		textRomanjiName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField("");
		textWeblink.setFont(font);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenArtist);
		tabLists.addTab("Works", Core.Resources.Icons.get("JDesktop/Explorer/Book"), editorWorks);
		editorCircles = new RecordCircleEditor(tokenArtist);
		tabLists.addTab("Circles", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), editorCircles);
		tabLists.setUI(new DouzTabbedPaneUI(new DouzCheckBoxList<?>[]{
				editorWorks.getCheckBoxList(),
				editorCircles.getCheckBoxList()
		}));
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

		new SwingWorker<Void, Object>() {
			@Override
			public Void doInBackground() {
				loadData();
				validateUI(new DouzEvent(DouzEvent.Type.DATABASE_REFRESH, null));
				return null;
			}
		}.execute();
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
		try
		{
			if(tokenArtist instanceof NullArtist)
				tokenArtist = Core.Database.doInsert(Artist.class);
			tokenArtist.setJapaneseName(textJapaneseName.getText());
			tokenArtist.setTranslatedName(textTranslatedName.getText());
			tokenArtist.setRomanjiName(textRomanjiName.getText());
			tokenArtist.setWeblink(textWeblink.getText());
			for(Book b : tokenArtist.getBooks())
				if(!editorWorks.contains(b))
					tokenArtist.removeBook(b);
			java.util.Iterator<Book> books = editorWorks.iterator();
			while(books.hasNext())
				tokenArtist.addBook(books.next());
			for(Circle c : tokenArtist.getCircles())
				if(!editorCircles.contains(c))
					tokenArtist.removeCircle(c);
			java.util.Iterator<Circle> circles = editorCircles.iterator();
			while(circles.hasNext())
				tokenArtist.addCircle(circles.next());
			
			new SwingWorker<Void, Object>() {
				@Override
				public Void doInBackground() {
					if(Core.Database.isAutocommit())
						Core.Database.doCommit();
					return null;
				}
				@Override
				public void done() {
					Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_UPDATE, tokenArtist));
					buttonConfirm.setEnabled(true);
				}
			}.execute();
		} catch (DataBaseException dbe) {
			buttonConfirm.setEnabled(true);
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(tokenArtist.isRecycled())
		{
			textJapaneseName.setEditable(false);
			textTranslatedName.setEditable(false);
			textRomanjiName.setEditable(false);
			textWeblink.setEditable(false);
			editorWorks.setEnabled(false);
			editorCircles.setEnabled(false);
			buttonConfirm.setEnabled(false);
		}
		if(ve.getType() != DouzEvent.Type.DATABASE_UPDATE)
		{
			if(ve.getParameter() instanceof Circle)
				editorCircles.validateUI(ve);
			if(ve.getParameter() instanceof Book)
				editorWorks.validateUI(ve);
		}else
		{
			editorCircles.validateUI(ve);
			editorWorks.validateUI(ve);
		}
	}
	
	private void loadData()
	{
		textJapaneseName.setText(tokenArtist.getJapaneseName());
		textTranslatedName.setText(tokenArtist.getTranslatedName());
		textRomanjiName.setText(tokenArtist.getRomanjiName());
		textWeblink.setText(tokenArtist.getWeblink());
	}
	
	private final class NullArtist implements Artist
	{
		@Override
		public String getID() throws DataBaseException { return null; }

		@Override
		public void doRecycle() throws DataBaseException { }

		@Override
		public void doRestore() throws DataBaseException { }

		@Override
		public boolean isRecycled() throws DataBaseException { return false; }

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
		public String getJapaneseName() throws DataBaseException { return ""; }

		@Override
		public String getTranslatedName() throws DataBaseException { return ""; }

		@Override
		public String getRomanjiName() throws DataBaseException { return ""; }

		@Override
		public String getWeblink() throws DataBaseException { return ""; }

		@Override
		public void setJapaneseName(String japaneseName) throws DataBaseException { }

		@Override
		public void setTranslatedName(String translatedName) throws DataBaseException { }

		@Override
		public void setRomanjiName(String romanjiName) throws DataBaseException { }

		@Override
		public void setWeblink(String weblink) throws DataBaseException { }

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Circle> getCircles() throws DataBaseException
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
		public void addCircle(Circle circle) throws DataBaseException { }

		@Override
		public void removeCircle(Circle circle) throws DataBaseException { }

		@Override
		public void removeAll() throws DataBaseException { }
	}
}