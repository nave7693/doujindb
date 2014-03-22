package org.dyndns.doujindb;

import java.awt.Font;
import java.io.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.dat.Repository;
import org.dyndns.doujindb.dat.impl.RepositoryImpl;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.log.*;
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
	public static Resources Resources;
	public static UI UI;
	public static DataBase Database;
	public static Repository Repository;
	
	public final static File DOUJINDB_HOME = getHomedir();
	
	private static final String TAG = "Core : ";
	
	private static File getHomedir()
	{
		File homedir;
		try
		{
			homedir = new File(java.net.URLDecoder.decode(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParentFile();
		} catch (UnsupportedEncodingException uee)
		{
			homedir = new File(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		}
		return homedir;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("core-bootstrap");
		Logger.logInfo(TAG + "bootstrap started.");
		
		Logger.logInfo(TAG + "discovering plugins ...");
		PluginManager.discovery();
		
		boolean isConfigurationWizard = false;
		try
		{
			Logger.logInfo(TAG + "loading system configuration ...");
			Configuration.configLoad();
			Logger.logInfo(TAG + "system configuration loaded.");
		} catch (ConfigurationException ce)
		{
			Logger.logError(TAG + "failed to load system configuration.", ce);
			Logger.logInfo(TAG + "Configuration Wizard scheduled to run on startup.");
			isConfigurationWizard = true;
		}
		if(java.awt.GraphicsEnvironment.isHeadless()) 
		{
			Logger.logFatal(TAG + "DoujinDB cannot run on headless systems.");
			System.exit(-1);
		}
		
		Logger.logInfo(TAG + "loading repository ...");
		Repository = new RepositoryImpl(new java.io.File(Configuration.configRead("org.dyndns.doujindb.dat.datastore").toString()));
		if(Configuration.configRead("org.dyndns.doujindb.dat.datastore").equals(Configuration.configRead("org.dyndns.doujindb.dat.temp")))
			Logger.logWarning(TAG + "repository folder is set to the temporary system folder.");
		Logger.logInfo(TAG + "repository loaded.");
		
		try
		{
			Logger.logInfo(TAG + "loading resources ...");
			Resources = new Resources();
			Resources.Font = new java.awt.Font(
				((Font)Configuration.configRead("org.dyndns.doujindb.ui.font")).getFontName(),
				java.awt.Font.PLAIN,
				((Integer)Configuration.configRead("org.dyndns.doujindb.ui.font_size")));
		} catch (Exception e)
		{
			Logger.logFatal(e.getMessage(), e);
			return;
		}
		Logger.logInfo(TAG + "resources loaded.");
		
		if(!((Boolean)Configuration.configRead("org.dyndns.doujindb.log.cayenne")))
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		
		Database = DataBase.getInstance();
		
		Logger.logInfo(TAG + "loading user interface ...");
		UI = new UI();
		Logger.logInfo(TAG + "user interface loaded.");
		
		PluginManager.startup();
		
		if(isConfigurationWizard)
		{
			Logger.logInfo(TAG + "running Configuration Wizard ...");
			UI.showConfigurationWizard();
		}
	}
}
