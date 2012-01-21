package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;

@SuppressWarnings("serial")
public final class PanelContent implements Validable, LayoutManager, ActionListener
{
	private DouzWindow parentWindow;
	private Content tokenContent;
	
	private Color backgroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor();
	private Color foregroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
	
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JLabel labelTagName;
	private JTextField textTagName;
	private JLabel labelInfo;
	private JTextArea textInfo;
	private JScrollPane scrollInfo;
	private JTabbedPane tabLists;
	private RecordBookEditor editorWorks;
	private JTextField textAlias;
	private JList<String> listAlias;
	private JButton addAlias;
	private JScrollPane scrollAlias;
	private JButton buttonConfirm;
	
	public PanelContent(DouzWindow parent, JComponent pane, Content token) throws DataBaseException
	{
		parentWindow = parent;

		if(token != null)
			tokenContent = token;
		else
			tokenContent = new NullContent();
		
		pane.setLayout(this);
		labelTagName = new JLabel("Tag Name");
		labelTagName.setFont(font);
		textTagName = new JTextField(tokenContent.getTagName());
		textTagName.setFont(font);
		labelInfo = new JLabel("Info");
		labelInfo.setFont(font);
		textInfo = new JTextArea(tokenContent.getInfo());
		textInfo.setFont(font);
		scrollInfo = new JScrollPane(textInfo);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenContent);
		tabLists.addTab("Works", Core.Resources.Icons.get("JDesktop/Explorer/Book"), editorWorks);
		JPanel panel = new JPanel();
		textAlias = new JTextField("");
		listAlias = new JList<String>(new DefaultListModel<String>());
		addAlias = new JButton(Core.Resources.Icons.get("JFrame/Tab/Explorer/Add"));
		textAlias.setFont(font);
		textAlias.setDocument(new PlainDocument()
		{
			@Override
			public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
			{
				if (str == null)
					return;
				if ((getLength() + str.length()) <= 32)
				{
					super.insertString(offset, str, attr);
				}
			}
		});
		textAlias.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) { }
		    public void removeUpdate(DocumentEvent e) { }
		    public void changedUpdate(DocumentEvent e) { }
		});
		panel.add(textAlias);
		addAlias.setBorder(null);
		addAlias.setFocusable(false);
		addAlias.setToolTipText("Add Alias");
		addAlias.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				final String alias = textAlias.getText();
				if(alias.equals(""))
					return;
				if(((DefaultListModel<String>)listAlias.getModel()).contains(alias))
					return;
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						((DefaultListModel<String>)listAlias.getModel()).add(0, alias);
						if(Core.Database.isAutocommit())
							Core.Database.doCommit();
					}
				});
				textAlias.setText("");
			}
		});
		panel.add(addAlias);
		listAlias.setCellRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(
				JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				if(!(value instanceof String))
					return null;
				super.getListCellRendererComponent(list, value, index, isSelected, false);
				setIcon(Core.Resources.Icons.get("JDesktop/Explorer/Content"));
				if(isSelected)
				{
					setBackground(foregroundColor);
					setForeground(backgroundColor);
				}
				return this;
			}

		});
		listAlias.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try
					{
						String item = (String)listAlias.getModel().getElementAt(listAlias.locationToIndex(e.getPoint()));
						textAlias.setText(item);
						tokenContent.removeAlias(item);
						((DefaultListModel<String>)listAlias.getModel()).removeElement(item);
						if(Core.Database.isAutocommit())
							Core.Database.doCommit();
					} catch (DataBaseException dbe) {
						Core.Logger.log(dbe.getMessage(), Level.ERROR);
						dbe.printStackTrace();
					}
				}else
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					// If not item is selected don't show any popup
					if(listAlias.getSelectedIndices().length < 1)
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
									for(Object o : listAlias.getSelectedValuesList())
										if(o instanceof String)
										{
											final String item = ((String)o);
											tokenContent.removeAlias(item);
											SwingUtilities.invokeLater(new Runnable()
											{
												@Override
												public void run()
												{
													((DefaultListModel<String>)listAlias.getModel()).removeElement(item);
													if(Core.Database.isAutocommit())
														Core.Database.doCommit();
												}
											});
											Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_DELETE, item));
										}
								} catch (DataBaseException dbe) {
									Core.Logger.log(dbe.getMessage(), Level.ERROR);
									dbe.printStackTrace();
								}
								listAlias.validate();
								break;
							}
							}
						}
					}.start();
				}
			  }
		});
		scrollAlias = new JScrollPane(listAlias);
		panel.add(scrollAlias);
		for(String alias : tokenContent.getAliases())
			((DefaultListModel<String>)listAlias.getModel()).add(0, alias);
		panel.setLayout(new LayoutManager()
		{
			@Override
			public void layoutContainer(Container parent)
			{
				int width = parent.getWidth(),
					height = parent.getHeight();
				textAlias.setBounds(1, 1, width - 22, 20);
				addAlias.setBounds(width - 21, 1, 20, 20);
				scrollAlias.setBounds(1, 22, width - 2, height - 22);
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
		tabLists.addTab("Aliases", Core.Resources.Icons.get("JDesktop/Explorer/Content"), panel);
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		pane.add(labelTagName);
		pane.add(textTagName);
		pane.add(labelInfo);
		pane.add(scrollInfo);
		pane.add(tabLists);
		pane.add(buttonConfirm);
	}
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		labelTagName.setBounds(3, 3, 100, 15);
		textTagName.setBounds(103, 3, width - 106, 15);
		labelInfo.setBounds(3, 3 + 15, 100, 15);
		scrollInfo.setBounds(3, 3 + 30, width - 6, 60);
		tabLists.setBounds(3, 3 + 90, width - 6, height - 120);
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
		if(textTagName.getText().length()<1)
		{
			final Border brd1 = textTagName.getBorder();
			final Border brd2 = BorderFactory.createLineBorder(Color.ORANGE);
			final Timer tmr = new Timer(100, new AbstractAction () {
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
				if(tokenContent instanceof NullContent)
					tokenContent = Core.Database.doInsert(Content.class);
				tokenContent.setTagName(textTagName.getText());
				tokenContent.setInfo(textInfo.getText());
				for(String a : tokenContent.getAliases())
					if(!((DefaultListModel<String>)listAlias.getModel()).contains(a))
						tokenContent.removeAlias(a);
				Enumeration<String> aliases = ((DefaultListModel<String>)listAlias.getModel()).elements();
				while(aliases.hasMoreElements())
					tokenContent.addAlias(aliases.nextElement());
				for(Book b : tokenContent.getBooks())
					if(!editorWorks.contains(b))
						tokenContent.removeBook(b);
				java.util.Iterator<Book> books = editorWorks.iterator();
				while(books.hasNext())
					tokenContent.addBook(books.next());
				if(Core.Database.isAutocommit())
					Core.Database.doCommit();
				Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.Type.DATABASE_UPDATE, tokenContent));					
				Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONTENT, tokenContent, rect);
			} catch (DataBaseException dbe) {
				Core.Logger.log(dbe.getMessage(), Level.ERROR);
				dbe.printStackTrace();
			}
		}
		buttonConfirm.setEnabled(true);
		buttonConfirm.setIcon(null);
		validateUI(new DouzEvent(DouzEvent.Type.DATABASE_REFRESH, null));
	}
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(tokenContent.isRecycled())
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
	
	private final class NullContent implements Content
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
		public Set<String> getAliases() throws DataBaseException { return new java.util.TreeSet<String>(); }

		@Override
		public void addAlias(String alias) throws DataBaseException { }

		@Override
		public void removeAlias(String alias) throws DataBaseException { }
	}
}