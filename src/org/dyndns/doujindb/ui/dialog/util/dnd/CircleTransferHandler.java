package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.Circle;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class CircleTransferHandler extends TransferHandlerEx<Circle>
{
	static private final DataFlavor cflavor = new CircleDataFlavor();

	public CircleTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_circle.getImage();
		flavor = CircleTransferHandler.cflavor;
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Circle> data = new Vector<Circle>();
    	for(int index : table.getSelectedRows())
    		data.add((Circle) table.getValueAt(index, 0));
    	
    	return new CircleTransferable(data, comp);
    }
	
	private static final class CircleDataFlavor extends DataFlavor
	{
		private CircleDataFlavor()
		{
			super("doujindb/record-circle", "DoujinDB.Record.Circle");
		}
	}
	
	private final class CircleTransferable extends TransferableEx<Circle>
	{
		private CircleTransferable(java.util.List<Circle> data, Component component)
		{
			super(data, component);
		}
	}
}
