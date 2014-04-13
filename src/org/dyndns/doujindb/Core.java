package org.dyndns.doujindb;

import java.io.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.log.*;
import org.dyndns.doujindb.plug.PluginManager;
import org.dyndns.doujindb.ui.*;

/**  
* Core.java - DoujinDB core.
* @author  nozomu
* @version 1.0
*/
public final class Core implements Runnable
{
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

	static
	{
		/**
		 * @see https://github.com/loli10K/doujindb/issues/2
		 * @see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7173464
		 */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("core-bootstrap");
		Logger.logInfo(TAG + "bootstrap started.");
		
		PluginManager.loadAll();
		
		boolean isConfigurationWizard = false;
		try
		{
			Logger.logInfo(TAG + "loading system configuration ...");
			Configuration.configLoad();
			Logger.logInfo(TAG + "system configuration loaded.");
			Logger.enableDebug((boolean) Configuration.configRead("org.dyndns.doujindb.log.debug"));
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
		
		if(Configuration.configRead("org.dyndns.doujindb.dat.datastore").equals(Configuration.configRead("org.dyndns.doujindb.dat.temp")))
			Logger.logWarning(TAG + "datastore directory is set to the temporary system directory.");
		
		if(!((Boolean)Configuration.configRead("org.dyndns.doujindb.log.cayenne")))
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		
		Logger.logInfo(TAG + "loading user interface ...");
		new UI();
		Logger.logInfo(TAG + "user interface loaded.");
		
		PluginManager.startAll();
		
		if(isConfigurationWizard)
		{
			Logger.logInfo(TAG + "running Configuration Wizard ...");
			UI.showConfigurationWizard();
		}
	}
}
