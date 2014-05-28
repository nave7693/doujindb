package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import org.dyndns.doujindb.db.records.Artist;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class ArtistTransferHandler extends TransferHandlerEx<Artist>
{
	static private final DataFlavor aflavor = new ArtistDataFlavor();
	
	public ArtistTransferHandler()
	{
		super();
		icon = Icon.desktop_explorer_artist.getImage();
		flavor = ArtistTransferHandler.aflavor;
	}
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Artist> data = new Vector<Artist>();
    	for(int index : table.getSelectedRows())
    		data.add((Artist) table.getValueAt(index, 0));
    	
    	return new ArtistTransferable(data, comp);
    }
	
	private static final class ArtistDataFlavor extends DataFlavor
	{
		private ArtistDataFlavor()
		{
			super("doujindb/record-artist", "DoujinDB.Record.Artist");
		}
	}
	
	private final class ArtistTransferable extends TransferableEx<Artist>
	{
		private ArtistTransferable(java.util.List<Artist> data, Component component)
		{
			super(data, component);
		}
	}
}
