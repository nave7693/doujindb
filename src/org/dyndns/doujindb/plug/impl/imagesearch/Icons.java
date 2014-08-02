package org.dyndns.doujindb.plug.impl.imagesearch;

import java.lang.reflect.*;
import java.net.URL;

import javax.swing.ImageIcon;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

public final class Icons
{
    public final ImageIcon icon = null;
    public final ImageIcon settings = null;
    public final ImageIcon search = null;
    public final ImageIcon search_preview = null;
    public final ImageIcon search_star = null;
    public final ImageIcon search_missing = null;
    public final ImageIcon worker_start = null;
    public final ImageIcon worker_pause = null;
    public final ImageIcon worker_stop = null;
    
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(Icons.class);
    
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
					LOG.debug("Loaded ImageIcon [{}]", iconPath);
					field.setAccessible(false);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.error("Error loading ImageIcon [{}]", iconPath, iae);
				}
			}
		}
	}

	private static ImageIcon loadImage(String iconPath)
	{
		URL rc = Icons.class.getResource("icons/" + iconPath);
		if(rc == null)
			LOG.error("Error loading ImageIcon [{}]", iconPath);
		return new ImageIcon(rc);
	}
}
