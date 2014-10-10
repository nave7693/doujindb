package org.dyndns.doujindb;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;

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
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(Core.class);
	
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
	
	static
	{
		/**
		 * @see https://github.com/loli10K/doujindb/issues/2
		 * @see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7173464
		 */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		/**
		 *  Setup SLF4J + Logback programmatically
		 *  We don't provide any Logback configuration file (logback.groovy, logback.xml) by default
		 *  User can provide custom logback.xml configuration running the JVM with -Dlogback.configurationFile=/path/to/config.xml
		 *  @see http://logback.qos.ch/manual/configuration.html
		 */
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss'Z'} [%-20.-20thread] %-5level %logger{0} - %msg%n");
        ple.setContext(lc);
        ple.start();
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders();
        /** 
         * Remove all default appenders from RootLogger (probably only the default appender configured by ch.qos.logback.classic.BasicConfigurator)
         * @see http://logback.qos.ch/manual/configuration.html (step 4)
         */
        while(i.hasNext())
        	logger.detachAppender(i.next());
        // Attach our appender to RootLogger
        logger.addAppender(consoleAppender);
        // Default logging level is INFO
        logger.setLevel(Level.INFO);
        // Don't let 'child' loggers inherit appenders
        logger.setAdditive(false);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("core-bootstrap");
		LOG.info("Core bootstrap started");
		
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
			LOG.error("Error loading system Configuration", ce);
			LOG.info("Scheduling Configuration Wizard");
			isConfigurationWizard = true;
		}
		
		/**
		 * We need AWT/Swing (graphical user interface) to run DoujinDB
		 * Quit if it's not available (running "Headless")
		 */
		if(java.awt.GraphicsEnvironment.isHeadless()) {
			LOG.error("DoujinDB cannot run on headless systems");
			System.exit(1);
		}
		
		//FIXME should be checked by the DataStore itself
		if(Configuration.configRead("org.dyndns.doujindb.dat.datastore").equals(Configuration.configRead("org.dyndns.doujindb.dat.temp")))
			LOG.warn("datastore directory is set to the temporary system directory");
		
		/**
		 * Load and setup Logging related Configuration
		 * Defaults to level 'INFO' in case of any Exception
		 */
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
				((Logger) LoggerFactory.getLogger(configurationKey)).setLevel(Level.valueOf(level));
				LOG.info("Configured log level [{}] for [{}]", level, configurationKey);
			} catch (Exception e) {
				LOG.error("Error loading logging configuration level for [{}]", configurationKey, e);
				LOG.info("Configuring log level [{}] for [{}]", "INFO", configurationKey);
				((Logger) LoggerFactory.getLogger(configurationKey)).setLevel(Level.INFO);
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
				LOG.error("Error loading Configuration Wizard", pve);
			}
		}
	}
}
