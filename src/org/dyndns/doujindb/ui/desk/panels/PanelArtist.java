package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.util.*;

@SuppressWarnings("serial")
public final class PanelArtist extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	private Artist tokenArtist;
	
	private final Font font = Core.Resources.Font;
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomajiName;
	private JTextField textRomajiName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private RecordCircleEditor editorCircles;
	private JButton buttonConfirm;
	
	public PanelArtist(Artist token) throws DataBaseException
	{
		tokenArtist = token;
		super.setLayout(this);
		labelJapaneseName = new JLabel("Japanese Name");
		labelJapaneseName.setFont(font);
		textJapaneseName = new JTextField("");
		textJapaneseName.setFont(font);
		labelTranslatedName = new JLabel("Translated Name");
		labelTranslatedName.setFont(font);
		textTranslatedName = new JTextField("");
		textTranslatedName.setFont(font);
		labelRomajiName = new JLabel("Romaji Name");
		labelRomajiName.setFont(font);
		textRomajiName = new JTextField("");
		textRomajiName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField("");
		textWeblink.setFont(font);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenArtist);
		tabLists.addTab("Works", Core.Resources.Icons.get("Desktop/Explorer/Book"), editorWorks);
		editorCircles = new RecordCircleEditor(tokenArtist);
		tabLists.addTab("Circles", Core.Resources.Icons.get("Desktop/Explorer/Circle"), editorCircles);
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				editorWorks.getRecordList(),
				editorCircles.getRecordList()
		}));
		try
		{
			DropTarget dt = new DropTarget();
			dt.addDropTargetListener(new java.awt.dnd.DropTargetAdapter()
			{
				@Override
				public void dragOver(DropTargetDragEvent dtde)
				{
					TabbedPaneUI tabpane = tabLists.getUI();
					for(int index=0;index<tabLists.getTabCount();index++)
						if(tabpane.getTabBounds(tabLists, index).contains(dtde.getLocation()))
							tabLists.setSelectedIndex(index);
					}

				@Override
				public void drop(DropTargetDropEvent dtde) { }
				
			});
			tabLists.setDropTarget(dt);
		} catch (TooManyListenersException tmle) {
			tmle.printStackTrace();
		}
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		super.add(labelJapaneseName);
		super.add(textJapaneseName);
		super.add(labelTranslatedName);
		super.add(textTranslatedName);
		super.add(labelRomajiName);
		super.add(textRomajiName);
		super.add(labelWeblink);
		super.add(textWeblink);
		super.add(tabLists);
		super.add(buttonConfirm);

		loadData();
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
		labelRomajiName.setBounds(3, 3 + 30, 100, 15);
		textRomajiName.setBounds(103, 3 + 30, width - 106, 15);
		labelWeblink.setBounds(3, 3 + 45, 100, 15);
		textWeblink.setBounds(103, 3 + 45, width - 106, 15);
		tabLists.setBounds(3, 3 + 60, width - 6, height - 90);
		buttonConfirm.setBounds(width / 2 - 40, height - 25, 80,  20);
	}
	
	public Artist getRecord()
	{
		return tokenArtist;
	}
	
	@Override
	public void addLayoutComponent(String key,Component c) {}
	
	@Override
	public void removeLayoutComponent(Component c) {}
	
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
			if(tokenArtist.getID() == null)
				tokenArtist = Core.Database.doInsert(Artist.class);
			tokenArtist.setJapaneseName(textJapaneseName.getText());
			tokenArtist.setTranslatedName(textTranslatedName.getText());
			tokenArtist.setRomajiName(textRomajiName.getText());
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
					buttonConfirm.setEnabled(true);
				}
			}.execute();
		} catch (DataBaseException dbe) {
			buttonConfirm.setEnabled(true);
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
	}
	
	private void loadData()
	{
		new SwingWorker<Void, Object>()
		{
			@Override
			public Void doInBackground()
			{
				textJapaneseName.setText(tokenArtist.getJapaneseName());
				textTranslatedName.setText(tokenArtist.getTranslatedName());
				textRomajiName.setText(tokenArtist.getRomajiName());
				textWeblink.setText(tokenArtist.getWeblink());
				if(tokenArtist.isRecycled())
				{
					textJapaneseName.setEditable(false);
					textTranslatedName.setEditable(false);
					textRomajiName.setEditable(false);
					textWeblink.setEditable(false);
					editorWorks.setEnabled(false);
					editorCircles.setEnabled(false);
					buttonConfirm.setEnabled(false);
				}
				return null;
			}
		}.execute();
	}

	@Override
	public void recordAdded(Record rcd) { }
	
	@Override
	public void recordDeleted(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordDeleted(rcd);
		if(rcd instanceof Book)
			editorWorks.recordDeleted(rcd);
		loadData();
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case PROPERTY:
			if(data.getProperty().equals("japanese_name"))
				textJapaneseName.setText(tokenArtist.getJapaneseName());
			if(data.getProperty().equals("translated_name"))
				textTranslatedName.setText(tokenArtist.getTranslatedName());
			if(data.getProperty().equals("romaji_name"))
				textRomajiName.setText(tokenArtist.getRomajiName());
			if(data.getProperty().equals("weblink"))
				textWeblink.setText(tokenArtist.getWeblink());
			break;
		//case LINK:
		//case UNLINK:
		default:
			if(data.getTarget() instanceof Circle)
				editorCircles.recordUpdated(rcd, data);
			if(data.getTarget() instanceof Book)
				editorWorks.recordUpdated(rcd, data);
		}
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordRecycled(rcd);
		if(rcd instanceof Book)
			editorWorks.recordRecycled(rcd);
		loadData();
	}
	
	@Override
	public void recordRestored(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordRestored(rcd);
		if(rcd instanceof Book)
			editorWorks.recordRestored(rcd);
		loadData();
	}
	
	@Override
	public void databaseConnected() {}
	
	@Override
	public void databaseDisconnected() {}
	
	@Override
	public void databaseCommit() {}
	
	@Override
	public void databaseRollback() {}
}