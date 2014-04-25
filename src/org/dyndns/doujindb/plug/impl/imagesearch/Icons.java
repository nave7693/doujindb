package org.dyndns.doujindb.plug.impl.imagesearch;

import java.lang.reflect.*;
import java.net.URL;

import javax.swing.ImageIcon;

import org.dyndns.doujindb.log.Logger;

public final class Icons
{
    public final ImageIcon icon = null;
    public final ImageIcon settings = null;
    public final ImageIcon search = null;
    public final ImageIcon search_preview = null;
    public final ImageIcon search_star = null;
    public final ImageIcon search_missing = null;
    
	private static final String TAG = "Icons : ";
	
	Icons()
	{
		for(Field field : Icons.class.getDeclaredFields())
		{
			if(field.getType().equals(ImageIcon.class))
			{
				String iconPath = field.getName().replaceAll("_", "/") + ".png";
				try {
					field.setAccessible(true);
					field.set(this, loadImage(iconPath));
					Logger.logDebug(TAG + "loaded icon resource '" + iconPath + "'");
					field.setAccessible(false);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					Logger.logFatal(TAG + "error loading resource icon '" + iconPath + "'", iae);
					System.exit(-1);
				}
			}
		}
	}

	private static ImageIcon loadImage(String iconPath)
	{
		URL rc = Icons.class.getResource("icons/" + iconPath);
		if(rc == null)
		{
			Logger.logFatal(TAG + "could not find resource icon '" + iconPath + "'");
			System.exit(-1);
		}
		return new ImageIcon(rc);
	}
}
