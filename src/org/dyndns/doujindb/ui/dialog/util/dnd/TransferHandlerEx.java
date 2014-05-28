package org.dyndns.doujindb.ui.dialog.util.dnd;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.ui.dialog.util.list.*;

@SuppressWarnings("serial")
abstract class TransferHandlerEx<T extends Record> extends TransferHandler
{
	protected boolean dragEnabled = false;
	protected boolean dropEnabled = false;
	protected Image icon;
	protected DataFlavor flavor;
	protected DataFlavor flavorComponentSource = new DataFlavor("dnd-transfer/source", "DND.TransferSource")
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
	
	public TransferHandlerEx() { }
	
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
		
		if (!info.isDataFlavorSupported(flavor))
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
	
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
    public boolean importData(TransferHandler.TransferSupport info)
    {
		if(!dropEnabled)
			return false;
		
		if (!info.isDrop())
            return false;
		
		if (!info.isDataFlavorSupported(flavor))
    		return false;
		
		java.util.List<T> data;
		JTable table = (JTable)info.getComponent();
		RecordList<T>.RecordTableModel<T> model = (RecordList<T>.RecordTableModel<T>) table.getModel();
		JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
		
		if(!table.isEnabled())
			return false;
		
		try {
			data = (java.util.List<T>) info.getTransferable().getTransferData(flavor);
			 for(T record : data)
				 model.addRecord(record);
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
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		super.exportDone(source, data, action);
	}
	
	abstract class TransferableEx<R extends T> implements Transferable
	{
		protected java.util.List<R> data;
		protected Component component;
		
		protected TransferableEx(java.util.List<R> data, Component component)
		{
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
			return new DataFlavor[]{ flavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return flavor.getMimeType().equals(flavor.getMimeType());
		}
	}
}
