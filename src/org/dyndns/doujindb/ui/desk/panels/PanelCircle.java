package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.util.RecordList;
import org.dyndns.doujindb.ui.desk.panels.util.TabbedPaneUIEx;
import org.dyndns.doujindb.util.ImageTool;

@SuppressWarnings("serial")
public final class PanelCircle extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	private Circle tokenCircle;
	
	private final Font font = Core.Resources.Font;
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomajiName;
	private JTextField textRomajiName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
	private JLabel labelBanner;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private RecordArtistEditor editorArtists;
	private JButton buttonConfirm;
	
	public PanelCircle(Circle token) throws DataBaseException
	{
		tokenCircle = token;
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
		labelBanner = new JLabel(Core.Resources.Icons.get("Desktop/Explorer/Circle/Banner"));
		labelBanner.setName("no-banner");
		if(tokenCircle.getID() == null)
			labelBanner.setEnabled(false);
		labelBanner.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(tokenCircle.isRecycled())
					return;
				if(tokenCircle.getID() == null)
					return;
				
	    		JPopupMenu popupMenu = new JPopupMenu();
	    		
	    		JMenuItem menuItemA = new JMenuItem("Add", Core.Resources.Icons.get("Desktop/Explorer/Circle/Popup/Add"));
				menuItemA.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae)
					{
						new SwingWorker<Void,Void>()
						{
							@Override
							protected Void doInBackground() throws Exception
							{
								JFileChooser fc = Core.UI.getFileChooser();
								fc.setMultiSelectionEnabled(false);
								int result = fc.showOpenDialog(Core.UI);
								if(result != JFileChooser.APPROVE_OPTION)
									return null;
								Image img = null;
								try
								{
									img = ImageTool.read(fc.getSelectedFile());
								} catch (IOException ioe) {
									Core.Logger.logError(ioe.getMessage(), ioe);
								}
								if(img == null)
									return null;
								try
								{
									DataFile ds = Core.Repository.child(tokenCircle.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenCircle.getID());
									ds.touch();
									OutputStream out = ds.getOutputStream();
									File in = fc.getSelectedFile();
									BufferedImage image = ImageTool.read(in);
									BufferedImage im = new BufferedImage(200, 40, BufferedImage.TYPE_INT_ARGB);
									Graphics2D graphics2D = im.createGraphics();
									graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
									graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
									graphics2D.drawImage(image, 0, 0, null);
									ImageTool.write(im, out);
									out.close();
								} catch (Exception e) {
									Core.Logger.logError(e.getMessage(), e);
								}
								try
								{
									DataFile ds = Core.Repository.child(tokenCircle.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenCircle.getID());
									if(ds.exists())
									{
										InputStream in = ds.getInputStream();
										final ImageIcon ii = new ImageIcon(ImageTool.read(in));
										SwingUtilities.invokeLater(new Runnable()
										{
											@Override
											public void run() {
												labelBanner.setIcon(ii);
												labelBanner.setName("banner");
											}
										});
										in.close();
									}
								} catch (NullPointerException | IOException | DataBaseException e) {
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelBanner.setIcon(Core.Resources.Icons.get("Desktop/Explorer/Circle/Banner"));
											labelBanner.setName("no-banner");
										}
									});
									e.printStackTrace();
								}
								return null;
							}
						}.execute();
					}
				});
				menuItemA.setName("add");
				menuItemA.setActionCommand("add");

				JMenuItem menuItemR = new JMenuItem("Remove", Core.Resources.Icons.get("Desktop/Explorer/Circle/Popup/Remove"));
				menuItemR.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae)
					{
						new SwingWorker<Void,Void>()
						{
							@Override
							protected Void doInBackground() throws Exception
							{
								try
								{
									DataFile df = Core.Repository.child(tokenCircle.getID());
									df.mkdir();
									df = Core.Repository.getPreview(tokenCircle.getID());
									df.delete();
								} catch (NullPointerException npe) {
								} catch (Exception e)
								{
									Core.Logger.logError(e.getMessage(), e);
								}
								return null;
							}
							@Override
							protected void done() {
								labelBanner.setIcon(Core.Resources.Icons.get("Desktop/Explorer/Circle/Banner"));
								labelBanner.setName("no-banner");
						    }
						}.execute();
					}
				});
				menuItemR.setName("remove");
				menuItemR.setActionCommand("remove");
				
				if(labelBanner.getName().equals("no-banner"))
					popupMenu.add(menuItemA);
				else
					popupMenu.add(menuItemR);
				
				popupMenu.show(me.getComponent(), me.getX(), me.getY());
			}
		});
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenCircle);
		tabLists.addTab("Works", Core.Resources.Icons.get("Desktop/Explorer/Book"), editorWorks);
		editorArtists = new RecordArtistEditor(tokenCircle);
		tabLists.addTab("Artists", Core.Resources.Icons.get("Desktop/Explorer/Artist"), editorArtists);
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				editorWorks.getRecordList(),
				editorArtists.getRecordList()
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
		super.add(labelBanner);
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
		labelBanner.setBounds(3, 3 + 62, 200, 40);
		tabLists.setBounds(3, 3 + 62 + 42, width - 6, height - 135);
		buttonConfirm.setBounds(width / 2 - 40, height - 25, 80,  20);
	}
	
	public Circle getRecord()
	{
		return tokenCircle;
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
			if(tokenCircle.getID() == null)
				tokenCircle = Core.Database.doInsert(Circle.class);
			tokenCircle.setJapaneseName(textJapaneseName.getText());
			tokenCircle.setTranslatedName(textTranslatedName.getText());
			tokenCircle.setRomajiName(textRomajiName.getText());
			tokenCircle.setWeblink(textWeblink.getText());
			for(Artist a : tokenCircle.getArtists())
				if(!editorArtists.contains(a))
				tokenCircle.removeArtist(a);
			java.util.Iterator<Artist> artists = editorArtists.iterator();
			while(artists.hasNext())
				tokenCircle.addArtist(artists.next());
			for(Book b : tokenCircle.getBooks())
				if(!editorWorks.contains(b))
				tokenCircle.removeBook(b);
			java.util.Iterator<Book> books = editorWorks.iterator();
			while(books.hasNext())
				tokenCircle.addBook(books.next());

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
			Core.Logger.logError(dbe.getMessage(), dbe);
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
				textJapaneseName.setText(tokenCircle.getJapaneseName());
				textTranslatedName.setText(tokenCircle.getTranslatedName());
				textRomajiName.setText(tokenCircle.getRomajiName());
				textWeblink.setText(tokenCircle.getWeblink());
				try
				{
					if(tokenCircle.getID() == null)
						return null;
					DataFile ds = Core.Repository.child(tokenCircle.getID());
					ds.mkdir();
					ds = Core.Repository.getPreview(tokenCircle.getID());
					if(ds.exists())
					{
						InputStream in = ds.getInputStream();
						labelBanner.setIcon(new ImageIcon(ImageTool.read(in)));
						labelBanner.setName("banner");
						in.close();
					}
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(tokenCircle.isRecycled())
				{
					textJapaneseName.setEditable(false);
					textTranslatedName.setEditable(false);
					textRomajiName.setEditable(false);
					textWeblink.setEditable(false);
					editorWorks.setEnabled(false);
					editorArtists.setEnabled(false);
					buttonConfirm.setEnabled(false);
				}
				if(tokenCircle.getID() == null)
					labelBanner.setEnabled(false);
				else
					labelBanner.setEnabled(true);
				return null;
			}
		}.execute();
	}

	@Override
	public void recordAdded(Record rcd) { }
	
	@Override
	public void recordDeleted(Record rcd)
	{
		if(rcd instanceof Artist)
			editorArtists.recordDeleted(rcd);
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
				textJapaneseName.setText(tokenCircle.getJapaneseName());
			if(data.getProperty().equals("translated_name"))
				textTranslatedName.setText(tokenCircle.getTranslatedName());
			if(data.getProperty().equals("romaji_name"))
				textRomajiName.setText(tokenCircle.getRomajiName());
			if(data.getProperty().equals("weblink"))
				textWeblink.setText(tokenCircle.getWeblink());
			break;
		//case LINK:
		//case UNLINK:
		default:
			if(data.getTarget() instanceof Artist)
				editorArtists.recordUpdated(rcd, data);
			if(data.getTarget() instanceof Book)
				editorWorks.recordUpdated(rcd, data);
		}
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Artist)
			editorArtists.recordRecycled(rcd);
		if(rcd instanceof Book)
			editorWorks.recordRecycled(rcd);
		loadData();
	}
	
	@Override
	public void recordRestored(Record rcd)
	{
		if(rcd instanceof Artist)
			editorArtists.recordRestored(rcd);
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