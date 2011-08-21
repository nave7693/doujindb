package org.dyndns.doujindb;

import java.net.*;
import java.rmi.*;

import org.dyndns.doujindb.dat.impl.*;
import org.dyndns.doujindb.dat.rmi.*;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.log.Level;

/**  
* Server.java - DoujinDB Server.
* @author nozomu
* @version 1.0
*/
public final class Server
{
	public DataBase DB;
	public RMIDataStoreImpl DS;
	
	public Server()
	{
		final int port = Core.Properties.get("org.dyndns.doujindb.net.listen_port").asNumber();
		
		new Thread(Server.class.getName() + "/RMIRegistry")
		{
			@Override
			public void run()
			{
				super.setPriority(Thread.MIN_PRIORITY);
				try
				{
					java.rmi.registry.LocateRegistry.createRegistry(port);
					Core.Logger.log("RMI Registry loaded.", Level.INFO);
					//System.setProperty("java.rmi.server.hostname", "192.168.1.201");
					//System.setProperty("java.rmi.server.useLocalHostname", "false");
					//System.out.println(System.getProperty("java.rmi.server.hostname"));
				} catch (RemoteException re) {
					re.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		try
		{
			DB = (DataBase) Class.forName("org.dyndns.doujindb.db.impl.DataBaseImpl").newInstance();
			Naming.rebind("rmi://localhost:" + port + "/DataBase", DB);
			Core.Logger.log("DataBase service loaded.", Level.INFO);
			DS = //new RemoteDataStore(
					new RMIDataStoreImpl(
							new DataStoreImpl(
									new java.io.File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString())
							)
						//)
					);
			Naming.rebind("rmi://localhost:" + port + "/DataStore", DS);
			Core.Logger.log("DataStore service loaded.", Level.INFO);
		} catch (InstantiationException ie) {
			// TODO Auto-generated catch block
			ie.printStackTrace();
		} catch (IllegalAccessException iae) {
			// TODO Auto-generated catch block
			iae.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			// TODO Auto-generated catch block
			cnfe.printStackTrace();
		} catch (RemoteException re) {
			// TODO Auto-generated catch block
			re.printStackTrace();
		} catch (MalformedURLException mue) {
			// TODO Auto-generated catch block
			mue.printStackTrace();
		}
		
//		try
//		{
//			DS = (DataStore) Class.forName("org.dyndns.doujindb.dat.impl.RemoteDataStore").newInstance();
//			Naming.rebind("rmi://localhost:7111/DataStore", DS);
//			Core.Logger.log("DataStore service loaded.", Level.INFO);
//		} catch (InstantiationException ie) {
//			// TODO Auto-generated catch block
//			ie.printStackTrace();
//		} catch (IllegalAccessException iae) {
//			// TODO Auto-generated catch block
//			iae.printStackTrace();
//		} catch (ClassNotFoundException cnfe) {
//			// TODO Auto-generated catch block
//			cnfe.printStackTrace();
//		}
	}
}
