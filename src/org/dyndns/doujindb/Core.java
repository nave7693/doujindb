package org.dyndns.doujindb;

import java.io.*;
import java.util.Vector;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.ui.*;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.rc.*;

/**  
* Core.java - DoujinDB core.
* @author  nozomu
* @version 1.0
*/
public final class Core implements Runnable
{
	public static Logger Logger;
	public static Properties Properties;
	public static Resources Resources;
	public static UI UI;
	public static Vector<Plugin>  Plugins;

	@Override
	public void run()
	{
		boolean isConfigurationWizard = false;
		
		new File(System.getProperty("user.home"), ".doujindb").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/lib").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/rc").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/log").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/plug").mkdir();
		try
		{
			Logger = org.dyndns.doujindb.log.impl.Factory.getService();
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		try
		{
			Properties = org.dyndns.doujindb.conf.impl.Factory.getService();
			Properties.load();
			Logger.log("System Properties loaded.", Level.INFO);
		} catch (Exception e)
		{
			Logger.log("Failed to load system Properties : " + e.getMessage() + ".", Level.ERROR);
			Logger.log("System Properties restored to default.", Level.INFO);
			isConfigurationWizard = true;
		}
		if(java.awt.GraphicsEnvironment.isHeadless()) 
		{
			Logger.log("DoujinDB cannot run on headless systems.", Level.ERROR);
			return;
		}
		Logger.log("Loading user interface ...", Level.INFO);
		try
		{
			Resources = new Resources();
			Resources.Font = Properties.get("org.dyndns.doujindb.ui.font").asFont();
		} catch (Exception e)
		{
			Core.Logger.log(e.getMessage(), Level.ERROR);
			return;
		}
		Core.Logger.log("System resources loaded.", Level.INFO);
		
		if(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString().equals(Core.Properties.get("org.dyndns.doujindb.dat.temp").asString()))
			Core.Logger.log("Repository folder is the temporary system folder.", Level.WARNING);
		Core.Logger.log("Repository loaded.", Level.INFO);
		
		String title = "DoujinDB v" + Core.class.getPackage().getSpecificationVersion();
		UI = new UI(title);
		Core.Logger.log("User interface loaded.", Level.INFO);
		
		if(isConfigurationWizard)
		{
			Core.Logger.log("Running first run wizard ...", Level.INFO);
			UI.showConfigurationWizard();
		}
		try
		{
			Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_RELOAD, null));
		} catch (Exception e) {
			Core.Logger.log(e.getMessage(), Level.ERROR);
		}
	}
}
