package org.dyndns.doujindb.dat;

import java.rmi.*;

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
