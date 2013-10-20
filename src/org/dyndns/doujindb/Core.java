package org.dyndns.doujindb;

import java.io.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.dat.Repository;
import org.dyndns.doujindb.dat.impl.RepositoryImpl;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.log.impl.SystemLogger;
import org.dyndns.doujindb.plug.PluginManager;
import org.dyndns.doujindb.ui.*;
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
	public static DataBase Database;
	public static Repository Repository;

	@Override
	public void run()
	{
		boolean isConfigurationWizard = false;
		try
		{
			System.setProperty("doujindb.home", new File(java.net.URLDecoder.decode(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParent());
		} catch (UnsupportedEncodingException uee)
		{
			System.setProperty("doujindb.home", new File(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());
		}
		try
		{
			Logger = new SystemLogger();
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		try
		{
			Properties = org.dyndns.doujindb.conf.impl.Factory.getService();
			Properties.load();
			Logger.logInfo("System Properties loaded.");
		} catch (Exception e)
		{
			Logger.logError("Failed to load system Properties", e);
			Logger.logInfo("System Properties restored to default.");
			isConfigurationWizard = true;
		}
		if(java.awt.GraphicsEnvironment.isHeadless()) 
		{
			Logger.logFatal("DoujinDB cannot run on headless systems.");
			return;
		}
		
		Repository = new RepositoryImpl(new java.io.File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString()));
		if(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString().equals(Core.Properties.get("org.dyndns.doujindb.dat.temp").asString()))
			Logger.logWarning("Repository folder is the temporary system folder.");
		Logger.logInfo("Repository loaded.");
		
		Logger.logInfo("Loading user interface ...");
		try
		{
			Resources = new Resources();
			Resources.Font = new java.awt.Font(
				Properties.get("org.dyndns.doujindb.ui.font").asFont().getFontName(),
				java.awt.Font.PLAIN,
				Properties.get("org.dyndns.doujindb.ui.font_size").asNumber());
		} catch (Exception e)
		{
			Logger.logFatal(e.getMessage(), e);
			return;
		}
		Logger.logInfo("Resources loaded.");
		
		if(!Properties.get("org.dyndns.doujindb.log.cayenne").asBoolean())
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		
		Database = DataBase.getInstance();
		
		Logger.logInfo("Discovering plugins ...");
		PluginManager.discovery();
		
		UI = new UI();
		Logger.logInfo("User interface loaded.");
		
		if(isConfigurationWizard)
		{
			Logger.logInfo("Running configuration wizard ...");
			UI.showConfigurationWizard();
		}
	}
}
