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
		iconKeys.put("Plugin/Task/Completed", "task/completed.png");
		iconKeys.put("Plugin/Task/Error", "task/error.png");
		iconKeys.put("Plugin/Task/Queued", "task/queued.png");
		iconKeys.put("Plugin/Task/Remove", "task/remove.png");
		iconKeys.put("Plugin/Task/Reset", "task/reset.png");
		iconKeys.put("Plugin/Task/Running", "task/running.png");
		iconKeys.put("Plugin/Task/Stop", "task/stop.png");
		iconKeys.put("Plugin/Task/Warning", "task/warning.png");
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
