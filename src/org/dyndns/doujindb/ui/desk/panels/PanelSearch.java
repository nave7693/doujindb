package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.PropertyException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;

@SuppressWarnings("serial")
public final class PanelSearch extends JPanel implements Validable
{
	private Validable panel;
	private Color backgroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor();
	private Color foregroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
	
	public enum Type
	{
		ISEARCH_ARTIST,
		ISEARCH_BOOK,
		ISEARCH_CIRCLE,
		ISEARCH_CONTENT,
		ISEARCH_CONVENTION,
		ISEARCH_PARODY
	}
	
	public PanelSearch(Type tokenType, JTabbedPane tab, int index)
	{
		super();
		switch(tokenType)
		{
		case ISEARCH_ARTIST:
		{
			panel = new IPanelArtist(this, tab, index);
			break;
		}
		case ISEARCH_BOOK:
		{
			panel = new IPanelBook(this, tab, index);
			break;
		}
		case ISEARCH_CIRCLE:
		{
			panel = new IPanelCircle(this, tab, index);
			break;
		}
		case ISEARCH_CONTENT:
		{
			panel = new IPanelContent(this, tab, index);
			break;
		}
		case ISEARCH_CONVENTION:
		{
			panel = new IPanelConvention(this, tab, index);
			break;
		}
		case ISEARCH_PARODY:
		{
			panel = new IPanelParody(this, tab, index);
			break;
		}
		}
	}
	
	private final class IPanelArtist implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JList<Artist> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelArtist(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField(".*");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField(".*");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField(".*");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField(".*");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Artist>(new DefaultListModel<Artist>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Artist))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					Artist a = (Artist) value;
					try {
						setText(a.getJapaneseName() + 
								(a.getRomanjiName().equals("") ? "" : " ("+a.getRomanjiName()+")") +
								(a.getTranslatedName().equals("") ? "" : " ("+a.getTranslatedName()+")"));
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					try {
						setToolTipText("<html><body>" +
								"<b>Japanese Name</b> : " + a.getJapaneseName() +
								"<br><b>Translated Name</b> : " + a.getTranslatedName() +
								"<br><b>Romanji Name</b> : " + a.getRomanjiName() +
								"<br><b>Weblink</b> : " + a.getWeblink() +
								"<br><b>Books</b> : " + a.getBooks().size() +
								"</body></html>");
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_ARTIST, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Artist)
											{
												Artist item = ((Artist)o);
												item.doRecycle();
												((DefaultListModel<Artist>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					}
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
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
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						
						((DefaultListModel<Artist>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							for(Artist a : Core.Database.getArtists(null))
							{
								if(stopped)
									break;
								try
								{
									if( a.getJapaneseName().matches(textJapaneseName.getText()) &&
										a.getTranslatedName().matches(textTranslatedName.getText()) &&
										a.getRomanjiName().matches(textRomanjiName.getText()) &&
										a.getWeblink().matches(textWeblink.getText()) /*&&
										//FIXME !Core.Database.getDeleted().contains(a)*/)
										((DefaultListModel<Artist>)listResults.getModel()).add(0, a);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Artist"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelCircle implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JList<Circle> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelCircle(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField(".*");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField(".*");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField(".*");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField(".*");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Circle>(new DefaultListModel<Circle>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Circle))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					Circle c = (Circle) value;
					try {
						setText(c.getJapaneseName() + 
								(c.getRomanjiName().equals("") ? "" : " ("+c.getRomanjiName()+")") +
								(c.getTranslatedName().equals("") ? "" : " ("+c.getTranslatedName()+")"));
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					try {
						setToolTipText("<html><body>" +
								"<b>Japanese Name</b> : " + c.getJapaneseName() +
								"<br><b>Translated Name</b> : " + c.getTranslatedName() +
								"<br><b>Romanji Name</b> : " + c.getRomanjiName() +
								"<br><b>Weblink</b> : " + c.getWeblink() +
								"<br><b>Books</b> : " + c.getBooks().size() +
								"</body></html>");
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CIRCLE, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Circle)
											{
												Circle item = ((Circle)o);
												item.doRecycle();
												((DefaultListModel<Circle>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					 }
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
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
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						((DefaultListModel<Circle>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							for(Circle c : Core.Database.getCircles(null))
							{
								if(stopped)
									break;
								try
								{
									if( c.getJapaneseName().matches(textJapaneseName.getText()) &&
										c.getTranslatedName().matches(textTranslatedName.getText()) &&
										c.getRomanjiName().matches(textRomanjiName.getText()) &&
										c.getWeblink().matches(textWeblink.getText()) /*&&
										//FIXME !Core.Database.getDeleted().contains(c)*/)
										((DefaultListModel<Circle>)listResults.getModel()).add(0, c);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Circle"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelBook implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelConvention;
		private JTextField textConvention;
		private JLabel labelContents;
		private JTextField textContents;
		private JLabel labelType;
		private JComboBox<Book.Type> comboType;
		private JCheckBox checkAdult;
		private JCheckBox checkDecensored;
		private JCheckBox checkTranslated;
		private JCheckBox checkColored;
		private JLabel labelResults;
		private JList<Book> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelBook(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField(".*");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField(".*");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField(".*");
			textRomanjiName.setFont(font);
			labelConvention = new JLabel("Convention");
			labelConvention.setFont(font);
			textConvention = new JTextField(".*");
			textConvention.setFont(font);
			labelContents = new JLabel("Contents");
			labelContents.setFont(font);
			textContents = new JTextField("");
			textContents.setFont(font);
			labelType = new JLabel("Type");
			labelType.setFont(font);
			comboType = new JComboBox<Book.Type>();
			comboType.setFont(font);
			comboType.setFocusable(false);
			for(Book.Type tokenType : Book.Type.values())
				comboType.addItem(tokenType);
			comboType.setSelectedItem(Book.Type.同人誌);
			checkAdult = new JCheckBox("Adult", true);
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
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Book>(new DefaultListModel<Book>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Book))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Book"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					final Book b = (Book) value;
					try {
						setText(b.getJapaneseName() + 
								(b.getRomanjiName().equals("") ? "" : " ("+b.getRomanjiName()+")") +
								(b.getTranslatedName().equals("") ? "" : " ("+b.getTranslatedName()+")"));
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					try {
						setToolTipText("<html><body>" +
								"<b>Japanese Name</b> : " + b.getJapaneseName() +
								"<br><b>Translated Name</b> : " + b.getTranslatedName() +
								"<br><b>Romanji Name</b> : " + b.getRomanjiName() +
								/*"<br><b>Artists</b> : " + new Object()
								{
									@Override
									public String toString()
									{
										String s = "<ul>";
										for(Artist a : b.getArtists())
											s += "<li>" + a.toString() + "</li>";
										return s + "</ul>";
									}
								} +
								"<br>." + //TODO Tooltip covers in search panel
								((Core.Repository.contains(b.getID() + "/.preview")) ?
										"<br><table border='0'><tr><td><img src='file:///" + Core.Repository.get(b.getID()).get(".preview") + "' /></td></tr></table>"
												:
										"") +*/
								"</body></html>");
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Book)
											{
												Book item = ((Book)o);
												item.doRecycle();
												((DefaultListModel<Book>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					 }
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelConvention);
			pane.add(textConvention);
			pane.add(labelContents);
			pane.add(textContents);
			pane.add(labelType);
			pane.add(comboType);
			pane.add(checkAdult);
			pane.add(checkDecensored);
			pane.add(checkTranslated);
			pane.add(checkColored);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
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
			labelConvention.setBounds(3, 3 + 45, 100, 15);
			textConvention.setBounds(103, 3 + 45, width - 106, 15);				
			labelContents.setBounds(3, 3 + 60, 100, 15);
			textContents.setBounds(103, 3 + 60, width - 106, 15);				
			labelType.setBounds(3, 3 + 75, 100, 20);
			comboType.setBounds(103, 3 + 75, 100, 20);				
			checkAdult.setBounds(3, 3 + 95, 100, 15);
			checkDecensored.setBounds(3, 3 + 110, 100, 15);
			checkTranslated.setBounds(3, 3 + 125, 100, 15);
			checkColored.setBounds(3, 3 + 140, 100, 15);
			labelResults.setBounds(3, 3 + 155, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 170, width - 5, height - 155 - 45);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						((DefaultListModel<Book>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try
						{
							for(Book b : Core.Database.getBooks(null))
							{
								if(stopped)
									break;
								try
								{
									boolean containsAllTags = false;
									Vector<String> tags0 = new Vector<String>();
									for(String tag : textContents.getText().split(","))
										if(!tag.equals(""))
											tags0.add(tag.trim());
									Vector<String> tags1 = new Vector<String>();
									for(Content cnt : b.getContents())
										tags1.add(cnt.getTagName());
									if(tags1.containsAll(tags0))
										containsAllTags = true;
									if( b.getJapaneseName().matches(textJapaneseName.getText()) &&
											b.getTranslatedName().matches(textTranslatedName.getText()) &&
											b.getRomanjiName().matches(textRomanjiName.getText()) &&
											(b.getConvention()==null?"":b.getConvention()+"").matches(textConvention.getText()) &&
											b.getType() == comboType.getSelectedItem() &&
											(checkAdult.isSelected()?b.isAdult():true) &&
											(checkDecensored.isSelected()?b.isDecensored():true) &&
											(checkTranslated.isSelected()?b.isTranslated():true) &&
											(checkColored.isSelected()?b.isColored():true) &&
											containsAllTags /*&&
											//FIXME !Core.Database.getDeleted().contains(b)*/
											)
											((DefaultListModel<Book>)listResults.getModel()).add(0, b);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (NullPointerException npe) { npe.printStackTrace(); }
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Book"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Book"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}		
	}
	private final class IPanelContent implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JList<Content> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelContent(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField(".*");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Content>(new DefaultListModel<Content>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Content))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Content"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					Content ct = (Content) value;
					try {
						setText(ct.getTagName());
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONTENT, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Content)
											{
												Content item = ((Content)o);
												item.doRecycle();
												((DefaultListModel<Content>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					 }
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelTagName);
			pane.add(textTagName);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			labelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						((DefaultListModel<Content>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							for(Content ct : Core.Database.getContents(null))
							{
								if(stopped)
									break;
								try
								{
									if( ct.getTagName().matches(textTagName.getText()) /*&&
											//FIXME !Core.Database.getDeleted().contains(ct)*/)
										((DefaultListModel<Content>)listResults.getModel()).add(0, ct);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Content"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Content"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}		
	}
	private final class IPanelConvention implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelTagName;
		private JTextField textTagName;
		private JLabel labelResults;
		private JList<Convention> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelConvention(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelTagName = new JLabel("Tag Name");
			labelTagName.setFont(font);
			textTagName = new JTextField(".*");
			textTagName.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Convention>(new DefaultListModel<Convention>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Convention))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					Convention cn = (Convention) value;
					try {
						setText(cn.getTagName() + (cn.getInfo().equals("") ? "":" (" + cn.getInfo() + ")"));
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					try {
						setToolTipText("<html><body><b>" + cn.getTagName() + "</b><br>" + 
								cn.getInfo() +
								"</body></html>");
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONVENTION, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Convention)
											{
												Convention item = ((Convention)o);
												item.doRecycle();
												((DefaultListModel<Convention>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					 }
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelTagName);
			pane.add(textTagName);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
		}
		@Override
		public void layoutContainer(Container parent)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			labelTagName.setBounds(3, 3, 100, 15);
			textTagName.setBounds(103, 3, width - 106, 15);
			labelResults.setBounds(3, 3 + 15, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 30, width - 5, height - 30 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						((DefaultListModel<Convention>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							for(Convention cn : Core.Database.getConventions(null))
							{
								if(stopped)
									break;
								try
								{
									if( cn.getTagName().matches(textTagName.getText()) /*&&
											//FIXME !Core.Database.getDeleted().contains(cn)*/)
										((DefaultListModel<Convention>)listResults.getModel()).add(0, cn);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Convention"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	private final class IPanelParody implements Validable, LayoutManager, ActionListener
	{
		private JTabbedPane tab;
		private int index;
		private Thread process;
		private boolean stopped = true;
		
		private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
		private JLabel labelJapaneseName;
		private JTextField textJapaneseName;
		private JLabel labelTranslatedName;
		private JTextField textTranslatedName;
		private JLabel labelRomanjiName;
		private JTextField textRomanjiName;
		private JLabel labelWeblink;
		private JTextField textWeblink;
		private JLabel labelResults;
		private JList<Parody> listResults;
		private JScrollPane scrollResults;
		private JButton buttonSearch;
		
		public IPanelParody(JPanel pane, JTabbedPane tab, int index)
		{
			this.tab = tab;
			this.index = index;
			pane.setLayout(this);
			labelJapaneseName = new JLabel("Japanese Name");
			labelJapaneseName.setFont(font);
			textJapaneseName = new JTextField(".*");
			textJapaneseName.setFont(font);
			labelTranslatedName = new JLabel("Translated Name");
			labelTranslatedName.setFont(font);
			textTranslatedName = new JTextField(".*");
			textTranslatedName.setFont(font);
			labelRomanjiName = new JLabel("Romanji Name");
			labelRomanjiName.setFont(font);
			textRomanjiName = new JTextField(".*");
			textRomanjiName.setFont(font);
			labelWeblink = new JLabel("Weblink");
			labelWeblink.setFont(font);
			textWeblink = new JTextField(".*");
			textWeblink.setFont(font);
			labelResults = new JLabel("Found");
			labelResults.setFont(font);
			listResults = new JList<Parody>(new DefaultListModel<Parody>());
			listResults.setCellRenderer(new DefaultListCellRenderer(){
				@Override
				public Component getListCellRendererComponent(
					JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					if(!(value instanceof Parody))
						return null;
					super.getListCellRendererComponent(list, value, index, isSelected, false);
					setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
					if(isSelected)
					{
						setBackground(foregroundColor);
						setForeground(backgroundColor);
					}
					Parody p = (Parody) value;
					try {
						setText(p.getJapaneseName() + 
								(p.getRomanjiName().equals("") ? "" : " ("+p.getRomanjiName()+")") +
								(p.getTranslatedName().equals("") ? "" : " ("+p.getTranslatedName()+")"));
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					setFont(font);
					try {
						setToolTipText("<html><body>" +
								"<b>Japanese Name</b> : " + p.getJapaneseName() +
								"<br><b>Translated Name</b> : " + p.getTranslatedName() +
								"<br><b>Romanji Name</b> : " + p.getRomanjiName() +
								"<br><b>Weblink</b> : " + p.getWeblink() +
								"<br><b>Books</b> : " + p.getBooks().size() +
								"</body></html>");
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
					return this;
				}

			});
			listResults.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					if(!stopped) // check if JList is not being populated or suffer a java.util.ConcurrentModificationException
						return;
					if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					{
						try {
							Record item = (Record)listResults.getModel().getElementAt(listResults.locationToIndex(e.getPoint()));
							Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_PARODY, item);
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						}
					}else
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						// If not item is selected don't show any popup
						if(listResults.getSelectedIndices().length < 1)
							return;
						Hashtable<String,ImageIcon> tbl = new Hashtable<String,ImageIcon>();
						tbl.put("Delete", Core.Resources.Icons.get("JDesktop/Explorer/Delete"));
						final DouzPopupMenu pop = new DouzPopupMenu("Options", tbl);
						pop.show((Component)e.getSource(), e.getX(), e.getY());
						new Thread(getClass().getName()+"/MouseClicked")
						{
							@Override
							public void run()
							{
								while(pop.isValid())
									try { sleep(1); } catch (InterruptedException ie) { ; }
								int selected = pop.getResult();
								switch(selected)
								{
								case 0:{
									try {
										for(Object o : listResults.getSelectedValuesList())
											if(o instanceof Parody)
											{
												Parody item = ((Parody)o);
												item.doRecycle();
												((DefaultListModel<Parody>)listResults.getModel()).removeElement(item);
												Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
											}
									} catch (DataBaseException dbe) {
										Core.Logger.log(dbe.getMessage(), Level.ERROR);
										dbe.printStackTrace();
									}
									listResults.validate();
									break;
								}
								}
							}
						}.start();
					 }
				  }
			});
			scrollResults = new JScrollPane(listResults);
			buttonSearch = new JButton("Search");
			buttonSearch.setMnemonic('S');
			buttonSearch.setFocusable(false);
			buttonSearch.addActionListener(this);
			pane.add(labelJapaneseName);
			pane.add(textJapaneseName);
			pane.add(labelTranslatedName);
			pane.add(textTranslatedName);
			pane.add(labelRomanjiName);
			pane.add(textRomanjiName);
			pane.add(labelWeblink);
			pane.add(textWeblink);
			pane.add(labelResults);
			pane.add(scrollResults);
			pane.add(buttonSearch);
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
			labelResults.setBounds(3, 3 + 60, width / 2 - 6, 15);
			scrollResults.setBounds(3, 3 + 75, width - 5, height - 75 - 30);
			buttonSearch.setBounds(width / 2 - 40, height - 25, 80,  20);
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
			if(stopped)
			{
				buttonSearch.setText("Cancel");
				buttonSearch.setMnemonic('C');
				stopped = false;
				process = new Thread(getClass().getName()+"/ActionPerformed")
				{
					@Override
					public void run()
					{
						((DefaultListModel<Parody>)listResults.getModel()).clear();
						tab.setIconAt(index, Core.Resources.Icons.get("JFrame/Loading"));
						try {
							for(Parody p : Core.Database.getParodies(null))
							{
								if(stopped)
									break;
								try
								{
									if( p.getJapaneseName().matches(textJapaneseName.getText()) &&
										p.getTranslatedName().matches(textTranslatedName.getText()) &&
										p.getRomanjiName().matches(textRomanjiName.getText()) &&
										p.getWeblink().matches(textWeblink.getText()) /*&&
										//FIXME !Core.Database.getDeleted().contains(p)*/)
										((DefaultListModel<Parody>)listResults.getModel()).add(0, p);
									sleep((Core.Properties.get("org.dyndns.doujindb.ui.delay_threads").asNumber()));
								}
								catch (InterruptedException ie) { ; }
								catch (java.util.regex.PatternSyntaxException pse) { ; }
							}
						} catch (DataBaseException dbe) {
							Core.Logger.log(dbe.getMessage(), Level.ERROR);
							dbe.printStackTrace();
						} catch (PropertyException pe) {
							Core.Logger.log(pe.getMessage(), Level.ERROR);
							pe.printStackTrace();
						}
						tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
						buttonSearch.setText("Search");
						buttonSearch.setMnemonic('S');
						stopped = true;
					}
				};
				process.start();	
			}else
			{
				tab.setIconAt(index, Core.Resources.Icons.get("JDesktop/Explorer/Parody"));
				buttonSearch.setText("Search");
				buttonSearch.setMnemonic('S');
				stopped = true;
			}
		}
		@Override
		public void validateUI(DouzEvent ve) {
			;
		}	
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		panel.validateUI(ve);
		super.validate();
	}
}