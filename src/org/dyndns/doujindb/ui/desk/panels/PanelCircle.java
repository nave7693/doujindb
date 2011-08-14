package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.dat.DataSource;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;

@SuppressWarnings("serial")
public final class PanelCircle implements Validable, LayoutManager, ActionListener
{
	private DouzWindow parentWindow;
	private Circle tokenCircle;
	private boolean isModify;
	
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JLabel labelJapaneseName;
	private JTextField textJapaneseName;
	private JLabel labelTranslatedName;
	private JTextField textTranslatedName;
	private JLabel labelRomanjiName;
	private JTextField textRomanjiName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
	private JLabel labelBanner;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private RecordArtistEditor editorArtists;
	private JButton buttonConfirm;
	
	public PanelCircle(DouzWindow parent, JComponent pane, Circle token)
	{
		parentWindow = parent;
		if(token == null)
		{
			tokenCircle = Client.DB.newCircle();
			tokenCircle.setJapaneseName("");
			isModify = false;
		}else
		{
			tokenCircle = token;
			isModify = true;
		}
		pane.setLayout(this);
		labelJapaneseName = new JLabel("Japanese Name");
		labelJapaneseName.setFont(font);
		textJapaneseName = new JTextField(tokenCircle.getJapaneseName());
		textJapaneseName.setFont(font);
		labelTranslatedName = new JLabel("Translated Name");
		labelTranslatedName.setFont(font);
		textTranslatedName = new JTextField(tokenCircle.getTranslatedName());
		textTranslatedName.setFont(font);
		labelRomanjiName = new JLabel("Romanji Name");
		labelRomanjiName.setFont(font);
		textRomanjiName = new JTextField(tokenCircle.getRomanjiName());
		textRomanjiName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField(tokenCircle.getWeblink());
		textWeblink.setFont(font);
		labelBanner = new JLabel(Core.Resources.Icons.get("JDesktop/Explorer/Circle/Banner"));
		labelBanner.setName("no-banner");
		_loadBanner:{
			try
			{
				if(!isModify)
				{
					labelBanner.setEnabled(false);
					break _loadBanner;
				}
				//TODO
				/*File root = new File((File)Core.Properties.getValue("org.dyndns.doujindb.dat.datastore"), tokenCircle.getID());
				File entry = new File(root, ".banner");
				if(entry.exists())
				{
					labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(entry)));
					labelBanner.setName("banner");
					break _loadBanner;
				}
				if(Core.Datastore.contains(tokenCircle.getID()))
				{
					DataSource source = Core.Datastore.get(tokenCircle.getID());
					if(source.contains(".banner"))
					{
						InputStream in = source.get(".banner").getInputStream();
						labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
						in.close();
						labelBanner.setName("banner");
						break _loadBanner;
					}
				}*/
				DataSource ds = Core.Datastore.child(tokenCircle.getID());
				ds.mkdir();
				ds = Core.Datastore.getPreview(tokenCircle.getID()); //ds.child(".banner");
				if(ds.exists())
				{
					InputStream in = ds.getInputStream();
					labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
					labelBanner.setName("banner");
					in.close();
				}
			} catch (NullPointerException npe) {
			} catch (Exception e) {
				e.printStackTrace();
				//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
			}
		}
		labelBanner.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent me)
			{
				if(!isModify)
					return;
				if(me.getButton() == MouseEvent.BUTTON3)
				{
					JLabel lab = (JLabel) me.getSource();
					Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
					final boolean isAdd = lab.getName().equals("no-banner");
					if(isAdd)
						tbl.put("Add Banner", Core.Resources.Icons.get("JDesktop/Explorer/Circle/Popup/Add"));
					else
						tbl.put("Remove Banner", Core.Resources.Icons.get("JDesktop/Explorer/Circle/Popup/Remove"));
					final DouzPopupMenu pop = new DouzPopupMenu("", tbl);
					pop.show(lab, me.getX(), me.getY());
					new Thread(getClass().getName()+"/PopupMenu")
					{
						@Override
						public void run()
						{
							while(pop.isValid())
								try { sleep(1); } catch (InterruptedException e) { }
							if(pop.getResult() == 0)
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
									//TODO
									/*File root = new File((File)Core.Properties.getValue("org.dyndns.doujindb.dat.datastore"), tokenCircle.getID());
									File out = new File(root, ".banner");
									out.getParentFile().mkdirs();
									OutputStream out = null;
									if(Core.Datastore.contains(tokenCircle.getID()))
									{
										DataSource source = Core.Datastore.get(tokenCircle.getID());
										if(source.contains(".banner"))
										{
											out = source.get(".banner").getOutputStream();
										}
										else
										{
											source.add(".banner");
											out = source.get(".banner").getOutputStream();
										}
									}else
									{
										Core.Datastore.add(tokenCircle.getID());
										DataSource source = Core.Datastore.get(tokenCircle.getID());
										if(source.contains(".banner"))
										{
											out = source.get(".banner").getOutputStream();
										}
										else
										{
											source.add(".banner");
											out = source.get(".banner").getOutputStream();
										}
									}*/
									DataSource ds = Core.Datastore.child(tokenCircle.getID());
									ds.mkdir();
									ds = Core.Datastore.getPreview(tokenCircle.getID()); //ds.child(".banner");
									ds.touch();
									OutputStream out = ds.getOutputStream();
									File in = fc.getSelectedFile();
									BufferedImage im = new BufferedImage(200, 40, BufferedImage.TYPE_INT_ARGB);
									Graphics2D graphics2D = im.createGraphics();
									graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
									graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
									graphics2D.drawImage(new ImageIcon(in.getAbsolutePath()).getImage(), 0, 0, null);
									javax.imageio.ImageIO.write(im, "PNG", out);
									out.close();
								} catch (Exception e) {
									e.printStackTrace();
									Core.Logger.log(e.getMessage(), Level.WARNING);
								}
								try {
									//TODO
									/*if(Core.Datastore.contains(tokenCircle.getID()))
									{
										DataSource source = Core.Datastore.get(tokenCircle.getID());
										if(source.contains(".banner"))
										{
											InputStream in = source.get(".banner").getInputStream();
											labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
											in.close();
										}
									}*/
									DataSource ds = Core.Datastore.child(tokenCircle.getID());
									ds.mkdir();
									ds = Core.Datastore.getPreview(tokenCircle.getID()); //ds.child(".banner");
									if(ds.exists())
									{
										InputStream in = ds.getInputStream();
										labelBanner.setIcon(new ImageIcon(javax.imageio.ImageIO.read(in)));
										in.close();
									}
								} catch (NullPointerException npe) {
								} catch (IOException e) {
									e.printStackTrace();
									//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
								}
								labelBanner.setName("banner");
							}
							else
							{
								try
								{
									//TODO
									/*if(Core.Datastore.contains(tokenCircle.getID()))
									{
										DataSource source = Core.Datastore.get(tokenCircle.getID());
										if(source.contains(".banner"))
										{
											source.remove(".banner");
										}
									}else
									{
										Core.Datastore.add(tokenCircle.getID());
										DataSource source = Core.Datastore.get(tokenCircle.getID());
										if(source.contains(".banner"))
										{
											source.remove(".banner");
										}
									}*/
									DataSource ds = Core.Datastore.child(tokenCircle.getID());
									ds.mkdir();
									ds = Core.Datastore.getPreview(tokenCircle.getID()); //ds.child(".banner");
									ds.delete();
								} catch (NullPointerException npe) {
								} catch (Exception e)
								{
									e.printStackTrace();
									//Core.Logger.log(new Event(e.getMessage(), Level.WARNING));
								}
								labelBanner.setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Circle/Banner"));
								labelBanner.setName("no-banner");
							}								
						}
					}.start();
				 }
			  }
		});
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenCircle);
		tabLists.addTab("Works", Core.Resources.Icons.get("JDesktop/Explorer/Book"), editorWorks);
		editorArtists = new RecordArtistEditor(tokenCircle);
		tabLists.addTab("Artists", Core.Resources.Icons.get("JDesktop/Explorer/Artist"), editorArtists);
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
		pane.add(labelBanner);
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
		labelBanner.setBounds(3, 3 + 62, 200, 40);
		tabLists.setBounds(3, 3 + 62 + 42, width - 6, height - 135);
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
				tokenCircle.setJapaneseName(textJapaneseName.getText());
				tokenCircle.setTranslatedName(textTranslatedName.getText());
				tokenCircle.setRomanjiName(textRomanjiName.getText());
				tokenCircle.setWeblink(textWeblink.getText());
				for(Book b : tokenCircle.getBooks())
				{
					if(!editorWorks.contains(b))
					{
						b.getCircles().remove(tokenCircle);
						tokenCircle.getBooks().remove(b);
					}
				}
				java.util.Iterator<Book> books = editorWorks.iterator();
				while(books.hasNext())
				{
					Book b = books.next();
					b.getCircles().add(tokenCircle);
					tokenCircle.getBooks().add(b);
				}
				for(Artist c : tokenCircle.getArtists())
				{
					if(!editorArtists.contains(c))
					{
						c.getCircles().remove(tokenCircle);
						tokenCircle.getArtists().remove(c);
					}
				}
				java.util.Iterator<Artist> Artists = editorArtists.iterator();
				while(Artists.hasNext())
				{
					Artist c = Artists.next();
					c.getCircles().add(tokenCircle);
					tokenCircle.getArtists().add(c);
				}
				if(!isModify)
				{
					Client.DB.getCircles().insert(tokenCircle);
					Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMADDED, tokenCircle));
				}
				else
					Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_ITEMCHANGED, tokenCircle));				
			}
			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CIRCLE, tokenCircle, rect);
		}
		buttonConfirm.setEnabled(true);
		buttonConfirm.setIcon(null);
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(ve.getType() != DouzEvent.DATABASE_ITEMCHANGED)
		{
			if(ve.getParameter() instanceof Artist)
				editorArtists.validateUI(ve);
			if(ve.getParameter() instanceof Book)
				editorWorks.validateUI(ve);
		}else
		{
			editorArtists.validateUI(ve);
			editorWorks.validateUI(ve);
		}
	}	
}