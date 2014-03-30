package org.dyndns.doujindb.ui.desk.panels;

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
import java.util.Comparator;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.TabbedPaneUI;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.db.records.Book.Type;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.util.ComboBoxConvention;
import org.dyndns.doujindb.ui.desk.panels.util.RecordList;
import org.dyndns.doujindb.ui.desk.panels.util.TabbedPaneUIEx;
import org.dyndns.doujindb.util.ImageTool;
import org.dyndns.doujindb.util.RepositoryIndexer;

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
	private JCheckBox checkDecensored;
	private JCheckBox checkTranslated;
	private JCheckBox checkColored;
	private BookRatingEditor editorRating;
	private JTabbedPane tabLists;
	private RecordArtistEditor editorArtists;
	private RecordCircleEditor editorCircles;
	private RecordContentEditor editorContents;
	private RecordParodyEditor editorParodies;
	private PanelBookMedia mediaManager;
	private JButton buttonConfirm;
	
	protected static final Font font = UI.Font;
	
	public PanelBook(Book token) throws DataBaseException
	{
		tokenBook = token;		
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
				if(tokenBook.getID() == null)
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
									Logger.logError(ioe.getMessage(), ioe);
								}
								if(img == null)
									return null;
								try
								{
									DataFile ds = Core.Repository.child(tokenBook.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenBook.getID());
									ds.touch();
									OutputStream out = ds.getOutputStream();
									File in = fc.getSelectedFile();
									BufferedImage image = ImageTool.read(in);
									ImageTool.write(ImageTool.getScaledInstance(image, 256, 256, true), out);
									out.close();
								} catch (Exception e) {
									Logger.logError(e.getMessage(), e);
								}
								try
								{
									DataFile ds = Core.Repository.child(tokenBook.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenBook.getID());
									if(ds.exists())
									{
										InputStream in = ds.getInputStream();
										final ImageIcon ii = new ImageIcon(ImageTool.read(in));
										SwingUtilities.invokeLater(new Runnable()
										{
											@Override
											public void run() {
												labelPreview.setIcon(ii);
												labelPreview.setName("preview");
											}
										});
										in.close();
									}
								} catch (NullPointerException | IOException | DataBaseException e) {
									SwingUtilities.invokeLater(new Runnable()
									{
										@Override
										public void run() {
											labelPreview.setIcon(Icon.desktop_explorer_book_cover);
											labelPreview.setName("no-preview");
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
								try
								{
									DataFile df = Core.Repository.child(tokenBook.getID());
									df.mkdir();
									df = Core.Repository.getPreview(tokenBook.getID());
									df.delete();
								} catch (NullPointerException npe) {
								} catch (Exception e)
								{
									Logger.logError(e.getMessage(), e);
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
		comboConvention.addItem(null);
		editorRating = new BookRatingEditor(tokenBook.getRating());
		checkAdult = new JCheckBox("Adult", false);
		checkAdult.setFont(font);
		checkAdult.setFocusable(false);
		checkDecensored = new JCheckBox("Decensored", false);
		checkDecensored.setFont(font);
		checkDecensored.setFocusable(false);
		checkTranslated = new JCheckBox("Translated", false);
		checkTranslated.setFont(font);
		checkTranslated.setFocusable(false);
		checkColored = new JCheckBox("Colored", false);
		checkColored.setFont(font);
		checkColored.setFocusable(false);
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
				height = parent.getHeight();
				labelJapaneseName.setBounds(3, 3, 100, 15);
				textJapaneseName.setBounds(103, 3, width - 106, 15);
				labelTranslatedName.setBounds(3, 3 + 15, 100, 15);
				textTranslatedName.setBounds(103, 3 + 15, width - 106, 15);
				labelRomajiName.setBounds(3, 3 + 30, 100, 15);
				textRomajiName.setBounds(103, 3 + 30, width - 106, 15);
				labelConvention.setBounds(3, 3 + 45, 100, 20);
				comboConvention.setBounds(103, 3 + 45, width - 106, 20);				
				labelType.setBounds(3, 3 + 65, 100, 20);
				comboType.setBounds(103, 3 + 65, 100, 20);				
				checkAdult.setBounds(3, 3 + 90, 100, 15);
				checkDecensored.setBounds(3, 3 + 105, 100, 15);
				checkTranslated.setBounds(3, 3 + 120, 100, 15);
				checkColored.setBounds(3, 3 + 135, 100, 15);
				editorRating.setBounds(width - 86 - 2, 80, 80, 15);
				labelDate.setBounds(3, 3 + 155, 80, 15);
				textDate.setBounds(3, 3 + 170, 80, 15);
				labelPages.setBounds(3, 3 + 185, 60, 15);
				textPages.setBounds(3, 3 + 200, 60, 15);
				labelInfo.setBounds(3, 3 + 355, 60, 15);
				scrollInfo.setBounds(3, 3 + 370, width - 6, height - 375);
				labelPreview.setBounds(width - 6 - 256, 100, 256, 256);
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
		panelInfo.add(checkDecensored);
		panelInfo.add(checkTranslated);
		panelInfo.add(checkColored);
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
		editorArtists = new RecordArtistEditor(tokenBook);
		tabLists.addTab("Artists", Icon.desktop_explorer_artist, editorArtists);
		editorCircles = new RecordCircleEditor(tokenBook);
		tabLists.addTab("Circles", Icon.desktop_explorer_circle, editorCircles);
		editorContents = new RecordContentEditor(tokenBook);
		tabLists.addTab("Contents", Icon.desktop_explorer_content, editorContents);
		editorParodies = new RecordParodyEditor(tokenBook);
		tabLists.addTab("Parodies", Icon.desktop_explorer_parody, editorParodies);
		mediaManager = new PanelBookMedia(tokenBook);
		tabLists.addTab("Media", Icon.desktop_explorer_book_media, mediaManager);
		if(tokenBook.getID() == null)
		{
			labelPreview.setEnabled(false);
			tabLists.setEnabledAt(tabLists.getTabCount()-1, false);
		}
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				null,
				editorArtists.getRecordList(),
				editorCircles.getRecordList(),
				editorContents.getRecordList(),
				editorParodies.getRecordList(),
				null
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
			try
			{
				if(textPages.getText().equals(""))
					break _pages;
				pages = Integer.parseInt(textPages.getText());
			}catch(NumberFormatException nfe){
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
		try
		{
			date = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(textDate.getText());
		} catch(ParseException pe)
		{
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
		} catch (DataBaseException dbe) {
			Logger.logError(dbe.getMessage(), dbe);
			dbe.printStackTrace();
		}
		if(date == null && !textDate.getText().equals("--/--/----"))
		{
			buttonConfirm.setEnabled(true);
			return;
		}
		try
		{
			if(tokenBook.getID() == null)
				tokenBook = Core.Database.doInsert(Book.class);
			else
				{
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
			tokenBook.setDecensored(checkDecensored.isSelected());
			tokenBook.setTranslated(checkTranslated.isSelected());
			tokenBook.setColored(checkColored.isSelected());
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
					if(tokenBook.getID() != null)
						try {
							RepositoryIndexer.index(tokenBook);
						} catch (RepositoryException re) {
							re.printStackTrace();
						}
					if(Core.Database.isAutocommit())
						Core.Database.doCommit();
					if(tokenBook.getID() != null)
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
			Logger.logError(dbe.getMessage(), dbe);
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
				textJapaneseName.setText(tokenBook.getJapaneseName());
				textTranslatedName.setText(tokenBook.getTranslatedName());
				textRomajiName.setText(tokenBook.getRomajiName());
				textInfo.setText(tokenBook.getInfo());
				comboType.removeAllItems();
				for(Type tokenType : Type.values())
					comboType.addItem(tokenType);
				comboType.setSelectedItem(tokenBook.getType());
				Iterator<Convention> i = Core.Database.getConventions(null).iterator();
				TreeSet<Convention> set = new TreeSet<Convention>(new Comparator<Convention>()
				{
					@Override
					public int compare(Convention c1, Convention c2) {
						return c1.getTagName().compareTo(c2.getTagName());
					}
				});
				while(i.hasNext())
					set.add(i.next());
				comboConvention.removeAllItems();
				for(Convention conv : set)
					comboConvention.addItem(conv);
				comboConvention.setSelectedItem(tokenBook.getConvention());
				checkAdult.setSelected(tokenBook.isAdult());
				checkDecensored.setSelected(tokenBook.isDecensored());
				checkTranslated.setSelected(tokenBook.isTranslated());
				checkColored.setSelected(tokenBook.isColored());
				textDate.setText(((tokenBook.getDate()==null)?"--/--/----":new java.text.SimpleDateFormat("dd/MM/yyyy").format(tokenBook.getDate())));
				textPages.setText("" + tokenBook.getPages());
				try
				{
					if(tokenBook.getID() == null)
						return null;
					DataFile ds = Core.Repository.child(tokenBook.getID());
					ds.mkdir();
					ds = Core.Repository.getPreview(tokenBook.getID());
					if(ds.exists())
					{
						InputStream in = ds.getInputStream();
						labelPreview.setIcon(new ImageIcon(ImageTool.read(in)));
						labelPreview.setName("preview");
						in.close();
					}
				} catch (NullPointerException npe) {
					npe.printStackTrace();
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
			if(data.getProperty().equals("decensored"))
				checkDecensored.setSelected(tokenBook.isDecensored());
			if(data.getProperty().equals("translated"))
				checkTranslated.setSelected(tokenBook.isTranslated());
			if(data.getProperty().equals("color"))
				checkColored.setSelected(tokenBook.isColored());
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
}