package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.rmi.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Content.java - Interface Content.
* @author nozomu
* @version 1.0
*/
public interface Content extends Record, Remote, Serializable, BookContainer
{
	public String getTagName() throws RemoteException;
	public String getInfo() throws RemoteException;
	public void setTagName(String tagName) throws RemoteException;
	public void setInfo(String info) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
}
