package org.dyndns.doujindb.ui.dialog;

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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.Artist;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.db.record.Circle;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.dialog.util.*;
import org.dyndns.doujindb.ui.dialog.util.list.ListArtist;
import org.dyndns.doujindb.ui.dialog.util.list.ListBook;
import org.dyndns.doujindb.ui.dialog.util.list.RecordList;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class PanelCircle extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	private Circle tokenCircle;
	
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
	private ListBook editorWorks;
	private ListArtist editorArtists;
	private JButton buttonConfirm;
	
	protected static final Font font = UI.Font;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PanelCircle.class);
	
	public PanelCircle(Circle token) throws DataBaseException
	{
		tokenCircle = (token == null ? new NullCircle() : token);
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
		labelBanner = new JLabel(Icon.desktop_explorer_circle_banner);
		labelBanner.setName("no-banner");
		if(tokenCircle instanceof NullCircle)
			labelBanner.setEnabled(false);
		labelBanner.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(tokenCircle.isRecycled())
					return;
				if(tokenCircle instanceof NullCircle)
					return;
				
	    		JPopupMenu popupMenu = new JPopupMenu();
	    		
	    		JMenuItem menuItemA = new JMenuItem("Add", Icon.desktop_explorer_circle_popup_add);
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
								JFileChooser fc = UI.FileChooser;
								fc.setMultiSelectionEnabled(false);
								fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
								if(fc.showOpenDialog(PanelCircle.this) != JFileChooser.APPROVE_OPTION)
									return null;
								BufferedImage image = null;
								File imageFile = fc.getSelectedFile();
								try {
									image = javax.imageio.ImageIO.read(imageFile);
								} catch (IOException ioe) {
									LOG.error("Error loading banner image for [{}] from file [{}]", new Object[]{tokenCircle, imageFile, ioe});
								}
								if(image == null)
									return null;
								BufferedImage resized_image = new BufferedImage(200, 40, BufferedImage.TYPE_INT_ARGB);
								try {
									DataFile banner = DataStore.getBanner(tokenCircle.getId());
									OutputStream out = banner.openOutputStream();
									Graphics2D graphics2D = resized_image.createGraphics();
									graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
									graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
									graphics2D.drawImage(image, 0, 0, null);
									javax.imageio.ImageIO.write(resized_image, "PNG", out);
									
									final ImageIcon ii = new ImageIcon(resized_image);
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelBanner.setIcon(ii);
											labelBanner.setName("banner");
										}
									});
									
									out.close();
								} catch (DataStoreException dse) {
									LOG.error("Error storing banner image in DataStore", dse);
									
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelBanner.setIcon(Icon.desktop_explorer_circle_banner);
											labelBanner.setName("no-banner");
										}
									});
								}
								return null;
							}
						}.execute();
					}
				});
				menuItemA.setName("add");
				menuItemA.setActionCommand("add");

				JMenuItem menuItemR = new JMenuItem("Remove", Icon.desktop_explorer_circle_popup_remove);
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
								try {
									DataFile banner = DataStore.getBanner(tokenCircle.getId());
									banner.delete();
								} catch (DataStoreException dse) {
									LOG.error("Error deleting banner image from DataStore", dse);
								}
								return null;
							}
							@Override
							protected void done() {
								labelBanner.setIcon(Icon.desktop_explorer_circle_banner);
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
		editorWorks = new ListBook(tokenCircle);
		tabLists.addTab("Works", Icon.desktop_explorer_book, editorWorks);
		editorArtists = new ListArtist(tokenCircle);
		tabLists.addTab("Artists", Icon.desktop_explorer_artist, editorArtists);
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				editorWorks,
				editorArtists
		}));
		tabLists.doLayout();
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
			height = parent.getHeight(),
			hsize = 18;
		labelJapaneseName.setBounds(3, 3, 100, hsize);
		textJapaneseName.setBounds(103, 3, width - 106, hsize);
		labelTranslatedName.setBounds(3, 3 + hsize, 100, hsize);
		textTranslatedName.setBounds(103, 3 + hsize, width - 106, hsize);
		labelRomajiName.setBounds(3, 3 + hsize*2, 100, hsize);
		textRomajiName.setBounds(103, 3 + hsize*2, width - 106, hsize);
		labelWeblink.setBounds(3, 3 + hsize*3, 100, hsize);
		textWeblink.setBounds(103, 3 + hsize*3, width - 106, hsize);
		labelBanner.setBounds(3, 3 + hsize*4 + 3, 200, 40);
		tabLists.setBounds(3, 3 + hsize*4 + 40 + 6, width - 6, height - hsize*6 - 40 - 6);
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
			if(tokenCircle instanceof NullCircle)
				tokenCircle = DataBase.doInsert(Circle.class);
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
					if(DataBase.isAutocommit())
						DataBase.doCommit();
					return null;
				}
				@Override
				public void done() {
					buttonConfirm.setEnabled(true);
				}
			}.execute();
		} catch (DataBaseException dbe) {
			buttonConfirm.setEnabled(true);
			LOG.error("Error saving record [{}]", tokenCircle, dbe);
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
					if(tokenCircle instanceof NullCircle)
						return null;
					DataFile banner = DataStore.getBanner(tokenCircle.getId());
					InputStream in = banner.openInputStream();
					labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
					labelBanner.setName("banner");
					in.close();
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				} catch (DataStoreException dse) {
					LOG.error("Error loading banner image for [{}]", tokenCircle, dse);
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
				if(tokenCircle instanceof NullCircle)
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
	
	private final class NullCircle implements Circle
	{
		@Override
		public Integer getId() throws DataBaseException { return null; }

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
				public int size() throws DataBaseException { return 0; }
				
			};
		}

		@Override
		public String getJapaneseName() throws DataBaseException { return ""; }

		@Override
		public String getTranslatedName() throws DataBaseException { return ""; }

		@Override
		public String getRomajiName() throws DataBaseException { return ""; }

		@Override
		public String getWeblink() throws DataBaseException { return ""; }

		@Override
		public void setJapaneseName(String japaneseName) throws DataBaseException { }

		@Override
		public void setTranslatedName(String translatedName) throws DataBaseException { }

		@Override
		public void setRomajiName(String romajiName) throws DataBaseException { }

		@Override
		public void setWeblink(String weblink) throws DataBaseException { }
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Artist> getArtists() throws DataBaseException
		{
			return new RecordSet()
			{

				@Override
				public Iterator iterator() { return new java.util.ArrayList().iterator(); }

				@Override
				public int size() throws DataBaseException { return 0; }
				
			};
		}

		@Override
		public void addArtist(Artist artist) throws DataBaseException { }
		
		@Override
		public void addBook(Book book) throws DataBaseException { }

		@Override
		public void removeArtist(Artist artist) throws DataBaseException { }
		
		@Override
		public void removeBook(Book book) throws DataBaseException { }
		
		@Override
		public Set<String> getAliases() throws DataBaseException { return new java.util.TreeSet<String>(); }

		@Override
		public void addAlias(String alias) throws DataBaseException { }

		@Override
		public void removeAlias(String alias) throws DataBaseException { }

		@Override
		public void removeAll() throws DataBaseException { }

		@Override
		public int compareTo(Circle o) {
			return 1;
		}
	}
}