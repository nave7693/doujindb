package org.dyndns.doujindb.ui.desk.panels.utils;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.*;
import org.dyndns.doujindb.ui.desk.events.*;




@SuppressWarnings({"unchecked", "serial", "rawtypes","unused"})
public final class DouzCheckBoxList<T extends Record> extends JPanel implements Validable, LayoutManager
{
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	private JScrollPane scrollPane;
	private JList listData;
	private JTextField filterField;
	private Model model;
	private Hashtable<Class,ImageIcon> iconData;
	
	public DouzCheckBoxList(Iterable<T> data, JTextField filter)
	{
		this(data, filter, null);
	}
	public DouzCheckBoxList(Iterable<T> data, JTextField filter, Hashtable<Class,ImageIcon> icons)
	{
		super();
		super.setLayout(this);
		filterField = filter;
		iconData = icons;
		CheckBoxItem<T>[] checkboxItems = buildCheckBoxItems(data);
		listData = new JList();
		listData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listData.setFont(font);
		listData.setCellRenderer(new Renderer());
		listData.addMouseListener(new MouseAdapter()
   		{
			public void mouseClicked(MouseEvent me)
   			{
				if(me.getButton() != MouseEvent.BUTTON1)
					return;
   				int selectedIndex = listData.locationToIndex(me.getPoint());
   				if (selectedIndex < 0)
					return;
   				CheckBoxItem<T> item = (CheckBoxItem<T>)listData.getModel().getElementAt(selectedIndex);
   				item.setChecked(!item.isChecked());
   				listData.setSelectedIndex(selectedIndex);
   				listData.repaint();
   			}
   		});
		model = new Model(checkboxItems);
		listData.setModel(model);
		listData.addMouseListener(new MouseAdapter()
		{
      		public void mouseClicked(MouseEvent me)
      		{
				if(me.getButton() == MouseEvent.BUTTON1)
					return;
	  			int selectedIndex = listData.locationToIndex(me.getPoint());
	  			if (selectedIndex < 0)
					return;
	  			CheckBoxItem<T> item = (CheckBoxItem<T>)listData.getModel().getElementAt(selectedIndex);
        		Object token = item.getItem();
        		try {
	        		if(token instanceof Artist)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_ARTIST, (Record)token);
	        		}
	        		if(token instanceof Book)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_BOOK, (Record)token);
	        		}
	        		if(token instanceof Circle)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CIRCLE, (Record)token);
	        		}
	        		if(token instanceof Convention)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONVENTION, (Record)token);
	        		}
	        		if(token instanceof Content)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_CONTENT, (Record)token);
	        		}
	        		if(token instanceof Parody)
	        		{
	        			Core.UI.Desktop.openWindow(DouzWindow.Type.WINDOW_PARODY, (Record)token);
	        		}
				} catch (DataBaseException dbe) {
					Core.Logger.log(dbe.getMessage(), Level.ERROR);
					dbe.printStackTrace();
				} catch (RemoteException re) {
					Core.Logger.log(re.getMessage(), Level.ERROR);
					re.printStackTrace();
				}
      		}
		});
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(listData);
		listData.setFixedCellHeight(16);
		add(scrollPane);
   		setVisible(true);
	}
	
	@Override
	public void addMouseListener(MouseListener listener)
	{
		listData.addMouseListener(listener);
	}
	
	private CheckBoxItem<T>[] buildCheckBoxItems(Iterable<T> items)
	{
		Vector<CheckBoxItem<T>> tmp = new Vector<CheckBoxItem<T>>();
		for(T item : items)
		{
			tmp.add(new CheckBoxItem<T>(item));
		}
		CheckBoxItem<T>[] checkboxItems = tmp.toArray(new CheckBoxItem[0]);
		return checkboxItems;
  	}
	private class CheckBoxItem<K>
	{
		private boolean isChecked;
		private K item;
		public CheckBoxItem(K o)
		{
			item = o;
			isChecked = false;
		}
		public boolean isChecked()
		{
			return isChecked;
		}
		public void setChecked(boolean value)
		{
			isChecked = value;
		}
		public K getItem()
		{
			return item;
		}
	}
	private class Model extends AbstractListModel implements Validable
	{
		ArrayList<CheckBoxItem<T>> items;
		ArrayList<CheckBoxItem<T>> filterItems;
		
		public Model(CheckBoxItem<T> data[])
		{
			items = new ArrayList<CheckBoxItem<T>>();
			for(CheckBoxItem<T> o : data)
				items.add(o);
			filterItems = new ArrayList<CheckBoxItem<T>>();
			for(CheckBoxItem<T> o : data)
				filterItems.add(o);
		}
		public Object getElementAt (int index)
		{
			if (index < filterItems.size())
				return filterItems.get (index);
			else
				return null;
		}
		public int getSize()
		{
			return filterItems.size();
		}
		private void refreshUI()
		{
			try
			{
				filterItems.clear();
				String term = filterField.getText();
				if(term.equals(""))
					for (int i=0; i<items.size(); i++)
						if (items.get(i).isChecked())
							filterItems.add(items.get(i));
						else
							;
				else
					if(term.equals("!"))
						for (int i=0; i<items.size(); i++)
							if (!items.get(i).isChecked())
								filterItems.add(items.get(i));
							else
								;
					else
						for (int i=0; i<items.size(); i++)
							if (items.get(i).getItem().toString().matches(term))
								filterItems.add(items.get(i));
			}catch(PatternSyntaxException pse){}
			fireContentsChanged(this, 0, getSize());
		}
		@Override
		public void validateUI(DouzEvent ve)
		{
			switch(ve.getType())
			{
			case DouzEvent.DATABASE_REFRESH:
				refreshUI();
				break;
			case DouzEvent.DATABASE_ITEMCHANGED:
				refreshUI();
				break;
			case DouzEvent.DATABASE_ITEMADDED:
				model.items.add(new CheckBoxItem(ve.getParameter()));
				refreshUI();
				break;
			case DouzEvent.DATABASE_ITEMREMOVED:
				CheckBoxItem<?> removed = null;
				for(CheckBoxItem<?> cbi : model.items)
					if(cbi.getItem() == ve.getParameter())
					{
						removed = cbi;
						break;
					}
				if(removed != null)
					model.items.remove(removed);
				refreshUI();
				break;
			default:
				;
			}
		}
	}
	
	private class Renderer extends JCheckBox implements ListCellRenderer
	{ 
		public Renderer()
		{
			setBackground(UIManager.getColor("List.textBackground"));
			setForeground(UIManager.getColor("List.textForeground"));
		}		
		public Component getListCellRendererComponent(JList listBox, Object obj, int currentindex, boolean isChecked, boolean hasFocus)
		{
			setSelected(((CheckBoxItem<T>)obj).isChecked());
			setText(((CheckBoxItem<T>)obj).getItem().toString());
			setFont(font);
			return this;
		}
	}
	
	@Override
	public void addLayoutComponent(String name, Component c) {}

	@Override
	public void layoutContainer(Container c)
	{
		scrollPane.setBounds(0,0,c.getWidth(), c.getHeight());
	}

	@Override
	public Dimension minimumLayoutSize(Container c) {
		return new Dimension(250, 150);
	}

	@Override
	public Dimension preferredLayoutSize(Container c) {
		return new Dimension(250, 250);
	}

	@Override
	public void removeLayoutComponent(Component c) {}
	
	@Override
	public void validateUI(DouzEvent ve)
	{
		model.validateUI(ve);
		listData.validate();
	}
	
	public void setSelectedItems(Iterable<T> items)
	{
		for(CheckBoxItem<T> cb : model.items)
			cb.setChecked(false);
		for(T item : items)
			for(CheckBoxItem<T> cb : model.items)
			{
				if(cb.getItem() == item)
					cb.setChecked(true);
			}
		validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
	}
	
	public Iterable<T> getSelectedItems()
	{
		Vector<T> v = new Vector<T>();
		for(CheckBoxItem<T> cb : model.filterItems)
		{
			if(cb.isChecked())
				v.add((T)cb.getItem());
		}
		return v;
	}
	
	public int getSelectedItemCount()
	{
		int count = 0;
		for(CheckBoxItem<T> cb : model.filterItems)
		{
			if(cb.isChecked())
				count++;
		}
		return count;
	}
	
	public void setItems(Iterable<T> items)
	{
		model.items.clear();
		for(T item : items)
			model.items.add(new CheckBoxItem<T>(item));
		validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
	}
	
	public Iterable<T> getItems()
	{
		Vector<T> v = new Vector<T>();
		for(CheckBoxItem<T> cb : model.items)
		{
			v.add((T)cb.getItem());
		}
		return v;
	}
	
	public int getItemCount()
	{
		return model.items.size();
	}
}
