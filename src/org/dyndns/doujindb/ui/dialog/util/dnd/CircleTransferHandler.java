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
	public CircleTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_circle.getImage();
		flavor = new CircleDataFlavor();
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
	
	private final class CircleDataFlavor extends DataFlavorEx<Circle>
	{
		private CircleDataFlavor()
		{
			mime = "doujindb/record-circle";
        	name = "DoujinDB.Record.Circle";
        	clazz = Circle.class;
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
