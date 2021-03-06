package org.dyndns.doujindb.ui.dialog;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.TabbedPaneUI;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.record.Artist;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.db.record.Circle;
import org.dyndns.doujindb.db.record.Content;
import org.dyndns.doujindb.db.record.Convention;
import org.dyndns.doujindb.db.record.Parody;
import org.dyndns.doujindb.db.record.Book.Type;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.dialog.util.*;
import org.dyndns.doujindb.ui.dialog.util.combobox.*;
import org.dyndns.doujindb.ui.dialog.util.list.*;
import org.dyndns.doujindb.util.ImageTool;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class PanelBook extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	private Book tokenBook;
	
	private JPanel panelInfo;
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomajiName;
	private JTextField textRomajiName;
	private JLabel labelInfo;
	private JTextArea textInfo;
	private JScrollPane scrollInfo;
	private JLabel labelPreview;
	private JLabel labelDate;
	private JTextField textDate;
	private JLabel labelPages;
	private JTextField textPages;
	private JLabel labelType;
	private JComboBox<Book.Type> comboType;
	private JLabel labelConvention;
	private ComboBoxConvention comboConvention;
	private JCheckBox checkAdult;
	private BookRatingEditor editorRating;
	private JTabbedPane tabLists;
	private ListArtist editorArtists;
	private ListCircle editorCircles;
	private ListContent editorContents;
	private ListParody editorParodies;
	private PanelBookMedia mediaManager;
	private JButton buttonConfirm;
	
	protected static final Font font = UI.Font;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PanelBook.class);
	
	public PanelBook(Book token) throws DataBaseException
	{
		tokenBook = (token == null ? new NullBook() : token);
		super.setLayout(this);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
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
		labelInfo = new JLabel("Info");
		labelInfo.setFont(font);
		textInfo = new JTextArea("");
		textInfo.setFont(font);
		scrollInfo = new JScrollPane(textInfo);
		labelPreview = new JLabel(Icon.desktop_explorer_book_cover);
		labelPreview.setName("no-preview");
		labelPreview.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(tokenBook.isRecycled())
					return;
				if(tokenBook instanceof NullBook)
					return;
				
	    		JPopupMenu popupMenu = new JPopupMenu();
	    		
	    		JMenuItem menuItemA = new JMenuItem("Add", Icon.desktop_explorer_book_popup_add);
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
								if(fc.showOpenDialog(PanelBook.this) != JFileChooser.APPROVE_OPTION)
									return null;
								BufferedImage image = null;
								File imageFile = fc.getSelectedFile();
								try {
									image = javax.imageio.ImageIO.read(imageFile);
								} catch (IOException ioe) {
									LOG.error("Error loading cover image for [{}] from file [{}]", new Object[]{tokenBook, imageFile, ioe});
								}
								if(image == null)
									return null;
								try {
									DataFile thumbnail = DataStore.getThumbnail(tokenBook.getId());
									OutputStream out = thumbnail.openOutputStream();
									javax.imageio.ImageIO.write(image = ImageTool.getScaledInstance(image, 256, 256, true), "PNG", out);
									
									final ImageIcon ii = new ImageIcon(image);
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelPreview.setIcon(ii);
											labelPreview.setName("preview");
										}
									});
									
									out.close();
								} catch (DataStoreException dse) {
									LOG.error("Error storing cover image in DataStore", dse);
									
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelPreview.setIcon(Icon.desktop_explorer_book_cover);
											labelPreview.setName("no-preview");
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

				JMenuItem menuItemR = new JMenuItem("Remove", Icon.desktop_explorer_book_popup_remove);
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
									DataFile thumbnail = DataStore.getThumbnail(tokenBook.getId());
									thumbnail.delete();
								} catch (DataStoreException dse) {
									LOG.error("Error deleting cover image from DataStore", dse);
								}
								return null;
							}
							@Override
							protected void done() {
								labelPreview.setIcon(Icon.desktop_explorer_book_cover);
								labelPreview.setName("no-preview");
						    }
						}.execute();
					}
				});
				menuItemR.setName("remove");
				menuItemR.setActionCommand("remove");
				
				if(labelPreview.getName().equals("no-preview"))
					popupMenu.add(menuItemA);
				else
					popupMenu.add(menuItemR);
				
				popupMenu.show(me.getComponent(), me.getX(), me.getY());
			}
		});
		labelType = new JLabel("Type");
		labelType.setFont(font);
		comboType = new JComboBox<Book.Type>();
		comboType.setFont(font);
		comboType.setFocusable(false);
		labelConvention = new JLabel("Convention");
		comboConvention = new ComboBoxConvention();
		comboConvention.setFont(font);
		comboConvention.setFocusable(true);
		editorRating = new BookRatingEditor(tokenBook.getRating());
		checkAdult = new JCheckBox("Adult", false);
		checkAdult.setFont(font);
		checkAdult.setFocusable(false);
		labelDate = new JLabel("Date");
		labelDate.setFont(font);
		textDate = new JTextField("");
		textDate.setFont(font);
		labelPages = new JLabel("Pages");
		labelPages.setFont(font);
		textPages = new JTextField("");
		textPages.setFont(font);
		panelInfo = new JPanel();
		panelInfo.setLayout(new LayoutManager()
		{
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
				labelConvention.setBounds(3, 3 + hsize*3, 100, hsize);
				comboConvention.setBounds(103, 3 + hsize*3, width - 106, hsize);
				labelType.setBounds(3, 3 + hsize*4, 100, hsize);
				comboType.setBounds(103, 3 + hsize*4, 100, hsize);
				editorRating.setBounds(width - editorRating.getPreferredSize().width - 3, 3 + hsize*4, editorRating.getPreferredSize().width, editorRating.getPreferredSize().height);
				labelDate.setBounds(3, 3 + hsize*5, 80, hsize);
				textDate.setBounds(3, 3 + hsize*6, 80, hsize);
				labelPages.setBounds(3, 3 + hsize*7, 60, hsize);
				textPages.setBounds(3, 3 + hsize*8, 60, hsize);
				checkAdult.setBounds(3, 3 + hsize*9, 100, hsize);
				labelInfo.setBounds(3, 3 + hsize*5 + 256, 60, hsize);
				scrollInfo.setBounds(3, 3 + hsize*6 + 256, width - 6, height - hsize*6 - 256 - 10);
				labelPreview.setBounds(width - 6 - 256, hsize*6, 256, 256);
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
		panelInfo.add(labelJapaneseName);
		panelInfo.add(textJapaneseName);
		panelInfo.add(labelTranslatedName);
		panelInfo.add(textTranslatedName);
		panelInfo.add(labelRomajiName);
		panelInfo.add(textRomajiName);
		panelInfo.add(labelInfo);
		panelInfo.add(scrollInfo);
		panelInfo.add(editorRating);
		panelInfo.add(checkAdult);
		panelInfo.add(labelConvention);
		panelInfo.add(comboConvention);
		panelInfo.add(labelDate);
		panelInfo.add(textDate);
		panelInfo.add(labelPages);
		panelInfo.add(textPages);
		panelInfo.add(labelType);
		panelInfo.add(comboType);
		panelInfo.add(labelPreview);
		tabLists.addTab("General", Icon.desktop_explorer_book_info, panelInfo);
		editorArtists = new ListArtist(tokenBook);
		tabLists.addTab("Artists", Icon.desktop_explorer_artist, editorArtists);
		editorCircles = new ListCircle(tokenBook);
		tabLists.addTab("Circles", Icon.desktop_explorer_circle, editorCircles);
		editorContents = new ListContent(tokenBook);
		tabLists.addTab("Contents", Icon.desktop_explorer_content, editorContents);
		editorParodies = new ListParody(tokenBook);
		tabLists.addTab("Parodies", Icon.desktop_explorer_parody, editorParodies);
		mediaManager = new PanelBookMedia(tokenBook);
		tabLists.addTab("Media", Icon.desktop_explorer_book_media, mediaManager);
		if(tokenBook instanceof NullBook)
		{
			labelPreview.setEnabled(false);
			tabLists.setEnabledAt(tabLists.getTabCount()-1, false);
		}
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				null,
				editorArtists,
				editorCircles,
				editorContents,
				editorParodies,
				null
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
		super.add(tabLists);
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		super.add(buttonConfirm);
		
		loadData();
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		tabLists.setBounds(1, 1, width - 6, height - 30);
		buttonConfirm.setBounds(width / 2 - 40, height - 25, 80,  20);
	}
	
	public Book getRecord()
	{
		return tokenBook;
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
		java.util.Date date = null;
		int pages = 0;
		_pages:
		{
			try {
				if(textPages.getText().equals(""))
					break _pages;
				pages = Integer.parseInt(textPages.getText());
			} catch(NumberFormatException nfe) {
				final Border brd1 = textPages.getBorder();
				final Border brd2 = BorderFactory.createLineBorder(Color.ORANGE);
				final Timer tmr = new Timer(100, new AbstractAction () {
					boolean hasBorder = true;
					int count = 0;
					public void actionPerformed (ActionEvent e) {
						if(count++ > 4)
							((javax.swing.Timer)e.getSource()).stop();
						if (hasBorder)
							textPages.setBorder(brd2);
						else
							textPages.setBorder(brd1);
						hasBorder = !hasBorder;
					}
				});
				tmr.start();
				buttonConfirm.setEnabled(true);
				return;
			}
		}
		if(!textDate.getText().equals("--/--/----"))
		try {
			date = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(textDate.getText());
		} catch(ParseException pe) {
			final Border brd1 = textDate.getBorder();
			final Border brd2 = BorderFactory.createLineBorder(Color.ORANGE);
			final Timer tmr = new Timer(100, new AbstractAction () {
				boolean hasBorder = true;
				int count = 0;
				public void actionPerformed (ActionEvent e) {
					if(count++ > 4)
						((javax.swing.Timer)e.getSource()).stop();
					if (hasBorder)
						textDate.setBorder(brd2);
					else
						textDate.setBorder(brd1);
					hasBorder = !hasBorder;
				}
			});
			tmr.start();
		}
		if(date == null && !textDate.getText().equals("--/--/----")) {
			buttonConfirm.setEnabled(true);
			return;
		}
		try {
			if(tokenBook instanceof NullBook)
				tokenBook = DataBase.doInsert(Book.class);
			else {
				labelPreview.setEnabled(true);
				tabLists.setEnabledAt(tabLists.getTabCount()-1, true);
			}
			tokenBook.setJapaneseName(textJapaneseName.getText());
			tokenBook.setTranslatedName(textTranslatedName.getText());
			tokenBook.setRomajiName(textRomajiName.getText());
			tokenBook.setInfo(textInfo.getText());
			tokenBook.setDate(date);
			tokenBook.setRating(editorRating.getRating());
			tokenBook.setConvention((Convention)comboConvention.getSelectedItem());
			tokenBook.setType((Type)comboType.getSelectedItem());
			tokenBook.setPages(pages);
			tokenBook.setAdult(checkAdult.isSelected());
			for(Artist a : tokenBook.getArtists())
				if(!editorArtists.contains(a))
					tokenBook.removeArtist(a);
			java.util.Iterator<Artist> Artists = editorArtists.iterator();
			while(Artists.hasNext())
				tokenBook.addArtist(Artists.next());
			for(Circle c : tokenBook.getCircles())
				if(!editorCircles.contains(c))
					tokenBook.removeCircle(c);
			java.util.Iterator<Circle> circles = editorCircles.iterator();
			while(circles.hasNext())
				tokenBook.addCircle(circles.next());
			for(Content c : tokenBook.getContents())
				if(!editorContents.contains(c))
					tokenBook.removeContent(c);
			java.util.Iterator<Content> contents = editorContents.iterator();
			while(contents.hasNext())
				tokenBook.addContent(contents.next());
			for(Parody p : tokenBook.getParodies())
				if(!editorParodies.contains(p))
					tokenBook.removeParody(p);
			java.util.Iterator<Parody> parodies = editorParodies.iterator();
			while(parodies.hasNext())
				tokenBook.addParody(parodies.next());
			
			new SwingWorker<Void, Object>() {
				@Override
				public Void doInBackground() {
					if(DataBase.isAutocommit())
						DataBase.doCommit();
					if(tokenBook instanceof NullBook)
					{
						labelPreview.setEnabled(true);
						tabLists.setEnabledAt(tabLists.getTabCount()-1, true);
					}
					return null;
				}
				@Override
				public void done() {
					buttonConfirm.setEnabled(true);
				}
			}.execute();
		} catch (DataBaseException dbe) {
			buttonConfirm.setEnabled(true);
			LOG.error("Error saving record [{}]", tokenBook, dbe);
		}
	}
	
	private void loadData()
	{
		new SwingWorker<Void, Object>()
		{
			@Override
			public Void doInBackground()
			{
				textJapaneseName.setText(tokenBook.getJapaneseName());
				textTranslatedName.setText(tokenBook.getTranslatedName());
				textRomajiName.setText(tokenBook.getRomajiName());
				textInfo.setText(tokenBook.getInfo());
				comboType.removeAllItems();
				for(Type tokenType : Type.values())
					comboType.addItem(tokenType);
				comboType.setSelectedItem(tokenBook.getType());
				comboConvention.setSelectedItem(tokenBook.getConvention());
				checkAdult.setSelected(tokenBook.isAdult());
				textDate.setText(((tokenBook.getDate()==null)?"--/--/----":new java.text.SimpleDateFormat("dd/MM/yyyy").format(tokenBook.getDate())));
				textPages.setText("" + tokenBook.getPages());
				try
				{
					if(tokenBook instanceof NullBook)
						return null;
					DataFile cover = DataStore.getThumbnail(tokenBook.getId());
					InputStream in = cover.openInputStream();
					labelPreview.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
					labelPreview.setName("preview");
					in.close();
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				} catch (DataStoreException dse) {
					LOG.error("Error loading cover image for [{}]", tokenBook, dse);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	@Override
	public void recordAdded(Record rcd) {}
	
	@Override
	public void recordDeleted(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordDeleted(rcd);
		if(rcd instanceof Artist)
			editorArtists.recordDeleted(rcd);
		if(rcd instanceof Content)
			editorContents.recordDeleted(rcd);
		if(rcd instanceof Parody)
			editorParodies.recordDeleted(rcd);
		loadData();
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case PROPERTY:
			if(data.getProperty().equals("japanese_name"))
				textJapaneseName.setText(tokenBook.getJapaneseName());
			if(data.getProperty().equals("translated_name"))
				textTranslatedName.setText(tokenBook.getTranslatedName());
			if(data.getProperty().equals("romaji_name"))
				textRomajiName.setText(tokenBook.getRomajiName());
			if(data.getProperty().equals("info"))
				textInfo.setText(tokenBook.getInfo());
			if(data.getProperty().equals("type"))
				comboType.setSelectedItem(tokenBook.getType());
			if(data.getProperty().equals("adult"))
				checkAdult.setSelected(tokenBook.isAdult());
			if(data.getProperty().equals("released"))
				textDate.setText(((tokenBook.getDate()==null)?"--/--/----":new java.text.SimpleDateFormat("dd/MM/yyyy").format(tokenBook.getDate())));
			if(data.getProperty().equals("pages"))
				textPages.setText("" + tokenBook.getPages());
			break;
		//case LINK:
		//case UNLINK:
		default:
			if(data.getTarget() instanceof Circle)
				editorCircles.recordUpdated(rcd, data);
			if(data.getTarget() instanceof Artist)
				editorArtists.recordUpdated(rcd, data);
			if(data.getTarget() instanceof Content)
				editorContents.recordUpdated(rcd, data);
			if(data.getTarget() instanceof Parody)
				editorParodies.recordUpdated(rcd, data);
		}
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordRecycled(rcd);
		if(rcd instanceof Artist)
			editorArtists.recordRecycled(rcd);
		if(rcd instanceof Content)
			editorContents.recordRecycled(rcd);
		if(rcd instanceof Parody)
			editorParodies.recordRecycled(rcd);
		loadData();
	}
	
	@Override
	public void recordRestored(Record rcd)
	{
		if(rcd instanceof Circle)
			editorCircles.recordRestored(rcd);
		if(rcd instanceof Artist)
			editorArtists.recordRestored(rcd);
		if(rcd instanceof Content)
			editorContents.recordRestored(rcd);
		if(rcd instanceof Parody)
			editorParodies.recordRestored(rcd);
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
	
	private final class NullBook implements Book
	{
		@Override
		public Integer getId() throws DataBaseException { return null; }

		@Override
		public void doRecycle() throws DataBaseException { }

		@Override
		public void doRestore() throws DataBaseException { }

		@Override
		public boolean isRecycled() throws DataBaseException { return false; }

		@Override
		public String getJapaneseName() throws DataBaseException { return ""; }

		@Override
		public String getTranslatedName() throws DataBaseException { return ""; }

		@Override
		public String getRomajiName() throws DataBaseException { return ""; }

		@Override
		public void setJapaneseName(String japaneseName) throws DataBaseException { }

		@Override
		public void setTranslatedName(String translatedName) throws DataBaseException { }

		@Override
		public void setRomajiName(String romajiName) throws DataBaseException { }

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Circle> getCircles() throws DataBaseException
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
		public Date getDate() throws DataBaseException { return new Date(); }

		@Override
		public Type getType() throws DataBaseException { return Type.不詳; }

		@Override
		public int getPages() throws DataBaseException { return 0; }

		@Override
		public void setPages(int pages) throws DataBaseException { }

		@Override
		public void setDate(Date date) throws DataBaseException { }

		@Override
		public void setType(Type type) throws DataBaseException { }

		@Override
		public boolean isAdult() throws DataBaseException { return false; }

		@Override
		public void setAdult(boolean adult) throws DataBaseException { }

		@Override
		public Rating getRating() throws DataBaseException { return Rating.UNRATED; }

		@Override
		public String getInfo() throws DataBaseException { return ""; }

		@Override
		public void setRating(Rating rating) throws DataBaseException { }

		@Override
		public void setInfo(String info) throws DataBaseException { }

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

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Content> getContents() throws DataBaseException
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
		public Convention getConvention() throws DataBaseException { return null; }

		@Override
		public void setConvention(Convention convention) throws DataBaseException { }

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public RecordSet<Parody> getParodies() throws DataBaseException
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
		public void addCircle(Circle circle) throws DataBaseException { }

		@Override
		public void addContent(Content content) throws DataBaseException { }

		@Override
		public void addParody(Parody parody) throws DataBaseException { }

		@Override
		public void removeArtist(Artist artist) throws DataBaseException { }
		
		@Override
		public void removeCircle(Circle circle) throws DataBaseException { }

		@Override
		public void removeContent(Content content) throws DataBaseException { }

		@Override
		public void removeParody(Parody parody) throws DataBaseException { }
		
		@Override
		public Set<String> getAliases() throws DataBaseException { return new java.util.TreeSet<String>(); }

		@Override
		public void addAlias(String alias) throws DataBaseException { }

		@Override
		public void removeAlias(String alias) throws DataBaseException { }

		@Override
		public void removeAll() throws DataBaseException { }

		@Override
		public int compareTo(Book o) {
			return 1;
		}
	}
}