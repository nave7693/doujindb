package org.dyndns.doujindb;

import java.net.*;
import java.rmi.*;

import org.dyndns.doujindb.conf.PropertyException;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.dat.impl.*;
import org.dyndns.doujindb.dat.rmi.*;
import org.dyndns.doujindb.db.impl.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.log.Level;

/**  
* Server.java - DoujinDB Server.
* @author nozomu
* @version 1.0
*/
public final class Server
{
	public static RMIDataBase DB;
	public static RMIDataStore DS;
	
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
			DB = new RMIDataBaseImpl(new DataBaseImpl());
			Naming.rebind("rmi://localhost:" + port + "/DataBase", DB);
			Core.Logger.log("DataBase service loaded.", Level.INFO);
			/*DB = new org.dyndns.doujindb.db.impl.DataBaseImpl();
			
			DB = (DataBase) Class.forName("org.dyndns.doujindb.db.impl.DataBaseImpl").newInstance();
			Naming.rebind("rmi://localhost:" + port + "/DataBase", DB);
			*/
			Core.Logger.log("DataBase service loaded.", Level.INFO);
			DS = new RMIDataStoreImpl(
					new DataStoreImpl(
						new java.io.File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString())
					)
				);
			Naming.rebind("rmi://localhost:" + port + "/DataStore", DS);
			Core.Logger.log("DataStore service loaded.", Level.INFO);
		} catch (DataBaseException dbe) {
			dbe.printStackTrace();
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (DataStoreException dse) {
			dse.printStackTrace();
		} catch (PropertyException pe) {
			pe.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		}
	}
}
