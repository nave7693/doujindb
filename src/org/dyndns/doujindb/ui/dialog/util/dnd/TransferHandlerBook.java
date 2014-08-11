package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.record.Book;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class TransferHandlerBook extends TransferHandlerEx<Book>
{
	static private final DataFlavor bflavor = new BookDataFlavor();

	public TransferHandlerBook()
	{
		super();
		icon = Icon.desktop_explorer_book.getImage();
		flavor = TransferHandlerBook.bflavor;
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Book> data = new Vector<Book>();
    	for(int index : table.getSelectedRows())
    		data.add((Book) table.getValueAt(index, 0));
    	
    	return new BookTransferable(data, comp);
    }
	
	private static final class BookDataFlavor extends DataFlavor
	{
		private BookDataFlavor()
		{
			super("doujindb/record-book", "DoujinDB.Record.Book");
		}
	}
	
	private final class BookTransferable extends TransferableEx<Book>
	{
		private BookTransferable(java.util.List<Book> data, Component component)
		{
			super(data, component);
		}
	}
}
