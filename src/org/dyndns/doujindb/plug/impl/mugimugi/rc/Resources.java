package org.dyndns.doujindb.plug.impl.mugimugi.rc;

import java.util.*;

import javax.swing.ImageIcon;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.Level;

public final class Resources
{
	public Hashtable<String, ImageIcon> Icons;
	
	public Resources()
	{
		Hashtable<String,String> iconKeys = new Hashtable<String,String>();
		
		iconKeys.put("Plugin/Icon", "plugin.png");
		iconKeys.put("Plugin/Add", "add.png");
		iconKeys.put("Plugin/Loading", "loading.gif");
		iconKeys.put("Plugin/Info", "info.png");
		iconKeys.put("Plugin/Refresh", "refresh.png");
		iconKeys.put("Plugin/Settings", "settings.png");
		iconKeys.put("Plugin/Tasks", "tasks.png");
		iconKeys.put("Plugin/Search", "search.png");
		iconKeys.put("Plugin/Confirm", "confirm.png");
		iconKeys.put("Plugin/Cancel", "cancel.png");
		iconKeys.put("Plugin/Cache", "cache.png");
		iconKeys.put("Plugin/Task/Pause", "task/pause.png");
		iconKeys.put("Plugin/Task/Resume", "task/resume.png");
		iconKeys.put("Plugin/Task/Delete", "task/delete.png");
		iconKeys.put("Plugin/Task/Reset", "task/reset.png");
		iconKeys.put("Plugin/Task/Skip", "task/skip.png");
		iconKeys.put("Plugin/Task/Book", "task/book.png");
		iconKeys.put("Plugin/Task/XML", "task/xml.png");
		iconKeys.put("Plugin/Task/Import", "task/import.png");
		iconKeys.put("Plugin/Task/Folder", "task/folder.png");
		iconKeys.put("Plugin/Task/Download", "task/download.png");
		iconKeys.put("Plugin/Task/SearchQuery/Star", "task/searchquery/star.png");
		iconKeys.put("Plugin/Task/Preview/Missing", "task/preview/missing.png");
		iconKeys.put("Plugin/Task/Info/Idle", "task/info/idle.png");
		iconKeys.put("Plugin/Task/Info/Running", "task/info/running.png");
		iconKeys.put("Plugin/Task/Info/Completed", "task/info/completed.png");
		iconKeys.put("Plugin/Task/Info/Warning", "task/info/warning.png");
		iconKeys.put("Plugin/Task/Info/Error", "task/info/error.png");
		iconKeys.put("Plugin/Task/Info/Stopped", "task/info/stopped.png");
		iconKeys.put("Plugin/Task/Info/Paused", "task/info/paused.png");
		iconKeys.put("Plugin/Search/Open", "search/open.png");
		iconKeys.put("Plugin/Search/Preview", "search/preview.png");
		iconKeys.put("Plugin/Search/Star", "search/star.png");
		
		Icons = new Hashtable<String,ImageIcon>();
		for(String key : iconKeys.keySet())
			try
			{
				Icons.put(key, new ImageIcon(Resources.class.getResource("icons/" + iconKeys.get(key))));
			} catch(NullPointerException npe)
			{
				npe.printStackTrace();
				Icons.put(key, new ImageIcon());
				Core.Logger.log("Icon resource " + key + " not found.", Level.ERROR);
			}
	}
}
