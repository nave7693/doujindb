package org.dyndns.doujindb.plug.impl.imagescanner.rc;

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
		iconKeys.put("Plugin/Loading", "loading.gif");
		iconKeys.put("Plugin/Settings", "settings.png");
		iconKeys.put("Plugin/Settings/Build", "settings/build.png");
		iconKeys.put("Plugin/Settings/Cancel", "settings/cancel.png");
		iconKeys.put("Plugin/Settings/Confirm", "settings/confirm.png");
		iconKeys.put("Plugin/Settings/Preview", "settings/preview.png");
		iconKeys.put("Plugin/Search", "search.png");
		iconKeys.put("Plugin/Search/Open", "search/open.png");
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
