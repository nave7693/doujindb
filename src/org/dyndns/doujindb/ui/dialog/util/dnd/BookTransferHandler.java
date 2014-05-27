package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class BookTransferHandler extends TransferHandlerEx<Book>
{
	public BookTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_book.getImage();
		flavor = new BookDataFlavor();
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
	
	private final class BookDataFlavor extends DataFlavorEx<Book>
	{
		private BookDataFlavor()
		{
			mime = "doujindb/record-book";
        	name = "Doujindb.Record.Book";
        	clazz = Book.class;
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
