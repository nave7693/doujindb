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
        ple.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss'Z'} [%thread] %-5level %logger{0} - %msg%n");
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
		 * Load global Configuration properties
		 * In case of Exception (e.g missing configuration file on first run) schedule the Configuration Wizard
		 */
		boolean isConfigurationWizard = false;
		try {
			ConfigurationParser.fromXML(Configuration.class, Configuration.CONFIG_FILE);
		} catch (ConfigurationException | IOException e) {
			LOG.error("Error loading system Configuration", e);
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
		
		/**
		 * Setup Logging
		 */
		((Logger)LoggerFactory.getLogger("org.apache.cayenne")).setLevel(Configuration.log_org_apache_cayenne.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.conf")).setLevel(Configuration.log_org_dyndns_doujindb_conf.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.dat")).setLevel(Configuration.log_org_dyndns_doujindb_dat.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.db")).setLevel(Configuration.log_org_dyndns_doujindb_db.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.plug")).setLevel(Configuration.log_org_dyndns_doujindb_plug.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.ui")).setLevel(Configuration.log_org_dyndns_doujindb_ui.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.util")).setLevel(Configuration.log_org_dyndns_doujindb_util.get());
		((Logger)LoggerFactory.getLogger("org.dyndns.doujindb.net")).setLevel(Configuration.log_org_dyndns_doujindb_net.get());
		
		/**
		 * Start the main User Interface
		 * We don't need to keep any reference to it, all public methods should be static anyway
		 */
		//TODO Catch Exceptions
		new UI();
		
		/**
		 * Bootstrap PluginManager: discover, load and run every Plugin
		 * //TODO Plugins should register listeners for DataBase/DataStore/UI/Configuration and decide for themselves when to "act"
		 */
		PluginManager.doBootstrap();
		
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
