package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.Convention;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class ConventionTransferHandler extends TransferHandlerEx<Convention>
{
	static private final DataFlavor eflavor = new ConventionDataFlavor();

	public ConventionTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_convention.getImage();
		flavor = ConventionTransferHandler.eflavor;
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Convention> data = new Vector<Convention>();
    	for(int index : table.getSelectedRows())
    		data.add((Convention) table.getValueAt(index, 0));
    	
    	return new ConventionTransferable(data, comp);
    }
	
	private static final class ConventionDataFlavor extends DataFlavor
	{
		private ConventionDataFlavor()
		{
			super("doujindb/record-convention", "Convention");
		}
	}
	
	private final class ConventionTransferable extends TransferableEx<Convention>
	{
		private ConventionTransferable(java.util.List<Convention> data, Component component)
		{
			super(data, component);
		}
	}
}
