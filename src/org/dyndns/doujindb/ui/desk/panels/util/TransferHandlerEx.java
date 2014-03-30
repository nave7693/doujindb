package org.dyndns.doujindb.ui.desk.panels.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public final class TransferHandlerEx extends TransferHandler
{
	public enum Type
	{
		ARTIST,
		BOOK,
		CIRCLE,
		CONTENT,
		CONVENTION,
		PARODY
	}
	
	private boolean dragEnabled = false;
	private boolean dropEnabled = false;
	private Type type;
	private Image icon;
	
	private static DataFlavor flavorComponentSource = new DataFlavor("dnd-transfer/source", "DND.TransferSource")
	{
		@Override
		public String getMimeType()
		{
			return "dnd-transfer/source";
		}

		@Override
		public String getHumanPresentableName()
		{
			return "DND.TransferSource";
		}

		@Override
		public Class<?> getRepresentationClass()
		{
			return Serializable.class;
		}
	};
	private static HashMap<Type, DataFlavor> flavors;
	{
		flavors = new HashMap<Type, DataFlavor>();
		for(Type t : Type.values())
			flavors.put(t, new DataFlavorEx(t));
	}
	
	private static HashMap<Type, Image> icons;
	{
		icons = new HashMap<Type, Image>();
		icons.put(Type.ARTIST, Icon.desktop_explorer_artist.getImage());
		icons.put(Type.BOOK, Icon.desktop_explorer_book.getImage());
		icons.put(Type.CIRCLE, Icon.desktop_explorer_circle.getImage());
		icons.put(Type.CONTENT, Icon.desktop_explorer_content.getImage());
		icons.put(Type.CONVENTION, Icon.desktop_explorer_convention.getImage());
		icons.put(Type.PARODY, Icon.desktop_explorer_parody.getImage());
	}
	
	public TransferHandlerEx(Type type)
	{
		super();
		
		this.type = type;
		icon = icons.get(type);
	}
	
	public boolean getDragEnabled()
	{
		return this.dragEnabled;
	}
	
	public boolean getDropEnabled()
	{
		return this.dropEnabled;
	}
	
	public void setDragEnabled(boolean drag)
	{
		this.dragEnabled = drag;
	}
	
	public void setDropEnabled(boolean drop)
	{
		this.dropEnabled = drop;
	}
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		if(!dropEnabled)
			return false;
		
		if (!info.isDataFlavorSupported(flavors.get(type)))
    		return false;
		
		try {
			/**
			 * We don't want to let the user drop items on the same component he's dragging from.
			 * Transferable.getTransferData(DataFlavor['dnd-transfer/source']) called from a TransferableEx returns the original Component.
			 */
			if(info.getTransferable().getTransferData(flavorComponentSource).equals(info.getComponent()))
				return false;
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		
		JTable table = (JTable)info.getComponent();
		if(!table.isEnabled())
			return false;
		
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
    public boolean importData(TransferHandler.TransferSupport info)
    {
		if(!dropEnabled)
			return false;
		
		if (!info.isDrop())
            return false;
		
		if (!info.isDataFlavorSupported(flavors.get(type)))
    		return false;
		
		java.util.List<Record> data;
		JTable table = (JTable)info.getComponent();
		RecordList.RecordTableModel model = (RecordList.RecordTableModel)table.getModel();
		JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
		
		if(!table.isEnabled())
			return false;
		
		try {
			data = (java.util.List<Record>) info.getTransferable().getTransferData(flavors.get(type));
			 for(Record rcd : data)
				 model.addRecord(rcd);
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;
    }
	
	@Override
    public int getSourceActions(JComponent comp)
    {
		if(!dragEnabled)
			return NONE;
		
    	this.setDragImage(icon);
    	this.setDragImageOffset(new Point(18, 0));
    	return COPY;
    }
	
	@Override
    protected Transferable createTransferable(JComponent comp)
    {
		if(!dragEnabled)
			return null;
		
		JTable table = (JTable)comp;
    	final java.util.List<Record> data = new Vector<Record>();
    	for(int index : table.getSelectedRows())
    		data.add((Record)table.getValueAt(index, 0));
    	
    	return new TransferableEx(type, data, comp);
    }
	
	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		super.exportDone(source, data, action);
	}
	
	private final class DataFlavorEx extends DataFlavor
	{
		private String mime = "doujindb/unknown";
		private Class<?> clazz = Record.class;
		private String name = "Doujindb.Record.Unknown";
		
		private DataFlavorEx(Type type)
		{
			super("doujindb/unknown", "Doujindb.Record.Unknown");
			
			switch(type)
            {
            case ARTIST:
            	mime = "doujindb/record-artist";
            	name = "Doujindb.Record.Artist";
            	clazz = Artist.class;
            	break;
            case BOOK:
            	mime = "doujindb/record-book";
            	name = "Doujindb.Record.Book";
            	clazz = Book.class;
            	break;
            case CIRCLE:
            	mime = "doujindb/record-circle";
            	name = "Doujindb.Record.Circle";
            	clazz = Circle.class;
            	break;
            case CONTENT:
            	mime = "doujindb/record-content";
            	name = "Doujindb.Record.Content";
            	clazz = Content.class;
            	break;
            case CONVENTION:
            	mime = "doujindb/record-convention";
            	name = "Doujindb.Record.Convention";
            	clazz = Convention.class;
            	break;
            case PARODY:
            	mime = "doujindb/record-parody";
            	name = "Doujindb.Record.Parody";
            	clazz = Parody.class;
            	break;
            }
		}
		
		@Override
		public String getMimeType()
		{
			return mime;
		}

		@Override
		public String getHumanPresentableName()
		{
			return name;
		}

		@Override
		public Class<?> getRepresentationClass()
		{
			return clazz;
		}
	}
	
	private final class TransferableEx implements Transferable
	{
		private Type type;
		private java.util.List<Record> data;
		private Component component;
		
		private TransferableEx(Type type, java.util.List<Record> data, Component component)
		{
			super();
			
			this.type = type;
			this.data = data;
			this.component = component;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			if(flavor.equals(flavorComponentSource))
			{
				/**
				 * We don't want to let the user drop items on the same component he's dragging from.
				 * Transferable.getTransferData((DataFlavor['dnd-transfer/source']) called from a TransferableEx returns the original Component.
				 */
				return component;
			} else
			if(!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return data;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[]{ flavors.get(type) };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return flavor.getMimeType().equals(flavors.get(type).getMimeType());
		}
	}
}