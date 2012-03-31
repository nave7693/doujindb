package org.dyndns.doujindb.ui.rc;

import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.ImageIcon;

public final class Resources
{
	public Hashtable<String, String> Strings;
	public Hashtable<String, ImageIcon> Icons;
	public Font Font = new Font("Tahoma", java.awt.Font.PLAIN, 10);
	
	public Resources() throws Exception
	{
		Hashtable<String,String> iconKeys = new Hashtable<String,String>();
		iconKeys.put("JFrame/Icon", "jframe/icon.png");
		iconKeys.put("JFrame/Tray","jframe/tray.png");
		iconKeys.put("JFrame/Tray/Exit","jframe/tray/exit.png");
		iconKeys.put("JFrame/Loading","jframe/loading.gif");
		iconKeys.put("JMenuBar/Logs","jmenubar/logs.png");
		iconKeys.put("JMenuBar/Logs/Message","jmenubar/logs/message.png");
		iconKeys.put("JMenuBar/Logs/Warning","jmenubar/logs/warning.png");
		iconKeys.put("JMenuBar/Logs/Error","jmenubar/logs/error.png");
		iconKeys.put("JMenuBar/Help","jmenubar/help.png");
		iconKeys.put("JMenuBar/Help/About","jmenubar/help/about.png");
		iconKeys.put("JMenuBar/Help/Bugtrack","jmenubar/help/bugtrack.png");
		iconKeys.put("JFrame/Tab/Explorer","jframe/tab/explorer.png");
		iconKeys.put("JFrame/Tab/Explorer/Desktop","jframe/tab/explorer/desktop.png");
		iconKeys.put("JFrame/Tab/Explorer/Search","jframe/tab/explorer/search.png");
		iconKeys.put("JFrame/Tab/Explorer/Add","jframe/tab/explorer/add.png");
		iconKeys.put("JFrame/Tab/Explorer/Import","jframe/tab/explorer/import.png");
		iconKeys.put("JFrame/Tab/Explorer/Export","jframe/tab/explorer/export.png");
		iconKeys.put("JFrame/Tab/Explorer/Commit","jframe/tab/explorer/commit.png");
		iconKeys.put("JFrame/Tab/Explorer/Rollback","jframe/tab/explorer/rollback.png");
		iconKeys.put("JFrame/Tab/Explorer/StatusBar/Connect","jframe/tab/explorer/statusbar/connect.png");
		iconKeys.put("JFrame/Tab/Explorer/StatusBar/Disconnect","jframe/tab/explorer/statusbar/disconnect.png");
		iconKeys.put("JFrame/Tab/Explorer/StatusBar/Connected","jframe/tab/explorer/statusbar/connected.png");
		iconKeys.put("JFrame/Tab/Explorer/StatusBar/Disconnected","jframe/tab/explorer/statusbar/disconnected.png");
		iconKeys.put("JFrame/Tab/Explorer/StatusBar/Connecting","jframe/tab/explorer/statusbar/connecting.gif");
		iconKeys.put("JFrame/Tab/Logs","jframe/tab/logs.png");
		iconKeys.put("JFrame/Tab/Logs/Message","jframe/tab/logs/message.png");
		iconKeys.put("JFrame/Tab/Logs/Warning","jframe/tab/logs/warning.png");
		iconKeys.put("JFrame/Tab/Logs/Error","jframe/tab/logs/error.png");
		iconKeys.put("JFrame/Tab/Logs/Clear","jframe/tab/logs/clear.png");
		iconKeys.put("JFrame/Tab/Settings","jframe/tab/settings.png");
		iconKeys.put("JFrame/Tab/Settings/Save","jframe/tab/settings/save.png");
		iconKeys.put("JFrame/Tab/Settings/Load","jframe/tab/settings/load.png");
		iconKeys.put("JFrame/Tab/Settings/Tree/Directory","jframe/tab/settings/tree/directory.png");
		iconKeys.put("JFrame/Tab/Settings/Tree/Value","jframe/tab/settings/tree/value.png");
		iconKeys.put("JFrame/Tab/Settings/Editor/Close","jframe/tab/settings/editor/close.png");
		iconKeys.put("JFrame/Tab/Settings/Editor/Apply","jframe/tab/settings/editor/apply.png");
		iconKeys.put("JFrame/Tab/Settings/Editor/Discard","jframe/tab/settings/editor/discard.png");
		iconKeys.put("JFrame/Tab/Plugins","jframe/tab/plugins.png");
		iconKeys.put("JFrame/Tab/Network","jframe/tab/network.png");
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
		iconKeys.put("JDesktop/IFrame/Iconify","jdesktop/iframe/iconify.png");
		iconKeys.put("JDesktop/IFrame/Minimize","jdesktop/iframe/minimize.png");
		iconKeys.put("JDesktop/IFrame/Maximize","jdesktop/iframe/maximize.png");
		iconKeys.put("JDesktop/IFrame/Close","jdesktop/iframe/close.png");
		iconKeys.put("JSlider/ThumbIcon","jslider/thumbicon.png");
		iconKeys.put("JDialog/Message","jdialog/message.png");
		iconKeys.put("JDialog/Warning","jdialog/warning.png");
		iconKeys.put("JDialog/Error","jdialog/error.png");
		iconKeys.put("JDialog/Confirm","jdialog/confirm.png");
		iconKeys.put("JDesktop/Explorer/?","jdesktop/explorer/unknown.png");
		iconKeys.put("JDesktop/Explorer/DB","jdesktop/explorer/db.png");
		iconKeys.put("JDesktop/Explorer/Folder","jdesktop/explorer/folder.png");
		iconKeys.put("JDesktop/Explorer/Artist","jdesktop/explorer/artist.png");
		iconKeys.put("JDesktop/Explorer/Circle","jdesktop/explorer/circle.png");
		iconKeys.put("JDesktop/Explorer/Circle/Banner","jdesktop/explorer/circle/banner.png");
		iconKeys.put("JDesktop/Explorer/Circle/Popup/Add","jdesktop/explorer/circle/popup/add.png");
		iconKeys.put("JDesktop/Explorer/Circle/Popup/Remove","jdesktop/explorer/circle/popup/remove.png");
		iconKeys.put("JDesktop/Explorer/Convention","jdesktop/explorer/convention.png");
		iconKeys.put("JDesktop/Explorer/Content","jdesktop/explorer/content.png");
		iconKeys.put("JDesktop/Explorer/Parody","jdesktop/explorer/parody.png");
		iconKeys.put("JDesktop/Explorer/Book","jdesktop/explorer/book.png");
		iconKeys.put("JDesktop/Explorer/Book/Info","jdesktop/explorer/book/info.png");
		iconKeys.put("JDesktop/Explorer/Book/Rating/Checked","jdesktop/explorer/book/rating/checked.png");
		iconKeys.put("JDesktop/Explorer/Book/Rating/Unchecked","jdesktop/explorer/book/rating/unchecked.png");
		iconKeys.put("JDesktop/Explorer/Book/Media","jdesktop/explorer/book/media.png");
		iconKeys.put("JDesktop/Explorer/Book/Cover","jdesktop/explorer/book/cover.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Repository","jdesktop/explorer/book/media/datastore.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Types/Folder","jdesktop/explorer/book/media/types/folder.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Types/Archive","jdesktop/explorer/book/media/types/archive.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Types/Unknown","jdesktop/explorer/book/media/types/unknown.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Download","jdesktop/explorer/book/media/download.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Upload","jdesktop/explorer/book/media/upload.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Delete","jdesktop/explorer/book/media/delete.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Browse","jdesktop/explorer/book/media/browse.png");
		iconKeys.put("JDesktop/Explorer/Book/Media/Reload","jdesktop/explorer/book/media/reload.png");
		iconKeys.put("JDesktop/Explorer/RecycleBin","jdesktop/explorer/recyclebin.png");
		iconKeys.put("JDesktop/Explorer/SharedItems","jdesktop/explorer/shareditems.png");
		iconKeys.put("JDesktop/Explorer/MediaManager","jdesktop/explorer/mediamanager.png");
		iconKeys.put("JDesktop/Explorer/Edit","jdesktop/explorer/edit.png");
		iconKeys.put("JDesktop/Explorer/Delete","jdesktop/explorer/delete.png");
		iconKeys.put("JDesktop/Explorer/Clone","jdesktop/explorer/clone.png");
		iconKeys.put("JDesktop/Explorer/ComboBox/Arrow","jdesktop/explorer/combobox/arrow.png");
		iconKeys.put("JFrame/RecycleBin/Restore","jframe/recyclebin/restore.png");
		iconKeys.put("JFrame/RecycleBin/Delete","jframe/recyclebin/delete.png");
		iconKeys.put("JFrame/RecycleBin/Empty","jframe/recyclebin/empty.png");
		iconKeys.put("JFrame/RecycleBin/SelectAll","jframe/recyclebin/selectall.png");
		iconKeys.put("JFrame/RecycleBin/DeselectAll","jframe/recyclebin/deselectall.png");
		iconKeys.put("JFrame/MediaManager/Refresh","jframe/mediamanager/refresh.png");
		iconKeys.put("JFrame/MediaManager/Import","jframe/mediamanager/import.png");
		iconKeys.put("JFrame/MediaManager/Export","jframe/mediamanager/export.png");
		iconKeys.put("JFrame/MediaManager/Delete","jframe/mediamanager/delete.png");
		iconKeys.put("JFrame/Dialog/About","jframe/dialog/about.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Icon","jframe/dialog/confwiz/icon.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Next","jframe/dialog/confwiz/next.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Prev","jframe/dialog/confwiz/prev.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Finish","jframe/dialog/confwiz/finish.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Cancel","jframe/dialog/confwiz/cancel.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Header","jframe/dialog/confwiz/header.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Loading","jframe/dialog/confwiz/loading.gif");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/DBTest","jframe/dialog/confwiz/dbtest.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/DSTest","jframe/dialog/confwiz/dstest.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Success","jframe/dialog/confwiz/success.png");
		iconKeys.put("JFrame/Dialog/ConfigurationWizard/Error","jframe/dialog/confwiz/error.png");
		iconKeys.put("JFrame/Dialog/About","jframe/dialog/about.png");
		iconKeys.put("JDesktop/Wallpaper/Import","jdesktop/wallpaper/import.png");
		iconKeys.put("JDesktop/RecycleBin/Full","jdesktop/recyclebin/full.png");
		iconKeys.put("JDesktop/RecycleBin/Empty","jdesktop/recyclebin/empty.png");
		iconKeys.put("JDesktop/RecycleBin/Disabled","jdesktop/recyclebin/disabled.png");
		iconKeys.put("JDesktop/SharedItems","jdesktop/shareditems.png");
		iconKeys.put("JDesktop/SharedItems/Connected","jdesktop/shareditems/connected.png");
		iconKeys.put("JDesktop/SharedItems/Disconnected","jdesktop/shareditems/disconnected.png");
		iconKeys.put("JDesktop/SharedItems/Connecting","jdesktop/shareditems/connecting.png");
		iconKeys.put("JDesktop/SharedItems/Disconnecting","jdesktop/shareditems/disconnecting.png");
		iconKeys.put("JDesktop/SharedItems/Disabled","jdesktop/shareditems/disabled.png");
		iconKeys.put("JDesktop/MediaManager/Enabled","jdesktop/mediamanager/enabled.png");
		iconKeys.put("JDesktop/MediaManager/Disabled","jdesktop/mediamanager/disabled.png");
		iconKeys.put("JPanel/ToggleButton/Checked","jpanel/togglebutton/checked.png");
		iconKeys.put("JPanel/ToggleButton/Unchecked","jpanel/togglebutton/unchecked.png");
		iconKeys.put("JTree/Node-","jtree/node-.png");
		iconKeys.put("JTree/Node+","jtree/node+.png");
		
		Icons = new Hashtable<String,ImageIcon>();
		for(String key : iconKeys.keySet())
			try
			{
				Icons.put(key, new ImageIcon(Resources.class.getResource("icons/" + iconKeys.get(key))));
			}catch(NullPointerException npe)
			{
				npe.printStackTrace();
				throw new Exception("Icon resource " + key + " not found.");
			}
		try
		{
			ZipFile zip = new ZipFile(new File(System.getProperty("doujindb.home"), "icons.zip"));
			Vector<String> entries = new Vector<String>();
			Enumeration<? extends ZipEntry> e = zip.entries();
			while(e.hasMoreElements())
				entries.add(e.nextElement().getName());
			for(String key : iconKeys.keySet())
				try
				{
					if(!entries.contains(iconKeys.get(key)))
						continue;
					Icons.put(key, new ImageIcon(javax.imageio.ImageIO.read(zip.getInputStream(zip.getEntry(iconKeys.get(key))))));
				}catch(Exception exc)
				{
					exc.printStackTrace();
				}
			zip.close();
		}catch(Exception e)
		{
			;
		}
	}
}
