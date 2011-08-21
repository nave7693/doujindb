package org.dyndns.doujindb.dat.rmi;

import java.rmi.*;

import org.dyndns.doujindb.dat.impl.StreamData;

/**  
* RMIInputStream.java - RMI InputStream.
* @author  nozomu
* @version 1.0
*/
public interface RMIInputStream extends Remote
{
	public int available() throws RemoteException;
	public void close() throws RemoteException;
	public void mark(int readlimit) throws RemoteException;
	public boolean markSupported() throws RemoteException;
	public int read() throws RemoteException;
	public StreamData read(int off, int len) throws RemoteException;
	public void reset() throws RemoteException;
	public long skip(long n) throws RemoteException;
}
