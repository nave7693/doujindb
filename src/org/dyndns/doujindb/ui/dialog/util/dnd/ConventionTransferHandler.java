package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class ConventionTransferHandler extends TransferHandlerEx<Convention>
{
	public ConventionTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_convention.getImage();
		flavor = new ConventionDataFlavor();
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
	
	private final class ConventionDataFlavor extends DataFlavorEx<Convention>
	{
		private ConventionDataFlavor()
		{
			mime = "doujindb/record-convention";
        	name = "Doujindb.Record.Convention";
        	clazz = Convention.class;
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
