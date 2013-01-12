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
		iconKeys.put("Plugin/CleanCompleted", "cleancompleted.png");
		iconKeys.put("Plugin/DeleteSelected", "deleteselected.png");
		iconKeys.put("Plugin/Task/Pause", "task/pause.png");
		iconKeys.put("Plugin/Task/Resume", "task/resume.png");
		iconKeys.put("Plugin/Task/Rerun", "task/rerun.png");
		iconKeys.put("Plugin/Task/Skip", "task/skip.png");
		iconKeys.put("Plugin/Task/Book", "task/book.png");
		iconKeys.put("Plugin/Task/Folder", "task/folder.png");
		iconKeys.put("Plugin/Task/Download", "task/download.png");
		iconKeys.put("Plugin/Task/SearchQuery/Star", "task/searchquery/star.png");
		iconKeys.put("Plugin/Task/Preview/Missing", "task/preview/missing.png");
		iconKeys.put("Plugin/Task/Step/Idle", "task/step/idle.png");
		iconKeys.put("Plugin/Task/Step/Running", "task/step/running.gif");
		iconKeys.put("Plugin/Task/Step/Completed", "task/step/completed.png");
		iconKeys.put("Plugin/Task/Step/Warning", "task/step/warning.png");
		iconKeys.put("Plugin/Task/Step/Error", "task/step/error.png");
		
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
