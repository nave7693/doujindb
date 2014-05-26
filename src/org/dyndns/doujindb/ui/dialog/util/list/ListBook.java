package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.List;

import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.containers.BookContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.WrapLayout;
import org.dyndns.doujindb.util.ImageTool;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public class ListBook extends RecordList<Book> implements ActionListener, LayoutManager// extends JPanel, DataBaseListener
{
	private BookContainer tokenIBook;
	private JPanel recordPreview;
	private JScrollPane scrollRecordPreview;
	private boolean previewToggled = false;
	private boolean previewEnabled = (boolean) Configuration.configRead("org.dyndns.doujindb.ui.book_preview");
	private JButton toggleList;
	private JButton togglePreview;
	
	public ListBook(BookContainer token) throws DataBaseException
	{
		super(token.getBooks(), Book.class);
		this.tokenIBook = token;
		super.setLayout(this);
		toggleList = new JButton(Icon.desktop_explorer_table_view_list);
		toggleList.setToolTipText("Toggle List");
		toggleList.addActionListener(this);
		toggleList.setFocusable(false);
		recordPreview = new JPanel();
		recordPreview.setLayout(new WrapLayout());
		if(previewEnabled) new SwingWorker<Void,JButton>()
		{
			private ActionListener listener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) {
					QueryBook query = new QueryBook();
					query.ID = ae.getActionCommand();
					RecordSet<Book> result = DataBase.getBooks(query);
					UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, result.iterator().next());
				}
			};
			@Override
			protected Void doInBackground() throws Exception
			{
				for(final Book book : tokenIBook.getBooks())
				{
					JButton bookButton;
					bookButton = new JButton(
						new ImageIcon(
							ImageTool.read(DataStore.getThumbnail(book.getID()).getInputStream())));
					bookButton.setActionCommand(book.getID());
					bookButton.addActionListener(listener);
					bookButton.setBorder(null);
					publish(bookButton);
				}
				return null;
			}
			@Override
			protected void process(List<JButton> chunks) {
				for(JButton button : chunks) {
					recordPreview.add(button);
				}
			}
			@Override
			protected void done()
			{
				recordPreview.validate();
				recordPreview.doLayout();
			}
		}.execute();
		scrollRecordPreview = new JScrollPane(recordPreview);
		scrollRecordPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		togglePreview = new JButton(Icon.desktop_explorer_table_view_preview);
		togglePreview.setToolTipText("Toggle Preview");
		togglePreview.addActionListener(this);
		togglePreview.setFocusable(false);
		if(previewEnabled)
		{
			super.add(toggleList);
			super.add(togglePreview);
			super.add(scrollRecordPreview);
		}
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) { }

	@Override
	public void removeLayoutComponent(Component comp) { }

	@Override
	public Dimension preferredLayoutSize(Container parent) { return new Dimension(250, 	250); }

	@Override
	public Dimension minimumLayoutSize(Container parent) { return new Dimension(250, 250); }

	@Override
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth(),
			height = parent.getHeight();
//TODO		searchField.setBounds(0, 0, width - 20, 20);
		if(!previewToggled)
		{
//TODO			toggleList.setBounds(0, 0, 0, 0);
//TODO			recordList.setBounds(0, 20, width, height - 20);
//TODO			togglePreview.setBounds(width - 20, 0, 20, 20);
//TODO			scrollRecordPreview.setBounds(0, 0, 0, 0);
		} else {
//TODO			toggleList.setBounds(width - 20, 0, 20, 20);
//TODO			recordList.setBounds(0, 0, 0, 0);
//TODO			togglePreview.setBounds(0, 0, 0, 0);
//TODO			scrollRecordPreview.setBounds(0, 20, width, height - 20);
		}
		super.layoutContainer(parent);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(toggleList))
		{
			previewToggled = false;
			doLayout();
			toggleList.validate();
			return;
		}
		if(ae.getSource().equals(togglePreview))
		{
			previewToggled = true;
			doLayout();
			scrollRecordPreview.validate();
			return;
		}
	}
	
	public boolean contains(Book item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Book> iterator()
	{
		return getRecords().iterator();
	}
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
//TODO			recordList.addRecord((Book)data.getTarget());
			break;
		case UNLINK:
//TODO			recordList.removeRecord((Book)data.getTarget());
			break;
		}
	}
}