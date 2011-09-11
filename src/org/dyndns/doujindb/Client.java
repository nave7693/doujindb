package org.dyndns.doujindb;

import java.rmi.*;
import java.net.*;

import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.dat.impl.*;
import org.dyndns.doujindb.dat.rmi.*;
import org.dyndns.doujindb.db.*;

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
		return DB != null;
	}
	
	public static void connect() throws RemoteException
	{
		connect("localhost");
	}
	
	public static void connect(String host) throws RemoteException
	{
		connect(host, 1099);
	}
	
	public static void connect(String host, int port) throws RemoteException
	{
		if(DB != null)
			throw new RemoteException("Client already connected.");
        try { 
//        	DB = (DataBase)
//        					Naming.lookup(
//        							"rmi://" + host + ":" + port + "/DataBase");
        	DB = Server.DB;
        	
        	DS = new RemoteDataStore(
        			(RMIDataStore)
        					Naming.lookup(
        							"rmi://" + host + ":" + port + "/DataStore")
        						);
        } 
        catch (MalformedURLException murle) { 
            throw new RemoteException(murle.getMessage());
        } 
        catch (NotBoundException nbe) { 
        	throw new RemoteException(nbe.getMessage());
        } 
        catch (java.lang.ArithmeticException ae) { 
        	throw new RemoteException(ae.getMessage());
        }
	}
	
	public static void disconnect() throws RemoteException
	{
		if(DB == null)
			throw new RemoteException("Client not connected.");
		DB = null;
		DS = null;
	}
}
