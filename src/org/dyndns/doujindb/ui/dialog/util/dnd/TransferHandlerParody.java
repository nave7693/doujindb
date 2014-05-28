package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.Parody;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class TransferHandlerParody extends TransferHandlerEx<Parody>
{
	static private final DataFlavor pflavor = new ParodyDataFlavor();

	public TransferHandlerParody()
	{
		super();
		icon = Icon.desktop_explorer_parody.getImage();
		flavor = TransferHandlerParody.pflavor;
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Parody> data = new Vector<Parody>();
    	for(int index : table.getSelectedRows())
    		data.add((Parody) table.getValueAt(index, 0));
    	
    	return new ParodyTransferable(data, comp);
    }
	
	private static final class ParodyDataFlavor extends DataFlavor
	{
		private ParodyDataFlavor()
		{
			super("doujindb/record-parody", "DoujinDB.Record.Parody");
		}
	}
	
	private final class ParodyTransferable extends TransferableEx<Parody>
	{
		private ParodyTransferable(java.util.List<Parody> data, Component component)
		{
			super(data, component);
		}
	}
}
