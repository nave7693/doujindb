package org.dyndns.doujindb;

import java.beans.PropertyVetoException;
import java.io.*;

import org.apache.commons.logging.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.plug.PluginManager;
import org.dyndns.doujindb.ui.*;
import org.dyndns.doujindb.ui.dialog.DialogConfigurationWizard;

/**  
* Core.java - DoujinDB core.
* @author  nozomu
* @version 1.0
*/
public final class Core implements Runnable
{
	public final static File DOUJINDB_HOME = getHomedir();
	
	private static final Log Logger = getLogger();
	
	private static File getHomedir()
	{
		File homedir;
		try {
			homedir = new File(java.net.URLDecoder.decode(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParentFile();
		} catch (UnsupportedEncodingException uee) {
			homedir = new File(Core.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		}
		return homedir;
	}
	
	private static Log getLogger()
	{
		// setup default Apache's commons-logging implementation
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "info");
		System.setProperty("org.apache.commons.logging.simplelog.showlogname", "false");
		System.setProperty("org.apache.commons.logging.simplelog.showShortLogname", "true");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss'Z'");
		// return default logger instance
		return LogFactory.getLog(Core.class);
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
		Logger.info("Core bootstrap started");
		
		/**
		 * Load all Plugins without starting them:
		 *    some Plugins may need the DataBase and/or DataStore online to be started
		 *    also Configuration properties have yet to be loaded
		 */
		PluginManager.loadAll();
		
		/**
		 * Load global Configuration properties
		 * In case of Exception (e.g missing configuration file on first run) schedule the Configuration Wizard
		 */
		boolean isConfigurationWizard = false;
		try {
			Configuration.configLoad();
		} catch (ConfigurationException ce) {
			Logger.error("Error loading system Configuration", ce);
			Logger.info("Scheduling Configuration Wizard");
			isConfigurationWizard = true;
		}
		
		/**
		 * We need AWT/Swing (graphical user interface) to run DoujinDB
		 * Quit if it's not available (running "Headless")
		 */
		if(java.awt.GraphicsEnvironment.isHeadless()) {
			Logger.fatal("DoujinDB cannot run on headless systems");
			System.exit(1);
		}
		
		//FIXME should be checked by the DataStore itself
		if(Configuration.configRead("org.dyndns.doujindb.dat.datastore").equals(Configuration.configRead("org.dyndns.doujindb.dat.temp")))
			Logger.warn("datastore directory is set to the temporary system directory");
		
		/**
		 * Load and setup Logging related Configuration
		 * Defaults to level 'info' in case of any Exception
		 */
		String baseCommonsLogging = "org.apache.commons.logging.simplelog.log.";
		String baseConfiguration = "org.dyndns.doujindb.log.";
		String[] configurationSet = new String[]{
			"org.apache.cayenne",
			"org.dyndns.doujindb.conf",
			"org.dyndns.doujindb.dat",
			"org.dyndns.doujindb.db",
			"org.dyndns.doujindb.plug",
			"org.dyndns.doujindb.ui",
			"org.dyndns.doujindb.util",
			"org.dyndns.doujindb.net"
		};
		for(String configurationKey : configurationSet) {
			try {
				String level = (String) Configuration.configRead(baseConfiguration + configurationKey);
				System.setProperty(baseCommonsLogging + configurationKey, level);
				Logger.info(String.format("Configured log level [%s] for [%s]", level, configurationKey));
			} catch (Exception e) {
				Logger.error(String.format("Error loading logging configuration level for [%s]", configurationKey), e);
				Logger.info(String.format("Configuring log level [%s] for [%s]", "info", configurationKey));
				System.setProperty(baseCommonsLogging + configurationKey, "info");
			}
		}
		
		/**
		 * Start the main User Interface
		 * We don't need to keep any reference to it, all public methods should be static anyway
		 */
		//TODO Catch Exceptions
		new UI();
		
		/**
		 * Now that we have up and running Logging, Configuration and User Interface we can manually start every Plugin instance
		 * //FIXME Plugins should register listeners for DataBase/DataStore/UI/Configuration and decide for themselves when to "start"
		 */
		PluginManager.startAll();
		
		/**
		 * Run now the Configuration Wizard if an Exception occurred during Configuration load
		 */
		if(isConfigurationWizard) {
			try {
				UI.Desktop.showDialog(new DialogConfigurationWizard());
			} catch (PropertyVetoException pve) {
				Logger.error("Error loading Configuration Wizard", pve);
			}
		}
	}
}
