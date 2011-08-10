package org.dyndns.doujindb;

import java.io.*;
import java.util.Vector;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.dat.*;
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
	public static DataStore Datastore;

	@Override
	public void run()
	{
		new File(System.getProperty("user.home"), ".doujindb").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/lib").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/rc").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/log").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/plug").mkdir();
		new File(System.getProperty("user.home"), ".doujindb/log").mkdir();
		try
		{
			Logger = org.dyndns.doujindb.log.impl.LoggerFactory.getService(
					org.dyndns.doujindb.log.impl.LoggerFactory.Type.STDOUT);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		try
		{
			Logger = org.dyndns.doujindb.log.impl.LoggerFactory.getService(
					org.dyndns.doujindb.log.impl.LoggerFactory.Type.FILE);
		} catch (Exception e)
		{
			Logger.log("Cannot load file logger : " + e.getMessage() + ".", Level.ERROR);
		}
		try
		{
			Properties = org.dyndns.doujindb.conf.impl.PropertiesFactory.getService( null );
			Properties.load();
			Logger.log("System Properties loaded.", Level.INFO);
		} catch (Exception e)
		{
			Logger.log("Failed to load system Properties : " + e.getMessage() + ".", Level.ERROR);
			Logger.log("System Properties restored to default.", Level.INFO);
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
		try
		{
			Datastore = org.dyndns.doujindb.core.dat.ServiceFactory.getService(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString());
		} catch (PropertyException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString().equals(Core.Properties.get("org.dyndns.doujindb.dat.temp").asString()))
			Core.Logger.log("DataStore folder is the temporary system folder.", Level.WARNING);
		Core.Logger.log("DataStore loaded.", Level.INFO);
		String title = "DoujinDB v" + Core.class.getPackage().getSpecificationVersion();
		UI = new UI(title);
		Core.Logger.log("User interface loaded.", Level.INFO);
		try
		{
			Core.UI.Desktop.validateUI(new DouzEvent(DouzEvent.DATABASE_RELOAD, null));
		} catch (Exception e) {
			Core.Logger.log(e.getMessage(), Level.ERROR);
		}
	}
}
