package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.DataFile;
import org.dyndns.doujindb.dat.RepositoryException;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.db.records.Book.Rating;
import org.dyndns.doujindb.db.records.Book.Type;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.utils.DouzCheckBoxList;
import org.dyndns.doujindb.ui.desk.panels.utils.DouzTabbedPaneUI;

@SuppressWarnings("serial")
public final class PanelBook implements Validable, LayoutManager, ActionListener
{
	private DouzWindow parentWindow;
	private Book tokenBook;
	
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomanjiName;
	private JTextField textRomanjiName;
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
	private JComboBox<Convention> comboConvention;
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
	
	public PanelBook(DouzWindow parent, JComponent pane, Book token) throws DataBaseException
	{
		parentWindow = parent;
		
		if(token != null)
			tokenBook = token;
		else
			tokenBook = new NullBook();
		
		pane.setLayout(this);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		labelJapaneseName = new JLabel("Japanese Name");
		labelJapaneseName.setFont(font);
		textJapaneseName = new JTextField(tokenBook.getJapaneseName());
		textJapaneseName.setFont(font);
		labelTranslatedName = new JLabel("Translated Name");
		labelTranslatedName.setFont(font);
		textTranslatedName = new JTextField(tokenBook.getTranslatedName());
		textTranslatedName.setFont(font);
		labelRomanjiName = new JLabel("Romanji Name");
		labelRomanjiName.setFont(font);
		textRomanjiName = new JTextField(tokenBook.getRomanjiName());
		textRomanjiName.setFont(font);
		labelInfo = new JLabel("Info");
		labelInfo.setFont(font);
		textInfo = new JTextArea(tokenBook.getInfo());
		textInfo.setFont(font);
		scrollInfo = new JScrollPane(textInfo);
		labelPreview = new JLabel(Core.Resources.Icons.get("JDesktop/Explorer/Book/Cover"));
		labelPreview.setName("no-preview");
		if(tokenBook.getID() == null)
			labelPreview.setEnabled(false);
		else
		try
		{
			DataFile ds = Core.Repository.child(tokenBook.getID());
			ds.mkdir();
			ds = Core.Repository.getPreview(tokenBook.getID()); //ds.child(".preview");
			if(ds.exists())
			{
				InputStream in = ds.getInputStream();
				labelPreview.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
				labelPreview.setName("preview");
				in.close();
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
		}
		labelPreview.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(tokenBook.isRecycled())
					return;
				if(tokenBook.getID() == null)
					return;
				if(me.getButton() == MouseEvent.BUTTON3)
				{
					JLabel lab = (JLabel) me.getSource();
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					final boolean isAdd = lab.getName().equals("no-preview");
					if(isAdd)
						tbl.put("Add Preview", Core.Resources.Icons.get("JDesktop/Explorer/Circle/Popup/Add"));
					else
						tbl.put("Remove Preview", Core.Resources.Icons.get("JDesktop/Explorer/Circle/Popup/Remove"));
					final DouzPopupMenu pop = new DouzPopupMenu("", tbl);
					pop.show(lab, me.getX(), me.getY());
					new Thread(getClass().getName()+"/PopupMenu")
					{
						@Override
						public void run()
						{
							while(pop.isValid())
								try { sleep(1); } catch (InterruptedException e) { }
							if(pop.getResult() == DouzPopupMenu.SELECTION_CANCELED)
								return;
							if(isAdd)
							{
								JFileChooser fc = Core.UI.getFileChooser();
								fc.setMultiSelectionEnabled(false);
								int result = fc.showOpenDialog(Core.UI);
								if(result != JFileChooser.APPROVE_OPTION)
									return;
								Image img = null;
								try { img = javax.imageio.ImageIO.read(fc.getSelectedFile()); } catch (IOException ioe)
								{
									Core.Logger.log(ioe.getMessage(), Level.WARNING);
								}
								if(img == null)
									return;
								try
								{
									DataFile ds = Core.Repository.child(tokenBook.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenBook.getID()); //ds.child(".preview");
									ds.touch();
									OutputStream out = ds.getOutputStream();
									File in = fc.getSelectedFile();
									BufferedImage image = javax.imageio.ImageIO.read(in);
									int wi = image.getWidth(null),
									hi = image.getHeight(null),
									wl = 256, 
									hl = 256; 
									if(!(wi < wl) && !(hi < hl)) // Cannot scale an image smaller than 256x256, or getScaledInstance is going to loop
										if ((double)wl/wi > (double)hl/hi)
										{
											wi = (int) (wi * (double)hl/hi);
											hi = (int) (hi * (double)hl/hi);
										}else{
											hi = (int) (hi * (double)wl/wi);
											wi = (int) (wi * (double)wl/wi);
										}
									javax.imageio.ImageIO.write(org.dyndns.doujindb.util.Image.getScaledInstance(image, wi, hi, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true), "PNG", out);
									out.close();
								} catch (Exception e) {
									e.printStackTrace();
									Core.Logger.log(e.getMessage(), Level.WARNING);
								}
								try
								{
									DataFile ds = Core.Repository.child(tokenBook.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenBook.getID()); //ds.child(".preview");
									if(ds.exists())
									{
										InputStream in = ds.getInputStream();
										labelPreview.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
										in.close();
									}
								} catch (NullPointerException npe) {
								} catch (IOException e) {
									e.printStackTrace();
									//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
								} catch (RepositoryException dbe) {
									dbe.printStackTrace();
								} catch (DataBaseException dbe) {
									dbe.printStackTrace();
								}
								labelPreview.setName("preview");
							}
							else
							{
								try
								{
									DataFile ds = Core.Repository.child(tokenBook.getID());
									ds.mkdir();
									ds = Core.Repository.getPreview(tokenBook.getID()); //ds.child(".preview");
									ds.delete();
								} catch (NullPointerException npe) {
								} catch (Exception e)
								{
									e.printStackTrace();
									//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
								}
								labelPreview.setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Book/Cover"));
								labelPreview.setName("no-preview");
							}								
						}
					}.start();
				 }
			  }
		});
		labelType = new JLabel("Type");
		labelType.setFont(font);
		comboType = new JComboBox<Book.Type>();
		comboType.setFont(font);
		comboType.setFocusable(false);
		for(Type tokenType : Type.values())
			comboType.addItem(tokenType);
		comboType.setSelectedItem(tokenBook.getType());
		labelConvention = new JLabel("Convention");
		comboConvention = new JComboBox<Convention>();
		comboConvention.setFont(font);
		comboConvention.setFocusable(false);
		comboConvention.addItem(null);
		for(Convention conv : Core.Database.getConventions(null))
			comboConvention.addItem(conv);
		comboConvention.setSelectedItem(tokenBook.getConvention());
		editorRating = new BookRatingEditor(tokenBook.getRating());
		checkAdult = new JCheckBox("Adult", tokenBook.isAdult());
		checkAdult.setFont(font);
		checkAdult.setFocusable(false);
		checkDecensored = new JCheckBox("Decensored", tokenBook.isDecensored());
		checkDecensored.setFont(font);
		checkDecensored.setFocusable(false);
		checkTranslated = new JCheckBox("Translated", tokenBook.isTranslated());
		checkTranslated.setFont(font);
		checkTranslated.setFocusable(false);
		checkColored = new JCheckBox("Colored", tokenBook.isColored());
		checkColored.setFont(font);
		checkColored.setFocusable(false);
		labelDate = new JLabel("Date");
		labelDate.setFont(font);
		textDate = new JTextField(((tokenBook.getDate()==null)?"--/--/----":new java.text.SimpleDateFormat("dd/MM/yyyy").format(tokenBook.getDate())));
		textDate.setFont(font);
		labelPages = new JLabel("Pages");
		labelPages.setFont(font);
		textPages = new JTextField("" + tokenBook.getPages());
		textPages.setFont(font);
		JPanel rootInfo = new JPanel();
		rootInfo.setLayout(new LayoutManager()
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
				labelRomanjiName.setBounds(3, 3 + 30, 100, 15);
				textRomanjiName.setBounds(103, 3 + 30, width - 106, 15);
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
		rootInfo.add(labelJapaneseName);
		rootInfo.add(textJapaneseName);
		rootInfo.add(labelTranslatedName);
		rootInfo.add(textTranslatedName);
		rootInfo.add(labelRomanjiName);
		rootInfo.add(textRomanjiName);
		rootInfo.add(labelInfo);
		rootInfo.add(scrollInfo);
		rootInfo.add(editorRating);
		rootInfo.add(checkAdult);
		rootInfo.add(checkDecensored);
		rootInfo.add(checkTranslated);
		rootInfo.add(checkColored);
		rootInfo.add(labelConvention);
		rootInfo.add(comboConvention);
		rootInfo.add(labelDate);
		rootInfo.add(textDate);
		rootInfo.add(labelPages);
		rootInfo.add(textPages);
		rootInfo.add(labelType);
		rootInfo.add(comboType);
		tabLists.addTab("General", Core.Resources.Icons.get("JDesktop/Explorer/Book/Info"), rootInfo);
		editorArtists = new RecordArtistEditor(tokenBook);
		tabLists.addTab("Artist", Core.Resources.Icons.get("JDesktop/Explorer/Artist"), editorArtists);
		editorCircles = new RecordCircleEditor(tokenBook);
		editorCircles.setEnabled(false);
		tabLists.addTab("Circles", Core.Resources.Icons.get("JDesktop/Explorer/Circle"), editorCircles);
		editorContents = new RecordContentEditor(tokenBook);
		tabLists.addTab("Contents", Core.Resources.Icons.get("JDesktop/Explorer/Content"), editorContents);
		editorParodies = new RecordParodyEditor(tokenBook);
		tabLists.addTab("Parodies", Core.Resources.Icons.get("JDesktop/Explorer/Parody"), editorParodies);
		if(token != null)
		{
			mediaManager = new PanelBookMedia(tokenBook);
			rootInfo.add(labelPreview);
			tabLists.addTab("Media", Core.Resources.Icons.get("JDesktop/Explorer/Book/Media"), mediaManager);
		} else {
			tabLists.addTab("Media", Core.Resources.Icons.get("JDesktop/Explorer/Book/Media"), new JPanel());
			tabLists.setEnabledAt(tabLists.getTabCount()-1, false);
		}
		tabLists.setUI(new DouzTabbedPaneUI(new DouzCheckBoxList<?>[]{
				null,
				editorArtists.getCheckBoxList(),
				editorCircles.getCheckBoxList(),
				editorContents.getCheckBoxList(),
				editorParodies.getCheckBoxList(),
				null
		}));
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		pane.add(tabLists);
		pane.add(buttonConfirm);
		validateUI(new DouzEvent(DouzEvent.Type.DATABASE_REFRESH, null));
	}
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
		height = parent.getHeight();
		tabLists.setBounds(1, 1, width - 6, height - 30);
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
				buttonConfirm.setIcon(null);
				return;
			}
		}
		if(!textDate.getText().equals("--/--/----"))
		try
		{
			tokenBook.setDate(date = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(textDate.getText()));
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
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
		if(date == null && !textDate.getText().equals("--/--/----"))
		{
			buttonConfirm.setEnabled(true);
			buttonConfirm.setIcon(null);
			return;
		}
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
			try
			{
				if(tokenBook instanceof NullBook)
					tokenBook = Core.Database.doInsert(Book.class);
				tokenBook.setJapaneseName(textJapaneseName.getText());
				tokenBook.setTranslatedName(textTranslatedName.getText());
				tokenBook.setRomanjiName(textRomanjiName.getText());
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
				for(Artist b : tokenBook.getArtists())
					if(!editorArtists.contains(b))
						tokenBook.removeArtist(b);
				java.util.Iterator<Artist> Artists = editorArtists.iterator();
				while(Artists.hasNext())
					tokenBook.addArtist(Artists.next());
				for(Content c : tokenBook.getContents())
					if(!editorContents.contains(c))
						tokenBook.removeContent(c);
				java.util.Iterator<Content> contents = editorContents.iterator();
				while(contents.hasNext())
					tokenBook.addContent(contents.next());
				for(Parody c : tokenBook.getParodies())
					if(!editorParodies.contains(c))
						tokenBook.removeParody(c);
				java.util.Iterator<Parody> parodies = editorParodies.iterator();
				while(parodies.hasNext())
					tokenBook.addParody(parodies.next());
				{
					if(tokenBook.getID() != null)
						writeXML(tokenBook, Core.Repository.getMetadata(tokenBook.getID()).getOutputStream());
				}
				if(Core.Database.isAutocommit())
					Core.Database.doCommit();
				Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_UPDATE, tokenBook));			
				Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, tokenBook, rect);
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
		if(tokenBook.isRecycled())
		{
			textJapaneseName.setEditable(false);
			textTranslatedName.setEditable(false);
			textRomanjiName.setEditable(false);
			textInfo.setEditable(false);
			textDate.setEditable(false);
			textPages.setEditable(false);
			comboType.setEnabled(false);
			comboConvention.setEnabled(false);
			checkAdult.setEnabled(false);
			checkDecensored.setEnabled(false);
			checkTranslated.setEnabled(false);
			checkColored.setEnabled(false);
			editorRating.setEnabled(false);
			editorArtists.setEnabled(false);
			editorCircles.setEnabled(false);
			editorContents.setEnabled(false);
			editorParodies.setEnabled(false);
			buttonConfirm.setEnabled(false);
		}
		if(tokenBook.getID() == null)
			labelPreview.setEnabled(false);
		else
			labelPreview.setEnabled(true);
		try {
			if(!Core.Database.getConventions(null).contains((Convention)comboConvention.getSelectedItem()))
				comboConvention.setSelectedItem(null);
		} catch (DataBaseException dbe) {
			Core.Logger.log(dbe.getMessage(), Level.ERROR);
			dbe.printStackTrace();
		}
		for(int i=0;i<comboConvention.getItemCount();i++)
			try {
				if(!Core.Database.getConventions(null).contains((Convention)comboConvention.getItemAt(i)) && comboConvention.getItemAt(i) != null)
					comboConvention.removeItemAt(i);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		if(ve.getType() != DouzEvent.Type.DATABASE_UPDATE)
		{
			if(ve.getParameter() instanceof Artist)
				editorArtists.validateUI(ve);
			if(ve.getParameter() instanceof Circle)
				editorCircles.validateUI(ve);
			if(ve.getParameter() instanceof Content)
				editorContents.validateUI(ve);
			if(ve.getParameter() instanceof Parody)
				editorParodies.validateUI(ve);
		}else
		{
			editorArtists.validateUI(ve);
			editorCircles.validateUI(ve);
			editorContents.validateUI(ve);
			editorParodies.validateUI(ve);
		}
	}
	
	private void writeXML(Book book, OutputStream dest) throws DataBaseException
	{
		XMLBook doujin = new XMLBook();
		doujin.japaneseName = book.getJapaneseName();
		doujin.translatedName = book.getTranslatedName();
		doujin.romanjiName = book.getRomanjiName();
		doujin.Convention = book.getConvention() == null ? "" : book.getConvention().getTagName();
		doujin.Released = book.getDate();
		doujin.Type = book.getType();
		doujin.Pages = book.getPages();
		doujin.Adult = book.isAdult();
		doujin.Decensored = book.isDecensored();
		doujin.Colored = book.isColored();
		doujin.Translated = book.isTranslated();
		doujin.Rating = book.getRating();
		doujin.Info = book.getInfo();
		for(Artist a : book.getArtists())
			doujin.artists.add(a.getJapaneseName());
		for(Circle c : book.getCircles())
			doujin.circles.add(c.getJapaneseName());
		for(Parody p : book.getParodies())
			doujin.parodies.add(p.getJapaneseName());
		for(Content ct : book.getContents())
			doujin.contents.add(ct.getTagName());
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLBook.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(doujin, dest);
		} catch (Exception e) {
			Core.Logger.log("Error parsing XML file (" + e.getMessage() + ").", Level.WARNING);
		}
	}
	
	@SuppressWarnings("unused")
	@XmlRootElement(name="Doujin")
	private static final class XMLBook
	{
		@XmlElement(required=true)
		private String japaneseName;
		@XmlElement(required=false)
		private String translatedName = "";
		@XmlElement(required=false)
		private String romanjiName = "";
		@XmlElement(required=false)
		private String Convention = "";
		@XmlElement(required=false)
		private Date Released;
		@XmlElement(required=false)
		private Type Type;
		@XmlElement(required=false)
		private int Pages;
		@XmlElement(required=false)
		private boolean Adult;
		@XmlElement(required=false)
		private boolean Decensored;
		@XmlElement(required=false)
		private boolean Translated;
		@XmlElement(required=false)
		private boolean Colored;
		@XmlElement(required=false)
		private Rating Rating;
		@XmlElement(required=false)
		private String Info;
		@XmlElement(name="Artist", required=false)
		private List<String> artists = new Vector<String>();
		@XmlElement(name="Circle", required=false)
		private List<String> circles = new Vector<String>();
		@XmlElement(name="Parody", required=false)
		private List<String> parodies = new Vector<String>();
		@XmlElement(name="Content", required=false)
		private List<String> contents = new Vector<String>();
	}
	
	private final class NullBook implements Book
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
		public String getJapaneseName() throws DataBaseException { return ""; }

		@Override
		public String getTranslatedName() throws DataBaseException { return ""; }

		@Override
		public String getRomanjiName() throws DataBaseException { return ""; }

		@Override
		public void setJapaneseName(String japaneseName) throws DataBaseException { }

		@Override
		public void setTranslatedName(String translatedName) throws DataBaseException { }

		@Override
		public void setRomanjiName(String romanjiName) throws DataBaseException { }

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
		public boolean isDecensored() throws DataBaseException { return false; }

		@Override
		public boolean isTranslated() throws DataBaseException { return false; }

		@Override
		public boolean isColored() throws DataBaseException { return false; }

		@Override
		public void setAdult(boolean adult) throws DataBaseException { }

		@Override
		public void setDecensored(boolean decensored) throws DataBaseException { }

		@Override
		public void setTranslated(boolean translated) throws DataBaseException { }

		@Override
		public void setColored(boolean colored) throws DataBaseException { }

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
				public boolean contains(Object o) throws DataBaseException { return false; }

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
				public boolean contains(Object o) throws DataBaseException { return false; }

				@Override
				public int size() throws DataBaseException { return 0; }
				
			};
		}

		@Override
		public Convention getConvention() throws DataBaseException { return null; } //FIXME

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
				public boolean contains(Object o) throws DataBaseException { return false; }

				@Override
				public int size() throws DataBaseException { return 0; }
				
			};
		}

		@Override
		public void addArtist(Artist artist) throws DataBaseException { }

		@Override
		public void addContent(Content content) throws DataBaseException { }

		@Override
		public void addParody(Parody parody) throws DataBaseException { }

		@Override
		public void removeArtist(Artist artist) throws DataBaseException { }

		@Override
		public void removeContent(Content content) throws DataBaseException { }

		@Override
		public void removeParody(Parody parody) throws DataBaseException { }
	}
}