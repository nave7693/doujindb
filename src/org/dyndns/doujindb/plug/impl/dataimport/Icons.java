package org.dyndns.doujindb.plug.impl.dataimport;

import java.lang.reflect.*;
import java.net.URL;

import javax.swing.ImageIcon;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

public final class Icons
{
    public final ImageIcon icon = null;
    public final ImageIcon add = null;
    public final ImageIcon loading = null;
    public final ImageIcon info = null;
    public final ImageIcon refresh = null;
    public final ImageIcon settings = null;
    public final ImageIcon tasks = null;
    public final ImageIcon search = null;
    public final ImageIcon confirm = null;
    public final ImageIcon cancel = null;
    public final ImageIcon cache = null;
    public final ImageIcon task_pause = null;
    public final ImageIcon task_resume = null;
    public final ImageIcon task_delete = null;
    public final ImageIcon task_reset = null;
    public final ImageIcon task_skip = null;
    public final ImageIcon task_book = null;
    public final ImageIcon task_xml = null;
    public final ImageIcon task_import = null;
    public final ImageIcon task_folder = null;
    public final ImageIcon task_download = null;
    public final ImageIcon task_searchquery_star = null;
    public final ImageIcon task_preview_missing = null;
    public final ImageIcon task_state_new = null;
    public final ImageIcon task_state_running = null;
    public final ImageIcon task_state_complete = null;
    public final ImageIcon task_state_warning = null;
    public final ImageIcon task_state_error = null;
    public final ImageIcon task_state_abort = null;
    public final ImageIcon task_state_unknow = null;
    public final ImageIcon search_open = null;
    public final ImageIcon search_preview = null;
    public final ImageIcon search_star = null;
    public final ImageIcon task_metadata_artist = null;
    public final ImageIcon task_metadata_book = null;
    public final ImageIcon task_metadata_circle = null;
    public final ImageIcon task_metadata_content = null;
    public final ImageIcon task_metadata_convention = null;
    public final ImageIcon task_metadata_parody = null;
    public final ImageIcon task_result_info = null;
    public final ImageIcon task_result_artist = null;
    public final ImageIcon task_result_circle = null;
    public final ImageIcon task_result_content = null;
    public final ImageIcon task_result_convention = null;
    public final ImageIcon task_result_parody = null;
    public final ImageIcon task_result_missing = null;
	
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
