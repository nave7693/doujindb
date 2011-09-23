package org.dyndns.doujindb;


import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.dat.impl.*;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.impl.*;

/**  
* Client.java - DoujinDB Client.
* @author nozomu
* @version 1.0
*/
public final class Client
{
	public static DataBase DB;
	public static DataStore DS;
	
	public static boolean isConnected()
	{
		return DB != null && DS != null;
	}
	
	public static void connect() throws RuntimeException
	{
		connect("localhost");
	}
	
	public static void connect(String host) throws RuntimeException
	{
		connect(host, 1099);
	}
	
	public static void connect(String host, int port) throws RuntimeException
	{
		if(DB != null)
			throw new RuntimeException("Client already connected.");
		DB = new DataBaseImpl();
		DS = new DataStoreImpl(new java.io.File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString()));
//        try { 
////        	DB = new RemoteDataBase(
////        				(RMIDataBase)
////        					Naming.lookup(
////        							"rmi://" + host + ":" + port + "/DataBase")
////        						);
////        	DB = (DataBase)
////        					Naming.lookup(
////        							"rmi://" + host + ":" + port + "/DataBase");
//        	
//        	DS = new RemoteDataStore(
//        				(RMIDataStore)
//        					Naming.lookup(
//        							"rmi://" + host + ":" + port + "/DataStore")
//        						);
//        } 
//        catch (MalformedURLException murle) { 
//            throw new RuntimeException(murle.getMessage());
//        } 
//        catch (NotBoundException nbe) { 
//        	throw new RuntimeException(nbe.getMessage());
//        } 
//        catch (java.lang.ArithmeticException ae) { 
//        	throw new RuntimeException(ae.getMessage());
//        } catch (RemoteException re) {
//        	throw new RuntimeException(re.getMessage());
//		}
	}
	
	public static void disconnect() throws RuntimeException
	{
		if(DB == null || DS == null)
			throw new RuntimeException("Client not connected.");
		DB = null;
		DS = null;
	}
}
