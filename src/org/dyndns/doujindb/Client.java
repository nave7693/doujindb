package org.dyndns.doujindb;

import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.db.DataBaseException;

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
	
	public static void connect() throws DataBaseException
	{
		if(DB != null)
			throw new DataBaseException("Client already connected.");
		//TODO RMI Magic
		/*try
		{
			Class<?> clazz = Class.forName(DBType);
			DB = (DataBase) clazz.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new DataBaseException("Cannot connect to Client.DB '" + DBType + "' : Class not found.");
		} catch (InstantiationException ie) {
			throw new DataBaseException("Cannot connect to Client.DB '" + DBType + "' : Instantiation exception.");
		} catch (IllegalAccessException iae) {
			throw new DataBaseException("Cannot connect to Client.DB '" + DBType + "' : Illegal access exception.");
		} catch (ClassCastException cce) {
			throw new DataBaseException("Cannot connect to Client.DB '" + DBType + "' : Class cast exception.");
		}*/
	}
	
	public static void disconnect() throws DataBaseException
	{
		if(DB == null)
			throw new DataBaseException("Client not connected.");
		DB = null;
	}
}
