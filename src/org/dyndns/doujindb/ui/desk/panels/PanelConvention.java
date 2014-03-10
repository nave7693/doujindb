package org.dyndns.doujindb.ui.desk.panels;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.text.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.*;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.ui.desk.panels.edit.*;
import org.dyndns.doujindb.ui.desk.panels.util.RecordList;
import org.dyndns.doujindb.ui.desk.panels.util.TabbedPaneUIEx;

@SuppressWarnings("serial")
public final class PanelConvention extends JPanel implements DataBaseListener, LayoutManager, ActionListener
{
	private Convention tokenConvention;
	
	private Color backgroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.background").asColor();
	private Color foregroundColor = Core.Properties.get("org.dyndns.doujindb.ui.theme.color").asColor();
	
	private final Font font = Core.Resources.Font;
	private JLabel labelTagName;
	private JTextField textTagName;
	private JLabel labelWeblink;
	private JTextField textWeblink;
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
	
	public PanelConvention(Convention token) throws DataBaseException
	{
		tokenConvention = token;
		super.setLayout(this);
		labelTagName = new JLabel("Tag Name");
		labelTagName.setFont(font);
		textTagName = new JTextField("");
		textTagName.setFont(font);
		labelWeblink = new JLabel("Weblink");
		labelWeblink.setFont(font);
		textWeblink = new JTextField("");
		textWeblink.setFont(font);
		labelInfo = new JLabel("Info");
		labelInfo.setFont(font);
		textInfo = new JTextArea("");
		textInfo.setFont(font);
		scrollInfo = new JScrollPane(textInfo);
		tabLists = new JTabbedPane();
		tabLists.setFocusable(false);
		editorWorks = new RecordBookEditor(tokenConvention);
		tabLists.addTab("Works", Core.Resources.Icons.get("Desktop/Explorer/Book"), editorWorks);
		JPanel panel = new JPanel();
		textAlias = new JTextField("");
		listAlias = new JList<String>(new DefaultListModel<String>());
		addAlias = new JButton(Core.Resources.Icons.get("Frame/Tab/Explorer/Add"));
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
		    public void insertUpdate(DocumentEvent de) { }
		    public void removeUpdate(DocumentEvent de) { }
		    public void changedUpdate(DocumentEvent de) { }
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
				setIcon(Core.Resources.Icons.get("Desktop/Explorer/Convention"));
				if(isSelected)
				{
					setBackground(foregroundColor);
					setForeground(backgroundColor);
				}
				return this;
			}

		});
		listAlias.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent me)
			{
				if(me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1)
				{
					try
					{
						String item = (String)listAlias.getModel().getElementAt(listAlias.locationToIndex(me.getPoint()));
						textAlias.setText(item);
						tokenConvention.removeAlias(item);
						((DefaultListModel<String>)listAlias.getModel()).removeElement(item);
						if(Core.Database.isAutocommit())
							Core.Database.doCommit();
					} catch (DataBaseException dbe) {
						Logger.logError(dbe.getMessage(), dbe);
						dbe.printStackTrace();
					}
					return;
				}
				checkPopup(me);
			}
			@Override
			public void mousePressed(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseEntered(MouseEvent me) {
				checkPopup(me);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				checkPopup(me);
			}
			private void checkPopup(MouseEvent me)
			{
				if(!me.isPopupTrigger())
					return;
				
				// If not item is selected don't show any popup
				if(listAlias.getSelectedIndices().length < 1)
					return;
				
				JPopupMenu popupMenu = new JPopupMenu();
	    		JMenuItem menuItem = new JMenuItem("Delete", Core.Resources.Icons.get("Desktop/Explorer/Delete"));
	    		menuItem.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent ae)
					{
						try
						{
							for(Object o : listAlias.getSelectedValuesList())
								if(o instanceof String)
								{
									final String item = ((String)o);
									tokenConvention.removeAlias(item);
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
								}
						} catch (DataBaseException dbe) {
							Logger.logError(dbe.getMessage(), dbe);
							dbe.printStackTrace();
						}
						listAlias.validate();
					}
				});
	    		menuItem.setName("delete");
				menuItem.setActionCommand("delete");
				popupMenu.add(menuItem);
				popupMenu.show(me.getComponent(), me.getX(), me.getY());
			}
		});
		scrollAlias = new JScrollPane(listAlias);
		panel.add(scrollAlias);
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
		tabLists.addTab("Aliases", Core.Resources.Icons.get("Desktop/Explorer/Convention"), panel);
		tabLists.setUI(new TabbedPaneUIEx(new RecordList<?>[]{
				editorWorks.getRecordList(),
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
		buttonConfirm = new JButton("Ok");
		buttonConfirm.setMnemonic('O');
		buttonConfirm.setFocusable(false);
		buttonConfirm.addActionListener(this);
		super.add(labelTagName);
		super.add(textTagName);
		super.add(labelWeblink);
		super.add(textWeblink);
		super.add(labelInfo);
		super.add(scrollInfo);
		super.add(tabLists);
		super.add(buttonConfirm);

		loadData();
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
		labelTagName.setBounds(3, 3, 100, 15);
		textTagName.setBounds(103, 3, width - 106, 15);
		labelWeblink.setBounds(3, 3 + 15, 100, 15);
		textWeblink.setBounds(103, 3 + 15, width - 106, 15);
		labelInfo.setBounds(3, 3 + 30, 100, 15);
		scrollInfo.setBounds(3, 3 + 45, width - 6, 60);
		tabLists.setBounds(3, 3 + 105, width - 6, height - 135);
		buttonConfirm.setBounds(width / 2 - 40, height - 25, 80,  20);
	}
	
	public Convention getRecord()
	{
		return tokenConvention;
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
			if(tokenConvention.getID() == null)
				tokenConvention = Core.Database.doInsert(Convention.class);
			tokenConvention.setTagName(textTagName.getText());
			tokenConvention.setWeblink(textWeblink.getText());
			tokenConvention.setInfo(textInfo.getText());
			for(String a : tokenConvention.getAliases())
				if(!((DefaultListModel<String>)listAlias.getModel()).contains(a))
					tokenConvention.removeAlias(a);
			Enumeration<String> aliases = ((DefaultListModel<String>)listAlias.getModel()).elements();
			while(aliases.hasMoreElements())
				tokenConvention.addAlias(aliases.nextElement());
			for(Book b : tokenConvention.getBooks())
				if(!editorWorks.contains(b))
					tokenConvention.removeBook(b);
			java.util.Iterator<Book> books = editorWorks.iterator();
			while(books.hasNext())
				tokenConvention.addBook(books.next());

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
				textTagName.setText(tokenConvention.getTagName());
				textWeblink.setText(tokenConvention.getWeblink());
				textInfo.setText(tokenConvention.getInfo());
				for(String alias : tokenConvention.getAliases())
					((DefaultListModel<String>)listAlias.getModel()).add(0, alias);
				if(tokenConvention.isRecycled())
				{
					textTagName.setEditable(false);
					textInfo.setEditable(false);
					editorWorks.setEnabled(false);
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
			if(data.getProperty().equals("tag_name"))
				textTagName.setText(tokenConvention.getTagName());
			if(data.getProperty().equals("info"))
				textInfo.setText(tokenConvention.getInfo());
			if(data.getProperty().equals("weblink"))
				textWeblink.setText(tokenConvention.getWeblink());
			break;
		//case LINK:
		//case UNLINK:
		default:
			if(data.getTarget() instanceof Book)
				editorWorks.recordUpdated(rcd, data);
		}
	}
	
	@Override
	public void recordRecycled(Record rcd)
	{
		if(rcd instanceof Book)
			editorWorks.recordRecycled(rcd);
		loadData();
	}
	
	@Override
	public void recordRestored(Record rcd)
	{
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