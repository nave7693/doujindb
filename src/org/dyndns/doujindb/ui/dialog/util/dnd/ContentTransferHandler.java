package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.Content;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class ContentTransferHandler extends TransferHandlerEx<Content>
{
	public ContentTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_content.getImage();
		flavor = new ContentDataFlavor();
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Content> data = new Vector<Content>();
    	for(int index : table.getSelectedRows())
    		data.add((Content) table.getValueAt(index, 0));
    	
    	return new ContentTransferable(data, comp);
    }
	
	private final class ContentDataFlavor extends DataFlavorEx<Content>
	{
		private ContentDataFlavor()
		{
			mime = "doujindb/record-content";
        	name = "DoujinDB.Record.Content";
        	clazz = Content.class;
		}
	}
	
	private final class ContentTransferable extends TransferableEx<Content>
	{
		private ContentTransferable(java.util.List<Content> data, Component component)
		{
			super(data, component);
		}
	}
}
