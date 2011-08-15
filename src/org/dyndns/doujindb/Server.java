package org.dyndns.doujindb;

import java.net.MalformedURLException;
import java.rmi.*;

import org.dyndns.doujindb.dat.DataStore;
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
	public DataStore DS;
	
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
//			DS = (DataStore) Class.forName("org.dyndns.doujindb.dat.impl.DataStoreImpl").newInstance();
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
