package org.dyndns.doujindb.ui;

import java.lang.reflect.*;
import java.net.URL;

import javax.swing.ImageIcon;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

public final class Icons
{
	public final ImageIcon window_icon = null;
	public final ImageIcon window_tray = null;
	public final ImageIcon window_tray_exit = null;
	public final ImageIcon window_loading = null;
	public final ImageIcon window_tab_explorer = null;
	public final ImageIcon window_tab_explorer_desktop = null;
	public final ImageIcon window_tab_explorer_search = null;
	public final ImageIcon window_tab_explorer_add = null;
	public final ImageIcon window_tab_explorer_remove = null;
	public final ImageIcon window_tab_explorer_statusbar_connect = null;
	public final ImageIcon window_tab_explorer_statusbar_disconnect = null;
	public final ImageIcon window_tab_explorer_statusbar_connected = null;
	public final ImageIcon window_tab_explorer_statusbar_disconnected = null;
	public final ImageIcon window_tab_logs = null;
	public final ImageIcon window_tab_logs_message = null;
	public final ImageIcon window_tab_logs_warning = null;
	public final ImageIcon window_tab_logs_error = null;
	public final ImageIcon window_tab_logs_debug = null;
	public final ImageIcon window_tab_logs_fatal = null;
	public final ImageIcon window_tab_logs_clear = null;
	public final ImageIcon window_tab_settings = null;
	public final ImageIcon window_tab_settings_save = null;
	public final ImageIcon window_tab_settings_load = null;
	public final ImageIcon window_tab_settings_tree_directory = null;
	public final ImageIcon window_tab_settings_tree_value = null;
	public final ImageIcon window_tab_settings_editor_close = null;
	public final ImageIcon window_tab_settings_editor_apply = null;
	public final ImageIcon window_tab_settings_editor_discard = null;
	public final ImageIcon window_tab_plugins = null;
	public final ImageIcon window_tab_plugins_reload = null;
	public final ImageIcon window_tab_network = null;
	public final ImageIcon window_trash_restore = null;
	public final ImageIcon window_trash_delete = null;
	public final ImageIcon window_trash_empty = null;
	public final ImageIcon window_trash_selectall = null;
	public final ImageIcon window_trash_deselectall = null;
	public final ImageIcon window_mediamanager_refresh = null;
	public final ImageIcon window_mediamanager_import = null;
	public final ImageIcon window_mediamanager_export = null;
	public final ImageIcon window_mediamanager_delete = null;
	public final ImageIcon window_dialog_about = null;
	public final ImageIcon window_dialog_configwiz_icon = null;
	public final ImageIcon window_dialog_configwiz_next = null;
	public final ImageIcon window_dialog_configwiz_prev = null;
	public final ImageIcon window_dialog_configwiz_finish = null;
	public final ImageIcon window_dialog_configwiz_cancel = null;
	public final ImageIcon window_dialog_configwiz_header = null;
	public final ImageIcon window_dialog_configwiz_loading = null;
	public final ImageIcon window_dialog_configwiz_reload = null;
	public final ImageIcon window_dialog_configwiz_download = null;
	public final ImageIcon window_dialog_configwiz_success = null;
	public final ImageIcon window_dialog_configwiz_error = null;
	public final ImageIcon desktop_wallpaper_import = null;
	public final ImageIcon desktop_trash_full = null;
	public final ImageIcon desktop_trash_empty = null;
	public final ImageIcon desktop_trash_disabled = null;
	public final ImageIcon desktop_tools_enabled = null;
	public final ImageIcon desktop_tools_disabled = null;
	public final ImageIcon desktop_shareditems = null;
	public final ImageIcon desktop_shareditems_connected = null;
	public final ImageIcon desktop_shareditems_disconnected = null;
	public final ImageIcon desktop_shareditems_connecting = null;
	public final ImageIcon desktop_shareditems_disconnecting = null;
	public final ImageIcon desktop_shareditems_disabled = null;
	public final ImageIcon desktop_mediamanager_enabled = null;
	public final ImageIcon desktop_mediamanager_disabled = null;
	public final ImageIcon desktop_explorer_db = null;
	public final ImageIcon desktop_explorer_folder = null;
	public final ImageIcon desktop_explorer_artist = null;
	public final ImageIcon desktop_explorer_circle = null;
	public final ImageIcon desktop_explorer_circle_banner = null;
	public final ImageIcon desktop_explorer_circle_popup_add = null;
	public final ImageIcon desktop_explorer_circle_popup_remove = null;
	public final ImageIcon desktop_explorer_convention = null;
	public final ImageIcon desktop_explorer_content = null;
	public final ImageIcon desktop_explorer_parody = null;
	public final ImageIcon desktop_explorer_book = null;
	public final ImageIcon desktop_explorer_book_info = null;
	public final ImageIcon desktop_explorer_book_rating_checked = null;
	public final ImageIcon desktop_explorer_book_rating_unchecked = null;
	public final ImageIcon desktop_explorer_book_cover = null;
	public final ImageIcon desktop_explorer_book_popup_add = null;
	public final ImageIcon desktop_explorer_book_popup_remove = null;
	public final ImageIcon desktop_explorer_book_media = null;
	public final ImageIcon desktop_explorer_book_media_repository = null;
	public final ImageIcon desktop_explorer_book_media_types_folder = null;
	public final ImageIcon desktop_explorer_book_media_types_archive = null;
	public final ImageIcon desktop_explorer_book_media_types_unknown = null;
	public final ImageIcon desktop_explorer_book_media_download = null;
	public final ImageIcon desktop_explorer_book_media_upload = null;
	public final ImageIcon desktop_explorer_book_media_delete = null;
	public final ImageIcon desktop_explorer_book_media_browse = null;
	public final ImageIcon desktop_explorer_book_media_reload = null;
	public final ImageIcon desktop_explorer_book_media_package = null;
	public final ImageIcon desktop_explorer_trash = null;
	public final ImageIcon desktop_explorer_tools = null;
	public final ImageIcon desktop_explorer_shareditems = null;
	public final ImageIcon desktop_explorer_mediamanager = null;
	public final ImageIcon desktop_explorer_edit = null;
	public final ImageIcon desktop_explorer_delete = null;
	public final ImageIcon desktop_explorer_remove = null;
	public final ImageIcon desktop_explorer_clone = null;
	public final ImageIcon desktop_explorer_combobox_arrow = null;
	public final ImageIcon desktop_explorer_table_view_list = null;
	public final ImageIcon desktop_explorer_table_view_preview = null;
	public final ImageIcon jdesktop_iframe_iconify = null;
	public final ImageIcon jdesktop_iframe_minimize = null;
	public final ImageIcon jdesktop_iframe_maximize = null;
	public final ImageIcon jdesktop_iframe_close = null;
	public final ImageIcon filechooser_detailsview = null;
	public final ImageIcon filechooser_homefolder = null;
	public final ImageIcon filechooser_listview = null;
	public final ImageIcon filechooser_newfolder = null;
	public final ImageIcon filechooser_upfolder = null;
	public final ImageIcon filechooser_computer = null;
	public final ImageIcon filechooser_directory = null;
	public final ImageIcon filechooser_file = null;
	public final ImageIcon filechooser_floppydrive = null;
	public final ImageIcon filechooser_harddrive = null;
	public final ImageIcon fileview_folder = null;
	public final ImageIcon fileview_default = null;
	public final ImageIcon fileview_disk = null;
	public final ImageIcon fileview_database = null;
	public final ImageIcon fileview_archive = null;
	public final ImageIcon fileview_image = null;
	public final ImageIcon fileview_text = null;
	public final ImageIcon jpanel_togglebutton_checked = null;
	public final ImageIcon jpanel_togglebutton_unchecked = null;
	public final ImageIcon jslider_thumbicon = null;
	public final ImageIcon jtree_node_collapse = null;
	public final ImageIcon jtree_node_expand = null;
	public final ImageIcon menubar_logs = null;
	public final ImageIcon menubar_logs_message = null;
	public final ImageIcon menubar_logs_warning = null;
	public final ImageIcon menubar_logs_error = null;
	public final ImageIcon menubar_help = null;
	public final ImageIcon menubar_help_about = null;
	public final ImageIcon menubar_help_bugtrack = null;
	public final ImageIcon menubar_help_update = null;
	public final ImageIcon dialog_message = null;
	public final ImageIcon dialog_warning = null;
	public final ImageIcon dialog_error = null;
	public final ImageIcon dialog_confirm = null;
	
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
