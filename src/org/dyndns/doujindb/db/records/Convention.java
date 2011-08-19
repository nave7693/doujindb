package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.rmi.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Convention.java - Interface Convention.
* @author nozomu
* @version 1.0
*/
public interface Convention extends Record, Remote, Serializable, BookContainer
{
	public String getTagName() throws RemoteException;
	public String getInfo() throws RemoteException;
	public String getWeblink() throws RemoteException;
	public void setTagName(String tagName) throws RemoteException;
	public void setInfo(String info) throws RemoteException;
	public void setWeblink(String weblink) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
}
