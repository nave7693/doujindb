package org.dyndns.doujindb.ui.rc;

import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.ImageIcon;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.Logger;

public final class Resources
{
	public Hashtable<String, String> Strings;
	public Hashtable<String, ImageIcon> Icons;
	public Font Font = new Font("Tahoma", java.awt.Font.PLAIN, 10);
	
	private static Logger Logger = Core.Logger;
	
	public Resources() throws Exception
	{
		Hashtable<String,String> iconKeys = new Hashtable<String,String>();
		iconKeys.put("Frame/Icon","frame/icon.png");
		iconKeys.put("Frame/Tray","frame/tray.png");
		iconKeys.put("Frame/Tray/Exit","frame/tray/exit.png");
		iconKeys.put("Frame/Loading","frame/loading.gif");
		iconKeys.put("Frame/Tab/Explorer","frame/tab/explorer.png");
		iconKeys.put("Frame/Tab/Explorer/Desktop","frame/tab/explorer/desktop.png");
		iconKeys.put("Frame/Tab/Explorer/Search","frame/tab/explorer/search.png");
		iconKeys.put("Frame/Tab/Explorer/Add","frame/tab/explorer/add.png");
		iconKeys.put("Frame/Tab/Explorer/StatusBar/Connect","frame/tab/explorer/statusbar/connect.png");
		iconKeys.put("Frame/Tab/Explorer/StatusBar/Disconnect","frame/tab/explorer/statusbar/disconnect.png");
		iconKeys.put("Frame/Tab/Explorer/StatusBar/Connected","frame/tab/explorer/statusbar/connected.png");
		iconKeys.put("Frame/Tab/Explorer/StatusBar/Disconnected","frame/tab/explorer/statusbar/disconnected.png");
		iconKeys.put("Frame/Tab/Explorer/StatusBar/Connecting","frame/tab/explorer/statusbar/connecting.gif");
		iconKeys.put("Frame/Tab/Logs","frame/tab/logs.png");
		iconKeys.put("Frame/Tab/Logs/Message","frame/tab/logs/message.png");
		iconKeys.put("Frame/Tab/Logs/Warning","frame/tab/logs/warning.png");
		iconKeys.put("Frame/Tab/Logs/Error","frame/tab/logs/error.png");
		iconKeys.put("Frame/Tab/Logs/Debug","frame/tab/logs/debug.png");
		iconKeys.put("Frame/Tab/Logs/Fatal","frame/tab/logs/fatal.png");
		iconKeys.put("Frame/Tab/Logs/Clear","frame/tab/logs/clear.png");
		iconKeys.put("Frame/Tab/Settings","frame/tab/settings.png");
		iconKeys.put("Frame/Tab/Settings/Save","frame/tab/settings/save.png");
		iconKeys.put("Frame/Tab/Settings/Load","frame/tab/settings/load.png");
		iconKeys.put("Frame/Tab/Settings/Tree/Directory","frame/tab/settings/tree/directory.png");
		iconKeys.put("Frame/Tab/Settings/Tree/Value","frame/tab/settings/tree/value.png");
		iconKeys.put("Frame/Tab/Settings/Editor/Close","frame/tab/settings/editor/close.png");
		iconKeys.put("Frame/Tab/Settings/Editor/Apply","frame/tab/settings/editor/apply.png");
		iconKeys.put("Frame/Tab/Settings/Editor/Discard","frame/tab/settings/editor/discard.png");
		iconKeys.put("Frame/Tab/Plugins","frame/tab/plugins.png");
		iconKeys.put("Frame/Tab/Plugins/Reload","frame/tab/plugins/reload.png");
		iconKeys.put("Frame/Tab/Network","frame/tab/network.png");
		iconKeys.put("Frame/Trash/Restore","frame/trash/restore.png");
		iconKeys.put("Frame/Trash/Delete","frame/trash/delete.png");
		iconKeys.put("Frame/Trash/Empty","frame/trash/empty.png");
		iconKeys.put("Frame/Trash/SelectAll","frame/trash/selectall.png");
		iconKeys.put("Frame/Trash/DeselectAll","frame/trash/deselectall.png");
		iconKeys.put("Frame/MediaManager/Refresh","frame/mediamanager/refresh.png");
		iconKeys.put("Frame/MediaManager/Import","frame/mediamanager/import.png");
		iconKeys.put("Frame/MediaManager/Export","frame/mediamanager/export.png");
		iconKeys.put("Frame/MediaManager/Delete","frame/mediamanager/delete.png");
		iconKeys.put("Frame/Dialog/About","frame/dialog/about.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Icon","frame/dialog/confwiz/icon.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Next","frame/dialog/confwiz/next.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Prev","frame/dialog/confwiz/prev.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Finish","frame/dialog/confwiz/finish.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Cancel","frame/dialog/confwiz/cancel.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Header","frame/dialog/confwiz/header.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Loading","frame/dialog/confwiz/loading.gif");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/DBTest","frame/dialog/confwiz/dbtest.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/DSTest","frame/dialog/confwiz/dstest.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Success","frame/dialog/confwiz/success.png");
		iconKeys.put("Frame/Dialog/ConfigurationWizard/Error","frame/dialog/confwiz/error.png");
		iconKeys.put("Frame/Dialog/About","frame/dialog/about.png");
		iconKeys.put("Desktop/Wallpaper/Import","desktop/wallpaper/import.png");
		iconKeys.put("Desktop/Trash/Full","desktop/trash/full.png");
		iconKeys.put("Desktop/Trash/Empty","desktop/trash/empty.png");
		iconKeys.put("Desktop/Trash/Disabled","desktop/trash/disabled.png");
		iconKeys.put("Desktop/Tools/Enabled","desktop/tools/enabled.png");
		iconKeys.put("Desktop/Tools/Disabled","desktop/tools/disabled.png");
		iconKeys.put("Desktop/SharedItems","desktop/shareditems.png");
		iconKeys.put("Desktop/SharedItems/Connected","desktop/shareditems/connected.png");
		iconKeys.put("Desktop/SharedItems/Disconnected","desktop/shareditems/disconnected.png");
		iconKeys.put("Desktop/SharedItems/Connecting","desktop/shareditems/connecting.png");
		iconKeys.put("Desktop/SharedItems/Disconnecting","desktop/shareditems/disconnecting.png");
		iconKeys.put("Desktop/SharedItems/Disabled","desktop/shareditems/disabled.png");
		iconKeys.put("Desktop/MediaManager/Enabled","desktop/mediamanager/enabled.png");
		iconKeys.put("Desktop/MediaManager/Disabled","desktop/mediamanager/disabled.png");
		iconKeys.put("Desktop/Explorer/DB","desktop/explorer/db.png");
		iconKeys.put("Desktop/Explorer/Folder","desktop/explorer/folder.png");
		iconKeys.put("Desktop/Explorer/Artist","desktop/explorer/artist.png");
		iconKeys.put("Desktop/Explorer/Circle","desktop/explorer/circle.png");
		iconKeys.put("Desktop/Explorer/Circle/Banner","desktop/explorer/circle/banner.png");
		iconKeys.put("Desktop/Explorer/Circle/Popup/Add","desktop/explorer/circle/popup/add.png");
		iconKeys.put("Desktop/Explorer/Circle/Popup/Remove","desktop/explorer/circle/popup/remove.png");
		iconKeys.put("Desktop/Explorer/Convention","desktop/explorer/convention.png");
		iconKeys.put("Desktop/Explorer/Content","desktop/explorer/content.png");
		iconKeys.put("Desktop/Explorer/Parody","desktop/explorer/parody.png");
		iconKeys.put("Desktop/Explorer/Book","desktop/explorer/book.png");
		iconKeys.put("Desktop/Explorer/Book/Info","desktop/explorer/book/info.png");
		iconKeys.put("Desktop/Explorer/Book/Rating/Checked","desktop/explorer/book/rating/checked.png");
		iconKeys.put("Desktop/Explorer/Book/Rating/Unchecked","desktop/explorer/book/rating/unchecked.png");
		iconKeys.put("Desktop/Explorer/Book/Media","desktop/explorer/book/media.png");
		iconKeys.put("Desktop/Explorer/Book/Cover","desktop/explorer/book/cover.png");
		iconKeys.put("Desktop/Explorer/Book/Popup/Add","desktop/explorer/book/popup/add.png");
		iconKeys.put("Desktop/Explorer/Book/Popup/Remove","desktop/explorer/book/popup/remove.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Repository","desktop/explorer/book/media/datastore.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Types/Folder","desktop/explorer/book/media/types/folder.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Types/Archive","desktop/explorer/book/media/types/archive.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Types/Unknown","desktop/explorer/book/media/types/unknown.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Download","desktop/explorer/book/media/download.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Upload","desktop/explorer/book/media/upload.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Delete","desktop/explorer/book/media/delete.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Browse","desktop/explorer/book/media/browse.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Reload","desktop/explorer/book/media/reload.png");
		iconKeys.put("Desktop/Explorer/Book/Media/Package","desktop/explorer/book/media/package.png");
		iconKeys.put("Desktop/Explorer/Trash","desktop/explorer/trash.png");
		iconKeys.put("Desktop/Explorer/Tools","desktop/explorer/tools.png");
		iconKeys.put("Desktop/Explorer/SharedItems","desktop/explorer/shareditems.png");
		iconKeys.put("Desktop/Explorer/MediaManager","desktop/explorer/mediamanager.png");
		iconKeys.put("Desktop/Explorer/Edit","desktop/explorer/edit.png");
		iconKeys.put("Desktop/Explorer/Delete","desktop/explorer/delete.png");
		iconKeys.put("Desktop/Explorer/Remove","desktop/explorer/remove.png");
		iconKeys.put("Desktop/Explorer/Clone","desktop/explorer/clone.png");
		iconKeys.put("Desktop/Explorer/ComboBox/Arrow","desktop/explorer/combobox/arrow.png");
		iconKeys.put("Desktop/Explorer/Table/View/List","desktop/explorer/table/view/list.png");
		iconKeys.put("Desktop/Explorer/Table/View/Preview","desktop/explorer/table/view/preview.png");
		iconKeys.put("JDesktop/IFrame/Iconify","jdesktop/iframe/iconify.png");
		iconKeys.put("JDesktop/IFrame/Minimize","jdesktop/iframe/minimize.png");
		iconKeys.put("JDesktop/IFrame/Maximize","jdesktop/iframe/maximize.png");
		iconKeys.put("JDesktop/IFrame/Close","jdesktop/iframe/close.png");
		iconKeys.put("FileChooser/detailsViewIcon","filechooser/detailsview.png");
		iconKeys.put("FileChooser/homeFolderIcon","filechooser/homefolder.png");
		iconKeys.put("FileChooser/listViewIcon","filechooser/listview.png");
		iconKeys.put("FileChooser/newFolderIcon","filechooser/newfolder.png");
		iconKeys.put("FileChooser/upFolderIcon","filechooser/upfolder.png");
		iconKeys.put("FileChooser/computerIcon","filechooser/computer.png");
		iconKeys.put("FileChooser/directoryIcon","filechooser/folder.png");
		iconKeys.put("FileChooser/fileIcon","filechooser/file.png");
		iconKeys.put("FileChooser/floppyDriveIcon","filechooser/floppy.png");
		iconKeys.put("FileChooser/hardDriveIcon","filechooser/drive.png");
		iconKeys.put("FileView/Folder","fileview/folder.png");
		iconKeys.put("FileView/Default","fileview/default.png");
		iconKeys.put("FileView/Disk","fileview/disk.png");
		iconKeys.put("FileView/Database","fileview/database.png");
		iconKeys.put("FileView/Archive","fileview/archive.png");
		iconKeys.put("FileView/Image","fileview/image.png");
		iconKeys.put("FileView/Text","fileview/text.png");
		iconKeys.put("JPanel/ToggleButton/Checked","jpanel/togglebutton/checked.png");
		iconKeys.put("JPanel/ToggleButton/Unchecked","jpanel/togglebutton/unchecked.png");
		iconKeys.put("JSlider/ThumbIcon","jslider/thumbicon.png");
		iconKeys.put("JTree/Node-","jtree/node-minus.png");
		iconKeys.put("JTree/Node+","jtree/node-plus.png");
		iconKeys.put("MenuBar/Logs","menubar/logs.png");
		iconKeys.put("MenuBar/Logs/Message","menubar/logs/message.png");
		iconKeys.put("MenuBar/Logs/Warning","menubar/logs/warning.png");
		iconKeys.put("MenuBar/Logs/Error","menubar/logs/error.png");
		iconKeys.put("MenuBar/Help","menubar/help.png");
		iconKeys.put("MenuBar/Help/About","menubar/help/about.png");
		iconKeys.put("MenuBar/Help/Bugtrack","menubar/help/bugtrack.png");
		iconKeys.put("Dialog/Message","dialog/message.png");
		iconKeys.put("Dialog/Warning","dialog/warning.png");
		iconKeys.put("Dialog/Error","dialog/error.png");
		iconKeys.put("Dialog/Confirm","dialog/confirm.png");
		
		Icons = new Hashtable<String, ImageIcon>();
		for(String key : iconKeys.keySet())
			try
			{
				Icons.put(key, new ImageIcon(Resources.class.getResource("icons/" + iconKeys.get(key))));
			} catch(NullPointerException npe) {
				throw new Exception("Icon resource '" + key + "' not found.");
			}
		try
		{
			ZipFile zip = new ZipFile(new File(Core.DOUJINDB_HOME, "icons.zip"));
			Enumeration<? extends ZipEntry> ze = zip.entries();
			while(ze.hasMoreElements())
			{
				String entry = ze.nextElement().getName();
				try
				{
					if(entry.endsWith("/"))
						// ZipEntry of 'folder' type
						// We don't need to load this
						continue;
					if(!iconKeys.containsValue(entry))
					{
						Logger.logWarning("Resource '" + entry + "' does not match any system key and won't be loaded");
						continue;
					}
					Icons.put(entry, new ImageIcon(javax.imageio.ImageIO.read(zip.getInputStream(zip.getEntry(entry)))));
				} catch(Exception e) {
					Logger.logError("Error while loading resource '" + entry + "' from external bundle", e);
				}
			}
			zip.close();
		} catch(FileNotFoundException fnfe) {
			// An external resource bundle was not found
			// We can ignore this error
		}
	}
}
