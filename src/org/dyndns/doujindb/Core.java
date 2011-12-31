package org.dyndns.doujindb;

import java.io.*;
import java.util.Vector;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.dat.Repository;
import org.dyndns.doujindb.dat.impl.RepositoryImpl;
import org.dyndns.doujindb.db.DataBase;
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
	public static DataBase Database;
	public static Repository Repository;

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
			Properties.get("org.dyndns.doujindb.ui.font").setValue(
					new java.awt.Font(
							Properties.get("org.dyndns.doujindb.ui.font").asFont().getFontName(),
							java.awt.Font.PLAIN,
							Properties.get("org.dyndns.doujindb.ui.font_size").asNumber()
							));
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
		
		Repository = new RepositoryImpl(new java.io.File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString()));
		if(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString().equals(Core.Properties.get("org.dyndns.doujindb.dat.temp").asString()))
			Core.Logger.log("Repository folder is the temporary system folder.", Level.WARNING);
		Core.Logger.log("Repository loaded.", Level.INFO);
		
		Database = DataBase.getInstance();
		
		String title = "DoujinDB v" + Core.class.getPackage().getSpecificationVersion();
		UI = new UI(title);
		Core.Logger.log("User interface loaded.", Level.INFO);
		
		if(isConfigurationWizard)
		{
			Core.Logger.log("Running configuration wizard ...", Level.INFO);
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
